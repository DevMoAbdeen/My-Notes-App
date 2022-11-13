package com.msa.mynotes.activities

import android.app.ProgressDialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.google.firebase.FirebaseException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.msa.mynotes.R
import com.msa.mynotes.brodcast_receiver.MyReceiver
import com.msa.mynotes.databinding.ActivitySignUpBinding
import com.msa.mynotes.models.Notes
import java.math.BigInteger
import java.security.MessageDigest
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

class SignUpActivity : AppCompatActivity() {
    private lateinit var binding : ActivitySignUpBinding
    // If code sending faild, will used to resend
    private var forceResendingToken: PhoneAuthProvider.ForceResendingToken? = null
    private var mCallBacks: PhoneAuthProvider.OnVerificationStateChangedCallbacks? = null
    private var mVerficationId: String? = null
    private lateinit var firebaseAuth: FirebaseAuth
    val db = Firebase.firestore
    private lateinit var userPhone: String
    private lateinit var userEmail: String
    private lateinit var userPassword: String

    // Progress dialog
    private lateinit var progressDialog: ProgressDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignUpBinding.inflate(layoutInflater)
        setContentView(binding.root)
//        supportActionBar!!.hide()
        firebaseAuth = FirebaseAuth.getInstance()

        progressDialog = ProgressDialog(this)
        progressDialog.setTitle("Please wait...")
        progressDialog.setCanceledOnTouchOutside(false)

        mCallBacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks(){
            override fun onVerificationCompleted(phone: PhoneAuthCredential) {
                signInWithPhoneAuthCredential(phone)
            }

            override fun onVerificationFailed(e: FirebaseException) {
                progressDialog.dismiss()
                Toast.makeText(this@SignUpActivity, "Error onVerificationFailed: ${e.message}", Toast.LENGTH_LONG).show()
            }

            override fun onCodeSent(verificationId: String, token: PhoneAuthProvider.ForceResendingToken) {
                mVerficationId = verificationId
                forceResendingToken = token
                progressDialog.dismiss()

                binding.mobile.visibility = View.GONE
                binding.email.visibility = View.GONE
                binding.password.visibility = View.GONE
                binding.btnSignup.visibility = View.GONE
                binding.icon.setImageResource(R.drawable.ic_act_security)
                binding.cardVerifityCode.visibility = View.VISIBLE
            }
        }


        binding.btnSignup.setOnClickListener {
            val mobile = binding.etMobile.text.toString().trim()
            val email = binding.etEmail.text.toString().trim()
            val pass = binding.etPassword.text.toString().trim()

            val len = pass.length
            if (mobile.isEmpty() || pass.isEmpty() || email.isEmpty()) {
                Toast.makeText(this, R.string.fill_fields, Toast.LENGTH_SHORT).show()
            }else if((! email.contains("@")) || email.endsWith("@")){
                Toast.makeText(this, R.string.write_correct_Email, Toast.LENGTH_SHORT).show()
            }else if(len < 6){
                Toast.makeText(this, R.string.lettersPassword, Toast.LENGTH_SHORT).show()
            }else{
                val myReceiver = MyReceiver()
                if(myReceiver.checkConnection(this)){
                    binding.btnSignup.isClickable = false
                    binding.etMobile.isEnabled = false
                    binding.etEmail.isEnabled = false
                    binding.etPassword.isEnabled = false

                    val password = toMD5(pass)
                    checkMobile(mobile, email, password)
                }else{
                    Toast.makeText(this, R.string.noInternet, Toast.LENGTH_LONG).show()
                }
            }
        }

        binding.tvResendCode.setOnClickListener {
            val myReceiver = MyReceiver()
            if(myReceiver.checkConnection(this)){
                binding.mobile.visibility = View.GONE
                binding.email.visibility = View.GONE
                binding.password.visibility = View.GONE
                binding.btnSignup.visibility = View.GONE
                binding.icon.setImageResource(R.drawable.ic_act_security)
                binding.cardVerifityCode.visibility = View.VISIBLE

                resendVerificationCode(userPhone, forceResendingToken)
            }else{
                Toast.makeText(this, R.string.noInternetToDoThis, Toast.LENGTH_LONG).show()
            }
        }

