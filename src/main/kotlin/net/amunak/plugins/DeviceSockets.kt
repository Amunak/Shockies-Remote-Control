package net.amunak.plugins

import net.amunak.repository.ShockiesClientRepository
import io.ktor.server.application.*
import io.ktor.server.routing.*
import io.ktor.server.websocket.*
import io.ktor.server.websocket.webSocket
import io.ktor.websocket.*
import kotlinx.datetime.Clock
import net.amunak.eventBus.DeviceMessageEventBus
import net.amunak.models.*
import net.amunak.models.WebsocketLogMessage.WebsocketMessageDirection
import java.time.Duration

fun Application.configureDeviceSockets() {
	routing {
		webSocket("/ws") {
			log.debug("New websocket connection")

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
							if (list.size != 2) {
								logAndSend(client?.log ?: tempLog, "ERROR: Invalid format")
								continue
							}

							if (list[1].isBlank() || list[1].length !in 32..255) {
								logAndSend(client?.log ?: tempLog, "ERROR: Invalid ID")
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

							client = ShockiesClientRepository.addIfAbsent(ShockiesClient(id, this, log = tempLog))
							log.info("Registered client ${client.id}")

							val url = call.request.local.run {
								if ((serverPort == 80 && scheme == "http") || (serverPort == 443 && scheme == "https")) {
									"${scheme}://${serverHost}/configure/${client.id}"
								} else {
									"${scheme}://${serverHost}:${serverPort}/configure/${client.id}"
								}
							}
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
