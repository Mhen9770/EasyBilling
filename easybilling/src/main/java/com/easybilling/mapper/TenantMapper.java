package com.easybilling.mapper;

import com.easybilling.dto.TenantRequest;
import com.easybilling.dto.TenantResponse;
import com.easybilling.entity.Tenant;
import org.mapstruct.*;

/**
 * MapStruct mapper for Tenant entity and DTOs.
 */
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface TenantMapper {
    
    TenantResponse toResponse(Tenant tenant);
    
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "databaseName", ignore = true)
    @Mapping(target = "schemaName", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    Tenant toEntity(TenantRequest request);
    
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "slug", ignore = true)
    @Mapping(target = "databaseName", ignore = true)
    @Mapping(target = "schemaName", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    void updateEntity(@MappingTarget Tenant tenant, TenantRequest request);
}
