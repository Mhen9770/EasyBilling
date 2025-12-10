# EasyBilling - Deployment Guide

## Overview
This guide covers deploying the EasyBilling platform to production environments. The application consists of multiple microservices (backend) and a Next.js frontend.

## Architecture

```
┌─────────────────┐
│   Load Balancer │
└────────┬────────┘
         │
    ┌────┴─────┐
    │          │
┌───▼──┐   ┌──▼────┐
│Frontend│ │Gateway│
│Next.js │ │ :8080 │
│  :3000 │ └───┬───┘
└────────┘     │
           ┌───┴────────────────────┬──────────┬────────────┐
       ┌───▼───┐  ┌────▼────┐  ┌───▼────┐  ┌─▼──────┐  ┌─▼────────┐
       │ Auth  │  │Billing  │  │Inventory│ │Customer│  │Supplier  │
       │ :8081 │  │ :8083   │  │  :8084  │ │ :8085  │  │  :8086   │
       └───┬───┘  └────┬────┘  └────┬────┘ └────┬───┘  └────┬─────┘
           │           │            │            │            │
           └───────────┴────────────┴────────────┴────────────┘
                                    │
                        ┌───────────┴──────────┐
                    ┌───▼───┐            ┌────▼────┐
                    │ MySQL │            │  Redis  │
                    │ :3306 │            │  :6379  │
                    └───────┘            └─────────┘
```

## Prerequisites

### Required Software
- **Docker** (20.10+) and **Docker Compose** (2.0+)
- **Node.js** (18+ for local development)
- **Java** (17+ for local development)
- **MySQL** (8.0+)
- **Redis** (7.0+)

### Hardware Requirements

**Minimum (Development)**:
- CPU: 4 cores
- RAM: 8 GB
- Disk: 20 GB

**Recommended (Production)**:
- CPU: 8+ cores
- RAM: 16+ GB
- Disk: 100+ GB SSD

## Environment Configuration

### Frontend Environment Variables

Create `/frontend/.env.production`:
```bash
# API Configuration
NEXT_PUBLIC_API_URL=https://api.yourdomain.com

# App Configuration
NEXT_PUBLIC_APP_NAME=EasyBilling
NODE_ENV=production

# Optional: Analytics
NEXT_PUBLIC_GOOGLE_ANALYTICS_ID=UA-XXXXXXXXX-X
```

### Backend Environment Variables

Each service needs environment variables. Create a `.env` file in the project root:

```bash
# Database Configuration
MYSQL_HOST=mysql
MYSQL_PORT=3306
MYSQL_DATABASE=easybilling
MYSQL_USER=easybilling_user
MYSQL_PASSWORD=<strong-password>

# Redis Configuration
REDIS_HOST=redis
REDIS_PORT=6379
REDIS_PASSWORD=<strong-password>

# JWT Configuration
JWT_SECRET=<generate-strong-secret>
JWT_EXPIRATION=86400000

# Service Ports
GATEWAY_PORT=8080
AUTH_SERVICE_PORT=8081
TENANT_SERVICE_PORT=8082
BILLING_SERVICE_PORT=8083
INVENTORY_SERVICE_PORT=8084
CUSTOMER_SERVICE_PORT=8085
SUPPLIER_SERVICE_PORT=8086

# Spring Profiles
SPRING_PROFILES_ACTIVE=prod
```

## Deployment Options

### Option 1: Docker Compose (Recommended for Small-Medium Scale)

#### Step 1: Prepare Production Docker Compose

Create `docker-compose.prod.yml`:

