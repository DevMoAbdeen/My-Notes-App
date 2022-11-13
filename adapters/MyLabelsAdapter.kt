package com.msa.mynotes.adapters

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.msa.mynotes.R
import com.msa.mynotes.activities.HomeActivity
import com.msa.mynotes.activities.LabelNotesActivity
import com.msa.mynotes.brodcast_receiver.MyReceiver
import com.msa.mynotes.databinding.RvLabelsListBinding
import com.msa.mynotes.fragmenst.LabelDialogFragment
import com.msa.mynotes.models.Labels

class MyLabelsAdapter(var activity: Activity, var data: ArrayList<Labels>, var onLabelListener: OnLabelListener):
    RecyclerView.Adapter<MyLabelsAdapter.LabelsViewHolder>(){
    val db = Firebase.firestore

    interface OnLabelListener{
        fun deleteLabel(position : Int)
    }

    inner class LabelsViewHolder(var binding: RvLabelsListBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LabelsViewHolder {
        return LabelsViewHolder(RvLabelsListBinding.inflate(LayoutInflater.from(activity), parent, false))
    }

    override fun onBindViewHolder(holder: LabelsViewHolder, @SuppressLint("RecyclerView") position: Int) {
        // لو كان التطبيق بالوضع الليلي يغير الألوان لوضع الليل
        val sharedSetting = activity.getSharedPreferences("SettingsApp", AppCompatActivity.MODE_PRIVATE)
        if(sharedSetting.getString("theme", "light") == "dark") {
            holder.binding.tvName.setTextColor(Color.parseColor("#FFFFFF"))
            holder.binding.tvAbout.setTextColor(Color.parseColor("#FFFFFF"))
        }

        holder.binding.tvName.text = data[holder.adapterPosition].name
        holder.binding.tvAbout.text = data[holder.adapterPosition].about

        // ببعت الID عشان أعرض الملاحظات اللي إلهم نفس الlabelID
        holder.binding.cardLabel.setOnClickListener {
            val intent = Intent(activity, LabelNotesActivity::class.java)
            intent.putExtra("labelID", data[holder.adapterPosition].labelId)
            activity.startActivity(intent)
        }

        holder.binding.editLabel.setOnClickListener {
            Labels.type = "updateLabel"

            val bottomDialog = LabelDialogFragment(object: LabelDialogFragment.OnLabelListener{
                override fun addLabel(label: Labels) {}
                // بعدل الlabel اللي اختارها المستخدم وبعدها بعمل تحديث على الadapter عشان يبين التعديل مباشرة
                override fun updateLabel(label: Labels) {
                    data[holder.adapterPosition] = label
                    notifyDataSetChanged()
                }
            })
            val sharedPref = activity.getSharedPreferences("MyLabel", AppCompatActivity.MODE_PRIVATE)
            val label = sharedPref.edit()
            label.putString("labelId", data[holder.adapterPosition].labelId)
            label.putString("name", data[holder.adapterPosition].name)
            label.putString("about", data[holder.adapterPosition].about)
            label.putString("date", data[holder.adapterPosition].date)
            label.apply()

            val home = activity as HomeActivity
            bottomDialog.show(home.supportFragmentManager, "editLabel")
        }

        holder.binding.deleteLabel.setOnClickListener {
            val alert = AlertDialog.Builder(activity)
            alert.setTitle(R.string.delete_label)
            alert.setMessage(R.string.want_delete_label)
            alert.setIcon(R.drawable.ic_delete)
            alert.setCancelable(true)

            alert.setPositiveButton(R.string.yes) { d, i ->
                val myReceiver = MyReceiver()
                if (myReceiver.checkConnection(activity)) {
                    deleteLabel(data[position].labelId.toString())
                } else {
                    deleteLabel(data[position].labelId.toString())
                    Toast.makeText(activity, R.string.label_deleted, Toast.LENGTH_SHORT).show()
                    notifyDataSetChanged()
                }

                onLabelListener.deleteLabel(position)
            }

            alert.setNegativeButton(R.string.cancel) { d, i ->
                d.cancel()
            }
            alert.create().show()

        }
    }

    override fun getItemCount(): Int {
        return data.size
    }

    fun deleteLabel(labelId: String){
        db.collection("Labels").document(labelId)
            .delete()
            .addOnSuccessListener {
                Toast.makeText(activity, R.string.label_deleted, Toast.LENGTH_SHORT).show()
                notifyDataSetChanged()
            }

            .addOnFailureListener { error ->
                Toast.makeText(activity, "Delete failed.\n${error.message}", Toast.LENGTH_SHORT).show()
            }
    }

}
