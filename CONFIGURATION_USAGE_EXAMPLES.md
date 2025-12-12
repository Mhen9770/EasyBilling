# Configuration Usage Examples

## How to Use Configurations in Your Services

This guide shows how to replace hardcoded values with configurable settings.

## Basic Configuration Usage

### 1. Inject ConfigurationService

```java
@Service
@RequiredArgsConstructor
public class YourService {
    private final ConfigurationService configurationService;
    
    // ... your methods
}
```

### 2. Retrieve Configuration Values

```java
// Get string value with tenant override
String invoicePrefix = configurationService.getConfigValue("billing.invoice_prefix");

// Get integer value with default
Integer paymentTerms = configurationService.getConfigValueAsInt("billing.payment_terms_days", 30);

// Get boolean value with default
Boolean allowNegativeInventory = configurationService.getConfigValueAsBoolean(
    "billing.allow_negative_inventory", 
    false
);

// Get decimal value with default
Double taxRate = configurationService.getConfigValueAsDouble("tax.default_rate", 0.0);
```

## Real-World Examples

### Example 1: Invoice Number Generation

**Before (Hardcoded):**
```java
public String generateInvoiceNumber() {
    String prefix = "INV"; // Hardcoded
    String financialYear = getCurrentFinancialYear();
    int sequence = getNextSequence();
    return String.format("%s/%s/%04d", prefix, financialYear, sequence);
}
```

**After (Configurable):**
```java
public String generateInvoiceNumber(Integer tenantId) {
    // Get tenant-specific or system default prefix
    String prefix = configurationService.getConfigValue(
        "billing.invoice_prefix", 
        tenantId
    );
    
    // Fallback to default if not configured
    if (prefix == null || prefix.isEmpty()) {
        prefix = "INV";
    }
    
    String suffix = configurationService.getConfigValue(
        "billing.invoice_suffix", 
        tenantId
    );
    
    String financialYear = getCurrentFinancialYear();
    int sequence = getNextSequence();
    
    String invoiceNumber = String.format("%s/%s/%04d", prefix, financialYear, sequence);
    
    if (suffix != null && !suffix.isEmpty()) {
        invoiceNumber += "-" + suffix;
    }
    
    return invoiceNumber;
}
```

### Example 2: Payment Terms

**Before (Hardcoded):**
```java
public Invoice createInvoice(InvoiceRequest request) {
    Invoice invoice = new Invoice();
    invoice.setPaymentTermsDays(30); // Hardcoded
    invoice.setDueDate(LocalDate.now().plusDays(30));
    // ...
}
```

**After (Configurable):**
```java
public Invoice createInvoice(InvoiceRequest request) {
    Invoice invoice = new Invoice();
    
    // Get configured payment terms
    Integer paymentTerms = configurationService.getConfigValueAsInt(
        "billing.payment_terms_days", 
        30 // Default if not configured
    );
    
    invoice.setPaymentTermsDays(paymentTerms);
    invoice.setDueDate(LocalDate.now().plusDays(paymentTerms));
    
    // Check if customer is required
    Boolean requireCustomer = configurationService.getConfigValueAsBoolean(
        "billing.require_customer", 
        false
    );
    
    if (requireCustomer && invoice.getCustomerId() == null) {
        throw new ValidationException("Customer is required for invoices");
    }
    
    // ...
}
```

### Example 3: Tax Calculation

**Before (Hardcoded):**
```java
public BigDecimal calculateTax(BigDecimal amount) {
    BigDecimal taxRate = new BigDecimal("18.0"); // Hardcoded GST rate
    return amount.multiply(taxRate).divide(new BigDecimal("100"));
}
```

**After (Configurable):**
```java
public BigDecimal calculateTax(BigDecimal amount, Integer tenantId) {
    // Check if tax is enabled
    Boolean taxEnabled = configurationService.getConfigValueAsBoolean(
        "tax.enabled", 
        true
    );
    
    if (!taxEnabled) {
        return BigDecimal.ZERO;
    }
    
    // Get tax regime
    String taxRegime = configurationService.getConfigValue("tax.regime");
    
    // Get tax rate
    Double taxRateValue = configurationService.getConfigValueAsDouble(
        "tax.default_rate", 
        0.0
    );
    BigDecimal taxRate = BigDecimal.valueOf(taxRateValue);
    
    // Check if tax is inclusive
    Boolean taxInclusive = configurationService.getConfigValueAsBoolean(
        "tax.inclusive", 
        false
    );
    
    BigDecimal taxAmount;
    if (taxInclusive) {
        // Tax is included in amount: tax = amount * (rate / (100 + rate))
        taxAmount = amount.multiply(taxRate)
                         .divide(BigDecimal.valueOf(100).add(taxRate), 2, RoundingMode.HALF_UP);
    } else {
        // Tax is additional: tax = amount * (rate / 100)
        taxAmount = amount.multiply(taxRate)
                         .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
    }
    
    return taxAmount;
}
```

