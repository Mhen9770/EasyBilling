# EasyBilling

Modular Monolithic Billing & POS Application built with Spring Boot 3.4.0 and Java 17.

## Prerequisites

- Java 17 or higher
- Maven 3.9+ 
- MySQL 8.0+
- Redis (optional, for caching)

## Building the Project

```bash
cd easybilling
mvn clean install
```

## Running the Application

```bash
mvn spring-boot:run
```

Or run the JAR:

```bash
java -jar target/easybilling-1.0.0-SNAPSHOT.jar
```

## Configuration

Edit `src/main/resources/application.yml` to configure:

- Database connection (MySQL)
- Redis connection (optional)
- JWT secret key
- Server port (default: 8080)

## Environment Variables

- `DB_HOST` - Database host (default: localhost)
- `DB_PORT` - Database port (default: 3306)
- `DB_NAME` - Database name (default: easybilling)
- `DB_USER` - Database user (default: root)
- `DB_PASSWORD` - Database password (default: root)
- `REDIS_HOST` - Redis host (default: localhost)
- `REDIS_PORT` - Redis port (default: 6379)
- `JWT_SECRET` - JWT secret key (required in production)

## API Documentation

Once the application is running, access Swagger UI at:

```
http://localhost:8080/swagger-ui.html
```

## Project Structure

```
easybilling/
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/easybilling/
│   │   │       ├── config/          # Configuration classes
│   │   │       ├── controller/      # REST controllers
│   │   │       ├── service/         # Business logic
│   │   │       ├── repository/      # Data access layer
│   │   │       ├── entity/          # JPA entities
│   │   │       ├── dto/             # Data transfer objects
│   │   │       ├── exception/      # Custom exceptions
│   │   │       └── EasyBillingApplication.java
│   │   └── resources/
│   │       ├── application.yml      # Application configuration
│   │       └── db/migration/       # Flyway migrations
│   └── test/                        # Test classes
└── pom.xml                          # Maven configuration
```

## Features

- **Multi-tenancy**: Schema-per-tenant strategy
- **Authentication**: JWT-based authentication
- **Billing & POS**: Invoice creation, payments, returns
- **Inventory Management**: Products, stock, movements
- **Customer Management**: Customer profiles, loyalty points, wallet
- **Reports**: Sales reports, inventory reports
- **Swagger UI**: API documentation

## License

Copyright (c) 2025 EasyBilling

