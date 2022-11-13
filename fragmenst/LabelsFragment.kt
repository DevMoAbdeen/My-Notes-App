package com.msa.mynotes.fragmenst

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.addTextChangedListener
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.msa.mynotes.R
import com.msa.mynotes.activities.HomeActivity
import com.msa.mynotes.activities.LoginActivity
import com.msa.mynotes.adapters.MyLabelsAdapter
import com.msa.mynotes.adapters.MyNotesAdapter
import com.msa.mynotes.databinding.FragmentLabelsBinding
import com.msa.mynotes.models.Labels
import com.msa.mynotes.models.Notes

class LabelsFragment : Fragment() {
    lateinit var labelBinding: FragmentLabelsBinding
    val db = Firebase.firestore
    lateinit var mobile: String

    companion object {
        val labels = ArrayList<Labels>()
        lateinit var labelsAdapter: MyLabelsAdapter
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        labelBinding = FragmentLabelsBinding.inflate(inflater, container, false)

        // اذا فش رقم جوال محفوظ بالsharedPref ينقله على واجهة تسجيل الدخول عشان ميحفظش ملاحظات برقم مش موجود
        val sharedPref = requireContext().getSharedPreferences("UserMobile", Context.MODE_PRIVATE)
        mobile = sharedPref.getString("mobile", "null").toString()
        if(mobile == "null"){
            requireActivity().startActivity(Intent(requireContext(), LoginActivity::class.java))
            requireActivity().finish()
        }

        // لو كان التطبيق بالوضع الليلي يغير الألوان لوضع الليل
        val sharedSetting = requireActivity().getSharedPreferences("SettingsApp", AppCompatActivity.MODE_PRIVATE)
        if(sharedSetting.getString("theme", "light") == "dark") {
            labelBinding.labelsRecyclerView.setBackgroundColor(Color.parseColor("#3A3939"))
            labelBinding.tvLabels.setTextColor(Color.parseColor("#FFFFFF"))
        }

//        getAllLabels()
//        if(labels.isEmpty()) {
//            getAllLabels()
//        }else{
//            labelsAdapter = MyLabelsAdapter(requireActivity(), labels, object: MyLabelsAdapter.OnLabelListener{
//                override fun deleteLabel(position: Int) {
//                    labels.removeAt(position)
//                }
//            })
//            labelBinding.labelsRecyclerView.adapter = labelsAdapter
//            labelBinding.labelsRecyclerView.layoutManager = LinearLayoutManager(requireContext())
//        }

        //  كل ما يغير بالنص اللي بالبحث يبحث اذا في نص هيبحث عنه بكل الملاحظات.. واذا فش هيظهر كل الملاحظات
        labelBinding.etSearch.addTextChangedListener {
            val searchNotes = ArrayList<Labels>()
            val search = labelBinding.etSearch.text.toString()
            if(search.isNotEmpty()){
                // البحث بكون من الArrayList عشان البحث يكون سريع.. وببحث عن النص بالعنوان والملاحظة
                for(i in 0 until labels.size){
                    if(labels[i].name.contains(search, true) ||
                        labels[i].about.contains(search, true)){
                        searchNotes.add(labels[i])
                    }
                }
                if(searchNotes.isEmpty()){
                    labelBinding.labelsRecyclerView.visibility = View.GONE
                    labelBinding.linearLabels.visibility = View.VISIBLE
                    labelBinding.tvNoLabels.setText(R.string.no_label_containWord)
                }else{
                    labelBinding.linearLabels.visibility = View.GONE
                    labelBinding.labelsRecyclerView.visibility = View.VISIBLE

                    val labelsAdapter = MyLabelsAdapter(requireActivity(), searchNotes, object: MyLabelsAdapter.OnLabelListener{
                        override fun deleteLabel(position: Int) {
                            labels.removeAt(position)
                        }
                    })
                    labelBinding.labelsRecyclerView.adapter = labelsAdapter
                    labelBinding.labelsRecyclerView.layoutManager = LinearLayoutManager(requireContext())
                }
            }else{
                labelBinding.linearLabels.visibility = View.GONE
                labelBinding.labelsRecyclerView.visibility = View.VISIBLE

                val labelsAdapter = MyLabelsAdapter(requireActivity(), labels, object: MyLabelsAdapter.OnLabelListener{
                    override fun deleteLabel(position: Int) {
                        labels.removeAt(position)
                    }
                })
                labelBinding.labelsRecyclerView.adapter = labelsAdapter
                labelBinding.labelsRecyclerView.layoutManager = LinearLayoutManager(requireContext())
            }
        }



        return labelBinding.root
    }

    fun getAllLabels(){
        labels.removeAll(labels)
        db.collection("Labels").orderBy("date", Query.Direction.DESCENDING)
            .whereEqualTo("userMobile", mobile)
            .get()
            .addOnSuccessListener { documentReference ->
                if(documentReference.isEmpty){
                    labelBinding.labelsRecyclerView.visibility = View.GONE
                    labelBinding.linearLabels.visibility = View.VISIBLE
                }else {
                    labelBinding.linearLabels.visibility = View.GONE
                    labelBinding.labelsRecyclerView.visibility = View.VISIBLE

                    for (label in documentReference) {
                        val userLabel = Labels(label.id, label.getString("userMobile").toString(),
                            label.getString("labelName").toString(), label.getString("aboutLabel").toString(),
                            label.getString("date").toString())

                        labels.add(userLabel)
                    }

                    labelsAdapter = MyLabelsAdapter(requireActivity(), labels, object: MyLabelsAdapter.OnLabelListener{
                        override fun deleteLabel(position: Int) {
                            labels.removeAt(position)
                        }
                    })
                    labelBinding.labelsRecyclerView.adapter = labelsAdapter
                    labelBinding.labelsRecyclerView.layoutManager = LinearLayoutManager(requireContext())
                }
            }
            .addOnFailureListener {error ->
                Toast.makeText(requireContext(), "Error,  ${error.message}", Toast.LENGTH_SHORT).show()
                Log.e("msa", "Error,  ${error.message}")
            }
    }

    override fun onResume() {
        super.onResume()
        // هيتحقق الشرط التاني بعد ما أضيف Label
        if(labels.isEmpty() || Labels.fragment == "LabelDialogFragment") {
            getAllLabels()
            // برجع القيمة عشان لما أرجع لل LabelFragment كمان مرة
            // متكونش القيمة نفسها ويضل يجيب البيانات من الفايربيز بكل مرة
            Labels.fragment = "LabelsFragment"
        }else{
            labelsAdapter = MyLabelsAdapter(requireActivity(), labels, object: MyLabelsAdapter.OnLabelListener{
                override fun deleteLabel(position: Int) {
                    labels.removeAt(position)
                }
            })
            labelBinding.labelsRecyclerView.adapter = labelsAdapter
            labelBinding.labelsRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        }

    }

}