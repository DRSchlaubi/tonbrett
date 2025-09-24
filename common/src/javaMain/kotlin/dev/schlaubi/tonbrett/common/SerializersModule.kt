package dev.schlaubi.tonbrett.common

import kotlinx.serialization.KSerializer
import kotlinx.serialization.modules.SerializersModule

public actual val TonbrettSerializersModule: SerializersModule = SerializersModule {
    contextual(Id::class, IdSerializer)
    @Suppress("UNCHECKED_CAST") // because of the serializers semantics, we know that this will work
    contextual(WrappedId::class, IdSerializer as KSerializer<WrappedId<*>>)
}
