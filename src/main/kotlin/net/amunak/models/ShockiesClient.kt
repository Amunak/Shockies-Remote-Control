package net.amunak.models

import io.ktor.websocket.*
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import net.amunak.repository.ShockiesControlLinkRepository

data class ShockiesClient(
	val id: ShockiesClientId,
	var ws: WebSocketSession,
	val log: WebsocketMessageLog = WebsocketMessageLog(),
	val createdAt: Instant = Clock.System.now(),
	var lastSeenAt: Instant = Clock.System.now(),
	var disconnectedAt: Instant? = null,
	var deviceConfiguration: ShockiesDeviceConfiguration? = null,
) {
	init {
		if (ShockiesControlLinkRepository.get(id).isEmpty()) {
			val controlLink = ShockiesControlLink(clientId = id, configuration = deviceConfiguration ?: ShockiesDeviceConfiguration())
			ShockiesControlLinkRepository.add(controlLink)
		}
	}

	override fun equals(other: Any?): Boolean = id == (other as? ShockiesClient)?.id

	val controlLinks get() = ShockiesControlLinkRepository.get(id)

	fun updateLastSeen() {
		lastSeenAt = Clock.System.now()
	}
}
