package dev.schlaubi.tonbrett.app.android.shared

import androidx.datastore.core.Serializer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.InputStream
import java.io.OutputStream

const val AUTH_STATE_PATH = "dev.schlaubi.authstate"

object AuthStateSerializer : Serializer<AuthState> {
    override val defaultValue: AuthState = AuthState.getDefaultInstance()
    override suspend fun readFrom(input: InputStream): AuthState = withContext(Dispatchers.IO) {
        AuthState.parseFrom(input)
    }
    override suspend fun writeTo(t: AuthState, output: OutputStream) = withContext(Dispatchers.IO) {
        t.writeTo(output)
    }
}
