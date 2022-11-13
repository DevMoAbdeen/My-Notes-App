package com.msa.mynotes.fragmenst

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Context.CLIPBOARD_SERVICE
import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.msa.mynotes.R
import com.msa.mynotes.activities.ArchiveNotesActivity
import com.msa.mynotes.activities.EditNoteActivity
import com.msa.mynotes.activities.HomeActivity
import com.msa.mynotes.brodcast_receiver.MyReceiver
import com.msa.mynotes.databinding.FragmentBottomDialogBinding
import com.msa.mynotes.models.Labels
import com.msa.mynotes.models.Notes
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

class BottomDialogFragment(var onNoteListener: OnNoteListener) : BottomSheetDialogFragment() {
    interface OnNoteListener{
        fun deleteNote(position: Int)
    }

    private lateinit var dialogBinding: FragmentBottomDialogBinding
    val db = Firebase.firestore
    val labelsObject = ArrayList<Labels>()
    val labelsName = ArrayList<String>()

    @RequiresApi(Build.VERSION_CODES.Q)
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        dialogBinding = FragmentBottomDialogBinding.inflate(inflater, container, false)

        getLabelsName()

        // لو كان التطبيق بالوضع الليلي يغير الألوان لوضع الليل
        val sharedSetting = requireActivity().getSharedPreferences("SettingsApp", AppCompatActivity.MODE_PRIVATE)
        if(sharedSetting.getString("theme", "light") == "dark") {
            dialogBinding.activity.setBackgroundColor(Color.parseColor("#253557"))
            dialogBinding.archiveNote.setTextColor(Color.parseColor("#ffffff"))
            dialogBinding.unarchiveNote.setTextColor(Color.parseColor("#ffffff"))
            dialogBinding.deleteNote.setTextColor(Color.parseColor("#ffffff"))
            dialogBinding.makeCopy.setTextColor(Color.parseColor("#ffffff"))
            dialogBinding.share.setTextColor(Color.parseColor("#ffffff"))
            dialogBinding.lable.setTextColor(Color.parseColor("#ffffff"))
            dialogBinding.colorWhite.setTextColor(Color.parseColor("#000000"))
        }

        // تحديد شو يظهر من الbottomDialog حسب كل واجهة ممكن تظهر فيها
        val sharedPref = requireContext().getSharedPreferences("MyPref", AppCompatActivity.MODE_PRIVATE)
        if(sharedPref.getString("type", "").toString() == "addNote"){
            dialogBinding.replayNote.visibility = View.GONE
            dialogBinding.archiveNote.visibility = View.GONE
            dialogBinding.unarchiveNote.visibility = View.GONE
            dialogBinding.deleteNote.visibility = View.GONE
            dialogBinding.deleteForever.visibility = View.GONE
        }else if(sharedPref.getString("type", "").toString() == "editNote"){
            dialogBinding.replayNote.visibility = View.GONE
            dialogBinding.archiveNote.visibility = View.GONE
            dialogBinding.unarchiveNote.visibility = View.GONE
            dialogBinding.deleteForever.visibility = View.GONE
        }else if(sharedPref.getString("type", "").toString() == "adapterInHome"){
            dialogBinding.replayNote.visibility = View.GONE
            dialogBinding.linearColor.visibility = View.GONE
            dialogBinding.unarchiveNote.visibility = View.GONE
            dialogBinding.deleteForever.visibility = View.GONE
            dialogBinding.labelsSpinner.visibility = View.GONE
        }else if(sharedPref.getString("type", "").toString() == "adapterInLabelNotes"){
            dialogBinding.linearColor.visibility = View.GONE
            dialogBinding.replayNote.visibility = View.GONE
            dialogBinding.unarchiveNote.visibility = View.GONE
            dialogBinding.deleteForever.visibility = View.GONE
            dialogBinding.labelsSpinner.visibility = View.GONE
        } else if(sharedPref.getString("type", "").toString() == "adapterInArchive"){
            dialogBinding.replayNote.visibility = View.GONE
            dialogBinding.linearColor.visibility = View.GONE
            dialogBinding.archiveNote.visibility = View.GONE
            dialogBinding.deleteForever.visibility = View.GONE
            dialogBinding.labelsSpinner.visibility = View.GONE
        }else if(sharedPref.getString("type", "").toString() == "adapterInTrash"){
            dialogBinding.linearColor.visibility = View.GONE
            dialogBinding.archiveNote.visibility = View.GONE
            dialogBinding.unarchiveNote.visibility = View.GONE
            dialogBinding.deleteNote.visibility = View.GONE
            dialogBinding.makeCopy.visibility = View.GONE
            dialogBinding.share.visibility = View.GONE
            dialogBinding.labelsSpinner.visibility = View.GONE
        }

