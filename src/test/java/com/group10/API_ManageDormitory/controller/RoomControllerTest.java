package com.group10.API_ManageDormitory.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.group10.API_ManageDormitory.dtos.request.RoomRequest;
import com.group10.API_ManageDormitory.dtos.response.RoomResponse;
import com.group10.API_ManageDormitory.exception.AppException;
import com.group10.API_ManageDormitory.exception.ErrorCode;
import com.group10.API_ManageDormitory.exception.GlobalExceptionHandler;
import com.group10.API_ManageDormitory.service.RoomService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class RoomControllerTest {

    private MockMvc mockMvc;

    @Mock
    private RoomService roomService;

    @InjectMocks
    private RoomController roomController;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setup() {
        mockMvc = MockMvcBuilders.standaloneSetup(roomController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    // ─────────────────────────────────────────────────────────
    // GET /api/v1/rooms
    // ─────────────────────────────────────────────────────────

    @Test
    void getRooms_noFilter_returnsList() throws Exception {
        RoomResponse room = RoomResponse.builder().roomId(1).roomNumber("101").currentStatus("AVAILABLE").build();
        when(roomService.getRooms(null, null, null, null)).thenReturn(List.of(room));

        mockMvc.perform(get("/api/v1/rooms"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result[0].roomNumber").value("101"));
    }

    @Test
    void getRooms_withFilters_callsServiceWithParams() throws Exception {
        when(roomService.getRooms(eq(1), eq("AVAILABLE"), any(), any())).thenReturn(List.of());

        mockMvc.perform(get("/api/v1/rooms")
                        .param("floorId", "1")
                        .param("status", "AVAILABLE"))
                .andExpect(status().isOk());

        verify(roomService).getRooms(eq(1), eq("AVAILABLE"), isNull(), isNull());
    }

    // ─────────────────────────────────────────────────────────
    // GET /api/v1/rooms/{id}
    // ─────────────────────────────────────────────────────────

    @Test
    void getRoom_success() throws Exception {
        RoomResponse room = RoomResponse.builder().roomId(1).roomNumber("101").build();
        when(roomService.getRoomDetail(1)).thenReturn(room);

        mockMvc.perform(get("/api/v1/rooms/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result.roomId").value(1));
    }

    @Test
    void getRoom_notFound_returns404() throws Exception {
        when(roomService.getRoomDetail(99)).thenThrow(new AppException(ErrorCode.ROOM_NOT_FOUND));

        mockMvc.perform(get("/api/v1/rooms/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value(1014));
    }

    // ─────────────────────────────────────────────────────────
    // POST /api/v1/rooms
    // ─────────────────────────────────────────────────────────

    @Test
    void createRoom_success() throws Exception {
        RoomRequest request = RoomRequest.builder()
                .roomNumber("201")
                .floorId(1)
                .roomTypeId(1)
                .build();
        RoomResponse response = RoomResponse.builder().roomId(2).roomNumber("201").currentStatus("AVAILABLE").build();
        when(roomService.createRoom(any())).thenReturn(response);

        mockMvc.perform(post("/api/v1/rooms")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result.roomNumber").value("201"));
    }

    @Test
    void createRoom_missingRoomNumber_badRequest() throws Exception {
        // roomNumber is @NotBlank
        RoomRequest request = RoomRequest.builder().floorId(1).roomTypeId(1).build();

        mockMvc.perform(post("/api/v1/rooms")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    // ─────────────────────────────────────────────────────────
    // PUT /api/v1/rooms/{id}
    // ─────────────────────────────────────────────────────────

    @Test
    void updateRoom_success() throws Exception {
        RoomRequest request = RoomRequest.builder().roomNumber("999").floorId(1).roomTypeId(1).build();
        RoomResponse response = RoomResponse.builder().roomId(1).roomNumber("999").build();
        when(roomService.updateRoom(eq(1), any())).thenReturn(response);

        mockMvc.perform(put("/api/v1/rooms/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result.roomNumber").value("999"));
    }

    @Test
    void updateRoom_notFound_returns404() throws Exception {
        RoomRequest request = RoomRequest.builder().roomNumber("999").floorId(1).roomTypeId(1).build();
        when(roomService.updateRoom(eq(99), any())).thenThrow(new AppException(ErrorCode.ROOM_NOT_FOUND));

        mockMvc.perform(put("/api/v1/rooms/99")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value(1014));
    }

    // ─────────────────────────────────────────────────────────
    // PATCH /api/v1/rooms/{id}/status
    // ─────────────────────────────────────────────────────────

    @Test
    void updateRoomStatus_success() throws Exception {
        RoomResponse response = RoomResponse.builder().roomId(1).currentStatus("MAINTENANCE").build();
        when(roomService.updateRoomStatus(eq(1), eq("MAINTENANCE"))).thenReturn(response);

        mockMvc.perform(patch("/api/v1/rooms/1/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\":\"MAINTENANCE\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result.currentStatus").value("MAINTENANCE"));
    }

    @Test
    void updateRoomStatus_missingStatusKey_throws500() throws Exception {
        // When "status" key is absent → RuntimeException from controller guard
        mockMvc.perform(patch("/api/v1/rooms/1/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isInternalServerError());
    }

    // ─────────────────────────────────────────────────────────
    // POST /api/v1/rooms/{id}/images  (multipart)
    // ─────────────────────────────────────────────────────────

    @Test
    void uploadRoomImages_success() throws Exception {
        RoomResponse response = RoomResponse.builder()
                .roomId(1)
                .roomNumber("101")
                .imageUrls(List.of("http://cdn/img1.jpg"))
                .build();
        when(roomService.uploadRoomImages(eq(1), anyList())).thenReturn(response);

        MockMultipartFile image = new MockMultipartFile(
                "images", "photo.jpg", MediaType.IMAGE_JPEG_VALUE, new byte[]{1, 2, 3});

        mockMvc.perform(multipart("/api/v1/rooms/1/images").file(image))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result.imageUrls[0]").value("http://cdn/img1.jpg"));
    }

    @Test
    void uploadRoomImages_roomNotFound_returns404() throws Exception {
        when(roomService.uploadRoomImages(eq(99), anyList()))
                .thenThrow(new AppException(ErrorCode.ROOM_NOT_FOUND));

        MockMultipartFile image = new MockMultipartFile(
                "images", "photo.jpg", MediaType.IMAGE_JPEG_VALUE, new byte[]{1});

        mockMvc.perform(multipart("/api/v1/rooms/99/images").file(image))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value(1014));
    }

    @Test
    void uploadRoomImages_limitExceeded_returns400() throws Exception {
        when(roomService.uploadRoomImages(eq(1), anyList()))
                .thenThrow(new AppException(ErrorCode.ROOM_IMAGE_LIMIT_EXCEEDED));

        MockMultipartFile image = new MockMultipartFile(
                "images", "photo.jpg", MediaType.IMAGE_JPEG_VALUE, new byte[]{1});

        mockMvc.perform(multipart("/api/v1/rooms/1/images").file(image))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(1022));
    }

    @Test
    void uploadRoomImages_invalidFormat_returns400() throws Exception {
        when(roomService.uploadRoomImages(eq(1), anyList()))
                .thenThrow(new AppException(ErrorCode.INVALID_IMAGE_FORMAT));

        MockMultipartFile gifFile = new MockMultipartFile(
                "images", "anim.gif", "image/gif", new byte[]{1});

        mockMvc.perform(multipart("/api/v1/rooms/1/images").file(gifFile))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(1023));
    }
}
