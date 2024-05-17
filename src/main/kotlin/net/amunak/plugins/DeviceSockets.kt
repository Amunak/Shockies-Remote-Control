package net.amunak.plugins

import io.ktor.server.application.*
import io.ktor.server.routing.*
import io.ktor.server.websocket.*
import io.ktor.websocket.*
import kotlinx.datetime.Clock
import net.amunak.RouteGenerator
import net.amunak.baseUri
import net.amunak.eventBus.DeviceMessageEventBus
import net.amunak.models.*
import net.amunak.models.WebsocketLogMessage.WebsocketMessageDirection
import net.amunak.repository.ShockiesClientRepository
import net.amunak.updateBaseUri

fun Application.configureDeviceSockets() {
	routing {
		webSocket("/ws") {
			log.debug("New websocket connection")

			call.request.updateBaseUri()

			val tempLog: WebsocketMessageLog = WebsocketMessageLog()
			var client: ShockiesClient? = null

			try {
				for (frame in incoming) {
					client?.updateLastSeen()

					if (frame is Frame.Text) {
						val text = frame.readText()
						tempLog.push(WebsocketLogMessage(WebsocketMessageDirection.RECEIVE, text))

						if (text.startsWith("ERROR", ignoreCase = true)) {
							log.info("Received error: $text")

							if (client != null) {
								DeviceMessageEventBus.produceEvent(DeviceMessage(client.id, text))
							}

							continue
						}

						val list = text.split(" ")
						if (list.isEmpty()) {
							logAndSend(client?.log ?: tempLog, "ERROR: Invalid format")
							continue
						}

						val command = list[0].uppercase()

						// register format: `REGISTER <id>`
						if (command == "REGISTER") {
							if (list.size != 3) {
								logAndSend(client?.log ?: tempLog, "ERROR: Invalid format")
								continue
							}

							if (list[1].isBlank() || list[1].length !in 32..255) {
								logAndSend(client?.log ?: tempLog, "ERROR: Invalid ID")
								continue
							}

							if (list[2].isBlank() || !list[2].matches(Regex("^\\d+\\.\\d+\\.\\d+\$"))) {
								logAndSend(client?.log ?: tempLog, "ERROR: Invalid version")
								continue
							}

							val id = ShockiesClientId(list[1])
							if (client == null) {
								client = ShockiesClientRepository[id]
							}

							if (client != null) {
								ShockiesClientRepository.remove(client)
								log.info("Removing client ${client.id} from repository")
								logAndSend(tempLog, "INFO: Already registered as ${client.id}, re-registering.")

								tempLog.prependOther(client.log)
							}

							client = ShockiesClientRepository.addIfAbsent(ShockiesClient(id, this, version = Version.fromString(list[2]), log = tempLog))
							log.info("Registered client ${client.id}")

							val url = baseUri + RouteGenerator.generateRoute("configure", mapOf("clientId" to client.id)).trimStart('/')
							logAndSend(client.log, "REMOTE URL $url")

							continue
						}

						if (client == null) {
							logAndSend(tempLog, "ERROR: Not registered")

							continue
						}

						if (text.startsWith("CONFIG:", ignoreCase = true)) {
							client.deviceConfiguration = ShockiesDeviceConfiguration.fromString(text)

							continue
						}

						DeviceMessageEventBus.produceEvent(DeviceMessage(client.id, text))
					}
				}
			} catch (e: Exception) {
				log.error("Error handling websocket", e)
			} finally {
				client?.let {
					log.info("Client ${it.id} disconnected")
					it.disconnectedAt = Clock.System.now()
				}
			}
		}
	}
}

private suspend fun WebSocketSession.logAndSend(log: WebsocketMessageLog, message: String) {
	log.push(WebsocketLogMessage(WebsocketMessageDirection.SEND, message))
	send(message)
}
