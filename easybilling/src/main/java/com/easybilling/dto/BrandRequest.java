package com.easybilling.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class BrandRequest {
    @NotBlank(message = "Brand name is required")
    private String name;

    private String description;
    private String logoUrl;
}
