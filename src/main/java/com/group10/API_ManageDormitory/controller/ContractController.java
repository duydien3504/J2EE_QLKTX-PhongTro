package com.group10.API_ManageDormitory.controller;

import com.group10.API_ManageDormitory.dtos.request.ContractRequest;
import com.group10.API_ManageDormitory.dtos.request.MemberRequest;
import com.group10.API_ManageDormitory.dtos.request.RoomRegistrationRequest;
import com.group10.API_ManageDormitory.dtos.response.ApiResponse;
import com.group10.API_ManageDormitory.dtos.response.ContractRegistrationResponse;
import com.group10.API_ManageDormitory.dtos.response.ContractResponse;
import com.group10.API_ManageDormitory.service.ContractService;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/v1/contracts")
@RequiredArgsConstructor
public class ContractController {
    private final ContractService contractService;

    @GetMapping
    @PreAuthorize("hasAuthority('SCOPE_ADMIN') or hasAuthority('SCOPE_OWNER') or hasAuthority('SCOPE_STAFF')")
    public ApiResponse<List<ContractResponse>> getContracts(@RequestParam(required = false) String status) {
        return ApiResponse.<List<ContractResponse>>builder()
                .result(contractService.getContracts(status))
                .build();
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('SCOPE_ADMIN') or hasAuthority('SCOPE_OWNER') or hasAuthority('SCOPE_STAFF') or hasAuthority('SCOPE_TENANT')")
    public ApiResponse<ContractResponse> getContract(@PathVariable Integer id) {
        return ApiResponse.<ContractResponse>builder()
                .result(contractService.getContract(id))
                .build();
    }

    @PostMapping
    @PreAuthorize("hasAuthority('SCOPE_OWNER')")
    public ApiResponse<ContractResponse> createContract(@RequestBody @Valid ContractRequest request) {
        return ApiResponse.<ContractResponse>builder()
                .result(contractService.createContract(request))
                .build();
    }

    @PostMapping("/register")
    @PreAuthorize("hasAuthority('SCOPE_USER') or hasAuthority('SCOPE_TENANT')")
    public ApiResponse<ContractRegistrationResponse> registerContract(@RequestBody @Valid RoomRegistrationRequest request) {
        return ApiResponse.<ContractRegistrationResponse>builder()
                .result(contractService.registerContract(request))
                .build();
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('SCOPE_OWNER')")
    public ApiResponse<ContractResponse> updateContract(@PathVariable Integer id,
            @RequestBody ContractRequest request) {
        return ApiResponse.<ContractResponse>builder()
                .result(contractService.updateContract(id, request))
                .build();
    }

    @GetMapping("/download/{id}")
    @PreAuthorize("hasAuthority('SCOPE_OWNER') or hasAuthority('SCOPE_STAFF') or hasAuthority('SCOPE_TENANT')")
    public void downloadContract(@PathVariable Integer id, HttpServletResponse response) throws IOException {
        contractService.downloadContract(id, response);
    }

    @PostMapping("/{id}/add-member")
    @PreAuthorize("hasAuthority('SCOPE_OWNER') or hasAuthority('SCOPE_STAFF')")
    public ApiResponse<ContractResponse> addMember(@PathVariable Integer id,
            @RequestBody @Valid MemberRequest request) {
        return ApiResponse.<ContractResponse>builder()
                .result(contractService.addMember(id, request))
                .build();
    }

    @DeleteMapping("/{id}/remove-member")
    @PreAuthorize("hasAuthority('SCOPE_OWNER') or hasAuthority('SCOPE_STAFF')")
    public ApiResponse<String> removeMember(@PathVariable Integer id, @RequestParam Integer tenantId) {
        contractService.removeMember(id, tenantId);
        return ApiResponse.<String>builder()
                .result("Member removed successfully")
                .build();
    }

    @PostMapping("/{id}/terminate")
    @PreAuthorize("hasAuthority('SCOPE_OWNER')")
    public ApiResponse<com.group10.API_ManageDormitory.dtos.response.LiquidationResponse> terminateContract(
            @PathVariable Integer id,
            @RequestBody @Valid com.group10.API_ManageDormitory.dtos.request.TerminateRequest request) {
        return ApiResponse.<com.group10.API_ManageDormitory.dtos.response.LiquidationResponse>builder()
                .result(contractService.terminateContract(id, request))
                .build();
    }

    @GetMapping("/{id}/liquidation")
    @PreAuthorize("hasAuthority('SCOPE_OWNER') or hasAuthority('SCOPE_TENANT')")
    public ApiResponse<com.group10.API_ManageDormitory.dtos.response.LiquidationResponse> getLiquidation(
            @PathVariable Integer id) {
        return ApiResponse.<com.group10.API_ManageDormitory.dtos.response.LiquidationResponse>builder()
                .result(contractService.getLiquidation(id))
                .build();
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('SCOPE_ADMIN') or hasAuthority('SCOPE_OWNER')")
    public void deleteContract(@PathVariable Integer id) {
        contractService.deleteContract(id);
    }
}
