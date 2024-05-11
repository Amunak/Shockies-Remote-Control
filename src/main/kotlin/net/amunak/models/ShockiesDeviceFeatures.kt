package net.amunak.models

data class ShockiesDeviceFeatures(
	val shock: Boolean = true,
	val vibrate: Boolean = true,
	val beep: Boolean = true,
	val light: Boolean = true,
) {
	companion object {
		fun decode(char: Int): ShockiesDeviceFeatures {
			return ShockiesDeviceFeatures(
				(char and 0b0001) == 1,
				(char and 0b0010) == 1,
				(char and 0b0100) == 1,
				(char and 0b1000) == 1,
			)
		}
	}

	fun encode(): Int = (if (shock) 0b0001 else 0) or
			   (if (vibrate) 0b0010 else 0) or
			   (if (beep) 0b0100 else 0) or
			   (if (light) 0b1000 else 0)

	override fun toString(): String {
		return "Shock: $shock, Vibrate: $vibrate, Beep: $beep, Light: $light"
	}
}
