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
    private final NotificationService notificationService;

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

        Incident savedIncident = incidentRepository.save(incident);

        // Notify Building Manager
        if (room.getFloor() != null && room.getFloor().getBuilding() != null && room.getFloor().getBuilding().getManager() != null) {
            notificationService.createNotification(com.group10.API_ManageDormitory.dtos.request.NotificationRequest.builder()
                    .title("Báo cáo sự cố mới - Phòng " + room.getRoomNumber())
                    .content("Người thuê " + tenant.getFullName() + " vừa báo cáo sự cố: " + request.getDescription())
                    .type("INCIDENT")
                    .userIds(java.util.List.of(room.getFloor().getBuilding().getManager().getUserId()))
                    .build());
        }

        return toIncidentResponse(savedIncident);
    }

    public List<IncidentResponse> getAllIncidents() {
        String username = com.group10.API_ManageDormitory.utils.SecurityUtils.getCurrentUsername();
        boolean isAdmin = com.group10.API_ManageDormitory.utils.SecurityUtils.hasRole("SCOPE_ADMIN") || 
                         com.group10.API_ManageDormitory.utils.SecurityUtils.hasRole("ADMIN");
        boolean isManageRole = com.group10.API_ManageDormitory.utils.SecurityUtils.hasRole("SCOPE_OWNER") ||
                              com.group10.API_ManageDormitory.utils.SecurityUtils.hasRole("SCOPE_STAFF") ||
                              com.group10.API_ManageDormitory.utils.SecurityUtils.hasRole("OWNER") ||
                              com.group10.API_ManageDormitory.utils.SecurityUtils.hasRole("STAFF");

        List<Incident> incidents = incidentRepository.findAll();

        return incidents.stream()
                .filter(incident -> isAdmin || !isManageRole || isIncidentManagedBy(incident, username))
                .map(this::toIncidentResponse)
                .collect(Collectors.toList());
    }

    private boolean isIncidentManagedBy(Incident incident, String username) {
        return incident.getRoom() != null && 
               incident.getRoom().getFloor() != null && 
               incident.getRoom().getFloor().getBuilding() != null && 
               incident.getRoom().getFloor().getBuilding().getManager() != null && 
               incident.getRoom().getFloor().getBuilding().getManager().getUsername().equals(username);
    }

    public IncidentResponse updateIncidentStatus(Integer incidentId, String status) {
        Incident incident = incidentRepository.findById(incidentId)
                .orElseThrow(() -> new AppException(ErrorCode.INCIDENT_NOT_FOUND));

        checkIncidentOwnership(incident);

        incident.setStatus(status);
        Incident updatedIncident = incidentRepository.save(incident);

        // Notify Tenant about status update
        if (updatedIncident.getTenant() != null && updatedIncident.getTenant().getUser() != null) {
            String statusVn = status.equals("RESOLVED") ? "Đã xử lý xong" : status;
            notificationService.createNotification(com.group10.API_ManageDormitory.dtos.request.NotificationRequest.builder()
                    .title("Cập nhật trạng thái sự cố")
                    .content("Sự cố tại phòng " + updatedIncident.getRoom().getRoomNumber() + " đã được chuyển sang trạng thái: " + statusVn)
                    .type("INCIDENT")
                    .userIds(java.util.List.of(updatedIncident.getTenant().getUser().getUserId()))
                    .build());
        }

        return toIncidentResponse(updatedIncident);
    }

    public List<IncidentResponse> getIncidentsByRoom(Integer roomId) {
        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new AppException(ErrorCode.ROOM_NOT_FOUND));
        
        checkRoomOwnership(room);

        return incidentRepository.findByRoom_RoomId(roomId).stream()
                .map(this::toIncidentResponse)
                .collect(Collectors.toList());
    }

    public List<IncidentResponse> getIncidentsByTenant(Integer tenantId) {
        String username = com.group10.API_ManageDormitory.utils.SecurityUtils.getCurrentUsername();
        boolean isAdmin = com.group10.API_ManageDormitory.utils.SecurityUtils.hasRole("SCOPE_ADMIN") || 
                         com.group10.API_ManageDormitory.utils.SecurityUtils.hasRole("ADMIN");
        boolean isManageRole = com.group10.API_ManageDormitory.utils.SecurityUtils.hasRole("SCOPE_OWNER") ||
                              com.group10.API_ManageDormitory.utils.SecurityUtils.hasRole("SCOPE_STAFF") ||
                              com.group10.API_ManageDormitory.utils.SecurityUtils.hasRole("OWNER") ||
                              com.group10.API_ManageDormitory.utils.SecurityUtils.hasRole("STAFF");

        if (!isAdmin && !isManageRole && username != null) {
            Tenant requester = tenantRepository.findByUser_Username(username)
                    .orElseThrow(() -> new AppException(ErrorCode.TENANT_NOT_FOUND));
            if (!requester.getTenantId().equals(tenantId)) {
                throw new AppException(ErrorCode.ACCESS_DENIED_TO_RESOURCE);
            }
        }

        return incidentRepository.findByTenant_TenantId(tenantId).stream()
                .map(this::toIncidentResponse)
                .collect(Collectors.toList());
    }

    private void checkIncidentOwnership(Incident incident) {
        String username = com.group10.API_ManageDormitory.utils.SecurityUtils.getCurrentUsername();
        boolean isAdmin = com.group10.API_ManageDormitory.utils.SecurityUtils.hasRole("SCOPE_ADMIN") || 
                         com.group10.API_ManageDormitory.utils.SecurityUtils.hasRole("ADMIN");
        boolean isManageRole = com.group10.API_ManageDormitory.utils.SecurityUtils.hasRole("SCOPE_OWNER") ||
                              com.group10.API_ManageDormitory.utils.SecurityUtils.hasRole("SCOPE_STAFF") ||
                              com.group10.API_ManageDormitory.utils.SecurityUtils.hasRole("OWNER") ||
                              com.group10.API_ManageDormitory.utils.SecurityUtils.hasRole("STAFF");

        if (isAdmin || !isManageRole || username == null) return;

        if (!isIncidentManagedBy(incident, username)) {
            throw new AppException(ErrorCode.ACCESS_DENIED_TO_RESOURCE);
        }
    }

    private void checkRoomOwnership(Room room) {
        String username = com.group10.API_ManageDormitory.utils.SecurityUtils.getCurrentUsername();
        boolean isAdmin = com.group10.API_ManageDormitory.utils.SecurityUtils.hasRole("SCOPE_ADMIN") || 
                         com.group10.API_ManageDormitory.utils.SecurityUtils.hasRole("ADMIN");
        boolean isManageRole = com.group10.API_ManageDormitory.utils.SecurityUtils.hasRole("SCOPE_OWNER") ||
                              com.group10.API_ManageDormitory.utils.SecurityUtils.hasRole("SCOPE_STAFF") ||
                              com.group10.API_ManageDormitory.utils.SecurityUtils.hasRole("OWNER") ||
                              com.group10.API_ManageDormitory.utils.SecurityUtils.hasRole("STAFF");

        if (isAdmin || !isManageRole || username == null) return;

        if (room.getFloor() == null || room.getFloor().getBuilding() == null || room.getFloor().getBuilding().getManager() == null ||
            !room.getFloor().getBuilding().getManager().getUsername().equals(username)) {
            throw new AppException(ErrorCode.ACCESS_DENIED_TO_RESOURCE);
        }
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
