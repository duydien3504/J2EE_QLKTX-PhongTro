package com.group10.API_ManageDormitory.service;

import com.group10.API_ManageDormitory.dtos.request.MeterReadingRequest;
import com.group10.API_ManageDormitory.dtos.response.MeterReadingResponse;
import com.group10.API_ManageDormitory.entity.*;
import com.group10.API_ManageDormitory.repository.MeterReadingRepository;
import com.group10.API_ManageDormitory.repository.RoomRepository;
import com.group10.API_ManageDormitory.repository.ServiceRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MeterReadingServiceTest {
    @Mock
    private MeterReadingRepository meterReadingRepository;
    @Mock
    private RoomRepository roomRepository;
    @Mock
    private ServiceRepository serviceRepository;
    @InjectMocks
    private MeterReadingService meterReadingService;

    @Test
    void recordReading_success_auto_fetched_previous() {
        MeterReadingRequest request = MeterReadingRequest.builder()
                .roomId(1)
                .serviceId(1)
                .currentIndex(100.0)
                .build();

        Room room = Room.builder().roomId(1).roomNumber("101").build();
        Service service = Service.builder().serviceId(1).serviceName("Elec").build();

        MeterReading last = MeterReading.builder().currentIndex(50.0).build();
        MeterReading saved = MeterReading.builder()
                .room(room)
                .service(service)
                .previousIndex(50.0)
                .currentIndex(100.0)
                .build();

        when(roomRepository.findById(1)).thenReturn(Optional.of(room));
        when(serviceRepository.findById(1)).thenReturn(Optional.of(service));
        when(meterReadingRepository.findFirstByRoom_RoomIdAndService_ServiceIdOrderByReadingDateDesc(1, 1))
                .thenReturn(Optional.of(last));
        when(meterReadingRepository.save(any(MeterReading.class))).thenReturn(saved);

        MeterReadingResponse response = meterReadingService.recordReading(request);
        assertEquals(50.0, response.getPreviousIndex());
        assertEquals(100.0, response.getCurrentIndex());
    }
}
