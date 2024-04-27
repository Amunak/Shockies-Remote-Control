package net.amunak

import io.ktor.server.application.*
import io.ktor.server.cio.*
import io.ktor.server.engine.*
import net.amunak.plugins.configureRouting
import net.amunak.plugins.configureSockets
import net.amunak.plugins.configureTemplating

fun main() {
	embeddedServer(CIO, port = 8080, host = "0.0.0.0", module = Application::module, watchPaths = listOf("classes", "resources"))
		.start(wait = true)
}

fun Application.module() {
	configureSockets()
	configureTemplating()
	configureRouting()
}
