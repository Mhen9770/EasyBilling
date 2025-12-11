package com.easybilling.controller;

import com.easybilling.dto.ApiResponse;
import com.easybilling.dto.GstCalculation;
import com.easybilling.entity.GstRate;
import com.easybilling.repository.GstRateRepository;
import com.easybilling.service.GstCalculationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

/**
 * REST controller for GST operations (India).
 */
@RestController
@RequestMapping("/api/v1/gst")
@RequiredArgsConstructor
@Tag(name = "GST Management", description = "APIs for GST calculation and validation (India)")
@SecurityRequirement(name = "bearer-jwt")
public class GstController {
    
    private final GstCalculationService gstCalculationService;
    private final GstRateRepository gstRateRepository;
    
    @PostMapping("/calculate")
    @Operation(summary = "Calculate GST", description = "Calculate GST for given amount and HSN/SAC code")
    public ApiResponse<GstCalculation> calculateGst(@Valid @RequestBody GstCalculationRequest request) {
        GstCalculation calculation = gstCalculationService.calculateGst(
                request.getHsnOrSacCode(),
                request.getAmount(),
                request.getSupplierState(),
                request.getCustomerState()
        );
        return ApiResponse.success(calculation);
    }
    
    @PostMapping("/calculate-by-category")
    @Operation(summary = "Calculate GST by category", description = "Calculate GST using tax category (GST_5, GST_12, etc.)")
    public ApiResponse<GstCalculation> calculateGstByCategory(@Valid @RequestBody GstCategoryRequest request) {
        GstCalculation calculation = gstCalculationService.calculateGstByCategory(
                request.getTaxCategory(),
                request.getAmount(),
                request.getIsInterstate()
        );
        return ApiResponse.success(calculation);
    }
    
    @GetMapping("/rates")
    @Operation(summary = "Get all GST rates", description = "Get list of all GST rates")
    public ApiResponse<List<GstRate>> getAllGstRates() {
        List<GstRate> rates = gstRateRepository.findAll();
        return ApiResponse.success(rates);
    }
    
    @GetMapping("/rates/active")
    @Operation(summary = "Get active GST rates", description = "Get list of active GST rates")
    public ApiResponse<List<GstRate>> getActiveGstRates() {
        List<GstRate> rates = gstRateRepository.findByIsActiveTrue();
        return ApiResponse.success(rates);
    }
    
    @PostMapping("/validate-gstin")
    @Operation(summary = "Validate GSTIN", description = "Validate GSTIN format")
    public ApiResponse<Boolean> validateGstin(@Valid @RequestBody GstinValidationRequest request) {
        boolean isValid = gstCalculationService.validateGstin(request.getGstin());
        return ApiResponse.success(isValid);
    }
    
    @GetMapping("/state-code/{gstin}")
    @Operation(summary = "Extract state code from GSTIN", description = "Get state code from GSTIN")
    public ApiResponse<String> getStateCodeFromGstin(@PathVariable String gstin) {
        String stateCode = gstCalculationService.extractStateCodeFromGstin(gstin);
        return ApiResponse.success(stateCode);
    }
    
    /**
     * Request DTO for GST calculation.
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class GstCalculationRequest {
        @NotBlank(message = "HSN or SAC code is required")
        private String hsnOrSacCode;
        
        @NotNull(message = "Amount is required")
        private BigDecimal amount;
        
        @NotBlank(message = "Supplier state is required")
        private String supplierState;
        
        @NotBlank(message = "Customer state is required")
        private String customerState;
    }
    
    /**
     * Request DTO for GST calculation by category.
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class GstCategoryRequest {
        @NotBlank(message = "Tax category is required")
        private String taxCategory;
        
        @NotNull(message = "Amount is required")
        private BigDecimal amount;
        
        @NotNull(message = "Interstate flag is required")
        private Boolean isInterstate;
    }
    
    /**
     * Request DTO for GSTIN validation.
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class GstinValidationRequest {
        @NotBlank(message = "GSTIN is required")
        private String gstin;
    }
}
