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
    //id("com.heroku.sdk.heroku-gradle") version "2.0.0"
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

val sshAntTask = configurations.create("sshAntTask")

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

    //For ssh
    sshAntTask("org.apache.ant:ant-jsch:1.10.12")
}

tasks.withType<com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar> {
    manifest {
        attributes(
            mapOf(
                "Main-Class" to application.mainClass
            )
        )
    }
}

ant.withGroovyBuilder {
    "taskdef"(
            "name" to "scp",
            "classname" to "org.apache.tools.ant.taskdefs.optional.ssh.Scp",
            "classpath" to configurations["sshAntTask"].asPath
            )
    "taskdef"(
            "name" to "ssh",
            "classname" to "org.apache.tools.ant.taskdefs.optional.ssh.SSHExec",
            "classpath" to configurations["sshAntTask"].asPath
            )
}

task("deploy") {
    dependsOn("clean", "shadowJar")
    ant.withGroovyBuilder {
        doLast {
            val knownHosts = File.createTempFile("knownhosts", "txt")
            val user = "ubuntu"
            val host = "51.91.58.109"
            val key = file("keys/smallbrotherkey")
            val jarFileName = "smallBrother-api-$version.jar"
            try {
                "scp"(
                    "file" to file("build/libs/$jarFileName"),
                    "todir" to "$user@$host:~/smallbrother",
                    "keyfile" to key,
                    "trust" to true,
                    "knownhosts" to knownHosts
                )
                "ssh"(
                    "host" to host,
                    "username" to user,
                    "keyfile" to key,
                    "trust" to true,
                    "knownhosts" to knownHosts,
                    "command" to "mv ~/smallbrother/$jarFileName ~/smallbrother/smallbrother.jar"
                )
                "ssh"(
                    "host" to host,
                    "username" to user,
                    "keyfile" to key,
                    "trust" to true,
                    "knownhosts" to knownHosts,
                    "command" to "systemctl stop smallbrother"
                )
                "ssh"(
                    "host" to host,
                    "username" to user,
                    "keyfile" to key,
                    "trust" to true,
                    "knownhosts" to knownHosts,
                    "command" to "systemctl start smallbrother"
                )
            } finally {
                knownHosts.delete()
            }
        }
    }
}

//this is for heroku setup
/*tasks.create("stage") {
    dependsOn("installDist")
}

heroku {
    appName= "smallbrother-api"
}

tasks.withType<JavaCompile> {
    options.release.set(11)
}*/

//for fatJar
tasks.withType<Jar> {
    manifest {
        attributes(
            mapOf(
                "Main-Class" to application.mainClass
            )
        )
    }
}

ktor {
    docker {
        jreVersion.set(io.ktor.plugin.features.JreVersion.JRE_17)
    }
}