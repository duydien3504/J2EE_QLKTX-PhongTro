package com.group10.API_ManageDormitory.service;

import com.group10.API_ManageDormitory.dtos.request.ContractRequest;
import com.group10.API_ManageDormitory.dtos.response.ContractResponse;
import com.group10.API_ManageDormitory.entity.*;
import com.group10.API_ManageDormitory.repository.ContractRepository;
import com.group10.API_ManageDormitory.repository.ContractTenantRepository;
import com.group10.API_ManageDormitory.repository.RoomRepository;
import com.group10.API_ManageDormitory.repository.TenantRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ContractServiceTest {
    @Mock
    private ContractRepository contractRepository;
    @Mock
    private ContractTenantRepository contractTenantRepository;
    @Mock
    private RoomRepository roomRepository;
    @Mock
    private TenantRepository tenantRepository;
    @InjectMocks
    private ContractService contractService;

    @Test
    void createContract_success() {
        ContractRequest request = ContractRequest.builder()
                .roomId(1)
                .representativeTenantId(1)
                .build();

        Room room = Room.builder().roomId(1).roomNumber("101").currentStatus("AVAILABLE").build();
        Tenant tenant = Tenant.builder().tenantId(1).build();
        Contract contract = Contract.builder().contractId(1).room(room).build();

        when(roomRepository.findById(1)).thenReturn(Optional.of(room));
        when(tenantRepository.findById(1)).thenReturn(Optional.of(tenant));
        when(contractRepository.save(any(Contract.class))).thenReturn(contract);
        when(contractTenantRepository.findByContract_ContractId(1)).thenReturn(Collections.emptyList());

        ContractResponse response = contractService.createContract(request);
        assertEquals(1, response.getContractId());
        assertEquals("OCCUPIED", room.getCurrentStatus());
    }

    @Test
    void terminateContract_success() {
        com.group10.API_ManageDormitory.dtos.request.TerminateRequest request = com.group10.API_ManageDormitory.dtos.request.TerminateRequest
                .builder()
                .deductionAmount(java.math.BigDecimal.ZERO)
                .build();

        Room room = Room.builder().roomId(1).currentStatus("OCCUPIED").build();
        Contract contract = Contract.builder()
                .contractId(1)
                .contractStatus("ACTIVE")
                .depositAmount(java.math.BigDecimal.TEN)
                .room(room)
                .build();

        when(contractRepository.findById(1)).thenReturn(Optional.of(contract));
        when(contractRepository.save(any(Contract.class))).thenReturn(contract);

        com.group10.API_ManageDormitory.dtos.response.LiquidationResponse response = contractService
                .terminateContract(1, request);
        assertEquals("AVAILABLE", room.getCurrentStatus());
        assertEquals("TERMINATED", contract.getContractStatus());
    }
}