```yaml
version: '3.8'

services:
  # MySQL Database
  mysql:
    image: mysql:8.0
    container_name: easybilling-mysql
    environment:
      MYSQL_ROOT_PASSWORD: ${MYSQL_ROOT_PASSWORD}
      MYSQL_DATABASE: ${MYSQL_DATABASE}
      MYSQL_USER: ${MYSQL_USER}
      MYSQL_PASSWORD: ${MYSQL_PASSWORD}
    volumes:
      - mysql-data:/var/lib/mysql
    networks:
      - easybilling-network
    restart: always
    healthcheck:
      test: ["CMD", "mysqladmin", "ping", "-h", "localhost"]
      interval: 10s
      timeout: 5s
      retries: 5

  # Redis Cache
  redis:
    image: redis:7-alpine
    container_name: easybilling-redis
    command: redis-server --requirepass ${REDIS_PASSWORD}
    volumes:
      - redis-data:/data
    networks:
      - easybilling-network
    restart: always

  # Gateway Service
  gateway-service:
    build:
      context: ./backend/services/gateway-service
      dockerfile: Dockerfile.prod
    container_name: easybilling-gateway
    environment:
      SPRING_PROFILES_ACTIVE: prod
      MYSQL_HOST: mysql
      REDIS_HOST: redis
    ports:
      - "${GATEWAY_PORT}:8080"
    depends_on:
      - mysql
      - redis
    networks:
      - easybilling-network
    restart: always

  # Frontend
  frontend:
    build:
      context: ./frontend
      dockerfile: Dockerfile.prod
      args:
        NEXT_PUBLIC_API_URL: ${NEXT_PUBLIC_API_URL}
    container_name: easybilling-frontend
    environment:
      NODE_ENV: production
    ports:
      - "3000:3000"
    depends_on:
      - gateway-service
    networks:
      - easybilling-network
    restart: always

volumes:
  mysql-data:
  redis-data:

networks:
  easybilling-network:
    driver: bridge
```

#### Step 2: Build and Deploy

```bash
# Clone repository
git clone <repository-url>
cd EasyBilling

# Create environment file
cp .env.example .env
# Edit .env with production values

# Build images
docker-compose -f docker-compose.prod.yml build

# Start services
docker-compose -f docker-compose.prod.yml up -d

# Check logs
docker-compose -f docker-compose.prod.yml logs -f

# Verify health
docker-compose -f docker-compose.prod.yml ps
```

#### Step 3: Initialize Database

```bash
# Run migrations
docker-compose -f docker-compose.prod.yml exec gateway-service \
  java -jar /app/migrations/run-migrations.jar

# Create default admin user
docker-compose -f docker-compose.prod.yml exec auth-service \
  java -jar /app/scripts/create-admin.jar \
  --username admin \
  --password <secure-password> \
  --email admin@yourdomain.com
```

### Option 2: Kubernetes (For Large Scale)

#### Prerequisites
- Kubernetes cluster (1.21+)
- kubectl configured
- Helm (3.0+)

#### Helm Chart Structure

```
helm/
├── Chart.yaml
├── values.yaml
├── templates/
│   ├── deployment-frontend.yaml
│   ├── deployment-gateway.yaml
│   ├── deployment-auth.yaml
│   ├── service-frontend.yaml
│   ├── service-gateway.yaml
│   ├── ingress.yaml
│   ├── configmap.yaml
│   └── secrets.yaml
```

#### Deploy with Helm

```bash
# Add Helm repository
helm repo add easybilling ./helm

# Install or upgrade
helm upgrade --install easybilling ./helm \
  --namespace production \
  --create-namespace \
  --values ./helm/values.prod.yaml

# Check status
kubectl get pods -n production
kubectl get svc -n production
```

### Option 3: Cloud-Specific Deployments

#### AWS (Elastic Beanstalk + RDS)

```bash
# Install EB CLI
pip install awsebcli

# Initialize
eb init -p docker easybilling

# Create environment
eb create production \
  --database.engine mysql \
  --database.version 8.0 \
  --envvars MYSQL_HOST=<rds-endpoint>

# Deploy
eb deploy production
```

#### Google Cloud (Cloud Run + Cloud SQL)

```bash
# Build and push images
gcloud builds submit --tag gcr.io/PROJECT_ID/easybilling-frontend frontend/
gcloud builds submit --tag gcr.io/PROJECT_ID/easybilling-gateway backend/services/gateway-service/

# Deploy
gcloud run deploy easybilling-frontend \
  --image gcr.io/PROJECT_ID/easybilling-frontend \
  --platform managed \
  --region us-central1

gcloud run deploy easybilling-gateway \
  --image gcr.io/PROJECT_ID/easybilling-gateway \
  --platform managed \
  --region us-central1
```

#### Azure (App Service + Azure Database)

