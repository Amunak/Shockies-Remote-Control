package net.amunak.plugins

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.pebble.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.websocket.*
import io.ktor.websocket.*
import io.pebbletemplates.pebble.extension.AbstractExtension
import io.pebbletemplates.pebble.extension.Function
import io.pebbletemplates.pebble.loader.ClasspathLoader
import io.pebbletemplates.pebble.template.EvaluationContext
import io.pebbletemplates.pebble.template.PebbleTemplate
import net.amunak.RouteGenerator
import net.amunak.baseUri
import net.amunak.models.ShockiesClientId
import net.amunak.models.WebsocketLogMessage
import net.amunak.models.WebsocketMessageLogObserver
import net.amunak.repository.ShockiesClientRepository

fun Application.configureTemplating() {
	val application = this

	install(Pebble) {
		loader(ClasspathLoader().apply {
			prefix = "templates"
		})

		class AbsoluteUri: Function {
			override fun getArgumentNames(): MutableList<String> = mutableListOf("path")
			override fun execute(p0: MutableMap<String, Any>?, p1: PebbleTemplate?, p2: EvaluationContext?, p3: Int): Any = application.baseUri + ((p0?.get("path") as? String)?.trimStart('/') ?: throw IllegalArgumentException("Missing path argument"))
		}

		class CustomExtension : AbstractExtension() {
			override fun getGlobalVariables(): MutableMap<String, Any> = mutableMapOf(
				"baseUri" to application.baseUri,
			)

			override fun getFunctions(): MutableMap<String, Function> = mutableMapOf(
				"route" to RouteGenerator,
				"absoluteUri" to AbsoluteUri(),
			)
		}

		extension(CustomExtension())
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
