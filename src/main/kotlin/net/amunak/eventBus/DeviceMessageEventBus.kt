package net.amunak.eventBus

import kotlinx.coroutines.flow.*
import net.amunak.models.DeviceMessage
import net.amunak.models.ShockiesClientId
import net.amunak.repository.ShockiesClientRepository

object DeviceMessageEventBus {
	private val flows: MutableMap<ShockiesClientId, MutableSharedFlow<DeviceMessage>> = mutableMapOf()

	fun flow(clientId: ShockiesClientId): SharedFlow<DeviceMessage> = flows.getOrPut(clientId) { MutableSharedFlow() }.asSharedFlow()
	suspend fun produceEvent(message: DeviceMessage) = flows[message.clientId]?.emit(message)

	fun cleanup() = flows.keys.removeIf { ShockiesClientRepository.contains(it).not() }
}
