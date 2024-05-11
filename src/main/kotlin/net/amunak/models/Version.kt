package net.amunak.models

class Version(val major: Int, val minor: Int, val patch: Int) : Comparable<Version> {

	public constructor(major: Int, minor: Int) : this(major, minor, 0)

	private val version = versionOf(major, minor, patch)

	private fun versionOf(major: Int, minor: Int, patch: Int): Int {
		require(major in 0..Version.MAX_COMPONENT_VALUE && minor in 0..Version.MAX_COMPONENT_VALUE && patch in 0..Version.MAX_COMPONENT_VALUE) {
			"Version components are out of range: $major.$minor.$patch"
		}
		return major.shl(16) + minor.shl(8) + patch
	}

	override fun toString(): String = "$major.$minor.$patch"

	override fun equals(other: Any?): Boolean {
		if (this === other) return true
		val otherVersion = (other as? Version) ?: return false
		return this.version == otherVersion.version
	}

	override fun hashCode(): Int = version

	override fun compareTo(other: Version): Int = version - other.version

	public fun isAtLeast(major: Int, minor: Int): Boolean =
		this.major > major || (this.major == major &&
				this.minor >= minor)

	public fun isAtLeast(major: Int, minor: Int, patch: Int): Boolean =
		this.major > major || (this.major == major &&
				(this.minor > minor || this.minor == minor &&
						this.patch >= patch))

	companion object {
		public const val MAX_COMPONENT_VALUE = 255

		fun fromString(version: String): Version {
			val parts = version.split(".")
			if (parts.size != 3) {
				throw IllegalArgumentException("Invalid version string: $version")
			}

			return Version(parts[0].toInt(), parts[1].toInt(), parts[2].toInt())
		}
	}
}
