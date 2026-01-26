package com.group10.API_ManageDormitory.repository;

import com.group10.API_ManageDormitory.entity.BuildingService;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BuildingServiceRepository extends JpaRepository<BuildingService, Integer> {
    List<BuildingService> findByBuilding_BuildingId(Integer buildingId);

    Optional<BuildingService> findByBuilding_BuildingIdAndService_ServiceId(Integer buildingId, Integer serviceId);
}
