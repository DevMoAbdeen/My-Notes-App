package com.msa.mynotes.activities

import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.core.widget.addTextChangedListener
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.MobileAds
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.msa.mynotes.R
import com.msa.mynotes.adapters.MyNotesAdapter
import com.msa.mynotes.databinding.ActivityLabelNotesBinding
import com.msa.mynotes.models.Notes

class LabelNotesActivity : AppCompatActivity() {
    lateinit var binding: ActivityLabelNotesBinding
    val db = Firebase.firestore
    val notesPin = ArrayList<Notes>()
    val notes = ArrayList<Notes>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLabelNotesBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // تعريف إعلان بانر
        MobileAds.initialize(this) {}
        val adView = AdView(this)
        adView.adUnitId = "ca-app-pub-7581259865493448/3113680431"

        // عشان أحدد شو يظهر بالBottomDialog
        val myPref = getSharedPreferences("MyPref", MODE_PRIVATE)
        val type = myPref.edit()
        type.putString("type", "adapterInLabelNotes").apply()

        // لو كان التطبيق بالوضع الليلي يغير الألوان لوضع الليل
        val sharedSetting = getSharedPreferences("SettingsApp", AppCompatActivity.MODE_PRIVATE)
        if(sharedSetting.getString("theme", "light") == "dark") {
            binding.recyclerViewNotesLabel.setBackgroundColor(Color.parseColor("#3A3939"))
            binding.tvLabelnotes.setTextColor(Color.parseColor("#FFFFFF"))
        }

        val labelId = intent.getStringExtra("labelID").toString()
        getAllNotes(labelId)

        //  كل ما يغير بالنص اللي بالبحث يبحث اذا في نص هيبحث عنه بكل الملاحظات.. واذا فش هيظهر كل الملاحظات
        binding.etSearch.addTextChangedListener {
            val searchNotes = ArrayList<Notes>()
            val search = binding.etSearch.text.toString()
            if(search.isNotEmpty()){
                // البحث بكون من الArrayList عشان البحث يكون سريع.. وببحث عن النص بالعنوان والملاحظة
                for(i in 0 until notesPin.size){
                    if(notesPin[i].title.contains(search, true) ||
                        notesPin[i].description.contains(search, true)){
                        searchNotes.add(notesPin[i])
                    }
                }
                if(searchNotes.isEmpty()){
                    binding.recyclerViewNotesLabel.visibility = View.GONE
                    binding.linearInLabelsNotes.visibility = View.VISIBLE
                }else{
                    binding.linearInLabelsNotes.visibility = View.GONE
                    binding.recyclerViewNotesLabel.visibility = View.VISIBLE
                    val notesAdapter = MyNotesAdapter(this@LabelNotesActivity, searchNotes)
                    binding.recyclerViewNotesLabel.adapter = notesAdapter
                    binding.recyclerViewNotesLabel.layoutManager = LinearLayoutManager(this@LabelNotesActivity)
                }
            }else{
                binding.linearInLabelsNotes.visibility = View.GONE
                binding.recyclerViewNotesLabel.visibility = View.VISIBLE
                val notesAdapter = MyNotesAdapter(this@LabelNotesActivity, notesPin)
                binding.recyclerViewNotesLabel.adapter = notesAdapter
                binding.recyclerViewNotesLabel.layoutManager = LinearLayoutManager(this@LabelNotesActivity)
            }
        }


    }

    private fun getAllNotes(labelId: String){
        val sharedPref = getSharedPreferences("UserMobile", MODE_PRIVATE)
        val mobile = sharedPref.getString("mobile", "null").toString()

        db.collection("Notes").orderBy("updatedAt", Query.Direction.DESCENDING)
            .whereEqualTo("userMobile", mobile)
            .whereEqualTo("isDeleted", 0)
            .whereEqualTo("inArchives", 0)
            .whereEqualTo("labelId", labelId)
            .get()
            .addOnSuccessListener { documentReference ->
                // اذا فش بيانات بيظهر اعلان بانر بالصفحة.. واذا في بيانات هيظهر البيانات مباشرة
                if(documentReference.isEmpty){
                    binding.recyclerViewNotesLabel.visibility = View.GONE
                    binding.linearInLabelsNotes.visibility = View.VISIBLE
                    binding.tvNoNotesLabel.setText(R.string.noNote_inLabel)
                    binding.adView.visibility = View.VISIBLE

                    val adRequest = AdRequest.Builder().build()
                    binding.adView.loadAd(adRequest)
                }else {
                    binding.linearInLabelsNotes.visibility = View.GONE

                    // بفصل الملاحظات المثبتة عن الغير مثبتة بدل ما أجيب البيانات مرتين
                    for (note in documentReference) {
                        if (note.get("isFixed").toString().toInt() == 1) {
                            val userNote = Notes(note.id, note.getString("userMobile").toString(),
                                note.getString("title").toString(), note.getString("description").toString(),
                                note.getString("createdAt").toString(), note.getString("updatedAt").toString(),
                                note.getString("deletedAt").toString(), 0, 1, 0,
                                note.getString("color").toString(), note.getString("labelId").toString())
                            notesPin.add(userNote)
                        } else {
                            val userNote = Notes(note.id, note.getString("userMobile").toString(),
                                note.getString("title").toString(), note.getString("description").toString(),
                                note.getString("createdAt").toString(), note.getString("updatedAt").toString(),
                                note.getString("deletedAt").toString(), 0, 0, 0,
                                note.getString("color").toString(), note.getString("labelId").toString())
                            notes.add(userNote)
                        }
                    }
                }
                // بضيف الملاحظات الغير مثبتة على الملاحظات المثبتة عشان المثبتة تكون بالأول
                notesPin.addAll(notes)

                val notesAdapter = MyNotesAdapter(this@LabelNotesActivity, notesPin)
                binding.recyclerViewNotesLabel.adapter = notesAdapter
                binding.recyclerViewNotesLabel.layoutManager = GridLayoutManager(this@LabelNotesActivity, 1)
            }
            .addOnFailureListener { error ->
                Toast.makeText(this@LabelNotesActivity, "Error,  ${error.message}", Toast.LENGTH_SHORT).show()
                Log.e("msa", "Error,  ${error.message}")
            }
    }

}