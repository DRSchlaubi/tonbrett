package dev.schlaubi.tonbrett.common

import kotlinx.serialization.Contextual
import org.litote.kmongo.Id

public actual typealias Id<T> = @Contextual Id<T>
