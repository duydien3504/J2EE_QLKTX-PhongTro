package com.group10.API_ManageDormitory.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "Permissions")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Permission {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "permission_id")
    private Integer permissionId;

    @Column(name = "permission_code", nullable = false, unique = true)
    private String permissionCode;

    @Column(name = "description")
    private String description;
}