### Example 4: Low Stock Alert

**Before (Hardcoded):**
```java
public boolean isLowStock(Product product) {
    int threshold = 10; // Hardcoded
    return product.getQuantity() < threshold;
}
```

**After (Configurable):**
```java
public boolean isLowStock(Product product, Integer tenantId) {
    // Get tenant-specific low stock threshold
    Integer threshold = configurationService.getConfigValueAsInt(
        "inventory.low_stock_threshold", 
        10
    );
    
    return product.getQuantity() < threshold;
}

public void checkAndSendLowStockAlert(Product product, Integer tenantId) {
    if (isLowStock(product, tenantId)) {
        // Check if notifications are enabled
        Boolean alertEnabled = configurationService.getConfigValueAsBoolean(
            "notification.low_stock_alert", 
            true
        );
        
        if (alertEnabled) {
            // Check if auto-reorder is enabled
            Boolean autoReorder = configurationService.getConfigValueAsBoolean(
                "inventory.auto_reorder_enabled", 
                false
            );
            
            if (autoReorder) {
                Integer reorderQty = configurationService.getConfigValueAsInt(
                    "inventory.reorder_quantity", 
                    50
                );
                createPurchaseOrder(product, reorderQty);
            } else {
                sendLowStockNotification(product);
            }
        }
    }
}
```

### Example 5: Date Formatting

**Before (Hardcoded):**
```java
public String formatDate(LocalDate date) {
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    return date.format(formatter);
}
```

**After (Configurable):**
```java
public String formatDate(LocalDate date, Integer tenantId) {
    String pattern = configurationService.getConfigValue(
        "ui.date_format", 
        tenantId
    );
    
    if (pattern == null || pattern.isEmpty()) {
        pattern = "yyyy-MM-dd"; // Default
    }
    
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern(pattern);
    return date.format(formatter);
}

public String formatDateTime(LocalDateTime dateTime, Integer tenantId) {
    String pattern = configurationService.getConfigValue(
        "ui.datetime_format", 
        tenantId
    );
    
    if (pattern == null || pattern.isEmpty()) {
        pattern = "yyyy-MM-dd HH:mm"; // Default
    }
    
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern(pattern);
    return dateTime.format(formatter);
}
```

### Example 6: Currency Formatting

**Before (Hardcoded):**
```java
public String formatCurrency(BigDecimal amount) {
    return String.format("$%.2f", amount);
}
```

**After (Configurable):**
```java
public String formatCurrency(BigDecimal amount, Integer tenantId) {
    // Get currency code
    String currencyCode = configurationService.getConfigValue(
        "billing.default_currency", 
        tenantId
    );
    if (currencyCode == null) {
        currencyCode = "USD";
    }
    
    // Get currency symbol
    String currencySymbol = getCurrencySymbol(currencyCode);
    
    // Get currency position
    String position = configurationService.getConfigValue(
        "ui.currency_position", 
        tenantId
    );
    if (position == null) {
        position = "PREFIX";
    }
    
    // Get decimal separator
    String decimalSep = configurationService.getConfigValue(
        "ui.decimal_separator", 
        tenantId
    );
    if (decimalSep == null) {
        decimalSep = ".";
    }
    
    // Get thousand separator
    String thousandSep = configurationService.getConfigValue(
        "ui.thousand_separator", 
        tenantId
    );
    if (thousandSep == null) {
        thousandSep = ",";
    }
    
    // Format number
    String formattedNumber = formatNumber(amount, decimalSep, thousandSep);
    
    // Add currency symbol
    if ("PREFIX".equals(position)) {
        return currencySymbol + formattedNumber;
    } else {
        return formattedNumber + currencySymbol;
    }
}

private String getCurrencySymbol(String currencyCode) {
    return switch (currencyCode) {
        case "USD" -> "$";
        case "EUR" -> "€";
        case "GBP" -> "£";
        case "INR" -> "₹";
        case "JPY" -> "¥";
        default -> currencyCode + " ";
    };
}
```

