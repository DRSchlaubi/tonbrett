package dev.schlaubi.tonbrett.common

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonClassDiscriminator

@OptIn(ExperimentalSerializationApi::class)
@JsonClassDiscriminator("type")
@Serializable
public sealed interface Event

@Serializable
@SerialName("interface_availability_change")
public data class InterfaceAvailabilityChangeEvent(val available: Boolean, val playingSongId: Id<Sound>?) : Event

@Serializable
@SerialName("voice_state_update")
public data class VoiceStateUpdateEvent(val voiceState: User.VoiceState?) : Event