        binding.btnVerityCode.setOnClickListener {
            val code = binding.etCode.text.toString().trim()
            if(code.isEmpty()){
                Toast.makeText(this, R.string.write_code, Toast.LENGTH_SHORT).show()
            }else{
                val myReceiver = MyReceiver()
                if(myReceiver.checkConnection(this@SignUpActivity)){
                    verifityPhoneNumberWithCode(mVerficationId!!, code)
                }else{
                    Toast.makeText(this@SignUpActivity, R.string.noInternetToDoThis, Toast.LENGTH_LONG).show()
                }
            }
        }

    }


    private fun startPhoneNumberVerfication(mobile: String){
        progressDialog.setMessage("Verifying Phone Number...")
        progressDialog.show()

        val options = PhoneAuthOptions.newBuilder(firebaseAuth)
            .setPhoneNumber(mobile)
            .setTimeout(60L, TimeUnit.SECONDS)
            .setActivity(this)
            .setCallbacks(mCallBacks!!)
            .build()

        PhoneAuthProvider.verifyPhoneNumber(options)
    }

    private fun resendVerificationCode(mobile: String, token: PhoneAuthProvider.ForceResendingToken?){
        progressDialog.setMessage("Resending Code...")
        progressDialog.show()

        val options = PhoneAuthOptions.newBuilder(firebaseAuth)
            .setPhoneNumber(mobile)
            .setTimeout(60L, TimeUnit.SECONDS)
            .setActivity(this)
            .setCallbacks(mCallBacks!!)
            .setForceResendingToken(token!!)
            .build()

        PhoneAuthProvider.verifyPhoneNumber(options)
    }

    private fun verifityPhoneNumberWithCode(verificationId: String, code: String){
        progressDialog.setMessage("Verifying Code...")
        progressDialog.show()

        val cerdential = PhoneAuthProvider.getCredential(verificationId, code)
        signInWithPhoneAuthCredential(cerdential)
    }

    private fun signInWithPhoneAuthCredential(cerdential: PhoneAuthCredential) {
        progressDialog.setMessage("Login In")

        firebaseAuth.signInWithCredential(cerdential)
            .addOnSuccessListener {
                progressDialog.dismiss()

//                register(userEmail, userPassword)
                insertUser(userPhone, userEmail, userPassword)
                addLabel(userPhone)

                val sharedPref = getSharedPreferences("UserMobile", MODE_PRIVATE)
                val editor = sharedPref.edit()
                editor.putString("mobile", userPhone)
                editor.apply()

                val rememberMe = getSharedPreferences("Remember", MODE_PRIVATE)
                val rememberEditor = rememberMe.edit()
                rememberEditor.putBoolean("remember", true)
                rememberEditor.apply()

                startActivity(Intent(this, HomeActivity::class.java))
                finish()
            }

            .addOnFailureListener{e ->
                progressDialog.dismiss()
                Toast.makeText(this, "Error signInWithPhoneAuthCredential: ${e.message}", Toast.LENGTH_LONG).show()
            }
    }


//    fun register(email: String, password: String){
//        firebaseAuth.createUserWithEmailAndPassword(email,password).addOnCompleteListener { task ->
//            if(task.isSuccessful){
//
//            }
//        }.addOnFailureListener { exception ->
//            Toast.makeText(applicationContext,exception.localizedMessage,Toast.LENGTH_LONG).show()
//        }
//    }

