package com.uragon.tv.ui

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.uragon.tv.R
import com.uragon.tv.data.Channel
import com.uragon.tv.data.FavoritesManager
import com.uragon.tv.databinding.FragmentContentBinding

class ContentFragment : Fragment() {

    private var _binding: FragmentContentBinding? = null
    private val binding get() = _binding!!

    private lateinit var adapter: ChannelAdapter
    private lateinit var favs: FavoritesManager

    private var allChannels: List<Channel> = emptyList()
    private var currentGroup: String = "All"
    private var searchQuery: String = ""

    companion object {
        const val ARG_KIND = "kind"
        const val KIND_LIVE = "live"
        const val KIND_MOVIES = "movies"
        const val KIND_SERIES = "series"
        const val KIND_FAVORITES = "favorites"

        fun newInstance(kind: String): ContentFragment {
            return ContentFragment().apply {
                arguments = Bundle().apply { putString(ARG_KIND, kind) }
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentContentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        favs = FavoritesManager(requireContext())

        adapter = ChannelAdapter(
            onClick = { ch -> (requireActivity() as? OnChannelPlayListener)?.onPlay(ch) },
            onFavClick = { ch ->
                val added = favs.toggle(ch)
                Toast.makeText(
                    requireContext(),
                    if (added) getString(R.string.added_favorite) else getString(R.string.removed_favorite),
                    Toast.LENGTH_SHORT
                ).show()
                adapter.notifyDataSetChanged()
                if (kind() == KIND_FAVORITES) {
                    allChannels = favs.getFavorites()
                    applyFilter()
                }
            },
            isFavorite = { favs.isFavorite(it.url) }
        )
        binding.recycler.layoutManager = LinearLayoutManager(requireContext())
        binding.recycler.adapter = adapter

        binding.swipeRefresh.setOnRefreshListener {
            (requireActivity() as? OnRefreshListener)?.onRefresh()
        }

        binding.searchInput.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                searchQuery = s?.toString() ?: ""
                applyFilter()
            }
            override fun afterTextChanged(s: Editable?) {}
        })
    }

    fun kind(): String = arguments?.getString(ARG_KIND) ?: KIND_LIVE

    fun setChannels(channels: List<Channel>) {
        allChannels = when (kind()) {
            KIND_MOVIES -> channels.filter { it.isVod && !it.isSeries }
            KIND_SERIES -> channels.filter { it.isSeries }
            KIND_FAVORITES -> favs.getFavorites()
            else -> channels.filter { !it.isVod }
        }
        buildGroupChips()
        applyFilter()
    }

    fun setRefreshing(refreshing: Boolean) {
        if (_binding != null) binding.swipeRefresh.isRefreshing = refreshing
    }

    fun showLoading(show: Boolean) {
        if (_binding == null) return
        binding.loading.visibility = if (show) View.VISIBLE else View.GONE
        binding.emptyView.visibility = if (!show && adapter.itemCount == 0) View.VISIBLE else View.GONE
    }

    private fun buildGroupChips() {
        val container = binding.groupChips
        container.removeAllViews()
        val groups = (listOf("All") + allChannels.map { it.group }.distinct().sorted()).take(40)
        groups.forEach { g ->
            val chip = TextView(requireContext()).apply {
                text = g
                setPadding(28, 12, 28, 12)
                setBackgroundResource(R.drawable.bg_chip)
                setTextColor(
                    if (g == currentGroup) R.color.uragon_navy else R.color.uragon_text
                )
                if (g == currentGroup) {
                    setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.uragon_accent))
                }
                setOnClickListener {
                    currentGroup = g
                    buildGroupChips()
                    applyFilter()
                }
            }
            val lp = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply { marginEnd = 12 }
            chip.layoutParams = lp
            container.addView(chip)
        }
    }

    private fun applyFilter() {
        var list = allChannels
        if (currentGroup != "All") list = list.filter { it.group == currentGroup }
        if (searchQuery.isNotBlank()) {
            val q = searchQuery.lowercase()
            list = list.filter { it.title.lowercase().contains(q) || it.group.lowercase().contains(q) }
        }
        adapter.submit(list)
        binding.emptyView.visibility = if (list.isEmpty() && binding.loading.visibility != View.VISIBLE) View.VISIBLE else View.GONE
    }

    override fun onResume() {
        super.onResume()
        if (kind() == KIND_FAVORITES) {
            allChannels = favs.getFavorites()
            buildGroupChips()
            applyFilter()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    interface OnChannelPlayListener {
        fun onPlay(channel: Channel)
    }

    interface OnRefreshListener {
        fun onRefresh()
    }
}
