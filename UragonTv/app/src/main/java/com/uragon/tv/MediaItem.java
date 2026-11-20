package com.uragon.tv;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Unified media model representing a Live TV channel, Movie, or Series episode.
 */
public class MediaItem implements Serializable {

    public static final int TYPE_LIVE = 0;
    public static final int TYPE_MOVIE = 1;
    public static final int TYPE_SERIES = 2;
    public static final int TYPE_EPISODE = 3;

    private String id;
    private String title;
    private String description;
    private String posterUrl;
    private String backdropUrl;
    private String streamUrl;
    private String category;
    private String group;
    private int type;
    private String logo;
    private int year;
    private double rating;
    private String duration;
    private String quality;
    // For series
    private String season;
    private String episode;
    private List<MediaItem> episodes;

    public MediaItem() {
        this.episodes = new ArrayList<>();
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getPosterUrl() { return posterUrl; }
    public void setPosterUrl(String posterUrl) { this.posterUrl = posterUrl; }

    public String getBackdropUrl() { return backdropUrl; }
    public void setBackdropUrl(String backdropUrl) { this.backdropUrl = backdropUrl; }

    public String getStreamUrl() { return streamUrl; }
    public void setStreamUrl(String streamUrl) { this.streamUrl = streamUrl; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getGroup() { return group; }
    public void setGroup(String group) { this.group = group; }

    public int getType() { return type; }
    public void setType(int type) { this.type = type; }

    public String getLogo() { return logo; }
    public void setLogo(String logo) { this.logo = logo; }

    public int getYear() { return year; }
    public void setYear(int year) { this.year = year; }

    public double getRating() { return rating; }
    public void setRating(double rating) { this.rating = rating; }

    public String getDuration() { return duration; }
    public void setDuration(String duration) { this.duration = duration; }

    public String getQuality() { return quality; }
    public void setQuality(String quality) { this.quality = quality; }

    public String getSeason() { return season; }
    public void setSeason(String season) { this.season = season; }

    public String getEpisode() { return episode; }
    public void setEpisode(String episode) { this.episode = episode; }

    public List<MediaItem> getEpisodes() { return episodes; }
    public void setEpisodes(List<MediaItem> episodes) { this.episodes = episodes; }

    public String getTypeLabel() {
        switch (type) {
            case TYPE_LIVE: return "LIVE";
            case TYPE_MOVIE: return "MOVIE";
            case TYPE_SERIES: return "SERIES";
            case TYPE_EPISODE: return "EPISODE";
            default: return "";
        }
    }
}
