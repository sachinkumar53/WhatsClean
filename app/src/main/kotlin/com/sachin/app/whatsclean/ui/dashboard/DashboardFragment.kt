package com.sachin.app.whatsclean.ui.dashboard

import android.os.Bundle
import android.view.View
import androidx.activity.OnBackPressedCallback
import androidx.core.view.doOnPreDraw
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import com.google.android.material.card.MaterialCardView
import com.hoc081098.viewbindingdelegate.viewBinding
import com.sachin.app.whatsclean.R
import com.sachin.app.whatsclean.data.model.Card
import com.sachin.app.whatsclean.data.repositories.LoadStatus
import com.sachin.app.whatsclean.databinding.FragmentDashboardBinding
import com.sachin.app.whatsclean.util.BottomSheetDialogBuilder
import com.sachin.app.whatsclean.util.FileSizeFormatter
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class DashboardFragment : Fragment(R.layout.fragment_dashboard) {
    private val binding: FragmentDashboardBinding by viewBinding()
    private val viewModel: DashboardViewModel by viewModels()
    private lateinit var adapter: CardAdapter
    private val backPressedCallback = object : OnBackPressedCallback(false) {
        override fun handleOnBackPressed() {
            //if (viewModel.selectedCardList.isNotEmpty())
            clearCardSelection()
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        postponeEnterTransition()
        view.doOnPreDraw { startPostponedEnterTransition() }

        activity?.onBackPressedDispatcher?.addCallback(viewLifecycleOwner, backPressedCallback)

        adapter = CardAdapter(viewModel, ::onExploreClicked, ::onDeleteClicked)
        binding.scrollView.apply {
            scrollY = viewModel.lastScrollPosition
        }
        binding.recyclerView.apply {
            setHasFixedSize(true)
            adapter = this@DashboardFragment.adapter
        }

        binding.swipeRefreshLayout.run {
            setOnRefreshListener {
                viewModel.refresh()
            }
        }

        subscribeUi()

        binding.cleanButton.setOnClickListener {
            BottomSheetDialogBuilder(
                context = requireActivity(),
                title = getString(R.string.delete_dialog_title),
                message = getString(R.string.delete_dialog_message),
                positiveButtonText = getString(R.string.delete),
                negativeButtonText = getString(R.string.cancel),
                onNegativeButtonClick = {
                    clearCardSelection()
                },
                onPositiveButtonClick = {
                    viewModel.deleteSelectedCards {
                        clearCardSelection()
                    }
                }
            ).build().show()
        }
    }

    private fun onDeleteClicked(view: MaterialCardView, card: Card) {
        BottomSheetDialogBuilder(
            context = requireActivity(),
            title = getString(R.string.delete_dialog_title),
            message = getString(R.string.delete_dialog_message),
            positiveButtonText = getString(R.string.delete),
            negativeButtonText = getString(R.string.cancel),
            onPositiveButtonClick = {
                viewModel.deleteCardOfType(card.type) {
                    adapter.notifyItemRemoved(adapter.currentList.indexOf(card))
                }
            }
        ).build().show()
    }

    private fun clearCardSelection() {
        viewModel.selectedCardList.map {
            adapter.currentList.indexOf(it)
        }.forEach {
            adapter.notifyItemChanged(it)
        }
        viewModel.clearCardSelection()
    }

    override fun onPause() {
        super.onPause()
        viewModel.lastScrollPosition = binding.scrollView.scrollY
    }

    private fun subscribeUi() {
        viewModel.loadStatus.observe(viewLifecycleOwner) {
            val isDataLoading = it == LoadStatus.LOADING
            binding.swipeRefreshLayout.isRefreshing = isDataLoading
            binding.scrollView.isScrollable = !isDataLoading
            binding.placeholder.root.run {
                if (isDataLoading) startShimmer() else stopShimmer()
                isVisible = isDataLoading
            }
            binding.recyclerView.isVisible = !isDataLoading
        }

        viewModel.totalSizeAndCount.observe(viewLifecycleOwner) {
            val fileSize = FileSizeFormatter.format(size = it.totalSize)
            with(binding) {
                sizeTextView.text = fileSize.size
                unitTextView.text = fileSize.unit
                countTextView.text = resources.getQuantityString(
                    R.plurals.file_count_format,
                    it.totalCount,
                    it.totalCount
                )
            }
        }

        viewModel.cardList.observe(viewLifecycleOwner) {
            adapter.submitList(it)
        }

        viewModel.selectedCardList.observe(viewLifecycleOwner) {
            binding.cleanButton.apply {
                isVisible = it.isNotEmpty()
                val size = viewModel.selectedCardList.sumOf { it.size }
                val sizeString = FileSizeFormatter.format(size)
                text = getString(R.string.clean_button_text_format, sizeString)
            }
            backPressedCallback.isEnabled = it.isNotEmpty()
        }
    }


    private fun onExploreClicked(cardView: MaterialCardView, card: Card) {

        val options = NavOptions.Builder()
            .setLaunchSingleTop(true)
            .setEnterAnim(R.anim.slide_in_up)
            .setExitAnim(R.anim.fade_out)
            .setPopEnterAnim(R.anim.fade_in)
            .setPopExitAnim(R.anim.slide_out_down)
            .build()

        val action = DashboardFragmentDirections.actionDashboardFragmentToMediaFragment(
            card.type,
            getString(card.titleResId)
        )

        findNavController().navigate(action, options)
    }

}

private const val TAG = "DashboardFragment"