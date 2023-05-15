package dev.schlaubi.tonbrett.common

import kotlinx.serialization.Serializable

/**
 * Multiplatform snowflake.
 *
 * @property value the Snowflake as a [ULong]
 */
public expect class Snowflake(value: ULong) {
    public val value: ULong
}