### Example 7: Customer Loyalty Points

**Before (Hardcoded):**
```java
public void addLoyaltyPoints(Customer customer, BigDecimal purchaseAmount) {
    int points = purchaseAmount.intValue(); // 1 point per dollar
    customer.setLoyaltyPoints(customer.getLoyaltyPoints() + points);
}
```

**After (Configurable):**
```java
public void addLoyaltyPoints(Customer customer, BigDecimal purchaseAmount, Integer tenantId) {
    // Check if loyalty program is enabled
    Boolean loyaltyEnabled = configurationService.getConfigValueAsBoolean(
        "customer.loyalty_enabled", 
        false
    );
    
    if (!loyaltyEnabled) {
        return; // Loyalty program disabled
    }
    
    // Get points earning rate
    Double pointsPerCurrency = configurationService.getConfigValueAsDouble(
        "customer.loyalty_points_per_currency", 
        1.0
    );
    
    // Calculate points
    int points = (int) (purchaseAmount.doubleValue() * pointsPerCurrency);
    
    // Add points
    customer.setLoyaltyPoints(customer.getLoyaltyPoints() + points);
    
    // Check for VIP upgrade
    checkVIPUpgrade(customer, tenantId);
}

private void checkVIPUpgrade(Customer customer, Integer tenantId) {
    Double vipThreshold = configurationService.getConfigValueAsDouble(
        "customer.segment_vip_threshold", 
        10000.0
    );
    
    if (customer.getTotalPurchaseAmount().doubleValue() >= vipThreshold) {
        customer.setSegment(CustomerSegment.VIP);
    }
}
```

## Best Practices

### 1. Always Provide Defaults
```java
// Good - provides fallback
Integer threshold = configurationService.getConfigValueAsInt(
    "inventory.low_stock_threshold", 
    10  // Default value
);

// Bad - could return null
Integer threshold = configurationService.getConfigValueAsInt(
    "inventory.low_stock_threshold", 
    null
);
```

### 2. Cache Frequently Used Configurations
```java
@Service
public class CachedConfigService {
    private final ConfigurationService configurationService;
    private final Map<String, Object> cache = new ConcurrentHashMap<>();
    
    public String getConfig(String key, Integer tenantId) {
        String cacheKey = tenantId + "_" + key;
        return (String) cache.computeIfAbsent(cacheKey, 
            k -> configurationService.getConfigValue(key, tenantId)
        );
    }
    
    public void clearCache() {
        cache.clear();
    }
}
```

### 3. Validate Configuration Values
```java
public void setPaymentTerms(Integer tenantId, String value) {
    try {
        int days = Integer.parseInt(value);
        if (days < 0 || days > 365) {
            throw new ValidationException("Payment terms must be between 0 and 365 days");
        }
        // Save configuration
    } catch (NumberFormatException e) {
        throw new ValidationException("Payment terms must be a valid number");
    }
}
```

### 4. Document Configuration Keys
```java
/**
 * Configuration Keys Used:
 * - billing.invoice_prefix: Invoice number prefix
 * - billing.payment_terms_days: Default payment terms
 * - billing.require_customer: Whether customer is mandatory
 * - tax.enabled: Enable/disable tax calculations
 * - tax.default_rate: Default tax rate percentage
 */
@Service
public class InvoiceService {
    // ...
}
```

## Testing with Configurations

```java
@Test
public void testInvoiceGenerationWithCustomPrefix() {
    // Setup test configuration
    Integer testTenantId = 1;
    configurationService.createOrUpdateTenantConfiguration(
        TenantConfigurationDTO.builder()
            .tenantId(testTenantId)
            .configKey("billing.invoice_prefix")
            .configValue("TEST")
            .build()
    );
    
    // Test invoice generation
    String invoiceNumber = invoiceService.generateInvoiceNumber(testTenantId);
    
    // Verify it uses custom prefix
    assertTrue(invoiceNumber.startsWith("TEST/"));
}
```

## Summary

By using the ConfigurationService, you can:

✅ Make hardcoded values configurable  
✅ Support multi-tenant customization  
✅ Enable domain-agnostic features  
✅ Provide flexible business rules  
✅ Support regional variations  
✅ Allow customer-specific settings  

This approach makes your code more flexible, maintainable, and suitable for diverse business requirements.
