package net.amunak.repository

import net.amunak.models.ShockiesClient
import java.util.concurrent.locks.ReentrantReadWriteLock
import kotlin.concurrent.read
import kotlin.concurrent.write

object ShockiesClientRepository: MutableSet<ShockiesClient> {
	private val clients: HashMap<String, ShockiesClient> = hashMapOf()
	private val lock = ReentrantReadWriteLock()

	operator fun get(id: String) = lock.read { clients[id] }
	fun addIfAbsent(element: ShockiesClient): ShockiesClient = lock.write { clients.putIfAbsent(element.id, element) ?: element }
	override fun add(element: ShockiesClient) = lock.write { clients.putIfAbsent(element.id, element) == null }
	override val size get() = lock.read { clients.size }
	override fun clear() = lock.write { clients.clear() }
	override fun isEmpty() = lock.read { clients.isEmpty() }
	override fun iterator() = lock.read { clients.values.iterator() }
	override fun retainAll(elements: Collection<ShockiesClient>) = lock.write { clients.keys.retainAll(elements.map { client -> client.id}.toSet()) }
	override fun removeAll(elements: Collection<ShockiesClient>) = lock.write { clients.keys.removeAll(elements.map { client -> client.id}.toSet()) }
	override fun remove(element: ShockiesClient) = lock.write { clients.remove(element.id) != null }
	override fun containsAll(elements: Collection<ShockiesClient>) = lock.read { clients.keys.containsAll(elements.map { client -> client.id}.toSet()) }
	override operator fun contains(element: ShockiesClient) = lock.read { clients.containsKey(element.id) }
	override fun addAll(elements: Collection<ShockiesClient>) = elements.map { client -> add(client) }.contains(false)
}
