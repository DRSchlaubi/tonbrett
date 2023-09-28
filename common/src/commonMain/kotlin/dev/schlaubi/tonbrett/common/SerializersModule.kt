package dev.schlaubi.tonbrett.common

import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.Serializable

/**
 * The [SerializersModule] required for using the Tonbrett [Serializable] entities.
 */
public expect val TonbrettSerializersModule: SerializersModule
