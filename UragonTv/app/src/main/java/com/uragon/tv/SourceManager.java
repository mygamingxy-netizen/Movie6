package com.uragon.tv;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Central manager for all content sources (IPTV live, Movies, Series).
 * Deduplicates sources and provides playable streams.
 */
public class SourceManager {

    private static final String TAG = "SourceManager";

    // IMDb API key (provided)
    private static final String IMDB_KEY = BuildConfig.API_KEY;

    // Deduplicated IPTV M3U sources (Live TV + VOD channels)
    public static final String[][] IPTV_SOURCES = {
            {"All Channels", "https://iptv-org.github.io/iptv/index.m3u"},
            {"Movies", "https://iptv-org.github.io/iptv/categories/movies.m3u"},
            {"News", "https://iptv-org.github.io/iptv/categories/news.m3u"},
            {"Documentary", "https://iptv-org.github.io/iptv/categories/documentary.m3u"},
            {"Music", "https://iptv-org.github.io/iptv/categories/music.m3u"},
            {"Comedy", "https://iptv-org.github.io/iptv/categories/comedy.m3u"},
            {"United States", "https://iptv-org.github.io/iptv/countries/us.m3u"},
            {"Tubi VOD", "https://raw.githubusercontent.com/BuddyChewChew/app-m3u-generator/refs/heads/main/playlists/tubi_all.m3u"},
            {"Rakuten UK", "https://www.apsattv.com/rakutentv-uk.m3u"},
            {"Rakuten FR", "https://www.apsattv.com/rakutentv-fr.m3u"},
            {"Xiaomi", "https://www.apsattv.com/xiaomi.m3u"},
    };

    // VOD embed providers for movies & series (playable via WebView)
    public static final String[] VOD_PROVIDERS = {
            "https://vidsrc.to/embed/movie/%s",
            "https://vidsrc.xyz/embed/movie/%s",
            "https://multiembed.mov/?video_id=%s&tmdb=1",
    };

    public static final String[] SERIES_PROVIDERS = {
            "https://vidsrc.to/embed/tv/%s/%s/%s",
            "https://vidsrc.xyz/embed/tv/%s/%s/%s",
            "https://multiembed.mov/?video_id=%s&tmdb=1&s=%s&e=%s",
    };

    public interface Callback<T> {
        void onResult(T result);
        void onError(Exception e);
    }

    /** Fetch a single M3U playlist and return parsed items. */
    public static void fetchPlaylist(final String name, final String url, final Callback<List<MediaItem>> cb) {
        new Thread(() -> {
            try {
                String content = NetworkClient.get(url);
                List<MediaItem> items = M3UParser.parse(content, name);
                cb.onResult(items);
            } catch (Exception e) {
                Log.e(TAG, "Failed playlist: " + url, e);
                cb.onError(e);
            }
        }).start();
    }

    /** Fetch all IPTV playlists and merge into a deduplicated list. */
    public static void fetchAllLive(final Callback<List<MediaItem>> cb) {
        new Thread(() -> {
            List<MediaItem> all = new ArrayList<>();
            for (String[] src : IPTV_SOURCES) {
                try {
                    String content = NetworkClient.get(src[1]);
                    List<MediaItem> items = M3UParser.parse(content, src[0]);
                    all.addAll(items);
                } catch (Exception e) {
                    Log.w(TAG, "Skipping " + src[0] + ": " + e.getMessage());
                }
            }
            // Deduplicate by stream URL
            Map<String, MediaItem> unique = new LinkedHashMap<>();
            for (MediaItem item : all) {
                if (item.getStreamUrl() != null && !unique.containsKey(item.getStreamUrl())) {
                    unique.put(item.getStreamUrl(), item);
                }
            }
            cb.onResult(new ArrayList<>(unique.values()));
        }).start();
    }

