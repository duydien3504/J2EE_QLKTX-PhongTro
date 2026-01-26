package com.group10.API_ManageDormitory.service;

import com.group10.API_ManageDormitory.dtos.request.BuildingRequest;
import com.group10.API_ManageDormitory.dtos.request.FloorRequest;
import com.group10.API_ManageDormitory.dtos.response.BuildingResponse;
import com.group10.API_ManageDormitory.dtos.response.FloorResponse;
import com.group10.API_ManageDormitory.entity.Building;
import com.group10.API_ManageDormitory.entity.Floor;
import com.group10.API_ManageDormitory.entity.User;
import com.group10.API_ManageDormitory.exception.AppException;
import com.group10.API_ManageDormitory.exception.ErrorCode;
import com.group10.API_ManageDormitory.repository.BuildingRepository;
import com.group10.API_ManageDormitory.repository.FloorRepository;
import com.group10.API_ManageDormitory.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BuildingService {
    private final BuildingRepository buildingRepository;
    private final FloorRepository floorRepository;
    private final UserRepository userRepository;

    // Building CRUD
    public List<BuildingResponse> getAllBuildings() {
        return buildingRepository.findAll().stream()
                .map(this::toBuildingResponse)
                .collect(Collectors.toList());
    }

    public BuildingResponse createBuilding(BuildingRequest request) {
        Building building = Building.builder()
                .buildingName(request.getBuildingName())
                .address(request.getAddress())
                .totalFloors(request.getTotalFloors())
                .build();

        if (request.getManagerId() != null) {
            User manager = userRepository.findById(request.getManagerId())
                    .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));
            building.setManager(manager);
        }

        return toBuildingResponse(buildingRepository.save(building));
    }

    public BuildingResponse updateBuilding(Integer id, BuildingRequest request) {
        Building building = buildingRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Building not found")); // To define ErrorCode

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
                .orElseThrow(() -> new RuntimeException("Building not found"));

        if (!floorRepository.findByBuilding(building).isEmpty()) {
            throw new RuntimeException("Cannot delete building with existing floors");
        }

        buildingRepository.delete(building);
    }

    // Floor CRUD
    public List<FloorResponse> getFloorsByBuilding(Integer buildingId) {
        Building building = buildingRepository.findById(buildingId)
                .orElseThrow(() -> new RuntimeException("Building not found"));

        return floorRepository.findByBuilding(building).stream()
                .map(this::toFloorResponse)
                .collect(Collectors.toList());
    }

    public FloorResponse createFloor(FloorRequest request) {
        Building building = buildingRepository.findById(request.getBuildingId())
                .orElseThrow(() -> new RuntimeException("Building not found"));

        Floor floor = Floor.builder()
                .floorName(request.getFloorName())
                .building(building)
                .build();

        return toFloorResponse(floorRepository.save(floor));
    }

    // Mappers
    private BuildingResponse toBuildingResponse(Building building) {
        return BuildingResponse.builder()
                .buildingId(building.getBuildingId())
                .buildingName(building.getBuildingName())
                .address(building.getAddress())
                .totalFloors(building.getTotalFloors())
                .managerId(building.getManager() != null ? building.getManager().getUserId() : null)
                .managerName(building.getManager() != null ? building.getManager().getFullName() : null)
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
}
