package com.msa.mynotes.fragmenst

import android.content.Context.MODE_PRIVATE
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
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.msa.mynotes.R
import com.msa.mynotes.activities.LoginActivity
import com.msa.mynotes.adapters.MyNotesAdapter
import com.msa.mynotes.brodcast_receiver.CheckNetworkConnection
import com.msa.mynotes.brodcast_receiver.MyReceiver
import com.msa.mynotes.brodcast_receiver.NetworkConnection
import com.msa.mynotes.databinding.FragmentHomeBinding
import com.msa.mynotes.models.Notes

class HomeFragment : Fragment() {
    lateinit var homeBinding: FragmentHomeBinding
    val db = Firebase.firestore
    lateinit var mobile: String
    val notesPin = ArrayList<Notes>()
    val notes = ArrayList<Notes>()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        homeBinding = FragmentHomeBinding.inflate(inflater, container, false)

        // اذا فش رقم جوال محفوظ بالsharedPref ينقله على واجهة تسجيل الدخول عشان ميحفظش ملاحظات برقم مش موجود
        val sharedPref = requireContext().getSharedPreferences("UserMobile", MODE_PRIVATE)
        mobile = sharedPref.getString("mobile", "null").toString()
        if(mobile == "null"){
            requireActivity().startActivity(Intent(requireContext(), LoginActivity::class.java))
            requireActivity().finish()
        }

        // عشان أحدد شو يظهر بالBottomDialog
        val myPref = requireActivity().getSharedPreferences("MyPref", MODE_PRIVATE)
        val type = myPref.edit()
        type.putString("type", "adapterInHome").apply()

        // لو كان التطبيق بالوضع الليلي يغير الألوان لوضع الليل
        val sharedSetting = requireActivity().getSharedPreferences("SettingsApp", AppCompatActivity.MODE_PRIVATE)
        if(sharedSetting.getString("theme", "light") == "dark") {
            homeBinding.notesRecyclerView.setBackgroundColor(Color.parseColor("#3A3939"))
            homeBinding.tvNotes.setTextColor(Color.parseColor("#FFFFFF"))
        }

        if(notesPin.isEmpty()) {
            getAllNotes()
        }else{
            val notesAdapter = MyNotesAdapter(requireActivity(), notesPin)
            homeBinding.notesRecyclerView.adapter = notesAdapter
            homeBinding.notesRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        }

        homeBinding.tvNotes.setOnClickListener {
                val networkConnection = NetworkConnection()
                if(networkConnection.isGoodInternetConnection()){
                    Toast.makeText(requireContext(), "Connect", Toast.LENGTH_SHORT).show()
                    Log.e("msaCheckInternet", "Var internet")
                }else{
                    Toast.makeText(requireContext(), "Not Connect", Toast.LENGTH_SHORT).show()
                    Log.e("msaCheckInternet", "Hayer internet")
                }

//            var check = MyReceiver()
//            if(check.checkForInternet(requireActivity())){
//                Toast.makeText(requireContext(), "Connect", Toast.LENGTH_SHORT).show()
//                Log.e("msaCheckInternet", "Var internet")
//            }else{
//                Toast.makeText(requireContext(), "Not Connect", Toast.LENGTH_SHORT).show()
//                Log.e("msaCheckInternet", "Hayer internet")
//            }
        }

        //  كل ما يغير بالنص اللي بالبحث يبحث اذا في نص هيبحث عنه بكل الملاحظات.. واذا فش هيظهر كل الملاحظات
        homeBinding.etSearch.addTextChangedListener {
            val searchNotes = ArrayList<Notes>()
            val search = homeBinding.etSearch.text.toString()
            if(search.isNotEmpty()){
                // البحث بكون من الArrayList عشان البحث يكون سريع.. وببحث عن النص بالعنوان والملاحظة
                for(i in 0 until notesPin.size){
                    if(notesPin[i].title.contains(search, true) ||
                        notesPin[i].description.contains(search, true)){
                        searchNotes.add(notesPin[i])
                    }
                }
                if(searchNotes.isEmpty()){
                    homeBinding.notesRecyclerView.visibility = View.GONE
                    homeBinding.linearNotes.visibility = View.VISIBLE
                    homeBinding.tvNoNotes.setText(R.string.no_note_contain)
//                    homeBinding.tvNoNotes.textSize = R.dimen.noNoteSearch
                }else{
                    homeBinding.linearNotes.visibility = View.GONE
                    homeBinding.notesRecyclerView.visibility = View.VISIBLE
                    val notesAdapter = MyNotesAdapter(requireActivity(), searchNotes)
                    homeBinding.notesRecyclerView.adapter = notesAdapter
                    homeBinding.notesRecyclerView.layoutManager = LinearLayoutManager(requireContext())
                }
            }else{
                homeBinding.linearNotes.visibility = View.GONE
                homeBinding.notesRecyclerView.visibility = View.VISIBLE
                val notesAdapter = MyNotesAdapter(requireActivity(), notesPin)
                homeBinding.notesRecyclerView.adapter = notesAdapter
                homeBinding.notesRecyclerView.layoutManager = LinearLayoutManager(requireContext())
            }
        }

        return homeBinding.root
    }

    private fun getAllNotes(){
        db.collection("Notes").orderBy("updatedAt", Query.Direction.DESCENDING)
            .whereEqualTo("userMobile", mobile)
            .whereEqualTo("isDeleted", 0)
            .whereEqualTo("inArchives", 0)
            .get()
            .addOnSuccessListener { documentReference ->
                if(documentReference.isEmpty){
                    homeBinding.notesRecyclerView.visibility = View.GONE
                    homeBinding.linearNotes.visibility = View.VISIBLE
                }else {
                    homeBinding.linearNotes.visibility = View.GONE
                    homeBinding.notesRecyclerView.visibility = View.VISIBLE

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

                val notesAdapter = MyNotesAdapter(requireActivity(), notesPin)
                homeBinding.notesRecyclerView.adapter = notesAdapter
                homeBinding.notesRecyclerView.layoutManager = GridLayoutManager(requireContext(), 1)
            }
            .addOnFailureListener { error ->
                Toast.makeText(requireContext(), "Error,  ${error.message}", Toast.LENGTH_SHORT).show()
                Log.e("msa", "Error,  ${error.message}")
            }
    }

}