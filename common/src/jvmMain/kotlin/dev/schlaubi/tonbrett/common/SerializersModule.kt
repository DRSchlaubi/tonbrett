package dev.schlaubi.tonbrett.common

import kotlinx.serialization.modules.SerializersModule

public actual val TonbrettSerializersModule: SerializersModule = SerializersModule {
    contextual(Id::class, IdSerializer)
}
