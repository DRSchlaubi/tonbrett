package dev.schlaubi.tonbrett.lavalink

import dev.schlaubi.tonbrett.common.Sound
import jakarta.annotation.PreDestroy
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController
import kotlin.coroutines.CoroutineContext

@RestController
class RestHandler(private val soundDownloader: SoundDownloader) : CoroutineScope {

    override val coroutineContext: CoroutineContext = Dispatchers.IO + SupervisorJob()

    @PutMapping(value = ["/v4/sounds"])
    fun downloadSound(@RequestBody sound: Sound): ResponseEntity<Any> = runBlocking {
        soundDownloader.downloadSound(sound)

        ResponseEntity.accepted().build<Any>()
    }

    @DeleteMapping(value = ["/v4/sounds/{id}"])
    fun deleteSound(@PathVariable("id") id: String): ResponseEntity<Any> {
        soundDownloader.deleteSound(id)

        return ResponseEntity.accepted().build()
    }

    @PostMapping(value = ["/v4/sounds/sync"])
    fun syncSounds(): ResponseEntity<Any> {
        launch {
            soundDownloader.syncSounds()
        }

        return ResponseEntity.accepted().build()
    }

    @PreDestroy
    fun close() = cancel()
}
