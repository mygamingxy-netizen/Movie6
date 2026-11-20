package com.uragon.tv.data

data class Channel(
    val title: String,
    val url: String,
    val logo: String? = null,
    val group: String = "Uncategorized",
    val tvgId: String? = null,
    val source: String = ""
) {
    val isVod: Boolean
        get() {
            val g = group.lowercase()
            val u = url.lowercase()
            return g.contains("movie") || g.contains("film") || g.contains("series") ||
                    g.contains("vod") || g.contains("on demand") ||
                    u.endsWith(".mp4") || u.endsWith(".mkv") || u.endsWith(".avi") ||
                    u.contains("/movie/") || u.contains("/series/") || u.contains("/vod/")
        }

    val isSeries: Boolean
        get() {
            val g = group.lowercase()
            return g.contains("series") || g.contains("tv show") || g.contains("show")
        }
}

data class Playlist(
    val name: String,
    val channels: List<Channel>
)
