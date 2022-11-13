package com.msa.mynotes.activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.msa.mynotes.R
import com.msa.mynotes.databinding.ActivityLoginBinding
import java.math.BigInteger
import java.security.MessageDigest

class LoginActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLoginBinding
//    private lateinit var firebaseAuth: FirebaseAuth
    val db = Firebase.firestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)
//        firebaseAuth = FirebaseAuth.getInstance()

        binding.btnLogin.setOnClickListener {
            binding.btnLogin.isClickable = false
            binding.btnSignup.isClickable = false
            binding.tvForget.isClickable = false
            binding.etEmail.isEnabled = false
            binding.etPassword.isEnabled = false

            val email = binding.etEmail.text.toString().trim()
            val password = binding.etPassword.text.toString().trim()

            if(email.isEmpty() || password.isEmpty()){
                Toast.makeText(this, R.string.fill_fields, Toast.LENGTH_SHORT).show()
                binding.btnLogin.isClickable = true
                binding.btnSignup.isClickable = true
                binding.tvForget.isClickable = true
                binding.etEmail.isEnabled = true
                binding.etPassword.isEnabled = true
            }else{
                login(email, password)
            }

        }

        binding.btnSignup.setOnClickListener {
            startActivity(Intent(this, SignUpActivity::class.java))
        }

        binding.tvForget.setOnClickListener {
            startActivity(Intent(this, ForgotPasswordActivity::class.java))
        }
    }

    private fun login(email: String, password: String){
        db.collection("Users")
            .whereEqualTo("email", email).limit(1)
            .get()
            .addOnSuccessListener { user ->
                if(user.isEmpty){
                    Toast.makeText(this@LoginActivity, R.string.incorrectly, Toast.LENGTH_SHORT).show()
                    binding.btnLogin.isClickable = true
                    binding.btnSignup.isClickable = true
                    binding.tvForget.isClickable = true
                    binding.etEmail.isEnabled = true
                    binding.etPassword.isEnabled = true
                }else{
                    val correctPassword = user.documents[0].getString("password").toString()
                    val writePassword = toMD5(password)
                    if(correctPassword.equals(writePassword)) {
                        val userMobile = user.documents[0].getString("mobile").toString()

                        if(binding.cbRememberMe.isChecked){
                            val rememberMe = getSharedPreferences("Remember", MODE_PRIVATE)
                            val editor = rememberMe.edit()
                            editor.putBoolean("remember", true)
                            editor.apply()
                        }

                        val sharedPref = getSharedPreferences("UserMobile", MODE_PRIVATE)
                        val editor = sharedPref.edit()
                        editor.putString("mobile", userMobile)
                        editor.apply()

                        val intent= Intent(this,HomeActivity::class.java)
                        startActivity(intent)
                        finish()
                    }else{
                        Toast.makeText(this@LoginActivity, R.string.incorrectly, Toast.LENGTH_SHORT).show()
                        binding.btnLogin.isClickable = true
                        binding.btnSignup.isClickable = true
                        binding.tvForget.isClickable = true
                        binding.etEmail.isEnabled = true
                        binding.etPassword.isEnabled = true
                    }
                }
            }
            .addOnFailureListener { error ->
                Toast.makeText(this@LoginActivity, "${error.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun toMD5(password: String): String{
        val md5 = MessageDigest.getInstance("MD5")
        return BigInteger(1, md5.digest(password.toByteArray()))
            .toString(16).padStart(23, '0')
    }

}