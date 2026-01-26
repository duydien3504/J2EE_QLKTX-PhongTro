package com.group10.API_ManageDormitory.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "Floors")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Floor {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "floor_id")
    private Integer floorId;

    @Column(name = "floor_name", nullable = false)
    private String floorName;

    @ManyToOne
    @JoinColumn(name = "building_id", nullable = false)
    private Building building;
}
