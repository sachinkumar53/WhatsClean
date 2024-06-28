package com.sachin.app.whatsclean.ui.media

import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.ViewGroup
import androidx.core.view.doOnPreDraw
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.afollestad.dragselectrecyclerview.DragSelectReceiver
import com.afollestad.dragselectrecyclerview.DragSelectTouchListener
import com.hoc081098.viewbindingdelegate.viewBinding
import com.sachin.app.whatsclean.R
import com.sachin.app.whatsclean.data.model.GridType
import com.sachin.app.whatsclean.databinding.FragmentMediaPageBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MediaPageFragment : Fragment(R.layout.fragment_media_page) {
    private val binding: FragmentMediaPageBinding by viewBinding()
    private val viewModel: MediaViewModel by viewModels(ownerProducer = { requireParentFragment() })
    private val adapter: MediaAdapter by lazy { MediaAdapter(viewModel) }
    private var gridType = GridType.RECEIVED

    private val dragSelectReceiver = object : DragSelectReceiver {
        override fun setSelected(index: Int, selected: Boolean) {
            val file = adapter.currentList[index]
            if (selected && !viewModel.isMediaFileSelected(file)) {
                viewModel.selectItem(file, adapter, true)
            } else if (!selected) {
                viewModel.selectItem(file, adapter, false)
            }
        }

        override fun isSelected(index: Int): Boolean {
            return viewModel.isMediaFileSelected(adapter.currentList[index])
        }

        override fun isIndexSelectable(index: Int) = true

        override fun getItemCount() = adapter.itemCount
    }

    private val touchListener by lazy {
        DragSelectTouchListener.create(
            requireContext(),
            dragSelectReceiver
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        (requireArguments().get("grid_type") as GridType).also { gridType = it }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        setItemClickListeners()
        subscribeUI()
    }

    private fun subscribeUI() {
        viewModel.getMediaFilesFlowByType(gridType).observe(viewLifecycleOwner) {
            binding.noFilesTextview.isVisible = it.isEmpty()
            adapter.submitList(it)
            (parentFragment?.view?.parent as? ViewGroup)?.doOnPreDraw {
                requireParentFragment().startPostponedEnterTransition()
            }
        }
    }

    private fun setupRecyclerView() {
        binding.recyclerView.apply {
            adapter = this@MediaPageFragment.adapter
            isNestedScrollingEnabled = false
            setHasFixedSize(true)
            addOnItemTouchListener(touchListener)
        }
    }

    private fun setItemClickListeners() {
        adapter.setOnItemLongClickListener { position, file ->
            viewModel.onItemLongClicked(position, file, adapter, touchListener)
        }

        adapter.setOnItemClickListener { img, _, file ->
            viewModel.onItemClicked(
                requireContext(),
                file,
                adapter,
                findNavController(),
                gridType
            )
        }
    }


    companion object {

        fun newInstance(gridType: GridType): MediaPageFragment {
            val args = Bundle()
            args.putSerializable("grid_type", gridType)
            val fragment = MediaPageFragment()
            fragment.arguments = args
            return fragment
        }
    }
}

