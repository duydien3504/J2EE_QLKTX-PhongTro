package com.group10.API_ManageDormitory.service;

import com.group10.API_ManageDormitory.dtos.request.BuildingServiceRequest;
import com.group10.API_ManageDormitory.dtos.request.ServiceInfoRequest;
import com.group10.API_ManageDormitory.dtos.response.BuildingServiceResponse;
import com.group10.API_ManageDormitory.dtos.response.ServiceInfoResponse;
import com.group10.API_ManageDormitory.entity.Building;
import com.group10.API_ManageDormitory.entity.BuildingService;
import com.group10.API_ManageDormitory.exception.AppException;
import com.group10.API_ManageDormitory.exception.ErrorCode;
import com.group10.API_ManageDormitory.repository.BuildingRepository;
import com.group10.API_ManageDormitory.repository.BuildingServiceRepository;
import com.group10.API_ManageDormitory.repository.ServiceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ServiceConfigService {
    private final ServiceRepository serviceRepository;
    private final BuildingServiceRepository buildingServiceRepository;
    private final BuildingRepository buildingRepository;

    // Global Services
    public List<ServiceInfoResponse> getAllServices() {
        return serviceRepository.findAll().stream()
                .map(this::toServiceInfoResponse)
                .collect(Collectors.toList());
    }

    public ServiceInfoResponse createService(ServiceInfoRequest request) {
        com.group10.API_ManageDormitory.entity.Service service = com.group10.API_ManageDormitory.entity.Service
                .builder()
                .serviceName(request.getServiceName())
                .unit(request.getUnit())
                .calculationMethod(request.getCalculationMethod())
                .build();
        return toServiceInfoResponse(serviceRepository.save(service));
    }

    public ServiceInfoResponse updateService(Integer id, ServiceInfoRequest request) {
        com.group10.API_ManageDormitory.entity.Service service = serviceRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Service not found"));

        if (request.getServiceName() != null)
            service.setServiceName(request.getServiceName());
        if (request.getUnit() != null)
            service.setUnit(request.getUnit());
        if (request.getCalculationMethod() != null)
            service.setCalculationMethod(request.getCalculationMethod());

        return toServiceInfoResponse(serviceRepository.save(service));
    }

    // Building Services
    public List<BuildingServiceResponse> getBuildingServices(Integer buildingId) {
        if (!buildingRepository.existsById(buildingId)) {
            // Using placeholder ErrorCode, assuming NOT_FOUND generic or specific
            throw new RuntimeException("Building not found");
        }
        return buildingServiceRepository.findByBuilding_BuildingId(buildingId).stream()
                .map(this::toBuildingServiceResponse)
                .collect(Collectors.toList());
    }

    public BuildingServiceResponse upsertBuildingService(Integer buildingId, BuildingServiceRequest request) {
        Building building = buildingRepository.findById(buildingId)
                .orElseThrow(() -> new RuntimeException("Building not found"));

        com.group10.API_ManageDormitory.entity.Service service = serviceRepository.findById(request.getServiceId())
                .orElseThrow(() -> new RuntimeException("Service not found"));

        Optional<BuildingService> existing = buildingServiceRepository
                .findByBuilding_BuildingIdAndService_ServiceId(buildingId, request.getServiceId());

        BuildingService buildingService;
        if (existing.isPresent()) {
            buildingService = existing.get();
            buildingService.setUnitPrice(request.getUnitPrice());
        } else {
            buildingService = BuildingService.builder()
                    .building(building)
                    .service(service)
                    .unitPrice(request.getUnitPrice())
                    .build();
        }

        return toBuildingServiceResponse(buildingServiceRepository.save(buildingService));
    }

    // Mappers
    private ServiceInfoResponse toServiceInfoResponse(com.group10.API_ManageDormitory.entity.Service service) {
        return ServiceInfoResponse.builder()
                .serviceId(service.getServiceId())
                .serviceName(service.getServiceName())
                .unit(service.getUnit())
                .calculationMethod(service.getCalculationMethod())
                .build();
    }

    private BuildingServiceResponse toBuildingServiceResponse(BuildingService bs) {
        return BuildingServiceResponse.builder()
                .buildingServiceId(bs.getBuildingServiceId())
                .buildingId(bs.getBuilding().getBuildingId())
                .serviceId(bs.getService().getServiceId())
                .serviceName(bs.getService().getServiceName())
                .unit(bs.getService().getUnit())
                .unitPrice(bs.getUnitPrice())
                .build();
    }
}