```bash
# Create resource group
az group create --name easybilling-rg --location eastus

# Create container registry
az acr create --resource-group easybilling-rg \
  --name easybillingacr --sku Basic

# Build and push
az acr build --registry easybillingacr \
  --image easybilling-frontend:latest frontend/

# Create app service
az appservice plan create --name easybilling-plan \
  --resource-group easybilling-rg --is-linux

az webapp create --resource-group easybilling-rg \
  --plan easybilling-plan --name easybilling-app \
  --deployment-container-image-name easybillingacr.azurecr.io/easybilling-frontend:latest
```

## SSL/TLS Configuration

### Using Let's Encrypt (Recommended)

```bash
# Install certbot
sudo apt-get install certbot

# Generate certificate
sudo certbot certonly --standalone \
  -d yourdomain.com \
  -d api.yourdomain.com

# Configure Nginx
# /etc/nginx/sites-available/easybilling
server {
    listen 443 ssl http2;
    server_name yourdomain.com;

    ssl_certificate /etc/letsencrypt/live/yourdomain.com/fullchain.pem;
    ssl_certificate_key /etc/letsencrypt/live/yourdomain.com/privkey.pem;

    location / {
        proxy_pass http://localhost:3000;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
    }
}

server {
    listen 443 ssl http2;
    server_name api.yourdomain.com;

    ssl_certificate /etc/letsencrypt/live/api.yourdomain.com/fullchain.pem;
    ssl_certificate_key /etc/letsencrypt/live/api.yourdomain.com/privkey.pem;

    location / {
        proxy_pass http://localhost:8080;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
    }
}

# Reload Nginx
sudo systemctl reload nginx
```

## Monitoring and Logging

### Prometheus + Grafana

```yaml
# docker-compose.monitoring.yml
version: '3.8'

services:
  prometheus:
    image: prom/prometheus
    volumes:
      - ./prometheus.yml:/etc/prometheus/prometheus.yml
      - prometheus-data:/prometheus
    ports:
      - "9090:9090"
    networks:
      - easybilling-network

  grafana:
    image: grafana/grafana
    ports:
      - "3001:3000"
    volumes:
      - grafana-data:/var/lib/grafana
    networks:
      - easybilling-network
    environment:
      - GF_SECURITY_ADMIN_PASSWORD=admin

volumes:
  prometheus-data:
  grafana-data:
```

### ELK Stack (Elasticsearch, Logstash, Kibana)

```yaml
# docker-compose.elk.yml
version: '3.8'

services:
  elasticsearch:
    image: docker.elastic.co/elasticsearch/elasticsearch:8.5.0
    environment:
      - discovery.type=single-node
    volumes:
      - elasticsearch-data:/usr/share/elasticsearch/data
    ports:
      - "9200:9200"

  logstash:
    image: docker.elastic.co/logstash/logstash:8.5.0
    volumes:
      - ./logstash.conf:/usr/share/logstash/pipeline/logstash.conf
    ports:
      - "5000:5000"

  kibana:
    image: docker.elastic.co/kibana/kibana:8.5.0
    ports:
      - "5601:5601"
    depends_on:
      - elasticsearch

volumes:
  elasticsearch-data:
```

## Backup and Recovery

### Database Backup

```bash
# Automated daily backup script
#!/bin/bash
# /opt/easybilling/backup.sh

BACKUP_DIR="/backups/easybilling"
DATE=$(date +%Y%m%d_%H%M%S)

# MySQL backup
docker exec easybilling-mysql mysqldump \
  -u root -p${MYSQL_ROOT_PASSWORD} \
  easybilling > ${BACKUP_DIR}/mysql_${DATE}.sql

# Compress
gzip ${BACKUP_DIR}/mysql_${DATE}.sql

# Keep only last 30 days
find ${BACKUP_DIR} -name "mysql_*.sql.gz" -mtime +30 -delete

# Upload to S3 (optional)
aws s3 cp ${BACKUP_DIR}/mysql_${DATE}.sql.gz \
  s3://easybilling-backups/mysql/
```

Add to crontab:
```bash
0 2 * * * /opt/easybilling/backup.sh
```

### Restore from Backup

```bash
# Download from S3
aws s3 cp s3://easybilling-backups/mysql/mysql_20241210.sql.gz .

# Decompress
gunzip mysql_20241210.sql.gz

# Restore
docker exec -i easybilling-mysql mysql \
  -u root -p${MYSQL_ROOT_PASSWORD} \
  easybilling < mysql_20241210.sql
```

