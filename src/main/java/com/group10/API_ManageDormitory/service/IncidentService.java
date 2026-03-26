package com.group10.API_ManageDormitory.service;

import com.group10.API_ManageDormitory.dtos.request.IncidentRequest;
import com.group10.API_ManageDormitory.dtos.response.IncidentResponse;
import com.group10.API_ManageDormitory.entity.Incident;
import com.group10.API_ManageDormitory.entity.Room;
import com.group10.API_ManageDormitory.entity.Tenant;
import com.group10.API_ManageDormitory.entity.User;
import com.group10.API_ManageDormitory.exception.AppException;
import com.group10.API_ManageDormitory.exception.ErrorCode;
import com.group10.API_ManageDormitory.repository.ContractTenantRepository;
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
    private final ContractTenantRepository contractTenantRepository;
    private final AccessValidationService accessValidationService;

    public IncidentResponse createIncident(IncidentRequest request) {
        Room room = roomRepository.findById(request.getRoomId())
                .orElseThrow(() -> new AppException(ErrorCode.ROOM_NOT_FOUND));

        Tenant reportedTenant = tenantRepository.findById(request.getTenantId())
                .orElseThrow(() -> new AppException(ErrorCode.TENANT_NOT_FOUND));

        // Security: If tenant is reporting, validate they own the tenantId and occupy the room
        if (accessValidationService.isTenant() && !accessValidationService.isAdmin()) {
            User currentUser = accessValidationService.getCurrentUser();
            Tenant requester = tenantRepository.findByUser_Username(currentUser.getUsername())
                    .orElseThrow(() -> new AppException(ErrorCode.TENANT_NOT_FOUND));
            
            if (!requester.getTenantId().equals(reportedTenant.getTenantId())) {
                throw new AppException(ErrorCode.ACCESS_DENIED_TO_RESOURCE);
            }

            // Optimized query for room occupation check
            boolean occupiesRoom = contractTenantRepository.existsByTenant_TenantIdAndContract_Room_RoomIdAndContract_ContractStatusAndContract_IsDeletedFalse(
                    requester.getTenantId(), room.getRoomId(), "ACTIVE");
            
            if (!occupiesRoom) {
                throw new AppException(ErrorCode.ACCESS_DENIED_TO_RESOURCE);
            }
        }

        Incident incident = Incident.builder()
                .room(room)
                .tenant(reportedTenant)
                .description(request.getDescription())
                .build();

        Incident savedIncident = incidentRepository.save(incident);

        // Notify Building Manager
        if (room.getFloor() != null && room.getFloor().getBuilding() != null && room.getFloor().getBuilding().getManager() != null) {
            notificationService.createNotification(com.group10.API_ManageDormitory.dtos.request.NotificationRequest.builder()
                    .title("Báo cáo sự cố mới - Phòng " + room.getRoomNumber())
                    .content("Người thuê " + reportedTenant.getFullName() + " vừa báo cáo sự cố: " + request.getDescription())
                    .type("INCIDENT")
                    .userIds(java.util.List.of(room.getFloor().getBuilding().getManager().getUserId()))
                    .build());
        }

        return toIncidentResponse(savedIncident);
    }

    public List<IncidentResponse> getAllIncidents() {
        List<Incident> incidents = incidentRepository.findAll();

        if (accessValidationService.isAdmin()) {
            return incidents.stream()
                    .map(this::toIncidentResponse)
                    .collect(Collectors.toList());
        }

        return incidents.stream()
                .filter(incident -> accessValidationService.hasRoomAccess(incident.getRoom()))
                .map(this::toIncidentResponse)
                .collect(Collectors.toList());
    }



    public IncidentResponse updateIncidentStatus(Integer incidentId, String status) {
        Incident incident = incidentRepository.findById(incidentId)
                .orElseThrow(() -> new AppException(ErrorCode.INCIDENT_NOT_FOUND));

        accessValidationService.validateRoomAccess(incident.getRoom());

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
        
        accessValidationService.validateRoomAccess(room);

        return incidentRepository.findByRoom_RoomId(roomId).stream()
                .map(this::toIncidentResponse)
                .collect(Collectors.toList());
    }

    public List<IncidentResponse> getIncidentsByTenant(Integer tenantId) {
        if (!accessValidationService.isAdmin() && !accessValidationService.isManageRole()) {
            User currentUser = accessValidationService.getCurrentUser();
            Tenant requester = tenantRepository.findByUser_Username(currentUser.getUsername())
                    .orElseThrow(() -> new AppException(ErrorCode.TENANT_NOT_FOUND));
            if (!requester.getTenantId().equals(tenantId)) {
                throw new AppException(ErrorCode.ACCESS_DENIED_TO_RESOURCE);
            }
        }

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
