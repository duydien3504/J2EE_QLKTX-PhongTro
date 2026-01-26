package com.group10.API_ManageDormitory.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;

@Entity
@Table(name = "RoomTypes")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RoomType {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "room_type_id")
    private Integer roomTypeId;

    @Column(name = "type_name", nullable = false)
    private String typeName;

    @Column(name = "base_price", precision = 10, scale = 2)
    private BigDecimal basePrice;

    @Column(name = "area")
    private Double area;

    @Column(name = "max_occupancy")
    private Integer maxOccupancy;

    @Column(name = "description")
    private String description;
}
