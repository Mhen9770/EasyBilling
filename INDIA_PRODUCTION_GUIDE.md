# Production-Ready Features for India Market Launch

## Overview
This guide focuses on **essential features** needed to launch EasyBilling in production for the Indian market. These are prioritized for immediate implementation (2-4 weeks) to get the application market-ready.

## Critical Features for India Launch

### 1. GST Compliance (MUST HAVE) ⚡
**Priority**: CRITICAL
**Timeline**: 1 week
**Effort**: 40 hours

#### GST Tax Engine Implementation

**Database Schema:**
```sql
-- Add to migration: V4__Add_GST_support.sql

CREATE TABLE gst_rates (
    id VARCHAR(36) PRIMARY KEY,
    hsn_code VARCHAR(20),
    tax_category VARCHAR(50) NOT NULL,
    cgst_rate DECIMAL(5,2) NOT NULL,
    sgst_rate DECIMAL(5,2) NOT NULL,
    igst_rate DECIMAL(5,2) NOT NULL,
    cess_rate DECIMAL(5,2) DEFAULT 0,
    effective_from DATE NOT NULL,
    effective_to DATE,
    is_active BOOLEAN DEFAULT TRUE
);

-- Update invoice_items table
ALTER TABLE invoice_items ADD COLUMN hsn_code VARCHAR(20);
ALTER TABLE invoice_items ADD COLUMN cgst_amount DECIMAL(10,2) DEFAULT 0;
ALTER TABLE invoice_items ADD COLUMN sgst_amount DECIMAL(10,2) DEFAULT 0;
ALTER TABLE invoice_items ADD COLUMN igst_amount DECIMAL(10,2) DEFAULT 0;
ALTER TABLE invoice_items ADD COLUMN cess_amount DECIMAL(10,2) DEFAULT 0;

-- Update invoices table
ALTER TABLE invoices ADD COLUMN total_cgst DECIMAL(10,2) DEFAULT 0;
ALTER TABLE invoices ADD COLUMN total_sgst DECIMAL(10,2) DEFAULT 0;
ALTER TABLE invoices ADD COLUMN total_igst DECIMAL(10,2) DEFAULT 0;
ALTER TABLE invoices ADD COLUMN total_cess DECIMAL(10,2) DEFAULT 0;
ALTER TABLE invoices ADD COLUMN place_of_supply VARCHAR(50);
ALTER TABLE invoices ADD COLUMN reverse_charge BOOLEAN DEFAULT FALSE;
```

**Backend Implementation:**

```java
// Entity: GstRate.java
@Entity
@Table(name = "gst_rates")
public class GstRate {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;
    
    @Column(name = "hsn_code", length = 20)
    private String hsnCode;
    
    @Column(name = "tax_category", nullable = false, length = 50)
    private String taxCategory;
    
    @Column(name = "cgst_rate", nullable = false)
    private BigDecimal cgstRate;
    
    @Column(name = "sgst_rate", nullable = false)
    private BigDecimal sgstRate;
    
    @Column(name = "igst_rate", nullable = false)
    private BigDecimal igstRate;
    
    @Column(name = "cess_rate")
    private BigDecimal cessRate = BigDecimal.ZERO;
    
    @Column(name = "effective_from", nullable = false)
    private LocalDate effectiveFrom;
    
    @Column(name = "effective_to")
    private LocalDate effectiveTo;
    
    @Column(name = "is_active")
    private Boolean isActive = true;
}

// Service: GstCalculationService.java
@Service
@RequiredArgsConstructor
public class GstCalculationService {
    
    private final GstRateRepository gstRateRepository;
    
    public GstCalculation calculateGst(
            String hsnCode, 
            BigDecimal amount, 
            String supplierState, 
            String customerState) {
        
        GstRate rate = gstRateRepository.findByHsnCodeAndActiveTrue(hsnCode)
                .orElseThrow(() -> new BusinessException("GST_RATE_NOT_FOUND", 
                    "GST rate not found for HSN: " + hsnCode));
        
        boolean isInterState = !supplierState.equals(customerState);
        
        GstCalculation calculation = new GstCalculation();
        
        if (isInterState) {
            // IGST for inter-state
            calculation.setIgst(amount.multiply(rate.getIgstRate()).divide(BigDecimal.valueOf(100)));
            calculation.setCgst(BigDecimal.ZERO);
            calculation.setSgst(BigDecimal.ZERO);
        } else {
            // CGST + SGST for intra-state
            calculation.setCgst(amount.multiply(rate.getCgstRate()).divide(BigDecimal.valueOf(100)));
            calculation.setSgst(amount.multiply(rate.getSgstRate()).divide(BigDecimal.valueOf(100)));
            calculation.setIgst(BigDecimal.ZERO);
        }
        
        calculation.setCess(amount.multiply(rate.getCessRate()).divide(BigDecimal.valueOf(100)));
        calculation.setTotalTax(calculation.getCgst()
                .add(calculation.getSgst())
                .add(calculation.getIgst())
                .add(calculation.getCess()));
        
        return calculation;
    }
}
```

