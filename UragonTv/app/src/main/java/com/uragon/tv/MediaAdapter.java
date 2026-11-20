package com.uragon.tv;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;

import java.util.ArrayList;
import java.util.List;

public class MediaAdapter extends RecyclerView.Adapter<MediaAdapter.VH> {

    public interface OnItemClickListener {
        void onItemClick(MediaItem item, int position);
    }

    private List<MediaItem> items = new ArrayList<>();
    private OnItemClickListener listener;

    public MediaAdapter(OnItemClickListener listener) {
        this.listener = listener;
    }

    public void setItems(List<MediaItem> newItems) {
        this.items = newItems != null ? newItems : new ArrayList<>();
        notifyDataSetChanged();
    }

    public List<MediaItem> getItems() {
        return items;
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_media, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int position) {
        MediaItem item = items.get(position);
        h.title.setText(item.getTitle());

        // Subtitle
        String sub = "";
        if (item.getYear() > 0) sub += item.getYear();
        if (item.getGroup() != null && !item.getGroup().isEmpty()) {
            sub += (sub.isEmpty() ? "" : " • ") + item.getGroup();
        }
        if (item.getQuality() != null && !item.getQuality().isEmpty()) {
            sub += (sub.isEmpty() ? "" : " • ") + item.getQuality();
        }
        h.subtitle.setText(sub);
        h.subtitle.setVisibility(sub.isEmpty() ? View.GONE : View.VISIBLE);

        // Type badge
        h.typeBadge.setText(item.getTypeLabel());
        if (item.getType() == MediaItem.TYPE_LIVE) {
            h.typeBadge.setBackgroundResource(R.drawable.badge_bg);
        } else {
            h.typeBadge.setBackgroundColor(0xFF1E88E5);
        }

        // Rating badge
        if (item.getRating() > 0) {
            h.ratingBadge.setText("★ " + item.getRating());
            h.ratingBadge.setVisibility(View.VISIBLE);
        } else {
            h.ratingBadge.setVisibility(View.GONE);
        }

        // Poster / logo
        String img = item.getPosterUrl();
        if (img == null || img.isEmpty()) img = item.getLogo();
        if (img != null && !img.isEmpty()) {
            Glide.with(h.poster.getContext())
                    .load(img)
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .placeholder(R.mipmap.ic_launcher)
                    .error(R.mipmap.ic_launcher)
                    .into(h.poster);
        } else {
            h.poster.setImageResource(R.mipmap.ic_launcher);
        }

        h.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onItemClick(item, h.getAdapterPosition());
        });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class VH extends RecyclerView.ViewHolder {
        ImageView poster;
        TextView title, subtitle, typeBadge, ratingBadge;

        VH(View v) {
            super(v);
            poster = v.findViewById(R.id.poster);
            title = v.findViewById(R.id.title);
            subtitle = v.findViewById(R.id.subtitle);
            typeBadge = v.findViewById(R.id.typeBadge);
            ratingBadge = v.findViewById(R.id.ratingBadge);
        }
    }
}
