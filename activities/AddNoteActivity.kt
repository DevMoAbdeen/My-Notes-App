package com.msa.mynotes.activities

import android.content.Intent
import android.content.IntentFilter
import android.graphics.Color
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.widget.addTextChangedListener
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.msa.mynotes.R
import com.msa.mynotes.brodcast_receiver.MyReceiver
import com.msa.mynotes.databinding.ActivityAddNoteBinding
import com.msa.mynotes.fragmenst.BottomDialogFragment
import com.msa.mynotes.models.Notes
import java.text.SimpleDateFormat
import java.util.*

class AddNoteActivity : AppCompatActivity() {
    lateinit var binding: ActivityAddNoteBinding
    val db = Firebase.firestore
    var countArchives = 0; var countFixed = 0
    var inArchives = 0; var isFixed = 0
    var type = "back"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddNoteBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // عشان أسمح بعمل scroll على الملاحظة
        binding.description.isVerticalScrollBarEnabled = true

        // لو كان التطبيق بالوضع الليلي يغير الألوان لوضع الليل
        val sharedSetting = getSharedPreferences("SettingsApp", MODE_PRIVATE)
        if(sharedSetting.getString("theme", "light") == "dark") {
            binding.activity.setBackgroundColor(Color.parseColor("#253557"))
            binding.linear.setBackgroundColor(Color.parseColor("#000000"))
            binding.etTitle.setTextColor(Color.parseColor("#ffffff"))
            binding.etDescription.setTextColor(Color.parseColor("#ffffff"))
        }

        val time = SimpleDateFormat("HH:mm").format(Date())
        binding.tvTime.text = "Last edit: $time"

        //  كل ما يغير بالنص اللي بالعنوان يبحث اذا في كلام بالعنوان أوالملاحظة يحط صورة الحفظ واذا فش كلام فيهم يحط صورة السهم
        binding.etTitle.addTextChangedListener {
            val title = binding.etTitle.text.toString().trim()
            val description = binding.etDescription.text.toString().trim()
            if (title.isNotEmpty() || description.isNotEmpty()) {
                binding.back.setImageResource(R.drawable.ic_save)
                type = "save"
            } else {
                binding.back.setImageResource(R.drawable.ic_back)
                type = "back"
            }
        }

        //  كل ما يغير بالنص اللي بالعنوان يبحث اذا في كلام بالعنوان أوالملاحظة يحط صورة الحفظ واذا فش كلام فيهم يحط صورة السهم
        binding.etDescription.addTextChangedListener {
            val title = binding.etTitle.text.toString().trim()
            val description = binding.etDescription.text.toString().trim()
            if (title.isNotEmpty() || description.isNotEmpty()) {
                binding.back.setImageResource(R.drawable.ic_save)
                type = "save"
            } else {
                binding.back.setImageResource(R.drawable.ic_back)
                type = "back"
            }
        }


        binding.back.setOnClickListener {
            // اذا القيمة كانت save معناه في كلام مكتوب وبده يحفظه.. لو القيمة back معناها فش كلام مكتوب وبده يرجع
            if( type == "save"){
                binding.back.isClickable = false
                binding.pin.isClickable = false
                binding.archive.isClickable = false
                binding.more.isClickable = false
                binding.etTitle.isEnabled = false
                binding.etDescription.isEnabled = false

                val title = binding.etTitle.text.toString().trim()
                val description = binding.etDescription.text.toString().trim()
                saveNote(title, description)
            }else {
                super.onBackPressed()
            }
        }

        binding.archive.setOnClickListener {
            // كل ما يضغط على الصورة القيمة هتزيد واحد.. ولو القيمة لا تقبل القسمة على 2 معناها حطها بالأرشفة
            countArchives++
            if(countArchives % 2 == 1){
                binding.archive.setImageResource(R.drawable.ic_unarchive_white)
            }else{
                binding.archive.setImageResource(R.drawable.ic_archive_white)
            }
        }

