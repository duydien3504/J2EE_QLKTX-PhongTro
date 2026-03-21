package com.group10.API_ManageDormitory.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "Services")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Service {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "service_id")
    private Integer serviceId;

    @Column(name = "service_name", nullable = false)
    private String serviceName;

    @Column(name = "unit")
    private String unit;

    @Column(name = "calculation_method")
    private String calculationMethod;

    @Column(name = "icon")
    private String icon;
}
