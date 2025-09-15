package com.chenyue404.intentfilter.ui

import android.content.Intent
import android.content.IntentFilter
import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.Space
import android.widget.TextView
import androidx.core.content.edit
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import com.chenyue404.intentfilter.App
import com.chenyue404.intentfilter.LogReceiver
import com.chenyue404.intentfilter.NonScrollableLinearLayoutManager
import com.chenyue404.intentfilter.R
import com.chenyue404.intentfilter.dp2Px
import com.chenyue404.intentfilter.entity.BasicInfo
import com.chenyue404.intentfilter.entity.LogEntity
import com.chenyue404.intentfilter.queryActivityInfo
import com.chenyue404.intentfilter.queryAppInfo
import com.chenyue404.intentfilter.timeToStr

class LogFragment : Fragment() {
    private val TAG = "intentfilter-hook-"

    private val rvList: RecyclerView by lazy { requireView().findViewById(R.id.rvList) }
    private val btClear: ImageButton by lazy { requireView().findViewById(R.id.btClear) }
    private val btStatus: ImageButton by lazy { requireView().findViewById(R.id.btStatus) }

    private lateinit var logReceiver: LogReceiver
    private val dataList = arrayListOf<LogEntity>()
    private val listAdapter = LogListAdapter(dataList)

    private val sp: SharedPreferences? by lazy { (requireActivity() as MainActivity).getSP() }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.fragment_log, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

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

        var sendBroadcast = sp?.getBoolean(App.KEY_SEND_BROADCAST, false) ?: false
        updateBtStatus(sendBroadcast)
        btStatus.setOnClickListener {
            sendBroadcast = !sendBroadcast
            updateBtStatus(sendBroadcast)
            sp?.edit(true) {
                putBoolean(App.KEY_SEND_BROADCAST, sendBroadcast)
            }
            startActivity(Intent(requireContext(), EmptyActivity::class.java))
        }

        btClear.setOnClickListener {
            listAdapter.clear()
        }

