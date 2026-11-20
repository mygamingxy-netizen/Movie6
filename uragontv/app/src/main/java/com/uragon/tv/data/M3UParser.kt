package com.uragon.tv.data

import java.util.regex.Pattern

object M3UParser {

    private val EXTINF_PATTERN = Pattern.compile(
        "#EXTINF:(-?\\d+)(.*),(.*)"
    )

    fun parse(content: String, sourceName: String = ""): List<Channel> {
        val channels = mutableListOf<Channel>()
        val lines = content.lines()
        var i = 0
        while (i < lines.size) {
            val line = lines[i].trim()
            if (line.startsWith("#EXTINF")) {
                val matcher = EXTINF_PATTERN.matcher(line)
                if (matcher.matches()) {
                    val attrs = matcher.group(2) ?: ""
                    val title = (matcher.group(3) ?: "").trim()
                    val logo = extractAttr(attrs, "tvg-logo")
                    val tvgId = extractAttr(attrs, "tvg-id")
                    val group = extractAttr(attrs, "group-title").ifBlank { "Uncategorized" }

                    // find the URL on the next non-empty, non-comment line
                    var url = ""
                    var j = i + 1
                    while (j < lines.size) {
                        val next = lines[j].trim()
                        if (next.isNotEmpty() && !next.startsWith("#")) {
                            url = next
                            break
                        }
                        j++
                    }
                    if (url.isNotEmpty()) {
                        channels.add(
                            Channel(
                                title = title.ifBlank { "Untitled" },
                                url = url,
                                logo = logo,
                                group = group,
                                tvgId = tvgId,
                                source = sourceName
                            )
                        )
                        i = j
                    }
                }
            }
            i++
        }
        return channels
    }

    private fun extractAttr(attrs: String, name: String): String {
        val pattern = Pattern.compile("$name=\"([^\"]*)\"", Pattern.CASE_INSENSITIVE)
        val m = pattern.matcher(attrs)
        return if (m.find()) m.group(1) ?: "" else ""
    }
}
