package com.sachin.app.whatsclean.ui.gallery

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.WindowManager
import android.view.WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_ALWAYS
import android.view.WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_DEFAULT
import android.view.animation.AccelerateDecelerateInterpolator
import androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.core.view.WindowInsetsControllerCompat.BEHAVIOR_SHOW_BARS_BY_TOUCH
import androidx.core.view.WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
import androidx.core.view.updatePadding
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.viewpager2.widget.ViewPager2
import com.hoc081098.viewbindingdelegate.viewBinding
import com.sachin.app.whatsclean.MainActivity
import com.sachin.app.whatsclean.R
import com.sachin.app.whatsclean.databinding.FragmentGalleryBinding
import com.sachin.app.whatsclean.util.FileSizeFormatter
import com.sachin.app.whatsclean.util.extension.isAndroidRorAbove
import com.sachin.app.whatsclean.util.extension.showToast
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

private const val TAG = "GalleryFragment"

@AndroidEntryPoint
class GalleryFragment : Fragment(R.layout.fragment_gallery) {
    private val binding: FragmentGalleryBinding by viewBinding()
    private val args: GalleryFragmentArgs by navArgs()
    private val viewModel: GalleryViewModel by viewModels()
    private var isSystemUiVisible = true

    private val galleryAdapter = GalleryAdapter { toggleSystemUiVisibility(!isSystemUiVisible) }


    private val launcher = registerForActivityResult(StartActivityForResult()) {
        val uri = it.data?.data
        if (it.resultCode == Activity.RESULT_OK) {
            if (uri == null) {
                requireContext().showToast("Null uri")
            } else {
                requireContext().contentResolver.takePersistableUriPermission(
                    uri,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
                )
                if (isAndroidRorAbove)
                    viewModel.moveFile(requireContext(), uri, this::onCompleteEvent)
            }
        } else {
            requireContext().showToast("Cancelled")
        }
    }

    override fun onDestroyView() {
        restoreDefaultSystemBar()
        super.onDestroyView()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupTranslucentSystemBar()

        binding.toolbar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }

        binding.viewPager2.apply {
            offscreenPageLimit = 2
            adapter = galleryAdapter
            registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
                override fun onPageSelected(position: Int) {
                    super.onPageSelected(position)
                    onPageChange(position)
                }
            })
        }

        binding.bottomNavigationMenu.setOnItemSelectedListener {
            viewModel.onBottomMenuItemSelected(
                requireActivity(),
                launcher,
                it.itemId,
                this::onCompleteEvent
            )
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.imageFilesFlow.collectLatest {
                    galleryAdapter.submitList(it)
                    viewModel.currentFile = it.getOrNull(viewModel.position)
                    if (binding.viewPager2.currentItem != viewModel.position) {
                        binding.viewPager2.setCurrentItem(
                            viewModel.position,
                            viewModel.position != args.initialPosition
                        )
                    }
                }
            }
        }
    }

    private fun onCompleteEvent() {
        val files = galleryAdapter.currentList
        if (files.isEmpty()) {
            findNavController().navigateUp()
        }/* else {
            //val size = files.size
            val index = binding.viewPager2.currentItem
            //val nextIndex = (index + 1) % size
            galleryAdapter.notifyItemRemoved(index)
            //binding.viewPager2.setCurrentItem(nextIndex, true)
        }*/
    }


    private fun onPageChange(position: Int) {
        Log.i(TAG, "onPageChange: $position")
        viewModel.position = position
        galleryAdapter.currentList.getOrNull(position)?.let { file ->
            viewModel.currentFile = file
            binding.toolbar.apply {
                title = file.name
                subtitle = FileSizeFormatter.format(file.length).toString()
            }
        } ?: run {
            findNavController().navigateUp()
        }
    }

    private fun toggleSystemUiVisibility(visible: Boolean) {
        activity?.window?.run {
            if (visible) {
                WindowInsetsControllerCompat(
                    this,
                    decorView
                ).show(WindowInsetsCompat.Type.systemBars())
            } else {
                WindowInsetsControllerCompat(
                    this,
                    decorView
                ).hide(WindowInsetsCompat.Type.systemBars())
            }
        }

        binding.toolbar.run {
            animate().alpha(
                if (visible) 1f else 0f
            ).withEndAction {
                isSystemUiVisible = visible
            }.setDuration(200)
                .setInterpolator(AccelerateDecelerateInterpolator())
                .start()
        }

        binding.bottomNavigationMenu.run {
            animate().alpha(
                if (visible) 1f else 0f
            ).setDuration(200)
                .setInterpolator(AccelerateDecelerateInterpolator())
                .start()
        }
    }

    private fun restoreDefaultSystemBar() {
        (activity as? MainActivity)?.run {
            with(window) {
                clearFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS)

                WindowInsetsControllerCompat(
                    this,
                    decorView
                ).apply {
                    isAppearanceLightNavigationBars = viewModel.isLightNav
                    systemBarsBehavior = BEHAVIOR_SHOW_BARS_BY_TOUCH
                    show(WindowInsetsCompat.Type.systemBars())
                }

                if (isAndroidRorAbove) {
                    val attr = attributes
                    attr.layoutInDisplayCutoutMode = LAYOUT_IN_DISPLAY_CUTOUT_MODE_DEFAULT
                    attributes = attr
                }
            }
            supportActionBar?.show()
        }
    }

    private fun setupTranslucentSystemBar() {
        (activity as? MainActivity)?.run {
            window.apply {
                addFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS)

                if (isAndroidRorAbove) {
                    val attr = attributes
                    attr.layoutInDisplayCutoutMode = LAYOUT_IN_DISPLAY_CUTOUT_MODE_ALWAYS
                    attributes = attr
                }

                val insets = ViewCompat.getRootWindowInsets(decorView)

                val sh = insets?.getInsets(WindowInsetsCompat.Type.statusBars())
                val nh = insets?.getInsets(WindowInsetsCompat.Type.navigationBars())

                binding.toolbar.updatePadding(top = sh?.top ?: 0)
                binding.bottomNavigationMenu.apply {
                    setOnApplyWindowInsetsListener(null)
                    updatePadding(bottom = nh?.bottom ?: 0)
                }

                WindowInsetsControllerCompat(
                    this,
                    decorView
                ).apply {
                    viewModel.isLightNav = isAppearanceLightNavigationBars
                    isAppearanceLightNavigationBars = false
                    systemBarsBehavior = BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
                }
                supportActionBar?.hide()
            }
        }
    }


}