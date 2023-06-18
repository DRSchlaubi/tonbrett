package dev.schlaubi.tonbrett.common

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonClassDiscriminator

@OptIn(ExperimentalSerializationApi::class)
@JsonClassDiscriminator("type")
@Serializable
public sealed interface Event

public sealed interface HasSound {
    public val sound: Sound

    public fun withSound(sound: Sound): Event
}

@Serializable
@SerialName("interface_availability_change")
public data class InterfaceAvailabilityChangeEvent(val available: Boolean, val playingSongId: Id<Sound>?) : Event

@Serializable
@SerialName("voice_state_update")
public data class VoiceStateUpdateEvent(val voiceState: User.VoiceState?) : Event

@Serializable
@SerialName("sound_create")
public data class SoundCreatedEvent(override val sound: Sound) : Event, HasSound {
    override fun withSound(sound: Sound): Event = copy(sound = sound)
}

@Serializable
@SerialName("sound_delete")
public data class SoundDeletedEvent(val id: Id<Sound>) : Event

@Serializable
@SerialName("sound_update")
public data class SoundUpdatedEvent(override val sound: Sound) : Event, HasSound {
    override fun withSound(sound: Sound): Event = copy(sound = sound)
}
