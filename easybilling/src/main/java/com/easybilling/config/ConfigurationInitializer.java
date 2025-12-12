package com.easybilling.config;

import com.easybilling.entity.SystemConfiguration;
import com.easybilling.repository.SystemConfigurationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

/**
 * Initializes default system configurations on application startup.
 * Makes the billing system fully customizable by providing configurable business rules.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class ConfigurationInitializer implements CommandLineRunner {
    
    private final SystemConfigurationRepository systemConfigRepository;
    
    @Override
    public void run(String... args) {
        log.info("Initializing default system configurations...");
        
        initializeBillingConfigurations();
        initializeInventoryConfigurations();
        initializeCustomerConfigurations();
        initializePaymentConfigurations();
        initializeTaxConfigurations();
        initializeNotificationConfigurations();
        initializeSecurityConfigurations();
        initializeUIConfigurations();
        initializeDocumentConfigurations();
        initializeIntegrationConfigurations();
        
        log.info("System configurations initialized successfully");
    }
    
    private void initializeBillingConfigurations() {
        createConfigIfNotExists("billing.invoice_prefix", "INV", "BILLING", "STRING",
                "Default invoice number prefix", true, false, "INV", null);
        
        createConfigIfNotExists("billing.invoice_suffix", "", "BILLING", "STRING",
                "Default invoice number suffix", true, false, "", null);
        
        createConfigIfNotExists("billing.payment_terms_days", "30", "BILLING", "INTEGER",
                "Default payment terms in days", true, false, "30", "^[0-9]{1,3}$");
        
        createConfigIfNotExists("billing.allow_negative_inventory", "false", "BILLING", "BOOLEAN",
                "Allow creating invoices with negative inventory", true, false, "false", null);
        
        createConfigIfNotExists("billing.require_customer", "false", "BILLING", "BOOLEAN",
                "Require customer for all invoices", true, false, "false", null);
        
        createConfigIfNotExists("billing.auto_approve_invoices", "true", "BILLING", "BOOLEAN",
                "Automatically approve new invoices", true, false, "true", null);
        
        createConfigIfNotExists("billing.default_currency", "USD", "BILLING", "STRING",
                "Default currency code", true, false, "USD", "^[A-Z]{3}$");
        
        createConfigIfNotExists("billing.invoice_due_reminder_days", "3", "BILLING", "INTEGER",
                "Days before due date to send reminder", true, false, "3", "^[0-9]{1,2}$");
    }
    
    private void initializeInventoryConfigurations() {
        createConfigIfNotExists("inventory.low_stock_threshold", "10", "INVENTORY", "INTEGER",
                "Default low stock alert threshold", true, false, "10", "^[0-9]+$");
        
        createConfigIfNotExists("inventory.auto_reorder_enabled", "false", "INVENTORY", "BOOLEAN",
                "Enable automatic reorder when stock is low", true, false, "false", null);
        
        createConfigIfNotExists("inventory.reorder_quantity", "50", "INVENTORY", "INTEGER",
                "Default reorder quantity", true, false, "50", "^[0-9]+$");
        
        createConfigIfNotExists("inventory.stock_valuation_method", "FIFO", "INVENTORY", "STRING",
                "Stock valuation method (FIFO, LIFO, WEIGHTED_AVERAGE)", true, false, "FIFO", "^(FIFO|LIFO|WEIGHTED_AVERAGE)$");
        
        createConfigIfNotExists("inventory.enable_batch_tracking", "false", "INVENTORY", "BOOLEAN",
                "Enable batch/lot tracking for products", true, false, "false", null);
        
        createConfigIfNotExists("inventory.enable_expiry_tracking", "false", "INVENTORY", "BOOLEAN",
                "Enable expiry date tracking for products", true, false, "false", null);
        
        createConfigIfNotExists("inventory.expiry_alert_days", "30", "INVENTORY", "INTEGER",
                "Days before expiry to send alert", true, false, "30", "^[0-9]+$");
    }
    
    private void initializeCustomerConfigurations() {
        createConfigIfNotExists("customer.loyalty_enabled", "false", "CUSTOMER", "BOOLEAN",
                "Enable customer loyalty program", true, false, "false", null);
        
        createConfigIfNotExists("customer.loyalty_points_per_currency", "1", "CUSTOMER", "DECIMAL",
                "Loyalty points earned per currency unit spent", true, false, "1", "^[0-9]+(\\.[0-9]{1,2})?$");
        
        createConfigIfNotExists("customer.loyalty_redemption_rate", "0.01", "CUSTOMER", "DECIMAL",
                "Currency value of 1 loyalty point", true, false, "0.01", "^[0-9]+(\\.[0-9]{1,4})?$");
        
        createConfigIfNotExists("customer.wallet_enabled", "false", "CUSTOMER", "BOOLEAN",
                "Enable customer wallet/store credit", true, false, "false", null);
        
        createConfigIfNotExists("customer.credit_limit_enabled", "false", "CUSTOMER", "BOOLEAN",
                "Enable credit limits for customers", true, false, "false", null);
        
        createConfigIfNotExists("customer.default_credit_limit", "1000", "CUSTOMER", "DECIMAL",
                "Default credit limit for new customers", true, false, "1000", "^[0-9]+(\\.[0-9]{1,2})?$");
        
        createConfigIfNotExists("customer.segment_vip_threshold", "10000", "CUSTOMER", "DECIMAL",
                "Total purchase amount to become VIP customer", true, false, "10000", "^[0-9]+(\\.[0-9]{1,2})?$");
    }
    
    private void initializePaymentConfigurations() {
        createConfigIfNotExists("payment.cash_rounding_enabled", "false", "PAYMENT", "BOOLEAN",
                "Enable cash rounding for payments", true, false, "false", null);
        
        createConfigIfNotExists("payment.rounding_precision", "0.05", "PAYMENT", "DECIMAL",
                "Rounding precision for cash payments", true, false, "0.05", "^[0-9]+(\\.[0-9]{1,2})?$");
        
        createConfigIfNotExists("payment.partial_payments_allowed", "true", "PAYMENT", "BOOLEAN",
                "Allow partial payments on invoices", true, false, "true", null);
        
        createConfigIfNotExists("payment.overpayment_to_wallet", "false", "PAYMENT", "BOOLEAN",
                "Add overpayments to customer wallet", true, false, "false", null);
        
        createConfigIfNotExists("payment.default_payment_method", "CASH", "PAYMENT", "STRING",
                "Default payment method", true, false, "CASH", null);
    }
    
    private void initializeTaxConfigurations() {
        createConfigIfNotExists("tax.enabled", "true", "TAX", "BOOLEAN",
                "Enable tax calculations", true, false, "true", null);
        
        createConfigIfNotExists("tax.regime", "STANDARD", "TAX", "STRING",
                "Tax regime (STANDARD, GST, VAT, SALES_TAX, NONE)", true, false, "STANDARD", "^(STANDARD|GST|VAT|SALES_TAX|NONE)$");
        
        createConfigIfNotExists("tax.default_rate", "0", "TAX", "DECIMAL",
                "Default tax rate percentage", true, false, "0", "^[0-9]+(\\.[0-9]{1,2})?$");
        
        createConfigIfNotExists("tax.inclusive", "false", "TAX", "BOOLEAN",
                "Tax inclusive pricing (prices include tax)", true, false, "false", null);
        
        createConfigIfNotExists("tax.compound", "false", "TAX", "BOOLEAN",
                "Enable compound tax (tax on tax)", true, false, "false", null);
        
        createConfigIfNotExists("tax.registration_number", "", "TAX", "STRING",
                "Business tax registration number", true, false, "", null);
    }
    
    private void initializeNotificationConfigurations() {
        createConfigIfNotExists("notification.email_enabled", "false", "NOTIFICATION", "BOOLEAN",
                "Enable email notifications", true, false, "false", null);
        
        createConfigIfNotExists("notification.sms_enabled", "false", "NOTIFICATION", "BOOLEAN",
                "Enable SMS notifications", true, false, "false", null);
        
        createConfigIfNotExists("notification.whatsapp_enabled", "false", "NOTIFICATION", "BOOLEAN",
                "Enable WhatsApp notifications", true, false, "false", null);
        
        createConfigIfNotExists("notification.invoice_created", "true", "NOTIFICATION", "BOOLEAN",
                "Notify when invoice is created", true, false, "true", null);
        
        createConfigIfNotExists("notification.payment_received", "true", "NOTIFICATION", "BOOLEAN",
                "Notify when payment is received", true, false, "true", null);
        
        createConfigIfNotExists("notification.low_stock_alert", "true", "NOTIFICATION", "BOOLEAN",
                "Notify on low stock", true, false, "true", null);
    }
    
    private void initializeSecurityConfigurations() {
        createConfigIfNotExists("security.password_min_length", "8", "SECURITY", "INTEGER",
                "Minimum password length", true, false, "8", "^[0-9]{1,2}$");
        
        createConfigIfNotExists("security.password_require_special_char", "true", "SECURITY", "BOOLEAN",
                "Require special characters in password", true, false, "true", null);
        
        createConfigIfNotExists("security.password_require_number", "true", "SECURITY", "BOOLEAN",
                "Require numbers in password", true, false, "true", null);
        
        createConfigIfNotExists("security.password_require_uppercase", "true", "SECURITY", "BOOLEAN",
                "Require uppercase letters in password", true, false, "true", null);
        
        createConfigIfNotExists("security.session_timeout_minutes", "60", "SECURITY", "INTEGER",
                "Session timeout in minutes", true, false, "60", "^[0-9]+$");
        
        createConfigIfNotExists("security.max_login_attempts", "5", "SECURITY", "INTEGER",
                "Maximum failed login attempts before lockout", true, false, "5", "^[0-9]{1,2}$");
        
        createConfigIfNotExists("security.lockout_duration_minutes", "30", "SECURITY", "INTEGER",
                "Account lockout duration in minutes", true, false, "30", "^[0-9]+$");
    }
    
    private void initializeUIConfigurations() {
        createConfigIfNotExists("ui.date_format", "yyyy-MM-dd", "UI", "STRING",
                "Date display format", true, false, "yyyy-MM-dd", null);
        
        createConfigIfNotExists("ui.time_format", "HH:mm:ss", "UI", "STRING",
                "Time display format", true, false, "HH:mm:ss", null);
        
        createConfigIfNotExists("ui.datetime_format", "yyyy-MM-dd HH:mm", "UI", "STRING",
                "DateTime display format", true, false, "yyyy-MM-dd HH:mm", null);
        
        createConfigIfNotExists("ui.decimal_separator", ".", "UI", "STRING",
                "Decimal separator", true, false, ".", "^[.,]$");
        
        createConfigIfNotExists("ui.thousand_separator", ",", "UI", "STRING",
                "Thousand separator", true, false, ",", "^[,. ]$");
        
        createConfigIfNotExists("ui.currency_position", "PREFIX", "UI", "STRING",
                "Currency symbol position (PREFIX or SUFFIX)", true, false, "PREFIX", "^(PREFIX|SUFFIX)$");
        
        createConfigIfNotExists("ui.items_per_page", "25", "UI", "INTEGER",
                "Default items per page in lists", true, false, "25", "^(10|25|50|100)$");
        
        createConfigIfNotExists("ui.language", "en", "UI", "STRING",
                "Default language code", true, false, "en", "^[a-z]{2}$");
        
        createConfigIfNotExists("ui.timezone", "UTC", "UI", "STRING",
                "Default timezone", true, false, "UTC", null);
    }
    
    private void initializeDocumentConfigurations() {
        createConfigIfNotExists("document.invoice_footer", "Thank you for your business!", "DOCUMENT", "TEXT",
                "Default invoice footer text", true, false, "Thank you for your business!", null);
        
        createConfigIfNotExists("document.terms_and_conditions", "", "DOCUMENT", "TEXT",
                "Default terms and conditions", true, false, "", null);
        
        createConfigIfNotExists("document.show_logo", "true", "DOCUMENT", "BOOLEAN",
                "Show company logo on documents", true, false, "true", null);
        
        createConfigIfNotExists("document.show_watermark", "false", "DOCUMENT", "BOOLEAN",
                "Show watermark on documents", true, false, "false", null);
        
        createConfigIfNotExists("document.watermark_text", "DRAFT", "DOCUMENT", "STRING",
                "Watermark text for draft documents", true, false, "DRAFT", null);
        
        createConfigIfNotExists("document.page_size", "A4", "DOCUMENT", "STRING",
                "Default document page size", true, false, "A4", "^(A4|LETTER|THERMAL)$");
    }
    
    private void initializeIntegrationConfigurations() {
        createConfigIfNotExists("integration.webhook_enabled", "false", "INTEGRATION", "BOOLEAN",
                "Enable webhook integrations", true, false, "false", null);
        
        createConfigIfNotExists("integration.api_rate_limit", "100", "INTEGRATION", "INTEGER",
                "API rate limit per minute", true, false, "100", "^[0-9]+$");
        
        createConfigIfNotExists("integration.enable_audit_log", "true", "INTEGRATION", "BOOLEAN",
                "Enable audit logging for integrations", true, false, "true", null);
    }
    
    private void createConfigIfNotExists(String key, String value, String category, String dataType,
                                        String description, boolean isEditable, boolean isSensitive,
                                        String defaultValue, String validationRule) {
        if (!systemConfigRepository.existsByConfigKey(key)) {
            SystemConfiguration config = SystemConfiguration.builder()
                    .configKey(key)
                    .configValue(value)
                    .category(category)
                    .dataType(dataType)
                    .description(description)
                    .isEditable(isEditable)
                    .isSensitive(isSensitive)
                    .defaultValue(defaultValue)
                    .validationRule(validationRule)
                    .updatedBy("SYSTEM")
                    .build();
            
            systemConfigRepository.save(config);
            log.debug("Created system configuration: {}", key);
        }
    }
}