////////////////////////////////////////////

    private fun insertUser(mobile: String, email: String, password: String) {
        val user = hashMapOf(
            "mobile" to mobile,
            "email" to email,
            "password" to password
        )

        db.collection("Users")
            .add(user)
            .addOnSuccessListener { result ->

            }

            .addOnFailureListener { error ->
                Toast.makeText(applicationContext, "Insert error\n${error.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun checkMobile(mobile: String, email: String, password: String) {
        db.collection("Users")
            .whereEqualTo("mobile", mobile)
            .get()
            .addOnSuccessListener { result ->
                if (result.isEmpty) {
                    checkEmail(mobile, email, password)
                }else{
                    Toast.makeText(applicationContext, R.string.mobile_or_email_registerd, Toast.LENGTH_SHORT).show()
                    binding.btnSignup.isClickable = true
                    binding.etMobile.isEnabled = true
                    binding.etEmail.isEnabled = true
                    binding.etPassword.isEnabled = true
                }
            }

            .addOnFailureListener { error ->
                Toast.makeText(applicationContext, "Check error\n${error.message}", Toast.LENGTH_SHORT).show()
                binding.btnSignup.isClickable = true
                binding.etMobile.isEnabled = true
                binding.etEmail.isEnabled = true
                binding.etPassword.isEnabled = true
            }
    }

    private fun checkEmail(mobile: String, email: String, password: String) {
        db.collection("Users")
            .whereEqualTo("email", email)
            .get()
            .addOnSuccessListener { result ->
                if (result.isEmpty) {
                    val alert = AlertDialog.Builder(this)
                    alert.setTitle(R.string.confirm_number)
                    alert.setMessage("The code will be sent to the mobile $mobile ,Is the number correct ?")
                    alert.setIcon(R.drawable.ic_send_to_mobile)
                    alert.setCancelable(false)

                    alert.setPositiveButton(R.string.send) { d, i ->
                        binding.btnSignup.isClickable = true
                        binding.etMobile.isEnabled = true
                        binding.etEmail.isEnabled = true
                        binding.etPassword.isEnabled = true

                        userPhone = mobile
                        userEmail = email
                        userPassword = password

                        startPhoneNumberVerfication(mobile)
                    }

                    alert.setNegativeButton(R.string.change_number) { d, i ->
                        binding.btnSignup.isClickable = true
                        binding.etMobile.isEnabled = true
                        binding.etEmail.isEnabled = true
                        binding.etPassword.isEnabled = true

                        binding.mobile.visibility = View.VISIBLE
                        binding.email.visibility = View.VISIBLE
                        binding.password.visibility = View.VISIBLE
                        binding.btnSignup.visibility = View.VISIBLE
                        binding.icon.setImageResource(R.drawable.ic_act_signup)
                        binding.cardVerifityCode.visibility = View.GONE
                        d.dismiss()
                    }

                    alert.create().show()

                } else {
                    Toast.makeText(applicationContext, R.string.mobile_or_email_registerd, Toast.LENGTH_SHORT).show()
                    binding.btnSignup.isClickable = true
                    binding.etMobile.isEnabled = true
                    binding.etEmail.isEnabled = true
                    binding.etPassword.isEnabled = true
                }
            }

            .addOnFailureListener { error ->
                Toast.makeText(applicationContext, "Check error\n${error.message}", Toast.LENGTH_SHORT).show()
                binding.btnSignup.isClickable = true
                binding.etMobile.isEnabled = true
                binding.etEmail.isEnabled = true
                binding.etPassword.isEnabled = true
            }
    }

    private fun toMD5(password: String): String{
        val md5 = MessageDigest.getInstance("MD5")
        return BigInteger(1, md5.digest(password.toByteArray()))
            .toString(16).padStart(23, '0')
    }

/////////////////////////////////////

    fun addLabel(userMobile: String){
        val date = SimpleDateFormat("yyyy/MM/dd - HH:mm").format(Date())
        val label = hashMapOf(
            "userMobile" to userMobile,
            "labelName" to "Without Label",
            "aboutLabel" to "About Without Label",
            "date" to date
        )

        db.collection("Labels")
            .add(label)
    }

    override fun onBackPressed() {
        Log.e("msaBackPressed", "Dont leave...")
    }


}