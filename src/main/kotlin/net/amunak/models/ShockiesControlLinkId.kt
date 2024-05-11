package net.amunak.models

import io.ktor.server.application.*
import java.security.SecureRandom

final data class ShockiesControlLinkId(val value: String) : Comparable<ShockiesControlLinkId> {
	companion object {
		fun generate(): ShockiesControlLinkId {
			val secureRandom = SecureRandom()
			val stringBuilder = StringBuilder()
			val chars: List<Char> = ('a'..'z') + ('A'..'Z') + ('0'..'9')

			for (i in 0..64) {
				stringBuilder.append(chars[secureRandom.nextInt(chars.size)])
			}

			return ShockiesControlLinkId(stringBuilder.toString())
		}
	}

	override fun compareTo(other: ShockiesControlLinkId): Int {
		return value.compareTo(other.value)
	}

	override fun toString(): String {
		return value
	}
}
