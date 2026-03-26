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
import com.group10.API_ManageDormitory.repository.*;
import com.group10.API_ManageDormitory.utils.SecurityUtils;
import com.group10.API_ManageDormitory.dtos.request.RoomRegistrationRequest;
import com.group10.API_ManageDormitory.dtos.response.ContractRegistrationResponse;
import com.group10.API_ManageDormitory.dtos.response.InvoiceResponse;
import com.group10.API_ManageDormitory.utils.momo.MoMoResponse;
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
    private final UserRepository userRepository;
    private final InvoiceRepository invoiceRepository;
    private final MoMoService moMoService;
    private final NotificationService notificationService;

    @Transactional(readOnly = true)
    public List<ContractResponse> getContracts(String status) {
        String username = SecurityUtils.getCurrentUsername();
        if (username == null) throw new AppException(ErrorCode.UNAUTHENTICATED);
        User currentUser = userRepository.findByUsername(username)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

        String role = currentUser.getRole() != null ? currentUser.getRole().getRoleName() : "";
        boolean isAdmin = "ADMIN".equalsIgnoreCase(role);

        return contractRepository.findAllByIsDeletedFalse().stream()
                .filter(c -> {
                    // Security filter: Owners/Staff only see their own buildings
                    if (!isAdmin) {
                        Building b = c.getRoom().getFloor().getBuilding();
                        if (b.getManager() == null || !b.getManager().getUserId().equals(currentUser.getUserId())) {
                            return false;
                        }
                    }

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
        Contract contract = contractRepository.findByContractIdAndIsDeletedFalse(id)
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

        // Notify Tenant about new contract
        if (tenant.getUser() != null) {
            notificationService.createNotification(com.group10.API_ManageDormitory.dtos.request.NotificationRequest.builder()
                    .title("Hợp động thuê phòng mới - " + room.getRoomNumber())
                    .content("Hệ thống đã tạo hợp đồng thuê phòng cho bạn. Vui lòng kiểm tra chi tiết trong phần Hợp đồng.")
                    .type("CONTRACT")
                    .userIds(java.util.List.of(tenant.getUser().getUserId()))
                    .build());
        }

        room.setCurrentStatus("OCCUPIED");
        roomRepository.save(room);

        return toContractResponse(contract);
    }

    @Transactional
    public ContractRegistrationResponse registerContract(RoomRegistrationRequest request) {
        String username = SecurityUtils.getCurrentUsername();
        if (username == null) throw new AppException(ErrorCode.UNAUTHENTICATED);

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

        Room room = roomRepository.findById(request.getRoomId())
                .orElseThrow(() -> new AppException(ErrorCode.ROOM_NOT_FOUND));

        if (!"AVAILABLE".equalsIgnoreCase(room.getCurrentStatus())) {
            throw new AppException(ErrorCode.ROOM_NOT_AVAILABLE);
        }

        // Find or create Tenant for this user
        Tenant tenant = tenantRepository.findByUser_UserId(user.getUserId())
                .orElseGet(() -> {
                    Tenant newTenant = Tenant.builder()
                            .user(user)
                            .fullName(user.getFullName() != null ? user.getFullName() : user.getUsername())
                            .email(user.getEmail())
                            .build();
                    return tenantRepository.save(newTenant);
                });

        // Create Contract (Status: WAITING_DEPOSIT)
        Contract contract = Contract.builder()
                .room(room)
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .rentalPrice(room.getRoomType().getBasePrice())
                .depositAmount(room.getRoomType().getBasePrice()) // Default 1 month deposit
                .paymentCycle(1)
                .contractStatus("WAITING_DEPOSIT")
                .build();

        contract = contractRepository.save(contract);

        // Link Tenant to Contract
        ContractTenant link = ContractTenant.builder()
                .contract(contract)
                .tenant(tenant)
                .isRepresentative(true)
                .build();
        contractTenantRepository.save(link);

        // Create Deposit Invoice
        Invoice depositInvoice = Invoice.builder()
                .contract(contract)
                .month(request.getStartDate().getMonthValue())
                .year(request.getStartDate().getYear())
                .createdDate(LocalDate.now())
                .dueDate(LocalDate.now().plusDays(3))
                .totalAmount(contract.getDepositAmount())
                .paymentStatus("PENDING")
                .paymentMethod(request.getPaymentMethod())
                .notes("Tiền cọc giữ phòng " + room.getRoomNumber())
                .build();

        depositInvoice = invoiceRepository.save(depositInvoice);

        // Notify Tenant about registration success
        if (user != null) {
            notificationService.createNotification(com.group10.API_ManageDormitory.dtos.request.NotificationRequest.builder()
                    .title("Đăng ký thuê phòng thành công")
                    .content("Bạn đã đăng ký thuê phòng " + room.getRoomNumber() + " thành công. Vui lòng hoàn tất thanh toán tiền cọc để giữ phòng.")
                    .type("REGISTRATION")
                    .userIds(java.util.List.of(user.getUserId()))
                    .build());
        }

        // If MoMo, generate payUrl
        String payUrl = null;
        if ("MOMO".equalsIgnoreCase(request.getPaymentMethod())) {
            try {
                MoMoResponse moMoResponse = 
                    moMoService.createPayment(depositInvoice.getInvoiceId());
                payUrl = moMoResponse.getPayUrl();
            } catch (Exception e) {
                System.err.println("MoMo payment initiation failed: " + e.getMessage());
            }
        }

        // Lock room status
        room.setCurrentStatus("OCCUPIED");
        roomRepository.save(room);

        return ContractRegistrationResponse.builder()
                .contract(toContractResponse(contract))
                .depositInvoice(toInvoiceResponse(depositInvoice))
                .payUrl(payUrl)
                .build();
    }

    private InvoiceResponse toInvoiceResponse(Invoice invoice) {
        if (invoice == null) return null;

        InvoiceResponse.RoomSummary roomSummary = null;
        if (invoice.getContract() != null && invoice.getContract().getRoom() != null) {
            roomSummary = InvoiceResponse.RoomSummary.builder()
                    .roomId(invoice.getContract().getRoom().getRoomId())
                    .roomNumber(invoice.getContract().getRoom().getRoomNumber())
                    .build();
        }

        InvoiceResponse.ContractSummary contractSummary = null;
        if (invoice.getContract() != null) {
            contractSummary = InvoiceResponse.ContractSummary.builder()
                    .contractId(invoice.getContract().getContractId())
                    .room(roomSummary)
                    .build();
        }

        return InvoiceResponse.builder()
                .invoiceId(invoice.getInvoiceId())
                .contract(contractSummary)
                .month(invoice.getMonth())
                .year(invoice.getYear())
                .createdDate(invoice.getCreatedDate())
                .dueDate(invoice.getDueDate())
                .totalAmount(invoice.getTotalAmount())
                .paymentStatus(invoice.getPaymentStatus())
                .notes(invoice.getNotes())
                .build();
    }


    @Transactional
    public ContractResponse updateContract(Integer id, ContractRequest request) {
        Contract contract = contractRepository.findByContractIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new AppException(ErrorCode.CONTRACT_NOT_FOUND));

        checkContractAccess(contract);

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
        Contract contract = contractRepository.findByContractIdAndIsDeletedFalse(contractId)
                .orElseThrow(() -> new AppException(ErrorCode.CONTRACT_NOT_FOUND));

        checkContractAccess(contract);

        Tenant tenant = tenantRepository.findById(request.getTenantId())
                .orElseThrow(() -> new AppException(ErrorCode.TENANT_NOT_FOUND));

        List<ContractTenant> members = contractTenantRepository.findByContract_ContractIdAndContract_IsDeletedFalse(contractId);
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
    public void deleteContract(Integer id) {
        Contract contract = contractRepository.findByContractIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new AppException(ErrorCode.CONTRACT_NOT_FOUND));

        checkContractAccess(contract);

        // Nếu hợp đồng đang ACTIVE hoặc WAITING_DEPOSIT, giải phóng phòng
        if ("ACTIVE".equalsIgnoreCase(contract.getContractStatus()) || 
            "WAITING_DEPOSIT".equalsIgnoreCase(contract.getContractStatus())) {
            Room room = contract.getRoom();
            room.setCurrentStatus("AVAILABLE");
            roomRepository.save(room);
        }

        // Hibernate @SQLDelete will handle the is_deleted = true
        contractRepository.delete(contract);
    }

    @Transactional
    public void cancelRegistration(Integer id) {
        String username = SecurityUtils.getCurrentUsername();
        if (username == null) throw new AppException(ErrorCode.UNAUTHENTICATED);

        Contract contract = contractRepository.findByContractIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new AppException(ErrorCode.CONTRACT_NOT_FOUND));

        // Check if user is the representative of this contract
        boolean isMine = contractTenantRepository.findByContract_ContractIdAndContract_IsDeletedFalse(id)
                .stream()
                .anyMatch(link -> Boolean.TRUE.equals(link.getIsRepresentative()) && 
                        link.getTenant().getUser() != null && 
                        link.getTenant().getUser().getUsername().equals(username));

        if (!isMine) {
            throw new AppException(ErrorCode.UNAUTHORIZED);
        }

        if (!"WAITING_DEPOSIT".equalsIgnoreCase(contract.getContractStatus())) {
            throw new RuntimeException("Chỉ có thể hủy đăng ký khi đang chờ đặt cọc.");
        }

        // Release room
        Room room = contract.getRoom();
        room.setCurrentStatus("AVAILABLE");
        roomRepository.save(room);

        // Delete ContractTenant links first to avoid Hibernate transient reference error
        List<ContractTenant> members = contractTenantRepository.findByContract_ContractIdAndContract_IsDeletedFalse(id);
        contractTenantRepository.deleteAll(members);

        contractRepository.delete(contract);
    }

    @Transactional
    public void removeMember(Integer contractId, Integer tenantId) {
        Contract contract = contractRepository.findByContractIdAndIsDeletedFalse(contractId)
                .orElseThrow(() -> new AppException(ErrorCode.CONTRACT_NOT_FOUND));

        checkContractAccess(contract);

        List<ContractTenant> members = contractTenantRepository.findByContract_ContractIdAndContract_IsDeletedFalse(contractId);
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
        Contract contract = contractRepository.findByContractIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new AppException(ErrorCode.CONTRACT_NOT_FOUND));

        checkContractAccess(contract);

        if (!"ACTIVE".equalsIgnoreCase(contract.getContractStatus())) {
            throw new RuntimeException("Contract is not ACTIVE");
        }

        BigDecimal deduction = request.getDeductionAmount() != null ? request.getDeductionAmount() : BigDecimal.ZERO;
        BigDecimal deposit = contract.getDepositAmount() != null ? contract.getDepositAmount() : BigDecimal.ZERO;

        if (deduction.compareTo(BigDecimal.ZERO) > 0 && 
            (request.getDeductionReason() == null || request.getDeductionReason().isBlank())) {
            throw new AppException(ErrorCode.DEDUCTION_REASON_REQUIRED);
        }

        // Allowing deduction > deposit for large debts/damages
        BigDecimal refund = deposit.subtract(deduction);

        contract.setContractStatus("TERMINATED");
        contract.setLiquidationDate(LocalDate.now());
        contract.setDeductionAmount(deduction);
        contract.setRefundAmount(refund);
        contract.setDeductionReason(request.getDeductionReason());
        contract.setFinalElectricityReading(request.getFinalElectricityReading());
        contract.setFinalWaterReading(request.getFinalWaterReading());

        contractRepository.save(contract);

        // Free the room
        Room room = contract.getRoom();
        room.setCurrentStatus("AVAILABLE");
        roomRepository.save(room);

        return toLiquidationResponse(contract);
    }

    public LiquidationResponse getLiquidation(Integer id) {
        Contract contract = contractRepository.findByContractIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new AppException(ErrorCode.CONTRACT_NOT_FOUND));

        checkContractAccess(contract);

        return toLiquidationResponse(contract);
    }

    private void checkContractAccess(Contract contract) {
        String username = SecurityUtils.getCurrentUsername();
        if (username == null) throw new AppException(ErrorCode.UNAUTHENTICATED);

        User currentUser = userRepository.findByUsername(username)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

        String role = currentUser.getRole() != null ? currentUser.getRole().getRoleName() : "";
        boolean isAdmin = "ADMIN".equalsIgnoreCase(role);

        if (!isAdmin) {
            if ("TENANT".equalsIgnoreCase(role)) {
                // Check if tenant is in this contract
                boolean isMember = contractTenantRepository.findByContract_ContractIdAndContract_IsDeletedFalse(contract.getContractId())
                        .stream().anyMatch(ct -> ct.getTenant().getUser() != null && 
                                               ct.getTenant().getUser().getUserId().equals(currentUser.getUserId()));
                if (!isMember) throw new AppException(ErrorCode.ACCESS_DENIED_TO_RESOURCE);
            } else {
                // For OWNER or STAFF, check building management
                Building building = contract.getRoom().getFloor().getBuilding();
                if (building.getManager() == null || !building.getManager().getUserId().equals(currentUser.getUserId())) {
                    throw new AppException(ErrorCode.ACCESS_DENIED_TO_RESOURCE);
                }
            }
        }
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
                .finalElectricityReading(contract.getFinalElectricityReading())
                .finalWaterReading(contract.getFinalWaterReading())
                .build();
    }

    public void downloadContract(Integer id, HttpServletResponse response) throws IOException {
        Contract contract = contractRepository.findByContractIdAndIsDeletedFalse(id)
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
        List<ContractTenant> members = contractTenantRepository.findByContract_ContractIdAndContract_IsDeletedFalse(contract.getContractId());

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
