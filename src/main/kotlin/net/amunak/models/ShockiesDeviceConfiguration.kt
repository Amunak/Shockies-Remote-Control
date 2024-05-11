package net.amunak.models

import java.util.BitSet

data class ShockiesDeviceConfiguration(
	val features: ShockiesDeviceFeatures = ShockiesDeviceFeatures(),
	val shockIntensity: Int = 100,
	val shockDuration: Int = 60,
	val vibrateIntensity: Int = 100,
	val vibrateDuration: Int = 60,
) {
	companion object {
		/**
		 * Parses a configuration string like `CONFIG:0000030A053C05` into a [ShockiesDeviceConfiguration] object.
		 */
		@OptIn(ExperimentalStdlibApi::class)
		fun fromString(configuration: String): ShockiesDeviceConfiguration {
			var conf = configuration.removePrefix("CONFIG:")

			val iterator = conf.chunkedSequence(2)
				.map { it.hexToInt() }
				.iterator()

			// throw away first 2 bytes (reserved)
			iterator.next()
			iterator.next()

			return ShockiesDeviceConfiguration(
				ShockiesDeviceFeatures.decode(iterator.next()),
				iterator.next(),
				iterator.next(),
				iterator.next(),
				iterator.next(),
			)
		}
	}

	@OptIn(ExperimentalStdlibApi::class)
	fun buildConfigString(): String {
		return "CONFIG:" + buildString {
			append("00") // reserved
			append("00") // reserved
			append(features.encode().toHexString())
			append(shockIntensity.toHexString())
			append(shockDuration.toHexString())
			append(vibrateIntensity.toHexString())
			append(vibrateDuration.toHexString())
		}
	}

	override fun toString(): String {
		return "Features: {$features}, Shock Intensity: $shockIntensity, Shock Duration: $shockDuration, Vibrate Intensity: $vibrateIntensity, Vibrate Duration: $vibrateDuration"
	}
}
