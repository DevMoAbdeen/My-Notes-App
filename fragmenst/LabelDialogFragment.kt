package com.msa.mynotes.fragmenst

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.msa.mynotes.R
import com.msa.mynotes.activities.LoginActivity
import com.msa.mynotes.brodcast_receiver.MyReceiver
import com.msa.mynotes.databinding.FragmentLabelDialogBinding
import com.msa.mynotes.models.Labels
import java.text.SimpleDateFormat
import java.util.*

class LabelDialogFragment(var onLabelListener: OnLabelListener) : BottomSheetDialogFragment() {
    interface OnLabelListener{
        fun addLabel(label: Labels)
        fun updateLabel(label: Labels)
    }

    private lateinit var labelBinding: FragmentLabelDialogBinding
    val db = Firebase.firestore
    lateinit var mobile: String
    var labelId = ""

    @RequiresApi(Build.VERSION_CODES.S)
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        labelBinding = FragmentLabelDialogBinding.inflate(inflater, container, false)

        // اذا فش رقم جوال محفوظ بالsharedPref ينقله على واجهة تسجيل الدخول عشان ميحفظش ملاحظات برقم مش موجود
        val myMobile = requireContext().getSharedPreferences("UserMobile", Context.MODE_PRIVATE)
        mobile = myMobile.getString("mobile", "null").toString()
        if(mobile == "null"){
            requireActivity().startActivity(Intent(requireContext(), LoginActivity::class.java))
            requireActivity().finish()
        }

        // لو كان التطبيق بالوضع الليلي يغير الألوان لوضع الليل
        val sharedPref = requireContext().getSharedPreferences("MyLabel", AppCompatActivity.MODE_PRIVATE)
        val sharedSetting = requireActivity().getSharedPreferences("SettingsApp", AppCompatActivity.MODE_PRIVATE)
        if(sharedSetting.getString("theme", "light") == "dark") {
            labelBinding.activity.setBackgroundColor(Color.parseColor("#253557"))
            labelBinding.btnAdd.setBackgroundColor(Color.parseColor("#FFFFFF"))
            labelBinding.btnAdd.setTextColor(Color.parseColor("#253557"))
            labelBinding.btnCancel.setBackgroundColor(Color.parseColor("#253557"))
            labelBinding.btnCancel.setTextColor(Color.parseColor("#FFFFFF"))
        }

        // لو كان النوع تعديل خليه يعرض البيانات بدل ما يكونوا فاضيين
        if(Labels.type == "updateLabel"){
            labelBinding.btnAdd.text = "${R.string.save_label}"
            labelBinding.etLabelName.setText(sharedPref.getString("name", "Exception"))
            labelBinding.etAboutLabel.setText(sharedPref.getString("about", "Exception"))
        }


        labelBinding.btnAdd.setOnClickListener {
            val name = labelBinding.etLabelName.text.toString().trim()
            val about = labelBinding.etAboutLabel.text.toString().trim()

            if(name.isEmpty() || about.isEmpty()){
                Toast.makeText(requireContext(), R.string.fill_fields, Toast.LENGTH_SHORT).show()
            }else{
                labelBinding.btnAdd.isClickable = false
                labelBinding.btnCancel.isClickable = false
                labelBinding.etLabelName.isEnabled = false
                labelBinding.etAboutLabel.isEnabled = false

                if(Labels.type == "newLabel"){
                    // بغير القيمة عشان لما ارجع على LabelFragment لو كانت قيمة Labels.fragment
                        // تساوي LabelDialogFragment هيحدث على الrecyclerView
                    Labels.fragment = "LabelDialogFragment"
                    val date = SimpleDateFormat("yyyy/MM/dd - HH:mm").format(Date())

                    val myReceiver = MyReceiver()
                    if(myReceiver.checkConnection(requireContext())) {
                        addLabel(mobile, name, about, date)
                    }else {
                        addLabel(mobile, name, about, date)
                        Toast.makeText(requireContext(), R.string.label_added, Toast.LENGTH_SHORT).show()
                        dismiss()

                        val label = Labels(labelId, mobile, name, about, date)
                        onLabelListener.addLabel(label)
                    }
                }else{ // if(Labels.type == "updateLabel")
                    var id = sharedPref.getString("labelId", "").toString()
                    if(id.isEmpty()) {
                        id = labelId
                    }
                    val date = SimpleDateFormat("yyyy/MM/dd - HH:mm").format(Date())

                    val myReceiver = MyReceiver()
                    if(myReceiver.checkConnection(requireContext())) {
                        updateLabel(id, name, about, date)
                    }else{
                        updateLabel(id, name, about, date)
                        Toast.makeText(requireContext(), R.string.label_updated, Toast.LENGTH_SHORT).show()
                        dismiss()
                    }
                    val label = Labels(id, mobile, name, about, date)
                    onLabelListener.updateLabel(label)
                }
            }
        }

        labelBinding.btnCancel.setOnClickListener{
            dismiss()
        }


        return labelBinding.root
    }

    fun addLabel(userMobile: String, name: String, about: String, date: String){
        val label = hashMapOf(
            "userMobile" to userMobile,
            "labelName" to name,
            "aboutLabel" to about,
            "date" to date
        )

        db.collection("Labels")
            .add(label)
            .addOnSuccessListener { documentReference ->
                Toast.makeText(requireContext(), R.string.label_added, Toast.LENGTH_SHORT).show()
                labelId = documentReference.id

                val label = Labels(labelId, mobile, name, about, date)
                onLabelListener.addLabel(label)
                dismiss()
            }
            .addOnFailureListener { error ->
                Toast.makeText(requireContext(), "Error,  ${error.message}", Toast.LENGTH_SHORT).show()
            }
    }

    fun updateLabel(labelId: String, name: String, about: String, date: String){
        val label = HashMap<String, Any>()
        label["labelName"] = name
        label["aboutLabel"] = about
        label["date"] = date

        db.collection("Labels").document(labelId)
            .update(label)
            .addOnSuccessListener {
                Toast.makeText(requireContext(), R.string.label_updated, Toast.LENGTH_SHORT).show()
                dismiss()
            }
            .addOnFailureListener { error ->
                Toast.makeText(requireContext(), "Error: ${error.message}", Toast.LENGTH_LONG).show()
            }
    }

}