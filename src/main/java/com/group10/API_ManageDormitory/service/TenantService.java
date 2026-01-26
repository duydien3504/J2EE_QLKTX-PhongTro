package com.group10.API_ManageDormitory.service;

import com.group10.API_ManageDormitory.dtos.request.TenantRequest;
import com.group10.API_ManageDormitory.dtos.response.TenantResponse;
import com.group10.API_ManageDormitory.entity.Tenant;
import com.group10.API_ManageDormitory.entity.User;
import com.group10.API_ManageDormitory.exception.AppException;
import com.group10.API_ManageDormitory.exception.ErrorCode;
import com.group10.API_ManageDormitory.repository.TenantRepository;
import com.group10.API_ManageDormitory.repository.UserRepository;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TenantService {
    private final TenantRepository tenantRepository;
    private final UserRepository userRepository;
    private final CloudinaryService cloudinaryService;

    // CRUD
    public List<TenantResponse> getTenants(String keyword) {
        return tenantRepository.searchTenants(keyword).stream()
                .map(this::toTenantResponse)
                .collect(Collectors.toList());
    }

    public TenantResponse getTenant(Integer id) {
        Tenant tenant = tenantRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Tenant not found"));
        return toTenantResponse(tenant);
    }

    public TenantResponse createTenant(TenantRequest request) {
        Tenant tenant = Tenant.builder()
                .fullName(request.getFullName())
                .phoneNumber(request.getPhoneNumber())
                .email(request.getEmail())
                .hometown(request.getHometown())
                .identityCardNumber(request.getIdentityCardNumber())
                .build();

        if (request.getUserId() != null) {
            User user = userRepository.findById(request.getUserId())
                    .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));
            tenant.setUser(user);
        }

        return toTenantResponse(tenantRepository.save(tenant));
    }

    public TenantResponse updateTenant(Integer id, TenantRequest request) {
        Tenant tenant = tenantRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Tenant not found"));

        if (request.getFullName() != null)
            tenant.setFullName(request.getFullName());
        if (request.getPhoneNumber() != null)
            tenant.setPhoneNumber(request.getPhoneNumber());
        if (request.getEmail() != null)
            tenant.setEmail(request.getEmail());
        if (request.getHometown() != null)
            tenant.setHometown(request.getHometown());
        if (request.getIdentityCardNumber() != null)
            tenant.setIdentityCardNumber(request.getIdentityCardNumber());

        return toTenantResponse(tenantRepository.save(tenant));
    }

    // Upload
    public TenantResponse uploadCCCD(Integer id, MultipartFile frontImage, MultipartFile backImage) throws IOException {
        Tenant tenant = tenantRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Tenant not found"));

        if (frontImage != null && !frontImage.isEmpty()) {
            String frontUrl = cloudinaryService.uploadImage(frontImage);
            tenant.setIdentityCardImageFront(frontUrl);
        }
        if (backImage != null && !backImage.isEmpty()) {
            String backUrl = cloudinaryService.uploadImage(backImage);
            tenant.setIdentityCardImageBack(backUrl);
        }

        return toTenantResponse(tenantRepository.save(tenant));
    }

    // Export Excel
    public void exportTenantsToExcel(HttpServletResponse response) throws IOException {
        List<Tenant> tenants = tenantRepository.findAll();

        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Tenants");

        Row headerRow = sheet.createRow(0);
        String[] columns = { "ID", "Full Name", "Phone", "Email", "CCCD", "Hometown" };
        for (int i = 0; i < columns.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(columns[i]);
            CellStyle style = workbook.createCellStyle();
            Font font = workbook.createFont();
            font.setBold(true);
            style.setFont(font);
            cell.setCellStyle(style);
        }

        int rowNum = 1;
        for (Tenant tenant : tenants) {
            Row row = sheet.createRow(rowNum++);
            row.createCell(0).setCellValue(tenant.getTenantId());
            row.createCell(1).setCellValue(tenant.getFullName());
            row.createCell(2).setCellValue(tenant.getPhoneNumber());
            row.createCell(3).setCellValue(tenant.getEmail());
            row.createCell(4).setCellValue(tenant.getIdentityCardNumber());
            row.createCell(5).setCellValue(tenant.getHometown());
        }

        for (int i = 0; i < columns.length; i++) {
            sheet.autoSizeColumn(i);
        }

        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setHeader("Content-Disposition", "attachment; filename=tenants.xlsx");

        workbook.write(response.getOutputStream());
        workbook.close();
    }

    private TenantResponse toTenantResponse(Tenant tenant) {
        return TenantResponse.builder()
                .tenantId(tenant.getTenantId())
                .fullName(tenant.getFullName())
                .phoneNumber(tenant.getPhoneNumber())
                .email(tenant.getEmail())
                .hometown(tenant.getHometown())
                .identityCardNumber(tenant.getIdentityCardNumber())
                .identityCardImageFront(tenant.getIdentityCardImageFront())
                .identityCardImageBack(tenant.getIdentityCardImageBack())
                .userId(tenant.getUser() != null ? tenant.getUser().getUserId() : null)
                .username(tenant.getUser() != null ? tenant.getUser().getUsername() : null)
                .build();
    }
}
