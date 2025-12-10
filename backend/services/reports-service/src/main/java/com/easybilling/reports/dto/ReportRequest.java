package com.easybilling.reports.dto;

import com.easybilling.reports.enums.ReportType;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;

@Data
public class ReportRequest {
    
    @NotNull(message = "Report type is required")
    private ReportType reportType;
    
    @NotNull(message = "Start date is required")
    private LocalDate startDate;
    
    @NotNull(message = "End date is required")
    private LocalDate endDate;
    
    private String storeId;
    
    private String userId;
    
    private String categoryId;
    
    private String format; // PDF, EXCEL, CSV
}
