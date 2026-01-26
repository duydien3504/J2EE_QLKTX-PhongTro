package com.group10.API_ManageDormitory.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "Buildings")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Building {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "building_id")
    private Integer buildingId;

    @Column(name = "building_name", nullable = false)
    private String buildingName;

    @Column(name = "address")
    private String address;

    @ManyToOne
    @JoinColumn(name = "manager_user_id")
    private User manager;

    @Column(name = "total_floors")
    private Integer totalFloors;
}
