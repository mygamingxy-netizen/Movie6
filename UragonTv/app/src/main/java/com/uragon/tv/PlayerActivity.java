package com.uragon.tv;

import android.annotation.SuppressLint;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.OptIn;
import androidx.appcompat.app.AppCompatActivity;
import androidx.media3.common.Player;
import androidx.media3.common.util.UnstableApi;
import androidx.media3.datasource.DefaultHttpDataSource;
import androidx.media3.exoplayer.ExoPlayer;
import androidx.media3.exoplayer.hls.HlsMediaSource;
import androidx.media3.exoplayer.source.MediaSource;
import androidx.media3.exoplayer.source.ProgressiveMediaSource;
import androidx.media3.ui.PlayerView;

public class PlayerActivity extends AppCompatActivity {

    public static final String EXTRA_URL = "url";
    public static final String EXTRA_TITLE = "title";
    public static final String EXTRA_TYPE = "type";

    public static final int TYPE_DIRECT = 0;
    public static final int TYPE_EMBED = 1;

    private PlayerView playerView;
    private WebView webView;
    private ProgressBar loading;
    private TextView errorText;
    private ExoPlayer player;

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player);

        playerView = findViewById(R.id.playerView);
        webView = findViewById(R.id.webView);
        loading = findViewById(R.id.loading);
        errorText = findViewById(R.id.errorText);

        String url = getIntent().getStringExtra(EXTRA_URL);
        String title = getIntent().getStringExtra(EXTRA_TITLE);
        int type = getIntent().getIntExtra(EXTRA_TYPE, TYPE_DIRECT);

        if (title != null) setTitle(title);

        if (url == null || url.isEmpty()) {
            showError("No stream URL provided");
            return;
        }

        // Auto-detect: direct video URLs use ExoPlayer, embed pages use WebView
        boolean isDirect = type == TYPE_DIRECT && isDirectVideoUrl(url);

        if (isDirect) {
            playDirect(url);
        } else {
            playEmbed(url);
        }
    }

    private boolean isDirectVideoUrl(String url) {
        String lower = url.toLowerCase();
        return lower.endsWith(".m3u8") || lower.endsWith(".mp4") || lower.endsWith(".webm")
                || lower.endsWith(".mkv") || lower.contains(".m3u8?") || lower.contains(".mp4?")
                || lower.endsWith(".ts") || lower.contains("manifest");
    }

    @OptIn(markerClass = UnstableApi.class)
    private void playDirect(String url) {
        playerView.setVisibility(View.VISIBLE);
        loading.setVisibility(View.VISIBLE);

        DefaultHttpDataSource.Factory dataSourceFactory = new DefaultHttpDataSource.Factory()
                .setUserAgent("Mozilla/5.0 (UragonTv) AppleWebKit/537.36")
                .setAllowCrossProtocolRedirects(true);

        Uri uri = Uri.parse(url);
        MediaSource mediaSource;
        if (url.toLowerCase().contains(".m3u8")) {
            mediaSource = new HlsMediaSource.Factory(dataSourceFactory)
                    .createMediaSource(androidx.media3.common.MediaItem.fromUri(uri));
        } else {
            mediaSource = new ProgressiveMediaSource.Factory(dataSourceFactory)
                    .createMediaSource(androidx.media3.common.MediaItem.fromUri(uri));
        }

        player = new ExoPlayer.Builder(this).build();
        playerView.setPlayer(player);
        player.setMediaSource(mediaSource);
        player.prepare();
        player.setPlayWhenReady(true);

        player.addListener(new Player.Listener() {
            @Override
            public void onPlaybackStateChanged(int state) {
                if (state == Player.STATE_READY) {
                    loading.setVisibility(View.GONE);
                } else if (state == Player.STATE_ENDED) {
                    finish();
                }
            }

            @Override
            public void onPlayerError(androidx.media3.common.PlaybackException error) {
                loading.setVisibility(View.GONE);
                showError("Playback failed: " + (error != null ? error.getMessage() : "unknown error"));
            }
        });
    }

    @SuppressLint("SetJavaScriptEnabled")
    private void playEmbed(String url) {
        webView.setVisibility(View.VISIBLE);
        loading.setVisibility(View.VISIBLE);

        WebSettings s = webView.getSettings();
        s.setJavaScriptEnabled(true);
        s.setDomStorageEnabled(true);
        s.setMediaPlaybackRequiresUserGesture(false);
        s.setLoadsImagesAutomatically(true);
        s.setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);
        s.setUserAgentString("Mozilla/5.0 (Linux; Android 12) AppleWebKit/537.36 Chrome/120.0 Mobile");

        webView.setWebChromeClient(new WebChromeClient());
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String u) {
                loading.setVisibility(View.GONE);
            }

            @Override
            public void onReceivedError(WebView view, WebResourceRequest req, WebResourceError er) {
                loading.setVisibility(View.GONE);
            }
        });
        webView.loadUrl(url);
    }

    private void showError(String msg) {
        errorText.setText(msg);
        errorText.setVisibility(View.VISIBLE);
        loading.setVisibility(View.GONE);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (player != null) {
            player.setPlayWhenReady(false);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (player != null) {
            player.release();
            player = null;
        }
        if (webView != null) {
            webView.destroy();
        }
    }
}
