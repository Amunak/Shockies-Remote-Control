package net.amunak.models

import kotlinx.datetime.*

data class WebsocketLogMessage(
	val direction: WebsocketMessageDirection,
	val message: String,
	val loggedAt: Instant = Clock.System.now(),
) {
	enum class WebsocketMessageDirection {
		SYSTEM,
		RECEIVE,
		SEND,
		;
	}

	override fun toString() = "${loggedAt.toLocalDateTime(TimeZone.currentSystemDefault()).format(LocalDateTime.Formats.ISO)} [$direction] $message\n"
}
