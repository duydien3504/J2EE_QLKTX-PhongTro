package com.group10.API_ManageDormitory.repository;

import com.group10.API_ManageDormitory.entity.Incident;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface IncidentRepository extends JpaRepository<Incident, Integer> {
    @EntityGraph(attributePaths = {"room", "tenant"})
    List<Incident> findByStatus(String status);
    
    @EntityGraph(attributePaths = {"room", "tenant"})
    List<Incident> findByRoom_RoomId(Integer roomId);
    
    @EntityGraph(attributePaths = {"room", "tenant"})
    List<Incident> findByTenant_TenantId(Integer tenantId);

    @Override
    @EntityGraph(attributePaths = {"room", "tenant"})
    List<Incident> findAll();
}
