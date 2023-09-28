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
// We cannot expect a class annotated with @Serializable, therefore we use @Contextual
// and annotate the actual classes with @Serializable
public typealias SerializableSnowflake = @Contextual Snowflake
