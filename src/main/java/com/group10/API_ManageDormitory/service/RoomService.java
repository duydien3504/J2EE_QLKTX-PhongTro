package com.group10.API_ManageDormitory.service;

import com.group10.API_ManageDormitory.constant.RoomImageConstant;
import com.group10.API_ManageDormitory.dtos.request.RoomRequest;
import com.group10.API_ManageDormitory.dtos.request.RoomTypeRequest;
import com.group10.API_ManageDormitory.dtos.response.RoomResponse;
import com.group10.API_ManageDormitory.dtos.response.RoomTypeResponse;
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
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RoomService {
    private final RoomRepository roomRepository;
    private final RoomTypeRepository roomTypeRepository;
    private final FloorRepository floorRepository;
    private final RoomImageRepository roomImageRepository;
    private final CloudinaryService cloudinaryService;

    // RoomType Operations
    public List<RoomTypeResponse> getAllRoomTypes() {
        return roomTypeRepository.findAll().stream()
                .map(this::toRoomTypeResponse)
                .collect(Collectors.toList());
    }

    public RoomTypeResponse createRoomType(RoomTypeRequest request) {
        RoomType roomType = RoomType.builder()
                .typeName(request.getTypeName())
                .basePrice(request.getBasePrice())
                .area(request.getArea())
                .maxOccupancy(request.getMaxOccupancy())
                .description(request.getDescription())
                .build();
        return toRoomTypeResponse(roomTypeRepository.save(roomType));
    }

    public RoomTypeResponse updateRoomType(Integer id, RoomTypeRequest request) {
        RoomType roomType = roomTypeRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND));
        if (request.getTypeName() != null) roomType.setTypeName(request.getTypeName());
        if (request.getBasePrice() != null) roomType.setBasePrice(request.getBasePrice());
        if (request.getArea() != null) roomType.setArea(request.getArea());
        if (request.getMaxOccupancy() != null) roomType.setMaxOccupancy(request.getMaxOccupancy());
        if (request.getDescription() != null) roomType.setDescription(request.getDescription());
        return toRoomTypeResponse(roomTypeRepository.save(roomType));
    }

    public void deleteRoomType(Integer id) {
        RoomType roomType = roomTypeRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND));
        if (roomRepository.existsByRoomType_RoomTypeId(id)) {
            throw new AppException(ErrorCode.ROOM_TYPE_IN_USE);
        }
        roomTypeRepository.delete(roomType);
    }

    // Room Operations
    public List<RoomResponse> getRooms(Integer floorId, String status, BigDecimal minPrice, BigDecimal maxPrice) {
        String username = com.group10.API_ManageDormitory.utils.SecurityUtils.getCurrentUsername();
        boolean isAdmin = com.group10.API_ManageDormitory.utils.SecurityUtils.hasRole("SCOPE_ADMIN")
                || com.group10.API_ManageDormitory.utils.SecurityUtils.hasRole("ADMIN");

        List<Room> rooms;
        if (isAdmin || username == null) {
            rooms = roomRepository.findAll();
        } else {
            rooms = roomRepository.findAllByFloor_Building_Manager_Username(username);
        }

        // Manual Filtering
        return rooms.stream()
                .filter(room -> floorId == null || room.getFloor().getFloorId().equals(floorId))
                .filter(room -> status == null || status.trim().isEmpty()
                        || (room.getCurrentStatus() != null && room.getCurrentStatus().equalsIgnoreCase(status)))
                .filter(room -> minPrice == null || room.getRoomType().getBasePrice().compareTo(minPrice) >= 0)
                .filter(room -> maxPrice == null || room.getRoomType().getBasePrice().compareTo(maxPrice) <= 0)
                .map(this::toRoomResponse)
                .collect(Collectors.toList());
    }

    public RoomResponse getRoomDetail(Integer id) {
        Room room = roomRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.ROOM_NOT_FOUND));
        checkRoomOwnership(room);
        return toRoomResponse(room);
    }

    public RoomResponse createRoom(RoomRequest request) {
        Floor floor = floorRepository.findById(request.getFloorId())
                .orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND));
        checkFloorOwnership(floor);

        RoomType roomType = roomTypeRepository.findById(request.getRoomTypeId())
                .orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND));

        String status = request.getCurrentStatus() != null ? request.getCurrentStatus() : "AVAILABLE";

        Room room = Room.builder()
                .roomNumber(request.getRoomNumber())
                .floor(floor)
                .roomType(roomType)
                .currentStatus(status)
                .build();

        return toRoomResponse(roomRepository.save(room));
    }

    public RoomResponse updateRoom(Integer id, RoomRequest request) {
        Room room = roomRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.ROOM_NOT_FOUND));
        checkRoomOwnership(room);

        if (request.getRoomNumber() != null)
            room.setRoomNumber(request.getRoomNumber());
        if (request.getCurrentStatus() != null)
            room.setCurrentStatus(request.getCurrentStatus());

        if (request.getFloorId() != null) {
            Floor floor = floorRepository.findById(request.getFloorId())
                    .orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND));
            checkFloorOwnership(floor); // ensure the new floor is also owned by the user
            room.setFloor(floor);
        }

        if (request.getRoomTypeId() != null) {
            RoomType roomType = roomTypeRepository.findById(request.getRoomTypeId())
                    .orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND));
            room.setRoomType(roomType);
        }

        return toRoomResponse(roomRepository.save(room));
    }

    public RoomResponse updateRoomStatus(Integer id, String status) {
        Room room = roomRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.ROOM_NOT_FOUND));
        checkRoomOwnership(room);
        room.setCurrentStatus(status);
        return toRoomResponse(roomRepository.save(room));
    }

    /**
     * Upload tối đa 10 ảnh cho phòng.
     * Validate:
     *   - Tổng ảnh hiện tại + ảnh mới <= MAX_IMAGES_PER_ROOM (10)  → O(1) COUNT query
     *   - Định dạng: chỉ chấp nhận image/jpeg, image/jpg, image/png   → O(n) - n là số ảnh upload
     *   - Dung lượng: mỗi file <= 5MB                                  → O(n)
     * Upload song song từng ảnh lên Cloudinary, lưu URL vào RoomImages.
     */
    public RoomResponse uploadRoomImages(Integer roomId, List<MultipartFile> images) throws IOException {
        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new AppException(ErrorCode.ROOM_NOT_FOUND));
        checkRoomOwnership(room);

        long currentImageCount = roomImageRepository.countByRoom_RoomId(roomId);
        if (currentImageCount + images.size() > RoomImageConstant.MAX_IMAGES_PER_ROOM) {
            throw new AppException(ErrorCode.ROOM_IMAGE_LIMIT_EXCEEDED);
        }

        for (MultipartFile file : images) {
            String contentType = file.getContentType();
            if (contentType == null || !RoomImageConstant.ALLOWED_CONTENT_TYPES.contains(contentType)) {
                throw new AppException(ErrorCode.INVALID_IMAGE_FORMAT);
            }
        }

        List<RoomImage> newImages = new ArrayList<>();
        boolean isFirstUpload = currentImageCount == 0;

        for (int i = 0; i < images.size(); i++) {
            String imageUrl;
            try {
                imageUrl = cloudinaryService.uploadImage(images.get(i));
            } catch (IOException | RuntimeException ex) {
                // Bọc lỗi Cloudinary (cả RuntimeException từ SDK khi cloud_name sai)
                // thành AppException có mã lỗi rõ ràng thay vì code 9999
                throw new AppException(ErrorCode.CLOUDINARY_UPLOAD_FAILED);
            }
            RoomImage roomImage = RoomImage.builder()
                    .room(room)
                    .imageUrl(imageUrl)
                    .isPrimary(isFirstUpload && i == 0) // first image of first upload becomes primary
                    .build();
            newImages.add(roomImage);
        }

        roomImageRepository.saveAll(newImages);
        return toRoomResponse(roomRepository.findById(roomId).orElseThrow());
    }

    private void checkRoomOwnership(Room room) {
        String username = com.group10.API_ManageDormitory.utils.SecurityUtils.getCurrentUsername();
        boolean isAdmin = com.group10.API_ManageDormitory.utils.SecurityUtils.hasRole("SCOPE_ADMIN");
        if (isAdmin || username == null) return;

        if (room.getFloor() != null && room.getFloor().getBuilding() != null && room.getFloor().getBuilding().getManager() != null) {
            if (!room.getFloor().getBuilding().getManager().getUsername().equals(username)) {
                throw new AppException(ErrorCode.ACCESS_DENIED_TO_RESOURCE);
            }
        }
    }

    private void checkFloorOwnership(Floor floor) {
        String username = com.group10.API_ManageDormitory.utils.SecurityUtils.getCurrentUsername();
        boolean isAdmin = com.group10.API_ManageDormitory.utils.SecurityUtils.hasRole("SCOPE_ADMIN");
        if (isAdmin || username == null) return;

        if (floor.getBuilding() != null && floor.getBuilding().getManager() != null) {
            if (!floor.getBuilding().getManager().getUsername().equals(username)) {
                throw new AppException(ErrorCode.ACCESS_DENIED_TO_RESOURCE);
            }
        }
    }

    // Mappers
    private RoomTypeResponse toRoomTypeResponse(RoomType roomType) {
        return RoomTypeResponse.builder()
                .roomTypeId(roomType.getRoomTypeId())
                .typeName(roomType.getTypeName())
                .basePrice(roomType.getBasePrice())
                .area(roomType.getArea())
                .maxOccupancy(roomType.getMaxOccupancy())
                .description(roomType.getDescription())
                .build();
    }

    private RoomResponse toRoomResponse(Room room) {
        List<String> imageUrls = roomImageRepository.findByRoom_RoomId(room.getRoomId())
                .stream()
                .map(RoomImage::getImageUrl)
                .collect(Collectors.toList());

        return RoomResponse.builder()
                .roomId(room.getRoomId())
                .roomNumber(room.getRoomNumber())
                .floorId(room.getFloor().getFloorId())
                .floorName(room.getFloor().getFloorName())
                .buildingId(room.getFloor().getBuilding().getBuildingId())
                .buildingName(room.getFloor().getBuilding().getBuildingName())
                .roomTypeId(room.getRoomType().getRoomTypeId())
                .roomTypeName(room.getRoomType().getTypeName())
                .price(room.getRoomType().getBasePrice())
                .currentStatus(room.getCurrentStatus())
                .description(room.getRoomType().getDescription())
                .imageUrls(imageUrls)
                .build();
    }
}

