package com.sachin.app.whatsclean.ui.sortby

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckedTextView
import androidx.annotation.StringRes
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.sachin.app.whatsclean.R
import com.sachin.app.whatsclean.data.model.SortType
import com.sachin.app.whatsclean.databinding.FragmentSortDialogBinding
import com.sachin.app.whatsclean.databinding.SortRowItemBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SortListDialogFragment : BottomSheetDialogFragment() {
    private var _binding: FragmentSortDialogBinding? = null
    private val binding get() = _binding!!
    private val viewModel: SortTypeViewModel by viewModels()
    private val sortAdapter = SortAdapter()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSortDialogBinding.inflate(inflater, container, false)
        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding.list.apply {
            setHasFixedSize(true)
            adapter = sortAdapter
        }

        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.selectedSortTypeFlow.collect {
                    sortAdapter.notifyDataSetChanged()
                }
            }
        }
    }

    private inner class SortAdapter : RecyclerView.Adapter<SortAdapter.ViewHolder>() {
        private val sortTypes = SortType.values()

        private inner class ViewHolder(
            binding: SortRowItemBinding
        ) : RecyclerView.ViewHolder(binding.root) {
            val text: CheckedTextView = binding.text

            init {
                text.setOnClickListener {
                    if (adapterPosition != RecyclerView.NO_POSITION) {
                        viewModel.onSortTypeChanged(sortType = sortTypes[adapterPosition])
                    }
                    dismiss()
                }
            }
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = ViewHolder(
            SortRowItemBinding.inflate(
                LayoutInflater.from(
                    parent.context
                ), parent, false
            )
        )

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val sortType = sortTypes[position]

            holder.text.apply {
                setText(getSortByText(sortType))

                (sortType == viewModel.selectedSortTypeFlow.value).let { isSelected ->
                    isChecked = isSelected
                    if (isSelected) {
                        setCheckMarkDrawable(R.drawable.ic_round_check_24)
                    } else checkMarkDrawable = null
                }
            }

        }

        @StringRes
        private fun getSortByText(sortType: SortType): Int {
            return when (sortType) {
                SortType.NEWEST_FIRST -> R.string.sort_by_newest_first
                SortType.OLDEST_FIRST -> R.string.sort_by_oldest_first
                SortType.LARGEST_FIRST -> R.string.sort_by_largest_first
                SortType.SMALLEST_FIRST -> R.string.sort_by_smallest_first
            }
        }

        override fun getItemCount(): Int = sortTypes.size
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}