**Key Features:**
- Auto GST calculation (CGST, SGST, IGST)
- HSN/SAC code support
- Inter-state vs Intra-state detection
- Reverse charge mechanism
- GST rate master data

---

### 2. GSTIN Validation (MUST HAVE) ⚡
**Priority**: CRITICAL
**Timeline**: 2 days
**Effort**: 10 hours

```java
@Service
public class GstinValidationService {
    
    public boolean validateGstin(String gstin) {
        if (gstin == null || gstin.length() != 15) {
            return false;
        }
        
        // Format: 22AAAAA0000A1Z5
        // First 2: State code
        // Next 10: PAN
        // 13th: Entity number
        // 14th: Z (default)
        // 15th: Checksum
        
        String regex = "^[0-9]{2}[A-Z]{5}[0-9]{4}[A-Z]{1}[1-9A-Z]{1}Z[0-9A-Z]{1}$";
        return gstin.matches(regex);
    }
    
    // Optional: API integration with GST portal for real-time validation
    public GstinDetails validateWithGstPortal(String gstin) {
        // Integrate with GST API (requires authentication)
        // Returns business name, address, registration status
    }
}
```

---

### 3. Indian Payment Gateways (MUST HAVE) ⚡
**Priority**: HIGH
**Timeline**: 3-4 days
**Effort**: 24 hours

#### Razorpay Integration (Most Popular in India)

```java
// Configuration
@Configuration
public class RazorpayConfig {
    @Value("${razorpay.key.id}")
    private String keyId;
    
    @Value("${razorpay.key.secret}")
    private String keySecret;
    
    @Bean
    public RazorpayClient razorpayClient() throws RazorpayException {
        return new RazorpayClient(keyId, keySecret);
    }
}

// Service
@Service
@RequiredArgsConstructor
public class RazorpayService {
    
    private final RazorpayClient razorpayClient;
    
    public String createOrder(BigDecimal amount, String currency, String receiptId) {
        try {
            JSONObject orderRequest = new JSONObject();
            orderRequest.put("amount", amount.multiply(BigDecimal.valueOf(100)).intValue()); // Convert to paise
            orderRequest.put("currency", currency);
            orderRequest.put("receipt", receiptId);
            
            Order order = razorpayClient.orders.create(orderRequest);
            return order.get("id");
        } catch (RazorpayException e) {
            throw new BusinessException("PAYMENT_ERROR", "Failed to create payment order", e);
        }
    }
    
    public boolean verifyPayment(String orderId, String paymentId, String signature) {
        String payload = orderId + "|" + paymentId;
        return Utils.verifySignature(payload, signature, keySecret);
    }
}
```

**Supported Payment Methods:**
- UPI (PhonePe, Google Pay, Paytm)
- Credit/Debit Cards
- Net Banking
- Wallets (Paytm, PhonePe, Amazon Pay)

**Alternative Gateways:**
- **Paytm** - For small businesses
- **PhonePe** - UPI focus
- **Cashfree** - Good for SMBs
- **Instamojo** - No setup fee

---

### 4. Invoice Numbering (India Format) (MUST HAVE) ⚡
**Priority**: HIGH
**Timeline**: 1 day
**Effort**: 6 hours

