package com.uragon.tv

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.datasource.DefaultHttpDataSource
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
import com.uragon.tv.databinding.ActivityPlayerBinding

class PlayerActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_URL = "extra_url"
        const val EXTRA_TITLE = "extra_title"
    }

    private lateinit var binding: ActivityPlayerBinding
    private var player: ExoPlayer? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPlayerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val url = intent.getStringExtra(EXTRA_URL) ?: return
        val title = intent.getStringExtra(EXTRA_TITLE) ?: "UragonTv"
        binding.playerView.setControllerAutoShow(true)

        initializePlayer(url, title)
    }

    private fun initializePlayer(url: String, title: String) {
        binding.progress.visibility = View.VISIBLE

        val dataSourceFactory = DefaultHttpDataSource.Factory()
            .setUserAgent("Mozilla/5.0 (UragonTv) AppleWebKit/537.36")
            .setAllowCrossProtocolRedirects(true)

        val mediaSourceFactory = DefaultMediaSourceFactory(this)
            .setDataSourceFactory(dataSourceFactory)

        player = ExoPlayer.Builder(this)
            .setMediaSourceFactory(mediaSourceFactory)
            .build()
            .also { exo ->
                binding.playerView.player = exo
                val mediaItem = MediaItem.Builder()
                    .setUri(url)
                    .build()
                exo.setMediaItem(mediaItem)
                exo.prepare()
                exo.playWhenReady = true

                exo.addListener(object : Player.Listener {
                    override fun onPlaybackStateChanged(state: Int) {
                        when (state) {
                            Player.STATE_READY -> binding.progress.visibility = View.GONE
                            Player.STATE_ENDED -> finish()
                            Player.STATE_BUFFERING -> binding.progress.visibility = View.VISIBLE
                            else -> {}
                        }
                    }

                    override fun onPlayerError(error: PlaybackException) {
                        binding.progress.visibility = View.GONE
                        binding.errorText.visibility = View.VISIBLE
                        binding.errorText.text = getString(R.string.play_error)
                    }
                })
            }

        binding.playerView.contentDescription = title
    }

    override fun onStop() {
        super.onStop()
        releasePlayer()
    }

    override fun onDestroy() {
        super.onDestroy()
        releasePlayer()
    }

    private fun releasePlayer() {
        player?.release()
        player = null
    }
}
