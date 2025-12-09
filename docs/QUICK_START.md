# Quick Start Guide - EasyBilling Platform

This guide will help you get the EasyBilling platform up and running in under 10 minutes.

## Prerequisites

Before you begin, ensure you have the following installed:

- **Java 17 or higher** - [Download](https://adoptium.net/)
- **Node.js 20 or higher** - [Download](https://nodejs.org/)
- **Docker & Docker Compose** - [Download](https://docs.docker.com/get-docker/)
- **Git** - [Download](https://git-scm.com/downloads)

## Step 1: Clone the Repository

```bash
git clone https://github.com/Mhen9770/EasyBilling.git
cd EasyBilling
```

## Step 2: Start Infrastructure Services

Start PostgreSQL and Redis using Docker Compose:

```bash
docker-compose up -d postgres redis
```

Verify services are running:

```bash
docker-compose ps
```

You should see both `easybilling-postgres` and `easybilling-redis` running.

## Step 3: Start Backend Services

### Option A: Using Gradle (Development)

```bash
cd backend
./gradlew :services:tenant-service:bootRun
```

The Tenant Service will start on `http://localhost:8082`

### Option B: Using Docker

```bash
cd backend
docker build -t easybilling/tenant-service:latest -f services/tenant-service/Dockerfile .
docker run -p 8082:8082 --network easybilling-network easybilling/tenant-service:latest
```

## Step 4: Start Frontend

### Option A: Using npm (Development)

```bash
cd frontend
npm install
npm run dev
```

The frontend will start on `http://localhost:3000`

### Option B: Using Docker

```bash
cd frontend
docker build -t easybilling/frontend:latest .
docker run -p 3000:3000 easybilling/frontend:latest
```

## Step 5: Verify Installation

1. **Frontend**: Open http://localhost:3000 in your browser
   - You should see the EasyBilling landing page

2. **Backend API**: Open http://localhost:8082/tenant-service/swagger-ui.html
   - You should see the Swagger API documentation

3. **Health Check**: 
   ```bash
   curl http://localhost:8082/tenant-service/actuator/health
   ```
   Should return: `{"status":"UP"}`

## Step 6: Create Your First Tenant

### Using Swagger UI

1. Navigate to http://localhost:8082/tenant-service/swagger-ui.html
2. Find the `POST /api/v1/tenants` endpoint
3. Click "Try it out"
4. Use this example request:

```json
{
  "name": "My Test Store",
  "slug": "test-store",
  "description": "My first test store",
  "plan": "BASIC",
  "contactEmail": "test@example.com",
  "contactPhone": "+1234567890",
  "address": "123 Main St",
  "city": "New York",
  "state": "NY",
  "country": "USA",
  "postalCode": "10001",
  "taxNumber": "12-3456789"
}
```

5. Click "Execute"
6. You should receive a 201 Created response with tenant details

### Using cURL

```bash
curl -X POST http://localhost:8082/tenant-service/api/v1/tenants \
  -H "Content-Type: application/json" \
  -d '{
    "name": "My Test Store",
    "slug": "test-store",
    "description": "My first test store",
    "plan": "BASIC",
    "contactEmail": "test@example.com",
    "contactPhone": "+1234567890"
  }'
```

## Step 7: Test Tenant Access

The system automatically creates a schema for your new tenant. You can verify it:

```bash
# Connect to PostgreSQL
docker exec -it easybilling-postgres psql -U easybilling -d easybilling

# List all schemas
\dn

# You should see 'tenant_test-store' in the list
```

## Common Operations

### View All Tenants

```bash
curl http://localhost:8082/tenant-service/api/v1/tenants
```

### Get Tenant by ID

```bash
curl http://localhost:8082/tenant-service/api/v1/tenants/{tenant-id}
```

### Activate Tenant

```bash
curl -X POST http://localhost:8082/tenant-service/api/v1/tenants/{tenant-id}/activate
```

### Suspend Tenant

```bash
curl -X POST http://localhost:8082/tenant-service/api/v1/tenants/{tenant-id}/suspend
```

## Environment Configuration

### Backend Configuration

Create `backend/services/tenant-service/src/main/resources/application-local.yml`:

```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/easybilling
    username: easybilling
    password: easybilling123
  
  data:
    redis:
      host: localhost
      port: 6379

logging:
  level:
    com.easybilling: DEBUG
```

Run with local profile:
```bash
./gradlew :services:tenant-service:bootRun --args='--spring.profiles.active=local'
```

### Frontend Configuration

Create `frontend/.env.local`:

```bash
NEXT_PUBLIC_API_URL=http://localhost:8082
NEXT_PUBLIC_APP_NAME=EasyBilling
```

## Troubleshooting

### Backend won't start

**Problem**: "Connection refused" or database errors

**Solution**: 
1. Ensure PostgreSQL is running: `docker-compose ps`
2. Check logs: `docker-compose logs postgres`
3. Restart services: `docker-compose restart postgres`

### Frontend build fails

**Problem**: Module not found errors

**Solution**:
```bash
cd frontend
rm -rf node_modules package-lock.json
npm install
npm run build
```

### Port already in use

**Problem**: "Port 8082 already in use"

**Solution**:
```bash
# Find process using port
lsof -i :8082
# Kill the process
kill -9 <PID>
```

## Next Steps

1. **Explore the API**: Browse the Swagger documentation at http://localhost:8082/swagger-ui.html
2. **Review Architecture**: Read [ARCHITECTURE.md](./ARCHITECTURE.md)
3. **Implement Auth Service**: Follow the development plan to add authentication
4. **Build Admin UI**: Create admin portal for tenant management

## Getting Help

- **Documentation**: Check the [docs](./docs/) folder
- **Issues**: Report bugs on [GitHub Issues](https://github.com/Mhen9770/EasyBilling/issues)
- **Development Plan**: See [DevelopmentPlan.md](../DevelopmentPlan.md) for roadmap

## Development Tips

### Hot Reload

**Backend**: Spring Boot DevTools is included
```bash
./gradlew :services:tenant-service:bootRun
# Changes to Java files will auto-reload
```

**Frontend**: Next.js has built-in hot reload
```bash
npm run dev
# Changes to React files will auto-reload
```

### Debugging

**Backend**: Add to your IDE run configuration:
```
-Dspring-boot.run.jvmArguments="-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=5005"
```

**Frontend**: Use Chrome DevTools or VS Code debugger

### Database Inspection

**Using psql**:
```bash
docker exec -it easybilling-postgres psql -U easybilling -d easybilling
```

**Using pgAdmin**: Connect to localhost:5432 with credentials from docker-compose.yml

## Production Deployment

For production deployment guide, see [DEPLOYMENT.md](./deployment/README.md)

---

**Congratulations!** 🎉 You now have EasyBilling running locally. Start building your retail empire!
