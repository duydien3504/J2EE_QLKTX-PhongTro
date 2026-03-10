package com.group10.API_ManageDormitory.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum ErrorCode {
    UNCATEGORIZED_EXCEPTION(9999, "Uncategorized error", HttpStatus.INTERNAL_SERVER_ERROR),
    INVALID_KEY(1001, "Uncategorized error", HttpStatus.BAD_REQUEST),
    USER_EXISTED(1002, "User existed", HttpStatus.BAD_REQUEST),
    USERNAME_INVALID(1003, "Username must be at least 3 characters", HttpStatus.BAD_REQUEST),
    INVALID_PASSWORD(1004, "Password must be at least 8 characters", HttpStatus.BAD_REQUEST),
    USER_NOT_EXISTED(1005, "User not existed", HttpStatus.NOT_FOUND),
    UNAUTHENTICATED(1006, "Unauthenticated", HttpStatus.UNAUTHORIZED),
    UNAUTHORIZED(1007, "You do not have permission", HttpStatus.FORBIDDEN),
    INVALID_DOB(1008, "Your age must be at least {min}", HttpStatus.BAD_REQUEST),
    EMAIL_EXISTED(1009, "Email already exists", HttpStatus.BAD_REQUEST),
    ROLE_NOT_FOUND(1010, "Role not found", HttpStatus.NOT_FOUND),
    PASSWORD_INCORRECT(1011, "Password incorrect", HttpStatus.BAD_REQUEST),
    TOKEN_INVALID(1012, "Token invalid or expired", HttpStatus.UNAUTHORIZED),
    CONTRACT_NOT_FOUND(1013, "Contract not found", HttpStatus.NOT_FOUND),
    ROOM_NOT_FOUND(1014, "Room not found", HttpStatus.NOT_FOUND),
    TENANT_NOT_FOUND(1015, "Tenant not found", HttpStatus.NOT_FOUND),
    ROOM_NOT_AVAILABLE(1016, "Room is not available", HttpStatus.BAD_REQUEST),
    OCCUPANCY_LIMIT_REACHED(1017, "Room occupancy limit reached", HttpStatus.BAD_REQUEST),
    TENANT_ALREADY_IN_CONTRACT(1018, "Tenant already in contract", HttpStatus.BAD_REQUEST),
    INCIDENT_NOT_FOUND(1019, "Incident not found", HttpStatus.NOT_FOUND),
    NOTIFICATION_NOT_FOUND(1020, "Notification not found", HttpStatus.NOT_FOUND);

    private final int code;
    private final String message;
    private final HttpStatus statusCode;
}
