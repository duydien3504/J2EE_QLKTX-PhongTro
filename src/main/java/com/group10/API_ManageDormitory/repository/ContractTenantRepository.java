package com.group10.API_ManageDormitory.repository;

import com.group10.API_ManageDormitory.entity.ContractTenant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
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

    @Query("SELECT COUNT(DISTINCT ct.tenant.tenantId) FROM ContractTenant ct " +
            "WHERE ct.contract.contractStatus = 'ACTIVE' " +
            "AND ct.contract.isDeleted = false " +
            "AND (ct.contract.endDate IS NULL OR ct.contract.endDate >= CURRENT_DATE) " +
            "AND (:buildingId IS NULL OR ct.contract.room.floor.building.buildingId = :buildingId)")
    long countActiveTenants(@org.springframework.data.repository.query.Param("buildingId") Integer buildingId);

    @Query("SELECT COUNT(DISTINCT ct.tenant.tenantId) FROM ContractTenant ct " +
            "WHERE ct.contract.contractStatus = 'ACTIVE' " +
            "AND ct.contract.isDeleted = false " +
            "AND (ct.contract.endDate IS NULL OR ct.contract.endDate >= CURRENT_DATE) " +
            "AND (:buildingId IS NULL OR ct.contract.room.floor.building.buildingId = :buildingId) " +
            "AND (:isAdmin = true OR ct.contract.room.floor.building.manager.userId = :userId OR ct.contract.room.floor.building.owner.userId = :userId)")
    long countActiveTenantsByUser(@org.springframework.data.repository.query.Param("buildingId") Integer buildingId, 
                                @org.springframework.data.repository.query.Param("userId") Integer userId, 
                                @org.springframework.data.repository.query.Param("isAdmin") boolean isAdmin);

    boolean existsByTenant_TenantIdAndContract_Room_RoomIdAndContract_ContractStatusAndContract_IsDeletedFalse(
            Integer tenantId, Integer roomId, String status);
}