        dialogBinding.colorWhite.setOnClickListener {
            dialogBinding.colorWhite.setText(R.string.select)
            dialogBinding.colorRed.setText("")
            dialogBinding.colorYellow.setText("")
            dialogBinding.colorBlue.setText("")
            dialogBinding.colorGreen.setText("")
            dialogBinding.colorPurple.setText("")

            Notes.color = "#FFFFFF"
//            EditNoteActivity.isChange = true
        }

        dialogBinding.colorRed.setOnClickListener {
            dialogBinding.colorWhite.setText("")
            dialogBinding.colorRed.setText(R.string.select)
            dialogBinding.colorYellow.setText("")
            dialogBinding.colorBlue.setText("")
            dialogBinding.colorGreen.setText("")
            dialogBinding.colorPurple.setText("")

            Notes.color = "#CA516E"
//            EditNoteActivity.isChange = true
        }

        dialogBinding.colorYellow.setOnClickListener {
            dialogBinding.colorWhite.setText("")
            dialogBinding.colorRed.setText("")
            dialogBinding.colorYellow.setText(R.string.select)
            dialogBinding.colorBlue.setText("")
            dialogBinding.colorGreen.setText("")
            dialogBinding.colorPurple.setText("")

            Notes.color = "#E3B84A"
//            EditNoteActivity.isChange = true
        }

        dialogBinding.colorBlue.setOnClickListener {
            dialogBinding.colorWhite.setText("")
            dialogBinding.colorRed.setText("")
            dialogBinding.colorYellow.setText("")
            dialogBinding.colorBlue.setText(R.string.select)
            dialogBinding.colorGreen.setText("")
            dialogBinding.colorPurple.setText("")

            Notes.color = "#6F8FF3"
//            EditNoteActivity.isChange = true
        }

        dialogBinding.colorGreen.setOnClickListener {
            dialogBinding.colorWhite.setText("")
            dialogBinding.colorRed.setText("")
            dialogBinding.colorYellow.setText("")
            dialogBinding.colorBlue.setText("")
            dialogBinding.colorGreen.setText(R.string.select)
            dialogBinding.colorPurple.setText("")

            Notes.color = "#9DD198"
//            EditNoteActivity.isChange = true
        }

        dialogBinding.colorPurple.setOnClickListener {
            dialogBinding.colorWhite.setText("")
            dialogBinding.colorRed.setText("")
            dialogBinding.colorYellow.setText("")
            dialogBinding.colorBlue.setText("")
            dialogBinding.colorGreen.setText("")
            dialogBinding.colorPurple.setText(R.string.select)

            Notes.color = "#8F92CA"
//            EditNoteActivity.isChange = true
        }

        /////////////////////////////////////////////////////////

        // أعادة الملاحظة من سلة المهملات.. وبعدل على قيمة تاريخ الحذف وقيمة الجذف بتصير 0
        dialogBinding.replayNote.setOnClickListener {
            val noteId = sharedPref.getString("noteId", "").toString()
            val replayAt = SimpleDateFormat("yyyy/MM/dd").format(Date())
            replayNote(noteId, replayAt)

            Toast.makeText(requireContext(), R.string.note_recovered, Toast.LENGTH_SHORT).show()
            val sharedPref = requireContext().getSharedPreferences("MyPref", AppCompatActivity.MODE_PRIVATE)
            onNoteListener.deleteNote(sharedPref.getInt("position", -1).toString().toInt())
            dismiss()
        }

        // نقل الملاحظة الى الأرشفة
        dialogBinding.archiveNote.setOnClickListener {
            val noteId = sharedPref.getString("noteId", "").toString()
            archiveNote(noteId)
            onNoteListener.deleteNote(sharedPref.getInt("position", -1).toString().toInt())
            dismiss()
        }

        // استعادة الملاحظة من الارشفة
        dialogBinding.unarchiveNote.setOnClickListener {
            val noteId = sharedPref.getString("noteId", "").toString()
            unarchiveNote(noteId)
            onNoteListener.deleteNote(sharedPref.getInt("position", -1).toString().toInt())
            dismiss()
        }

