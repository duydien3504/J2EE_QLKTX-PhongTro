package com.group10.API_ManageDormitory.service;

import com.group10.API_ManageDormitory.dtos.request.BuildingRequest;
import com.group10.API_ManageDormitory.dtos.response.BuildingResponse;
import com.group10.API_ManageDormitory.entity.Building;
import com.group10.API_ManageDormitory.repository.BuildingRepository;
import com.group10.API_ManageDormitory.repository.FloorRepository;
import com.group10.API_ManageDormitory.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BuildingServiceTest {
    @Mock
    private BuildingRepository buildingRepository;
    @Mock
    private FloorRepository floorRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private AccessValidationService accessValidationService;

    @InjectMocks
    private BuildingService buildingService;

    @Test
    void getAllBuildings_success() {
        Building building = Building.builder().buildingId(1).buildingName("A").build();
        when(buildingRepository.findAll()).thenReturn(List.of(building));

        List<BuildingResponse> result = buildingService.getAllBuildings();
        assertFalse(result.isEmpty());
        assertEquals("A", result.get(0).getBuildingName());
    }

    @Test
    void createBuilding_success() {
        BuildingRequest request = BuildingRequest.builder().buildingName("A").build();
        Building building = Building.builder().buildingId(1).buildingName("A").build();

        when(buildingRepository.save(any(Building.class))).thenReturn(building);

        BuildingResponse result = buildingService.createBuilding(request);
        assertEquals("A", result.getBuildingName());
    }

    @Test
    void deleteBuilding_withFloors_fails() {
        Building building = Building.builder().buildingId(1).build();
        when(buildingRepository.findById(1)).thenReturn(Optional.of(building));
        when(floorRepository.findByBuilding(building))
                .thenReturn(List.of(new com.group10.API_ManageDormitory.entity.Floor()));

        assertThrows(RuntimeException.class, () -> buildingService.deleteBuilding(1));
    }
}
