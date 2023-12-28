package dev.schlaubi.tonbrett.common

import com.github.jershell.kbson.BsonEncoder
import com.github.jershell.kbson.BsonFlexibleDecoder
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.JsonDecoder
import kotlinx.serialization.json.JsonEncoder
import org.bson.types.ObjectId
import org.litote.kmongo.id.toId
import org.litote.kmongo.toId


// d8 can't understand k2 meta yet, so we can't use type-aliases
@Serializable(with = IdSerializer::class)
@Suppress("ACTUAL_CLASSIFIER_MUST_HAVE_THE_SAME_SUPERTYPES_AS_NON_FINAL_EXPECT_CLASSIFIER_WARNING")
public actual interface Id<T> : org.litote.kmongo.Id<T>

@Serializable(with = IdSerializer::class)
@JvmInline
@Suppress("SERIALIZER_TYPE_INCOMPATIBLE") // because of the serializers semantics, this is irrelevant
internal value class WrappedId<T>(private val id: org.litote.kmongo.Id<T>) : Id<T>,
    org.litote.kmongo.Id<T> by id {
    override fun toString(): String = id.toString()
}

public fun <T> newId(): Id<T> = WrappedId(org.litote.kmongo.newId())

public object IdSerializer : KSerializer<Id<*>> {
    override val descriptor: SerialDescriptor =
        PrimitiveSerialDescriptor("MongoID", PrimitiveKind.STRING)

    override fun deserialize(decoder: Decoder): Id<*> {
        return if (decoder !is JsonDecoder && decoder is BsonFlexibleDecoder) {
            WrappedId<Any>(decoder.reader.readObjectId().toId())
        } else {
            WrappedId<Any>(decoder.decodeString().toId())
        }
    }

    override fun serialize(encoder: Encoder, value: Id<*>): Unit =
        if (encoder !is JsonEncoder && encoder is BsonEncoder) {
            val objectId = ObjectId(value.toString())
            encoder.encodeObjectId(objectId)
        } else {
            encoder.encodeString(value.toString())
        }
}
