package com.group10.API_ManageDormitory.repository;

import com.group10.API_ManageDormitory.entity.ContractTenant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ContractTenantRepository extends JpaRepository<ContractTenant, Integer> {
    List<ContractTenant> findByContract_ContractId(Integer contractId);
    java.util.Optional<ContractTenant> findByTenant_TenantId(Integer tenantId);
}
