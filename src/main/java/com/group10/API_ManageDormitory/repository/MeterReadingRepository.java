package com.group10.API_ManageDormitory.repository;

import com.group10.API_ManageDormitory.entity.MeterReading;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MeterReadingRepository extends JpaRepository<MeterReading, Integer> {
}
