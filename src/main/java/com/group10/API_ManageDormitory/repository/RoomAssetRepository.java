package com.group10.API_ManageDormitory.repository;

import com.group10.API_ManageDormitory.entity.RoomAsset;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RoomAssetRepository extends JpaRepository<RoomAsset, Integer> {
}
