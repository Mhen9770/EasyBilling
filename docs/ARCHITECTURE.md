# EasyBilling Platform - Architecture Documentation

## Overview

EasyBilling is a comprehensive, enterprise-grade, multi-tenant billing and POS platform built using modern microservices architecture. The platform is designed to serve all kinds of retail businesses including grocery stores, garments shops, electronics stores, jewelry shops, furniture stores, pharmacies, and more.

## Architecture Principles

### 1. Multi-Tenancy
- **Strategy**: Schema-per-tenant (default), with support for database-per-tenant and shared schema
- **Isolation**: Complete data isolation between tenants at the database schema level
- **Tenant Resolution**: Multiple resolution strategies (subdomain, HTTP header, JWT token)
- **Dynamic Provisioning**: Automated schema creation and migration on tenant onboarding

### 2. Microservices
- **Service Decomposition**: Domain-driven design with bounded contexts
- **Communication**: REST APIs with OpenAPI documentation
- **Inter-service**: Feign Client for synchronous communication
- **Future**: Event-driven architecture with Kafka/RabbitMQ for async operations

### 3. Technology Stack

#### Backend
- **Framework**: Spring Boot 3.4.x
- **Language**: Java 17
- **Build Tool**: Gradle with Kotlin DSL
- **Database**: MySQL 8 with Flyway migrations
- **Cache**: Redis 7 for hot data and session management
- **Security**: Spring Security 6+ with JWT authentication
- **API Documentation**: SpringDoc OpenAPI 3.0

#### Frontend
- **Framework**: Next.js 15 with App Router
- **Language**: TypeScript (strict mode)
- **Styling**: Tailwind CSS
- **State Management**: Zustand for client state, TanStack Query for server state
- **Components**: Custom components with shadcn/ui patterns

#### Infrastructure
- **Containerization**: Docker
- **Orchestration**: Kubernetes-ready
- **CI/CD**: GitHub Actions
- **Monitoring**: Spring Boot Actuator + Prometheus + Grafana

## System Components

### Shared Libraries

#### 1. Common Library (`libs/common`)
- Standard API response wrappers
- Common DTOs (ApiResponse, PageResponse, ErrorDetails)
- Exception hierarchy (BusinessException, ResourceNotFoundException, etc.)
- Utility classes (DateTimeUtils, etc.)
- Error codes and constants

#### 2. Multi-Tenancy Library (`libs/multi-tenancy`)
- TenantContext (ThreadLocal storage)
- Tenant resolvers (Header, Subdomain)
- Tenant interceptor for request processing
- Configuration and integration points

#### 3. Security Library (`libs/security`)
- JWT token generation and validation
- Authentication filters
- Authorization utilities
- Role and permission management

### Microservices

#### 0. API Gateway (Port: 8080) ✅
**Responsibility**: Request routing, authentication, and rate limiting

**Key Features**:
- Centralized entry point for all services  
- JWT authentication filter for protected endpoints
- Rate limiting (100 requests/minute per IP) using Redis
- CORS configuration for frontend
- Circuit breaker patterns with Resilience4j
- Dynamic routing based on context paths

**Routing Rules:**
- `/auth-service/**` → Auth Service (8081)
- `/tenant-service/**` → Tenant Service (8082)
- Public endpoints: login, register, refresh
- Protected: All other endpoints require JWT

#### 1. Tenant Service (Port: 8082, Context: /tenant-service) ✅
**Responsibility**: Tenant lifecycle management

**Key Features**:
- Tenant CRUD operations
- Subscription management (Trial, Active, Suspended, Cancelled)
- Dynamic schema provisioning
- Tenant activation/suspension/cancellation
- Plan management (Basic, Pro, Enterprise)

**Database Tables**:
- `tenants` (master schema)
- Per-tenant schemas with base tables

**Endpoints** (All prefixed with `/tenant-service`):
- `POST /tenant-service/api/v1/tenants` - Create tenant
- `GET /tenant-service/api/v1/tenants/{id}` - Get tenant details
- `PUT /tenant-service/api/v1/tenants/{id}` - Update tenant
- `POST /tenant-service/api/v1/tenants/{id}/activate` - Activate tenant
- `POST /tenant-service/api/v1/tenants/{id}/suspend` - Suspend tenant
- `GET /tenant-service/api/v1/tenants` - List tenants with pagination

#### 2. Auth Service (Port: 8081, Context: /auth-service) ✅
**Responsibility**: Authentication and authorization

**Key Features**:
- User authentication with JWT tokens
- Role-based access control (RBAC)
- BCrypt password encryption
- Account lockout after failed attempts
- Access and refresh token generation
- User registration and login

