package com.group10.API_ManageDormitory.repository;

import com.group10.API_ManageDormitory.entity.Room;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RoomRepository extends JpaRepository<Room, Integer> {
    long countByCurrentStatus(String currentStatus);

    List<Room> findAllByFloor_Building_Manager_Username(String username);
    boolean existsByRoomType_RoomTypeId(Integer roomTypeId);

    @Query("SELECT r.floor.building.buildingName, COUNT(r) FROM Room r GROUP BY r.floor.building.buildingName")
    List<Object[]> countRoomsByBuilding();

    @Query("SELECT r.floor.building.buildingName, COUNT(r) FROM Room r WHERE r.currentStatus = 'RENTED' GROUP BY r.floor.building.buildingName")
    List<Object[]> countRentedRoomsByBuilding();
}

