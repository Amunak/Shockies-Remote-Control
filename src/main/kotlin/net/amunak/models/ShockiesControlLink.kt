package net.amunak.models

import kotlinx.datetime.Clock
import kotlinx.datetime.Instant

data class ShockiesControlLink(
	val id: ShockiesControlLinkId = ShockiesControlLinkId.generate(),
	val clientId: ShockiesClientId,
	val configuration: ShockiesDeviceConfiguration,
	var expiresAt: Instant = Instant.DISTANT_FUTURE,
) {
	val isExpired: Boolean get() = expiresAt < Clock.System.now()

}
