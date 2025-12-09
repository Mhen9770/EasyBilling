package com.easybilling.tenant.service;

import com.easybilling.common.dto.PageResponse;
import com.easybilling.common.exception.BusinessException;
import com.easybilling.common.exception.ResourceNotFoundException;
import com.easybilling.tenant.dto.TenantRequest;
import com.easybilling.tenant.dto.TenantResponse;
import com.easybilling.tenant.entity.Tenant;
import com.easybilling.tenant.entity.Tenant.TenantStatus;
import com.easybilling.tenant.mapper.TenantMapper;
import com.easybilling.tenant.repository.TenantRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

/**
 * Service for tenant management operations.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TenantService {
    
    private final TenantRepository tenantRepository;
    private final TenantMapper tenantMapper;
    private final TenantProvisioningService provisioningService;
    
    /**
     * Create a new tenant.
     */
    @Transactional
    public TenantResponse createTenant(TenantRequest request) {
        log.info("Creating tenant with slug: {}", request.getSlug());
        
        // Check if slug already exists
        if (tenantRepository.existsBySlug(request.getSlug())) {
            throw new BusinessException("TENANT_EXISTS", "Tenant with slug already exists: " + request.getSlug());
        }
        
        // Map request to entity
        Tenant tenant = tenantMapper.toEntity(request);
        
        // Set initial status and trial period
        tenant.setStatus(TenantStatus.PENDING);
        tenant.setSubscriptionStartDate(Instant.now());
        tenant.setTrialEndDate(Instant.now().plus(14, ChronoUnit.DAYS)); // 14-day trial
        
        // Set default limits based on plan
        setDefaultLimits(tenant);
        
        // Save tenant
        tenant = tenantRepository.save(tenant);
        
        // Provision tenant schema/database
        provisioningService.provisionTenant(tenant);
        
        // Update status to TRIAL
        tenant.setStatus(TenantStatus.TRIAL);
        tenant = tenantRepository.save(tenant);
        
        log.info("Tenant created successfully: {}", tenant.getId());
        return tenantMapper.toResponse(tenant);
    }
    
    /**
     * Get tenant by ID.
     */
    @Transactional(readOnly = true)
    public TenantResponse getTenantById(String id) {
        Tenant tenant = findTenantById(id);
        return tenantMapper.toResponse(tenant);
    }
    
    /**
     * Get tenant by slug.
     */
    @Transactional(readOnly = true)
    public TenantResponse getTenantBySlug(String slug) {
        Tenant tenant = tenantRepository.findBySlug(slug)
                .orElseThrow(() -> new ResourceNotFoundException("Tenant", slug));
        return tenantMapper.toResponse(tenant);
    }
    
    /**
     * Update tenant.
     */
    @Transactional
    public TenantResponse updateTenant(String id, TenantRequest request) {
        log.info("Updating tenant: {}", id);
        
        Tenant tenant = findTenantById(id);
        tenantMapper.updateEntity(tenant, request);
        
        tenant = tenantRepository.save(tenant);
        
        log.info("Tenant updated successfully: {}", id);
        return tenantMapper.toResponse(tenant);
    }
    
    /**
     * Activate tenant subscription.
     */
    @Transactional
    public TenantResponse activateTenant(String id) {
        log.info("Activating tenant: {}", id);
        
        Tenant tenant = findTenantById(id);
        tenant.setStatus(TenantStatus.ACTIVE);
        tenant.setSubscriptionStartDate(Instant.now());
        tenant.setSubscriptionEndDate(Instant.now().plus(365, ChronoUnit.DAYS)); // 1 year
        
        tenant = tenantRepository.save(tenant);
        
        log.info("Tenant activated successfully: {}", id);
        return tenantMapper.toResponse(tenant);
    }
    
    /**
     * Suspend tenant.
     */
    @Transactional
    public TenantResponse suspendTenant(String id) {
        log.info("Suspending tenant: {}", id);
        
        Tenant tenant = findTenantById(id);
        tenant.setStatus(TenantStatus.SUSPENDED);
        
        tenant = tenantRepository.save(tenant);
        
        log.info("Tenant suspended successfully: {}", id);
        return tenantMapper.toResponse(tenant);
    }
    
    /**
     * Cancel tenant subscription.
     */
    @Transactional
    public TenantResponse cancelTenant(String id) {
        log.info("Cancelling tenant: {}", id);
        
        Tenant tenant = findTenantById(id);
        tenant.setStatus(TenantStatus.CANCELLED);
        
        tenant = tenantRepository.save(tenant);
        
        log.info("Tenant cancelled successfully: {}", id);
        return tenantMapper.toResponse(tenant);
    }
    
    /**
     * Get all tenants with pagination.
     */
    @Transactional(readOnly = true)
    public PageResponse<TenantResponse> getAllTenants(Pageable pageable) {
        Page<Tenant> page = tenantRepository.findAll(pageable);
        return PageResponse.of(
                page.getContent().stream().map(tenantMapper::toResponse).toList(),
                page.getNumber(),
                page.getSize(),
                page.getTotalElements()
        );
    }
    
    /**
     * Search tenants by name.
     */
    @Transactional(readOnly = true)
    public PageResponse<TenantResponse> searchTenants(String name, Pageable pageable) {
        Page<Tenant> page = tenantRepository.findByNameContainingIgnoreCase(name, pageable);
        return PageResponse.of(
                page.getContent().stream().map(tenantMapper::toResponse).toList(),
                page.getNumber(),
                page.getSize(),
                page.getTotalElements()
        );
    }
    
    private Tenant findTenantById(String id) {
        return tenantRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Tenant", id));
    }
    
    private void setDefaultLimits(Tenant tenant) {
        switch (tenant.getPlan()) {
            case BASIC -> {
                tenant.setMaxUsers(tenant.getMaxUsers() != null ? tenant.getMaxUsers() : 5);
                tenant.setMaxStores(tenant.getMaxStores() != null ? tenant.getMaxStores() : 1);
            }
            case PRO -> {
                tenant.setMaxUsers(tenant.getMaxUsers() != null ? tenant.getMaxUsers() : 20);
                tenant.setMaxStores(tenant.getMaxStores() != null ? tenant.getMaxStores() : 5);
            }
            case ENTERPRISE -> {
                tenant.setMaxUsers(tenant.getMaxUsers() != null ? tenant.getMaxUsers() : 100);
                tenant.setMaxStores(tenant.getMaxStores() != null ? tenant.getMaxStores() : 50);
            }
        }
    }
}
