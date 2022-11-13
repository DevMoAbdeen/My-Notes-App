package com.msa.mynotes.adapters

import android.annotation.SuppressLint
import android.app.Activity
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import com.msa.mynotes.R
import com.msa.mynotes.activities.*
import com.msa.mynotes.databinding.RvNotesListBinding
import com.msa.mynotes.fragmenst.BottomDialogFragment
import com.msa.mynotes.models.Notes

class MyNotesAdapter(var activity: Activity, var data: ArrayList<Notes>) :
    RecyclerView.Adapter<MyNotesAdapter.NotesViewHolder>() {
    inner class NotesViewHolder(var binding: RvNotesListBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NotesViewHolder {
        return NotesViewHolder(RvNotesListBinding.inflate(LayoutInflater.from(activity), parent, false))
    }

    override fun onBindViewHolder(holder: NotesViewHolder, @SuppressLint("RecyclerView") position: Int) {
        holder.binding.cardNote.setBackgroundColor(Color.parseColor(data[position].color))
        holder.binding.tvTitle.text = data[position].title
        holder.binding.tvDescription.text = data[position].description
        holder.binding.tvLastTime.text = "${data[position].updatedAt}"
        if (data[position].isFixed == 1) {
            holder.binding.pin.visibility = View.VISIBLE
        }

//        holder.binding.cardNote.setOnClickListener(object : DoubleClickListener() {
//            override fun onDoubleClick(v: View?) {
//                val sharedPref = activity.getSharedPreferences("MyPref", AppCompatActivity.MODE_PRIVATE)
//                if (sharedPref.getString("type", "").equals("adapterInTrash")) {
//                    val clipBoard = activity.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
//                    val clip: ClipData = ClipData.newPlainText("simple",
//                        "${data[position].title}\n${data[position].description}")
//                    clipBoard.setPrimaryClip(clip)
//                    Toast.makeText(activity, R.string.note_copy, Toast.LENGTH_LONG).show()
//                }
//            }
//        })

        holder.binding.cardNote.setOnClickListener {
            // عشان امنع التعديل على ملاحظة موجودة بسلة المهملات
            val sharedPref = activity.getSharedPreferences("MyPref", AppCompatActivity.MODE_PRIVATE)
            if (sharedPref.getString("type", "").equals("adapterInTrash")) {
                Toast.makeText(activity, R.string.noEditInTrash, Toast.LENGTH_SHORT).show()
            } else {
                // برسل البيانات عشان أعرضهم و أعدل عليهم
                val intent = Intent(activity, EditNoteActivity::class.java)
                intent.putExtra("noteID", data[position].noteId)
                intent.putExtra("title", data[position].title)
                intent.putExtra("description", data[position].description)
                intent.putExtra("date", data[position].updatedAt)
                intent.putExtra("inArchives", data[position].inArchives)
                intent.putExtra("isFixed", data[position].isFixed)
                intent.putExtra("color", data[position].color)
                intent.putExtra("labelId", data[position].labelId)
                activity.startActivity(intent)
            }
        }

        holder.binding.cardNote.setOnLongClickListener {
            val bottomDialog = BottomDialogFragment(object : BottomDialogFragment.OnNoteListener {
                override fun deleteNote(position: Int) {
                    data.removeAt(position)
                    notifyDataSetChanged()
                }
            })
            // برسل البيانات عشان لو بده ينسخ الملاحظة أو يشاركها
            val sharedPref = activity.getSharedPreferences("MyPref", AppCompatActivity.MODE_PRIVATE)
            val notes = sharedPref.edit()
            notes.putString("noteId", data[position].noteId)
            notes.putInt("position", position)
            notes.putString("title", data[position].title)
            notes.putString("description", data[position].description)
            notes.apply()

            // بشوف من أي واجهة ضغط على الملاحظة عشان أعرف الbottomDialog هيظهر كإنه أي واجهة !
            if (sharedPref.getString("type", "") == "adapterInHome") {
                val home = activity as HomeActivity
                bottomDialog.show(home.supportFragmentManager, "showMore")
            } else if (sharedPref.getString("type", "") == "adapterInArchive") {
                val archive = activity as ArchiveNotesActivity
                bottomDialog.show(archive.supportFragmentManager, "showMore")
            }else if (sharedPref.getString("type", "") == "adapterInTrash") {
                val trash = activity as DeletedNotesActivity
                bottomDialog.show(trash.supportFragmentManager, "showMore")
            }else if (sharedPref.getString("type", "") == "adapterInLabelNotes") {
                val labelNotes = activity as LabelNotesActivity
                bottomDialog.show(labelNotes.supportFragmentManager, "showMore")
            }

            true
        }
    }

    override fun getItemCount(): Int {
        return data.size
    }

}

// This class has methods that check if two clicks were registered
// within a span of DOUBLE_CLICK_TIME_DELTA i.e., in our case
// equivalent to 300 ms
//abstract class DoubleClickListener : View.OnClickListener {
//    companion object {
//        private const val DOUBLE_CLICK_TIME_DELTA: Long = 300 //milliseconds
//    }
//
//    var lastClickTime: Long = 0
//
//    override fun onClick(v: View?) {
//        val clickTime = System.currentTimeMillis()
//        if (clickTime - lastClickTime < DOUBLE_CLICK_TIME_DELTA) {
//            onDoubleClick(v)
//        }
//        lastClickTime = clickTime
//    }
//
//    abstract fun onDoubleClick(v: View?)
//
//}
