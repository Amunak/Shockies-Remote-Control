package net.amunak.models

import java.io.Console

final data class ShockiesClientId(val value: String) : Comparable<ShockiesClientId> {
	override fun compareTo(other: ShockiesClientId): Int {
		println("XComparing $value with ${(other as? ShockiesClientId)?.value}")
		return value.compareTo(other.value)
	}

	override fun toString(): String {
		return value
	}
}
