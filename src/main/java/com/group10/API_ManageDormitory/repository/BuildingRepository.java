package com.group10.API_ManageDormitory.repository;

import com.group10.API_ManageDormitory.entity.Building;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BuildingRepository extends JpaRepository<Building, Integer> {
    java.util.List<Building> findByManager_UserId(Integer userId);
    java.util.List<Building> findByOwner_UserId(Integer userId);
    java.util.List<Building> findByManager_UserIdOrOwner_UserId(Integer managerId, Integer ownerId);
    boolean existsByBuildingIdAndManager_UserId(Integer buildingId, Integer userId);
    boolean existsByBuildingIdAndOwner_UserId(Integer buildingId, Integer userId);
}