**Endpoints** (All prefixed with `/auth-service`):
- `POST /auth-service/api/v1/auth/login` - User login
- `POST /auth-service/api/v1/auth/register` - User registration
- `POST /auth-service/api/v1/auth/refresh` - Refresh access token
- `POST /auth-service/api/v1/auth/logout` - User logout

#### 3. Billing Service (Port: 8083) [Planned]
**Responsibility**: Core billing and POS operations

**Key Features**:
- Invoice creation and management
- Multiple payment modes
- Returns and exchanges
- Hold/resume bill functionality
- Barcode scanning integration
- Receipt printing

#### 4. Inventory Service (Port: 8084) [Planned]
**Responsibility**: Product and inventory management

**Key Features**:
- Product catalog management
- Product variants (size, color, attributes)
- Stock tracking and movements
- Stock adjustments and transfers
- Purchase orders and GRN
- Batching and expiry tracking

#### 5. API Gateway (Port: 8080) [Planned]
**Responsibility**: Single entry point for all clients

**Key Features**:
- Request routing
- Load balancing
- Rate limiting
- Authentication validation
- Tenant resolution
- API versioning

## Data Flow

### 1. Request Flow
```
Client → API Gateway → Tenant Resolution → Service Router → Microservice
                         ↓
                    TenantContext
                         ↓
                    Data Access Layer (Tenant Schema)
```

### 2. Tenant Onboarding Flow
```
1. Client submits tenant creation request
2. Tenant Service validates request
3. Create tenant record in master database
4. Provisioning Service creates tenant schema
5. Run Flyway migrations for tenant schema
6. Update tenant status to TRIAL
7. Return tenant details to client
```

### 3. Multi-Tenant Data Access
```
1. Request arrives with tenant identifier
2. Tenant Interceptor resolves tenant ID
3. TenantContext stores tenant ID in ThreadLocal
4. Data access layer switches to tenant schema
5. Query executes in tenant-specific schema
6. Response returned to client
7. TenantContext cleared after request
```

## Database Design

### Master Schema (public)
- `tenants` - Master tenant registry
- `users` - Global user accounts
- `audit_logs` - Platform-wide audit trail

### Tenant Schema (tenant_*)
Each tenant gets their own schema with:
- `users` - Tenant-specific users
- `stores` - Stores/branches
- `products` - Product catalog
- `invoices` - Sales invoices
- `customers` - Customer master
- `inventory` - Stock records
- And more...

## Security

### Authentication
- JWT tokens with RS256 signing
- Token expiration: 1 hour (access), 7 days (refresh)
- Token claims: user_id, tenant_id, roles, permissions

### Authorization
- Three-tier role system:
  1. SUPER_ADMIN - Platform administration
  2. TENANT_ADMIN - Tenant management
  3. STAFF - Configurable permissions

### Data Security
- Schema-level isolation between tenants
- Row-level security for shared tables
- Encrypted sensitive data (passwords, payment info)
- HTTPS/TLS for all communications

## Scalability

### Horizontal Scaling
- Stateless services for easy horizontal scaling
- Redis for distributed caching and sessions
- Database connection pooling (HikariCP)

### Performance Optimization
- Redis caching for:
  - Product catalog
  - Tenant metadata
  - Configuration data
  - Active sessions
- Database indexing strategies
- Query optimization with JPA
- Pagination for large datasets

### Load Distribution
- API Gateway for load balancing
- Multiple service instances per microservice
- Database read replicas (future)

## Monitoring & Observability

### Metrics
- Spring Boot Actuator endpoints
- Custom business metrics
- JVM metrics (memory, threads, GC)

### Logging
- Structured JSON logs
- Correlation IDs for request tracing
- Centralized logging (ELK stack ready)

### Health Checks
- Service health endpoints
- Database connectivity checks
- Redis connectivity checks
- Dependency health monitoring

## Deployment

### Development
```bash
docker-compose up -d
```

### Production
- Kubernetes deployment
- Helm charts for configuration
- GitOps workflow
- Blue-green deployment strategy

## Future Enhancements

1. **Event-Driven Architecture**
   - Kafka/RabbitMQ integration
   - Async communication between services
   - Event sourcing for audit trail

2. **Advanced Analytics**
   - OpenSearch/Elasticsearch integration
   - Real-time analytics dashboard
   - Machine learning for predictions

3. **Mobile Applications**
   - React Native mobile apps
   - Offline-first architecture
   - Mobile POS capabilities

4. **External Integrations**
   - Payment gateway adapters
   - SMS/Email/WhatsApp providers
   - Accounting software integration
   - E-commerce platform sync

## References

- [API Documentation](./api/README.md)
- [Deployment Guide](./deployment/README.md)
- [Development Plan](../DevelopmentPlan.md)
