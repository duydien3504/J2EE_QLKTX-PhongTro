package com.group10.API_ManageDormitory.dtos.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RoomStatusStatisticsResponse {
    private long emptyRooms;
    private long rentedRooms;
    private long totalRooms;
}
