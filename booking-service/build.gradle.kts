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
dependencyManagement{
	imports{
		mavenBom("org.springframework.cloud:spring-cloud-dependencies:2023.0.3")
	}
}


repositories {
	mavenCentral()
}

dependencies {
	implementation("org.springframework.boot:spring-boot-starter-actuator")
	implementation("org.springframework.boot:spring-boot-starter-data-mongodb")
	implementation("org.springframework.boot:spring-boot-starter-web")
	implementation("org.springframework.cloud:spring-cloud-contract-stub-runner")
	implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:2.6.0")
	testImplementation("org.springdoc:springdoc-openapi-starter-webmvc-api:2.6.0")
	implementation("org.springframework.cloud:spring-cloud-starter-circuitbreaker-resilience4j")
	implementation("org.springframework.cloud:spring-cloud-starter-contract-stub-runner")

	implementation("org.springframework.kafka:spring-kafka:3.3.0") //WK13 Lab
	testImplementation("org.springframework.kafka:spring-kafka-test:3.3.0") //wk13 lab
	testImplementation("org.testcontainers:kafka:1.20.4") //WK13 lab

	compileOnly("org.projectlombok:lombok")
	developmentOnly("org.springframework.boot:spring-boot-devtools")
	annotationProcessor("org.projectlombok:lombok")
	testImplementation("org.springframework.boot:spring-boot-starter-test")
	testImplementation("org.springframework.boot:spring-boot-testcontainers")
	testImplementation("org.testcontainers:junit-jupiter")
	testImplementation("io.rest-assured:rest-assured")
	testImplementation("org.testcontainers:mongodb")
	testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks.withType<Test> {
	useJUnitPlatform()
}
