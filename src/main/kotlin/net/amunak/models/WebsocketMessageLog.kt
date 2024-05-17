package net.amunak.models

import net.amunak.models.WebsocketLogMessage.WebsocketMessageDirection

const val maxMessages: Int = 1000;

class WebsocketMessageLog: Iterable<WebsocketLogMessage> {
	private val log: MutableList<WebsocketLogMessage> = mutableListOf()
	private val observers: MutableList<WebsocketMessageLogObserver> = mutableListOf()

	fun attachObserver(observer: WebsocketMessageLogObserver) = observers.add(observer)
	fun detachObserver(observer: WebsocketMessageLogObserver) = observers.remove(observer)

	suspend fun push(direction: WebsocketMessageDirection, message: String) = push(WebsocketLogMessage(direction, message))
	suspend fun push(vararg messages: WebsocketLogMessage) {
		log.addAll(messages)
		observers.forEach { observer ->
			messages.forEach  { message ->
				observer.onMessage(message)
			}
		}

		while (log.size > maxMessages) log.removeFirst()
	}

	suspend fun prependOther(other: WebsocketMessageLog) {
		other.push(WebsocketMessageDirection.SYSTEM, "--- Log from another client ---")

		log.addAll(0, other.log)

		observers.addAll(other.observers)
		other.observers.clear()
	}

	val size get()  = log.size
	fun asIterable() = log.asIterable()
	override fun iterator(): Iterator<WebsocketLogMessage> = log.iterator()
	override fun toString() = log.joinToString(separator = "")
}
