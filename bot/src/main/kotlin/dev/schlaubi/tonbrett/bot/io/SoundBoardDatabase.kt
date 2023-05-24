package dev.schlaubi.tonbrett.bot.io

import com.kotlindiscord.kord.extensions.koin.KordExKoinComponent
import dev.schlaubi.mikbot.plugin.api.io.getCollection
import dev.schlaubi.mikbot.plugin.api.util.database
import dev.schlaubi.stdx.core.isNotNullOrBlank
import dev.schlaubi.tonbrett.common.Snowflake
import dev.schlaubi.tonbrett.common.Sound
import kotlinx.coroutines.flow.Flow
import org.bson.types.ObjectId
import org.litote.kmongo.*
import org.litote.kmongo.coroutine.CoroutineCollection
import org.litote.kmongo.util.KMongoUtil

object SoundBoardDatabase : KordExKoinComponent {
    val sounds = database.getCollection<Sound>("sounds")
}

suspend fun CoroutineCollection<Sound>.findById(id: String) =
    findOneById(ObjectId(id))

data class Tag(val tag: String)

fun CoroutineCollection<Sound>.findAllTags(query: String? = null): Flow<Tag> {
    val pipeline = listOfNotNull(
        match(Sound::tag ne null),
        group(Tag::tag addToSet Sound::tag),
        if (query.isNotNullOrBlank()) match(query.toFuzzyFilter("tag")) else null
    )

    return aggregate<Tag>(pipeline).toFlow()
}

fun CoroutineCollection<Sound>.search(
    query: String?,
    onlineMine: Boolean,
    user: Snowflake
): Flow<Sound> {
    val filter = buildList {
        add(or(Sound::public eq true, Sound::owner eq user))
        if (onlineMine) {
            add(Sound::owner eq user)
        }
        if (query.isNotNullOrBlank()) {
            val queryFilter = when {
                query.startsWith("tag:") -> query.toFuzzyFilter("tag", "tag:")
                query.startsWith("description:") ->
                    query.toFuzzyFilter("description", "description:")
                else -> {
                    or(
                        query.toFuzzyFilter("name", query),
                        query.toFuzzyFilter("description", query),
                        query.toFuzzyFilter("tag", query),
                    )
                }
            }

            add(queryFilter)
        }
    }

    return find(and(filter)).toFlow()
}


private fun String.toFuzzyFilter(name: String) = KMongoUtil.toBson("{$name: /$this/i}")

private fun String.toFuzzyFilter(name: String, prefix: String) = substringAfter(prefix).toFuzzyFilter(name)