```java
@Service
public class InvoiceNumberService {
    
    // Format: INV/2024-25/0001
    // Financial year: April to March
    
    public String generateInvoiceNumber(String tenantId) {
        String financialYear = getCurrentFinancialYear();
        int sequence = getNextSequence(tenantId, financialYear);
        
        return String.format("INV/%s/%04d", financialYear, sequence);
    }
    
    private String getCurrentFinancialYear() {
        LocalDate now = LocalDate.now();
        int year = now.getYear();
        int month = now.getMonthValue();
        
        if (month >= 4) {
            // April onwards - current year to next year
            return String.format("%d-%02d", year, (year + 1) % 100);
        } else {
            // Jan to March - previous year to current year
            return String.format("%d-%02d", year - 1, year % 100);
        }
    }
}
```

---

### 5. E-Invoice & E-Way Bill Support (IMPORTANT) 📋
**Priority**: HIGH
**Timeline**: 5 days
**Effort**: 30 hours

```java
@Entity
@Table(name = "e_invoices")
public class EInvoice {
    @Id
    private String id;
    
    @Column(name = "invoice_id")
    private String invoiceId;
    
    @Column(name = "irn", unique = true) // Invoice Reference Number
    private String irn;
    
    @Column(name = "ack_no")
    private String ackNo;
    
    @Column(name = "ack_date")
    private Instant ackDate;
    
    @Column(name = "qr_code", columnDefinition = "TEXT")
    private String qrCode;
    
    @Column(name = "signed_invoice", columnDefinition = "TEXT")
    private String signedInvoice;
    
    @Column(name = "status")
    @Enumerated(EnumType.STRING)
    private EInvoiceStatus status;
}

@Service
public class EInvoiceService {
    
    public EInvoice generateIRN(Invoice invoice) {
        // Generate Invoice Reference Number (IRN)
        // Integrate with NIC E-Invoice API
        // Returns IRN, QR Code, and digitally signed invoice
    }
    
    public EWayBill generateEWayBill(Invoice invoice, TransportDetails transport) {
        // For goods movement > 50,000 INR
        // Integrate with E-Way Bill API
    }
}
```

**Note:** Mandatory for businesses with turnover > 5 crores

---

### 6. Indian Banking & Payment Reports (MUST HAVE) 📊
**Priority**: HIGH
**Timeline**: 2 days
**Effort**: 12 hours

```java
// GST Reports
@Service
public class GstReportService {
    
    public GstR1Report generateGSTR1(String tenantId, String month, String year) {
        // B2B Invoices
        // B2C Invoices (> 2.5 lakhs)
        // Credit/Debit Notes
        // Exports
        // HSN Summary
    }
    
    public GstR3BReport generateGSTR3B(String tenantId, String month, String year) {
        // Summary of outward supplies
        // ITC claims
        // Tax payable
        // Interest and late fees
    }
}

// TDS Report
@Service
public class TdsReportService {
    
    public TdsReport generateTDS(String tenantId, String quarter) {
        // TDS deducted on services
        // Form 26Q data
        // TAN details
    }
}
```

---

### 7. Multi-Language Support (IMPORTANT) 🌐
**Priority**: MEDIUM
**Timeline**: 3 days
**Effort**: 18 hours

**Languages to Support:**
1. English (Default)
2. Hindi (हिन्दी)
3. Marathi (मराठी)
4. Tamil (தமிழ்)
5. Telugu (తెలుగు)
6. Bengali (বাংলা)
7. Gujarati (ગુજરાતી)
8. Kannada (ಕನ್ನಡ)

**Implementation:**

```java
// Backend - i18n support
@Configuration
public class LocaleConfig {
    @Bean
    public LocaleResolver localeResolver() {
        SessionLocaleResolver resolver = new SessionLocaleResolver();
        resolver.setDefaultLocale(new Locale("en", "IN"));
        return resolver;
    }
    
    @Bean
    public MessageSource messageSource() {
        ResourceBundleMessageSource messageSource = new ResourceBundleMessageSource();
        messageSource.setBasename("messages");
        messageSource.setDefaultEncoding("UTF-8");
        return messageSource;
    }
}
```

**Frontend:**
```typescript
// Use react-i18next
import i18n from 'i18next';
import { initReactI18next } from 'react-i18next';

i18n
  .use(initReactI18next)
  .init({
    resources: {
      en: { translation: require('./locales/en.json') },
      hi: { translation: require('./locales/hi.json') },
      // ... other languages
    },
    lng: 'en',
    fallbackLng: 'en',
  });
```

---

### 8. Indian Number Formatting (MUST HAVE) 💰
**Priority**: HIGH
**Timeline**: 1 day
**Effort**: 4 hours

