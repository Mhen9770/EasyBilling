package com.runtime.engine.seeder;

import com.runtime.engine.entity.EntityDefinition;
import com.runtime.engine.entity.FieldDefinition;
import com.runtime.engine.permission.PermissionDefinition;
import com.runtime.engine.permission.SecurityGroup;
import com.runtime.engine.repo.*;
import com.runtime.engine.rule.RuleDefinition;
import com.runtime.engine.workflow.WorkflowDefinition;
import com.runtime.engine.workflow.WorkflowStep;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
@Order(1)
@RequiredArgsConstructor
@Slf4j
public class MetadataSeeder implements CommandLineRunner {
    
    private final EntityDefinitionRepository entityDefinitionRepository;
    private final FieldDefinitionRepository fieldDefinitionRepository;
    private final RuleDefinitionRepository ruleDefinitionRepository;
    private final WorkflowRepository workflowRepository;
    private final SecurityGroupRepository securityGroupRepository;
    
    private static final String DEFAULT_TENANT = "default";
    private static final String SYSTEM_USER = "system";
    
    @Override
    @Transactional
    public void run(String... args) {
        if (shouldSeed()) {
            log.info("Starting metadata seeding...");
            
            seedBasicEntities();
            seedBasicRules();
            seedBasicWorkflows();
            seedBasicPermissions();
            
            log.info("Metadata seeding completed successfully");
        } else {
            log.info("Metadata already exists, skipping seeding");
        }
    }
    
    private boolean shouldSeed() {
        return entityDefinitionRepository.count() == 0;
    }
    
    private void seedBasicEntities() {
        log.info("Seeding billing entities...");
        
        // Customer Entity
        seedCustomerEntity();
        
        // Product Entity
        seedProductEntity();
        
        // Invoice Entity  
        seedInvoiceEntity();
        
        // Payment Entity
        seedPaymentEntity();
        
        log.info("Billing entities seeded successfully");
    }
    
    private void seedCustomerEntity() {
        EntityDefinition customer = EntityDefinition.builder()
            .name("Customer")
            .tableName("customers")
            .description("Customer entity for billing system")
            .tenantId(DEFAULT_TENANT)
            .active(true)
            .auditable(true)
            .versionable(false)
            .metadata(createMetadata("icon", "user", "color", "#4A90E2"))
            .displayConfig(createDisplayConfig("customerName", List.of("customerName", "email", "phoneNumber", "balance")))
            .build();
        
        customer = entityDefinitionRepository.save(customer);
        
        // Customer Fields
        List<FieldDefinition> customerFields = new ArrayList<>();
        
        customerFields.add(createField(customer, "customerName", "Customer Name", "string", true, 1, 
            Map.of("minLength", 2, "maxLength", 100)));
        
        customerFields.add(createField(customer, "email", "Email Address", "string", true, 2,
            Map.of("pattern", "^[A-Za-z0-9+_.-]+@(.+)$", "maxLength", 255)));
        
        customerFields.add(createField(customer, "phoneNumber", "Phone Number", "string", true, 3,
            Map.of("maxLength", 20)));
        
        customerFields.add(createField(customer, "address", "Address", "text", false, 4,
            Map.of("maxLength", 500)));
        
        customerFields.add(createField(customer, "city", "City", "string", false, 5,
            Map.of("maxLength", 100)));
        
        customerFields.add(createField(customer, "postalCode", "Postal Code", "string", false, 6,
            Map.of("maxLength", 20)));
        
        customerFields.add(createField(customer, "taxId", "Tax ID", "string", false, 7,
            Map.of("maxLength", 50)));
        
        customerFields.add(createField(customer, "balance", "Account Balance", "decimal", false, 8,
            Map.of("min", -999999999, "max", 999999999)));
        
        customerFields.add(createField(customer, "creditLimit", "Credit Limit", "decimal", false, 9,
            Map.of("min", 0, "max", 999999999)));
        
        customerFields.add(createField(customer, "status", "Status", "string", false, 10,
            Map.of("enum", List.of("active", "inactive", "suspended", "blocked"))));
        
        fieldDefinitionRepository.saveAll(customerFields);
        log.info("Customer entity created with {} fields", customerFields.size());
    }
    
