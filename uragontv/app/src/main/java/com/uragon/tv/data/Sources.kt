package com.uragon.tv.data

object Sources {

    // API key provided by admin (used for VOD resolver endpoints)
    const val API_KEY = "w7gDGCa6du4uvcNO5K8Vs4puM13C0yuS"

    /**
     * Curated, deduplicated playlist sources.
     * Duplicates from the original list have been merged.
     * Kodi add-on repositories are listed separately as they require a Kodi runtime.
     */
    data class Source(val name: String, val url: String, val kind: Kind) {
        enum class Kind { M3U, KODI_REPO, VOD_API }
    }

    // Direct M3U playlists (playable inside UragonTv)
    val M3U_SOURCES = listOf(
        Source("IPTV-Org (All)", "https://iptv-org.github.io/iptv/index.m3u", Source.Kind.M3U),
        Source("IPTV-Org · Movies", "https://iptv-org.github.io/iptv/categories/movies.m3u", Source.Kind.M3U),
        Source("IPTV-Org · News", "https://iptv-org.github.io/iptv/categories/news.m3u", Source.Kind.M3U),
        Source("IPTV-Org · Documentary", "https://iptv-org.github.io/iptv/categories/documentary.m3u", Source.Kind.M3U),
        Source("IPTV-Org · Music", "https://iptv-org.github.io/iptv/categories/music.m3u", Source.Kind.M3U),
        Source("IPTV-Org · Comedy", "https://iptv-org.github.io/iptv/categories/comedy.m3u", Source.Kind.M3U),
        Source("IPTV-Org · USA", "https://iptv-org.github.io/iptv/countries/us.m3u", Source.Kind.M3U),
        Source("Tubi (All)", "https://raw.githubusercontent.com/BuddyChewChew/app-m3u-generator/refs/heads/main/playlists/tubi_all.m3u", Source.Kind.M3U),
        Source("Rakuten TV (UK)", "https://www.apsattv.com/rakutentv-uk.m3u", Source.Kind.M3U),
        Source("Rakuten TV (FR)", "https://www.apsattv.com/rakutentv-fr.m3u", Source.Kind.M3U),
        Source("Xiaomi", "https://www.apsattv.com/xiaomi.m3u", Source.Kind.M3U),
    )

    // Kodi add-on repositories (external; reference only)
    val KODI_REPOS = listOf(
        Source("Magnetic Repo", "https://magnetic.website/repo/", Source.Kind.KODI_REPO),
        Source("SlyGuy", "https://k.slyguy.xyz/", Source.Kind.KODI_REPO),
        Source("SlyGuy (mirror)", "https://slyguy.uk/", Source.Kind.KODI_REPO),
        Source("Bugatsinho", "https://bugatsinho.github.io/repo/", Source.Kind.KODI_REPO),
        Source("The Loop", "https://loopaddon.uk/theloop/", Source.Kind.KODI_REPO),
        Source("The L48", "https://tinyurl.com/TheL48", Source.Kind.KODI_REPO),
        Source("The L48 (mirror)", "https://www.midian.appboxes.co/wolfyB/Th3L48/repo/", Source.Kind.KODI_REPO),
        Source("Team Crew", "https://team-crew.github.io/", Source.Kind.KODI_REPO),
        Source("My Lost Soul", "https://mylostsoulspace.co.uk/repo/", Source.Kind.KODI_REPO),
        Source("Elementum", "https://elementum.surge.sh", Source.Kind.KODI_REPO),
        Source("CocoJoe", "https://cocojoe2411.github.io/", Source.Kind.KODI_REPO),
        Source("Umbrella", "https://umbrellaplug.github.io/", Source.Kind.KODI_REPO),
        Source("Hooty", "https://goldenfreddy0703.github.io/repository.hooty/", Source.Kind.KODI_REPO),
        Source("Angelitto", "https://angelitto2005.github.io/repository.angelitto/", Source.Kind.KODI_REPO),
    )

    // Source code repositories (reference / credits)
    val CODE_REPOS = listOf(
        "https://github.com/TorhamDev/Better-Movie-finder",
        "https://github.com/iptv-org/iptv",
        "https://github.com/truelockmc/streambert",
        "https://github.com/matjava/xtream-playlist",
        "https://github.com/Davidona/StreamVault-IPTV",
        "https://github.com/IMDb-API/IMDbApiLib",
        "https://github.com/xrce/mls",
        "https://github.com/tuhinpal/imdb-api",
        "https://github.com/mesamirh/MovieBox-Tui",
        "https://github.com/Uablebot/FileStore-And-LinkShortner-Bot",
        "https://github.com/P0SSIBLE-0/moviemax",
        "https://github.com/meetkool/Nightflix",
        "https://github.com/solo12345689/moviebox-internal-api",
    )
}
