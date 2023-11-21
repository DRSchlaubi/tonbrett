package dev.schlaubi.tonbrett.bot.io

import com.kotlindiscord.kord.extensions.koin.KordExKoinComponent
import com.mongodb.client.model.Collation
import dev.schlaubi.mikbot.plugin.api.io.getCollection
import dev.schlaubi.mikbot.plugin.api.util.database
import dev.schlaubi.stdx.core.isNotNullOrBlank
import dev.schlaubi.tonbrett.common.Snowflake
import dev.schlaubi.tonbrett.common.Sound
import dev.schlaubi.tonbrett.common.SoundGroup
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.serialization.Serializable
import org.bson.conversions.Bson
import org.bson.types.ObjectId
import org.litote.kmongo.*
import org.litote.kmongo.coroutine.CoroutineCollection
import org.litote.kmongo.coroutine.aggregate
import org.litote.kmongo.util.KMongoUtil

object SoundBoardDatabase : KordExKoinComponent {
    val sounds = database.getCollection<Sound>("sounds")
}

suspend fun CoroutineCollection<Sound>.findById(id: String) =
    findOneById(ObjectId(id))

@Serializable
data class Tags(val tags: List<String>)

@OptIn(ExperimentalCoroutinesApi::class)
fun CoroutineCollection<Sound>.findAllTags(query: String? = null, limit: Int? = null): Flow<String> {
    val pipeline = listOfNotNull(
        match(Sound::tag ne null),
        if (query.isNotNullOrBlank()) match(query.toFuzzyFilter("tag")) else null,
        group(null, Tags::tags.addToSet(Sound::tag)),
        project(fields(include(Tags::tags), excludeId()))
    )

    val flow = aggregate<Tags>(pipeline).toFlow()
        .flatMapConcat { it.tags.asFlow() }

    return if (limit != null) {
        flow.take(limit)
    } else {
        flow
    }
}

fun CoroutineCollection<Sound>.search(
    query: String?,
    onlineMine: Boolean,
    user: Snowflake
): Flow<Sound> {
    return find(buildSearchFilter(query, onlineMine, user)).toFlow()
}

fun CoroutineCollection<Sound>.searchGrouped(
    query: String?,
    onlineMine: Boolean,
    user: Snowflake
): Flow<SoundGroup> {
    val withTag = aggregate<SoundGroup>(
        match(buildSearchFilter(query, onlineMine, user)),
        match(not(Sound::tag eq null)),
        group(
            Sound::tag, SoundGroup::sounds.push("$\$ROOT")
        ),
    )
        .collation(Collation.builder().caseLevel(true).locale("en").build())
        .toFlow()
    val noTag = aggregate<SoundGroup>(
        match(buildSearchFilter(query, onlineMine, user)),
        match(Sound::tag eq null),
        group(
            Sound::tag, SoundGroup::sounds.push("$\$ROOT")
        )
    ).toFlow()

    return flow {
        emitAll(withTag)
        emitAll(noTag)
    }
}

private fun buildSearchFilter(query: String?, onlineMine: Boolean, user: Snowflake): Bson {
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

                query.startsWith("name:") -> query.toFuzzyFilter("name", "name:")
                else -> {
                    or(
                        query.toFuzzyFilter("name", "name:"),
                        query.toFuzzyFilter("description", "description:"),
                        query.toFuzzyFilter("tag", "tag:")
                    )
                }
            }

            add(queryFilter)
        }
    }

    return and(filter)
}


private fun String.toFuzzyFilter(name: String) = KMongoUtil.toBson("{$name: /$this/i}")

private fun String.toFuzzyFilter(name: String, prefix: String) = substringAfter(prefix).toFuzzyFilter(name)
