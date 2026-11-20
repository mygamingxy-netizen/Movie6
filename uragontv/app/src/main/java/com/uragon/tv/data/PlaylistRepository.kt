package com.uragon.tv.data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.util.concurrent.TimeUnit

object PlaylistRepository {

    private val client: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(20, TimeUnit.SECONDS)
            .followRedirects(true)
            .followSslRedirects(true)
            .build()
    }

    suspend fun fetchAll(
        sources: List<Sources.Source> = Sources.M3U_SOURCES,
        onProgress: ((String) -> Unit)? = null
    ): List<Channel> = withContext(Dispatchers.IO) {
        sources.map { src ->
            async {
                try {
                    onProgress?.invoke("Loading ${src.name}…")
                    val body = fetch(src.url)
                    if (body != null) {
                        M3UParser.parse(body, src.name)
                    } else {
                        emptyList()
                    }
                } catch (e: Exception) {
                    emptyList()
                }
            }
        }.awaitAll().flatten().distinctBy { it.url }
    }

    private fun fetch(url: String): String? {
        return try {
            val request = Request.Builder()
                .url(url)
                .header("User-Agent", "Mozilla/5.0 (UragonTv) AppleWebKit/537.36")
                .build()
            client.newCall(request).execute().use { response ->
                if (response.isSuccessful) response.body?.string() else null
            }
        } catch (e: Exception) {
            null
        }
    }
}
