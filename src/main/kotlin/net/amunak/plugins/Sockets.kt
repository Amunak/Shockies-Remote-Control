package net.amunak.plugins

import net.amunak.models.ShockiesClient
import net.amunak.repository.ShockiesClientRepository
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.websocket.*
import io.ktor.websocket.*
import java.time.Duration

fun Application.configureSockets() {
	install(WebSockets) {
		pingPeriod = Duration.ofSeconds(15)
		timeout = Duration.ofSeconds(15)
		maxFrameSize = Long.MAX_VALUE
		masking = false
	}
	routing {
		webSocket("/ws") {
			send("You are connected!")

			var client: ShockiesClient? = null

			try {
				for (frame in incoming) {
					if (frame is Frame.Text) {
						log.info("repo size: ${ShockiesClientRepository.size}")
						ShockiesClientRepository.iterator().forEach {
							log.info("client: ${it.id}")
						}

						val text = frame.readText()
						val list = text.split(" ")

						if (list.isEmpty()) {
							send("Invalid format")
							continue
						}

						val command = list[0].uppercase()

						if (command == "BYE") {
							if (client != null) {
								ShockiesClientRepository.remove(client)
							}
							close(CloseReason(CloseReason.Codes.NORMAL, "Client said BYE"))
						}

						// register format: `REGISTER <id>`
						if (command == "REGISTER") {
							if (list.size != 2) {
								send("Invalid format")
								continue
							}

							val id = list[1]
							if (id.isBlank() || id.length != 128) {
								send("Invalid ID")
								continue
							}

							if (client != null) {
								ShockiesClientRepository.remove(client)
								log.info("Removing client ${client.id} from repository")
								send("Already registered as ${client.id}, re-registering.")
							}

							client = ShockiesClientRepository.addIfAbsent(ShockiesClient(id))
							send("Registered as ${client.id}")
							log.info("Registered client ${client.id}")
						}

						if (client == null) {
							send("Not registered")
							continue
						}


					}
				}
			} catch (e: Exception) {
				log.error("Error handling websocket", e)
			} finally {
				client?.let {
					log.info("Removing client ${it.id} from repository")
					ShockiesClientRepository.remove(it)
				}
			}
		}
	}
}
