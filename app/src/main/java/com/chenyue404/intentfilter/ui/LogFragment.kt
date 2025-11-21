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
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.chenyue404.intentfilter.App
import com.chenyue404.intentfilter.LogReceiver
import com.chenyue404.intentfilter.NonScrollableLinearLayoutManager
import com.chenyue404.intentfilter.R
import com.chenyue404.intentfilter.dp2Px
import com.chenyue404.intentfilter.entity.BasicInfo
import com.chenyue404.intentfilter.entity.LogEntity
import com.chenyue404.intentfilter.timeToStr
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

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
            setItemViewCacheSize(5)
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

        logReceiver = LogReceiver { logEntityFromBroadcast ->
            lifecycleScope.launch(Dispatchers.IO) {
                logEntityFromBroadcast.processData()
                withContext(Dispatchers.Main) {
                    dataList.add(logEntityFromBroadcast)
                    listAdapter.notifyItemInserted(dataList.lastIndex)
                    if (!rvList.canScrollVertically(1)) {
                        rvList.scrollToPosition(dataList.lastIndex)
                    }
                }
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
        private val viewPool = RecyclerView.RecycledViewPool()
        fun clear() {
            unfoldIndexList.clear()
            val size = dataList.size
            dataList.clear()
            notifyItemRangeRemoved(0, size)
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

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val holder = ViewHolder(
                LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_log, parent, false)
            )
            holder.rvActivity.apply {
                setRecycledViewPool(viewPool)
                layoutManager = NonScrollableLinearLayoutManager(context)
                adapter = InfoAdapter()
//                (layoutManager as NonScrollableLinearLayoutManager).initialPrefetchItemCount = 10
            }
            holder.rvFrom.apply {
                setRecycledViewPool(viewPool)
                layoutManager = NonScrollableLinearLayoutManager(context)
                adapter = InfoAdapter()
//                (layoutManager as NonScrollableLinearLayoutManager).initialPrefetchItemCount = 5
            }
            return holder
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val logEntity = dataList[position]
            with(holder) {
                tvTime.text = logEntity.time.timeToStr()
                tvAction.text = logEntity.action
                tvType.text = logEntity.type
                tvDataString.text = logEntity.dataString

                (rvActivity.adapter as InfoAdapter).setData(
                    logEntity.activityInfoList,
                    logEntity.blockList
                )

                btMore.setOnClickListener {
                    val currentPosition = adapterPosition
                    if (currentPosition != RecyclerView.NO_POSITION) {
                        if (unfoldIndexList.contains(currentPosition)) {
                            unfoldIndexList.remove(currentPosition)
                        } else {
                            unfoldIndexList.add(currentPosition)
                        }
                        notifyItemChanged(currentPosition, "PAYLOAD_FOLD_STATE_CHANGED")
                    }
                }

                updateFoldStateUI(holder, logEntity, unfoldIndexList.contains(position))
            }
        }

        override fun onBindViewHolder(
            holder: ViewHolder,
            position: Int,
            payloads: MutableList<Any>
        ) {
            if (payloads.isEmpty()) {
                super.onBindViewHolder(holder, position, payloads)
            } else {
                if (payloads.contains("PAYLOAD_FOLD_STATE_CHANGED")) {
                    val logEntity = dataList[position]
                    updateFoldStateUI(holder, logEntity, unfoldIndexList.contains(position))
                }
            }
        }

        private fun updateFoldStateUI(
            holder: ViewHolder,
            logEntity: LogEntity,
            isUnfolded: Boolean
        ) {
            with(holder) {
                btMore.isVisible = logEntity.fromInfoList.size > 1
                btMore.rotation = if (isUnfolded) 180f else 0f

                val fromListSize = logEntity.fromInfoList.size
                if (!isUnfolded && fromListSize > 1) {
                    tvFrom.text = "$fromListSize apps"
                    tvFrom.isVisible = true
                    rvFrom.isVisible = false
                } else {
                    tvFrom.isVisible = false
                    rvFrom.isVisible = true
                    (rvFrom.adapter as InfoAdapter).setData(logEntity.fromInfoList)
                }
            }
        }

        override fun getItemCount() = dataList.size
    }

    private class InfoAdapter() : RecyclerView.Adapter<InfoAdapter.VH>() {
        private val diffCallback = object : DiffUtil.ItemCallback<Pair<String, BasicInfo>>() {
            override fun areItemsTheSame(
                oldItem: Pair<String, BasicInfo>,
                newItem: Pair<String, BasicInfo>
            ): Boolean {
                return oldItem.first == newItem.first
            }

            override fun areContentsTheSame(
                oldItem: Pair<String, BasicInfo>,
                newItem: Pair<String, BasicInfo>
            ): Boolean {
                val oldIsBlocked = blockList.contains(oldItem.first)
                val newIsBlocked = blockList.contains(newItem.first)
                return oldItem.second == newItem.second && oldIsBlocked == newIsBlocked
            }
        }
        private val differ = AsyncListDiffer(this, diffCallback)
        private var blockList: List<String> = emptyList()

        private class VH(itemView: View) : RecyclerView.ViewHolder(itemView) {
            val ivIcon: ImageView = itemView.findViewById(R.id.ivIcon)
            val tvName: TextView = itemView.findViewById(R.id.tvName)
            val tvPkg: TextView = itemView.findViewById(R.id.tvPkg)
            val sp0: Space = itemView.findViewById(R.id.sp0)
        }

        fun setData(
            newList: List<Pair<String, BasicInfo>>,
            newBlockList: List<String> = emptyList()
        ) {
            blockList = newBlockList
            differ.submitList(newList)
        }

        override fun onCreateViewHolder(
            parent: ViewGroup,
            viewType: Int
        ) = VH(
            LayoutInflater.from(parent.context)
                .inflate(R.layout.item_info, parent, false)
        )

        override fun onBindViewHolder(holder: VH, position: Int) {
            val pair = differ.currentList[position]
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
                val lastAppPkg = if (position > 0) {
                    differ.currentList.getOrNull(position - 1)?.first?.split("/")?.first()
                } else {
                    null
                }
                val showAsActivity = split.size > 1 && lastAppPkg == split.first()
                tvPkg.text =
                    if (showAsActivity) split.last()
                    else pkg
                sp0.isVisible = showAsActivity
                tvName.setTextIsSelectable(true)
                tvPkg.setTextIsSelectable(true)
            }
        }

        override fun getItemCount() = differ.currentList.size
    }
}