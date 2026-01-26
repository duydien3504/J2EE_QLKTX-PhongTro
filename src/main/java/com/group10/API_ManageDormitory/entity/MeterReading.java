package com.group10.API_ManageDormitory.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;

@Entity
@Table(name = "MeterReadings")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MeterReading {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "meter_reading_id")
    private Integer meterReadingId;

    @ManyToOne
    @JoinColumn(name = "room_id", nullable = false)
    private Room room;

    @ManyToOne
    @JoinColumn(name = "service_id", nullable = false)
    private Service service;

    @Column(name = "previous_index")
    private Double previousIndex;

    @Column(name = "current_index")
    private Double currentIndex;

    @Column(name = "reading_date")
    private LocalDate readingDate;

    @Column(name = "image_proof")
    private String imageProof;
}
