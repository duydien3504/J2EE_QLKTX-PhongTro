package com.group10.API_ManageDormitory.service;

import com.group10.API_ManageDormitory.dtos.request.TenantRequest;
import com.group10.API_ManageDormitory.dtos.response.TenantResponse;
import com.group10.API_ManageDormitory.entity.Tenant;
import com.group10.API_ManageDormitory.repository.TenantRepository;
import com.group10.API_ManageDormitory.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TenantServiceTest {
    @Mock
    private TenantRepository tenantRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private CloudinaryService cloudinaryService;

    @InjectMocks
    private TenantService tenantService;

    @Test
    void getTenants_search() {
        Tenant tenant = Tenant.builder().fullName("John Doe").build();
        when(tenantRepository.searchTenants("John")).thenReturn(List.of(tenant));

        List<TenantResponse> result = tenantService.getTenants("John");
        assertFalse(result.isEmpty());
        assertEquals("John Doe", result.get(0).getFullName());
    }

    @Test
    void createTenant_success() {
        TenantRequest request = TenantRequest.builder().fullName("John").identityCardNumber("123").build();
        Tenant tenant = Tenant.builder().fullName("John").identityCardNumber("123").build();

        when(tenantRepository.save(any(Tenant.class))).thenReturn(tenant);

        TenantResponse response = tenantService.createTenant(request);
        assertEquals("John", response.getFullName());
    }

    @Test
    void uploadCCCD_success() throws IOException {
        Tenant tenant = Tenant.builder().tenantId(1).build();
        MultipartFile file = mock(MultipartFile.class);

        when(tenantRepository.findById(1)).thenReturn(Optional.of(tenant));
        when(file.isEmpty()).thenReturn(false);
        when(cloudinaryService.uploadImage(file)).thenReturn("http://url");
        when(tenantRepository.save(any(Tenant.class))).thenReturn(tenant);

        TenantResponse response = tenantService.uploadCCCD(1, file, null);

        assertEquals("http://url", tenant.getIdentityCardImageFront());
    }
}