**Indian Numbering System:**
- Use lakhs and crores (not millions and billions)
- Format: 12,34,567.89 (not 1,234,567.89)

```java
@Service
public class IndianNumberFormatter {
    
    public String formatCurrency(BigDecimal amount) {
        NumberFormat indianFormat = NumberFormat.getCurrencyInstance(new Locale("en", "IN"));
        return indianFormat.format(amount);
    }
    
    public String formatNumber(BigDecimal number) {
        DecimalFormat indianFormatter = new DecimalFormat("##,##,###.##", 
            new DecimalFormatSymbols(new Locale("en", "IN")));
        return indianFormatter.format(number);
    }
}
```

**Frontend:**
```typescript
export const formatIndianCurrency = (amount: number): string => {
  return new Intl.NumberFormat('en-IN', {
    style: 'currency',
    currency: 'INR',
    minimumFractionDigits: 2,
  }).format(amount);
};

// Output: ₹12,34,567.89
```

---

### 9. Essential Security for Production (MUST HAVE) 🔒
**Priority**: CRITICAL
**Timeline**: 2 days
**Effort**: 12 hours

**Already Implemented:** ✅
- Permission-based access control
- Security groups
- ADMIN/STAFF roles

**Additional Requirements:**

```yaml
# application-prod.yml
spring:
  datasource:
    url: ${DB_URL}
    username: ${DB_USER}
    password: ${DB_PASSWORD}
    
  security:
    require-ssl: true
    
  session:
    timeout: 30m
    
server:
  ssl:
    enabled: true
    key-store: ${SSL_KEYSTORE_PATH}
    key-store-password: ${SSL_KEYSTORE_PASSWORD}
    
logging:
  level:
    root: INFO
    com.easybilling: INFO
    
management:
  endpoints:
    web:
      exposure:
        include: health,metrics
```

**Security Checklist:**
- ✅ SSL/TLS enabled
- ✅ Environment variables for secrets
- ✅ SQL injection prevention (JPA)
- ✅ CSRF protection
- ✅ Rate limiting (Redis)
- ✅ Input validation
- ✅ Password encryption (BCrypt)
- ✅ Session management

---

### 10. Production Database Setup (MUST HAVE) 🗄️
**Priority**: CRITICAL
**Timeline**: 1 day
**Effort**: 6 hours

**Recommended Setup:**

```yaml
# AWS RDS MySQL Configuration
Instance Class: db.t3.medium (2 vCPU, 4 GB RAM)
Storage: 100 GB SSD (Auto-scaling enabled)
Multi-AZ: Yes (for high availability)
Backup: Automated daily backups (7 day retention)
Encryption: At rest enabled

# Or
# DigitalOcean Managed MySQL
Plan: 2 GB RAM, 1 vCPU, 25 GB SSD
Standby Node: Yes
Automated Backups: Yes
```

**Connection Pool:**
```yaml
spring:
  datasource:
    hikari:
      maximum-pool-size: 20
      minimum-idle: 5
      connection-timeout: 30000
      idle-timeout: 600000
      max-lifetime: 1800000
```

---

### 11. Redis Cache Setup (MUST HAVE) ⚡
**Priority**: HIGH
**Timeline**: 1 day
**Effort**: 4 hours

**Already Configured:** ✅
- Permission caching
- Security group caching

**Production Setup:**

```yaml
# AWS ElastiCache Redis or DigitalOcean Managed Redis
Node Type: cache.t3.micro (0.5 GB)
Replicas: 1 (for high availability)
Automatic Failover: Yes
```

---

### 12. Deployment Setup (MUST HAVE) 🚀
**Priority**: CRITICAL
**Timeline**: 2 days
**Effort**: 12 hours

**Recommended Stack:**

**Option 1: AWS (Recommended)**
```yaml
Application: AWS Elastic Beanstalk (Java 17)
Database: AWS RDS MySQL
Cache: AWS ElastiCache Redis
Storage: AWS S3 (for invoices/documents)
CDN: AWS CloudFront
Domain: Route 53
SSL: AWS Certificate Manager (Free)
```

**Option 2: DigitalOcean (Budget-friendly)**
```yaml
Application: DigitalOcean App Platform (Docker)
Database: DigitalOcean Managed MySQL
Cache: DigitalOcean Managed Redis
Storage: DigitalOcean Spaces
CDN: DigitalOcean CDN
Domain: DigitalOcean DNS
SSL: Let's Encrypt (Free)
```

