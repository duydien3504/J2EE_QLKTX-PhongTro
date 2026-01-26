package com.group10.API_ManageDormitory.repository;

import com.group10.API_ManageDormitory.entity.ContractTenant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ContractTenantRepository extends JpaRepository<ContractTenant, Integer> {
}
