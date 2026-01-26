package com.group10.API_ManageDormitory.service;

import com.group10.API_ManageDormitory.dtos.request.ServiceInfoRequest;
import com.group10.API_ManageDormitory.dtos.response.ServiceInfoResponse;
import com.group10.API_ManageDormitory.entity.Service;
import com.group10.API_ManageDormitory.repository.BuildingRepository;
import com.group10.API_ManageDormitory.repository.BuildingServiceRepository;
import com.group10.API_ManageDormitory.repository.ServiceRepository;
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
class ServiceConfigServiceTest {
    @Mock
    private ServiceRepository serviceRepository;
    @Mock
    private BuildingServiceRepository buildingServiceRepository;
    @Mock
    private BuildingRepository buildingRepository;
    @InjectMocks
    private ServiceConfigService serviceConfigService;

    @Test
    void getAllServices_success() {
        Service service = Service.builder().serviceName("Electric").build();
        when(serviceRepository.findAll()).thenReturn(List.of(service));

        List<ServiceInfoResponse> result = serviceConfigService.getAllServices();
        assertFalse(result.isEmpty());
        assertEquals("Electric", result.get(0).getServiceName());
    }

    @Test
    void createService_success() {
        ServiceInfoRequest request = ServiceInfoRequest.builder().serviceName("Water").unit("m3").build();
        Service service = Service.builder().serviceName("Water").unit("m3").build();

        when(serviceRepository.save(any(Service.class))).thenReturn(service);

        ServiceInfoResponse response = serviceConfigService.createService(request);
        assertEquals("Water", response.getServiceName());
    }
}