**Docker Configuration:**
```dockerfile
# Dockerfile
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY target/easybilling-1.0.0-SNAPSHOT.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "-Dspring.profiles.active=prod", "app.jar"]
```

**Docker Compose (for testing):**
```yaml
version: '3.8'
services:
  app:
    build: .
    ports:
      - "8080:8080"
    environment:
      - SPRING_PROFILES_ACTIVE=prod
      - DB_HOST=mysql
      - REDIS_HOST=redis
    depends_on:
      - mysql
      - redis
      
  mysql:
    image: mysql:8.0
    environment:
      MYSQL_DATABASE: easybilling
      MYSQL_ROOT_PASSWORD: ${DB_PASSWORD}
    volumes:
      - mysql_data:/var/lib/mysql
      
  redis:
    image: redis:7-alpine
    
volumes:
  mysql_data:
```

---

## Implementation Priority for India Launch

### Week 1 (CRITICAL - Must Complete)
1. ✅ GST Tax Engine (3 days)
2. ✅ GSTIN Validation (1 day)
3. ✅ Indian Invoice Numbering (1 day)
4. ✅ Indian Number Formatting (1 day)
5. ✅ Production Security Setup (1 day)

### Week 2 (HIGH PRIORITY)
6. ✅ Razorpay Integration (3 days)
7. ✅ GST Reports (GSTR-1, GSTR-3B) (2 days)
8. ✅ Production Database Setup (1 day)
9. ✅ Redis Setup (1 day)

### Week 3-4 (IMPORTANT)
10. ✅ E-Invoice Support (5 days)
11. ✅ Multi-language Support (3 days)
12. ✅ Deployment Setup (2 days)
13. ✅ Load Testing (2 days)
14. ✅ Documentation (1 day)

---

## Cost Estimation for India Launch

### Monthly Operational Costs

**Infrastructure (AWS - Recommended)**
- EC2 t3.medium (Application): $30/month
- RDS MySQL db.t3.medium: $50/month
- ElastiCache Redis: $15/month
- S3 Storage (100 GB): $2/month
- CloudFront (CDN): $10/month
- Route 53 (DNS): $1/month
- **Total Infrastructure**: ~₹9,000/month ($107)

**Alternative (DigitalOcean - Budget)**
- App Platform (1 GB): $12/month
- Managed MySQL (2 GB): $15/month
- Managed Redis (1 GB): $15/month
- Spaces (100 GB): $5/month
- **Total Infrastructure**: ~₹4,000/month ($47)

**Third-Party Services**
- Razorpay: 2% transaction fee (no setup cost)
- SMS (for OTP): ₹0.20/SMS
- Email (SendGrid): Free tier (100/day)
- **Monthly**: ~₹2,000 (depends on volume)

**Total Monthly Cost**: ₹6,000 - ₹11,000/month

---

## Pre-Launch Checklist

### Legal & Compliance ✅
- [ ] GST Registration
- [ ] Business Registration
- [ ] Privacy Policy
- [ ] Terms of Service
- [ ] Refund Policy
- [ ] Data Protection compliance

### Technical ✅
- [ ] SSL Certificate installed
- [ ] Database backups configured
- [ ] Monitoring setup (Uptime, Errors)
- [ ] Load testing completed
- [ ] Security audit completed
- [ ] API documentation ready

### Business ✅
- [ ] Pricing plans defined
- [ ] Payment gateway account activated
- [ ] Support email/phone setup
- [ ] Landing page ready
- [ ] Demo videos created
- [ ] Initial marketing materials

---

## Post-Launch Roadmap (Next 3-6 Months)

### Month 1-2
1. **WhatsApp Integration** - Send invoices via WhatsApp Business API
2. **Barcode Scanning** - For POS (using phone camera)
3. **Offline Mode** - PWA for offline billing
4. **TDS Support** - TDS calculation and reports

### Month 3-4
5. **Inventory Alerts** - Low stock notifications
6. **Multiple Stores** - Branch management
7. **Staff Attendance** - Basic time tracking
8. **Advanced Reports** - Custom report builder

