package dev.schlaubi.tonbrett.common

import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

@Serializable(with = IdSerializer::class)
public actual interface Id<T>

@Serializable
private value class ActualId<T>(val value: String) : Id<T>

public class IdSerializer<T>(childSerializer: KSerializer<T>) : KSerializer<Id<T>> {
    private val parent = ActualId.serializer(childSerializer)
    override val descriptor: SerialDescriptor
        get() = parent.descriptor

    override fun serialize(encoder: Encoder, value: Id<T>) {
        encoder.encodeInline(descriptor).encodeSerializableValue(parent, (value as ActualId<T>))
    }

    override fun deserialize(decoder: Decoder): Id<T> = decoder.decodeInline(descriptor).decodeSerializableValue(parent)
}
