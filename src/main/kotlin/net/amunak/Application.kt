package net.amunak

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.cio.*
import io.ktor.server.engine.*
import io.ktor.server.plugins.defaultheaders.*
import io.ktor.server.plugins.ratelimit.*
import io.ktor.server.request.*
import io.ktor.server.websocket.*
import net.amunak.plugins.configureControlSockets
import net.amunak.plugins.configureDeviceSockets
import net.amunak.plugins.configureRouting
import net.amunak.plugins.configureTemplating
import net.amunak.repository.Properties
import java.time.Duration
import kotlin.time.Duration.Companion.milliseconds

fun main() {
	kotlin.concurrent.timer(period = 60000, daemon = true) {
		net.amunak.repository.ShockiesClientRepository.cleanupDisconnected()
		net.amunak.repository.ShockiesControlLinkRepository.cleanupOrphaned()
		net.amunak.eventBus.DeviceMessageEventBus.cleanup()
	}

	embeddedServer(CIO, port = 8080, host = "0.0.0.0", module = Application::module, watchPaths = listOf("classes", "resources"))
		.start(wait = true)
}

private var baseUri: String = "/"
public val Application.baseUri: String
	get() = net.amunak.baseUri

fun ApplicationRequest.updateBaseUri() {
	if (net.amunak.baseUri != "/") {
		return
	}

	net.amunak.baseUri = local.run {
		if ((serverPort == 80 && scheme == "http") || (serverPort == 443 && scheme == "https")) {
			"${scheme}://${serverHost}/"
		} else {
			"${scheme}://${serverHost}:${serverPort}/"
		}
	}
}

fun Application.module() {
	install(DefaultHeaders) {
		header(HttpHeaders.Server, "Ktor/${Properties.ktorVersion}, ${Properties.applicationName}/${Properties.applicationVersion}")
	}
	install(RateLimit) {
		register(RateLimitName("ws-clients")) {
			rateLimiter(initialSize = 8, limit = 4, refillPeriod = 900.milliseconds)
			requestKey { call ->
				call.request.local.remoteHost
			}
		}
	}
	install(io.ktor.server.websocket.WebSockets) {
		pingPeriod = Duration.ofSeconds(15)
		timeout = Duration.ofSeconds(15)
		maxFrameSize = Long.MAX_VALUE
		masking = false
	}
	configureDeviceSockets()
	configureControlSockets()
	configureTemplating()
	configureRouting()
}