        // نقل الملاحظة الى سلة المهملات
        dialogBinding.deleteNote.setOnClickListener {
            val alert = AlertDialog.Builder(requireContext())
            alert.setTitle(R.string.delete_note)
            alert.setMessage(R.string.sureDelete)
            alert.setIcon(R.drawable.ic_delete)
            alert.setCancelable(true)

            alert.setPositiveButton(R.string.yes){d, i ->
                Toast.makeText(requireContext(), R.string.moved_to_trash, Toast.LENGTH_SHORT).show()

                val noteId = sharedPref.getString("noteId", "").toString()
                val deletedAt = SimpleDateFormat("yyyy/MM/dd - HH:mm").format(Date())
                deleteNote(noteId, deletedAt)
                val sharedPref = requireContext().getSharedPreferences("MyPref", AppCompatActivity.MODE_PRIVATE)
                if(sharedPref.getString("type", "").toString() == "editNote"){
                    requireActivity().onBackPressed()
                }else{
                    onNoteListener.deleteNote(sharedPref.getInt("position", -1).toString().toInt())
                    dismiss()
                }

            }

            alert.setNegativeButton(R.string.cancel){d, i ->
                d.cancel()
            }
            alert.create().show()
        }

        // حذف الملاحظة بشكل نهائي
        dialogBinding.deleteForever.setOnClickListener {
            val alert = AlertDialog.Builder(requireContext())
            alert.setTitle(R.string.delete_note_forever)
            alert.setMessage(R.string.sureDelete_forever)
            alert.setIcon(R.drawable.ic_delete)
            alert.setCancelable(true)

            alert.setPositiveButton(R.string.yes){d, i ->
                Toast.makeText(requireContext(), R.string.note_deleted, Toast.LENGTH_SHORT).show()

                val noteId = sharedPref.getString("noteId", "").toString()
                deleteForever(noteId)
                val sharedPref = requireContext().getSharedPreferences("MyPref", AppCompatActivity.MODE_PRIVATE)
                onNoteListener.deleteNote(sharedPref.getInt("position", -1).toString().toInt())
                dismiss()

            }

            alert.setNegativeButton(R.string.cancel){d, i ->
                d.cancel()
            }
            alert.create().show()
        }

        // نسخ الملاحظة.. نسخ عنوان الملاحظة ووصفها
        dialogBinding.makeCopy.setOnClickListener {
            val title = sharedPref.getString("title", "").toString()
            val description = sharedPref.getString("description", "").toString()

            if (title.isNotEmpty() || description.isNotEmpty()) {
                val clipBoard = requireActivity().getSystemService(CLIPBOARD_SERVICE) as ClipboardManager
                val clip: ClipData = ClipData.newPlainText("simple", "$title\n$description")
                clipBoard.setPrimaryClip(clip)
                Toast.makeText(requireContext() , R.string.note_copy , Toast.LENGTH_LONG).show()
            }else{
                Toast.makeText(requireContext(), R.string.write_to_note_copy, Toast.LENGTH_SHORT).show()
            }
        }

        // مشاركة الملاحظة على تطبيقات أخرى.. مشاركة عنوان الملاحظة ووصفها
        dialogBinding.share.setOnClickListener {
            val title = sharedPref.getString("title", "").toString()
            val description = sharedPref.getString("description", "").toString()

            if (title.isNotEmpty() || description.isNotEmpty()) {
                val text = "$title.\n$description."
                val intent = Intent(Intent.ACTION_SEND)
                intent.setType("text/plain")
                intent.putExtra(Intent.EXTRA_TEXT, text)
                startActivity(intent)
            }else{
                Toast.makeText(requireContext(), R.string.write_to_share, Toast.LENGTH_SHORT).show()
            }
        }

//        dialogBinding.spnrLabelsName.adapter = ArrayAdapter(requireContext(),
//            android.R.layout.simple_spinner_dropdown_item, labelsName)
//
//        dialogBinding.spnrLabelsName.onItemSelectedListener = object: AdapterView.OnItemSelectedListener{
//            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, position: Int, p3: Long) {
//                Toast.makeText(requireContext(), "position select = $position, $p3", Toast.LENGTH_SHORT).show()
////                Notes.labelId = labelsObject[position].name
////                Toast.makeText(requireContext(), "object id = ${labelsObject[position].labelId}", Toast.LENGTH_SHORT).show()
//            }
//
//            override fun onNothingSelected(p0: AdapterView<*>?) {
//                Notes.labelId = "withoutLabel"
//                Toast.makeText(requireContext(), "position select = withoutLabel", Toast.LENGTH_SHORT).show()
//            }
//        }