    private void seedProductEntity() {
        EntityDefinition product = EntityDefinition.builder()
            .name("Product")
            .tableName("products")
            .description("Product/Service entity for billing")
            .tenantId(DEFAULT_TENANT)
            .active(true)
            .auditable(true)
            .versionable(true)
            .metadata(createMetadata("icon", "package", "color", "#28A745"))
            .displayConfig(createDisplayConfig("productName", List.of("productName", "sku", "price", "stockQuantity")))
            .build();
        
        product = entityDefinitionRepository.save(product);
        
        List<FieldDefinition> productFields = new ArrayList<>();
        
        productFields.add(createField(product, "productName", "Product Name", "string", true, 1,
            Map.of("minLength", 2, "maxLength", 200)));
        
        productFields.add(createField(product, "description", "Description", "text", false, 2,
            Map.of("maxLength", 2000)));
        
        productFields.add(createField(product, "sku", "SKU", "string", true, 3,
            Map.of("maxLength", 100)));
        
        productFields.add(createField(product, "price", "Unit Price", "decimal", true, 4,
            Map.of("min", 0, "max", 999999999)));
        
        productFields.add(createField(product, "cost", "Unit Cost", "decimal", false, 5,
            Map.of("min", 0, "max", 999999999)));
        
        productFields.add(createField(product, "taxRate", "Tax Rate (%)", "decimal", false, 6,
            Map.of("min", 0, "max", 100)));
        
        productFields.add(createField(product, "category", "Category", "string", false, 7,
            Map.of("maxLength", 100)));
        
        productFields.add(createField(product, "stockQuantity", "Stock Quantity", "integer", false, 8,
            Map.of("min", 0)));
        
        productFields.add(createField(product, "unit", "Unit of Measure", "string", false, 9,
            Map.of("enum", List.of("piece", "kg", "liter", "meter", "hour", "service"))));
        
        productFields.add(createField(product, "isActive", "Is Active", "boolean", false, 10,
            Map.of()));
        
        fieldDefinitionRepository.saveAll(productFields);
        log.info("Product entity created with {} fields", productFields.size());
    }
    
    private void seedInvoiceEntity() {
        EntityDefinition invoice = EntityDefinition.builder()
            .name("Invoice")
            .tableName("invoices")
            .description("Invoice entity for billing system")
            .tenantId(DEFAULT_TENANT)
            .active(true)
            .auditable(true)
            .versionable(true)
            .metadata(createMetadata("icon", "file-invoice", "color", "#FFC107"))
            .displayConfig(createDisplayConfig("invoiceNumber", List.of("invoiceNumber", "customerName", "totalAmount", "status")))
            .build();
        
        invoice = entityDefinitionRepository.save(invoice);
        
        List<FieldDefinition> invoiceFields = new ArrayList<>();
        
        invoiceFields.add(createField(invoice, "invoiceNumber", "Invoice Number", "string", true, 1,
            Map.of("minLength", 3, "maxLength", 50)));
        
        invoiceFields.add(createField(invoice, "customerId", "Customer ID", "string", true, 2,
            Map.of("maxLength", 50)));
        
        invoiceFields.add(createField(invoice, "customerName", "Customer Name", "string", true, 3,
            Map.of("maxLength", 100)));
        
        invoiceFields.add(createField(invoice, "invoiceDate", "Invoice Date", "date", true, 4,
            Map.of()));
        
        invoiceFields.add(createField(invoice, "dueDate", "Due Date", "date", true, 5,
            Map.of()));
        
        invoiceFields.add(createField(invoice, "subtotal", "Subtotal", "decimal", true, 6,
            Map.of("min", 0)));
        
        invoiceFields.add(createField(invoice, "taxAmount", "Tax Amount", "decimal", false, 7,
            Map.of("min", 0)));
        
        invoiceFields.add(createField(invoice, "discountAmount", "Discount Amount", "decimal", false, 8,
            Map.of("min", 0)));
        
        invoiceFields.add(createField(invoice, "totalAmount", "Total Amount", "decimal", true, 9,
            Map.of("min", 0)));
        
        invoiceFields.add(createField(invoice, "paidAmount", "Paid Amount", "decimal", false, 10,
            Map.of("min", 0)));
        
        invoiceFields.add(createField(invoice, "status", "Status", "string", true, 11,
            Map.of("enum", List.of("draft", "sent", "paid", "partial", "overdue", "cancelled"))));
        
        invoiceFields.add(createField(invoice, "notes", "Notes", "text", false, 12,
            Map.of("maxLength", 2000)));
        
        fieldDefinitionRepository.saveAll(invoiceFields);
        log.info("Invoice entity created with {} fields", invoiceFields.size());
    }
    
