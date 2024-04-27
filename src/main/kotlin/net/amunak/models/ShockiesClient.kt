package net.amunak.models

import kotlinx.datetime.Clock
import kotlinx.datetime.Instant

data class ShockiesClient(
	val id: String,
	val createdAt: Instant = Clock.System.now(),
	var lastSeenAt: Instant = Clock.System.now(),
) {
	override fun equals(other: Any?): Boolean = id == (other as? ShockiesClient)?.id

	fun updateLastSeen() {
		lastSeenAt = Clock.System.now()
	}
}
