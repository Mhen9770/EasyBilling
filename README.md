# EasyBilling Platform

A comprehensive, enterprise-grade, multi-tenant billing SaaS platform designed to serve all kinds of shops and showrooms including grocery stores, garments, electronics, jewellery, furniture, pharmacies, and more.

## 🏗️ Architecture Overview

EasyBilling is built using a **microservices architecture** with a modular monolith approach within each service. This design enables independent scaling and deployment while maintaining clean module boundaries.

### Tech Stack

#### Backend
- **Java 21** - Latest LTS with modern features (Records, Pattern Matching, Sealed Interfaces)
- **Spring Boot 3.4.x** - Core framework
- **Spring Security 6+** - Authentication & Authorization
- **Spring Data JPA** - Data access layer
- **Spring Cloud** - Microservices infrastructure
- **Gradle (Kotlin DSL)** - Build tool
- **PostgreSQL** - Primary database
- **Redis** - Caching layer
- **OpenSearch** - Search & Analytics (optional)

#### Frontend
- **Next.js 15** (App Router) - React framework
- **React 19** - UI library
- **TypeScript (Strict Mode)** - Type safety
- **Tailwind CSS** - Styling
- **shadcn/ui** - Component library
- **TanStack Query** - Server state management
- **Zustand** - Client state management

#### DevOps
- **Docker & Docker Compose** - Containerization
- **Kubernetes** - Orchestration
- **GitHub Actions** - CI/CD
- **Prometheus & Grafana** - Monitoring
- **ELK Stack** - Logging

## 📁 Project Structure

```
EasyBilling/
├── backend/                          # Backend services (Java/Spring Boot)
│   ├── buildSrc/                     # Gradle build logic
│   ├── libs/                         # Shared libraries
│   │   ├── common/                   # Common utilities
│   │   ├── security/                 # Security components
│   │   └── multi-tenancy/            # Multi-tenancy framework
│   ├── services/                     # Microservices
│   │   ├── auth-service/             # Authentication & IAM
│   │   ├── tenant-service/           # Tenant & Subscription Management
│   │   ├── billing-service/          # Billing & POS
│   │   ├── inventory-service/        # Product & Inventory
│   │   ├── customer-service/         # Customer & Loyalty
│   │   ├── pricing-service/          # Pricing & Offers
│   │   ├── tax-service/              # Tax & Compliance
│   │   ├── reporting-service/        # Reports & Analytics
│   │   ├── notification-service/     # Notifications
│   │   └── gateway-service/          # API Gateway
│   ├── build.gradle.kts              # Root build file
│   └── settings.gradle.kts           # Gradle settings
├── frontend/                         # Frontend application (Next.js)
│   ├── app/                          # Next.js app directory
│   │   ├── (superadmin)/            # Super admin portal
│   │   ├── (tenant)/                # Tenant backoffice
│   │   ├── (pos)/                   # POS interface
│   │   └── api/                     # API routes
│   ├── components/                  # React components
│   ├── lib/                         # Utilities & hooks
│   └── public/                      # Static assets
├── infrastructure/                   # Infrastructure as Code
│   ├── docker/                      # Docker files
│   ├── kubernetes/                  # K8s manifests
│   └── terraform/                   # Cloud infrastructure
├── docs/                            # Documentation
├── .github/                         # GitHub Actions workflows
└── DevelopmentPlan.md              # Detailed development plan

```

## 🏢 Multi-Tenancy Strategy

The platform supports three multi-tenancy models:

1. **Schema-per-tenant** (Default) - Recommended for most use cases
2. **Database-per-tenant** - For very large enterprise tenants
3. **Shared schema with tenant_id** - For small instances

### Tenant Resolution
- Subdomain: `tenantX.easybilling.com`
- HTTP Header: `X-Tenant-Id`
- JWT token context

## 🔑 Key Features

### Multi-Tenant Services
1. **Billing & POS** - Invoice creation, returns, exchanges, multiple payment modes
2. **Inventory & Product Catalog** - Products, variants, stock management
3. **Customer & Loyalty** - Customer management, loyalty points, membership
4. **Supplier & Purchase** - Purchase orders, GRN, returns
5. **Pricing & Offers** - Rule-based discounts, promotions
6. **Tax & Compliance** - GST/VAT configuration, tax calculations
7. **Reporting & Analytics** - Sales reports, inventory analysis
8. **Notifications** - SMS, Email, WhatsApp
9. **Document Templates** - Invoice templates, branding

### Global Services
1. **Auth & IAM** - User authentication, role-based access control
2. **Tenant Management** - Onboarding, subscription lifecycle
3. **Configuration** - Master data, global settings
4. **Integration Gateway** - Payment gateways, external integrations
5. **Audit & Logging** - Activity logs, security events
6. **API Gateway** - Single entry point, rate limiting

## 🚀 Getting Started

### Prerequisites
- Java 21 or higher
- Node.js 20 or higher
- Docker & Docker Compose
- PostgreSQL 16
- Redis 7

### Local Development Setup

#### 1. Clone the repository
```bash
git clone https://github.com/Mhen9770/EasyBilling.git
cd EasyBilling
```

#### 2. Start infrastructure services
```bash
docker-compose up -d postgres redis
```

#### 3. Setup Backend
```bash
cd backend
./gradlew clean build
./gradlew bootRun
```

#### 4. Setup Frontend
```bash
cd frontend
npm install
npm run dev
```

#### 5. Access the application
- Frontend: http://localhost:3000
- API Gateway: http://localhost:8080
- API Documentation: http://localhost:8080/swagger-ui.html

## 🔐 Security

- JWT-based authentication with OAuth2
- Role-Based Access Control (RBAC)
- Fine-grained permissions per tenant
- Data isolation between tenants
- CSRF protection
- Input validation at all layers

### Default Roles
- **SUPER_ADMIN** - Platform administration
- **TENANT_ADMIN** - Tenant management
- **STAFF** - Configurable permissions per user

## 📊 API Documentation

API documentation is available via OpenAPI/Swagger at `/swagger-ui.html` on each service.

## 🧪 Testing

```bash
# Backend tests
cd backend
./gradlew test

# Frontend tests
cd frontend
npm test

# Integration tests
./gradlew integrationTest
```

## 📦 Deployment

### Docker
```bash
docker-compose up -d
```

### Kubernetes
```bash
kubectl apply -f infrastructure/kubernetes/
```

### Helm Chart
```bash
helm install easybilling ./infrastructure/helm/easybilling
```

## 🔄 CI/CD

GitHub Actions workflows are configured for:
- Automated testing
- Code quality checks
- Security scanning
- Docker image building
- Kubernetes deployment

## 📈 Monitoring & Observability

- **Metrics**: Prometheus + Spring Boot Actuator
- **Logging**: ELK Stack (Elasticsearch, Logstash, Kibana)
- **Tracing**: OpenTelemetry
- **Health Checks**: `/actuator/health` endpoints

## 📝 License

[Add your license here]

## 👥 Contributing

[Add contribution guidelines]

## 📞 Support

For support, please contact [your support email/link]

---

Built with ❤️ for modern retail businesses
