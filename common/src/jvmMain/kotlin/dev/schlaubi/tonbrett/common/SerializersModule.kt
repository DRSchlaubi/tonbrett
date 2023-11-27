package dev.schlaubi.tonbrett.common

import kotlinx.serialization.modules.SerializersModule
import org.litote.kmongo.id.StringId

public actual val TonbrettSerializersModule: SerializersModule = SerializersModule {
    contextual(Id::class, IdSerializer)
}
