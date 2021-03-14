package com.chenyue404.intentfilter.ui

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.PowerManager
import android.provider.Settings
import android.view.Menu
import android.view.MenuItem
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentPagerAdapter
import androidx.viewpager.widget.ViewPager
import com.chenyue404.intentfilter.R

class MainActivity : AppCompatActivity() {
    private lateinit var vpContent: ViewPager
    private lateinit var tvLog: TextView
    private lateinit var tvRule: TextView

    private val logFragment by lazy { LogFragment() }
    private val ruleFragment by lazy { RuleFragment() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        vpContent = findViewById(R.id.vpContent)
        tvLog = findViewById(R.id.tvLog)
        tvRule = findViewById(R.id.tvRule)

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

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M &&
            !(getSystemService(Context.POWER_SERVICE) as PowerManager).isIgnoringBatteryOptimizations(
                packageName
            )
        ) {
            AlertDialog.Builder(this)
                .setMessage(R.string.batteryTip)
                .setPositiveButton(android.R.string.ok) { _, _ ->
                    startActivity(Intent().apply {
                        action = Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS
                    })
                }
                .create().show()
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
}