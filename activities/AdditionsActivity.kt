package com.msa.mynotes.activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.msa.mynotes.R
import com.msa.mynotes.databinding.ActivityAdditionsBinding

class AdditionsActivity : AppCompatActivity() {
    lateinit var binding: ActivityAdditionsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAdditionsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // عشان اعرف أي صورة ونص أعرض.. بعرض الصورة والنص حسب الرقم
        val sharedPref = getSharedPreferences("sharedPref", MODE_PRIVATE)
        var numActivity = sharedPref.getInt("activity", 0)

        binding.tvNext.setOnClickListener {
            numActivity++

            val activity = sharedPref.edit()
            activity.putInt("activity", numActivity)
            activity.apply()

            if(sharedPref.getInt("activity", 0) == 2) {
                binding.imageAnimation.setImageResource(R.drawable.ic_act_sync_firebase)
                binding.textViewDescription.setText(R.string.description_firebase)
                binding.firstCircle.setBackgroundResource(R.color.circleBackground)
                binding.secondCircle.setBackgroundResource(R.color.main)

            }else if(sharedPref.getInt("activity", 0) == 3){
                binding.imageAnimation.setImageResource(R.drawable.ic_act_share)
                binding.textViewDescription.setText(R.string.description_share)
                binding.firstCircle.setBackgroundResource(R.color.circleBackground)
                binding.secondCircle.setBackgroundResource(R.color.circleBackground)
                binding.thirdCircle.setBackgroundResource(R.color.main)
                binding.tvNext.setText(R.string.login)

            }else{
                val activity = sharedPref.edit()
                activity.putInt("activity", 1)
                activity.apply()

                startActivity(Intent(this, LoginActivity::class.java))
                finish()
            }
        }

        binding.tvSkip.setOnClickListener {
            val activity = sharedPref.edit()
            activity.putInt("activity", 1)
            activity.apply()

            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }


    }
}