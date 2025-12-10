# Service Configuration Quick Reference

## Service Ports and Context Paths

| Service | Port | Context Path | Base URL (Local) |
|---------|------|--------------|------------------|
| Gateway Service | 8080 | `/` | `http://localhost:8080` |
| Auth Service | 8081 | `/auth-service` | `http://localhost:8081/auth-service` |
| Tenant Service | 8082 | `/tenant-service` | `http://localhost:8082/tenant-service` |
| Billing Service | 8083 | `/billing-service` | `http://localhost:8083/billing-service` |
| Inventory Service | 8084 | `/inventory-service` | `http://localhost:8084/inventory-service` |
| Customer Service | 8085 | `/customer-service` | `http://localhost:8085/customer-service` |
| Supplier Service | 8086 | `/supplier-service` | `http://localhost:8086/supplier-service` |
| Reports Service | 8087 | `/reports-service` | `http://localhost:8087/reports-service` |

## API Endpoints Examples

### Authentication Endpoints
```
POST /auth-service/api/v1/auth/login
POST /auth-service/api/v1/auth/register
POST /auth-service/api/v1/auth/refresh
POST /auth-service/api/v1/auth/logout
```

### Customer Endpoints
```
GET    /customer-service/api/v1/customers
POST   /customer-service/api/v1/customers
GET    /customer-service/api/v1/customers/{id}
PUT    /customer-service/api/v1/customers/{id}
DELETE /customer-service/api/v1/customers/{id}
```

### Supplier Endpoints
```
GET    /supplier-service/api/v1/suppliers
POST   /supplier-service/api/v1/suppliers
GET    /supplier-service/api/v1/suppliers/{id}
PUT    /supplier-service/api/v1/suppliers/{id}
DELETE /supplier-service/api/v1/suppliers/{id}
```

### Inventory Endpoints
```
GET    /inventory-service/api/v1/products
POST   /inventory-service/api/v1/products
GET    /inventory-service/api/v1/products/{id}
PUT    /inventory-service/api/v1/products/{id}
DELETE /inventory-service/api/v1/products/{id}
GET    /inventory-service/api/v1/products/barcode/{barcode}

GET    /inventory-service/api/v1/categories
POST   /inventory-service/api/v1/categories
PUT    /inventory-service/api/v1/categories/{id}
DELETE /inventory-service/api/v1/categories/{id}

GET    /inventory-service/api/v1/brands
POST   /inventory-service/api/v1/brands
PUT    /inventory-service/api/v1/brands/{id}
DELETE /inventory-service/api/v1/brands/{id}

GET    /inventory-service/api/v1/stock/product/{productId}
POST   /inventory-service/api/v1/stock/movements
```

### Billing Endpoints
```
GET    /billing-service/api/v1/invoices
POST   /billing-service/api/v1/invoices
GET    /billing-service/api/v1/invoices/{id}
PUT    /billing-service/api/v1/invoices/{id}
POST   /billing-service/api/v1/invoices/{id}/complete
POST   /billing-service/api/v1/invoices/hold
```

### Reports Endpoints
```
POST   /reports-service/api/v1/reports/generate
GET    /reports-service/api/v1/reports/types
```

### User Management Endpoints
```
GET    /auth-service/api/v1/users
POST   /auth-service/api/v1/users
GET    /auth-service/api/v1/users/{id}
PUT    /auth-service/api/v1/users/{id}
DELETE /auth-service/api/v1/users/{id}
POST   /auth-service/api/v1/users/{id}/suspend
POST   /auth-service/api/v1/users/{id}/activate
PUT    /auth-service/api/v1/users/{id}/role
GET    /auth-service/api/v1/users/me
```

## Environment Variables

### Required for Production
```bash
# JWT Configuration
JWT_SECRET=<generate-with: openssl rand -base64 64>
JWT_ACCESS_TOKEN_VALIDITY=3600000  # 1 hour
JWT_REFRESH_TOKEN_VALIDITY=604800000  # 7 days

# Database Configuration
DB_HOST=localhost
DB_PORT=3306
DB_NAME=easybilling
DB_USER=root
DB_PASSWORD=<secure-password>

# Redis Configuration
REDIS_HOST=localhost
REDIS_PORT=6379
REDIS_PASSWORD=<secure-password>

# Frontend URL for CORS
FRONTEND_URL=http://localhost:3000

# Service URLs (for inter-service communication)
AUTH_SERVICE_URL=http://localhost:8081/auth-service
TENANT_SERVICE_URL=http://localhost:8082/tenant-service
BILLING_SERVICE_URL=http://localhost:8083/billing-service
INVENTORY_SERVICE_URL=http://localhost:8084/inventory-service
CUSTOMER_SERVICE_URL=http://localhost:8085/customer-service
SUPPLIER_SERVICE_URL=http://localhost:8086/supplier-service
REPORTS_SERVICE_URL=http://localhost:8087/reports-service
```

