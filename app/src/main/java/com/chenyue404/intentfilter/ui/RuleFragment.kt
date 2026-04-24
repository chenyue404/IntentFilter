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
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import android.widget.ToggleButton
import androidx.core.content.edit
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import com.chenyue404.intentfilter.App
import com.chenyue404.intentfilter.R
import com.chenyue404.intentfilter.dp2Px
import com.chenyue404.intentfilter.entity.RuleEntity
import com.chenyue404.intentfilter.visible
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class RuleFragment : Fragment() {
    private lateinit var rvList: RecyclerView
    private lateinit var btSave: ImageButton
    private lateinit var btAdd: ImageButton
    private lateinit var tvTip: TextView

    private val sp: SharedPreferences? by lazy { (requireActivity() as MainActivity).getSP() }
    private val listAdapter by lazy { RuleListAdapter() }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.fragment_rule, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        with(view) {
            rvList = findViewById(R.id.rvList)
            btSave = findViewById(R.id.btSave)
            btAdd = findViewById(R.id.btAdd)
            tvTip = findViewById(R.id.tvTip)
        }
        tvTip.text = Html.fromHtml(
            getString(R.string.tip_rule, App.SPLIT_LETTER),
            Html.FROM_HTML_MODE_LEGACY
        )
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
                listAdapter.dataList.any {
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
                    if (listAdapter.dataList.isEmpty()) ""
                    else Gson().toJson(listAdapter.dataList)
                )
            }
            startActivity(Intent(requireContext(), EmptyActivity::class.java))
            Toast.makeText(requireContext(), getString(R.string.saved), Toast.LENGTH_SHORT)
                .show()
        }
        btAdd.setOnClickListener {
            listAdapter.add(RuleEntity())
            rvList.scrollToPosition(listAdapter.dataList.size - 1)
        }
        readPerf()
        writeEmptyStr()
    }

    private fun readPerf() {
        val str = sp?.getString(App.KEY_NAME, "") ?: ""
        if (str.isEmpty()) return

        val type = TypeToken.getParameterized(MutableList::class.java, RuleEntity::class.java).type
        val list: MutableList<RuleEntity>? = try {
            Gson().fromJson(str, type)
        } catch (e: Exception) {
            null
        }

        listAdapter.setList(list ?: listOf())
    }

    private class RuleListAdapter() : RecyclerView.Adapter<RuleListAdapter.ViewHolder>() {
        private val _dataList = mutableListOf<RuleEntity>()

        val dataList: List<RuleEntity>
            get() = _dataList

        fun setList(list: List<RuleEntity>) {
            _dataList.clear()
            _dataList.addAll(list)
            notifyDataSetChanged()
        }

        fun delete(index: Int) {
            _dataList.removeAt(index)
            notifyItemRemoved(index)
        }

        fun add(ruleEntity: RuleEntity, index: Int = _dataList.size) {
            _dataList.add(index, ruleEntity)
            notifyItemInserted(index)
        }

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

            val actionTextWatcher = MyTextWatcher { rule, text ->
                rule.actionKeywords = text
                tbAction.visible(text.isNotEmpty())
            }
            val typeTextWatcher = MyTextWatcher { rule, text ->
                rule.typeKeywords = text
                tbType.visible(text.isNotEmpty())
            }
            val dataStringTextWatcher =
                MyTextWatcher { rule, text ->
                    rule.dataStringKeywords = text
                    tbDataString.visible(text.isNotEmpty())
                }
            val activityTextWatcher = MyTextWatcher { rule, text ->
                rule.activityKeywords = text
                tbActivity.visible(text.isNotEmpty())
            }
            val fromTextWatcher = MyTextWatcher { rule, text ->
                rule.from = text
                tbFrom.visible(text.isNotEmpty())
            }
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder = ViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.item_rule, parent, false)
        )

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val ruleEntity = _dataList[position]
            with(holder) {
                etAction.removeTextChangedListener(actionTextWatcher)
                etType.removeTextChangedListener(typeTextWatcher)
                etDataString.removeTextChangedListener(dataStringTextWatcher)
                etActivity.removeTextChangedListener(activityTextWatcher)
                etFrom.removeTextChangedListener(fromTextWatcher)

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

                etAction.addTextChangedListener(actionTextWatcher.updateRuleEntity(ruleEntity))
                etType.addTextChangedListener(typeTextWatcher.updateRuleEntity(ruleEntity))
                etDataString.addTextChangedListener(
                    dataStringTextWatcher.updateRuleEntity(
                        ruleEntity
                    )
                )
                etActivity.addTextChangedListener(activityTextWatcher.updateRuleEntity(ruleEntity))
                etFrom.addTextChangedListener(fromTextWatcher.updateRuleEntity(ruleEntity))

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
                    bindingAdapterPosition.takeIf { it >= 0 }?.let {
                        delete(it)
                    }
                }
            }
        }

        override fun getItemCount() = _dataList.size

        private class MyTextWatcher(private val onTextChanged: (RuleEntity, String) -> Unit) :
            android.text.TextWatcher {
            private var ruleEntity: RuleEntity? = null

            fun updateRuleEntity(ruleEntity: RuleEntity): MyTextWatcher {
                this.ruleEntity = ruleEntity
                return this
            }

            override fun afterTextChanged(s: android.text.Editable?) {
                ruleEntity?.let { onTextChanged(it, s.toString()) }
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        }
    }

    private fun writeEmptyStr() {
        sp?.let {
            if (it.getString(App.KEY_NAME, "").toString().isEmpty()) {
                it.edit(true) {
                    putString(App.KEY_NAME, "")
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