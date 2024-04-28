package net.amunak.repository

import java.util.Properties

object Properties {
	private val readApplicationProperties by lazy {
		Properties().also {
			it.load(javaClass.getResourceAsStream("/application.properties"))
		}
	}

	val ktorVersion by lazy { readApplicationProperties.getProperty("ktor.version") ?: throw IllegalStateException("ktor.version is not set") }
	val applicationName by lazy { readApplicationProperties.getProperty("application.name") ?: throw IllegalStateException("application.name is not set") }
	val applicationVersion by lazy { readApplicationProperties.getProperty("application.version") ?: throw IllegalStateException("application.version is not set") }
	val isDevelopment by lazy { readApplicationProperties.getProperty("development")?.toBoolean() ?: false }
}
