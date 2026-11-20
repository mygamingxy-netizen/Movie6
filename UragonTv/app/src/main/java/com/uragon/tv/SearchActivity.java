package com.uragon.tv;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class SearchActivity extends AppCompatActivity {

    private MediaAdapter adapter;
    private ProgressBar loading;
    private TextView emptyText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        String query = getIntent().getStringExtra("query");
        setTitle("Search: " + query);

        RecyclerView rv = findViewById(R.id.recyclerView);
        loading = findViewById(R.id.loading);
        emptyText = findViewById(R.id.emptyText);

        int cols = getResources().getConfiguration().smallestScreenWidthDp >= 600 ? 6 : 3;
        rv.setLayoutManager(new GridLayoutManager(this, cols));
        adapter = new MediaAdapter((item, position) -> {
            Intent i = new Intent(this, DetailActivity.class);
            i.putExtra(DetailActivity.EXTRA_ITEM, item);
            startActivity(i);
        });
        rv.setAdapter(adapter);

        loading.setVisibility(View.VISIBLE);
        SourceManager.search(query, new SourceManager.Callback<List<MediaItem>>() {
            @Override
            public void onResult(List<MediaItem> result) {
                runOnUiThread(() -> {
                    loading.setVisibility(View.GONE);
                    adapter.setItems(result);
                    emptyText.setVisibility(result.isEmpty() ? View.VISIBLE : View.GONE);
                });
            }

            @Override
            public void onError(Exception e) {
                runOnUiThread(() -> {
                    loading.setVisibility(View.GONE);
                    emptyText.setVisibility(View.VISIBLE);
                });
            }
        });
    }
}
