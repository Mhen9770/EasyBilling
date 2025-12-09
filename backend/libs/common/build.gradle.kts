plugins {
    id("java-library")
}

dependencies {
    // Spring Boot
    api("org.springframework.boot:spring-boot-starter")
    api("org.springframework.boot:spring-boot-starter-web")
    
    // Validation
    api("jakarta.validation:jakarta.validation-api")
    
    // Utilities
    api("org.apache.commons:commons-lang3:3.14.0")
    api("com.fasterxml.jackson.core:jackson-databind")
    api("com.fasterxml.jackson.datatype:jackson-datatype-jsr310")
}
