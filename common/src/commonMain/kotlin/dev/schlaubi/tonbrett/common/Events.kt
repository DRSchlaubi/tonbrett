package dev.schlaubi.tonbrett.common

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonClassDiscriminator

/**
 * Base class for all events.
 */
@OptIn(ExperimentalSerializationApi::class)
@JsonClassDiscriminator("type")
@Serializable
public sealed interface Event

/**
 * Marker interface for events with [sounds][Sound].
 *
 * @property sound the [Sound]
 */
public sealed interface HasSound {
    public val sound: Sound

    /**
     * Creates a copy of this event with [sound]
     */
    public fun withSound(sound: Sound): Event
}

/**
 * Event indicating the player availability changed.
 *
 * @property available whether the player is now locked or not
 * @property playingSongId the id of the sound which is currently playing if any
 * @property canBeStopped whether the current sound can be stopped
 */
@Serializable
@SerialName("interface_availability_change")
public data class InterfaceAvailabilityChangeEvent(
    val available: Boolean,
    val playingSongId: Id<Sound>?,
    val canBeStopped: Boolean
) : Event

/**
 * Event indicating that the current users voice state has updated.
 *
 * @property voiceState the new voice state or `null` if the user disconnected
 */
@Serializable
@SerialName("voice_state_update")
public data class VoiceStateUpdateEvent(val voiceState: User.VoiceState?) : Event

/**
 * Event indicating that [sound] has been created.
 */
@Serializable
@SerialName("sound_create")
public data class SoundCreatedEvent(override val sound: Sound) : Event, HasSound {
    override fun withSound(sound: Sound): Event = copy(sound = sound)
}

/**
 * Event indicating that the sound with [id] got deleted.
 */
@Serializable
@SerialName("sound_delete")
public data class SoundDeletedEvent(val id: Id<Sound>) : Event

/**
 * Event indicating that [sound] has been updated.
 *
 * The [sound] property represents the updated sound, use [Sound.id] to match it against the current state
 */
@Serializable
@SerialName("sound_update")
public data class SoundUpdatedEvent(override val sound: Sound) : Event, HasSound {
    override fun withSound(sound: Sound): Event = copy(sound = sound)
}
