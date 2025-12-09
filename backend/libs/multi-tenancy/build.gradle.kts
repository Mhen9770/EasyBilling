plugins {
    id("java-library")
}

dependencies {
    api(project(":libs:common"))
    
    // Spring Boot
    api("org.springframework.boot:spring-boot-starter-web")
    api("org.springframework.boot:spring-boot-starter-data-jpa")
    
    // Database
    api("org.postgresql:postgresql:42.7.1")
    api("com.zaxxer:HikariCP")
    
    // Flyway for migrations
    api("org.flywaydb:flyway-core")
    api("org.flywaydb:flyway-database-postgresql")
}
