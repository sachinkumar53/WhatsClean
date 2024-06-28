package com.sachin.app.whatsclean.ui.media

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.sachin.app.whatsclean.data.model.GridType
import com.sachin.app.whatsclean.data.model.MediaType

class MediaFragmentStateAdapter(
    fragment: Fragment,
    private val mediaType: MediaType
) : FragmentStateAdapter(fragment) {

    override fun getItemCount(): Int = if (mediaType.sentDir != null) 3 else 1

    override fun createFragment(position: Int): Fragment {
        return MediaPageFragment.newInstance(GridType.getTypeByPosition(position))
    }
}