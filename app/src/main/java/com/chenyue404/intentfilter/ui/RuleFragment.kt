package com.chenyue404.intentfilter.ui

import android.app.Activity
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.text.Html
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.core.content.edit
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import com.chenyue404.intentfilter.*
import com.chenyue404.intentfilter.entity.RuleEntity
import com.google.gson.Gson

class RuleFragment : Fragment() {
    private lateinit var rvList: RecyclerView
    private lateinit var btSave: ImageButton
    private lateinit var btAdd: ImageButton
    private lateinit var tvTip: TextView

    private val dataList = arrayListOf<RuleEntity>()
    private lateinit var listAdapter: RuleListAdapter
    private val sp: SharedPreferences? by lazy { (requireActivity() as MainActivity).getSP() }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ) = inflater.inflate(R.layout.fragment_rule, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        with(view) {
            rvList = findViewById(R.id.rvList)
            btSave = findViewById(R.id.btSave)
            btAdd = findViewById(R.id.btAdd)
            btAdd = findViewById(R.id.btAdd)
            tvTip = findViewById(R.id.tvTip)
        }
        tvTip.text = Html.fromHtml(
            getString(R.string.tip_rule, App.SPLIT_LETTER),
            Html.FROM_HTML_MODE_LEGACY
        )
        listAdapter = RuleListAdapter(dataList) {
            dataList.removeAt(it)
            listAdapter.notifyItemRemoved(it)
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

        btSave.setOnClickListener {
            hideKeyboard()
            val haveEmptyEntity =
                dataList.any {
                    it.actionKeywords.isEmpty()
                            && it.typeKeywords.isEmpty()
                            && it.dataStringKeywords.isEmpty()
                            && it.activityKeywords.isEmpty()
                            && it.from.isEmpty()
                }
            if (haveEmptyEntity) {
                Toast.makeText(requireContext(), getString(R.string.noEmpty), Toast.LENGTH_SHORT)
                    .show()
                return@setOnClickListener
            }

            sp?.edit(true) {
                putString(
                    App.KEY_NAME,
                    if (dataList.isEmpty()) App.EMPTY_STR
                    else Gson().toJson(dataList)
                )
            }
            startActivity(Intent(requireContext(), EmptyActivity::class.java))
            Toast.makeText(requireContext(), getString(R.string.saved), Toast.LENGTH_SHORT)
                .show()
        }
        btAdd.setOnClickListener {
            dataList.add(RuleEntity())
            listAdapter.notifyItemChanged(dataList.size - 1)
            rvList.scrollToPosition(dataList.size - 1)
        }
        readPerf()
        writeEmptyStr()
    }

    private fun readPerf() {
        val str = sp?.getString(App.KEY_NAME, App.EMPTY_STR) ?: return
        if (str == App.EMPTY_STR) return

        val list = fromJson<ArrayList<RuleEntity>>(str)

        dataList.apply {
            clear()
            if (!list.isEmpty()) {
                addAll(list)
            }
        }
        listAdapter.notifyDataSetChanged()
    }

    private class RuleListAdapter(
        val dataList: ArrayList<RuleEntity>,
        val deleteFun: (Int) -> Unit
    ) :
        RecyclerView.Adapter<RuleListAdapter.ViewHolder>() {

        private class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            val etAction: EditText = itemView.findViewById(R.id.etAction)
            val etType: EditText = itemView.findViewById(R.id.etType)
            val etDataString: EditText = itemView.findViewById(R.id.etDataString)
            val etActivity: EditText = itemView.findViewById(R.id.etActivity)
            val etFrom: EditText = itemView.findViewById(R.id.etFrom)
            val tbAction: ToggleButton = itemView.findViewById(R.id.tbAction)
            val tbType: ToggleButton = itemView.findViewById(R.id.tbType)
            val tbDataString: ToggleButton = itemView.findViewById(R.id.tbDataString)
            val tbActivity: ToggleButton = itemView.findViewById(R.id.tbActivity)
            val tbFrom: ToggleButton = itemView.findViewById(R.id.tbFrom)
            val ibDelete: ImageButton = itemView.findViewById(R.id.ibDelete)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder = ViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.item_rule, parent, false)
        )

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val ruleEntity = dataList[position]
            with(holder) {
                etAction.setText(ruleEntity.actionKeywords)
                etType.setText(ruleEntity.typeKeywords)
                etDataString.setText(ruleEntity.dataStringKeywords)
                etActivity.setText(ruleEntity.activityKeywords)
                etFrom.setText(ruleEntity.from)

                tbAction.isChecked = ruleEntity.actionBlack
                tbAction.visible(ruleEntity.actionKeywords.isNotEmpty())
                tbType.isChecked = ruleEntity.typeBlack
                tbType.visible(ruleEntity.typeKeywords.isNotEmpty())
                tbDataString.isChecked = ruleEntity.dataStringBlack
                tbDataString.visible(ruleEntity.dataStringKeywords.isNotEmpty())
                tbActivity.isChecked = ruleEntity.activityBlack
                tbActivity.visible(ruleEntity.activityKeywords.isNotEmpty())
                tbFrom.isChecked = ruleEntity.fromBlack
                tbFrom.visible(ruleEntity.from.isNotEmpty())

                etAction.doAfterTextChanged {
                    ruleEntity.actionKeywords = it.toString()
                    tbAction.visible(it.toString().isNotEmpty())
                }
                etType.doAfterTextChanged {
                    ruleEntity.typeKeywords = it.toString()
                    tbType.visible(it.toString().isNotEmpty())
                }
                etDataString.doAfterTextChanged {
                    ruleEntity.dataStringKeywords = it.toString()
                    tbDataString.visible(it.toString().isNotEmpty())
                }
                etActivity.doAfterTextChanged {
                    ruleEntity.activityKeywords = it.toString()
                    tbActivity.visible(it.toString().isNotEmpty())
                }
                etFrom.doAfterTextChanged {
                    ruleEntity.from = it.toString()
                    tbFrom.visible(it.toString().isNotEmpty())
                }

                tbAction.setOnCheckedChangeListener { _, isChecked ->
                    ruleEntity.actionBlack = isChecked
                }
                tbType.setOnCheckedChangeListener { _, isChecked ->
                    ruleEntity.typeBlack = isChecked
                }
                tbDataString.setOnCheckedChangeListener { _, isChecked ->
                    ruleEntity.dataStringBlack = isChecked
                }
                tbActivity.setOnCheckedChangeListener { _, isChecked ->
                    ruleEntity.activityBlack = isChecked
                }
                tbFrom.setOnCheckedChangeListener { _, isChecked ->
                    ruleEntity.fromBlack = isChecked
                }
                ibDelete.setOnClickListener {
                    val index = dataList.indexOf(ruleEntity)
                    if (index >= 0) deleteFun(index)
                }
            }
        }

        override fun getItemCount() = dataList.size
    }

    private fun writeEmptyStr() {
        sp?.let {
            if (it.getString(App.KEY_NAME, "").toString().isEmpty()) {
                it.edit(true) {
                    putString(App.KEY_NAME, App.EMPTY_STR)
                }
            }
        }
    }

    private fun hideKeyboard() {
        requireActivity().currentFocus?.let {
            (requireActivity().getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager)
                .hideSoftInputFromWindow(it.windowToken, InputMethodManager.HIDE_NOT_ALWAYS)
        }
    }
}