package com.group10.API_ManageDormitory.constant;

import java.util.Set;

public final class RoomImageConstant {

    private RoomImageConstant() {}

    public static final int MAX_IMAGES_PER_ROOM = 10;
    public static final long MAX_FILE_SIZE_BYTES = 5 * 1024 * 1024; // 5 MB
    public static final Set<String> ALLOWED_CONTENT_TYPES = Set.of(
            "image/jpeg",
            "image/jpg",
            "image/png"
    );
}
