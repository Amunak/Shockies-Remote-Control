package net.amunak.models

import kotlinx.coroutines.CoroutineScope

interface WebsocketMessageLogObserver {
	suspend fun onMessage(message: WebsocketLogMessage)
}