    private void seedPaymentEntity() {
        EntityDefinition payment = EntityDefinition.builder()
            .name("Payment")
            .tableName("payments")
            .description("Payment entity for billing system")
            .tenantId(DEFAULT_TENANT)
            .active(true)
            .auditable(true)
            .versionable(false)
            .metadata(createMetadata("icon", "credit-card", "color", "#DC3545"))
            .displayConfig(createDisplayConfig("paymentReference", List.of("paymentReference", "amount", "paymentMethod", "paymentDate")))
            .build();
        
        payment = entityDefinitionRepository.save(payment);
        
        List<FieldDefinition> paymentFields = new ArrayList<>();
        
        paymentFields.add(createField(payment, "paymentReference", "Payment Reference", "string", true, 1,
            Map.of("minLength", 3, "maxLength", 50)));
        
        paymentFields.add(createField(payment, "invoiceId", "Invoice ID", "string", true, 2,
            Map.of("maxLength", 50)));
        
        paymentFields.add(createField(payment, "customerId", "Customer ID", "string", true, 3,
            Map.of("maxLength", 50)));
        
        paymentFields.add(createField(payment, "amount", "Amount", "decimal", true, 4,
            Map.of("min", 0.01)));
        
        paymentFields.add(createField(payment, "paymentDate", "Payment Date", "date", true, 5,
            Map.of()));
        
        paymentFields.add(createField(payment, "paymentMethod", "Payment Method", "string", true, 6,
            Map.of("enum", List.of("cash", "credit_card", "debit_card", "bank_transfer", "check", "mobile_payment"))));
        
        paymentFields.add(createField(payment, "transactionId", "Transaction ID", "string", false, 7,
            Map.of("maxLength", 100)));
        
        paymentFields.add(createField(payment, "status", "Status", "string", true, 8,
            Map.of("enum", List.of("pending", "completed", "failed", "refunded"))));
        
        paymentFields.add(createField(payment, "notes", "Notes", "text", false, 9,
            Map.of("maxLength", 1000)));
        
        fieldDefinitionRepository.saveAll(paymentFields);
        log.info("Payment entity created with {} fields", paymentFields.size());
    }
    
    private void seedBasicRules() {
        log.info("Seeding billing rules...");
        
        // Customer email validation rule
        RuleDefinition customerEmailRule = RuleDefinition.builder()
            .name("CustomerEmailValidation")
            .description("Validate customer email format")
            .entityType("Customer")
            .tenantId(DEFAULT_TENANT)
            .trigger("pre_create")
            .condition("#email != null && #email.length() > 0")
            .actions(createRuleActions("validate_email"))
            .priority(10)
            .active(true)
            .metadata(createMetadata("category", "validation"))
            .createdBy(SYSTEM_USER)
            .build();
        
        // Invoice total calculation rule
        RuleDefinition invoiceTotalRule = RuleDefinition.builder()
            .name("InvoiceTotalCalculation")
            .description("Calculate invoice total from subtotal, tax, and discount")
            .entityType("Invoice")
            .tenantId(DEFAULT_TENANT)
            .trigger("pre_create")
            .condition("#subtotal != null")
            .actions(createRuleActions("calculate_total"))
            .priority(15)
            .active(true)
            .metadata(createMetadata("category", "calculation"))
            .createdBy(SYSTEM_USER)
            .build();
        
        // Payment amount validation rule
        RuleDefinition paymentValidationRule = RuleDefinition.builder()
            .name("PaymentAmountValidation")
            .description("Validate payment amount is positive")
            .entityType("Payment")
            .tenantId(DEFAULT_TENANT)
            .trigger("pre_create")
            .condition("#amount != null")
            .actions(createRuleActions("validate_amount"))
            .priority(10)
            .active(true)
            .metadata(createMetadata("category", "validation"))
            .createdBy(SYSTEM_USER)
            .build();
        
        // Product price validation rule
        RuleDefinition productPriceRule = RuleDefinition.builder()
            .name("ProductPriceValidation")
            .description("Ensure product price is greater than cost")
            .entityType("Product")
            .tenantId(DEFAULT_TENANT)
            .trigger("pre_create")
            .condition("#price != null && #cost != null")
            .actions(createRuleActions("validate_pricing"))
            .priority(10)
            .active(true)
            .metadata(createMetadata("category", "validation"))
            .createdBy(SYSTEM_USER)
            .build();
        
        // Credit limit check rule
        RuleDefinition creditLimitRule = RuleDefinition.builder()
            .name("CustomerCreditLimitCheck")
            .description("Check if customer has exceeded credit limit")
            .entityType("Customer")
            .tenantId(DEFAULT_TENANT)
            .trigger("post_update")
            .condition("#balance != null && #creditLimit != null")
            .actions(createRuleActions("check_credit_limit"))
            .priority(5)
            .active(true)
            .metadata(createMetadata("category", "business_logic"))
            .createdBy(SYSTEM_USER)
            .build();
        
        ruleDefinitionRepository.saveAll(List.of(
            customerEmailRule, 
            invoiceTotalRule, 
            paymentValidationRule, 
            productPriceRule,
            creditLimitRule
        ));
        log.info("Billing rules seeded successfully");
    }
    