        logReceiver = LogReceiver {
            dataList.add(it)
            listAdapter.notifyItemInserted(dataList.lastIndex)
            if (!rvList.canScrollVertically(1)) {
                rvList.scrollToPosition(dataList.lastIndex)
            }
        }
        requireActivity().registerReceiver(logReceiver, IntentFilter().apply {
            addAction(LogReceiver.ACTION)
        })
    }

    private fun updateBtStatus(sendBroadcast: Boolean) {
        btStatus.setImageResource(
            if (sendBroadcast) android.R.drawable.ic_media_pause
            else android.R.drawable.ic_media_play
        )
    }

    override fun onDestroy() {
        requireActivity().unregisterReceiver(logReceiver)
        super.onDestroy()
    }

    private class LogListAdapter(val dataList: ArrayList<LogEntity>) :
        RecyclerView.Adapter<LogListAdapter.ViewHolder>() {
        private var unfoldIndexList = mutableListOf<Int>()

        fun clear() {
            unfoldIndexList.clear()
            val size = dataList.size
            dataList.clear()
            notifyItemRangeRemoved(0, size)
        }

        private fun toggleFold(holder: ViewHolder, position: Int) {
            val logEntity = dataList[position]
            val fromList = logEntity.from.split(App.SPLIT_LETTER)
            val activityList = logEntity.activities.split(App.SPLIT_LETTER)
            val indexList = logEntity.blockIndexes.split(App.SPLIT_LETTER)
            val unfold = unfoldIndexList.contains(position)
            with(unfoldIndexList) {
                if (contains(position)) remove(position)
                else add(position)
            }
            with(holder) {
                btMore.rotation = if (unfold) 180f else 0f
                if (!unfold && fromList.size > 1) {
                    tvFrom.text = "${fromList.size} apps"
                    tvFrom.isVisible = true
                    rvFrom.isVisible = false
                } else {
                    tvFrom.isVisible = false
                    rvFrom.apply {
                        isVisible = true
                        layoutManager = NonScrollableLinearLayoutManager(itemView.context)
                        adapter = InfoAdapter().apply {
                            setData(buildInfoList(fromList))
                        }
                    }
                }

//                if (!unfold && activityList.size > 1) {
//                    tvActivities.text = "${activityList.size} activities"
//                    tvActivities.isVisible = true
//                    rvActivity.isVisible = false
//                } else {
//                    tvActivities.isVisible = false
//                    rvActivity.apply {
//                        isVisible = true
//                        layoutManager = NonScrollableLinearLayoutManager(itemView.context)
//                        adapter = InfoAdapter().apply {
//                            val blockList = indexList.mapNotNull {
//                                it.toIntOrNull()?.let { index -> activityList.getOrNull(index) }
//                            }
//                            setData(buildInfoList(activityList), blockList)
//                        }
//                    }
//                }
            }
        }

        private class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            val tvTime: TextView = itemView.findViewById(R.id.tvTime)
            val tvFrom: TextView = itemView.findViewById(R.id.tvFrom)
            val rvFrom: RecyclerView = itemView.findViewById(R.id.rvFrom)
            val tvAction: TextView = itemView.findViewById(R.id.tvAction)
            val tvType: TextView = itemView.findViewById(R.id.tvType)
            val tvDataString: TextView = itemView.findViewById(R.id.tvDataString)
            val tvActivities: TextView = itemView.findViewById(R.id.tvActivities)
            val rvActivity: RecyclerView = itemView.findViewById(R.id.rvActivity)
            val btMore: ImageButton = itemView.findViewById(R.id.btMore)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder = ViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.item_log, parent, false)
        )

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val logEntity = dataList[position]
            val fromList = logEntity.from.split(App.SPLIT_LETTER)
            val activityList = logEntity.activities.split(App.SPLIT_LETTER)
            val indexList = logEntity.blockIndexes.split(App.SPLIT_LETTER)
            val unfold = unfoldIndexList.contains(position)
            with(holder) {
                btMore.setOnClickListener {
                    toggleFold(holder, position)
                }
                btMore.isVisible = fromList.size > 1
                tvTime.text = logEntity.time.timeToStr()
                tvAction.text = logEntity.action
                tvType.text = logEntity.type
                tvDataString.text = logEntity.dataString

                tvActivities.isVisible = false
                rvActivity.apply {
                    isVisible = true
                    layoutManager = NonScrollableLinearLayoutManager(itemView.context)
                    adapter = InfoAdapter().apply {
                        val blockList = indexList.mapNotNull {
                            it.toIntOrNull()?.let { index -> activityList.getOrNull(index) }
                        }
                        setData(buildInfoList(activityList), blockList)
                    }
                }
            }
            toggleFold(holder, position)
        }

        override fun getItemCount() = dataList.size

        private fun buildInfoList(list: List<String>): List<Pair<String, BasicInfo>> {
            val newList: MutableList<Pair<String, BasicInfo>> = mutableListOf()
            val lastPkg = mutableListOf<String>()
            list.forEach {
                val isActivity = it.contains("/")
                if (isActivity) {
                    val pkg = it.split("/").first()
                    if (lastPkg.firstOrNull() == pkg) {
                        if (lastPkg.size == 1) {
                            newList.add(newList.size - 1, pkg to queryAppInfo(pkg))
                        }
                    } else {
                        lastPkg.clear()
                    }
                    lastPkg.add(pkg)
                    newList.add(it to queryActivityInfo(it))
                } else {
                    newList.add(it to queryAppInfo(it))
                }
            }
            return newList
        }
    }

    private class InfoAdapter() : RecyclerView.Adapter<InfoAdapter.VH>() {
        private class VH(itemView: View) : RecyclerView.ViewHolder(itemView) {
            val ivIcon: ImageView = itemView.findViewById(R.id.ivIcon)
            val tvName: TextView = itemView.findViewById(R.id.tvName)
            val tvPkg: TextView = itemView.findViewById(R.id.tvPkg)
            val sp0: Space = itemView.findViewById(R.id.sp0)
        }

        private var dataList: List<Pair<String, BasicInfo>> = listOf()
        private var blockList: List<String> = listOf()

        fun setData(list: List<Pair<String, BasicInfo>>, blockList: List<String> = emptyList()) {
            dataList = list.toList()
            this.blockList = blockList.toList()
            notifyItemRangeChanged(0, dataList.size)
        }

        override fun onCreateViewHolder(
            parent: ViewGroup,
            viewType: Int
        ) = VH(
            LayoutInflater.from(parent.context)
                .inflate(R.layout.item_info, parent, false)
        )

        override fun onBindViewHolder(holder: VH, position: Int) {
            val pair = dataList[position]
            val pkg = pair.first
            val info = pair.second
            val isBlocked = blockList.contains(pkg)
            holder.apply {
                ivIcon.setImageDrawable(info.icon)
                val textColor = itemView.context.getColor(
                    if (isBlocked) R.color.text_blocked
                    else R.color.text_normal
                )
                tvName.setTextColor(textColor)
                tvPkg.setTextColor(textColor)
                tvName.text = info.label
                val split = pkg.split("/")
                val lastAppPkg = dataList.getOrNull(position - 1)?.first?.split("/")?.first()
                val showAsActivity = split.size > 1 && lastAppPkg == split.first()
                tvPkg.text =
                    if (showAsActivity) split.last()
                    else pkg
                sp0.isVisible = showAsActivity
            }
        }

        override fun getItemCount() = dataList.size
    }
}