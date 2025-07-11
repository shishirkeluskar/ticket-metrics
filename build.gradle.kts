import com.google.protobuf.gradle.id

plugins {
	java
	id("org.springframework.boot") version "3.5.3"
	id("io.spring.dependency-management") version "1.1.7"
	id("com.google.protobuf") version "0.9.4"
}

group = "com.shishir.ticketmetrics"
version = "0.0.1-SNAPSHOT"

java {
	toolchain {
		languageVersion = JavaLanguageVersion.of(21)
	}
}

repositories {
	mavenCentral()
}

extra["springGrpcVersion"] = "0.9.0"

dependencies {
	implementation("io.grpc:grpc-services")
	implementation("org.springframework.grpc:spring-grpc-spring-boot-starter")
	testImplementation("org.springframework.boot:spring-boot-starter-test")
	testImplementation("org.springframework.grpc:spring-grpc-test")
	testRuntimeOnly("org.junit.platform:junit-platform-launcher")
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

tasks.withType<Test> {
	useJUnitPlatform()
}
