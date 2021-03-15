package com.chenyue404.intentfilter.ui

import android.content.IntentFilter
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import com.chenyue404.intentfilter.*
import com.chenyue404.intentfilter.entity.LogEntity

class LogFragment : Fragment() {
    private val TAG = "intentfilter-hook-"

    private lateinit var rvList: RecyclerView
    private lateinit var btClear: ImageButton

    private lateinit var logReceiver: LogReceiver
    private val dataList = arrayListOf<LogEntity>()
    private val listAdapter = LogListAdapter(dataList)

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ) = inflater.inflate(R.layout.fragment_log, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        view.apply {
            rvList = findViewById(R.id.rvList)
            btClear = findViewById(R.id.btClear)
        }

        rvList.apply {
            addItemDecoration(
                SpaceItemDecoration(
                    10.dp2Px(
                        requireContext()
                    )
                )
            )
            adapter = listAdapter
        }

        btClear.setOnClickListener {
            dataList.clear()
            listAdapter.notifyDataSetChanged()
        }

        logReceiver = LogReceiver {
            dataList.add(it)
            listAdapter.notifyItemChanged(dataList.lastIndex)
            rvList.scrollToPosition(dataList.lastIndex)
        }
        requireActivity().registerReceiver(logReceiver, IntentFilter().apply {
            addAction(LogReceiver.ACTION)
        })
    }

    override fun onDestroy() {
        requireActivity().unregisterReceiver(logReceiver)
        super.onDestroy()
    }

    private class LogListAdapter(val dataList: ArrayList<LogEntity>) :
        RecyclerView.Adapter<LogListAdapter.ViewHolder>() {

        private class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            val tvTime = itemView.findViewById<TextView>(R.id.tvTime)
            val tvUid = itemView.findViewById<TextView>(R.id.tvUid)
            val tvDataString = itemView.findViewById<TextView>(R.id.tvDataString)
            val tvActivities = itemView.findViewById<TextView>(R.id.tvActivities)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder = ViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.item_log, parent, false)
        )

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val logEntity = dataList[position]
            val activityList = logEntity.activities.split(App.SPLIT_LETTER)
            val indexList = logEntity.blockIndexes.split(App.SPLIT_LETTER)
            with(holder) {
                tvTime.text = logEntity.time.timeToStr()
                tvUid.text = logEntity.uid
                tvDataString.text = logEntity.dataString
                val spanUtils = SpanUtils.with(tvActivities)
                activityList.forEachIndexed { index, str ->
                    spanUtils.appendLine(str)
                        .setForegroundColor(
                            ContextCompat.getColor(
                                itemView.context,
                                if (indexList.contains(index.toString())) R.color.text_blocked
                                else R.color.text_normal
                            )
                        )
                }
                spanUtils.create()
            }
        }

        override fun getItemCount() = dataList.size
    }
}