    /**
     * Fetch popular movies from IMDb API. The API key is used to retrieve
     * metadata (poster, title, year, rating). Playback uses VOD embeds.
     */
    public static void fetchMovies(final Callback<List<MediaItem>> cb) {
        new Thread(() -> {
            try {
                List<MediaItem> movies = new ArrayList<>();
                // Try IMDb API popular movies endpoint
                String url = "https://imdb-api.com/en/API/MostPopularMovies/" + IMDB_KEY;
                String json = NetworkClient.get(url);
                JSONObject obj = new JSONObject(json);
                JSONArray items = obj.optJSONArray("items");
                if (items != null) {
                    int max = Math.min(items.length(), 60);
                    for (int i = 0; i < max; i++) {
                        JSONObject m = items.getJSONObject(i);
                        MediaItem item = new MediaItem();
                        item.setType(MediaItem.TYPE_MOVIE);
                        item.setId(m.optString("id"));
                        item.setTitle(m.optString("title"));
                        item.setPosterUrl(m.optString("image"));
                        item.setYear(parseIntSafe(m.optString("year")));
                        item.setRating(parseDoubleSafe(m.optString("imDbRating")));
                        item.setDescription(m.optString("description"));
                        item.setStreamUrl(String.format(VOD_PROVIDERS[0], item.getId()));
                        movies.add(item);
                    }
                }
                if (movies.isEmpty()) {
                    movies = fallbackMovies();
                }
                cb.onResult(movies);
            } catch (Exception e) {
                Log.e(TAG, "Movies fetch failed", e);
                cb.onResult(fallbackMovies());
            }
        }).start();
    }

    /** Fetch popular TV series from IMDb API. */
    public static void fetchSeries(final Callback<List<MediaItem>> cb) {
        new Thread(() -> {
            try {
                List<MediaItem> series = new ArrayList<>();
                String url = "https://imdb-api.com/en/API/MostPopularTVs/" + IMDB_KEY;
                String json = NetworkClient.get(url);
                JSONObject obj = new JSONObject(json);
                JSONArray items = obj.optJSONArray("items");
                if (items != null) {
                    int max = Math.min(items.length(), 60);
                    for (int i = 0; i < max; i++) {
                        JSONObject s = items.getJSONObject(i);
                        MediaItem item = new MediaItem();
                        item.setType(MediaItem.TYPE_SERIES);
                        item.setId(s.optString("id"));
                        item.setTitle(s.optString("title"));
                        item.setPosterUrl(s.optString("image"));
                        item.setYear(parseIntSafe(s.optString("year")));
                        item.setRating(parseDoubleSafe(s.optString("imDbRating")));
                        item.setDescription(s.optString("description"));
                        // Series playback URL template filled at play time
                        item.setStreamUrl(String.format(SERIES_PROVIDERS[0], item.getId(), "1", "1"));
                        series.add(item);
                    }
                }
                if (series.isEmpty()) {
                    series = fallbackSeries();
                }
                cb.onResult(series);
            } catch (Exception e) {
                Log.e(TAG, "Series fetch failed", e);
                cb.onResult(fallbackSeries());
            }
        }).start();
    }

    /** Search IMDb for movies/series by title. */
    public static void search(final String query, final Callback<List<MediaItem>> cb) {
        new Thread(() -> {
            try {
                List<MediaItem> results = new ArrayList<>();
                String url = "https://imdb-api.com/en/API/SearchAll/" + IMDB_KEY + "/" + query.replace(" ", "%20");
                String json = NetworkClient.get(url);
                JSONObject obj = new JSONObject(json);
                JSONArray items = obj.optJSONArray("results");
                if (items != null) {
                    for (int i = 0; i < items.length(); i++) {
                        JSONObject r = items.getJSONObject(i);
                        String type = r.optString("titleType", "");
                        MediaItem item = new MediaItem();
                        item.setId(r.optString("id"));
                        item.setTitle(r.optString("title"));
                        item.setPosterUrl(r.optString("image"));
                        item.setYear(parseIntSafe(r.optString("description")));
                        if (type.contains("series") || type.contains("tv")) {
                            item.setType(MediaItem.TYPE_SERIES);
                            item.setStreamUrl(String.format(SERIES_PROVIDERS[0], item.getId(), "1", "1"));
                        } else {
                            item.setType(MediaItem.TYPE_MOVIE);
                            item.setStreamUrl(String.format(VOD_PROVIDERS[0], item.getId()));
                        }
                        results.add(item);
                    }
                }
                cb.onResult(results);
            } catch (Exception e) {
                Log.e(TAG, "Search failed", e);
                cb.onError(e);
            }
        }).start();
    }

