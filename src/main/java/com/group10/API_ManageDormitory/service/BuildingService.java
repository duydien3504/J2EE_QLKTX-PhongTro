package com.group10.API_ManageDormitory.service;

import com.group10.API_ManageDormitory.dtos.request.BuildingRequest;
import com.group10.API_ManageDormitory.dtos.request.FloorRequest;
import com.group10.API_ManageDormitory.dtos.response.BuildingResponse;
import com.group10.API_ManageDormitory.dtos.response.FloorResponse;
import com.group10.API_ManageDormitory.entity.Building;
import com.group10.API_ManageDormitory.entity.Floor;
import com.group10.API_ManageDormitory.entity.Room;
import com.group10.API_ManageDormitory.entity.User;
import com.group10.API_ManageDormitory.exception.AppException;
import com.group10.API_ManageDormitory.exception.ErrorCode;
import com.group10.API_ManageDormitory.repository.BuildingRepository;
import com.group10.API_ManageDormitory.repository.ContractRepository;
import com.group10.API_ManageDormitory.repository.FloorRepository;
import com.group10.API_ManageDormitory.repository.RoomRepository;
import com.group10.API_ManageDormitory.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BuildingService {
    private final BuildingRepository buildingRepository;
    private final FloorRepository floorRepository;
    private final UserRepository userRepository;
    private final RoomRepository roomRepository;
    private final ContractRepository contractRepository;
    private final AccessValidationService accessValidationService;

    // Building CRUD
    public List<BuildingResponse> getAllBuildings() {
        if (accessValidationService.isAdmin() || !accessValidationService.isManageRole()) {
            return buildingRepository.findAll().stream()
                    .map(this::toBuildingResponse)
                    .collect(Collectors.toList());
        }

        User requester = accessValidationService.getCurrentUser();
        // Return buildings where the user is either the manager OR the owner
        return buildingRepository.findByManager_UserIdOrOwner_UserId(requester.getUserId(), requester.getUserId()).stream()
                .map(this::toBuildingResponse)
                .collect(Collectors.toList());
    }

    public BuildingResponse createBuilding(BuildingRequest request) {
        User requester = accessValidationService.getCurrentUser();
        Building building = Building.builder()
                .buildingName(request.getBuildingName())
                .address(request.getAddress())
                .totalFloors(request.getTotalFloors())
                .owner(requester) // Permanent owner
                .build();

        if (request.getManagerId() != null && request.getManagerId() > 0) {
            User manager = userRepository.findById(request.getManagerId())
                    .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));
            building.setManager(manager);
        } else {
            // Auto-assign the creator as manager if not specified
            building.setManager(requester);
        }

        return toBuildingResponse(buildingRepository.save(building));
    }

    public BuildingResponse updateBuilding(Integer id, BuildingRequest request) {
        Building building = buildingRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND));

        accessValidationService.validateBuildingAccess(building);

        if (request.getBuildingName() != null)
            building.setBuildingName(request.getBuildingName());
        if (request.getAddress() != null)
            building.setAddress(request.getAddress());
        if (request.getTotalFloors() != null)
            building.setTotalFloors(request.getTotalFloors());

        if (request.getManagerId() != null) {
            User manager = userRepository.findById(request.getManagerId())
                    .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));
            building.setManager(manager);
        }

        return toBuildingResponse(buildingRepository.save(building));
    }

    public void deleteBuilding(Integer id) {
        Building building = buildingRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND));

        accessValidationService.validateBuildingAccess(building);

        if (!floorRepository.findByBuilding(building).isEmpty()) {
            throw new RuntimeException("Cannot delete building with existing floors");
        }

        buildingRepository.delete(building);
    }

    // Floor CRUD
    public List<FloorResponse> getFloorsByBuilding(Integer buildingId) {
        Building building = buildingRepository.findById(buildingId)
                .orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND));

        return floorRepository.findByBuilding(building).stream()
                .map(this::toFloorResponse)
                .collect(Collectors.toList());
    }

    public FloorResponse createFloor(FloorRequest request) {
        Building building = buildingRepository.findById(request.getBuildingId())
                .orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND));

        accessValidationService.validateBuildingAccess(building);

        Floor floor = Floor.builder()
                .floorName(request.getFloorName())
                .building(building)
                .build();

        return toFloorResponse(floorRepository.save(floor));
    }

    public void deleteFloor(Integer floorId) {
        Floor floor = floorRepository.findById(floorId)
                .orElseThrow(() -> new AppException(ErrorCode.FLOOR_NOT_FOUND));

        accessValidationService.validateBuildingAccess(floor.getBuilding());

        if (roomRepository.existsByFloor_FloorId(floorId)) {
            throw new AppException(ErrorCode.FLOOR_IN_USE);
        }

        floorRepository.delete(floor);
    }

    public FloorResponse updateFloor(Integer id, FloorRequest request) {
        Floor floor = floorRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.FLOOR_NOT_FOUND));

        accessValidationService.validateBuildingAccess(floor.getBuilding());

        if (request.getFloorName() != null)
            floor.setFloorName(request.getFloorName());

        if (request.getBuildingId() != null) {
            Building building = buildingRepository.findById(request.getBuildingId())
                    .orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND));
            floor.setBuilding(building);
        }

        return toFloorResponse(floorRepository.save(floor));
    }

    public void updateManagerForBuildings(Integer userId, List<Integer> buildingIds) {
        User manager = (userId != null) ? userRepository.findById(userId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED)) : null;

        // Note: For simplicity, we only allow one manager per building.
        // But the owner always has access via the owner field.
        
        // If buildingIds is provided, we assign this user as manager for these buildings.
        // It does NOT change the owner.
        if (buildingIds != null && manager != null) {
            for (Integer bId : buildingIds) {
                Building b = buildingRepository.findById(bId)
                        .orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND));
                
                accessValidationService.validateBuildingAccess(b);
                
                // If it's a manager role, just update the manager. Creator stays owner.
                b.setManager(manager);
                buildingRepository.save(b);
            }
        }
    }

    // Mappers
    private BuildingResponse toBuildingResponse(Building building) {
        Integer managerId = building.getManager() != null ? building.getManager().getUserId() : null;
        Integer ownerId = building.getOwner() != null ? building.getOwner().getUserId() : managerId;
        String ownerName = building.getOwner() != null ? building.getOwner().getFullName() : building.getManager() != null ? building.getManager().getFullName() : null;

        return BuildingResponse.builder()
                .buildingId(building.getBuildingId())
                .buildingName(building.getBuildingName())
                .address(building.getAddress())
                .totalFloors(building.getTotalFloors())
                .managerId(managerId)
                .managerName(building.getManager() != null ? building.getManager().getFullName() : null)
                .ownerId(ownerId)
                .ownerName(ownerName)
                .build();
    }

    private FloorResponse toFloorResponse(Floor floor) {
        return FloorResponse.builder()
                .floorId(floor.getFloorId())
                .floorName(floor.getFloorName())
                .buildingId(floor.getBuilding().getBuildingId())
                .buildingName(floor.getBuilding().getBuildingName())
                .build();
    }

    @Transactional
    public void deleteFloorCompletely(Integer id) {
        Floor floor = floorRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND));

        accessValidationService.validateBuildingAccess(floor.getBuilding());

        List<Room> rooms = roomRepository.findByFloor_FloorId(id);
        
        // Safety checks for all rooms
        for (Room room : rooms) {
            if ("OCCUPIED".equalsIgnoreCase(room.getCurrentStatus())) {
                throw new AppException(ErrorCode.ROOM_OCCUPIED);
            }
            if (contractRepository.existsByRoom_RoomIdAndIsDeletedFalse(room.getRoomId())) {
                throw new AppException(ErrorCode.ROOM_IN_USE);
            }
        }

        // All rooms are safe to delete
        for (Room room : rooms) {
            roomRepository.delete(room);
        }

        floorRepository.delete(floor);
    }
}
