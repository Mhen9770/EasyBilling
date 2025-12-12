package com.easybilling.service;

import com.easybilling.dto.DocumentTemplateDTO;
import com.easybilling.entity.DocumentTemplate;
import com.easybilling.exception.ResourceNotFoundException;
import com.easybilling.repository.DocumentTemplateRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Service for managing document templates.
 * Allows tenants to create and manage custom templates for invoices, receipts, quotations, etc.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class DocumentTemplateService {
    
    private final DocumentTemplateRepository documentTemplateRepository;
    
    @Transactional(readOnly = true)
    public List<DocumentTemplateDTO> getAllTemplates(Integer tenantId) {
        return documentTemplateRepository.findByTenantId(tenantId).stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }
    
    @Transactional(readOnly = true)
    public List<DocumentTemplateDTO> getTemplatesByType(Integer tenantId, String templateType) {
        return documentTemplateRepository.findByTenantIdAndTemplateType(tenantId, templateType).stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }
    
    @Transactional(readOnly = true)
    @Cacheable(value = "template", key = "#tenantId + '_' + #templateType + '_default'")
    public DocumentTemplateDTO getDefaultTemplate(Integer tenantId, String templateType) {
        return documentTemplateRepository.findByTenantIdAndTemplateTypeAndIsDefaultTrue(tenantId, templateType)
                .map(this::toDTO)
                .orElseGet(() -> getSystemDefaultTemplate(templateType));
    }
    
    @Transactional(readOnly = true)
    public DocumentTemplateDTO getTemplateById(Long id, Integer tenantId) {
        return documentTemplateRepository.findByIdAndTenantId(id, tenantId)
                .map(this::toDTO)
                .orElseThrow(() -> new ResourceNotFoundException("Template not found: " + id));
    }
    
    @Transactional
    @CacheEvict(value = "template", key = "#dto.tenantId + '_' + #dto.templateType + '_default'")
    public DocumentTemplateDTO createTemplate(DocumentTemplateDTO dto) {
        // If setting as default, unset other defaults for this type
        if (Boolean.TRUE.equals(dto.getIsDefault())) {
            documentTemplateRepository.findByTenantIdAndTemplateType(dto.getTenantId(), dto.getTemplateType())
                    .forEach(template -> {
                        template.setIsDefault(false);
                        documentTemplateRepository.save(template);
                    });
        }
        
        DocumentTemplate template = DocumentTemplate.builder()
                .tenantId(dto.getTenantId())
                .templateName(dto.getTemplateName())
                .templateType(dto.getTemplateType())
                .templateFormat(dto.getTemplateFormat())
                .templateContent(dto.getTemplateContent())
                .templateCss(dto.getTemplateCss())
                .headerContent(dto.getHeaderContent())
                .footerContent(dto.getFooterContent())
                .pageSize(dto.getPageSize())
                .pageOrientation(dto.getPageOrientation())
                .marginTop(dto.getMarginTop())
                .marginBottom(dto.getMarginBottom())
                .marginLeft(dto.getMarginLeft())
                .marginRight(dto.getMarginRight())
                .showLogo(dto.getShowLogo() != null ? dto.getShowLogo() : true)
                .showCompanyDetails(dto.getShowCompanyDetails() != null ? dto.getShowCompanyDetails() : true)
                .showTaxDetails(dto.getShowTaxDetails() != null ? dto.getShowTaxDetails() : true)
                .showTerms(dto.getShowTerms() != null ? dto.getShowTerms() : true)
                .isDefault(dto.getIsDefault() != null ? dto.getIsDefault() : false)
                .isActive(dto.getIsActive() != null ? dto.getIsActive() : true)
                .description(dto.getDescription())
                .createdBy(dto.getCreatedBy())
                .build();
        
        template = documentTemplateRepository.save(template);
        log.info("Created document template: {} for tenant: {}", template.getTemplateName(), template.getTenantId());
        return toDTO(template);
    }
    
    @Transactional
    @CacheEvict(value = "template", key = "#dto.tenantId + '_' + #dto.templateType + '_default'")
    public DocumentTemplateDTO updateTemplate(Long id, DocumentTemplateDTO dto) {
        DocumentTemplate template = documentTemplateRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Template not found: " + id));
        
        // If setting as default, unset other defaults for this type
        if (Boolean.TRUE.equals(dto.getIsDefault()) && !template.getIsDefault()) {
            documentTemplateRepository.findByTenantIdAndTemplateType(template.getTenantId(), template.getTemplateType())
                    .forEach(t -> {
                        if (!t.getId().equals(id)) {
                            t.setIsDefault(false);
                            documentTemplateRepository.save(t);
                        }
                    });
        }
        
        template.setTemplateName(dto.getTemplateName());
        template.setTemplateContent(dto.getTemplateContent());
        template.setTemplateCss(dto.getTemplateCss());
        template.setHeaderContent(dto.getHeaderContent());
        template.setFooterContent(dto.getFooterContent());
        template.setPageSize(dto.getPageSize());
        template.setPageOrientation(dto.getPageOrientation());
        template.setMarginTop(dto.getMarginTop());
        template.setMarginBottom(dto.getMarginBottom());
        template.setMarginLeft(dto.getMarginLeft());
        template.setMarginRight(dto.getMarginRight());
        template.setShowLogo(dto.getShowLogo());
        template.setShowCompanyDetails(dto.getShowCompanyDetails());
        template.setShowTaxDetails(dto.getShowTaxDetails());
        template.setShowTerms(dto.getShowTerms());
        template.setIsDefault(dto.getIsDefault());
        template.setIsActive(dto.getIsActive());
        template.setDescription(dto.getDescription());
        
        template = documentTemplateRepository.save(template);
        log.info("Updated document template: {}", id);
        return toDTO(template);
    }
    
    @Transactional
    @CacheEvict(value = "template", allEntries = true)
    public void deleteTemplate(Long id, Integer tenantId) {
        DocumentTemplate template = documentTemplateRepository.findByIdAndTenantId(id, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Template not found: " + id));
        
        if (Boolean.TRUE.equals(template.getIsDefault())) {
            throw new IllegalStateException("Cannot delete default template. Set another template as default first.");
        }
        
        documentTemplateRepository.delete(template);
        log.info("Deleted document template: {}", id);
    }
    
    @Transactional
    @CacheEvict(value = "template", key = "#tenantId + '_' + #templateType + '_default'")
    public void setDefaultTemplate(Long id, Integer tenantId) {
        DocumentTemplate template = documentTemplateRepository.findByIdAndTenantId(id, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Template not found: " + id));
        
        // Unset all defaults for this type
        documentTemplateRepository.findByTenantIdAndTemplateType(tenantId, template.getTemplateType())
                .forEach(t -> {
                    t.setIsDefault(false);
                    documentTemplateRepository.save(t);
                });
        
        // Set this one as default
        template.setIsDefault(true);
        documentTemplateRepository.save(template);
        log.info("Set template {} as default for type: {}", id, template.getTemplateType());
    }
    
    /**
     * Get system default template when tenant hasn't created one
     */
    private DocumentTemplateDTO getSystemDefaultTemplate(String templateType) {
        return DocumentTemplateDTO.builder()
                .templateName("Default " + templateType)
                .templateType(templateType)
                .templateFormat("HTML")
                .templateContent(getDefaultTemplateContent(templateType))
                .pageSize("A4")
                .pageOrientation("PORTRAIT")
                .marginTop(20)
                .marginBottom(20)
                .marginLeft(20)
                .marginRight(20)
                .showLogo(true)
                .showCompanyDetails(true)
                .showTaxDetails(true)
                .showTerms(true)
                .isDefault(true)
                .isActive(true)
                .build();
    }
    
    /**
     * Get default HTML content for different template types
     */
    private String getDefaultTemplateContent(String templateType) {
        return switch (templateType.toUpperCase()) {
            case "INVOICE" -> getDefaultInvoiceTemplate();
            case "RECEIPT" -> getDefaultReceiptTemplate();
            case "QUOTATION" -> getDefaultQuotationTemplate();
            case "DELIVERY_NOTE" -> getDefaultDeliveryNoteTemplate();
            case "CREDIT_NOTE" -> getDefaultCreditNoteTemplate();
            default -> "<html><body><h1>" + templateType + "</h1><p>{{content}}</p></body></html>";
        };
    }
    
    private String getDefaultInvoiceTemplate() {
        return """
                <html>
                <head>
                    <style>
                        body { font-family: Arial, sans-serif; margin: 20px; }
                        .header { text-align: center; margin-bottom: 30px; }
                        .company-details { margin-bottom: 20px; }
                        .invoice-details { margin-bottom: 20px; }
                        .items-table { width: 100%; border-collapse: collapse; margin-bottom: 20px; }
                        .items-table th, .items-table td { border: 1px solid #ddd; padding: 8px; text-align: left; }
                        .items-table th { background-color: #f2f2f2; }
                        .totals { text-align: right; }
                        .footer { margin-top: 30px; text-align: center; font-size: 12px; }
                    </style>
                </head>
                <body>
                    <div class="header">
                        {{#if showLogo}}<img src="{{logoUrl}}" alt="Company Logo" style="max-height: 80px;" />{{/if}}
                        <h1>INVOICE</h1>
                    </div>
                    {{#if showCompanyDetails}}
                    <div class="company-details">
                        <strong>{{companyName}}</strong><br/>
                        {{companyAddress}}<br/>
                        {{companyPhone}}<br/>
                        {{companyEmail}}
                    </div>
                    {{/if}}
                    <div class="invoice-details">
                        <p><strong>Invoice Number:</strong> {{invoiceNumber}}</p>
                        <p><strong>Date:</strong> {{invoiceDate}}</p>
                        <p><strong>Customer:</strong> {{customerName}}</p>
                    </div>
                    <table class="items-table">
                        <thead>
                            <tr>
                                <th>Item</th>
                                <th>Quantity</th>
                                <th>Unit Price</th>
                                <th>Total</th>
                            </tr>
                        </thead>
                        <tbody>
                            {{#each items}}
                            <tr>
                                <td>{{productName}}</td>
                                <td>{{quantity}}</td>
                                <td>{{unitPrice}}</td>
                                <td>{{total}}</td>
                            </tr>
                            {{/each}}
                        </tbody>
                    </table>
                    <div class="totals">
                        <p><strong>Subtotal:</strong> {{subtotal}}</p>
                        {{#if showTaxDetails}}<p><strong>Tax:</strong> {{taxAmount}}</p>{{/if}}
                        <p><strong>Total:</strong> {{totalAmount}}</p>
                    </div>
                    {{#if showTerms}}
                    <div class="footer">
                        <p>{{terms}}</p>
                    </div>
                    {{/if}}
                </body>
                </html>
                """;
    }
    
    private String getDefaultReceiptTemplate() {
        return "<html><body><h2>RECEIPT</h2><p>Receipt Number: {{receiptNumber}}</p><p>Amount: {{amount}}</p></body></html>";
    }
    
    private String getDefaultQuotationTemplate() {
        return "<html><body><h2>QUOTATION</h2><p>Quote Number: {{quoteNumber}}</p><p>Valid Until: {{validUntil}}</p></body></html>";
    }
    
    private String getDefaultDeliveryNoteTemplate() {
        return "<html><body><h2>DELIVERY NOTE</h2><p>Delivery Note Number: {{deliveryNoteNumber}}</p></body></html>";
    }
    
    private String getDefaultCreditNoteTemplate() {
        return "<html><body><h2>CREDIT NOTE</h2><p>Credit Note Number: {{creditNoteNumber}}</p></body></html>";
    }
    
    private DocumentTemplateDTO toDTO(DocumentTemplate entity) {
        return DocumentTemplateDTO.builder()
                .id(entity.getId())
                .tenantId(entity.getTenantId())
                .templateName(entity.getTemplateName())
                .templateType(entity.getTemplateType())
                .templateFormat(entity.getTemplateFormat())
                .templateContent(entity.getTemplateContent())
                .templateCss(entity.getTemplateCss())
                .headerContent(entity.getHeaderContent())
                .footerContent(entity.getFooterContent())
                .pageSize(entity.getPageSize())
                .pageOrientation(entity.getPageOrientation())
                .marginTop(entity.getMarginTop())
                .marginBottom(entity.getMarginBottom())
                .marginLeft(entity.getMarginLeft())
                .marginRight(entity.getMarginRight())
                .showLogo(entity.getShowLogo())
                .showCompanyDetails(entity.getShowCompanyDetails())
                .showTaxDetails(entity.getShowTaxDetails())
                .showTerms(entity.getShowTerms())
                .isDefault(entity.getIsDefault())
                .isActive(entity.getIsActive())
                .description(entity.getDescription())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .createdBy(entity.getCreatedBy())
                .build();
    }
}