## Performance Optimization

### Frontend Optimization
```javascript
// next.config.js
module.exports = {
  compress: true,
  images: {
    domains: ['your-cdn.com'],
    formats: ['image/webp'],
  },
  experimental: {
    optimizeCss: true,
    optimizePackageImports: ['@tanstack/react-query'],
  },
}
```

### Backend Optimization
```yaml
# application-prod.yml
spring:
  datasource:
    hikari:
      maximum-pool-size: 20
      minimum-idle: 5
  jpa:
    hibernate:
      ddl-auto: none
    show-sql: false
  cache:
    type: redis
```

### Database Optimization
```sql
-- Add indexes
CREATE INDEX idx_customer_phone ON customers(phone);
CREATE INDEX idx_invoice_date ON invoices(created_at);
CREATE INDEX idx_product_barcode ON products(barcode);

-- Optimize queries
ANALYZE TABLE customers, invoices, products;
```

## Security Checklist

- [ ] Change all default passwords
- [ ] Enable SSL/TLS for all services
- [ ] Configure firewall (UFW/iptables)
- [ ] Enable rate limiting on API Gateway
- [ ] Set up fail2ban for brute force protection
- [ ] Configure CORS properly
- [ ] Enable audit logging
- [ ] Regular security updates
- [ ] Use secrets management (AWS Secrets Manager, Vault)
- [ ] Enable database encryption at rest

## Scaling Considerations

### Horizontal Scaling
```yaml
# docker-compose.scale.yml
services:
  gateway-service:
    deploy:
      replicas: 3
      update_config:
        parallelism: 1
        delay: 10s
      restart_policy:
        condition: on-failure
```

### Load Balancing
```nginx
# nginx.conf
upstream backend {
    least_conn;
    server gateway1:8080;
    server gateway2:8080;
    server gateway3:8080;
}

server {
    location / {
        proxy_pass http://backend;
    }
}
```

## Troubleshooting

### Common Issues

**Issue**: Frontend can't connect to backend
```bash
# Check network
docker network ls
docker network inspect easybilling-network

# Check environment variables
docker exec easybilling-frontend env | grep API_URL
```

**Issue**: Database connection failed
```bash
# Check MySQL logs
docker logs easybilling-mysql

# Test connection
docker exec easybilling-mysql mysql \
  -u ${MYSQL_USER} -p${MYSQL_PASSWORD} \
  -e "SELECT 1"
```

**Issue**: High memory usage
```bash
# Check resource usage
docker stats

# Adjust Java heap
# Add to service environment:
JAVA_OPTS: "-Xmx512m -Xms256m"
```

## CI/CD Pipeline

### GitHub Actions Example

```yaml
# .github/workflows/deploy.yml
name: Deploy to Production

on:
  push:
    branches: [main]

jobs:
  deploy:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v2
      
      - name: Build and Push Docker Images
        run: |
          echo ${{ secrets.DOCKER_PASSWORD }} | docker login -u ${{ secrets.DOCKER_USERNAME }} --password-stdin
          docker-compose -f docker-compose.prod.yml build
          docker-compose -f docker-compose.prod.yml push
      
      - name: Deploy to Server
        uses: appleboy/ssh-action@master
        with:
          host: ${{ secrets.HOST }}
          username: ${{ secrets.USERNAME }}
          key: ${{ secrets.SSH_KEY }}
          script: |
            cd /opt/easybilling
            docker-compose -f docker-compose.prod.yml pull
            docker-compose -f docker-compose.prod.yml up -d
```

## Health Checks

```bash
# Check all services
curl http://localhost:8080/actuator/health

# Check specific service
curl http://localhost:8081/actuator/health

# Frontend health
curl http://localhost:3000/api/health
```

## Support and Maintenance

- Monitor logs daily: `docker-compose logs -f --tail=100`
- Review metrics weekly in Grafana
- Update dependencies monthly
- Full backup verification quarterly
- Disaster recovery drill annually

## Conclusion

This deployment guide covers the essential steps to get EasyBilling running in production. Adjust configurations based on your specific infrastructure and scale requirements.

For support: create an issue on GitHub or contact the development team.
