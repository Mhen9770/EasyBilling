# Service Context Paths

## Overview

Each microservice in the EasyBilling platform uses a unique context path to ensure clear separation and easy routing. This approach provides several benefits:

1. **Clear Service Identification**: Each service is immediately identifiable by its URL
2. **Simplified Gateway Routing**: API Gateway can easily route requests based on path prefix
3. **No Port Conflicts**: Services can be deployed on the same port with different context paths
4. **Better Organization**: Swagger/OpenAPI documentation is cleanly separated per service

## Context Path Strategy

All services follow this naming convention:
```
/{service-name}-service
```

## Implemented Services

### Tenant Service
- **Port**: 8082
- **Context Path**: `/tenant-service`
- **Base URL**: `http://localhost:8082/tenant-service`
- **API Endpoints**: `/tenant-service/api/v1/tenants/*`
- **Swagger UI**: `http://localhost:8082/tenant-service/swagger-ui.html`
- **Health Check**: `http://localhost:8082/tenant-service/actuator/health`

## Planned Services

### API Gateway
- **Port**: 8080
- **Context Path**: `/` (root)
- **Base URL**: `http://localhost:8080`
- **Purpose**: Routes requests to all backend services
- **Features**: JWT authentication, rate limiting, CORS
- **Health Check**: `http://localhost:8080/actuator/health`

**Routing:**
- All requests go through gateway (port 8080)
- Gateway routes to services based on context path
- Automatic JWT validation for protected endpoints
- Rate limiting: 100 requests/minute per IP

### Auth Service
- **Port**: 8081 (internal)
- **Context Path**: `/auth-service`
- **Access via Gateway**: `http://localhost:8080/auth-service/**`
- **API Endpoints**: `/auth-service/api/v1/auth/*`
- **Direct Swagger UI**: `http://localhost:8081/auth-service/swagger-ui.html`
- **Health Check**: `http://localhost:8081/auth-service/actuator/health`

### API Gateway
- **Port**: 8080
- **Context Path**: `/`
- **Routes to**: All backend services

### Billing Service
- **Port**: 8083
- **Context Path**: `/billing-service`
- **Base URL**: `http://localhost:8083/billing-service`

### Inventory Service
- **Port**: 8084
- **Context Path**: `/inventory-service`
- **Base URL**: `http://localhost:8084/inventory-service`

### Customer Service
- **Port**: 8085
- **Context Path**: `/customer-service`
- **Base URL**: `http://localhost:8085/customer-service`

### Pricing Service
- **Port**: 8086
- **Context Path**: `/pricing-service`
- **Base URL**: `http://localhost:8086/pricing-service`

### Tax Service
- **Port**: 8087
- **Context Path**: `/tax-service`
- **Base URL**: `http://localhost:8087/tax-service`

### Reporting Service
- **Port**: 8088
- **Context Path**: `/reporting-service`
- **Base URL**: `http://localhost:8088/reporting-service`

### Notification Service
- **Port**: 8089
- **Context Path**: `/notification-service`
- **Base URL**: `http://localhost:8089/notification-service`

## Configuration

### Spring Boot Configuration

In each service's `application.yml`:

```yaml
server:
  port: 808X
  servlet:
    context-path: /{service-name}-service
```

### API Gateway Routing

When the API Gateway is implemented, it will route requests like:

```
http://localhost:8080/tenant-service/* → http://localhost:8082/tenant-service/*
http://localhost:8080/auth-service/* → http://localhost:8081/auth-service/*
http://localhost:8080/billing-service/* → http://localhost:8083/billing-service/*
```

## Frontend Integration

The frontend uses the API Gateway URL as the base:

```typescript
const API_BASE_URL = process.env.NEXT_PUBLIC_API_URL || 'http://localhost:8080';
```

Then makes requests like:
```typescript
// Tenant operations
axios.get(`${API_BASE_URL}/tenant-service/api/v1/tenants`);

// Auth operations
axios.post(`${API_BASE_URL}/auth-service/api/v1/auth/login`);
```

## Benefits

1. **Microservices Independence**: Each service is self-contained with its own context
2. **Easy Debugging**: Service logs are clearly separated by context path
3. **Gateway Simplicity**: Simple prefix-based routing rules
4. **Monitoring**: Easy to monitor and track metrics per service
5. **Deployment Flexibility**: Can deploy services independently or together

## Example API Calls

### Direct Service Call (Development)
```bash
curl http://localhost:8082/tenant-service/api/v1/tenants
```

### Through API Gateway (Production)
```bash
curl http://api.easybilling.com/tenant-service/api/v1/tenants
```

## Notes

- Context paths are consistent across all environments (dev, staging, production)
- Swagger UI is accessible at `/{context-path}/swagger-ui.html` for each service
- Actuator endpoints are at `/{context-path}/actuator/*` for monitoring
- All API endpoints should start with `/{context-path}/api/v1/`

---

**Last Updated**: December 2024  
**Related Documentation**: 
- [ARCHITECTURE.md](./ARCHITECTURE.md)
- [QUICK_START.md](./QUICK_START.md)
