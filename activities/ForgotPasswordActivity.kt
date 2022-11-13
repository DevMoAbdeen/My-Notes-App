package com.msa.mynotes.activities

import android.app.ProgressDialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.MobileAds
import com.google.firebase.FirebaseException
import com.google.firebase.auth.*
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.msa.mynotes.R
import com.msa.mynotes.brodcast_receiver.MyReceiver
import com.msa.mynotes.databinding.ActivityForgotPasswordBinding
import java.math.BigInteger
import java.security.MessageDigest
import java.util.concurrent.TimeUnit

class ForgotPasswordActivity : AppCompatActivity() {
    lateinit var binding: ActivityForgotPasswordBinding

    // If code sending faild, will used to resend
    private var forceResendingToken: PhoneAuthProvider.ForceResendingToken? = null
    private var mCallBacks: PhoneAuthProvider.OnVerificationStateChangedCallbacks? = null
    private var mVerificationId: String? = null
    private lateinit var firebaseAuth: FirebaseAuth
    private val TAG = "MAIN_TAG"
    lateinit var mobileNumber: String
    lateinit var userId: String

    // Progress dialog
    private lateinit var progressDialog: ProgressDialog

    val db = Firebase.firestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityForgotPasswordBinding.inflate(layoutInflater)
        setContentView(binding.root)
        // تعريف إعلان بانر
        MobileAds.initialize(this) {}
        val adView = AdView(this)
        adView.adUnitId = "ca-app-pub-7581259865493448/3113680431"

        // إظهار الإعلان
        val adRequest = AdRequest.Builder().build()
        binding.adView.loadAd(adRequest)

        firebaseAuth = FirebaseAuth.getInstance()

//        binding.tvMobileNumber.text = "Enter the code sent to mobile number $mobile"

        progressDialog = ProgressDialog(this)
        progressDialog.setTitle("Please wait")
        progressDialog.setCanceledOnTouchOutside(false)

        mCallBacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            override fun onVerificationCompleted(phone: PhoneAuthCredential) {
                signInWithPhoneAuthCredential(phone)
            }

            override fun onVerificationFailed(e: FirebaseException) {
                progressDialog.dismiss()
                Toast.makeText(applicationContext, "${e.message}", Toast.LENGTH_SHORT).show()
            }

