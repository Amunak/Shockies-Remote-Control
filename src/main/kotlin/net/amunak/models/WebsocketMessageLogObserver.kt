package net.amunak.models

interface WebsocketMessageLogObserver {
	suspend fun onMessage(message: WebsocketLogMessage)
}
