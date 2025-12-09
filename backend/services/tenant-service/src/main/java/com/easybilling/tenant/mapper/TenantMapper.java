package com.easybilling.tenant.mapper;

import com.easybilling.tenant.dto.TenantRequest;
import com.easybilling.tenant.dto.TenantResponse;
import com.easybilling.tenant.entity.Tenant;
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
