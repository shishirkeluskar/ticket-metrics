import com.google.protobuf.gradle.id

plugins {
    java
    jacoco
    id("org.springframework.boot") version "3.5.3"
    id("io.spring.dependency-management") version "1.1.7"
    id("com.google.protobuf") version "0.9.4"
}

group = "com.shishir.ticketmetrics"
version = "0.0.1-SNAPSHOT"

// https://javadoc.io/doc/org.mockito/mockito-core/latest/org.mockito/org/mockito/Mockito.html#mockito-instrumentation
// https://github.com/spring-io/initializr/issues/1590
val mockitoAgent = configurations.create("mockitoAgent")

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("io.grpc:grpc-services")
    implementation("org.springframework.grpc:spring-grpc-spring-boot-starter")
    implementation("org.springframework.boot:spring-boot-starter-cache")
    implementation("org.mybatis.spring.boot:mybatis-spring-boot-starter:${property("mybatisSpringBootVersion")}")
    implementation("org.xerial:sqlite-jdbc:3.50.2.0")

    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.mybatis.spring.boot:mybatis-spring-boot-starter-test:${property("mybatisSpringBootVersion")}")
    testImplementation("org.springframework.grpc:spring-grpc-test")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")

    mockitoAgent("org.mockito:mockito-core") { isTransitive = false }
}

dependencyManagement {
    imports {
        mavenBom("org.springframework.grpc:spring-grpc-dependencies:${property("springGrpcVersion")}")
    }
}

protobuf {
    protoc {
        artifact = "com.google.protobuf:protoc"
    }
    plugins {
        id("grpc") {
            artifact = "io.grpc:protoc-gen-grpc-java"
        }
    }
    generateProtoTasks {
        all().forEach {
            it.plugins {
                id("grpc") {
                    option("@generated=omit")
                }
            }
        }
    }
}

tasks.test {
    jvmArgs("-javaagent:${mockitoAgent.asPath}")
    useJUnitPlatform()
    finalizedBy(tasks.jacocoTestReport)

    testLogging {
        // Show test results
        events("passed", "skipped", "failed")
        showStandardStreams = true
    }
}