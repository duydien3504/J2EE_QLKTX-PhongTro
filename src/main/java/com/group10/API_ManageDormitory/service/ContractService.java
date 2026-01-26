package com.group10.API_ManageDormitory.service;

import com.group10.API_ManageDormitory.dtos.request.ContractRequest;
import com.group10.API_ManageDormitory.dtos.request.MemberRequest;
import com.group10.API_ManageDormitory.dtos.request.TerminateRequest;
import com.group10.API_ManageDormitory.dtos.response.ContractMemberResponse;
import com.group10.API_ManageDormitory.dtos.response.ContractResponse;
import com.group10.API_ManageDormitory.dtos.response.LiquidationResponse;
import com.group10.API_ManageDormitory.entity.*;
import com.group10.API_ManageDormitory.exception.AppException;
import com.group10.API_ManageDormitory.exception.ErrorCode;
import com.group10.API_ManageDormitory.repository.ContractRepository;
import com.group10.API_ManageDormitory.repository.ContractTenantRepository;
import com.group10.API_ManageDormitory.repository.RoomRepository;
import com.group10.API_ManageDormitory.repository.TenantRepository;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ContractService {
    private final ContractRepository contractRepository;
    private final ContractTenantRepository contractTenantRepository;
    private final RoomRepository roomRepository;
    private final TenantRepository tenantRepository;

    @Transactional(readOnly = true)
    public List<ContractResponse> getContracts(String status) {
        // Simple filtering in stream
        return contractRepository.findAll().stream()
                .filter(c -> {
                    if (status == null || status.isEmpty())
                        return true;
                    if ("EXPIRED".equalsIgnoreCase(status)) {
                        return c.getEndDate() != null && c.getEndDate().isBefore(LocalDate.now());
                    }
                    if ("VALID".equalsIgnoreCase(status)) {
                        return "ACTIVE".equalsIgnoreCase(c.getContractStatus())
                                && (c.getEndDate() == null || !c.getEndDate().isBefore(LocalDate.now()));
                    }
                    return status.equalsIgnoreCase(c.getContractStatus());
                })
                .map(this::toContractResponse)
                .collect(Collectors.toList());
    }

    public ContractResponse getContract(Integer id) {
        Contract contract = contractRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.CONTRACT_NOT_FOUND));
        return toContractResponse(contract);
    }

    @Transactional
    public ContractResponse createContract(ContractRequest request) {
        Room room = roomRepository.findById(request.getRoomId())
                .orElseThrow(() -> new AppException(ErrorCode.ROOM_NOT_FOUND));

        if (!"AVAILABLE".equalsIgnoreCase(room.getCurrentStatus())) {
            throw new AppException(ErrorCode.ROOM_NOT_AVAILABLE);
        }

        Tenant tenant = tenantRepository.findById(request.getRepresentativeTenantId())
                .orElseThrow(() -> new AppException(ErrorCode.TENANT_NOT_FOUND));

        Contract contract = Contract.builder()
                .room(room)
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .rentalPrice(request.getRentalPrice())
                .depositAmount(request.getDepositAmount())
                .paymentCycle(request.getPaymentCycle())
                .contractStatus("ACTIVE")
                .build();

        contract = contractRepository.save(contract);

        ContractTenant link = ContractTenant.builder()
                .contract(contract)
                .tenant(tenant)
                .isRepresentative(true)
                .build();
        contractTenantRepository.save(link);

        room.setCurrentStatus("OCCUPIED");
        roomRepository.save(room);

        return toContractResponse(contract);
    }

    @Transactional
    public ContractResponse updateContract(Integer id, ContractRequest request) {
        Contract contract = contractRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.CONTRACT_NOT_FOUND));

        if (request.getRentalPrice() != null)
            contract.setRentalPrice(request.getRentalPrice());
        if (request.getEndDate() != null)
            contract.setEndDate(request.getEndDate());
        if (request.getDepositAmount() != null)
            contract.setDepositAmount(request.getDepositAmount());

        return toContractResponse(contractRepository.save(contract));
    }

    @Transactional
    public ContractResponse addMember(Integer contractId, MemberRequest request) {
        Contract contract = contractRepository.findById(contractId)
                .orElseThrow(() -> new AppException(ErrorCode.CONTRACT_NOT_FOUND));

        Tenant tenant = tenantRepository.findById(request.getTenantId())
                .orElseThrow(() -> new AppException(ErrorCode.TENANT_NOT_FOUND));

        List<ContractTenant> members = contractTenantRepository.findByContract_ContractId(contractId);
        Room room = contract.getRoom();
        if (room.getRoomType().getMaxOccupancy() != null && members.size() >= room.getRoomType().getMaxOccupancy()) {
            throw new AppException(ErrorCode.OCCUPANCY_LIMIT_REACHED);
        }

        // Check if already in contract
        boolean exists = members.stream().anyMatch(m -> m.getTenant().getTenantId().equals(request.getTenantId()));
        if (exists) {
            throw new AppException(ErrorCode.TENANT_ALREADY_IN_CONTRACT);
        }

        ContractTenant link = ContractTenant.builder()
                .contract(contract)
                .tenant(tenant)
                .isRepresentative(false)
                .build();
        contractTenantRepository.save(link);

        return toContractResponse(contract);
    }

    @Transactional
    public void removeMember(Integer contractId, Integer tenantId) {
        List<ContractTenant> members = contractTenantRepository.findByContract_ContractId(contractId);
        ContractTenant target = members.stream()
                .filter(m -> m.getTenant().getTenantId().equals(tenantId))
                .findFirst()
                .orElseThrow(() -> new AppException(ErrorCode.TENANT_NOT_FOUND)); // Use better code if possible

        if (Boolean.TRUE.equals(target.getIsRepresentative())) {
            throw new RuntimeException("Cannot remove representative tenant. Update contract instead.");
        }

        contractTenantRepository.delete(target);
    }

    @Transactional
    public LiquidationResponse terminateContract(Integer id, TerminateRequest request) {
        Contract contract = contractRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.CONTRACT_NOT_FOUND));

        if (!"ACTIVE".equalsIgnoreCase(contract.getContractStatus())) {
            throw new RuntimeException("Contract is not ACTIVE");
        }

        BigDecimal deduction = request.getDeductionAmount() != null ? request.getDeductionAmount() : BigDecimal.ZERO;
        BigDecimal deposit = contract.getDepositAmount() != null ? contract.getDepositAmount() : BigDecimal.ZERO;

        if (deduction.compareTo(deposit) > 0) {
            throw new RuntimeException("Deduction amount cannot exceed deposit amount");
        }

        BigDecimal refund = deposit.subtract(deduction);

        contract.setContractStatus("TERMINATED");
        contract.setLiquidationDate(LocalDate.now());
        contract.setDeductionAmount(deduction);
        contract.setRefundAmount(refund);
        contract.setDeductionReason(request.getDeductionReason());

        contractRepository.save(contract);

        // Free the room
        Room room = contract.getRoom();
        room.setCurrentStatus("AVAILABLE");
        roomRepository.save(room);

        return toLiquidationResponse(contract);
    }

    public LiquidationResponse getLiquidation(Integer id) {
        Contract contract = contractRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.CONTRACT_NOT_FOUND));

        return toLiquidationResponse(contract);
    }

    private LiquidationResponse toLiquidationResponse(Contract contract) {
        return LiquidationResponse.builder()
                .contractId(contract.getContractId())
                .liquidationDate(contract.getLiquidationDate())
                .depositAmount(contract.getDepositAmount())
                .deductionAmount(contract.getDeductionAmount())
                .refundAmount(contract.getRefundAmount())
                .deductionReason(contract.getDeductionReason())
                .contractStatus(contract.getContractStatus())
                .build();
    }

    public void downloadContract(Integer id, HttpServletResponse response) throws IOException {
        Contract contract = contractRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.CONTRACT_NOT_FOUND));
        ContractResponse dto = toContractResponse(contract);

        XWPFDocument document = new XWPFDocument();
        XWPFParagraph title = document.createParagraph();
        XWPFRun run = title.createRun();
        run.setText("HOP DONG THUE TRO");
        run.setBold(true);
        run.setFontSize(20);

        XWPFParagraph body = document.createParagraph();
        XWPFRun runBody = body.createRun();
        runBody.setText("Contract ID: " + dto.getContractId());
        runBody.addBreak();
        runBody.setText("Room: " + dto.getRoomNumber());
        runBody.addBreak();
        runBody.setText("Price: " + dto.getRentalPrice());
        runBody.addBreak();
        runBody.setText("Date: " + dto.getStartDate() + " to " + dto.getEndDate());
        runBody.addBreak();
        runBody.setText("Representative: " + getRepresentativeName(dto.getMembers()));

        response.setContentType("application/vnd.openxmlformats-officedocument.wordprocessingml.document");
        response.setHeader("Content-Disposition", "attachment; filename=contract_" + id + ".docx");

        document.write(response.getOutputStream());
        document.close();
    }

    private String getRepresentativeName(List<ContractMemberResponse> members) {
        return members.stream().filter(m -> Boolean.TRUE.equals(m.getIsRepresentative()))
                .map(ContractMemberResponse::getFullName).findFirst().orElse("Unknown");
    }

    private ContractResponse toContractResponse(Contract contract) {
        List<ContractTenant> members = contractTenantRepository.findByContract_ContractId(contract.getContractId());

        List<ContractMemberResponse> memberDtos = members.stream().map(m -> ContractMemberResponse.builder()
                .contractTenantId(m.getContractTenantId())
                .tenantId(m.getTenant().getTenantId())
                .fullName(m.getTenant().getFullName())
                .phoneNumber(m.getTenant().getPhoneNumber())
                .isRepresentative(m.getIsRepresentative())
                .build()).collect(Collectors.toList());

        return ContractResponse.builder()
                .contractId(contract.getContractId())
                .roomId(contract.getRoom().getRoomId())
                .roomNumber(contract.getRoom().getRoomNumber())
                .rentalPrice(contract.getRentalPrice())
                .depositAmount(contract.getDepositAmount())
                .startDate(contract.getStartDate())
                .endDate(contract.getEndDate())
                .contractStatus(contract.getContractStatus())
                .members(memberDtos)
                .build();
    }
}
