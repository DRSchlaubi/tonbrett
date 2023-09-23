package dev.schlaubi.tonbrett.common

import kotlinx.serialization.InternalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.internal.InlinePrimitiveDescriptor

@Serializable(with = Snowflake.Serializer::class)
public actual class Snowflake actual constructor(public actual val value: ULong) {
    override fun toString(): String = value.toString()
    public companion object Serializer : KSerializer<Snowflake> {
        @OptIn(InternalSerializationApi::class)
        override val descriptor: SerialDescriptor = InlinePrimitiveDescriptor("Snowflake", ULong.serializer())

        override fun deserialize(decoder: Decoder): Snowflake =
            Snowflake(decoder.decodeSerializableValue(ULong.serializer()))

        override fun serialize(encoder: Encoder, value: Snowflake): Unit =
            encoder.encodeSerializableValue(ULong.serializer(), value.value)

    }
}
