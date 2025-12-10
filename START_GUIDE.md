# EasyBilling - How to Start All Services

This guide provides multiple ways to start all EasyBilling services.

## 🚀 Quick Start (Recommended - Docker Compose)

The easiest way to start everything is using Docker Compose:

```bash
# From the project root directory
docker-compose up -d
```

This will start:
- ✅ PostgreSQL database (port 5432)
- ✅ Redis cache (port 6379)
- ✅ Gateway Service (port 8080)
- ✅ Auth Service (port 8081)
- ✅ Tenant Service (port 8082)
- ✅ Billing Service (port 8083)
- ✅ Inventory Service (port 8084)
- ✅ Frontend (port 3000)

### Check Service Status

```bash
# View all running containers
docker-compose ps

# View logs for all services
docker-compose logs -f

# View logs for a specific service
docker-compose logs -f gateway-service
docker-compose logs -f auth-service
docker-compose logs -f billing-service
docker-compose logs -f inventory-service
```

### Stop All Services

```bash
docker-compose down

# To also remove volumes (clears database data)
docker-compose down -v
```

---

## 🛠️ Local Development Setup

For development, you may want to run services individually:

### Step 1: Start Infrastructure Services

```bash
# Start only PostgreSQL and Redis
docker-compose up -d postgres redis

# Verify they're running
docker-compose ps
```

### Step 2: Start Backend Services

Open multiple terminal windows/tabs and run each service:

#### Terminal 1 - Gateway Service
```bash
cd backend
.\gradlew.bat :services:gateway-service:bootRun
```
Gateway will start on: **http://localhost:8080**

#### Terminal 2 - Auth Service
```bash
cd backend
.\gradlew.bat :services:auth-service:bootRun
```
Auth Service will start on: **http://localhost:8081**

#### Terminal 3 - Tenant Service
```bash
cd backend
.\gradlew.bat :services:tenant-service:bootRun
```
Tenant Service will start on: **http://localhost:8082**

#### Terminal 4 - Billing Service
```bash
cd backend
.\gradlew.bat :services:billing-service:bootRun
```
Billing Service will start on: **http://localhost:8083**

#### Terminal 5 - Inventory Service
```bash
cd backend
.\gradlew.bat :services:inventory-service:bootRun
```
Inventory Service will start on: **http://localhost:8084**

### Step 3: Start Frontend

```bash
cd frontend
npm install
npm run dev
```
Frontend will start on: **http://localhost:3000**

---

## 📋 Service URLs & Endpoints

Once all services are running, you can access:

| Service | URL | Description |
|---------|-----|-------------|
| **Frontend** | http://localhost:3000 | Web application |
| **Gateway** | http://localhost:8080 | API Gateway (entry point) |
| **Auth Service** | http://localhost:8081/auth-service | Authentication APIs |
| **Tenant Service** | http://localhost:8082/tenant-service | Tenant management APIs |
| **Billing Service** | http://localhost:8083/billing-service | Billing & POS APIs |
| **Inventory Service** | http://localhost:8084/inventory-service | Inventory & Product APIs |

### API Documentation (Swagger)

Each service has Swagger UI available at:
- Gateway: http://localhost:8080/swagger-ui.html
- Auth Service: http://localhost:8081/auth-service/swagger-ui.html
- Tenant Service: http://localhost:8082/tenant-service/swagger-ui.html
- Billing Service: http://localhost:8083/billing-service/swagger-ui.html
- Inventory Service: http://localhost:8084/inventory-service/swagger-ui.html

### Health Checks

Check service health:
```bash
# Gateway
curl http://localhost:8080/actuator/health

# Auth Service
curl http://localhost:8081/auth-service/actuator/health

# Tenant Service
curl http://localhost:8082/tenant-service/actuator/health

# Billing Service
curl http://localhost:8083/billing-service/actuator/health

# Inventory Service
curl http://localhost:8084/inventory-service/actuator/health
```

---

## 🔧 Configuration

### Backend Configuration

Each service has its own `application.yml` file. For local development, you can create `application-local.yml`:

**Example for any service:**
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
```

Run with local profile:
```bash
.\gradlew.bat :services:tenant-service:bootRun --args='--spring.profiles.active=local'
```

### Frontend Configuration

Create `frontend/.env.local`:
```bash
NEXT_PUBLIC_API_URL=http://localhost:8080
NEXT_PUBLIC_APP_NAME=EasyBilling
```

---

## 🐛 Troubleshooting

### Port Already in Use

If a port is already in use:

**Windows:**
```powershell
# Find process using port
netstat -ano | findstr :8080

# Kill the process (replace PID with actual process ID)
taskkill /PID <PID> /F
```

**Linux/Mac:**
```bash
# Find process using port
lsof -i :8080

# Kill the process
kill -9 <PID>
```

### Database Connection Issues

1. Ensure PostgreSQL is running:
   ```bash
   docker-compose ps postgres
   ```

2. Check PostgreSQL logs:
   ```bash
   docker-compose logs postgres
   ```

3. Test connection:
   ```bash
   docker exec -it easybilling-postgres psql -U easybilling -d easybilling
   ```

### Service Won't Start

1. Check if dependencies are built:
   ```bash
   cd backend
   .\gradlew.bat clean build
   ```

2. Check service logs:
   ```bash
   # For Docker
   docker-compose logs <service-name>
   
   # For Gradle
   # Check the terminal output where you ran bootRun
   ```

### Frontend Build Issues

```bash
cd frontend
rm -rf node_modules package-lock.json
npm install
npm run dev
```

---

## 🎯 Quick Commands Reference

### Docker Compose

```bash
# Start all services
docker-compose up -d

# Stop all services
docker-compose down

# Restart a specific service
docker-compose restart gateway-service

# View logs
docker-compose logs -f

# Rebuild and start
docker-compose up -d --build

# Stop and remove everything including volumes
docker-compose down -v
```

### Gradle (Backend)

```bash
# Build all services
.\gradlew.bat build

# Run a specific service
.\gradlew.bat :services:gateway-service:bootRun
.\gradlew.bat :services:auth-service:bootRun
.\gradlew.bat :services:tenant-service:bootRun
.\gradlew.bat :services:billing-service:bootRun
.\gradlew.bat :services:inventory-service:bootRun

# Clean build
.\gradlew.bat clean build

# Run tests
.\gradlew.bat test
```

### Frontend

```bash
# Install dependencies
npm install

# Start development server
npm run dev

# Build for production
npm run build

# Start production server
npm start
```

---

## ✅ Verification Checklist

After starting all services, verify:

- [ ] PostgreSQL is running (port 5432)
- [ ] Redis is running (port 6379)
- [ ] Gateway Service is accessible (http://localhost:8080/actuator/health)
- [ ] Auth Service is accessible (http://localhost:8081/auth-service/actuator/health)
- [ ] Tenant Service is accessible (http://localhost:8082/tenant-service/actuator/health)
- [ ] Billing Service is accessible (http://localhost:8083/billing-service/actuator/health)
- [ ] Inventory Service is accessible (http://localhost:8084/inventory-service/actuator/health)
- [ ] Frontend is accessible (http://localhost:3000)
- [ ] Swagger UI is accessible for each service

---

## 🎉 Next Steps

1. **Create a Tenant**: Use the Tenant Service API to create your first tenant
2. **Explore APIs**: Check out Swagger documentation for each service
3. **Access Frontend**: Open http://localhost:3000 in your browser
4. **Read Documentation**: Check the `docs/` folder for more details

---

**Happy Coding! 🚀**

