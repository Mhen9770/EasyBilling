plugins {
    id("java-library")
}

dependencies {
    api(project(":libs:common"))
    
    // Spring Security
    api("org.springframework.boot:spring-boot-starter-security")
    api("org.springframework.security:spring-security-oauth2-jose")
    api("org.springframework.security:spring-security-oauth2-resource-server")
    
    // JWT
    api("io.jsonwebtoken:jjwt-api:0.12.3")
    runtimeOnly("io.jsonwebtoken:jjwt-impl:0.12.3")
    runtimeOnly("io.jsonwebtoken:jjwt-jackson:0.12.3")
}
