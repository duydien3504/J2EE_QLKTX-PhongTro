package com.group10.API_ManageDormitory.service;

import com.group10.API_ManageDormitory.dtos.request.RoomRequest;
import com.group10.API_ManageDormitory.dtos.request.RoomTypeRequest;
import com.group10.API_ManageDormitory.dtos.response.RoomResponse;
import com.group10.API_ManageDormitory.dtos.response.RoomTypeResponse;
import com.group10.API_ManageDormitory.entity.Floor;
import com.group10.API_ManageDormitory.entity.Room;
import com.group10.API_ManageDormitory.entity.RoomType;
import com.group10.API_ManageDormitory.exception.AppException;
import com.group10.API_ManageDormitory.exception.ErrorCode;
import com.group10.API_ManageDormitory.repository.FloorRepository;
import com.group10.API_ManageDormitory.repository.RoomRepository;
import com.group10.API_ManageDormitory.repository.RoomTypeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RoomService {
    private final RoomRepository roomRepository;
    private final RoomTypeRepository roomTypeRepository;
    private final FloorRepository floorRepository;

    // RoomType Operations
    public List<RoomTypeResponse> getAllRoomTypes() {
        return roomTypeRepository.findAll().stream()
                .map(this::toRoomTypeResponse)
                .collect(Collectors.toList());
    }

    public RoomTypeResponse createRoomType(RoomTypeRequest request) {
        RoomType roomType = RoomType.builder()
                .typeName(request.getTypeName())
                .basePrice(request.getBasePrice())
                .area(request.getArea())
                .maxOccupancy(request.getMaxOccupancy())
                .description(request.getDescription())
                .build();
        return toRoomTypeResponse(roomTypeRepository.save(roomType));
    }

    // Room Operations
    public List<RoomResponse> getRooms(Integer floorId, String status, BigDecimal minPrice, BigDecimal maxPrice) {
        List<Room> rooms = roomRepository.findAll();

        // Manual Filtering
        return rooms.stream()
                .filter(room -> floorId == null || room.getFloor().getFloorId().equals(floorId))
                .filter(room -> status == null
                        || (room.getCurrentStatus() != null && room.getCurrentStatus().equalsIgnoreCase(status)))
                .filter(room -> minPrice == null || room.getRoomType().getBasePrice().compareTo(minPrice) >= 0)
                .filter(room -> maxPrice == null || room.getRoomType().getBasePrice().compareTo(maxPrice) <= 0)
                .map(this::toRoomResponse)
                .collect(Collectors.toList());
    }

    public RoomResponse getRoomDetail(Integer id) {
        Room room = roomRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Room not found"));
        return toRoomResponse(room);
    }

    public RoomResponse createRoom(RoomRequest request) {
        Floor floor = floorRepository.findById(request.getFloorId())
                .orElseThrow(() -> new RuntimeException("Floor not found"));

        RoomType roomType = roomTypeRepository.findById(request.getRoomTypeId())
                .orElseThrow(() -> new RuntimeException("RoomType not found"));

        if (request.getCurrentStatus() == null) {
            request.setCurrentStatus("AVAILABLE");
        }

        Room room = Room.builder()
                .roomNumber(request.getRoomNumber())
                .floor(floor)
                .roomType(roomType)
                .currentStatus(request.getCurrentStatus())
                .build();

        return toRoomResponse(roomRepository.save(room));
    }

    public RoomResponse updateRoom(Integer id, RoomRequest request) {
        Room room = roomRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Room not found"));

        if (request.getRoomNumber() != null)
            room.setRoomNumber(request.getRoomNumber());
        if (request.getCurrentStatus() != null)
            room.setCurrentStatus(request.getCurrentStatus());

        if (request.getFloorId() != null) {
            Floor floor = floorRepository.findById(request.getFloorId())
                    .orElseThrow(() -> new RuntimeException("Floor not found"));
            room.setFloor(floor);
        }

        if (request.getRoomTypeId() != null) {
            RoomType roomType = roomTypeRepository.findById(request.getRoomTypeId())
                    .orElseThrow(() -> new RuntimeException("RoomType not found"));
            room.setRoomType(roomType);
        }

        return toRoomResponse(roomRepository.save(room));
    }

    public RoomResponse updateRoomStatus(Integer id, String status) {
        Room room = roomRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Room not found"));
        room.setCurrentStatus(status);
        return toRoomResponse(roomRepository.save(room));
    }

    // Mappers
    private RoomTypeResponse toRoomTypeResponse(RoomType roomType) {
        return RoomTypeResponse.builder()
                .roomTypeId(roomType.getRoomTypeId())
                .typeName(roomType.getTypeName())
                .basePrice(roomType.getBasePrice())
                .area(roomType.getArea())
                .maxOccupancy(roomType.getMaxOccupancy())
                .description(roomType.getDescription())
                .build();
    }

    private RoomResponse toRoomResponse(Room room) {
        return RoomResponse.builder()
                .roomId(room.getRoomId())
                .roomNumber(room.getRoomNumber())
                .floorId(room.getFloor().getFloorId())
                .floorName(room.getFloor().getFloorName())
                .buildingName(room.getFloor().getBuilding().getBuildingName())
                .roomTypeId(room.getRoomType().getRoomTypeId())
                .roomTypeName(room.getRoomType().getTypeName())
                .price(room.getRoomType().getBasePrice())
                .currentStatus(room.getCurrentStatus())
                .build();
    }
}
