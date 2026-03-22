package com.group10.API_ManageDormitory.service;

import com.group10.API_ManageDormitory.dtos.request.IncidentRequest;
import com.group10.API_ManageDormitory.dtos.response.IncidentResponse;
import com.group10.API_ManageDormitory.entity.Incident;
import com.group10.API_ManageDormitory.entity.Room;
import com.group10.API_ManageDormitory.entity.Tenant;
import com.group10.API_ManageDormitory.exception.AppException;
import com.group10.API_ManageDormitory.exception.ErrorCode;
import com.group10.API_ManageDormitory.repository.IncidentRepository;
import com.group10.API_ManageDormitory.repository.RoomRepository;
import com.group10.API_ManageDormitory.repository.TenantRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class IncidentService {
    private final IncidentRepository incidentRepository;
    private final RoomRepository roomRepository;
    private final TenantRepository tenantRepository;

    public IncidentResponse createIncident(IncidentRequest request) {
        Room room = roomRepository.findById(request.getRoomId())
                .orElseThrow(() -> new AppException(ErrorCode.ROOM_NOT_FOUND));

        Tenant tenant = tenantRepository.findById(request.getTenantId())
                .orElseThrow(() -> new AppException(ErrorCode.TENANT_NOT_FOUND));

        Incident incident = Incident.builder()
                .room(room)
                .tenant(tenant)
                .description(request.getDescription())
                .build();

        return toIncidentResponse(incidentRepository.save(incident));
    }

    public List<IncidentResponse> getAllIncidents() {
        return incidentRepository.findAll().stream()
                .map(this::toIncidentResponse)
                .collect(Collectors.toList());
    }

    public IncidentResponse updateIncidentStatus(Integer incidentId, String status) {
        Incident incident = incidentRepository.findById(incidentId)
                .orElseThrow(() -> new AppException(ErrorCode.INCIDENT_NOT_FOUND));

        incident.setStatus(status);
        return toIncidentResponse(incidentRepository.save(incident));
    }

    public List<IncidentResponse> getIncidentsByRoom(Integer roomId) {
        return incidentRepository.findByRoom_RoomId(roomId).stream()
                .map(this::toIncidentResponse)
                .collect(Collectors.toList());
    }

    public List<IncidentResponse> getIncidentsByTenant(Integer tenantId) {
        return incidentRepository.findByTenant_TenantId(tenantId).stream()
                .map(this::toIncidentResponse)
                .collect(Collectors.toList());
    }

    private IncidentResponse toIncidentResponse(Incident incident) {
        return IncidentResponse.builder()
                .incidentId(incident.getIncidentId())
                .roomId(incident.getRoom().getRoomId())
                .roomNumber(incident.getRoom().getRoomNumber())
                .tenantId(incident.getTenant().getTenantId())
                .tenantName(incident.getTenant().getFullName())
                .description(incident.getDescription())
                .status(incident.getStatus())
                .reportedDate(incident.getReportedDate())
                .build();
    }
}
