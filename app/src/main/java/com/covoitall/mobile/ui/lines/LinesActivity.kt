package com.covoitall.mobile.ui.lines

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.covoitall.mobile.databinding.ActivityLinesBinding
import com.covoitall.mobile.databinding.ItemLineBinding
import com.covoitall.mobile.ui.common.appViewModels
import com.covoitall.mobile.ui.common.onTextChanged
import com.covoitall.mobile.ui.common.showMessage

class LinesActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLinesBinding
    private val viewModel: LinesViewModel by appViewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLinesBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val adapter = LineAdapter { item ->
            startActivity(Intent(this, LineDetailActivity::class.java).putExtra(LineDetailActivity.EXTRA_LINE_ID, item.id))
        }
        binding.toolbar.setNavigationOnClickListener { finish() }
        binding.linesRecycler.layoutManager = LinearLayoutManager(this)
        binding.linesRecycler.adapter = adapter
        binding.searchInput.onTextChanged(viewModel::setQuery)
        binding.swipeRefresh.setOnRefreshListener { viewModel.refresh() }

        viewModel.lines.observe(this) { lines ->
            adapter.submitList(lines)
            binding.emptyText.isVisible = lines.isEmpty() && viewModel.loading.value == false
        }
        viewModel.loading.observe(this) { binding.swipeRefresh.isRefreshing = it }
        viewModel.message.observe(this) { event -> event.consume()?.let(::showMessage) }
    }
}

private class LineAdapter(private val onClick: (LineItem) -> Unit) : ListAdapter<LineItem, LineAdapter.Holder>(Diff) {
    class Holder(val binding: ItemLineBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        Holder(ItemLineBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun onBindViewHolder(holder: Holder, position: Int) {
        val item = getItem(position)
        with(holder.binding) {
            codeText.text = item.code
            nameText.text = item.name
            routeText.text = item.route
            nextText.text = "Prochain départ : ${item.nextDeparture}"
            infoText.text = "${item.stopCount} arrêts · ${item.scheduleCount} horaire(s) · Suivi en temps réel"
            root.setOnClickListener { onClick(item) }
        }
    }

    object Diff : DiffUtil.ItemCallback<LineItem>() {
        override fun areItemsTheSame(oldItem: LineItem, newItem: LineItem) = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: LineItem, newItem: LineItem) = oldItem == newItem
    }
}
