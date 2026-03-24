package com.group10.API_ManageDormitory.controller;

import com.group10.API_ManageDormitory.dtos.response.ApiResponse;
import com.group10.API_ManageDormitory.dtos.response.MyRoomResponse;
import com.group10.API_ManageDormitory.service.MyRoomService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/my-room")
@RequiredArgsConstructor
public class MyRoomController {
    private final MyRoomService myRoomService;

    @GetMapping
    @PreAuthorize("hasAuthority('SCOPE_USER') or hasAuthority('SCOPE_TENANT')")
    public ApiResponse<MyRoomResponse> getMyRoom() {
        return ApiResponse.<MyRoomResponse>builder()
                .result(myRoomService.getMyRoom())
                .build();
    }
}
