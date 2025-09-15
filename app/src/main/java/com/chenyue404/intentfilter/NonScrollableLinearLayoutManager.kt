package com.chenyue404.intentfilter

import android.content.Context
import androidx.recyclerview.widget.LinearLayoutManager

class NonScrollableLinearLayoutManager(context: Context) : LinearLayoutManager(context) {
    override fun canScrollVertically() = false
    override fun canScrollHorizontally() = false
}