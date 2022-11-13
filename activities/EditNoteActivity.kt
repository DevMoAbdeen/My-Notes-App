package com.msa.mynotes.activities

import android.content.Intent
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.widget.addTextChangedListener
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.msa.mynotes.R
import com.msa.mynotes.brodcast_receiver.MyReceiver
import com.msa.mynotes.databinding.ActivityEditNoteBinding
import com.msa.mynotes.fragmenst.BottomDialogFragment
import com.msa.mynotes.models.Notes
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.HashMap

class EditNoteActivity : AppCompatActivity() {
    lateinit var binding: ActivityEditNoteBinding
    val db = Firebase.firestore
    lateinit var documentID: String
    var countArchives = 0; var countFixed = 0
    var inArchives = 0; var isFixed = 0

    companion object{
        var isChange = false
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditNoteBinding.inflate(layoutInflater)
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

        // بستقبل البيانات اللي برسلها لما أضغط على الملاحظة عشان أعرض البيانات أو أغيرهم
        documentID = intent.getStringExtra("noteID").toString()
        val title = intent.getStringExtra("title").toString()
        val description = intent.getStringExtra("description").toString()
        val date = intent.getStringExtra("date").toString()
        inArchives = intent.getIntExtra("inArchives", 0).toString().toInt()
        isFixed = intent.getIntExtra("isFixed", 0).toString().toInt()
        Notes.color = intent.getStringExtra("color").toString()
        Notes.labelId = intent.getStringExtra("labelId").toString()
        countFixed = isFixed
        countArchives = inArchives

        binding.etTitle.setText(title)
        binding.etDescription.setText(description)
        binding.tvTime.setText("$date")

        if(inArchives == 0){
            binding.archive.setImageResource(R.drawable.ic_archive)
        }else{
            binding.archive.setImageResource(R.drawable.ic_unarchive)
        }

        if(isFixed == 0){
            binding.pin.background = null
        }else{
            binding.pin.setBackgroundColor(resources.getColor(R.color.white))
        }

        // لو كتب اشي أو غير اشي على المكتوب بخلي قيمة isChange true عشان أحول زر الرجوع للحفظ
        binding.etTitle.addTextChangedListener {
            binding.btnSaveNote.visibility = View.VISIBLE
            isChange = true
        }

        // لو كتب اشي أو غير اشي على المكتوب بخلي قيمة isChange true عشان أحول زر الرجوع للحفظ
        binding.etDescription.addTextChangedListener {
            binding.btnSaveNote.visibility = View.VISIBLE
            isChange = true
        }

        binding.back.setOnClickListener {
            super.onBackPressed()
        }

        binding.btnSaveNote.setOnClickListener {
            binding.back.isClickable = false
            binding.pin.isClickable = false
            binding.archive.isClickable = false
            binding.more.isClickable = false
            binding.etTitle.isEnabled = false
            binding.etDescription.isEnabled = false

            updateNote()
        }

        binding.archive.setOnClickListener {
            binding.btnSaveNote.visibility = View.VISIBLE
            isChange = true
            countArchives++
            if(countArchives % 2 == 1){
                binding.archive.setImageResource(R.drawable.ic_unarchive_white)
            }else{
                binding.archive.setImageResource(R.drawable.ic_archive_white)
            }
        }

        binding.pin.setOnClickListener {
            binding.btnSaveNote.visibility = View.VISIBLE
            isChange = true
            countFixed++
            if(countFixed % 2 == 0){
                binding.pin.background = null
            }else{
                binding.pin.setBackgroundColor(resources.getColor(R.color.white))
            }
        }

//        binding.share.setOnClickListener {
//            val title = binding.etTitle.text.toString().trim()
//            val description = binding.etDescription.text.toString().trim()
//
//            if (title.isNotEmpty() || description.isNotEmpty()) {
//                val text = "$title.\n$description."
//                val intent = Intent(Intent.ACTION_SEND)
//                intent.setType("text/plain")
//                intent.putExtra(Intent.EXTRA_TEXT, text)
//                startActivity(intent)
//            }else{
//                Toast.makeText(this@EditNoteActivity, "Write note to share it !", Toast.LENGTH_SHORT).show()
//            }
//        }

        binding.more.setOnClickListener {
            val bottomDialog = BottomDialogFragment(object: BottomDialogFragment.OnNoteListener{
                override fun deleteNote(position: Int) {}
            })
            // برسل البيانات عشان لو بده ينسخ الملاحظة أو يشاركها
            val sharedPref = getSharedPreferences("MyPref", MODE_PRIVATE)
            val notes = sharedPref.edit()
            notes.putString("type", "editNote")
            notes.putString("noteId", documentID)
            notes.putString("title", binding.etTitle.text.toString())
            notes.putString("description", binding.etDescription.text.toString())
            notes.apply()

            bottomDialog.show(supportFragmentManager, "showMore")
        }



    }

