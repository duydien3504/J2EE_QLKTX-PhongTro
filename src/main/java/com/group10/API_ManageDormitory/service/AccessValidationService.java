package com.group10.API_ManageDormitory.service;

import com.group10.API_ManageDormitory.entity.Building;
import com.group10.API_ManageDormitory.entity.Contract;
import com.group10.API_ManageDormitory.entity.Room;
import com.group10.API_ManageDormitory.entity.User;
import com.group10.API_ManageDormitory.exception.AppException;
import com.group10.API_ManageDormitory.exception.ErrorCode;
import com.group10.API_ManageDormitory.repository.ContractTenantRepository;
import com.group10.API_ManageDormitory.repository.UserRepository;
import com.group10.API_ManageDormitory.utils.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AccessValidationService {

    private final UserRepository userRepository;
    private final ContractTenantRepository contractTenantRepository;

    public User getCurrentUser() {
        String username = SecurityUtils.getCurrentUsername();
        if (username == null) throw new AppException(ErrorCode.UNAUTHENTICATED);
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));
    }

    public boolean isAdmin() {
        return SecurityUtils.hasRole("SCOPE_ADMIN") || SecurityUtils.hasRole("ADMIN");
    }

    public boolean isManageRole() {
        return SecurityUtils.hasRole("SCOPE_OWNER") || SecurityUtils.hasRole("SCOPE_STAFF") ||
               SecurityUtils.hasRole("OWNER") || SecurityUtils.hasRole("STAFF");
    }

    public boolean isTenant() {
        return SecurityUtils.hasRole("SCOPE_TENANT") || SecurityUtils.hasRole("TENANT");
    }

    public void validateBuildingAccess(Building building) {
        if (!hasBuildingAccess(building)) {
            throw new AppException(ErrorCode.ACCESS_DENIED_TO_RESOURCE);
        }
    }

    public boolean hasBuildingAccess(Building building) {
        if (building == null) return false;
        if (isAdmin()) return true;

        try {
            User currentUser = getCurrentUser();
            boolean isOwner = building.getOwner() != null && building.getOwner().getUserId().equals(currentUser.getUserId());
            boolean isManager = building.getManager() != null && building.getManager().getUserId().equals(currentUser.getUserId());
            return isOwner || isManager;
        } catch (AppException e) {
            return false;
        }
    }

    public void validateRoomAccess(Room room) {
        if (!hasRoomAccess(room)) {
            throw new AppException(ErrorCode.ACCESS_DENIED_TO_RESOURCE);
        }
    }
    
    public boolean hasRoomAccess(Room room) {
        if (room == null || room.getFloor() == null || room.getFloor().getBuilding() == null) {
            return false;
        }
        return hasBuildingAccess(room.getFloor().getBuilding());
    }

    public void validateContractAccess(Contract contract) {
        if (!hasContractAccess(contract)) {
            throw new AppException(ErrorCode.ACCESS_DENIED_TO_RESOURCE);
        }
    }

    public boolean hasContractAccess(Contract contract) {
        if (contract == null || contract.getRoom() == null) {
            return false;
        }

        if (isAdmin()) return true;

        try {
            User currentUser = getCurrentUser();
            if (isTenant()) {
                return contractTenantRepository.findByContract_ContractIdAndContract_IsDeletedFalse(contract.getContractId())
                        .stream().anyMatch(ct -> ct.getTenant().getUser() != null && 
                                               ct.getTenant().getUser().getUserId().equals(currentUser.getUserId()));
            } else {
                return hasRoomAccess(contract.getRoom());
            }
        } catch (AppException e) {
            return false;
        }
    }
}
