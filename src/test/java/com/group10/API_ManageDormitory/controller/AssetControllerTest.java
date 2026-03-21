package com.group10.API_ManageDormitory.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.group10.API_ManageDormitory.dtos.request.AssetRequest;
import com.group10.API_ManageDormitory.dtos.response.AssetResponse;
import com.group10.API_ManageDormitory.exception.AppException;
import com.group10.API_ManageDormitory.exception.ErrorCode;
import com.group10.API_ManageDormitory.exception.GlobalExceptionHandler;
import com.group10.API_ManageDormitory.service.AssetService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class AssetControllerTest {

    private MockMvc mockMvc;

    @Mock
    private AssetService assetService;

    @InjectMocks
    private AssetController assetController;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setup() {
        mockMvc = MockMvcBuilders.standaloneSetup(assetController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    // ─────────────────────────────────────────────────────────
    // GET /api/v1/assets
    // ─────────────────────────────────────────────────────────

    @Test
    void getAllAssets_success() throws Exception {
        AssetResponse response = AssetResponse.builder().assetId(1).assetName("AC").build();
        when(assetService.getAllAssets()).thenReturn(List.of(response));

        mockMvc.perform(get("/api/v1/assets"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result[0].assetName").value("AC"));
    }

    // ─────────────────────────────────────────────────────────
    // POST /api/v1/assets
    // ─────────────────────────────────────────────────────────

    @Test
    void createAsset_success() throws Exception {
        AssetRequest request = AssetRequest.builder()
                .assetName("Fan")
                .assetCode("F001")
                .purchasePrice(BigDecimal.valueOf(150))
                .build();
        AssetResponse response = AssetResponse.builder().assetId(1).assetName("Fan").assetCode("F001").build();
        when(assetService.createAsset(any())).thenReturn(response);

        mockMvc.perform(post("/api/v1/assets")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result.assetName").value("Fan"));
    }

    @Test
    void createAsset_missingAssetName_badRequest() throws Exception {
        // assetName is @NotBlank → validation rejects it
        AssetRequest request = AssetRequest.builder().assetName("").build();

        mockMvc.perform(post("/api/v1/assets")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    // ─────────────────────────────────────────────────────────
    // DELETE /api/v1/assets/{id}
    // ─────────────────────────────────────────────────────────

    @Test
    void deleteAsset_success() throws Exception {
        doNothing().when(assetService).deleteAsset(1);

        mockMvc.perform(delete("/api/v1/assets/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result").value("Asset deleted successfully"));
    }

    @Test
    void deleteAsset_assetNotFound_returns404() throws Exception {
        doThrow(new AppException(ErrorCode.ASSET_NOT_FOUND))
                .when(assetService).deleteAsset(99);

        mockMvc.perform(delete("/api/v1/assets/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value(1020));
    }

    @Test
    void deleteAsset_assetInUse_returns400() throws Exception {
        doThrow(new AppException(ErrorCode.ASSET_IN_USE))
                .when(assetService).deleteAsset(1);

        mockMvc.perform(delete("/api/v1/assets/1"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(1021));
    }
}
