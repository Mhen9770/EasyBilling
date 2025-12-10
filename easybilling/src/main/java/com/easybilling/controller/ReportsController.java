package com.easybilling.controller;

import com.easybilling.dto.ReportRequest;
import com.easybilling.service.ReportsService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/reports")
@RequiredArgsConstructor
@Slf4j
public class ReportsController extends BaseController {
    
    private final ReportsService reportsService;
    
    @PostMapping("/generate")
    public ResponseEntity<Map<String, Object>> generateReport(@Valid @RequestBody ReportRequest request) {
        String tenantId = getCurrentTenantId();
        log.info("Generating report of type {} for tenant: {}", request.getReportType(), tenantId);
        
        Object reportData = reportsService.generateReport(request, tenantId);
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("data", reportData);
        response.put("reportType", request.getReportType());
        response.put("startDate", request.getStartDate());
        response.put("endDate", request.getEndDate());
        response.put("timestamp", System.currentTimeMillis());
        
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/types")
    public ResponseEntity<Map<String, Object>> getReportTypes() {
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("data", new String[]{
            "SALES", "INVENTORY", "CUSTOMER", "TAX", "PROFIT_LOSS", "PERFORMANCE"
        });
        response.put("timestamp", System.currentTimeMillis());
        
        return ResponseEntity.ok(response);
    }
}
