package dev.schlaubi.tonbrett.common

import kotlinx.serialization.Contextual

/**
 * Multiplatform snowflake.
 *
 * @property value the Snowflake as a [ULong]
 */
public expect class Snowflake(value: ULong) {
    public val value: ULong
}

/**
 * Serializable version of [Snowflake].
 */
public typealias SerializableSnowflake = @Contextual Snowflake