        // تحديد الملاحظة تابعة لأي تصنيف
        dialogBinding.labelsSpinner.setOnClickListener {
            if(labelsObject.isEmpty()){
                Toast.makeText(requireContext(), R.string.no_any_label, Toast.LENGTH_SHORT).show()
            }
            val arrayAdapdet = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_dropdown_item, labelsName)
            // بعرض أسماء كل التصنيفات الموجودة بالتطبيق
            dialogBinding.spnrLabelsName.adapter = arrayAdapdet
            dialogBinding.spnrLabelsName.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                    override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
                        Notes.labelId = labelsObject[p2].labelId
//                        EditNoteActivity.isChange = true
                    }
                    override fun onNothingSelected(p0: AdapterView<*>?) {}
            }
        }

        return dialogBinding.root
    }

    private fun replayNote(noteId: String, dateReplay: String){
        val note = HashMap<String, Any>()
        note["isDeleted"] = 0
        note["deletedAt"] = "replay, $dateReplay"

        db.collection("Notes").document(noteId)
            .update(note)
            .addOnSuccessListener {
//                Toast.makeText(requireContext(), R.string.note_recovered, Toast.LENGTH_SHORT).show()
//
//                val sharedPref = requireContext().getSharedPreferences("MyPref", AppCompatActivity.MODE_PRIVATE)
//                onNoteListener.deleteNote(sharedPref.getInt("position", -1).toString().toInt())
//                dismiss()
            }

            .addOnFailureListener { error ->
                Toast.makeText(requireContext(), "Error: ${error.message}", Toast.LENGTH_LONG).show()
            }
    }

    private fun deleteNote(noteId: String, deletedAt: String){
        val note = HashMap<String, Any>()
        note["isDeleted"] = 1
        note["deletedAt"] = deletedAt

        db.collection("Notes").document(noteId)
            .update(note)
            .addOnSuccessListener {
//                Toast.makeText(requireContext(), R.string.moved_to_trash, Toast.LENGTH_SHORT).show()
//
//                val sharedPref = requireContext().getSharedPreferences("MyPref", AppCompatActivity.MODE_PRIVATE)
//                if(sharedPref.getString("type", "").toString() == "editNote"){
//                    requireActivity().onBackPressed()
//                }else{
//                    onNoteListener.deleteNote(sharedPref.getInt("position", -1).toString().toInt())
//                    dismiss()
//                }
            }

            .addOnFailureListener { error ->
                Toast.makeText(requireContext(), "Error: ${error.message}", Toast.LENGTH_LONG).show()
            }
    }

    private fun deleteForever(noteId: String){
        db.collection("Notes").document(noteId)
            .delete()
            .addOnSuccessListener {
//                Toast.makeText(requireContext(), R.string.note_deleted, Toast.LENGTH_SHORT).show()
//
//                val sharedPref = requireContext().getSharedPreferences("MyPref", AppCompatActivity.MODE_PRIVATE)
//                onNoteListener.deleteNote(sharedPref.getInt("position", -1).toString().toInt())
//                dismiss()
            }

            .addOnFailureListener { error ->
                Toast.makeText(requireContext(), "Delete failed.\n${error.message}", Toast.LENGTH_SHORT).show()
            }
    }

    fun getLabelsName(){
        val sharedPref = requireContext().getSharedPreferences("UserMobile", Context.MODE_PRIVATE)
        val mobile = sharedPref.getString("mobile", "null").toString()

        db.collection("Labels").orderBy("date", Query.Direction.DESCENDING)
            .whereEqualTo("userMobile", mobile)
            .get()
            .addOnSuccessListener { documentReference ->
                if(! documentReference.isEmpty){
                    for (label in documentReference) {
                        val userLabel = Labels(label.id, label.getString("userMobile").toString(),
                            label.getString("labelName").toString(), label.getString("aboutLabel").toString(),
                            label.getString("date").toString())

                        labelsObject.add(userLabel)
                        labelsName.add(label.getString("labelName").toString())
                    }
                }
            }
            .addOnFailureListener {error ->
                Toast.makeText(requireContext(), "Error,  ${error.message}", Toast.LENGTH_SHORT).show()
                Log.e("msa", "Error,  ${error.message}")
            }
    }

    private fun archiveNote(noteId: String){
        val note = HashMap<String, Any>()
        note["inArchives"] = 1

        db.collection("Notes").document(noteId)
            .update(note)
            .addOnSuccessListener {
//                Toast.makeText(requireContext(), "The ", Toast.LENGTH_SHORT).show()
            }

            .addOnFailureListener { error ->
                Toast.makeText(requireContext(), "Error: ${error.message}", Toast.LENGTH_LONG).show()
            }
    }

    private fun unarchiveNote(noteId: String){
        val note = HashMap<String, Any>()
        note["inArchives"] = 0

        db.collection("Notes").document(noteId)
            .update(note)
            .addOnSuccessListener {
//                Toast.makeText(requireContext(), "The ", Toast.LENGTH_SHORT).show()
            }

            .addOnFailureListener { error ->
                Toast.makeText(requireContext(), "Error: ${error.message}", Toast.LENGTH_LONG).show()
            }
    }

}