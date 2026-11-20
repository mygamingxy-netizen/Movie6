package com.uragon.tv;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class ContentFragment extends Fragment {

    public static final int TYPE_LIVE = 0;
    public static final int TYPE_MOVIES = 1;
    public static final int TYPE_SERIES = 2;
    public static final int TYPE_FAVORITES = 3;

    private static final String ARG_TYPE = "type";

    private int type;
    private MediaAdapter adapter;
    private View loadingView;
    private View emptyView;

    public static ContentFragment newInstance(int type) {
        ContentFragment f = new ContentFragment();
        Bundle b = new Bundle();
        b.putInt(ARG_TYPE, type);
        f.setArguments(b);
        return f;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) type = getArguments().getInt(ARG_TYPE);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_content, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        RecyclerView rv = view.findViewById(R.id.recyclerView);

        // Grid columns: TV/wider screens get more columns
        int cols = getResources().getConfiguration().smallestScreenWidthDp >= 600 ? 6 : 3;
        rv.setLayoutManager(new GridLayoutManager(getContext(), cols));
        rv.setHasFixedSize(true);

        adapter = new MediaAdapter((item, position) -> openItem(item));
        rv.setAdapter(adapter);

        loadContent();
    }

    private void loadContent() {
        if (type == TYPE_LIVE) {
            SourceManager.fetchAllLive(new SourceManager.Callback<List<MediaItem>>() {
                @Override
                public void onResult(List<MediaItem> result) {
                    updateUI(result);
                }
                @Override
                public void onError(Exception e) {
                    updateUI(new ArrayList<>());
                }
            });
        } else if (type == TYPE_MOVIES) {
            SourceManager.fetchMovies(new SourceManager.Callback<List<MediaItem>>() {
                @Override
                public void onResult(List<MediaItem> result) { updateUI(result); }
                @Override
                public void onError(Exception e) { updateUI(new ArrayList<>()); }
            });
        } else if (type == TYPE_SERIES) {
            SourceManager.fetchSeries(new SourceManager.Callback<List<MediaItem>>() {
                @Override
                public void onResult(List<MediaItem> result) { updateUI(result); }
                @Override
                public void onError(Exception e) { updateUI(new ArrayList<>()); }
            });
        } else if (type == TYPE_FAVORITES) {
            List<MediaItem> favs = FavoritesManager.get(getContext());
            updateUI(favs);
        }
    }

    private void updateUI(List<MediaItem> items) {
        if (getActivity() == null) return;
        getActivity().runOnUiThread(() -> {
            adapter.setItems(items);
        });
    }

    private void openItem(MediaItem item) {
        if (item.getType() == MediaItem.TYPE_LIVE) {
            Intent i = new Intent(getContext(), PlayerActivity.class);
            i.putExtra(PlayerActivity.EXTRA_URL, item.getStreamUrl());
            i.putExtra(PlayerActivity.EXTRA_TITLE, item.getTitle());
            i.putExtra(PlayerActivity.EXTRA_TYPE, PlayerActivity.TYPE_DIRECT);
            startActivity(i);
        } else {
            Intent i = new Intent(getContext(), DetailActivity.class);
            i.putExtra(DetailActivity.EXTRA_ITEM, item);
            startActivity(i);
        }
    }
}