    private fun changeNote(noteId: String, title: String, description: String, updatedAt: String,
                           inArchives: Int, isFixed: Int, color: String, labelId: String){
        val note = HashMap<String, Any>()
        note["title"] = title
        note["description"] = description
        note["updatedAt"] = updatedAt
        note["inArchives"] = inArchives
        note["isFixed"] = isFixed
        note["color"] = color
        note["labelId"] = labelId

        db.collection("Notes").document(noteId)
            .update(note)
            .addOnSuccessListener {
                Toast.makeText(this@EditNoteActivity, R.string.note_updated, Toast.LENGTH_SHORT).show()
                val activity = intent.getIntExtra("inArchives", 0).toString().toInt()
                if(activity == 0) {
                    startActivity(Intent(this, HomeActivity::class.java))
                    finish()
                }else{
                    startActivity(Intent(this, ArchiveNotesActivity::class.java))
                    finish()
                }
            }

            .addOnFailureListener { error ->
                Toast.makeText(this@EditNoteActivity, "Error: ${error.message}", Toast.LENGTH_LONG).show()
            }
    }

    private fun updateNote(){
        val title = binding.etTitle.text.toString().trim()
        val description = binding.etDescription.text.toString().trim()

        // اذا حذف كل الكلام المكتوب يعني نحذف الملاحظة تماما
        if(title.isEmpty() && description.isEmpty()){
            deleteNote(documentID)
            val activity = intent.getIntExtra("inArchives", 0).toString().toInt()
            if(activity == 0) {
                startActivity(Intent(this, HomeActivity::class.java))
                finish()
            }else{
                startActivity(Intent(this, ArchiveNotesActivity::class.java))
                finish()
            }
        }else {
            val updatedAt = SimpleDateFormat("yyyy/MM/dd - HH:mm").format(Date())
//        val date = SimpleDateFormat("HH:mm").format(Date())

            if (countArchives % 2 == 0) {
                inArchives = 0
            } else {
                inArchives = 1
            }

            if (countFixed % 2 == 0) {
                isFixed = 0
            } else {
                isFixed = 1
            }
            val myReceiver = MyReceiver()
            if (myReceiver.checkConnection(this)) {
                changeNote(documentID, title, description, updatedAt, inArchives, isFixed, Notes.color, Notes.labelId)
            } else {
                changeNote(documentID, title, description, updatedAt, inArchives, isFixed, Notes.color, Notes.labelId)
                Toast.makeText(this, R.string.note_updated, Toast.LENGTH_SHORT).show()
                val activity = intent.getIntExtra("inArchives", 0).toString().toInt()
                if(activity == 0) {
                    startActivity(Intent(this, HomeActivity::class.java))
                    finish()
                }else{
                    startActivity(Intent(this, ArchiveNotesActivity::class.java))
                    finish()
                }
            }
        }

    }

    override fun onBackPressed() {
        super.onBackPressed()
        if(! isChange){
            super.onBackPressed()
        }else{
            val alert = AlertDialog.Builder(this@EditNoteActivity)
            alert.setTitle(R.string.save)
            alert.setMessage(R.string.save_change)
            alert.setIcon(R.drawable.ic_save)
            alert.setCancelable(false)

            alert.setPositiveButton(R.string.yes){d, i ->
                updateNote()
            }

            alert.setNegativeButton("No"){d, i ->
                Toast.makeText(this@EditNoteActivity, R.string.not_change, Toast.LENGTH_SHORT).show()
                super.onBackPressed()
            }

            alert.create().show()
        }
    }

    private fun deleteNote(noteId: String){
        db.collection("Notes").document(noteId)
            .delete()
            .addOnSuccessListener {
            }

            .addOnFailureListener { error ->
                Toast.makeText(this@EditNoteActivity, "Delete failed.\n${error.message}", Toast.LENGTH_SHORT).show()
            }
    }

}