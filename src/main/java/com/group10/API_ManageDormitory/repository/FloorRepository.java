package com.group10.API_ManageDormitory.repository;

import com.group10.API_ManageDormitory.entity.Floor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.group10.API_ManageDormitory.entity.Building;
import java.util.List;

@Repository
public interface FloorRepository extends JpaRepository<Floor, Integer> {
    List<Floor> findByBuilding(Building building);
}