    private void seedBasicWorkflows() {
        log.info("Seeding billing workflows...");
        
        // Invoice creation workflow
        WorkflowDefinition invoiceWorkflow = WorkflowDefinition.builder()
            .name("InvoiceCreationWorkflow")
            .description("Workflow executed when a new invoice is created")
            .entityType("Invoice")
            .tenantId(DEFAULT_TENANT)
            .trigger("post_create")
            .active(true)
            .config(createMetadata("async", false))
            .metadata(createMetadata("category", "billing"))
            .createdBy(SYSTEM_USER)
            .build();
        
        invoiceWorkflow = workflowRepository.save(invoiceWorkflow);
        
        WorkflowStep step1 = createWorkflowStep(invoiceWorkflow, "ValidateInvoiceData", "validation", 1,
            "#totalAmount > 0", true, false);
        WorkflowStep step2 = createWorkflowStep(invoiceWorkflow, "GenerateInvoiceNumber", "enrichment", 2,
            "#invoiceNumber == null", false, false);
        WorkflowStep step3 = createWorkflowStep(invoiceWorkflow, "SendInvoiceNotification", "notification", 3,
            "#status == 'sent'", false, false);
        WorkflowStep step4 = createWorkflowStep(invoiceWorkflow, "UpdateCustomerBalance", "enrichment", 4,
            "#customerId != null", false, false);
        
        invoiceWorkflow.setSteps(List.of(step1, step2, step3, step4));
        workflowRepository.save(invoiceWorkflow);
        
        // Payment processing workflow
        WorkflowDefinition paymentWorkflow = WorkflowDefinition.builder()
            .name("PaymentProcessingWorkflow")
            .description("Workflow executed when a payment is processed")
            .entityType("Payment")
            .tenantId(DEFAULT_TENANT)
            .trigger("post_create")
            .active(true)
            .config(createMetadata("async", false))
            .metadata(createMetadata("category", "billing"))
            .createdBy(SYSTEM_USER)
            .build();
        
        paymentWorkflow = workflowRepository.save(paymentWorkflow);
        
        WorkflowStep payStep1 = createWorkflowStep(paymentWorkflow, "ValidatePayment", "validation", 1,
            "#amount > 0", true, false);
        WorkflowStep payStep2 = createWorkflowStep(paymentWorkflow, "UpdateInvoiceStatus", "enrichment", 2,
            "#invoiceId != null", true, false);
        WorkflowStep payStep3 = createWorkflowStep(paymentWorkflow, "UpdateCustomerBalance", "enrichment", 3,
            "#customerId != null", false, false);
        WorkflowStep payStep4 = createWorkflowStep(paymentWorkflow, "SendPaymentReceipt", "notification", 4,
            "#status == 'completed'", false, false);
        
        paymentWorkflow.setSteps(List.of(payStep1, payStep2, payStep3, payStep4));
        workflowRepository.save(paymentWorkflow);
        
        // Customer creation workflow
        WorkflowDefinition customerWorkflow = WorkflowDefinition.builder()
            .name("CustomerOnboardingWorkflow")
            .description("Workflow executed when a new customer is created")
            .entityType("Customer")
            .tenantId(DEFAULT_TENANT)
            .trigger("post_create")
            .active(true)
            .config(createMetadata("async", false))
            .metadata(createMetadata("category", "onboarding"))
            .createdBy(SYSTEM_USER)
            .build();
        
        customerWorkflow = workflowRepository.save(customerWorkflow);
        
        WorkflowStep custStep1 = createWorkflowStep(customerWorkflow, "ValidateCustomerData", "validation", 1,
            "#email != null", true, false);
        WorkflowStep custStep2 = createWorkflowStep(customerWorkflow, "InitializeBalance", "enrichment", 2,
            "#balance == null", false, false);
        WorkflowStep custStep3 = createWorkflowStep(customerWorkflow, "SendWelcomeEmail", "notification", 3,
            "#status == 'active'", false, false);
        
        customerWorkflow.setSteps(List.of(custStep1, custStep2, custStep3));
        workflowRepository.save(customerWorkflow);
        
        log.info("Billing workflows seeded successfully");
    }
    
