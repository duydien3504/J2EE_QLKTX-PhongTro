package com.group10.API_ManageDormitory.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;

@Entity
@Table(name = "InvoiceDetails")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InvoiceDetail {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "invoice_detail_id")
    private Integer invoiceDetailId;

    @ManyToOne
    @JoinColumn(name = "invoice_id", nullable = false)
    private Invoice invoice;

    @Column(name = "service_name")
    private String serviceName;

    @Column(name = "quantity")
    private Double quantity;

    @Column(name = "old_index")
    private Double oldIndex;

    @Column(name = "new_index")
    private Double newIndex;

    @Column(name = "unit_price", precision = 10, scale = 2)
    private BigDecimal unitPrice;

    @Column(name = "amount", precision = 10, scale = 2)
    private BigDecimal amount;
}
