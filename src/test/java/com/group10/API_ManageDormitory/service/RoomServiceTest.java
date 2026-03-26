package com.group10.API_ManageDormitory.service;

import com.group10.API_ManageDormitory.constant.RoomImageConstant;
import com.group10.API_ManageDormitory.dtos.request.RoomRequest;
import com.group10.API_ManageDormitory.dtos.response.RoomResponse;
import com.group10.API_ManageDormitory.entity.Building;
import com.group10.API_ManageDormitory.entity.Floor;
import com.group10.API_ManageDormitory.entity.Room;
import com.group10.API_ManageDormitory.entity.RoomImage;
import com.group10.API_ManageDormitory.entity.RoomType;
import com.group10.API_ManageDormitory.exception.AppException;
import com.group10.API_ManageDormitory.exception.ErrorCode;
import com.group10.API_ManageDormitory.repository.FloorRepository;
import com.group10.API_ManageDormitory.repository.RoomImageRepository;
import com.group10.API_ManageDormitory.repository.RoomRepository;
import com.group10.API_ManageDormitory.repository.RoomTypeRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RoomServiceTest {

    @Mock
    private RoomRepository roomRepository;
    @Mock
    private RoomTypeRepository roomTypeRepository;
    @Mock
    private FloorRepository floorRepository;
    @Mock
    private RoomImageRepository roomImageRepository;
    @Mock
    private CloudinaryService cloudinaryService;
    @Mock
    private AccessValidationService accessValidationService;

    @InjectMocks
    private RoomService roomService;

    // ─── helpers ────────────────────────────────────────────────────────────────

    private Building buildBuilding() {
        return Building.builder().buildingName("B1").build();
    }

    private Floor buildFloor(Integer id) {
        return Floor.builder().floorId(id).floorName("F1").building(buildBuilding()).build();
    }

    private RoomType buildRoomType(Integer id) {
        return RoomType.builder().roomTypeId(id).typeName("Standard").basePrice(BigDecimal.valueOf(500)).build();
    }

    private Room buildRoom(Integer id) {
        return Room.builder()
                .roomId(id)
                .roomNumber("10" + id)
                .floor(buildFloor(1))
                .roomType(buildRoomType(1))
                .currentStatus("AVAILABLE")
                .build();
    }

    // ─────────────────────────────────────────────────────────
    // getRooms (filter)
    // ─────────────────────────────────────────────────────────

    @Test
    void getRooms_filterByFloor_match() {
        Room room = buildRoom(1);
        when(roomRepository.findAll()).thenReturn(List.of(room));
        when(roomImageRepository.findByRoom_RoomId(any())).thenReturn(Collections.emptyList());

        List<RoomResponse> result = roomService.getRooms(1, null, null, null);

        assertFalse(result.isEmpty());
        assertEquals("101", result.get(0).getRoomNumber());
    }

    @Test
    void getRooms_filterByFloor_noMatch() {
        Room room = buildRoom(1);
        when(roomRepository.findAll()).thenReturn(List.of(room));

        List<RoomResponse> result = roomService.getRooms(99, null, null, null);

        assertTrue(result.isEmpty());
    }

    @Test
    void getRooms_filterByStatus_match() {
        Room room = buildRoom(1);
        when(roomRepository.findAll()).thenReturn(List.of(room));
        when(roomImageRepository.findByRoom_RoomId(any())).thenReturn(Collections.emptyList());

        List<RoomResponse> result = roomService.getRooms(null, "available", null, null); // case-insensitive

        assertFalse(result.isEmpty());
    }

    @Test
    void getRooms_filterByPrice_match() {
        Room room = buildRoom(1); // price = 500
        when(roomRepository.findAll()).thenReturn(List.of(room));
        when(roomImageRepository.findByRoom_RoomId(any())).thenReturn(Collections.emptyList());

        List<RoomResponse> result = roomService.getRooms(null, null, BigDecimal.valueOf(100), BigDecimal.valueOf(600));

        assertFalse(result.isEmpty());
    }

    @Test
    void getRooms_filterByPrice_noMatch() {
        Room room = buildRoom(1); // price = 500
        when(roomRepository.findAll()).thenReturn(List.of(room));

        List<RoomResponse> result = roomService.getRooms(null, null, BigDecimal.valueOf(600), null);

        assertTrue(result.isEmpty());
    }

    // ─────────────────────────────────────────────────────────
    // getRoomDetail
    // ─────────────────────────────────────────────────────────

    @Test
    void getRoomDetail_success() {
        Room room = buildRoom(1);
        when(roomRepository.findById(1)).thenReturn(Optional.of(room));
        when(roomImageRepository.findByRoom_RoomId(1)).thenReturn(Collections.emptyList());

        RoomResponse response = roomService.getRoomDetail(1);

        assertEquals(1, response.getRoomId());
        assertEquals("AVAILABLE", response.getCurrentStatus());
        assertNotNull(response.getImageUrls());
        assertTrue(response.getImageUrls().isEmpty());
    }

    @Test
    void getRoomDetail_notFound_throwsAppException() {
        when(roomRepository.findById(99)).thenReturn(Optional.empty());

        AppException ex = assertThrows(AppException.class, () -> roomService.getRoomDetail(99));
        assertEquals(ErrorCode.ROOM_NOT_FOUND, ex.getErrorCode());
    }

    // ─────────────────────────────────────────────────────────
    // createRoom
    // ─────────────────────────────────────────────────────────

    @Test
    void createRoom_success_defaultStatusAvailable() {
        RoomRequest request = RoomRequest.builder()
                .roomNumber("201")
                .floorId(1)
                .roomTypeId(1)
                .build(); // currentStatus = null → defaults to AVAILABLE

        Floor floor = buildFloor(1);
        RoomType type = buildRoomType(1);
        Room saved = buildRoom(2);

        when(floorRepository.findById(1)).thenReturn(Optional.of(floor));
        when(roomTypeRepository.findById(1)).thenReturn(Optional.of(type));
        when(roomRepository.save(any(Room.class))).thenReturn(saved);
        when(roomImageRepository.findByRoom_RoomId(any())).thenReturn(Collections.emptyList());

        RoomResponse response = roomService.createRoom(request);

        assertNotNull(response);
        verify(roomRepository, times(1)).save(any(Room.class));
    }

    @Test
    void createRoom_floorNotFound_throwsAppException() {
        RoomRequest request = RoomRequest.builder().roomNumber("202").floorId(99).roomTypeId(1).build();
        when(floorRepository.findById(99)).thenReturn(Optional.empty());

        AppException ex = assertThrows(AppException.class, () -> roomService.createRoom(request));
        assertEquals(ErrorCode.RESOURCE_NOT_FOUND, ex.getErrorCode());
    }

    @Test
    void createRoom_roomTypeNotFound_throwsAppException() {
        RoomRequest request = RoomRequest.builder().roomNumber("203").floorId(1).roomTypeId(99).build();
        Floor floor = buildFloor(1);

        when(floorRepository.findById(1)).thenReturn(Optional.of(floor));
        when(roomTypeRepository.findById(99)).thenReturn(Optional.empty());

        AppException ex = assertThrows(AppException.class, () -> roomService.createRoom(request));
        assertEquals(ErrorCode.RESOURCE_NOT_FOUND, ex.getErrorCode());
    }

    // ─────────────────────────────────────────────────────────
    // updateRoom
    // ─────────────────────────────────────────────────────────

    @Test
    void updateRoom_success_partialUpdate() {
        Room room = buildRoom(1);
        RoomRequest request = RoomRequest.builder()
                .roomNumber("999")
                .floorId(null)
                .roomTypeId(null)
                .build();

        when(roomRepository.findById(1)).thenReturn(Optional.of(room));
        when(roomRepository.save(any(Room.class))).thenReturn(room);
        when(roomImageRepository.findByRoom_RoomId(any())).thenReturn(Collections.emptyList());

        RoomResponse response = roomService.updateRoom(1, request);

        assertEquals("999", room.getRoomNumber());
        assertNotNull(response);
    }

    @Test
    void updateRoom_notFound_throwsAppException() {
        when(roomRepository.findById(99)).thenReturn(Optional.empty());

        AppException ex = assertThrows(AppException.class,
                () -> roomService.updateRoom(99, RoomRequest.builder().build()));
        assertEquals(ErrorCode.ROOM_NOT_FOUND, ex.getErrorCode());
    }

    // ─────────────────────────────────────────────────────────
    // updateRoomStatus
    // ─────────────────────────────────────────────────────────

    @Test
    void updateRoomStatus_success() {
        Room room = buildRoom(1);
        when(roomRepository.findById(1)).thenReturn(Optional.of(room));
        when(roomRepository.save(any(Room.class))).thenReturn(room);
        when(roomImageRepository.findByRoom_RoomId(any())).thenReturn(Collections.emptyList());

        RoomResponse response = roomService.updateRoomStatus(1, "MAINTENANCE");

        assertEquals("MAINTENANCE", room.getCurrentStatus());
        assertNotNull(response);
    }

    @Test
    void updateRoomStatus_roomNotFound_throwsAppException() {
        when(roomRepository.findById(99)).thenReturn(Optional.empty());

        AppException ex = assertThrows(AppException.class, () -> roomService.updateRoomStatus(99, "MAINTENANCE"));
        assertEquals(ErrorCode.ROOM_NOT_FOUND, ex.getErrorCode());
    }

    // ─────────────────────────────────────────────────────────
    // uploadRoomImages
    // ─────────────────────────────────────────────────────────

    @Test
    void uploadRoomImages_success_firstImageSetAsPrimary() throws IOException {
        Room room = buildRoom(1);
        MultipartFile file1 = new MockMultipartFile("img1", "img1.jpg", "image/jpeg", new byte[]{1});
        MultipartFile file2 = new MockMultipartFile("img2", "img2.png", "image/png", new byte[]{2});

        when(roomRepository.findById(1)).thenReturn(Optional.of(room));
        when(roomImageRepository.countByRoom_RoomId(1)).thenReturn(0L); // no existing images
        when(cloudinaryService.uploadImage(file1)).thenReturn("http://cdn/img1.jpg");
        when(cloudinaryService.uploadImage(file2)).thenReturn("http://cdn/img2.png");
        when(roomImageRepository.saveAll(anyList())).thenReturn(Collections.emptyList());
        when(roomRepository.findById(1)).thenReturn(Optional.of(room));
        when(roomImageRepository.findByRoom_RoomId(1)).thenReturn(List.of(
                RoomImage.builder().imageUrl("http://cdn/img1.jpg").isPrimary(true).room(room).build(),
                RoomImage.builder().imageUrl("http://cdn/img2.png").isPrimary(false).room(room).build()
        ));

        RoomResponse response = roomService.uploadRoomImages(1, List.of(file1, file2));

        assertNotNull(response);
        assertEquals(2, response.getImageUrls().size());
        verify(roomImageRepository, times(1)).saveAll(anyList());
    }

    @Test
    void uploadRoomImages_roomNotFound_throwsAppException() {
        when(roomRepository.findById(99)).thenReturn(Optional.empty());

        AppException ex = assertThrows(AppException.class,
                () -> roomService.uploadRoomImages(99, List.of()));
        assertEquals(ErrorCode.ROOM_NOT_FOUND, ex.getErrorCode());
    }

    @Test
    void uploadRoomImages_exceedsMaxLimit_throwsAppException() {
        Room room = buildRoom(1);
        when(roomRepository.findById(1)).thenReturn(Optional.of(room));
        when(roomImageRepository.countByRoom_RoomId(1)).thenReturn(8L); // already 8 images

        // Trying to upload 3 more → 8+3=11 > 10
        List<MultipartFile> files = List.of(
                new MockMultipartFile("img", "a.jpg", "image/jpeg", new byte[]{1}),
                new MockMultipartFile("img", "b.jpg", "image/jpeg", new byte[]{2}),
                new MockMultipartFile("img", "c.jpg", "image/jpeg", new byte[]{3})
        );

        AppException ex = assertThrows(AppException.class,
                () -> roomService.uploadRoomImages(1, files));
        assertEquals(ErrorCode.ROOM_IMAGE_LIMIT_EXCEEDED, ex.getErrorCode());
    }

    @Test
    void uploadRoomImages_exactlyAtLimit_success() throws IOException {
        Room room = buildRoom(1);
        when(roomRepository.findById(1)).thenReturn(Optional.of(room));
        when(roomImageRepository.countByRoom_RoomId(1)).thenReturn(8L); // 8 + 2 = 10 ✓

        MultipartFile f1 = new MockMultipartFile("img", "a.jpg", "image/jpeg", new byte[]{1});
        MultipartFile f2 = new MockMultipartFile("img", "b.png", "image/png", new byte[]{2});

        when(cloudinaryService.uploadImage(f1)).thenReturn("http://cdn/a.jpg");
        when(cloudinaryService.uploadImage(f2)).thenReturn("http://cdn/b.png");
        when(roomImageRepository.saveAll(anyList())).thenReturn(Collections.emptyList());
        when(roomRepository.findById(1)).thenReturn(Optional.of(room));
        when(roomImageRepository.findByRoom_RoomId(any())).thenReturn(Collections.emptyList());

        assertDoesNotThrow(() -> roomService.uploadRoomImages(1, List.of(f1, f2)));
    }

    @Test
    void uploadRoomImages_invalidFormat_throwsAppException() {
        Room room = buildRoom(1);
        when(roomRepository.findById(1)).thenReturn(Optional.of(room));
        when(roomImageRepository.countByRoom_RoomId(1)).thenReturn(0L);

        // GIF is not allowed
        MultipartFile gifFile = new MockMultipartFile("img", "anim.gif", "image/gif", new byte[]{1});

        AppException ex = assertThrows(AppException.class,
                () -> roomService.uploadRoomImages(1, List.of(gifFile)));
        assertEquals(ErrorCode.INVALID_IMAGE_FORMAT, ex.getErrorCode());
    }

    @Test
    void uploadRoomImages_nullContentType_throwsAppException() {
        Room room = buildRoom(1);
        when(roomRepository.findById(1)).thenReturn(Optional.of(room));
        when(roomImageRepository.countByRoom_RoomId(1)).thenReturn(0L);

        // contentType = null
        MultipartFile badFile = new MockMultipartFile("img", "file.bin", null, new byte[]{1});

        AppException ex = assertThrows(AppException.class,
                () -> roomService.uploadRoomImages(1, List.of(badFile)));
        assertEquals(ErrorCode.INVALID_IMAGE_FORMAT, ex.getErrorCode());
    }

    @Test
    void uploadRoomImages_maxConstant_isCorrect() {
        // O(1) constant verification – no need for IO
        assertEquals(10, RoomImageConstant.MAX_IMAGES_PER_ROOM);
    }
}