    private void seedBasicPermissions() {
        log.info("Seeding billing permissions...");
        
        // Admin security group
        SecurityGroup adminGroup = SecurityGroup.builder()
            .name("BillingAdministrators")
            .description("Full billing system access for administrators")
            .tenantId(DEFAULT_TENANT)
            .active(true)
            .metadata(createMetadata("level", "admin"))
            .createdBy(SYSTEM_USER)
            .build();
        
        List<String> adminPermissions = List.of(
            "Customer:create", "Customer:update", "Customer:find", "Customer:delete",
            "Product:create", "Product:update", "Product:find", "Product:delete",
            "Invoice:create", "Invoice:update", "Invoice:find", "Invoice:delete",
            "Payment:create", "Payment:update", "Payment:find", "Payment:delete"
        );
        adminGroup.setPermissions(adminPermissions);
        
        // Accountant security group
        SecurityGroup accountantGroup = SecurityGroup.builder()
            .name("Accountants")
            .description("Full access to invoices and payments")
            .tenantId(DEFAULT_TENANT)
            .active(true)
            .metadata(createMetadata("level", "accountant"))
            .createdBy(SYSTEM_USER)
            .build();
        
        List<String> accountantPermissions = List.of(
            "Customer:find", "Customer:update",
            "Product:find",
            "Invoice:create", "Invoice:update", "Invoice:find",
            "Payment:create", "Payment:update", "Payment:find"
        );
        accountantGroup.setPermissions(accountantPermissions);
        
        // Sales security group
        SecurityGroup salesGroup = SecurityGroup.builder()
            .name("SalesTeam")
            .description("Access to customers, products, and invoices")
            .tenantId(DEFAULT_TENANT)
            .active(true)
            .metadata(createMetadata("level", "sales"))
            .createdBy(SYSTEM_USER)
            .build();
        
        List<String> salesPermissions = List.of(
            "Customer:create", "Customer:update", "Customer:find",
            "Product:find",
            "Invoice:create", "Invoice:find",
            "Payment:find"
        );
        salesGroup.setPermissions(salesPermissions);
        
        // Viewer security group
        SecurityGroup viewerGroup = SecurityGroup.builder()
            .name("BillingViewers")
            .description("Read-only access to billing data")
            .tenantId(DEFAULT_TENANT)
            .active(true)
            .metadata(createMetadata("level", "viewer"))
            .createdBy(SYSTEM_USER)
            .build();
        
        List<String> viewerPermissions = List.of(
            "Customer:find",
            "Product:find",
            "Invoice:find",
            "Payment:find"
        );
        viewerGroup.setPermissions(viewerPermissions);
        
        securityGroupRepository.saveAll(List.of(adminGroup, accountantGroup, salesGroup, viewerGroup));
        log.info("Billing permissions seeded successfully");
    }
    
    private FieldDefinition createField(EntityDefinition entity, String name, String label, 
                                       String dataType, boolean required, int order, 
                                       Map<String, Object> validationRules) {
        return FieldDefinition.builder()
            .entityDefinition(entity)
            .name(name)
            .label(label)
            .dataType(dataType)
            .required(required)
            .orderIndex(order)
            .displayInList(order <= 4)
            .displayInForm(true)
            .searchable(order <= 3)
            .validationRules(validationRules)
            .build();
    }
    
    private WorkflowStep createWorkflowStep(WorkflowDefinition workflow, String name, String stepType,
                                           int order, String condition, boolean mandatory, boolean async) {
        WorkflowStep step = WorkflowStep.builder()
            .workflowDefinition(workflow)
            .name(name)
            .stepType(stepType)
            .stepOrder(order)
            .condition(condition)
            .mandatory(mandatory)
            .async(async)
            .build();
        
        Map<String, Object> actions = new HashMap<>();
        actions.put("log", Map.of("message", "Executing step: " + name, "level", "info"));
        step.setActions(actions);
        
        return step;
    }
    
    private Map<String, Object> createMetadata(String key, Object value) {
        Map<String, Object> metadata = new HashMap<>();
        metadata.put(key, value);
        return metadata;
    }
    
    private Map<String, Object> createMetadata(String key1, Object value1, String key2, Object value2) {
        Map<String, Object> metadata = new HashMap<>();
        metadata.put(key1, value1);
        metadata.put(key2, value2);
        return metadata;
    }
    
    private Map<String, Object> createDisplayConfig(String titleField, List<String> listFields) {
        Map<String, Object> config = new HashMap<>();
        config.put("titleField", titleField);
        config.put("listFields", listFields);
        return config;
    }
    
    private Map<String, Object> createRuleActions(String actionType) {
        Map<String, Object> actions = new HashMap<>();
        actions.put("log", Map.of("message", "Rule action: " + actionType, "level", "info"));
        return actions;
    }
}
