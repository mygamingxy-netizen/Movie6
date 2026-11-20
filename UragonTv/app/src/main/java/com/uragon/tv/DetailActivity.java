package com.uragon.tv;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.List;

public class DetailActivity extends AppCompatActivity {

    public static final String EXTRA_ITEM = "media_item";

    private MediaItem item;
    private int selectedSeason = 1;
    private int selectedEpisode = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        item = (MediaItem) getIntent().getSerializableExtra(EXTRA_ITEM);
        if (item == null) {
            finish();
            return;
        }

        ImageView backdrop = findViewById(R.id.backdrop);
        ImageView poster = findViewById(R.id.poster);
        TextView title = findViewById(R.id.title);
        TextView year = findViewById(R.id.year);
        TextView rating = findViewById(R.id.rating);
        TextView typeLabel = findViewById(R.id.typeLabel);
        TextView description = findViewById(R.id.description);
        Button playBtn = findViewById(R.id.playBtn);
        Button favBtn = findViewById(R.id.favBtn);
        View seriesSection = findViewById(R.id.seriesSection);
        Spinner seasonSpinner = findViewById(R.id.seasonSpinner);
        Spinner episodeSpinner = findViewById(R.id.episodeSpinner);

        title.setText(item.getTitle());
        if (item.getYear() > 0) year.setText(String.valueOf(item.getYear()));
        if (item.getRating() > 0) rating.setText("★ " + item.getRating());
        typeLabel.setText(item.getTypeLabel());
        description.setText(item.getDescription() != null ? item.getDescription() : "No description available.");

        String img = item.getPosterUrl();
        if (img != null && !img.isEmpty()) {
            Glide.with(this).load(img).into(poster);
            Glide.with(this).load(img).centerCrop().into(backdrop);
        }

        // Favorite state
        updateFavButton(favBtn);

        favBtn.setOnClickListener(v -> {
            FavoritesManager.toggle(this, item);
            updateFavButton(favBtn);
        });

        // Series episode selector
        if (item.getType() == MediaItem.TYPE_SERIES) {
            seriesSection.setVisibility(View.VISIBLE);
            List<String> seasons = new ArrayList<>();
            for (int i = 1; i <= 10; i++) seasons.add("Season " + i);
            seasonSpinner.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, seasons));

            seasonSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    selectedSeason = position + 1;
                    updateEpisodes(episodeSpinner);
                }
                @Override
                public void onNothingSelected(AdapterView<?> parent) {}
            });
            updateEpisodes(episodeSpinner);
            episodeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    selectedEpisode = position + 1;
                }
                @Override
                public void onNothingSelected(AdapterView<?> parent) {}
            });
        }

        playBtn.setOnClickListener(v -> play());
    }

    private void updateEpisodes(Spinner episodeSpinner) {
        List<String> episodes = new ArrayList<>();
        for (int i = 1; i <= 24; i++) episodes.add("Episode " + i);
        episodeSpinner.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, episodes));
    }

    private void updateFavButton(Button favBtn) {
        if (FavoritesManager.isFavorite(this, item)) {
            favBtn.setText("★ Saved");
            favBtn.setBackgroundTintList(android.content.res.ColorStateList.valueOf(0xFFFFC107));
            favBtn.setTextColor(0xFF000000);
        } else {
            favBtn.setText("☆ Favorite");
            favBtn.setBackgroundTintList(android.content.res.ColorStateList.valueOf(0xFF1A2236));
            favBtn.setTextColor(0xFFF5F7FA);
        }
    }

    private void play() {
        String url;
        if (item.getType() == MediaItem.TYPE_SERIES) {
            url = SourceManager.seriesStreamUrl(item.getId(), selectedSeason, selectedEpisode);
        } else {
            url = item.getStreamUrl();
        }
        Intent i = new Intent(this, PlayerActivity.class);
        i.putExtra(PlayerActivity.EXTRA_URL, url);
        i.putExtra(PlayerActivity.EXTRA_TITLE, item.getTitle());
        i.putExtra(PlayerActivity.EXTRA_TYPE, PlayerActivity.TYPE_EMBED);
        startActivity(i);
    }
}
