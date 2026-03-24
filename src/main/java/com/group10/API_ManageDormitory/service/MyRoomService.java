package com.group10.API_ManageDormitory.service;

import com.group10.API_ManageDormitory.dtos.response.*;
import com.group10.API_ManageDormitory.entity.*;
import com.group10.API_ManageDormitory.exception.AppException;
import com.group10.API_ManageDormitory.exception.ErrorCode;
import com.group10.API_ManageDormitory.repository.*;
import com.group10.API_ManageDormitory.utils.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MyRoomService {
    private final UserRepository userRepository;
    private final TenantRepository tenantRepository;
    private final ContractTenantRepository contractTenantRepository;
    private final BuildingServiceRepository buildingServiceRepository;
    private final InvoiceRepository invoiceRepository;

    public MyRoomResponse getMyRoom() {
        String username = SecurityUtils.getCurrentUsername();
        if (username == null) throw new AppException(ErrorCode.UNAUTHENTICATED);

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

        Tenant tenant = tenantRepository.findByUser_UserId(user.getUserId())
                .orElseThrow(() -> new AppException(ErrorCode.TENANT_NOT_FOUND));

        // Find active contract for this tenant
        // Try ACTIVE first, then WAITING_DEPOSIT
        List<ContractTenant> activeLinks = contractTenantRepository.findByTenant_TenantIdAndContract_ContractStatusAndContract_IsDeletedFalse(tenant.getTenantId(), "ACTIVE");
        if (activeLinks.isEmpty()) {
            activeLinks = contractTenantRepository.findByTenant_TenantIdAndContract_ContractStatusAndContract_IsDeletedFalse(tenant.getTenantId(), "WAITING_DEPOSIT");
        }

        if (activeLinks.isEmpty()) {
            return MyRoomResponse.builder().build(); // No active room
        }

        Contract contract = activeLinks.get(0).getContract();
        Room room = contract.getRoom();

        // Get Roommates
        List<TenantResponse> roommates = contractTenantRepository.findByContract_ContractIdAndContract_IsDeletedFalse(contract.getContractId())
                .stream()
                .map(link -> toTenantResponse(link.getTenant()))
                .collect(Collectors.toList());

        // Get Services
        List<BuildingServiceResponse> services = buildingServiceRepository.findByBuilding_BuildingId(room.getFloor().getBuilding().getBuildingId())
                .stream()
                .map(this::toBuildingServiceResponse)
                .collect(Collectors.toList());

        // Get Recent Invoices
        List<InvoiceResponse> recentInvoices = invoiceRepository.findByContract_ContractId(contract.getContractId())
                .stream()
                .map(this::toInvoiceResponse)
                .sorted((a, b) -> b.getInvoiceId().compareTo(a.getInvoiceId()))
                .limit(10)
                .collect(Collectors.toList());

        return MyRoomResponse.builder()
                .room(toRoomResponse(room))
                .contract(toContractResponse(contract))
                .roommates(roommates)
                .services(services)
                .recentInvoices(recentInvoices)
                .build();
    }

    private TenantResponse toTenantResponse(Tenant tenant) {
        return TenantResponse.builder()
                .tenantId(tenant.getTenantId())
                .fullName(tenant.getFullName())
                .phoneNumber(tenant.getPhoneNumber())
                .email(tenant.getEmail())
                .identityCardNumber(tenant.getIdentityCardNumber())
                .build();
    }

    private BuildingServiceResponse toBuildingServiceResponse(com.group10.API_ManageDormitory.entity.BuildingService bs) {
        return BuildingServiceResponse.builder()
                .buildingServiceId(bs.getBuildingServiceId())
                .buildingId(bs.getBuilding().getBuildingId())
                .serviceId(bs.getService().getServiceId())
                .serviceName(bs.getService().getServiceName())
                .unit(bs.getService().getUnit())
                .unitPrice(bs.getUnitPrice())
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
                .paymentMethod(invoice.getPaymentMethod())
                .lastTransactionStatus(invoice.getLastTransactionStatus())
                .notes(invoice.getNotes())
                .build();
    }

    private RoomResponse toRoomResponse(Room room) {
        return RoomResponse.builder()
                .roomId(room.getRoomId())
                .roomNumber(room.getRoomNumber())
                .currentStatus(room.getCurrentStatus())
                .roomTypeName(room.getRoomType().getTypeName())
                .price(room.getRoomType().getBasePrice())
                .buildingName(room.getFloor().getBuilding().getBuildingName())
                .floorName(room.getFloor().getFloorName())
                .build();
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
