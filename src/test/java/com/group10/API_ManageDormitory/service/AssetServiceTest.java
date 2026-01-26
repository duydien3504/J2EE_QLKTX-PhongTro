package com.group10.API_ManageDormitory.service;

import com.group10.API_ManageDormitory.dtos.request.AssetRequest;
import com.group10.API_ManageDormitory.dtos.request.RoomAssetRequest;
import com.group10.API_ManageDormitory.dtos.response.AssetResponse;
import com.group10.API_ManageDormitory.dtos.response.RoomAssetResponse;
import com.group10.API_ManageDormitory.entity.Asset;
import com.group10.API_ManageDormitory.entity.Room;
import com.group10.API_ManageDormitory.entity.RoomAsset;
import com.group10.API_ManageDormitory.repository.AssetRepository;
import com.group10.API_ManageDormitory.repository.RoomAssetRepository;
import com.group10.API_ManageDormitory.repository.RoomRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

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

    @Test
    void createAsset_success() {
        AssetRequest request = AssetRequest.builder().assetName("AC").build();
        Asset asset = Asset.builder().assetName("AC").build();
        when(assetRepository.save(any(Asset.class))).thenReturn(asset);

        AssetResponse response = assetService.createAsset(request);
        assertEquals("AC", response.getAssetName());
    }

    @Test
    void assignAssetToRoom_success() {
        RoomAssetRequest request = RoomAssetRequest.builder().roomId(1).assetId(1).quantity(1).build();
        Room room = Room.builder().roomId(1).roomNumber("101").build();
        Asset asset = Asset.builder().assetId(1).assetName("AC").build();
        RoomAsset saved = RoomAsset.builder().room(room).asset(asset).quantity(1).build();

        when(roomRepository.findById(1)).thenReturn(Optional.of(room));
        when(assetRepository.findById(1)).thenReturn(Optional.of(asset));
        when(roomAssetRepository.save(any(RoomAsset.class))).thenReturn(saved);

        RoomAssetResponse response = assetService.assignAssetToRoom(request);
        assertEquals(1, response.getQuantity());
        assertEquals("101", response.getRoomNumber());
    }
}
