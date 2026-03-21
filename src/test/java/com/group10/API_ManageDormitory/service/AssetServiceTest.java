package com.group10.API_ManageDormitory.service;

import com.group10.API_ManageDormitory.dtos.request.AssetRequest;
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
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AssetServiceTest {

    @Mock
    private AssetRepository assetRepository;
    @Mock
    private RoomAssetRepository roomAssetRepository;
    @Mock
    private RoomRepository roomRepository;

    @InjectMocks
    private AssetService assetService;

    // ─────────────────────────────────────────────────────────
    // getAllAssets
    // ─────────────────────────────────────────────────────────

    @Test
    void getAllAssets_returnsMappedList() {
        Asset a1 = Asset.builder().assetId(1).assetName("AC").build();
        Asset a2 = Asset.builder().assetId(2).assetName("Fan").build();
        when(assetRepository.findAll()).thenReturn(List.of(a1, a2));

        List<AssetResponse> result = assetService.getAllAssets();

        assertEquals(2, result.size());
        assertEquals("AC", result.get(0).getAssetName());
        assertEquals("Fan", result.get(1).getAssetName());
    }

    // ─────────────────────────────────────────────────────────
    // createAsset
    // ─────────────────────────────────────────────────────────

    @Test
    void createAsset_success() {
        AssetRequest request = AssetRequest.builder().assetName("AC").assetCode("A001").build();
        Asset saved = Asset.builder().assetId(1).assetName("AC").assetCode("A001").build();
        when(assetRepository.save(any(Asset.class))).thenReturn(saved);

        AssetResponse response = assetService.createAsset(request);

        assertEquals("AC", response.getAssetName());
        assertEquals("A001", response.getAssetCode());
        verify(assetRepository, times(1)).save(any(Asset.class));
    }

    @Test
    void createAsset_withNullPurchaseDate_usesToday() {
        AssetRequest request = AssetRequest.builder().assetName("Fan").purchaseDate(null).build();
        Asset saved = Asset.builder().assetId(2).assetName("Fan").build();
        when(assetRepository.save(any(Asset.class))).thenReturn(saved);

        AssetResponse response = assetService.createAsset(request);

        assertNotNull(response);
        // purchaseDate=null → service defaults to LocalDate.now(), no exception thrown
        verify(assetRepository).save(any(Asset.class));
    }

    // ─────────────────────────────────────────────────────────
    // deleteAsset
    // ─────────────────────────────────────────────────────────

    @Test
    void deleteAsset_success() {
        // GIVEN: asset exists, not in use
        when(assetRepository.existsById(1)).thenReturn(true);
        when(roomAssetRepository.existsByAsset_AssetId(1)).thenReturn(false);

        // WHEN
        assetService.deleteAsset(1);

        // THEN: deleteById must be called exactly once
        verify(assetRepository, times(1)).deleteById(1);
    }

    @Test
    void deleteAsset_assetNotFound_throwsAppException() {
        // GIVEN: asset does not exist
        when(assetRepository.existsById(99)).thenReturn(false);

        // WHEN & THEN
        AppException ex = assertThrows(AppException.class, () -> assetService.deleteAsset(99));
        assertEquals(ErrorCode.ASSET_NOT_FOUND, ex.getErrorCode());

        // deleteById must NEVER be called
        verify(assetRepository, never()).deleteById(any());
    }

    @Test
    void deleteAsset_assetInUse_throwsAppException() {
        // GIVEN: asset exists but is assigned to a room
        when(assetRepository.existsById(1)).thenReturn(true);
        when(roomAssetRepository.existsByAsset_AssetId(1)).thenReturn(true);

        // WHEN & THEN
        AppException ex = assertThrows(AppException.class, () -> assetService.deleteAsset(1));
        assertEquals(ErrorCode.ASSET_IN_USE, ex.getErrorCode());

        // deleteById must NEVER be called
        verify(assetRepository, never()).deleteById(any());
    }

    // ─────────────────────────────────────────────────────────
    // getAssetsByRoom
    // ─────────────────────────────────────────────────────────

    @Test
    void getAssetsByRoom_roomNotFound_throwsAppException() {
        when(roomRepository.existsById(99)).thenReturn(false);

        AppException ex = assertThrows(AppException.class, () -> assetService.getAssetsByRoom(99));
        assertEquals(ErrorCode.ROOM_NOT_FOUND, ex.getErrorCode());
    }

    @Test
    void getAssetsByRoom_success() {
        Room room = Room.builder().roomId(1).roomNumber("101").build();
        Asset asset = Asset.builder().assetId(1).assetName("AC").build();
        RoomAsset ra = RoomAsset.builder().roomAssetId(1).room(room).asset(asset).quantity(2).conditionStatus("GOOD").build();

        when(roomRepository.existsById(1)).thenReturn(true);
        when(roomAssetRepository.findByRoom_RoomId(1)).thenReturn(List.of(ra));

        List<RoomAssetResponse> result = assetService.getAssetsByRoom(1);

        assertEquals(1, result.size());
        assertEquals("AC", result.get(0).getAssetName());
        assertEquals(2, result.get(0).getQuantity());
    }

    // ─────────────────────────────────────────────────────────
    // assignAssetToRoom
    // ─────────────────────────────────────────────────────────

    @Test
    void assignAssetToRoom_success() {
        RoomAssetRequest request = RoomAssetRequest.builder().roomId(1).assetId(1).quantity(2).build();
        Room room = Room.builder().roomId(1).roomNumber("101").build();
        Asset asset = Asset.builder().assetId(1).assetName("AC").build();
        RoomAsset saved = RoomAsset.builder().roomAssetId(1).room(room).asset(asset).quantity(2).conditionStatus("GOOD").build();

        when(roomRepository.findById(1)).thenReturn(Optional.of(room));
        when(assetRepository.findById(1)).thenReturn(Optional.of(asset));
        when(roomAssetRepository.save(any(RoomAsset.class))).thenReturn(saved);

        RoomAssetResponse response = assetService.assignAssetToRoom(request);

        assertEquals(2, response.getQuantity());
        assertEquals("101", response.getRoomNumber());
        assertEquals("AC", response.getAssetName());
    }

    @Test
    void assignAssetToRoom_roomNotFound_throwsAppException() {
        RoomAssetRequest request = RoomAssetRequest.builder().roomId(99).assetId(1).build();
        when(roomRepository.findById(99)).thenReturn(Optional.empty());

        AppException ex = assertThrows(AppException.class, () -> assetService.assignAssetToRoom(request));
        assertEquals(ErrorCode.ROOM_NOT_FOUND, ex.getErrorCode());
    }

    @Test
    void assignAssetToRoom_assetNotFound_throwsAppException() {
        RoomAssetRequest request = RoomAssetRequest.builder().roomId(1).assetId(99).build();
        Room room = Room.builder().roomId(1).roomNumber("101").build();

        when(roomRepository.findById(1)).thenReturn(Optional.of(room));
        when(assetRepository.findById(99)).thenReturn(Optional.empty());

        AppException ex = assertThrows(AppException.class, () -> assetService.assignAssetToRoom(request));
        assertEquals(ErrorCode.ASSET_NOT_FOUND, ex.getErrorCode());
    }

    // ─────────────────────────────────────────────────────────
    // updateRoomAsset
    // ─────────────────────────────────────────────────────────

    @Test
    void updateRoomAsset_success() {
        Room room = Room.builder().roomId(1).roomNumber("101").build();
        Asset asset = Asset.builder().assetId(1).assetName("AC").build();
        RoomAsset existing = RoomAsset.builder().roomAssetId(1).room(room).asset(asset).quantity(1).conditionStatus("GOOD").build();
        RoomAssetRequest request = RoomAssetRequest.builder().quantity(5).conditionStatus("DAMAGED").build();

        when(roomAssetRepository.findById(1)).thenReturn(Optional.of(existing));
        when(roomAssetRepository.save(any(RoomAsset.class))).thenReturn(existing);

        RoomAssetResponse response = assetService.updateRoomAsset(1, request);

        assertEquals(5, existing.getQuantity());
        assertEquals("DAMAGED", existing.getConditionStatus());
        assertNotNull(response);
    }

    @Test
    void updateRoomAsset_notFound_throwsAppException() {
        when(roomAssetRepository.findById(99)).thenReturn(Optional.empty());

        AppException ex = assertThrows(AppException.class, () ->
                assetService.updateRoomAsset(99, RoomAssetRequest.builder().build()));
        assertEquals(ErrorCode.RESOURCE_NOT_FOUND, ex.getErrorCode());
    }

    // ─────────────────────────────────────────────────────────
    // removeRoomAsset
    // ─────────────────────────────────────────────────────────

    @Test
    void removeRoomAsset_success() {
        Room room = Room.builder().roomId(1).roomNumber("101").build();
        Asset asset = Asset.builder().assetId(1).assetName("AC").build();
        RoomAsset ra = RoomAsset.builder().roomAssetId(1).room(room).asset(asset).build();

        when(roomAssetRepository.findById(1)).thenReturn(Optional.of(ra));

        assetService.removeRoomAsset(1);

        verify(roomAssetRepository, times(1)).delete(ra);
    }

    @Test
    void removeRoomAsset_notFound_throwsAppException() {
        when(roomAssetRepository.findById(99)).thenReturn(Optional.empty());

        AppException ex = assertThrows(AppException.class, () -> assetService.removeRoomAsset(99));
        assertEquals(ErrorCode.RESOURCE_NOT_FOUND, ex.getErrorCode());
    }
}
