package net.amunak.plugins

import io.ktor.server.application.*
import io.ktor.server.routing.*
import io.ktor.server.websocket.*
import io.ktor.websocket.*
import kotlinx.coroutines.launch
import net.amunak.eventBus.DeviceMessageEventBus
import net.amunak.models.ShockiesControlLink
import net.amunak.models.ShockiesControlLinkId
import net.amunak.models.WebsocketLogMessage
import net.amunak.models.WebsocketLogMessage.WebsocketMessageDirection
import net.amunak.repository.ShockiesClientRepository
import net.amunak.repository.ShockiesControlLinkRepository

fun Application.configureControlSockets() {
	routing {
		webSocket("/control/{id}/ws") {
			val id = call.parameters["id"]?.let { ShockiesControlLinkId(it) }
			val controlLink = id?.let { ShockiesControlLinkRepository[id] }
			if (controlLink == null) {
				close(CloseReason(CloseReason.Codes.NORMAL, "ERROR: INVALID ID"))
				return@webSocket
			}

			assertNotExpired(controlLink)

			launch {
				DeviceMessageEventBus.flow(controlLink.clientId).collect() { message ->
					assertNotExpired(controlLink)
					send(message.message)
				}
			}

			for (frame in incoming) {
				assertNotExpired(controlLink)

				if (frame is Frame.Binary) {
					close(CloseReason(CloseReason.Codes.NORMAL, "ERROR: BINARY NOT SUPPORTED"))
					return@webSocket
				}

				if (frame is Frame.Text) {
					val text = frame.readText()
					if (text.startsWith("ERROR", ignoreCase = true)) {
						continue
					}
					if (text.startsWith("INFO", ignoreCase = true)) {
						continue
					}

					val list = text.split(" ")

					if (list.isEmpty()) {
						send("ERROR: INVALID FORMAT")
						continue
					}

					val command = list[0].uppercase()

					// we send our own configuration
					if (command == "C") {
						send(controlLink.configuration.buildConfigString())
						continue
					}

					// we ignore ping commands
					if (command == "P") {
						continue
					}

					// todo this should be an enum
					val allowedCommands = setOf(
						"X", // estop
						"R", // reset running command
						"L", // light
						"B", // beep
						"V", // vibration
						"S", // shock
					)
					if (command !in allowedCommands) {
						send("ERROR: INVALID COMMAND")
						continue
					}

					// @todo implement limit checking

					val client = ShockiesClientRepository[controlLink.clientId]
					if (client == null) {
						close(CloseReason(CloseReason.Codes.NORMAL, "ERROR: CLIENT MISSING"))
						return@webSocket
					}

					client.log.push(WebsocketLogMessage(WebsocketMessageDirection.SEND, text))
					client.ws.send(text)
				}
			}
		}
	}
}

private suspend fun WebSocketSession.assertNotExpired(controlLink: ShockiesControlLink) {
	if (controlLink.isExpired) {
		close(CloseReason(CloseReason.Codes.NORMAL, "ERROR: EXPIRED"))
	}
}
