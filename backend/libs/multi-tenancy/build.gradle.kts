plugins {
    id("java-library")
}

dependencies {
    api(project(":libs:common"))
    
    // Spring Boot
    api("org.springframework.boot:spring-boot-starter-web")
    api("org.springframework.boot:spring-boot-starter-data-jpa")
    
    // Database
    api("com.mysql:mysql-connector-j")
    api("com.zaxxer:HikariCP")
    
    // Flyway for migrations
    api("org.flywaydb:flyway-core")
    api("org.flywaydb:flyway-mysql")
}
