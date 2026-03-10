package com.group10.API_ManageDormitory.repository;

import com.group10.API_ManageDormitory.entity.Incident;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface IncidentRepository extends JpaRepository<Incident, Integer> {
    List<Incident> findByStatus(String status);
    List<Incident> findByRoom_RoomId(Integer roomId);
    List<Incident> findByTenant_TenantId(Integer tenantId);
}
