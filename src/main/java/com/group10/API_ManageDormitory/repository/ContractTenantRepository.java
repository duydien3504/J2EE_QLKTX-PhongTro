package com.group10.API_ManageDormitory.repository;

import com.group10.API_ManageDormitory.entity.ContractTenant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ContractTenantRepository extends JpaRepository<ContractTenant, Integer> {
    // Original names for compatibility
    List<ContractTenant> findByContract_ContractId(Integer contractId);
    java.util.Optional<ContractTenant> findByTenant_TenantId(Integer tenantId);
    java.util.List<ContractTenant> findByTenant_TenantIdAndContract_ContractStatus(Integer tenantId, String status);

    // Filtered names for active logic
    List<ContractTenant> findByContract_ContractIdAndContract_IsDeletedFalse(Integer contractId);
    java.util.Optional<ContractTenant> findByTenant_TenantIdAndContract_IsDeletedFalse(Integer tenantId);
    java.util.List<ContractTenant> findByTenant_TenantIdAndContract_ContractStatusAndContract_IsDeletedFalse(Integer tenantId, String status);
}
