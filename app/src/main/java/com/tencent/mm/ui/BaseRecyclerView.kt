package com.tencent.mm.ui

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.view.FocusFinder
import android.view.View
import androidx.recyclerview.R
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomnavigation.BottomNavigationView

@SuppressLint("ViewConstructor")
class BaseRecyclerView : RecyclerView {

    internal constructor(context: Context) : super(context)
    internal constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
    internal constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    override fun focusSearch(focused: View?, direction: Int): View? {
        val view = super.focusSearch(focused,direction)
        val nextFocus = FocusFinder.getInstance().findNextFocus(this,focused,direction)
        return when(direction) {
            FOCUS_DOWN -> {
                if (nextFocus!=null&&nextFocus is BottomNavigationView) return view
                if (view!=null&&view is BottomNavigationView) return view
                if (nextFocus==null&& !canScrollVertically(1)) {
                    focused
                }  else view
            }
            else -> view
        }
    }

}