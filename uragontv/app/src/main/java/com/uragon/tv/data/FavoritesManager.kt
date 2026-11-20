package com.uragon.tv.data

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject

class FavoritesManager(context: Context) {

    private val prefs = context.getSharedPreferences("uragon_favorites", Context.MODE_PRIVATE)

    fun getFavorites(): List<Channel> {
        val raw = prefs.getString("favs", "[]") ?: "[]"
        val arr = JSONArray(raw)
        val list = mutableListOf<Channel>()
        for (i in 0 until arr.length()) {
            val o = arr.getJSONObject(i)
            list.add(
                Channel(
                    title = o.optString("title"),
                    url = o.optString("url"),
                    logo = o.optString("logo"),
                    group = o.optString("group", "Uncategorized"),
                    tvgId = o.optString("tvgId"),
                    source = o.optString("source")
                )
            )
        }
        return list
    }

    fun isFavorite(url: String): Boolean {
        return getFavorites().any { it.url == url }
    }

    fun toggle(channel: Channel): Boolean {
        val favs = getFavorites().toMutableList()
        val existing = favs.indexOfFirst { it.url == channel.url }
        return if (existing >= 0) {
            favs.removeAt(existing)
            save(favs)
            false
        } else {
            favs.add(channel)
            save(favs)
            true
        }
    }

    private fun save(list: List<Channel>) {
        val arr = JSONArray()
        list.forEach { c ->
            val o = JSONObject()
            o.put("title", c.title)
            o.put("url", c.url)
            o.put("logo", c.logo ?: "")
            o.put("group", c.group)
            o.put("tvgId", c.tvgId ?: "")
            o.put("source", c.source)
            arr.put(o)
        }
        prefs.edit().putString("favs", arr.toString()).apply()
    }
}