## Starting Services

### Development (Local)
```bash
# Start MySQL and Redis
docker-compose up -d mysql redis

# Start each service
cd backend/services/gateway-service && ./gradlew bootRun
cd backend/services/auth-service && ./gradlew bootRun
cd backend/services/tenant-service && ./gradlew bootRun
cd backend/services/billing-service && ./gradlew bootRun
cd backend/services/inventory-service && ./gradlew bootRun
cd backend/services/customer-service && ./gradlew bootRun
cd backend/services/supplier-service && ./gradlew bootRun
cd backend/services/reports-service && ./gradlew bootRun

# Start frontend
cd frontend && npm run dev
```

### Production (Docker Compose)
```bash
docker-compose -f docker-compose.prod.yml up -d
```

## Health Check Endpoints

Each service exposes health endpoints:
```
GET /auth-service/actuator/health
GET /tenant-service/actuator/health
GET /billing-service/actuator/health
GET /inventory-service/actuator/health
GET /customer-service/actuator/health
GET /supplier-service/actuator/health
GET /reports-service/actuator/health
GET /gateway-service/actuator/health
```

## Swagger UI URLs

API documentation available at:
```
http://localhost:8081/auth-service/swagger-ui.html
http://localhost:8082/tenant-service/swagger-ui.html
http://localhost:8083/billing-service/swagger-ui.html
http://localhost:8084/inventory-service/swagger-ui.html
http://localhost:8085/customer-service/swagger-ui.html
http://localhost:8086/supplier-service/swagger-ui.html
http://localhost:8087/reports-service/swagger-ui.html
```

## Security Headers

All API requests should include:
```
Authorization: Bearer <jwt-token>
X-Tenant-Id: <tenant-id>
Content-Type: application/json
```

## Common Issues and Solutions

### Issue: Port already in use
```bash
# Find and kill process using port
lsof -i :8080
kill -9 <PID>
```

### Issue: Database connection refused
```bash
# Check MySQL is running
docker ps | grep mysql

# Test connection
mysql -h localhost -u root -p easybilling
```

### Issue: Redis connection refused
```bash
# Check Redis is running
docker ps | grep redis

# Test connection
redis-cli ping
```

### Issue: Frontend can't connect to backend
```bash
# Check NEXT_PUBLIC_API_URL in frontend/.env.local
echo $NEXT_PUBLIC_API_URL

# Should be: http://localhost:8080
```

### Issue: JWT token expired
```bash
# Use refresh token endpoint
POST /auth-service/api/v1/auth/refresh
{
  "refreshToken": "<your-refresh-token>"
}
```

## Troubleshooting Commands

```bash
# Check all service logs
docker-compose logs -f

# Check specific service
docker-compose logs -f auth-service

# Check service status
curl http://localhost:8081/auth-service/actuator/health

# Test authentication
curl -X POST http://localhost:8081/auth-service/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"admin"}'

# Test API with token
curl http://localhost:8085/customer-service/api/v1/customers \
  -H "Authorization: Bearer <token>" \
  -H "X-Tenant-Id: <tenant-id>"
```

## Changes Made in Security Audit

### Supplier Service
- ✅ Added context path: `/supplier-service`
- ✅ Changed port from 8085 to 8086 (resolved conflict)
- ✅ Added JWT configuration
- ✅ Added Flyway migrations config
- ✅ Added database connection pooling
- ✅ Added Actuator endpoints
- ✅ Added OpenAPI/Swagger configuration

### Reports Service
- ✅ Added context path: `/reports-service`
- ✅ Changed port to 8087
- ✅ Added JWT configuration
- ✅ Added database configuration
- ✅ Fixed inter-service URLs (now include context paths)
- ✅ Added Actuator endpoints
- ✅ Added OpenAPI/Swagger configuration

### Inventory Service
- ✅ Added JWT configuration

### Customer Service
- ✅ Added JWT configuration
- ✅ Fixed database username (was postgres, now root)

## Next Steps

1. **Before Production**:
   - [ ] Change JWT_SECRET to secure value
   - [ ] Update CORS configuration with production URLs
   - [ ] Disable Swagger UI in production
   - [ ] Configure SSL/TLS certificates
   - [ ] Set up monitoring and alerting
   - [ ] Enable database backups
   - [ ] Configure rate limiting

2. **Testing**:
   - [ ] Run security audit tests
   - [ ] Perform load testing
   - [ ] Test all API endpoints
   - [ ] Verify tenant isolation
   - [ ] Test token refresh flow

3. **Documentation**:
   - [x] Security audit complete
   - [x] API endpoints documented
   - [x] Configuration reference created
   - [ ] Deployment runbook
   - [ ] Incident response procedures