            override fun onCodeSent(verificationId: String, token: PhoneAuthProvider.ForceResendingToken) {
                Log.d(TAG, "onCodeSent: $verificationId")
                mVerificationId = verificationId
                forceResendingToken = token
                progressDialog.dismiss()
            }

        }


        binding.btnSendCode.setOnClickListener {
            val email = binding.etEmail.text.toString().trim()
            if(email.isEmpty()){
                Toast.makeText(this@ForgotPasswordActivity, R.string.write_email, Toast.LENGTH_SHORT).show()
            }else{
                val myReceiver = MyReceiver()
                if(myReceiver.checkConnection(this@ForgotPasswordActivity)){
                    isUserRegister(email)
                }else{
                    Toast.makeText(this@ForgotPasswordActivity, R.string.noInternetToDoThis, Toast.LENGTH_LONG).show()
                }
            }
        }

        binding.tvResendCode.setOnClickListener {
            val myReceiver = MyReceiver()
            if(myReceiver.checkConnection(this@ForgotPasswordActivity)){
                binding.icon.setImageResource(R.drawable.ic_act_security)
                binding.email.visibility = View.GONE
                binding.btnSendCode.visibility = View.GONE
                binding.cardChangePassword.visibility = View.GONE
                binding.cardVerifityCode.visibility = View.VISIBLE

                resendVerificationCode(mobileNumber, forceResendingToken)
            }else{
                Toast.makeText(this@ForgotPasswordActivity, R.string.noInternetToDoThis, Toast.LENGTH_LONG).show()
            }
        }

        binding.btnVerityCode.setOnClickListener {
            val code = binding.etCode.text.toString().trim()
            if(code.isEmpty()){
                Toast.makeText(this, R.string.write_code, Toast.LENGTH_SHORT).show()
            }else{
                val myReceiver = MyReceiver()
                if(myReceiver.checkConnection(this@ForgotPasswordActivity)){
                    verifityPhoneNumberWithCode(mVerificationId!!, code)
                }else{
                    Toast.makeText(this@ForgotPasswordActivity, R.string.noInternetToDoThis, Toast.LENGTH_LONG).show()
                }
            }
        }


        binding.btnChange.setOnClickListener {
            val password = binding.etPassword.text.toString().trim()
            val len = password.length
            if(password.isEmpty()){
                Toast.makeText(this, R.string.write_new_password, Toast.LENGTH_SHORT).show()
            }else if(len < 6){
                Toast.makeText(this, R.string.lettersPassword, Toast.LENGTH_SHORT).show()
            }else{
                // Password encryption
                val newPassword = toMD5(password)
                updatePassword(userId, newPassword)
//                changePassword(newPassword)
            }
        }
    }

    private fun startPhoneNumberVerfication(mobile: String) {
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

    private fun resendVerificationCode(mobile: String, token: PhoneAuthProvider.ForceResendingToken?) {
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

    private fun verifityPhoneNumberWithCode(verificationId: String, code: String) {
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

                val sharedPref = getSharedPreferences("UserMobile", MODE_PRIVATE)
                val editor = sharedPref.edit()
                editor.putString("mobile", mobileNumber)
                editor.apply()

                binding.tvWriteEmail.setText(R.string.write_new_password)
                binding.code.visibility = View.GONE
                binding.cardVerifityCode.visibility = View.GONE
                binding.cardChangePassword.visibility = View.VISIBLE
            }

            .addOnFailureListener{e ->
                progressDialog.dismiss()
                Toast.makeText(this, "${e.message}", Toast.LENGTH_LONG).show()
            }
    }

//////////////////////////////////////////////

    // ببحث اذا الايميل المكتوب او الرقم مسجلين قبل هيك او لا عشان لو مسجلين أطلع رسالة error بوجود الرقم او الايميل من قبل
    private fun isUserRegister(email: String){
        db.collection("Users")
            .whereEqualTo("email", email).limit(1)
            .get()
            .addOnSuccessListener { user ->
                if(user.isEmpty){
                    Toast.makeText(this@ForgotPasswordActivity, R.string.email_notRegister, Toast.LENGTH_SHORT).show()
                }else{
                    userId = user.documents[0].id
                    mobileNumber = user.documents[0].getString("mobile").toString()

                    var mobile = ""
                    val len = mobileNumber.length
                    for(i in 0 until len - 3){
                        mobile += "*"
                    }
                    mobile += mobileNumber.substring(len - 3)

                    binding.tvWriteEmail.text = "Enter the code we sent to the number $mobile"
                    binding.icon.setImageResource(R.drawable.ic_act_security)
                    binding.email.visibility = View.GONE
                    binding.btnSendCode.visibility = View.GONE
                    binding.cardChangePassword.visibility = View.GONE
                    binding.cardVerifityCode.visibility = View.VISIBLE

                    startPhoneNumberVerfication(user.documents[0].getString("mobile").toString())
                }
            }
            .addOnFailureListener { error ->
                Toast.makeText(this@ForgotPasswordActivity, "${error.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun updatePassword(id: String, newPassword: String){
        val user = HashMap<String, Any>()
        user["password"] = newPassword

        db.collection("Users").document(id)
            .update(user)
            .addOnSuccessListener {
                val sharedPref = getSharedPreferences("UserMobile", MODE_PRIVATE)
                val editor = sharedPref.edit()
                editor.putString("mobile", mobileNumber)
                editor.apply()

                Toast.makeText(this@ForgotPasswordActivity, R.string.update_password, Toast.LENGTH_SHORT).show()
                startActivity(Intent(this@ForgotPasswordActivity, HomeActivity::class.java))
                finish()
            }
            .addOnFailureListener { error ->
                Toast.makeText(this@ForgotPasswordActivity, "${error.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun toMD5(password: String): String{
        val md5 = MessageDigest.getInstance("MD5")
        return BigInteger(1, md5.digest(password.toByteArray()))
            .toString(16).padStart(23, '0')
    }

    // Change password in email Auth
    /*
    fun changePassword(newPassword: String){
        val user = firebaseAuth.currentUser
        if(user != null && user.email != null){
            val credential = EmailAuthProvider.getCredential(user.email!!, oldPassword)

            user?.reauthenticate(credential)
                ?.addOnCompleteListener {
                    if(it.isSuccessful){
                        user?.updatePassword(newPassword)
                            ?.addOnCompleteListener {
                                Toast.makeText(this, "Password Changed", Toast.LENGTH_SHORT).show()
                            }
                            ?.addOnFailureListener { error ->
                                Toast.makeText(this@ForgotPasswordActivity, "${error.message}", Toast.LENGTH_SHORT).show()
                            }
                    }else{
                        Toast.makeText(this@ForgotPasswordActivity, "Not successfully !", Toast.LENGTH_SHORT).show()
                    }
                }
                ?.addOnFailureListener { error ->
                    Toast.makeText(this@ForgotPasswordActivity, "${error.message}", Toast.LENGTH_SHORT).show()
                }

        }
    }
*/
}