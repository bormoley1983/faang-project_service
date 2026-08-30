plugins {
    java
    id("org.springframework.boot") version "4.1.1"
    id("io.spring.dependency-management") version "1.1.7"
    id("jacoco")
}

group = "faang.school"
version = "1.0"

val javaVersion = 25
val springCloudVersion = "2025.1.3"
val testcontainersVersion = "2.0.5"
val mapstructVersion = "1.6.3"

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(javaVersion)
    }
}

repositories {
    mavenCentral()
    maven {
        name = "atlassian-public"
        url = uri("https://packages.atlassian.com/maven/repository/public")
    }
}

dependencyManagement {
    imports {
        mavenBom("org.springframework.cloud:spring-cloud-dependencies:$springCloudVersion")
        mavenBom("org.testcontainers:testcontainers-bom:$testcontainersVersion")
    }
}

dependencies {
    /**
     * Spring boot starters
     */
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("org.springframework.boot:spring-boot-starter-freemarker")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("org.springframework.cloud:spring-cloud-starter-openfeign")
    annotationProcessor("org.springframework.boot:spring-boot-configuration-processor")
    
    /**
     * Database
     */
    implementation("org.liquibase:liquibase-core")
    implementation("redis.clients:jedis")
    runtimeOnly("org.postgresql:postgresql")

    // Swagger
    implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:3.1.0")

    /**
     * Utils & Logging
     */
    implementation("org.projectlombok:lombok")
    annotationProcessor("org.projectlombok:lombok")

    implementation("org.mapstruct:mapstruct:$mapstructVersion")
    annotationProcessor("org.mapstruct:mapstruct-processor:$mapstructVersion")

    implementation("com.fasterxml.jackson.core:jackson-databind")

    /**
     * Amazon S3
     */
    implementation(platform("software.amazon.awssdk:bom:2.54.6"))
    implementation("software.amazon.awssdk:s3")

    /**
     * Google calendar API
     */
    implementation("com.google.auth:google-auth-library-oauth2-http:1.42.1")
    implementation("com.google.api-client:google-api-client:2.8.1")
    implementation("com.google.apis:google-api-services-calendar:v3-rev20250115-2.0.0")

    /**
     * Jira
     */
    implementation("com.atlassian.jira:jira-rest-java-client-core:7.0.1") 
    implementation("com.atlassian.jira:jira-rest-java-client-api:7.0.1")
    
    implementation("org.glassfish.jersey.core:jersey-common:4.0.2")
    implementation("io.atlassian.fugue:fugue:5.0.2")

    /**
     * Imgscalr
     */
    //?
    implementation("org.imgscalr:imgscalr-lib:4.2")

    /**
     * PDF
     */
    implementation("org.apache.pdfbox:pdfbox")
    implementation("io.github.openhtmltopdf:openhtmltopdf-pdfbox:1.1.37")    

    /**
     * Tests
     */
    testCompileOnly("org.projectlombok:lombok")
    testAnnotationProcessor("org.projectlombok:lombok")

    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.springframework.boot:spring-boot-test-autoconfigure")
    testImplementation("org.springframework.boot:spring-boot-starter-webmvc-test")
    testImplementation("org.springframework.boot:spring-boot-testcontainers")
    testImplementation("org.springframework.boot:spring-boot-starter-data-redis") 

    testImplementation("org.testcontainers:testcontainers")
    testImplementation("org.testcontainers:testcontainers-junit-jupiter")
    testImplementation("org.testcontainers:testcontainers-postgresql")
    
    testImplementation("org.assertj:assertj-core")
}

jacoco {
    toolVersion = "0.8.15"
}

tasks.jacocoTestReport {
    dependsOn(tasks.test)
    reports {
        xml.required.set(true)
        html.required.set(true)
        csv.required.set(false)
    }

    sourceSets {
        named("test") {
            java.srcDirs("src/test/java")
            resources.srcDirs("src/test/resources")
        }
    }
    classDirectories.setFrom(
        fileTree(project.buildDir.resolve("classes/java/main")) {
            include("**/service/**")
        }
    )

    executionData.setFrom(fileTree(project.buildDir).include("jacoco/test.exec"))
}

tasks.jacocoTestCoverageVerification {
    dependsOn(tasks.jacocoTestReport)

    classDirectories.setFrom(
        fileTree(project.buildDir.resolve("classes/java/main")) {
            include("**/service/**)")
        }
    )

    violationRules {
        rule {
            limit {
                counter = "LINE"
                value = "COVEREDRATIO"
                minimum = 0.70.toBigDecimal()
            }
        }
    }
}

tasks.processTestResources {
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
}

tasks.withType<Test> {
    useJUnitPlatform()

    jvmArgs(
        "-XX:+EnableDynamicAgentLoading",
        "--enable-native-access=ALL-UNNAMED"
    )
}

tasks.test {
    useJUnitPlatform {
        excludeTags("integration")
    }
    
    testLogging {
        events("passed", "skipped", "failed", "standardOut", "standardError")
        showStandardStreams = true
        exceptionFormat = org.gradle.api.tasks.testing.logging.TestExceptionFormat.FULL
    }    
}

tasks.bootJar {
    archiveFileName.set("service.jar")
}
