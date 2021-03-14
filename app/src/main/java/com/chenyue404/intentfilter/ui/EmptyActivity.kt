package com.chenyue404.intentfilter.ui

import android.app.Activity
import android.os.Bundle
import android.view.Gravity

/**
 * Created by Eddie on 2021/3/13 0013.
 */
class EmptyActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.apply {
            setGravity(Gravity.TOP or Gravity.START)
            attributes.width = 1
            attributes.height = 1
        }

        finish()
    }
}