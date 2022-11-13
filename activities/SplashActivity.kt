package com.msa.mynotes.activities

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.msa.mynotes.R
import com.msa.mynotes.app.App
import com.msa.mynotes.databinding.ActivitySplashBinding

class SplashActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySplashBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val sharedPref = getSharedPreferences("sharedPref", MODE_PRIVATE)
        val activity = sharedPref.edit()
        activity.putInt("activity", 1)
        activity.apply()

        val rememberMe = getSharedPreferences("Remember", MODE_PRIVATE)
        val remember = rememberMe.getBoolean("remember", false)

        // عمل animation .. عرض شكل عند فتح الواجهة
        val animationRotate = AnimationUtils.loadAnimation(this, R.anim.slide_down)
        binding.linearInSplash.startAnimation(animationRotate)
        animationRotate.setAnimationListener(object: Animation.AnimationListener{
            override fun onAnimationStart(p0: Animation?) {}

            override fun onAnimationEnd(p0: Animation?) {
                // اذا في بيانات محفوظة يفتح الhome مباشرة
                if(remember){
                    startActivity(Intent(applicationContext, HomeActivity::class.java))
                    finish()
                }else{
                    startActivity(Intent(applicationContext, AdditionsActivity::class.java))
                    finish()
                }
            }

            override fun onAnimationRepeat(p0: Animation?) {}
        })


    }
}