        binding.pin.setOnClickListener {
            // كل ما يضغط على الصورة القيمة هتزيد واحد.. ولو القيمة لا تقبل القسمة على 2 معناها ثبت الملاحظة
            countFixed++
            if(countFixed % 2 == 1){
                binding.pin.background = null
            }else{
                binding.pin.setBackgroundColor(resources.getColor(R.color.white))
            }
        }

        binding.more.setOnClickListener {
            val bottomDialog = BottomDialogFragment(object: BottomDialogFragment.OnNoteListener{
                override fun deleteNote(position: Int) {}
            })
            // برسل البيانات عشان لو بده ينسخ الملاحظة أو يشاركها
            val sharedPref = getSharedPreferences("MyPref", MODE_PRIVATE)
            val notes = sharedPref.edit()
            notes.putString("type", "addNote")
            notes.putString("title", binding.etTitle.text.toString())
            notes.putString("description", binding.etDescription.text.toString())
            notes.apply()

            bottomDialog.show(supportFragmentManager, "showMore")
        }

    }

    override fun onBackPressed() {
        val title = binding.etTitle.text.toString().trim()
        val description = binding.etDescription.text.toString().trim()

        if (title.isEmpty() && description.isEmpty()) {
            super.onBackPressed()
        } else {
            val alert = AlertDialog.Builder(this@AddNoteActivity)
            alert.setTitle(R.string.save)
            alert.setMessage(R.string.want_save)
            alert.setIcon(R.drawable.ic_save)
            alert.setCancelable(true)

            alert.setPositiveButton(R.string.yes){d, i ->
                saveNote(title, description)
            }

            alert.setNegativeButton(R.string.no){d, i ->
                Toast.makeText(this@AddNoteActivity, "Note Not Added", Toast.LENGTH_SHORT).show()
                super.onBackPressed()
            }
            alert.create().show()
        }
    }

    private fun saveNote(title: String, description: String){
        val createdAt = SimpleDateFormat("yyyy/MM/dd - HH:mm").format(Date())

        // اذا الباقي 1 معناها حطها بالأرشيف
        if(countArchives % 2 == 1){
            inArchives = 1
        }

        // اذا الباقي 1 معناها ثبتها
        if(countFixed % 2 == 1){
            isFixed = 1
        }

        val sharedPref = getSharedPreferences("UserMobile", MODE_PRIVATE)
        val mobile = sharedPref.getString("mobile", null).toString()

        // بفحص اذا في نت بنفذ الاضافة على الفايربيز مباشرة.. واذا فش نت بضيف وبنفذ باقي الكود
        val myReceiver = MyReceiver()
        if(myReceiver.checkConnection(this)){
            addNote(mobile, title, description, createdAt, inArchives, isFixed, Notes.color, Notes.labelId)
        }else{
            addNote(mobile, title, description, createdAt, inArchives, isFixed, Notes.color, Notes.labelId)
            Toast.makeText(this, R.string.note_added, Toast.LENGTH_SHORT).show()
            startActivity(Intent(this, HomeActivity::class.java))
            finish()
        }


    }

    private fun addNote(userMobile: String, title: String, description: String,
                        createdAt: String, inArchives: Int, isFixed: Int, color: String, labelId: String) {
// Create a new user with a first and last name
        val note = hashMapOf(
            "userMobile" to userMobile,
            "title" to title,
            "description" to description,
            "createdAt" to createdAt,
            "updatedAt" to createdAt,
            "deletedAt" to "",
            "inArchives" to inArchives,
            "isFixed" to isFixed,
            "isDeleted" to 0,
            "color" to color,
            "labelId" to labelId
        )

// Add a new document with a generated ID
        db.collection("Notes")
            .add(note)
            .addOnSuccessListener { documentReference ->
                Toast.makeText(this@AddNoteActivity, R.string.note_added, Toast.LENGTH_SHORT).show()
                val intent = Intent(this, HomeActivity::class.java)
                startActivity(intent)
                finish()
            }
            .addOnFailureListener { error ->
                Toast.makeText(this@AddNoteActivity, "Error,  ${error.message}", Toast.LENGTH_SHORT).show()
            }
    }

}