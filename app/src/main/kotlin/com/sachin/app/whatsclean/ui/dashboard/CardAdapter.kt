package com.sachin.app.whatsclean.ui.dashboard

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isInvisible
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.card.MaterialCardView
import com.sachin.app.whatsclean.data.model.Card
import com.sachin.app.whatsclean.data.model.MediaType
import com.sachin.app.whatsclean.data.model.loadIcon
import com.sachin.app.whatsclean.databinding.CardListItemBinding

class CardAdapter(
    private val viewModel: DashboardViewModel,
    private inline val onExploreClickListener: (MaterialCardView, Card) -> Unit,
    private inline val onDeleteClickListener: (MaterialCardView, Card) -> Unit
) : ListAdapter<Card, CardAdapter.CardViewHolder>(
    object : DiffUtil.ItemCallback<Card>() {
        override fun areItemsTheSame(oldItem: Card, newItem: Card): Boolean {
            return oldItem.titleResId == newItem.titleResId
        }

        override fun areContentsTheSame(oldItem: Card, newItem: Card): Boolean {
            return oldItem == newItem
        }

    }
) {

    inner class CardViewHolder(private val binding: CardListItemBinding) :
        RecyclerView.ViewHolder(binding.root) {

        init {
            binding.exploreButton.setOnClickListener {
                if (adapterPosition != RecyclerView.NO_POSITION) {
                    onExploreClickListener(binding.root, getItem(adapterPosition))
                    viewModel.selectedCardList.clear()
                }
            }

            binding.deleteButton.setOnClickListener {
                if (adapterPosition != RecyclerView.NO_POSITION) {
                    onDeleteClickListener(binding.root, getItem(adapterPosition))
                    viewModel.selectedCardList.clear()
                }
            }

            binding.root.setOnClickListener {
                if (adapterPosition != RecyclerView.NO_POSITION) {
                    val card = getItem(adapterPosition)
                    binding.root.isChecked = viewModel.toggleCardSelection(card)
                }
            }
        }

        fun bind(item: Card?) = with(binding) {
            item?.let { card ->
                cardTitle.text = root.context.getText(card.titleResId)
                cardSize.text = String.format("%d files • %s", card.count, card.formattedSize)
                val isSelected = viewModel.isCardSelected(card)
                root.isChecked = isSelected

                val shouldShowDeleteButton = card.type == MediaType.DATABASE
                deleteButton.isVisible = shouldShowDeleteButton
                exploreButton.isVisible = !shouldShowDeleteButton

                for (i in 0..3) {
                    val file = if (card.previewList.size > i) card.previewList[i] else null
                    when (i) {
                        0 -> preview0
                        1 -> preview1
                        2 -> preview2
                        3 -> preview3
                        else -> null
                    }?.let {
                        if (file == null) {
                            it.isInvisible = true
                        } else {
                            if (it.isInvisible) it.isInvisible = false
                            file.loadIcon(it)
                        }
                    }
                }
            }

        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = CardViewHolder(
        CardListItemBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
    )


    override fun onBindViewHolder(holder: CardViewHolder, position: Int) {
        holder.bind(getItem(position))
    }
}