    private static List<MediaItem> fallbackMovies() {
        List<MediaItem> list = new ArrayList<>();
        String[][] data = {
                {"tt1375666", "Inception", "2010"},
                {"tt0133093", "The Matrix", "1999"},
                {"tt0468569", "The Dark Knight", "2008"},
                {"tt0111161", "The Shawshank Redemption", "1994"},
                {"tt0110912", "Pulp Fiction", "1994"},
                {"tt0816692", "Interstellar", "2014"},
                {"tt0120737", "The Lord of the Rings: The Fellowship of the Ring", "2001"},
                {"tt0068646", "The Godfather", "1972"},
                {"tt0118799", "Life Is Beautiful", "1997"},
                {"tt0109830", "Forrest Gump", "1994"},
                {"tt1853728", "Django Unchained", "2012"},
                {"tt0114369", "Se7en", "1995"},
        };
        for (String[] d : data) {
            MediaItem m = new MediaItem();
            m.setType(MediaItem.TYPE_MOVIE);
            m.setId(d[0]);
            m.setTitle(d[1]);
            m.setYear(Integer.parseInt(d[2]));
            m.setStreamUrl(String.format(VOD_PROVIDERS[0], d[0]));
            list.add(m);
        }
        return list;
    }

    private static List<MediaItem> fallbackSeries() {
        List<MediaItem> list = new ArrayList<>();
        String[][] data = {
                {"tt0903747", "Breaking Bad", "2008"},
                {"tt4574334", "Stranger Things", "2016"},
                {"tt0944947", "Game of Thrones", "2011"},
                {"tt0436992", "Doctor Who", "2005"},
                {"tt0898266", "The Big Bang Theory", "2007"},
                {"tt1475582", "Sherlock", "2010"},
                {"tt0413573", "Grey's Anatomy", "2005"},
                {"tt2467372", "Brooklyn Nine-Nine", "2013"},
                {"tt2661044", "The 100", "2014"},
                {"tt0460649", "How I Met Your Mother", "2005"},
        };
        for (String[] d : data) {
            MediaItem s = new MediaItem();
            s.setType(MediaItem.TYPE_SERIES);
            s.setId(d[0]);
            s.setTitle(d[1]);
            s.setYear(Integer.parseInt(d[2]));
            s.setStreamUrl(String.format(SERIES_PROVIDERS[0], d[0], "1", "1"));
            list.add(s);
        }
        return list;
    }

    private static int parseIntSafe(String s) {
        if (s == null) return 0;
        try { return Integer.parseInt(s.trim().replaceAll("[^0-9]", "")); }
        catch (Exception e) { return 0; }
    }

    private static double parseDoubleSafe(String s) {
        if (s == null) return 0;
        try { return Double.parseDouble(s.trim()); }
        catch (Exception e) { return 0; }
    }

    /** Build a playable embed URL for a movie. */
    public static String movieStreamUrl(String imdbId) {
        return String.format(VOD_PROVIDERS[0], imdbId);
    }

    /** Build a playable embed URL for a series episode. */
    public static String seriesStreamUrl(String imdbId, int season, int episode) {
        return String.format(SERIES_PROVIDERS[0], imdbId, season, episode);
    }

    /** Group items by their group/category. */
    public static Map<String, List<MediaItem>> groupByCategory(List<MediaItem> items) {
        Map<String, List<MediaItem>> map = new LinkedHashMap<>();
        for (MediaItem item : items) {
            String key = item.getGroup() != null ? item.getGroup() : "Other";
            if (!map.containsKey(key)) map.put(key, new ArrayList<>());
            map.get(key).add(item);
        }
        return map;
    }
}
