package com.uragon.tv

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.google.android.material.tabs.TabLayout
import com.uragon.tv.data.Channel
import com.uragon.tv.data.PlaylistRepository
import com.uragon.tv.data.Sources
import com.uragon.tv.databinding.ActivityMainBinding
import com.uragon.tv.ui.ContentFragment
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity(),
    ContentFragment.OnChannelPlayListener,
    ContentFragment.OnRefreshListener {

    private lateinit var binding: ActivityMainBinding
    private var allChannels: List<Channel> = emptyList()
    private val fragments = mutableMapOf<String, ContentFragment>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)

        setupTabs()
        loadPlaylists()
    }

    private fun setupTabs() {
        val tabs = listOf(
            ContentFragment.KIND_LIVE to getString(R.string.tab_live),
            ContentFragment.KIND_MOVIES to getString(R.string.tab_movies),
            ContentFragment.KIND_SERIES to getString(R.string.tab_series),
            ContentFragment.KIND_FAVORITES to getString(R.string.tab_favorites),
            "sources" to getString(R.string.tab_settings)
        )
        tabs.forEach { (kind, title) ->
            val tab = binding.tabs.newTab().setText(title)
            tab.tag = kind
            binding.tabs.addTab(tab)
        }

        binding.tabs.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) {
                val tag = tab.tag as String
                if (tag == "sources") {
                    showSources()
                } else {
                    showFragment(tag)
                }
            }
            override fun onTabUnselected(tab: TabLayout.Tab) {}
            override fun onTabReselected(tab: TabLayout.Tab) {}
        })

        // default to live
        showFragment(ContentFragment.KIND_LIVE)
    }

    private fun showFragment(kind: String) {
        val fragment = fragments.getOrPut(kind) { ContentFragment.newInstance(kind) }
        if (allChannels.isNotEmpty()) {
            fragment.setChannels(allChannels)
        }
        supportFragmentManager.beginTransaction()
            .replace(R.id.container, fragment)
            .commitAllowingStateLoss()
    }

    private fun loadPlaylists() {
        fragments.values.forEach { it.showLoading(true) }
        lifecycleScope.launch {
            val status = findViewById<TextView?>(R.id.toolbar)
            val channels = PlaylistRepository.fetchAll(Sources.M3U_SOURCES) { progress ->
                runOnUiThread {
                    binding.toolbar.subtitle = progress
                }
            }
            allChannels = channels
            binding.toolbar.subtitle = "${channels.size} streams ready"
            fragments.values.forEach {
                it.setChannels(channels)
                it.showLoading(false)
            }
            if (channels.isEmpty()) {
                Toast.makeText(
                    this@MainActivity,
                    "Could not load streams. Pull to refresh or check your connection.",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    override fun onRefresh() {
        loadPlaylists()
    }

    override fun onPlay(channel: Channel) {
        val intent = Intent(this, PlayerActivity::class.java).apply {
            putExtra(PlayerActivity.EXTRA_URL, channel.url)
            putExtra(PlayerActivity.EXTRA_TITLE, channel.title)
        }
        startActivity(intent)
    }

    private fun showSources() {
        val msg = buildString {
            append("UragonTv aggregates public IPTV & VOD playlists.\n\n")
            append("M3U Playlists (playable in-app):\n")
            Sources.M3U_SOURCES.forEach { append("• ").append(it.name).append("\n") }
            append("\nKodi Add-on Repos (reference):\n")
            Sources.KODI_REPOS.take(5).forEach { append("• ").append(it.name).append("\n") }
            append("…and ${Sources.KODI_REPOS.size - 5} more.\n")
            append("\nAPI key configured for VOD resolver endpoints.")
        }
        AlertDialog.Builder(this)
            .setTitle(R.string.sources_title)
            .setMessage(msg)
            .setPositiveButton("OK", null)
            .show()
    }
}
