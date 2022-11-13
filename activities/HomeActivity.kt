package com.msa.mynotes.activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.akexorcist.localizationactivity.ui.LocalizationActivity
import com.msa.mynotes.R
import com.msa.mynotes.databinding.ActivityHomeBinding
import com.msa.mynotes.fragmenst.*
import com.msa.mynotes.models.Labels

class HomeActivity : LocalizationActivity() {
    lateinit var binding: ActivityHomeBinding
    var fragment = "Note"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // عشان bottomNavigationView تبين بالشكل المطلوب
        binding.bottomNavigationView.background = null
        binding.bottomNavigationView.menu.getItem(2).isEnabled = false
        binding.bottomNavigationView.menu.getItem(3).isEnabled = false

        // بعمل objects من الfragments عشان لما انزل بالlist وبعد ها اغير الfragment وارجع للlist يرجعني على نفس المكان اللي كنت فيه مش يرجعني بالاول
        val home = HomeFragment()
        val lable = LabelsFragment()
        val setting = SettingsFragment()

        val activity = intent.getStringExtra("activity").toString()
        if(activity == "Setting"){
            replaceFragment(SettingsFragment())
        }else {
            replaceFragment(HomeFragment())
        }

        binding.bottomNavigationView.setOnItemSelectedListener {
            when (it.itemId) {
                R.id.nav_home -> {
                    replaceFragment(home)
                    fragment = "Note"
                    binding.fab.visibility = View.VISIBLE
                    return@setOnItemSelectedListener true
                }

                R.id.nav_label -> {
                    replaceFragment(lable)
                    fragment = "Label"
                    binding.fab.visibility = View.VISIBLE
                    return@setOnItemSelectedListener true
                }

                R.id.nav_setting -> {
                    replaceFragment(setting)
                    fragment = "Setting"
                    binding.fab.visibility = View.GONE
                    return@setOnItemSelectedListener true
                }

                else -> {
                    return@setOnItemSelectedListener false
                }
            }
        }

        binding.fab.setOnClickListener {
            if(fragment == "Note") {
                startActivity(Intent(this, AddNoteActivity::class.java))
            }else{
                Labels.type = "newLabel"
                val bottomDialog = LabelDialogFragment(object: LabelDialogFragment.OnLabelListener{
                    override fun addLabel(label: Labels) {
                        LabelsFragment.labels.add(label)
                        LabelsFragment.labelsAdapter.notifyDataSetChanged()
                    }
                    override fun updateLabel(label: Labels) {}
                })

                bottomDialog.show(supportFragmentManager, "addLabel")
            }
        }

    }

    fun replaceFragment(fragment: Fragment) {
        val fm = supportFragmentManager
        fm.beginTransaction().replace(R.id.container, fragment).commit()
    }

    override fun onBackPressed() {
        finish()
    }

}