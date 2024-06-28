package com.sachin.app.whatsclean.ui.view

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import androidx.core.widget.NestedScrollView

class LockableNestedScrollView(
    context: Context,
    attrs: AttributeSet?,
    defStyleAttr: Int,
) : NestedScrollView(context, attrs, defStyleAttr) {

    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context) : this(context, null)

    var isScrollable = true

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(ev: MotionEvent): Boolean {
        return isScrollable && super.onTouchEvent(ev)
    }

    override fun onInterceptTouchEvent(ev: MotionEvent): Boolean {
        return isScrollable && super.onInterceptTouchEvent(ev)
    }

}