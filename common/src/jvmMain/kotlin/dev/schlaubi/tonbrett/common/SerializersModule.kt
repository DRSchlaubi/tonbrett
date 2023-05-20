package dev.schlaubi.tonbrett.common

import kotlinx.serialization.modules.SerializersModule
import org.litote.kmongo.id.serialization.IdKotlinXSerializationModule

public actual val TonbrettSerializersModule: SerializersModule = IdKotlinXSerializationModule
