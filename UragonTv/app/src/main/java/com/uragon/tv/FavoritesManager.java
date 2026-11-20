package com.uragon.tv;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

/**
 * Stores favorite media items in SharedPreferences.
 */
public class FavoritesManager {

    private static final String PREFS = "uragon_favorites";
    private static final String KEY = "favs";

    public static List<MediaItem> get(Context ctx) {
        SharedPreferences sp = ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
        String json = sp.getString(KEY, "[]");
        Type type = new TypeToken<List<MediaItem>>() {}.getType();
        List<MediaItem> list = new Gson().fromJson(json, type);
        return list != null ? list : new ArrayList<>();
    }

    public static void save(Context ctx, List<MediaItem> items) {
        SharedPreferences sp = ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
        sp.edit().putString(KEY, new Gson().toJson(items)).apply();
    }

    public static boolean isFavorite(Context ctx, MediaItem item) {
        List<MediaItem> list = get(ctx);
        for (MediaItem m : list) {
            if (m.getId() != null && m.getId().equals(item.getId())) return true;
            if (m.getStreamUrl() != null && m.getStreamUrl().equals(item.getStreamUrl())) return true;
        }
        return false;
    }

    public static void toggle(Context ctx, MediaItem item) {
        List<MediaItem> list = get(ctx);
        int idx = -1;
        for (int i = 0; i < list.size(); i++) {
            MediaItem m = list.get(i);
            if (m.getId() != null && m.getId().equals(item.getId())) { idx = i; break; }
            if (m.getStreamUrl() != null && m.getStreamUrl().equals(item.getStreamUrl())) { idx = i; break; }
        }
        if (idx >= 0) list.remove(idx);
        else list.add(item);
        save(ctx, list);
    }
}
