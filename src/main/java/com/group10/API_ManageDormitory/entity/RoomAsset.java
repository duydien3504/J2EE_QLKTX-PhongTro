package com.group10.API_ManageDormitory.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "RoomAssets")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RoomAsset {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "room_asset_id")
    private Integer roomAssetId;

    @ManyToOne
    @JoinColumn(name = "room_id", nullable = false)
    private Room room;

    @ManyToOne
    @JoinColumn(name = "asset_id", nullable = false)
    private Asset asset;

    @Column(name = "quantity")
    private Integer quantity;

    @Column(name = "condition_status")
    private String conditionStatus;
}
