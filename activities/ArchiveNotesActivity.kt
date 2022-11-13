package com.msa.mynotes.activities

import android.content.Intent
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.core.widget.addTextChangedListener
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.gms.ads.*
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.msa.mynotes.R
import com.msa.mynotes.adapters.MyNotesAdapter
import com.msa.mynotes.databinding.ActivityArchiveNotesBinding
import com.msa.mynotes.models.Notes

class ArchiveNotesActivity : AppCompatActivity() {
    lateinit var binding: ActivityArchiveNotesBinding
    val db = Firebase.firestore
    val notes = ArrayList<Notes>()
    val notesPin = ArrayList<Notes>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityArchiveNotesBinding.inflate(layoutInflater)
        setContentView(binding.root)
        // تعريف إعلان بانر
        MobileAds.initialize(this) {}
        val adView = AdView(this)
        adView.adUnitId = "ca-app-pub-7581259865493448/3113680431"

//        supportActionBar!!.hide()
//        MobileAds.initialize(this){}
//
//        mInterstitialAd = InterstitialAd(this)
//        mInterstitialAd.adUnitId = "ca-app-pub-3940256099942544~3347511713"
//
//        mInterstitialAd.loadAd(AdRequest.Builder().build())
//
//        if(mInterstitialAd.isLoaded){
//            mInterstitialAd.show()
//        }else{
//            Log.e("msa", "The interstitial wasn't loaded yet.")
//        }

        //  عشان أحدد شو يظهر بالBottomDialog
        val myPref = getSharedPreferences("MyPref", MODE_PRIVATE)
        val type = myPref.edit()
        type.putString("type", "adapterInArchive").apply()

        // لو كان التطبيق بالوضع الليلي يغير الألوان لوضع الليل
        val sharedSetting = getSharedPreferences("SettingsApp", MODE_PRIVATE)
        if(sharedSetting.getString("theme", "light") == "dark") {
            binding.notesRecyclerView.setBackgroundColor(Color.parseColor("#3A3939"))
            binding.tvArchive.setTextColor(Color.parseColor("#FFFFFF"))
        }

        getAllNotesInArchive()

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
                    binding.notesRecyclerView.visibility = View.GONE
                    binding.linearInSplash.visibility = View.VISIBLE
                    binding.tvNoNotes.setText(R.string.no_note_contain)
                }else{
                    binding.linearInSplash.visibility = View.GONE
                    binding.notesRecyclerView.visibility = View.VISIBLE
                    val notesAdapter = MyNotesAdapter(this@ArchiveNotesActivity, searchNotes)
                    binding.notesRecyclerView.adapter = notesAdapter
                    binding.notesRecyclerView.layoutManager = LinearLayoutManager(this@ArchiveNotesActivity)
                }
            }else{
                binding.linearInSplash.visibility = View.GONE
                binding.notesRecyclerView.visibility = View.VISIBLE
                val notesAdapter = MyNotesAdapter(this@ArchiveNotesActivity, notesPin)
                binding.notesRecyclerView.adapter = notesAdapter
                binding.notesRecyclerView.layoutManager = LinearLayoutManager(this@ArchiveNotesActivity)
            }
        }

    }

    private fun getAllNotesInArchive(){
        val sharedPref = getSharedPreferences("UserMobile", MODE_PRIVATE)
        val mobile = sharedPref.getString("mobile", "null").toString()

        db.collection("Notes").orderBy("updatedAt", Query.Direction.DESCENDING)
            .whereEqualTo("userMobile", mobile)
            .whereEqualTo("isDeleted", 0)
            .whereEqualTo("inArchives", 1)
            .get()
            .addOnSuccessListener { documentReference ->
                // اذا فش بيانات بيظهر اعلان بانر بالصفحة.. واذا في بيانات هيظهر البيانات مباشرة
                if(documentReference.isEmpty){
                    binding.notesRecyclerView.visibility = View.GONE
                    binding.linearInSplash.visibility = View.VISIBLE
                    binding.adView.visibility = View.VISIBLE

                    val adRequest = AdRequest.Builder().build()
                    binding.adView.loadAd(adRequest)
                }else{
                    binding.linearInSplash.visibility = View.GONE
                    binding.notesRecyclerView.visibility = View.VISIBLE
                }

                // بفصل الملاحظات المثبتة عن الغير مثبتة بدل ما أجيب البيانات مرتين
                for(note in documentReference){
                    if(note.get("isFixed").toString().toInt() == 1){
                        val userNote = Notes(note.id, note.getString("userMobile").toString(),
                            note.getString("title").toString(), note.getString("description").toString(),
                            note.getString("createdAt").toString(), note.getString("updatedAt").toString(),
                            note.getString("deletedAt").toString(), 1, 1, 0,
                            note.getString("color").toString(), note.getString("labelId").toString())
                        notesPin.add(userNote)
                    }else{
                        val userNote = Notes(note.id, note.getString("userMobile").toString(),
                            note.getString("title").toString(), note.getString("description").toString(),
                            note.getString("createdAt").toString(), note.getString("updatedAt").toString(),
                            note.getString("deletedAt").toString(), 1, 0, 0,
                            note.getString("color").toString(), note.getString("labelId").toString())
                        notes.add(userNote)
                    }
                }

                notesPin.addAll(notes)

                val notesAdapter = MyNotesAdapter(this@ArchiveNotesActivity, notesPin)
                binding.notesRecyclerView.adapter = notesAdapter
                binding.notesRecyclerView.layoutManager = LinearLayoutManager(this@ArchiveNotesActivity)

            }
            .addOnFailureListener { error ->
                Toast.makeText(this@ArchiveNotesActivity, "Error,  ${error.message}", Toast.LENGTH_SHORT).show()
            }
    }

   // عشان لو ضغط على زر الرجوع يفتح fragment الاعدادات بدل الرئيسية
    override fun onBackPressed() {
        val intent = Intent(this@ArchiveNotesActivity, HomeActivity::class.java)
        intent.putExtra("activity", "Setting")
        startActivity(intent)
    }

}