### Month 5-6
9. **Mobile Apps** - Native Android app
10. **E-Way Bill** - Auto-generation
11. **Subscription Plans** - For SaaS customers
12. **API Access** - For integrations

---

## Quick Start Implementation Guide

### Step 1: GST Tax Engine (Day 1-3)

1. Create migration file
2. Add GstRate entity and repository
3. Create GstCalculationService
4. Update Invoice and InvoiceItem entities
5. Modify invoice creation logic to include GST
6. Add GST rate master data seeder
7. Test with sample invoices

### Step 2: Payment Gateway (Day 4-6)

1. Sign up for Razorpay account
2. Add Razorpay SDK dependency
3. Create RazorpayService
4. Add payment endpoints
5. Update invoice to track payment status
6. Test payment flow (use test mode)

### Step 3: Reports & Formatting (Day 7-9)

1. Implement Indian number formatter
2. Update invoice templates
3. Create GST report services
4. Add report generation endpoints
5. Test report accuracy

### Step 4: Production Deployment (Day 10-14)

1. Setup production database (AWS RDS/DigitalOcean)
2. Configure Redis cache
3. Setup application server (Elastic Beanstalk/App Platform)
4. Configure SSL certificate
5. Setup domain and DNS
6. Deploy application
7. Run smoke tests
8. Monitor for 24 hours

---

## Sample Configuration Files

### application-prod.yml
```yaml
spring:
  application:
    name: easybilling-production
    
  datasource:
    url: ${DB_URL:jdbc:mysql://localhost:3306/easybilling}
    username: ${DB_USER:root}
    password: ${DB_PASSWORD}
    hikari:
      maximum-pool-size: 20
      minimum-idle: 5
      
  jpa:
    hibernate:
      ddl-auto: validate
    show-sql: false
    
  data:
    redis:
      host: ${REDIS_HOST:localhost}
      port: ${REDIS_PORT:6379}
      
  cache:
    type: redis
    redis:
      time-to-live: 3600000
      
server:
  port: 8080
  compression:
    enabled: true
    
app:
  jwt:
    secret: ${JWT_SECRET}
    access-token-validity-ms: 3600000
    
  razorpay:
    key-id: ${RAZORPAY_KEY_ID}
    key-secret: ${RAZORPAY_KEY_SECRET}
    
  gst:
    api-url: ${GST_API_URL}
    api-key: ${GST_API_KEY}
```

---

## Support & Maintenance

### Monitoring (Free Tools)
- **UptimeRobot**: 50 monitors free (check if app is up)
- **Sentry**: Error tracking (free tier)
- **Google Analytics**: User tracking
- **AWS CloudWatch**: Infrastructure monitoring

### Backup Strategy
- **Database**: Automated daily backups (7-day retention)
- **Code**: GitHub (already done)
- **Documents**: S3 with versioning enabled

### Support Channels
- Email: support@easybilling.in
- Phone: +91-XXXXXXXXXX
- WhatsApp Business: +91-XXXXXXXXXX
- Documentation: docs.easybilling.in

---

## Success Metrics

### Technical KPIs
- Uptime: > 99.5%
- Page Load Time: < 2 seconds
- API Response Time: < 500ms
- Error Rate: < 0.1%

### Business KPIs
- Customer Acquisition: 50 businesses/month
- Churn Rate: < 5%
- Revenue: ₹5 lakhs/month (by month 6)
- Support Tickets: < 10/week

---

## Conclusion

**Time to Production**: 2-4 weeks
**Initial Investment**: ₹50,000 - ₹1,00,000 (development)
**Monthly Operating Cost**: ₹6,000 - ₹11,000

**Essential Features Implemented:**
✅ Enterprise Security (Already Done)
✅ Multi-tenant Architecture (Already Done)
🔧 GST Compliance (2-4 weeks)
🔧 Payment Gateway (2-4 weeks)
🔧 Production Infrastructure (2-4 weeks)

The application is **80% ready** for production. The remaining 20% focuses on India-specific features (GST, payments, formatting) which are critical but straightforward to implement.

**Recommended Approach:**
1. Implement GST engine (Week 1)
2. Integrate Razorpay (Week 2)
3. Setup production infrastructure (Week 3)
4. Testing & soft launch (Week 4)
5. Official launch with marketing

This roadmap prioritizes **speed to market** while maintaining **quality and compliance** for the Indian market.
