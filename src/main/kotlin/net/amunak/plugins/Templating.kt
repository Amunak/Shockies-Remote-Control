package net.amunak.plugins

import net.amunak.repository.ShockiesClientRepository
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.pebble.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.pebbletemplates.pebble.loader.ClasspathLoader

fun Application.configureTemplating() {
	install(Pebble) {
		loader(ClasspathLoader().apply {
			prefix = "templates"
		})
	}

	routing {
		get("/") {
			call.respond(PebbleContent("admin.html", mapOf("repository" to ShockiesClientRepository)))
		}
		route("/configure/{id}") {
			handle {
				call.pathParameters["id"]?.let { id ->
					ShockiesClientRepository[id]?.let {
						call.respond(PebbleContent("configure.html", mapOf("repository" to ShockiesClientRepository, "client" to it)))
						return@handle
					}
				}

				call.respond(HttpStatusCode.NotFound)

			}
		}
	}
}

data class PebbleUser(val id: Int, val name: String)
