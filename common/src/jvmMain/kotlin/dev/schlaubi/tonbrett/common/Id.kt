package dev.schlaubi.tonbrett.common

import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import org.litote.kmongo.toId

public actual typealias Id<T> = @Serializable(with= IdSerializer::class) org.litote.kmongo.Id<T>

public object IdSerializer : KSerializer<Id<*>> {
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("MongoID", PrimitiveKind.STRING)

    override fun deserialize(decoder: Decoder): Id<*> = decoder.decodeString().toId<Any>()

    override fun serialize(encoder: Encoder, value: Id<*>): Unit = encoder.encodeString(value.toString())
}
