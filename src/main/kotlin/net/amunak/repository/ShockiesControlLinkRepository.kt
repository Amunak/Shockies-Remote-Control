package net.amunak.repository

import net.amunak.models.ShockiesClientId
import net.amunak.models.ShockiesControlLink
import net.amunak.models.ShockiesControlLinkId
import java.util.concurrent.locks.ReentrantReadWriteLock
import kotlin.concurrent.read
import kotlin.concurrent.write

object ShockiesControlLinkRepository: MutableSet<ShockiesControlLink> {
	private val links: HashMap<ShockiesControlLinkId, ShockiesControlLink> = hashMapOf()
	private val lock = ReentrantReadWriteLock()

	fun cleanupOrphaned() {
		lock.write {
			links.values.removeIf { ShockiesClientRepository.contains(it.clientId).not() }
		}
	}

	operator fun get(id: ShockiesControlLinkId) = lock.read { links[id] }
	operator fun get(clientId: ShockiesClientId) = lock.read { links.values.filter { it.clientId == clientId } }
	fun addIfAbsent(element: ShockiesControlLink): ShockiesControlLink = lock.write { links.putIfAbsent(element.id, element) ?: element }
	override fun add(element: ShockiesControlLink) = lock.write { links.putIfAbsent(element.id, element) == null }
	override val size get() = lock.read { links.size }
	override fun clear() = lock.write { links.clear() }
	override fun isEmpty() = lock.read { links.isEmpty() }
	override fun iterator() = lock.read { links.values.iterator() }
	override fun retainAll(elements: Collection<ShockiesControlLink>) = lock.write { links.keys.retainAll(elements.map { it.id}.toSet()) }
	override fun removeAll(elements: Collection<ShockiesControlLink>) = lock.write { links.keys.removeAll(elements.map { it.id}.toSet()) }
	override fun remove(element: ShockiesControlLink) = lock.write { links.remove(element.id) != null }
	fun remove(clientId: ShockiesClientId) = lock.write { links.values.removeIf { it.clientId == clientId } }
	override fun containsAll(elements: Collection<ShockiesControlLink>) = lock.read { links.keys.containsAll(elements.map { it.id}.toSet()) }
	override operator fun contains(element: ShockiesControlLink) = lock.read { links.containsKey(element.id) }
	override fun addAll(elements: Collection<ShockiesControlLink>) = elements.map { add(it) }.contains(false)
}
