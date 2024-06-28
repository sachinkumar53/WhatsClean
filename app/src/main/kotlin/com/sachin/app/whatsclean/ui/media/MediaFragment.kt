package com.sachin.app.whatsclean.ui.media

import android.app.Activity.RESULT_OK
import android.content.Intent
import android.os.Bundle
import android.util.TypedValue
import android.view.*
import android.widget.CompoundButton
import androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult
import androidx.appcompat.view.ActionMode
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.interpolator.view.animation.FastOutSlowInInterpolator
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.viewpager2.widget.ViewPager2
import com.hoc081098.viewbindingdelegate.viewBinding
import com.sachin.app.whatsclean.MainActivity
import com.sachin.app.whatsclean.R
import com.sachin.app.whatsclean.data.model.GridType
import com.sachin.app.whatsclean.databinding.CustomActionModeBinding
import com.sachin.app.whatsclean.databinding.FragmentMediaBinding
import com.sachin.app.whatsclean.util.extension.isAndroidRorAbove
import com.sachin.app.whatsclean.util.extension.showToast
import com.sachin.app.whatsclean.util.extension.startSupportActionMode
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MediaFragment : Fragment(R.layout.fragment_media), ActionMode.Callback {
    private val binding: FragmentMediaBinding by viewBinding()
    private val viewModel: MediaViewModel by viewModels()
    private val args: MediaFragmentArgs by navArgs()
    private var actionMode: ActionMode? = null
    private var actionModeBinding: CustomActionModeBinding? = null
    private var selectAllChangeListener = { _: CompoundButton, isChecked: Boolean ->
        viewModel.onSelectAllChanged(isChecked)
    }
    private var gridType = GridType.RECEIVED
    private val launcher = registerForActivityResult(StartActivityForResult()) {
        val uri = it.data?.data
        if (it.resultCode == RESULT_OK) {
            if (uri == null) {
                requireContext().showToast("Null uri")
            } else {
                requireContext().contentResolver.takePersistableUriPermission(
                    uri,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
                )
                if (isAndroidRorAbove)
                    viewModel.moveSelectedFiles(
                        (requireActivity() as MainActivity),
                        uri,
                        actionMode
                    )
            }
        } else {
            requireContext().showToast("Cancelled")
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        postponeEnterTransition()

        val mediaFragmentStateAdapter =
            MediaFragmentStateAdapter(this@MediaFragment, args.mediaType)

        binding.bottomMenuBar.setOnItemSelectedListener {
            viewModel.onBottomMenuItemSelected(
                (requireActivity() as MainActivity),
                launcher,
                actionMode,
                gridType,
                it.itemId
            )
        }

        binding.viewPager.apply {
            adapter = mediaFragmentStateAdapter
            registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
                override fun onPageSelected(position: Int) {
                    super.onPageSelected(position)
                    gridType = GridType.getTypeByPosition(position)
                    binding.bottomTabLayout.itemActiveIndex = position
                    binding.bottomMenuBar.menu.apply {
                        val isDuplicate = gridType == GridType.DUPLICATES
                        findItem(R.id.action_share)?.isVisible = !isDuplicate
                        findItem(R.id.action_move)?.isVisible = !isDuplicate
                    }
                }
            })
        }

        binding.bottomTabLayout.onItemSelected = { position ->
            binding.viewPager.setCurrentItem(position, true)
        }

        if (args.mediaType.sentDir == null) {
            binding.bottomTabLayout.isVisible = false
        }

        subscribeUI()
        viewLifecycleOwner.lifecycleScope.launchWhenResumed {
            delay(400)
            binding.viewPager.offscreenPageLimit = 2
        }
    }

    private fun subscribeUI() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.RESUMED) {
                viewModel.selectedCountFlow.collectLatest {
                    actionModeBinding?.title?.text =
                        if (it <= 0) "Select all" else "$it selected"
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.RESUMED) {
                viewModel.selectAllFlow.collectLatest {
                    actionModeBinding?.checkbox?.apply {
                        setOnCheckedChangeListener(null)
                        isChecked = it
                        setOnCheckedChangeListener(selectAllChangeListener)
                    }
                }
            }
        }


        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.RESUMED) {
                viewModel.isSelectionMode.collectLatest { selectionMode ->
                    binding.viewPager.isUserInputEnabled = !selectionMode
                    handleSelectionModeUI(selectionMode)
                    if (selectionMode && actionMode == null) {
                        actionMode = startSupportActionMode(this@MediaFragment)
                    } else if (!selectionMode && actionMode != null) {
                        actionMode?.finish()
                    }
                }
            }
        }


        /*viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {

                viewModel.subTitleFlow.collectLatest { (count, size) ->
                    actionModeBinding?.actionBarSubtitle?.apply {
                        isVisible = count > 0
                        text = resources.getQuantityString(
                            R.plurals.media_subtitle_format,
                            count,
                            count, size
                        )
                    }
                }

            }
        }*/

    }

    private fun handleSelectionModeUI(isSelectionMode: Boolean) {
        val offSet = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            72f,
            requireContext().resources.displayMetrics
        )

        if (isSelectionMode) {
            binding.bottomTabLayout.animate()
                .translationY(offSet)
                .withEndAction {
                    binding.bottomMenuBar.animate()
                        .translationY(0f)
                        .setInterpolator(FastOutSlowInInterpolator())
                        .setDuration(300)
                        .start()
                }.setInterpolator(FastOutSlowInInterpolator())
                .setDuration(300)
                .start()
        } else {
            binding.bottomMenuBar.animate()
                .translationY(offSet)
                .withEndAction {
                    binding.bottomTabLayout.animate()
                        .translationY(0f)
                        .setInterpolator(FastOutSlowInInterpolator())
                        .setDuration(300)
                        .start()
                }.setInterpolator(FastOutSlowInInterpolator())
                .setDuration(300)
                .start()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
        /*val scs = requireContext().themeColor(R.attr.colorSurface)
        val sce = ContextCompat.getColor(requireContext(), R.color.surface_color)
        sharedElementEnterTransition = MaterialContainerTransform().apply {
            drawingViewId = R.id.fragmentContainerView
            duration = resources.getInteger(R.integer.transition_duration_long).toLong()
            scrimColor = scs
            startContainerColor = scs
            endContainerColor = sce
        }*/
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.menu_media, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return if (item.itemId == R.id.action_sort_by) {
            findNavController().navigate(R.id.sortListDialogFragment)
            true
        } else super.onOptionsItemSelected(item)
    }

    override fun onCreateActionMode(mode: ActionMode?, menu: Menu?): Boolean {
        actionModeBinding =
            CustomActionModeBinding.inflate(LayoutInflater.from(requireContext()))
        actionModeBinding!!.checkbox.setOnCheckedChangeListener(selectAllChangeListener)
        mode?.customView = actionModeBinding?.root
        return true
    }

    override fun onPrepareActionMode(mode: ActionMode?, menu: Menu?): Boolean = false

    override fun onActionItemClicked(mode: ActionMode?, item: MenuItem?): Boolean =
        false

    override fun onDestroyActionMode(mode: ActionMode?) {
        viewModel.onDestroyActionMode()
        actionModeBinding = null
        actionMode = null
    }
}