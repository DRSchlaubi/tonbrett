package dev.schlaubi.tonbrett.common

import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import org.litote.kmongo.toId


// d8 can't understand k2 meta yet, so we can't use type-aliases
@Serializable(with = IdSerializer::class)
public actual interface Id<T> : org.litote.kmongo.Id<T>

@JvmInline
private value class WrappedId<T>(private val id: org.litote.kmongo.Id<T>) : Id<T>,
    org.litote.kmongo.Id<T> by id {
    override fun toString(): String = id.toString()
}

public fun <T> newId(): Id<T> = WrappedId(org.litote.kmongo.newId())

public object IdSerializer : KSerializer<Id<*>> {
    override val descriptor: SerialDescriptor =
        PrimitiveSerialDescriptor("MongoID", PrimitiveKind.STRING)

    override fun deserialize(decoder: Decoder): Id<*> =
        WrappedId(decoder.decodeString().toId<Any>())

    override fun serialize(encoder: Encoder, value: Id<*>): Unit =
        encoder.encodeString(value.toString())
}
