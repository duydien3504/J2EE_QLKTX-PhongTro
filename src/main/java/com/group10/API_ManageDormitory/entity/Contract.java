package com.group10.API_ManageDormitory.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import org.hibernate.annotations.SQLDelete;

@Entity
@Table(name = "Contracts")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@SQLDelete(sql = "UPDATE contracts SET is_deleted = true WHERE contract_id = ?")
public class Contract {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "contract_id")
    private Integer contractId;

    @ManyToOne
    @JoinColumn(name = "room_id", nullable = false)
    private Room room;

    @Column(name = "start_date")
    private LocalDate startDate;

    @Column(name = "end_date")
    private LocalDate endDate;

    @Column(name = "deposit_amount", precision = 10, scale = 2)
    private BigDecimal depositAmount;

    @Column(name = "rental_price", precision = 10, scale = 2)
    private BigDecimal rentalPrice;

    @Column(name = "payment_cycle")
    private Integer paymentCycle;

    @Column(name = "contract_status")
    private String contractStatus;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "liquidation_date")
    private LocalDate liquidationDate;

    @Column(name = "deduction_amount", precision = 10, scale = 2)
    private BigDecimal deductionAmount;

    @Column(name = "refund_amount", precision = 10, scale = 2)
    private BigDecimal refundAmount;

    @Column(name = "deduction_reason")
    private String deductionReason;

    @Column(name = "final_electricity_reading", precision = 10, scale = 2)
    private BigDecimal finalElectricityReading;

    @Column(name = "final_water_reading", precision = 10, scale = 2)
    private BigDecimal finalWaterReading;

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
        if (isDeleted == null) {
            isDeleted = false;
        }
    }

    @Builder.Default
    @Column(name = "is_deleted")
    private Boolean isDeleted = false;
}
