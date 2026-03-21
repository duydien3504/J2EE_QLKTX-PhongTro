package com.group10.API_ManageDormitory.repository;

import com.group10.API_ManageDormitory.entity.RoomImage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RoomImageRepository extends JpaRepository<RoomImage, Integer> {

    // O(log n) - index-based query on room_id FK
    List<RoomImage> findByRoom_RoomId(Integer roomId);

    // O(1) - COUNT aggregate at DB level, no in-memory filtering
    long countByRoom_RoomId(Integer roomId);
}
