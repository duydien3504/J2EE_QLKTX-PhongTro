package com.group10.API_ManageDormitory.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "Tenants")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Tenant {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "tenant_id")
    private Integer tenantId;

    @Column(name = "full_name", nullable = false)
    private String fullName;

    @Column(name = "phone_number")
    private String phoneNumber;

    @Column(name = "email")
    private String email;

    @Column(name = "hometown")
    private String hometown;

    @Column(name = "identity_card_number", unique = true)
    private String identityCardNumber;

    @Column(name = "identity_card_image_front")
    private String identityCardImageFront;

    @Column(name = "identity_card_image_back")
    private String identityCardImageBack;

    @OneToOne
    @JoinColumn(name = "user_id")
    private User user;
}
