package com.group10.API_ManageDormitory.repository;

import com.group10.API_ManageDormitory.entity.MeterReading;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface MeterReadingRepository extends JpaRepository<MeterReading, Integer> {
    List<MeterReading> findByRoom_RoomId(Integer roomId);

    Optional<MeterReading> findFirstByRoom_RoomIdAndService_ServiceIdOrderByReadingDateDesc(Integer roomId,
            Integer serviceId);

    // For filtering by month (approximate range)
    List<MeterReading> findByRoom_RoomIdAndReadingDateBetween(Integer roomId, LocalDate startDate, LocalDate endDate);

    List<MeterReading> findByReadingDateBetween(LocalDate startDate, LocalDate endDate);
}
