plugins {
    java
    id("org.springframework.boot") version "3.3.5"
    id("io.spring.dependency-management") version "1.1.6"
}

group = "ca.gbc"
version = "0.0.1-SNAPSHOT"

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(23)
    }
}

configurations {
    compileOnly {
        extendsFrom(configurations.annotationProcessor.get())
    }
}

repositories {
    mavenCentral()
    maven { url = uri("https://repo.spring.io/milestone") } // For Spring milestone dependencies
    maven { url = uri("https://repo.spring.io/snapshot") } // Add this for snapshot dependencies if needed
}


dependencies {
    // Spring Boot and Spring Cloud dependencies
    implementation("org.springframework.cloud:spring-cloud-starter-gateway-mvc")
    implementation("org.springframework.boot:spring-boot-starter-oauth2-resource-server")
    implementation("org.springframework.boot:spring-boot-starter-security")
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("org.springframework.cloud:spring-cloud-starter-circuitbreaker-resilience4j")

    // OpenAPI for documentation
    implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:2.6.0")
    testImplementation("org.springdoc:springdoc-openapi-starter-webmvc-api:2.6.0")

    // Lombok
    compileOnly("org.projectlombok:lombok:1.18.34")
    annotationProcessor("org.projectlombok:lombok:1.18.34")

    // Test dependencies
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")

    // Servlet API (if needed for specific servlet-related functionality)
    compileOnly("jakarta.servlet:jakarta.servlet-api:6.1.0")
}

dependencyManagement{
    imports{
        mavenBom("org.springframework.cloud:spring-cloud-dependencies:2023.0.3")
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
}
