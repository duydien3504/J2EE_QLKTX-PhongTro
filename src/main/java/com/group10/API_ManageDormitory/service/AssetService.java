package com.group10.API_ManageDormitory.service;

import com.group10.API_ManageDormitory.dtos.request.AssetRequest;
import com.group10.API_ManageDormitory.dtos.request.BulkRoomAssetRequest;
import com.group10.API_ManageDormitory.dtos.request.RoomAssetRequest;
import com.group10.API_ManageDormitory.dtos.response.AssetResponse;
import com.group10.API_ManageDormitory.dtos.response.RoomAssetResponse;
import com.group10.API_ManageDormitory.entity.Asset;
import com.group10.API_ManageDormitory.entity.Room;
import com.group10.API_ManageDormitory.entity.RoomAsset;
import com.group10.API_ManageDormitory.exception.AppException;
import com.group10.API_ManageDormitory.exception.ErrorCode;
import com.group10.API_ManageDormitory.repository.AssetRepository;
import com.group10.API_ManageDormitory.repository.RoomAssetRepository;
import com.group10.API_ManageDormitory.repository.RoomRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AssetService {
    private final AssetRepository assetRepository;
    private final RoomAssetRepository roomAssetRepository;
    private final RoomRepository roomRepository;

    // Assets
    public List<AssetResponse> getAllAssets() {
        return assetRepository.findAll().stream()
                .map(this::toAssetResponse)
                .collect(Collectors.toList());
    }

    public AssetResponse createAsset(AssetRequest request) {
        Asset asset = Asset.builder()
                .assetName(request.getAssetName())
                .assetCode(request.getAssetCode())
                .purchasePrice(request.getPurchasePrice())
                .purchaseDate(request.getPurchaseDate() != null ? request.getPurchaseDate() : LocalDate.now())
                .build();
        return toAssetResponse(assetRepository.save(asset));
    }

    public AssetResponse updateAsset(Integer assetId, AssetRequest request) {
        Asset asset = assetRepository.findById(assetId)
                .orElseThrow(() -> new AppException(ErrorCode.ASSET_NOT_FOUND));

        if (request.getAssetName() != null)
            asset.setAssetName(request.getAssetName());
        if (request.getAssetCode() != null)
            asset.setAssetCode(request.getAssetCode());
        if (request.getPurchasePrice() != null)
            asset.setPurchasePrice(request.getPurchasePrice());
        if (request.getPurchaseDate() != null)
            asset.setPurchaseDate(request.getPurchaseDate());

        return toAssetResponse(assetRepository.save(asset));
    }

    /**
     * Xóa tài sản khỏi danh mục.
     * Guard: Nếu tài sản đang được gán cho bất kỳ phòng nào (RoomAssets),
     *        ném ASSET_IN_USE để bảo vệ toàn vẹn dữ liệu.
     * Complexity: O(1) - existsById + existsByAsset_AssetId đều dùng DB-level index.
     */
    public void deleteAsset(Integer assetId) {
        if (!assetRepository.existsById(assetId)) {
            throw new AppException(ErrorCode.ASSET_NOT_FOUND);
        }
        if (roomAssetRepository.existsByAsset_AssetId(assetId)) {
            throw new AppException(ErrorCode.ASSET_IN_USE);
        }
        assetRepository.deleteById(assetId);
    }

    // Room Assets
    public List<RoomAssetResponse> getAssetsByRoom(Integer roomId) {
        if (!roomRepository.existsById(roomId)) {
            throw new AppException(ErrorCode.ROOM_NOT_FOUND);
        }
        return roomAssetRepository.findByRoom_RoomId(roomId).stream()
                .map(this::toRoomAssetResponse)
                .collect(Collectors.toList());
    }

    public RoomAssetResponse assignAssetToRoom(RoomAssetRequest request) {
        Room room = roomRepository.findById(request.getRoomId())
                .orElseThrow(() -> new AppException(ErrorCode.ROOM_NOT_FOUND));

        Asset asset = assetRepository.findById(request.getAssetId())
                .orElseThrow(() -> new AppException(ErrorCode.ASSET_NOT_FOUND));

        RoomAsset roomAsset = RoomAsset.builder()
                .room(room)
                .asset(asset)
                .quantity(request.getQuantity())
                .conditionStatus(request.getConditionStatus() != null ? request.getConditionStatus() : "GOOD")
                .build();

        return toRoomAssetResponse(roomAssetRepository.save(roomAsset));
    }

    public RoomAssetResponse updateRoomAsset(Integer id, RoomAssetRequest request) {
        RoomAsset roomAsset = roomAssetRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND));

        if (request.getQuantity() != null)
            roomAsset.setQuantity(request.getQuantity());
        if (request.getConditionStatus() != null)
            roomAsset.setConditionStatus(request.getConditionStatus());

        return toRoomAssetResponse(roomAssetRepository.save(roomAsset));
    }

    @Transactional
    public void bulkAssignAssets(BulkRoomAssetRequest request) {
        List<Room> rooms;
        if (request.getRoomTypeId() != null) {
            rooms = roomRepository.findByFloor_Building_BuildingIdAndRoomType_RoomTypeId(
                    request.getBuildingId(), request.getRoomTypeId());
        } else {
            rooms = roomRepository.findByFloor_Building_BuildingId(request.getBuildingId());
        }

        Asset asset = assetRepository.findById(request.getAssetId())
                .orElseThrow(() -> new AppException(ErrorCode.ASSET_NOT_FOUND));

        for (Room room : rooms) {
            RoomAsset roomAsset = RoomAsset.builder()
                    .room(room)
                    .asset(asset)
                    .quantity(request.getQuantity())
                    .conditionStatus(request.getConditionStatus() != null ? request.getConditionStatus() : "GOOD")
                    .build();
            roomAssetRepository.save(roomAsset);
        }
    }

    @Transactional
    public void bulkRemoveAssets(BulkRoomAssetRequest request) {
        if (request.getRoomTypeId() != null) {
            roomAssetRepository.deleteByRoom_Floor_Building_BuildingIdAndAsset_AssetIdAndRoom_RoomType_RoomTypeId(
                    request.getBuildingId(), request.getAssetId(), request.getRoomTypeId());
        } else {
            roomAssetRepository.deleteByRoom_Floor_Building_BuildingIdAndAsset_AssetId(
                    request.getBuildingId(), request.getAssetId());
        }
    }

    public void removeRoomAsset(Integer id) {
        RoomAsset roomAsset = roomAssetRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND));
        roomAssetRepository.delete(roomAsset);
    }

    // Mappers
    private AssetResponse toAssetResponse(Asset asset) {
        return AssetResponse.builder()
                .assetId(asset.getAssetId())
                .assetName(asset.getAssetName())
                .assetCode(asset.getAssetCode())
                .purchaseDate(asset.getPurchaseDate())
                .purchasePrice(asset.getPurchasePrice())
                .build();
    }

    private RoomAssetResponse toRoomAssetResponse(RoomAsset ra) {
        return RoomAssetResponse.builder()
                .roomAssetId(ra.getRoomAssetId())
                .roomId(ra.getRoom().getRoomId())
                .roomNumber(ra.getRoom().getRoomNumber())
                .assetId(ra.getAsset().getAssetId())
                .assetName(ra.getAsset().getAssetName())
                .quantity(ra.getQuantity())
                .conditionStatus(ra.getConditionStatus())
                .build();
    }
}

