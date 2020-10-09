package com.atmko.skiptoit.model

import com.google.gson.annotations.SerializedName

class PodcastDetails(
    @SerializedName("id") val id: String,
    @SerializedName("title") val title: String?,
    @SerializedName("publisher") val publisher: String,
    val image: String,
    val description: String?,
    @SerializedName("total_episodes") val totalEpisodes: Int,
    val episodes: List<Episode>,
    @SerializedName("next_episode_pub_date") val nextEpisodePubDate: Long?


) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as PodcastDetails

        if (id != other.id) return false
        if (title != other.title) return false
        if (publisher != other.publisher) return false
        if (image != other.image) return false
        if (description != other.description) return false
        if (totalEpisodes != other.totalEpisodes) return false
        if (episodes != other.episodes) return false
        if (nextEpisodePubDate != other.nextEpisodePubDate) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + (title?.hashCode() ?: 0)
        result = 31 * result + publisher.hashCode()
        result = 31 * result + image.hashCode()
        result = 31 * result + description.hashCode()
        result = 31 * result + totalEpisodes
        result = 31 * result + episodes.hashCode()
        result = 31 * result + (nextEpisodePubDate?.hashCode() ?: 0)
        return result
    }
}