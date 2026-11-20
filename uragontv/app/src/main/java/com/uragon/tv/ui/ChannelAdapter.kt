package com.uragon.tv.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.uragon.tv.R
import com.uragon.tv.data.Channel

class ChannelAdapter(
    private val onClick: (Channel) -> Unit,
    private val onFavClick: (Channel) -> Unit,
    private val isFavorite: (Channel) -> Boolean
) : RecyclerView.Adapter<ChannelAdapter.VH>() {

    private var items: List<Channel> = emptyList()

    fun submit(list: List<Channel>) {
        items = list
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.item_channel, parent, false)
        return VH(v)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size

    inner class VH(view: View) : RecyclerView.ViewHolder(view) {
        private val logo: ImageView = view.findViewById(R.id.channel_logo)
        private val title: TextView = view.findViewById(R.id.channel_title)
        private val group: TextView = view.findViewById(R.id.channel_group)
        private val fav: ImageView = view.findViewById(R.id.fav_icon)

        fun bind(ch: Channel) {
            title.text = ch.title
            group.text = ch.group
            if (!ch.logo.isNullOrBlank()) {
                Glide.with(logo.context)
                    .load(ch.logo)
                    .placeholder(R.drawable.ic_launcher)
                    .error(R.drawable.ic_launcher)
                    .into(logo)
            } else {
                logo.setImageResource(R.drawable.ic_launcher)
            }
            fav.setImageResource(
                if (isFavorite(ch)) android.R.drawable.star_big_on
                else android.R.drawable.star_big_off
            )
            itemView.setOnClickListener { onClick(ch) }
            fav.setOnClickListener { onFavClick(ch) }
        }
    }
}
