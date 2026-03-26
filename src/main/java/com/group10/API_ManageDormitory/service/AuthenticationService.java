package com.group10.API_ManageDormitory.service;

import com.group10.API_ManageDormitory.dtos.request.ChangePasswordRequest;
import com.group10.API_ManageDormitory.dtos.request.UpdateProfileRequest;
import org.springframework.security.core.context.SecurityContextHolder;
import com.group10.API_ManageDormitory.dtos.request.LoginRequest;
import com.group10.API_ManageDormitory.dtos.request.RegisterRequest;
import com.group10.API_ManageDormitory.dtos.response.AuthenticationResponse;
import com.group10.API_ManageDormitory.dtos.response.UserResponse;
import com.group10.API_ManageDormitory.entity.User;
import com.group10.API_ManageDormitory.entity.Role;
import com.group10.API_ManageDormitory.exception.AppException;
import com.group10.API_ManageDormitory.exception.ErrorCode;
import com.group10.API_ManageDormitory.mapper.UserMapper;
import com.group10.API_ManageDormitory.repository.UserRepository;
import com.group10.API_ManageDormitory.repository.RoleRepository;
import com.group10.API_ManageDormitory.repository.TenantRepository;
import com.nimbusds.jose.*;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jwt.JWTClaimsSet;
import lombok.RequiredArgsConstructor;
import lombok.experimental.NonFinal;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;

@Service
@RequiredArgsConstructor
public class AuthenticationService {
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final TenantRepository tenantRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

    @NonFinal
    @Value("${jwt.signerKey:546546456546345345435345345345345345435345634645645612345678901234567890}") // Default key
                                                                                                        // (82 chars)
    protected String SIGNER_KEY;

    public UserResponse register(RegisterRequest request) {
        if (userRepository.findByUsername(request.getUsername()).isPresent()) {
            throw new AppException(ErrorCode.USER_EXISTED);
        }

        User user = userMapper.toUser(request);
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));

        Role role = roleRepository.findByRoleName("Tenant")
                .orElseThrow(() -> new AppException(ErrorCode.ROLE_NOT_FOUND));
        user.setRole(role);
        User savedUser = userRepository.save(user);

        return userMapper.toUserResponse(savedUser, null);
    }

    public AuthenticationResponse login(LoginRequest request) {
        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

        boolean authenticated = passwordEncoder.matches(request.getPassword(), user.getPasswordHash());

        if (!authenticated) {
            throw new AppException(ErrorCode.UNAUTHENTICATED);
        }

        var token = generateToken(user);

        return AuthenticationResponse.builder()
                .token(token)
                .authenticated(true)
                .role(user.getRole() != null ? user.getRole().getRoleName() : null)
                .build();
    }

    public void changePassword(ChangePasswordRequest request) {
        var context = SecurityContextHolder.getContext();
        String name = context.getAuthentication().getName();

        User user = userRepository.findByUsername(name).orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPasswordHash())) {
            throw new AppException(ErrorCode.PASSWORD_INCORRECT);
        }

        user.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
    }

    public UserResponse getMyInfo() {
        var context = SecurityContextHolder.getContext();
        String name = context.getAuthentication().getName();

        User user = userRepository.findByUsername(name).orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));
        
        Integer tenantId = tenantRepository.findByUser_UserId(user.getUserId())
                .map(t -> t.getTenantId()).orElse(null);

        return userMapper.toUserResponse(user, tenantId);
    }

    public UserResponse updateProfile(UpdateProfileRequest request) {
        var context = SecurityContextHolder.getContext();
        String name = context.getAuthentication().getName();

        User user = userRepository.findByUsername(name).orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

        if (request.getFullName() != null) {
            user.setFullName(request.getFullName());
        }
        if (request.getPhoneNumber() != null) {
            user.setPhoneNumber(request.getPhoneNumber());
        }
        // Avatar column missing in Entity. Skipping avatar for now.

        User savedUser = userRepository.save(user);
        Integer tenantId = tenantRepository.findByUser_UserId(savedUser.getUserId())
                .map(t -> t.getTenantId()).orElse(null);

        return userMapper.toUserResponse(savedUser, tenantId);
    }

    private String generateToken(User user) {
        JWSHeader header = new JWSHeader(JWSAlgorithm.HS512);

        JWTClaimsSet jwtClaimsSet = new JWTClaimsSet.Builder()
                .subject(user.getUsername())
                .issuer("group10.com")
                .issueTime(new Date())
                .expirationTime(new Date(
                        Instant.now().plus(1, ChronoUnit.HOURS).toEpochMilli()))
                .claim("userId", user.getUserId())
                .claim("scope", buildScope(user))
                .build();

        Payload payload = new Payload(jwtClaimsSet.toJSONObject());

        JWSObject jwsObject = new JWSObject(header, payload);

        try {
            jwsObject.sign(new MACSigner(SIGNER_KEY.getBytes()));
            return jwsObject.serialize();
        } catch (JOSEException e) {
            throw new RuntimeException(e);
        }
    }

    private String buildScope(User user) {
        if (user.getRole() == null)
            return "";
        return user.getRole().getRoleName().toUpperCase();
    }
}
