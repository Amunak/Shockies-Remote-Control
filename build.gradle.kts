val ktorVersion: String by project
val kotlinVersion: String by project
val logbackVersion: String by project
val kotlinxDatetimeVersion: String by project

plugins {
	kotlin("jvm") version "1.9.23"
	application
}

group = "net.amunak"
version = "0.0.1"

application {
	mainClass.set("net.amunak.ApplicationKt")

	val isDevelopment: Boolean = project.ext.has("development")
	applicationDefaultJvmArgs = listOf("-Dio.ktor.development=$isDevelopment")
}

repositories {
	mavenCentral()
}

dependencies {
	implementation("io.ktor:ktor-server-core-jvm:$ktorVersion")
	implementation("io.ktor:ktor-server-websockets-jvm:$ktorVersion")
	implementation("io.ktor:ktor-server-pebble-jvm:$ktorVersion")
	implementation("io.ktor:ktor-server-host-common-jvm:$ktorVersion")
	implementation("io.ktor:ktor-server-status-pages-jvm:$ktorVersion")
	implementation("io.ktor:ktor-server-cio:$ktorVersion")
	implementation("ch.qos.logback:logback-classic:$logbackVersion")
	testImplementation("io.ktor:ktor-server-tests-jvm:$ktorVersion")
	testImplementation("org.jetbrains.kotlin:kotlin-test-junit:$kotlinVersion")
	implementation("org.jetbrains.kotlinx:kotlinx-datetime:$kotlinxDatetimeVersion")
}
