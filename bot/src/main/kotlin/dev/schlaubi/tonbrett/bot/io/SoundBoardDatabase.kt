package dev.schlaubi.tonbrett.bot.io

import com.kotlindiscord.kord.extensions.koin.KordExKoinComponent
import dev.schlaubi.mikbot.plugin.api.io.getCollection
import dev.schlaubi.mikbot.plugin.api.util.database
import dev.schlaubi.tonbrett.common.Sound
import org.bson.types.ObjectId
import org.litote.kmongo.coroutine.CoroutineCollection

object SoundBoardDatabase : KordExKoinComponent {
    val sounds = database.getCollection<Sound>("sounds")
}

suspend fun CoroutineCollection<Sound>.findById(id: String) =
    findOneById(ObjectId(id))
