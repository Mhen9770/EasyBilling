plugins {
    id("org.springframework.boot")
    id("io.spring.dependency-management")
    java
}

group = "com.easybilling"
version = "1.0.0"

dependencies {
    // Spring Cloud Gateway
    implementation("org.springframework.cloud:spring-cloud-starter-gateway")
    
    // Spring Boot
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("org.springframework.boot:spring-boot-starter-data-redis-reactive")
    
    // Circuit Breaker
    implementation("org.springframework.cloud:spring-cloud-starter-circuitbreaker-reactor-resilience4j")
    
    // JWT
    implementation("io.jsonwebtoken:jjwt-api:0.12.3")
    runtimeOnly("io.jsonwebtoken:jjwt-impl:0.12.3")
    runtimeOnly("io.jsonwebtoken:jjwt-jackson:0.12.3")
    
    // Monitoring
    implementation("io.micrometer:micrometer-registry-prometheus")
    
    // Lombok
    compileOnly("org.projectlombok:lombok")
    annotationProcessor("org.projectlombok:lombok")
    
    // OpenAPI
    implementation("org.springdoc:springdoc-openapi-starter-webflux-ui:2.3.0")
    
    // Internal dependencies
    implementation(project(":libs:common"))
    implementation(project(":libs:security"))
}

dependencyManagement {
    imports {
        mavenBom("org.springframework.cloud:spring-cloud-dependencies:2023.0.0")
    }
}

tasks.bootJar {
    archiveFileName.set("gateway-service.jar")
}
