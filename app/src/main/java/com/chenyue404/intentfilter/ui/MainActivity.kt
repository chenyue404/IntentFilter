package com.chenyue404.intentfilter.ui

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.edit
import androidx.fragment.app.FragmentPagerAdapter
import androidx.viewpager.widget.ViewPager
import com.chenyue404.intentfilter.App
import com.chenyue404.intentfilter.R

class MainActivity : AppCompatActivity() {
    private val vpContent: ViewPager by lazy { findViewById(R.id.vpContent) }
    private val tvLog: TextView by lazy { findViewById(R.id.tvLog) }
    private val tvRule: TextView by lazy { findViewById(R.id.tvRule) }

    private val logFragment by lazy { LogFragment() }
    private val ruleFragment by lazy { RuleFragment() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        vpContent.adapter = object : FragmentPagerAdapter(
            supportFragmentManager,
            BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT
        ) {
            override fun getCount() = 2

            override fun getItem(position: Int) = if (position == 0) logFragment else ruleFragment
        }
        vpContent.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            override fun onPageScrolled(
                position: Int,
                positionOffset: Float,
                positionOffsetPixels: Int
            ) {
            }

            override fun onPageSelected(position: Int) {
                selectTab(position)
            }

            override fun onPageScrollStateChanged(state: Int) {
            }
        })
        selectTab(0)
        tvLog.setOnClickListener { selectTab(0) }
        tvRule.setOnClickListener { selectTab(1) }
        tvLog.setOnLongClickListener {
            showLogDialog()
            true
        }
    }

    private fun selectTab(position: Int) {
        tvLog.apply {
            setTextColor(if (position == 0) Color.BLACK else Color.GRAY)
            textSize = if (position == 0) 20f else 18f
        }
        tvRule.apply {
            setTextColor(if (position == 1) Color.BLACK else Color.GRAY)
            textSize = if (position == 1) 20f else 18f
        }
        vpContent.currentItem = position
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.about -> {
                val address = getString(R.string.githubAddress)
                AlertDialog.Builder(this)
                    .setTitle(R.string.about)
                    .setMessage(address)
                    .setPositiveButton(
                        R.string.view
                    ) { dialog, which ->
                        startActivity(
                            Intent(
                                Intent.ACTION_VIEW,
                                Uri.parse(address)
                            )
                        )
                    }
                    .create()
                    .show()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun showLogDialog() {
        var showLog = getSP()?.getBoolean(App.KEY_SHOW_LOG_NAME, false)
            ?: false
        AlertDialog.Builder(this)
            .setTitle(R.string.show_log_title)
            .setSingleChoiceItems(
                arrayOf("YES", "NO"),
                if (showLog) 0 else 1
            ) { dialog, which ->
                showLog = which == 0
            }
            .setPositiveButton(R.string.save) { dialog, which ->
                getSP()?.edit(true) {
                    putBoolean(App.KEY_SHOW_LOG_NAME, which == 0)
                }
                startActivity(Intent(this, EmptyActivity::class.java))
            }
            .create()
            .show()
    }

    fun getSP() = try {
        getSharedPreferences(
            App.PREF_NAME,
            Context.MODE_WORLD_READABLE
        )
    } catch (e: SecurityException) {
        // The new XSharedPreferences is not enabled or module's not loading
        null // other fallback, if any
    }
}