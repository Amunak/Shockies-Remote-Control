package net.amunak.plugins

import net.amunak.repository.ShockiesClientRepository
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.pebble.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.websocket.*
import io.ktor.websocket.*
import io.pebbletemplates.pebble.loader.ClasspathLoader
import kotlinx.coroutines.channels.consumeEach
import net.amunak.models.ShockiesClientId
import net.amunak.models.WebsocketLogMessage
import net.amunak.models.WebsocketMessageLogObserver

fun Application.configureTemplating() {
	install(Pebble) {
		loader(ClasspathLoader().apply {
			prefix = "templates"
		})
	}

	routing {
		get("/") {
			call.respond(PebbleContent("admin.html.peb", mapOf("repository" to ShockiesClientRepository)))
		}
		route("/configure/{id}") {
			handle {
				call.pathParameters["id"]?.let { id ->
					ShockiesClientRepository[ShockiesClientId(id)]?.let {
						call.respond(PebbleContent("configure.html.peb", mapOf("repository" to ShockiesClientRepository, "client" to it)))
						return@handle
					}
				}

				call.respond(HttpStatusCode.NotFound)
			}

			webSocket("/log-stream") {
				class LogObserver : WebsocketMessageLogObserver {
					private val websocketClient: WebSocketSession
					constructor(
						websocketClient: WebSocketSession
					) {
						this.websocketClient = websocketClient
					}

					override suspend fun onMessage(message: WebsocketLogMessage) {
						websocketClient.send(message.toString())
					}
				}

				call.parameters["id"]?.let { id ->
					ShockiesClientRepository[ShockiesClientId(id)]?.let { client ->
						send(client.log.toString())
						val observer = LogObserver(this)
						try {
							client.log.attachObserver(observer)
							for (frame in incoming) {
								// do nothing
							}
						} finally {
							// at this point client could be different
							ShockiesClientRepository[ShockiesClientId(id)]?.log?.detachObserver(observer)

							return@webSocket
						}
					}

					call.respond(HttpStatusCode.NotFound)
				}
			}
		}
	}
}

data class PebbleUser(val id: Int, val name: String)
