package com.uragon.tv;

import java.util.ArrayList;
import java.util.List;

/**
 * Parses M3U / M3U8 playlist text into a list of MediaItem.
 */
public class M3UParser {

    public static List<MediaItem> parse(String content, String defaultGroup) {
        List<MediaItem> items = new ArrayList<>();
        if (content == null || content.isEmpty()) return items;

        String[] lines = content.split("\\r?\\n");
        MediaItem current = null;

        for (String rawLine : lines) {
            String line = rawLine.trim();
            if (line.isEmpty()) continue;

            if (line.startsWith("#EXTINF")) {
                current = new MediaItem();
                current.setType(MediaItem.TYPE_LIVE);
                // Extract group-title
                String group = extractAttribute(line, "group-title");
                if (group == null || group.isEmpty()) group = defaultGroup;
                current.setGroup(group);
                current.setCategory(group);

                // Extract tvg-logo
                String logo = extractAttribute(line, "tvg-logo");
                if (logo != null && !logo.isEmpty()) {
                    current.setLogo(logo);
                    current.setPosterUrl(logo);
                }

                // Extract tvg-id
                String tvgId = extractAttribute(line, "tvg-id");
                if (tvgId != null) current.setId(tvgId);

                // Title after the last comma
                int comma = line.lastIndexOf(',');
                if (comma >= 0 && comma < line.length() - 1) {
                    current.setTitle(line.substring(comma + 1).trim());
                } else {
                    current.setTitle("Channel");
                }
            } else if (!line.startsWith("#") && current != null) {
                current.setStreamUrl(line);
                if (current.getId() == null) {
                    current.setId(String.valueOf(items.size()));
                }
                items.add(current);
                current = null;
            }
        }
        return items;
    }

    private static String extractAttribute(String line, String attr) {
        String key = attr + "=\"";
        int start = line.indexOf(key);
        if (start < 0) return null;
        start += key.length();
        int end = line.indexOf('"', start);
        if (end < 0) return null;
        return line.substring(start, end);
    }
}
