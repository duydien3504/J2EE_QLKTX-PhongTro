package com.group10.API_ManageDormitory.service;

import com.group10.API_ManageDormitory.dtos.request.RoomRequest;
import com.group10.API_ManageDormitory.dtos.response.RoomResponse;
import com.group10.API_ManageDormitory.entity.Floor;
import com.group10.API_ManageDormitory.entity.Room;
import com.group10.API_ManageDormitory.entity.RoomType;
import com.group10.API_ManageDormitory.entity.Building;
import com.group10.API_ManageDormitory.repository.FloorRepository;
import com.group10.API_ManageDormitory.repository.RoomRepository;
import com.group10.API_ManageDormitory.repository.RoomTypeRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RoomServiceTest {
    @Mock
    private RoomRepository roomRepository;
    @Mock
    private RoomTypeRepository roomTypeRepository;
    @Mock
    private FloorRepository floorRepository;
    @InjectMocks
    private RoomService roomService;

    @Test
    void getRooms_filter_success() {
        Building building = Building.builder().buildingName("B").build();
        Floor floor = Floor.builder().floorId(1).floorName("F1").building(building).build();
        RoomType type = RoomType.builder().basePrice(BigDecimal.valueOf(100)).typeName("T1").build();
        Room room = Room.builder().roomId(1).roomNumber("101").floor(floor).roomType(type).currentStatus("AVAILABLE")
                .build();

        when(roomRepository.findAll()).thenReturn(List.of(room));

        // Filter Match
        List<RoomResponse> result = roomService.getRooms(1, "AVAILABLE", null, null);
        assertFalse(result.isEmpty());

        // Filter Mismatch
        result = roomService.getRooms(2, null, null, null);
        assertTrue(result.isEmpty());
    }

    @Test
    void createRoom_success() {
        RoomRequest request = RoomRequest.builder()
                .roomNumber("101")
                .floorId(1)
                .roomTypeId(1)
                .build();

        Building building = Building.builder().buildingName("B").build();
        Floor floor = Floor.builder().floorId(1).floorName("F1").building(building).build();
        RoomType type = RoomType.builder().roomTypeId(1).typeName("T1").basePrice(BigDecimal.TEN).build();
        Room room = Room.builder().roomId(1).roomNumber("101").floor(floor).roomType(type).build();

        when(floorRepository.findById(1)).thenReturn(Optional.of(floor));
        when(roomTypeRepository.findById(1)).thenReturn(Optional.of(type));
        when(roomRepository.save(any(Room.class))).thenReturn(room);

        RoomResponse response = roomService.createRoom(request);
        assertEquals("101", response.getRoomNumber());
    }
}
