package com.group10.API_ManageDormitory.repository;

import com.group10.API_ManageDormitory.entity.Room;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RoomRepository extends JpaRepository<Room, Integer> {
    List<Room> findAllByFloor_Building_Manager_Username(String username);
    boolean existsByRoomType_RoomTypeId(Integer roomTypeId);
}
