package com.group10.API_ManageDormitory.repository;

import com.group10.API_ManageDormitory.entity.Room;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RoomRepository extends JpaRepository<Room, Integer> {
    long countByCurrentStatus(String currentStatus);
    long countByFloor_Building_BuildingId(Integer buildingId);
    long countByCurrentStatusAndFloor_Building_BuildingId(String currentStatus, Integer buildingId);
    long countByFloor_Building_Manager_UserId(Integer managerId);
    long countByCurrentStatusAndFloor_Building_Manager_UserId(String status, Integer managerId);
    long countByFloor_Building_Owner_UserId(Integer ownerId);
    long countByCurrentStatusAndFloor_Building_Owner_UserId(String status, Integer ownerId);

    List<Room> findByFloor_Building_BuildingId(Integer buildingId);
    List<Room> findByFloor_Building_BuildingIdAndRoomType_RoomTypeId(Integer buildingId, Integer roomTypeId);
    boolean existsByRoomType_RoomTypeId(Integer roomTypeId);
    boolean existsByFloor_FloorId(Integer floorId);

    @Query("SELECT r.floor.building.buildingName, COUNT(r) FROM Room r GROUP BY r.floor.building.buildingName")
    List<Object[]> countRoomsByBuilding();

    @Query("SELECT r.floor.building.buildingName, COUNT(r) FROM Room r WHERE r.currentStatus = 'OCCUPIED' GROUP BY r.floor.building.buildingName")
    List<Object[]> countRentedRoomsByBuilding();

    @Query("SELECT r.floor.building.buildingName, COUNT(r) FROM Room r WHERE r.floor.building.manager.userId = :managerId GROUP BY r.floor.building.buildingName")
    List<Object[]> countRoomsByBuildingManager(@Param("managerId") Integer managerId);

    @Query("SELECT r.floor.building.buildingName, COUNT(r) FROM Room r WHERE r.currentStatus = 'OCCUPIED' AND r.floor.building.manager.userId = :managerId GROUP BY r.floor.building.buildingName")
    List<Object[]> countRentedRoomsByBuildingManager(@Param("managerId") Integer managerId);

    @Query("SELECT r.floor.building.buildingName, COUNT(r) FROM Room r WHERE r.floor.building.owner.userId = :ownerId GROUP BY r.floor.building.buildingName")
    List<Object[]> countRoomsByBuildingOwner(@Param("ownerId") Integer ownerId);

    @Query("SELECT r.floor.building.buildingName, COUNT(r) FROM Room r WHERE r.currentStatus = 'OCCUPIED' AND r.floor.building.owner.userId = :ownerId GROUP BY r.floor.building.buildingName")
    List<Object[]> countRentedRoomsByBuildingOwner(@Param("ownerId") Integer ownerId);
}
