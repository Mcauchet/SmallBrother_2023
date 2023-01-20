val ktorVersion: String by project
val kotlinVersion: String by project
val logbackVersion: String by project
val exposedVersion: String by project
val postgresqlVersion: String by project

plugins {
    application
    kotlin("jvm") version "1.7.20"
    id("io.ktor.plugin") version "2.2.2"
    id("org.jetbrains.kotlin.plugin.serialization") version "1.7.20"
    id("distribution")
}

group = "com.example"
version = "0.0.1"

application {
    mainClass.set("io.ktor.server.netty.EngineMain")

    val isDevelopment: Boolean = project.ext.has("development")
    applicationDefaultJvmArgs = listOf("-Dio.ktor.development=$isDevelopment")
}

repositories {
    mavenCentral()
}

dependencies {
    //Ktor's core components
    implementation("io.ktor:ktor-server-core-jvm:$ktorVersion")
    //Mechanism for converting Kotlin objects into a serialized form like JSON
    implementation("io.ktor:ktor-server-content-negotiation-jvm:$ktorVersion")
    implementation("io.ktor:ktor-serialization-kotlinx-json-jvm:$ktorVersion")
    //Adds the Netty engine
    implementation("io.ktor:ktor-server-netty:$ktorVersion")
    implementation("io.ktor:ktor-server-netty-jvm:$ktorVersion")
    //Implementation of SLF4J for formatted logs
    implementation("ch.qos.logback:logback-classic:$logbackVersion")
    //Templating
    implementation("io.ktor:ktor-server-freemarker:$ktorVersion")
    implementation("io.ktor:ktor-server-call-logging-jvm:2.2.2")
    //Log HTTPRequest
    implementation("io.ktor:ktor-server-call-logging:$ktorVersion")
    //Allows to test parts of the Ktor application
    testImplementation("io.ktor:ktor-server-tests-jvm:$ktorVersion")
    testImplementation("io.ktor:ktor-server-test-host:$ktorVersion")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit:$kotlinVersion")
    //Exposed dependencies
    implementation("org.jetbrains.exposed:exposed-core:$exposedVersion")
    implementation("org.jetbrains.exposed:exposed-dao:$exposedVersion")
    implementation("org.jetbrains.exposed:exposed-jdbc:$exposedVersion")
    //PostgreSQL Database
    implementation("org.postgresql:postgresql:$postgresqlVersion")
    implementation("org.jetbrains.exposed:exposed-java-time:0.40.1")
    //Authentication
    implementation("io.ktor:ktor-server-auth:$ktorVersion")
    //Sessions
    implementation("io.ktor:ktor-server-sessions:$ktorVersion")
    //Encryption
    implementation("org.mindrot:jbcrypt:0.4")
}

//this is for heroku setup
tasks.create("stage") {
    dependsOn("installDist")
}

tasks.withType<Jar> {
    manifest {
        attributes(
            mapOf(
                "Main-Class" to application.mainClass
            )
        )
    }
}

//for fatJar
ktor {
    fatJar {
        archiveFileName.set("fat.jar")
    }
    docker {
        jreVersion.set(io.ktor.plugin.features.JreVersion.JRE_17)
    }
}