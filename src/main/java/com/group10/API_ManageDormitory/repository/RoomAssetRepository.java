package com.group10.API_ManageDormitory.repository;

import com.group10.API_ManageDormitory.entity.RoomAsset;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RoomAssetRepository extends JpaRepository<RoomAsset, Integer> {
    List<RoomAsset> findByRoom_RoomId(Integer roomId);

    // O(1) - DB-level EXISTS check, no full table scan
    boolean existsByAsset_AssetId(Integer assetId);
}

