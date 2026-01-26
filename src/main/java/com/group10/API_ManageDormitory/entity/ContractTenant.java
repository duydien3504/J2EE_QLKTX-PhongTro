package com.group10.API_ManageDormitory.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "ContractTenants")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ContractTenant {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "contract_tenant_id")
    private Integer contractTenantId;

    @ManyToOne
    @JoinColumn(name = "contract_id", nullable = false)
    private Contract contract;

    @ManyToOne
    @JoinColumn(name = "tenant_id", nullable = false)
    private Tenant tenant;

    @Column(name = "is_representative")
    private Boolean isRepresentative;
}
