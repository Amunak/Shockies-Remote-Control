package net.amunak.plugins

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.http.content.*
import io.ktor.server.plugins.statuspages.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Application.configureRouting() {
	val teapotStatusCode = HttpStatusCode(418, "I'm a teapot")

	install(StatusPages) {
		exception<Throwable> { call, cause ->
			this@configureRouting.log.error("500: $cause")
			call.respondText(text = "500: Internal Server Error", status = HttpStatusCode.InternalServerError)
		}

		val requestErrors = HttpStatusCode.allStatusCodes.filter { it.value in 400..<500 }.toMutableList()
		requestErrors.add(teapotStatusCode)
		status(*requestErrors.toTypedArray()) { call, cause ->
			this@configureRouting.log.debug("$cause: ${call.request.uri}")
			call.respondText(text = cause.toString(), status = cause)
		}

		status(*HttpStatusCode.allStatusCodes.filter { it.value >= 500 }.toTypedArray()) { call, cause ->
			this@configureRouting.log.error("$cause: ${call.request.uri}")
			call.respondText(text = cause.toString(), status = cause)
		}
	}

	routing {
		route("/tea") { handle { call.respond(teapotStatusCode) } }
		staticResources("/assets", "assets")
	}
}
