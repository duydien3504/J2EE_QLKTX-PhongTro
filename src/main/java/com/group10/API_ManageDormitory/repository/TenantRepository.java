package com.group10.API_ManageDormitory.repository;

import com.group10.API_ManageDormitory.entity.Tenant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TenantRepository extends JpaRepository<Tenant, Integer> {

    @Query("SELECT t FROM Tenant t WHERE " +
            "(:keyword IS NULL OR :keyword = '' OR " +
            "t.fullName LIKE %:keyword% OR " +
            "t.phoneNumber LIKE %:keyword% OR " +
            "t.identityCardNumber LIKE %:keyword%)")
    List<Tenant> searchTenants(@org.springframework.data.repository.query.Param("keyword") String keyword);
}
