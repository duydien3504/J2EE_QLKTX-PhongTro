package com.group10.API_ManageDormitory.service;

import com.group10.API_ManageDormitory.dtos.request.MeterReadingRequest;
import com.group10.API_ManageDormitory.dtos.response.MeterReadingResponse;
import com.group10.API_ManageDormitory.entity.MeterReading;
import com.group10.API_ManageDormitory.entity.Room;
import com.group10.API_ManageDormitory.entity.Service;
import com.group10.API_ManageDormitory.exception.AppException;
import com.group10.API_ManageDormitory.exception.ErrorCode;
import com.group10.API_ManageDormitory.repository.MeterReadingRepository;
import com.group10.API_ManageDormitory.repository.RoomRepository;
import com.group10.API_ManageDormitory.repository.ServiceRepository;
import com.group10.API_ManageDormitory.repository.UserRepository;
import com.group10.API_ManageDormitory.repository.TenantRepository;
import com.group10.API_ManageDormitory.repository.ContractTenantRepository;
import com.group10.API_ManageDormitory.entity.User;
import com.group10.API_ManageDormitory.entity.Tenant;
import com.group10.API_ManageDormitory.entity.ContractTenant;
import org.springframework.security.core.context.SecurityContextHolder;
import lombok.RequiredArgsConstructor;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@org.springframework.stereotype.Service
@RequiredArgsConstructor
public class MeterReadingService {
    private final MeterReadingRepository meterReadingRepository;
    private final RoomRepository roomRepository;
    private final ServiceRepository serviceRepository;
    private final UserRepository userRepository;
    private final TenantRepository tenantRepository;
    private final ContractTenantRepository contractTenantRepository;

    private void checkDataOwnership(Integer roomId) {
        var context = SecurityContextHolder.getContext();
        if (context == null || context.getAuthentication() == null) return;
        
        String username = context.getAuthentication().getName();
        User user = userRepository.findByUsername(username).orElse(null);
        
        if (user != null && user.getRole() != null && "TENANT".equals(user.getRole().getRoleName())) {
            Tenant tenant = tenantRepository.findByUser_UserId(user.getUserId()).orElse(null);
            if (tenant != null) {
                ContractTenant contractTenant = contractTenantRepository.findByTenant_TenantIdAndContract_IsDeletedFalse(tenant.getTenantId()).orElse(null);
                if (contractTenant != null) {
                    Integer tenantRoomId = contractTenant.getContract().getRoom().getRoomId();
                    if (roomId != null && !roomId.equals(tenantRoomId)) {
                        throw new AppException(ErrorCode.UNAUTHORIZED);
                    }
                } else {
                     throw new AppException(ErrorCode.UNAUTHORIZED);
                }
            } else {
                throw new AppException(ErrorCode.UNAUTHORIZED);
            }
        }
    }

    public List<MeterReadingResponse> getReadings(Integer roomId, Integer month, Integer year) {
        checkDataOwnership(roomId);
        if (roomId != null) {
            if (month != null && year != null) {
                LocalDate start = LocalDate.of(year, month, 1);
                LocalDate end = start.withDayOfMonth(start.lengthOfMonth());
                return meterReadingRepository.findByRoom_RoomIdAndReadingDateBetween(roomId, start, end).stream()
                        .map(this::toResponse).collect(Collectors.toList());
            }
            return meterReadingRepository.findByRoom_RoomId(roomId).stream()
                    .map(this::toResponse).collect(Collectors.toList());
        }
        if (month != null && year != null) {
            LocalDate start = LocalDate.of(year, month, 1);
            LocalDate end = start.withDayOfMonth(start.lengthOfMonth());
            return meterReadingRepository.findByReadingDateBetween(start, end).stream()
                    .map(this::toResponse).collect(Collectors.toList());
        }
        return meterReadingRepository.findAll().stream()
                .map(this::toResponse).collect(Collectors.toList());
    }

    public MeterReadingResponse getLastMonthReading(Integer roomId, Integer serviceId) {
        checkDataOwnership(roomId);
        MeterReading reading = meterReadingRepository
                .findFirstByRoom_RoomIdAndService_ServiceIdOrderByReadingDateDesc(roomId, serviceId)
                .orElse(null);
        if (reading == null)
            return null;
        return toResponse(reading);
    }

    public MeterReadingResponse recordReading(MeterReadingRequest request) {
        Room room = roomRepository.findById(request.getRoomId())
                .orElseThrow(() -> new AppException(ErrorCode.ROOM_NOT_FOUND));

        Service service = serviceRepository.findById(request.getServiceId())
                .orElseThrow(() -> new RuntimeException("Service not found"));

        Double previous = request.getPreviousIndex();
        if (previous == null) {
            // Auto-fetch last reading
            MeterReading last = meterReadingRepository.findFirstByRoom_RoomIdAndService_ServiceIdOrderByReadingDateDesc(
                    request.getRoomId(), request.getServiceId()).orElse(null);
            previous = last != null ? last.getCurrentIndex() : 0.0;
        }

        if (request.getCurrentIndex() < previous) {
            throw new RuntimeException("Current index cannot be less than previous index (" + previous + ")");
        }

        MeterReading reading = MeterReading.builder()
                .room(room)
                .service(service)
                .previousIndex(previous)
                .currentIndex(request.getCurrentIndex())
                .readingDate(request.getReadingDate() != null ? request.getReadingDate() : LocalDate.now())
                .imageProof(request.getImageProof())
                .build();

        return toResponse(meterReadingRepository.save(reading));
    }

    public List<MeterReadingResponse> bulkRecord(List<MeterReadingRequest> requests) {
        return requests.stream().map(this::recordReading).collect(Collectors.toList());
    }

    public MeterReadingResponse updateReading(Integer id, MeterReadingRequest request) {
        MeterReading reading = meterReadingRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Meter reading not found"));

        if (request.getCurrentIndex() != null) {
            if (request.getCurrentIndex() < reading.getPreviousIndex()) {
                throw new RuntimeException("Current index cannot be less than previous index");
            }
            reading.setCurrentIndex(request.getCurrentIndex());
        }
        if (request.getImageProof() != null)
            reading.setImageProof(request.getImageProof());

        return toResponse(meterReadingRepository.save(reading));
    }

    private MeterReadingResponse toResponse(MeterReading entity) {
        return MeterReadingResponse.builder()
                .meterReadingId(entity.getMeterReadingId())
                .roomId(entity.getRoom().getRoomId())
                .roomNumber(entity.getRoom().getRoomNumber())
                .serviceId(entity.getService().getServiceId())
                .serviceName(entity.getService().getServiceName())
                .previousIndex(entity.getPreviousIndex())
                .currentIndex(entity.getCurrentIndex())
                .usage((entity.getCurrentIndex() != null && entity.getPreviousIndex() != null)
                        ? entity.getCurrentIndex() - entity.getPreviousIndex()
                        : 0.0)
                .readingDate(entity.getReadingDate())
                .imageProof(entity.getImageProof())
                .build();
    }
}
