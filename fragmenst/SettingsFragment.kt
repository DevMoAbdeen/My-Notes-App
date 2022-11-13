package com.msa.mynotes.fragmenst

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import com.akexorcist.localizationactivity.core.LanguageSetting
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.msa.mynotes.R
import com.msa.mynotes.activities.ArchiveNotesActivity
import com.msa.mynotes.activities.DeletedNotesActivity
import com.msa.mynotes.activities.ForgotPasswordActivity
import com.msa.mynotes.activities.LoginActivity
import com.msa.mynotes.brodcast_receiver.MyReceiver
import com.msa.mynotes.databinding.FragmentSettingsBinding
import java.util.*

class SettingsFragment : Fragment() {
    lateinit var settingBinding: FragmentSettingsBinding
    private var mInterstitialAd: InterstitialAd? = null
    private final var TAG = "msaAdmob"

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        settingBinding = FragmentSettingsBinding.inflate(inflater, container, false)
        MobileAds.initialize(requireContext()) {}

        // تعريف إعلان بينر
//        val adView = AdView(requireContext())
//        adView.adUnitId = "ca-app-pub-7581259865493448/3113680431"
//        val adRequest = AdRequest.Builder().build()
//        settingBinding.adView.loadAd(adRequest)


        // عشان أضيف الاعلان على الشاشة كاملة
        MobileAds.initialize(requireContext())
        var adRequest = AdRequest.Builder().build()

        InterstitialAd.load(requireContext(), "ca-app-pub-7581259865493448/2983750128", adRequest, object : InterstitialAdLoadCallback() {
            override fun onAdFailedToLoad(adError: LoadAdError) {
                Log.d(TAG, adError.toString())
                mInterstitialAd = null
            }
            override fun onAdLoaded(interstitialAd: InterstitialAd) {
                Log.d(TAG, "Ad was loaded.")
                mInterstitialAd = interstitialAd
            }
        })

        // لو كان التطبيق بالوضع الليلي يغير الألوان لوضع الليل
        val sharedSetting = requireActivity().getSharedPreferences("SettingsApp", AppCompatActivity.MODE_PRIVATE)
        val setting = sharedSetting.edit()
        if(sharedSetting.getString("theme", "light") == "dark") {
            settingBinding.tvSettings.setTextColor(Color.parseColor("#FFFFFF"))
            settingBinding.tvAppName.setTextColor(Color.parseColor("#253557"))
        }
//         تغيير البيانات على الواجهة حسب اللي بختاره المستخدم
        if(sharedSetting.getString("language", "arabic") == "english"){
            settingBinding.rbEnglish.isChecked = true
        }else{
            settingBinding.rbArabic.isChecked = true
        }

        settingBinding.switchDarkMode.isChecked = sharedSetting.getString("theme", "dark") != "light"

        ///////////////////////////////////////////////

        // تغيير اعدادات التطبيق مثل الوضع الليلي واللغة
        settingBinding.switchDarkMode.setOnClickListener {
            if(settingBinding.switchDarkMode.isChecked){
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
                requireActivity().recreate()
                setting.putString("theme", "dark").apply()
            }else{
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
                requireActivity().recreate()
                setting.putString("theme", "light").apply()
            }
        }

        settingBinding.rbArabic.setOnClickListener {
            LanguageSetting.setLanguage(requireActivity(), Locale("ar"))
            setting.putString("language", "arabic").apply()
            requireActivity().recreate()
        }

        settingBinding.rbEnglish.setOnClickListener{
            LanguageSetting.setLanguage(requireActivity(), Locale("en"))
            setting.putString("language", "english").apply()
            requireActivity().recreate()
        }

        ////////////////////////

        settingBinding.cardProfile.setOnClickListener {
            val myReceiver = MyReceiver()
            if(myReceiver.checkConnection(requireContext())){
                requireActivity().startActivity(Intent(requireActivity(), ForgotPasswordActivity::class.java))
            }else{
                Toast.makeText(requireContext(), R.string.noInternetToDoThis, Toast.LENGTH_LONG).show()
            }
        }

        settingBinding.cardArchive.setOnClickListener {
            // إظهار الإعلان
            if (mInterstitialAd != null) {
                mInterstitialAd?.show(requireActivity())
            } else {
                Log.d(TAG, "The interstitial ad wasn't ready yet.")
            }

            requireActivity().startActivity(Intent(requireActivity(), ArchiveNotesActivity::class.java))
        }

        settingBinding.cardDeleted.setOnClickListener {
            // إظهار الإعلان
            if (mInterstitialAd != null) {
                mInterstitialAd?.show(requireActivity())
            } else {
                Log.d(TAG, "The interstitial ad wasn't ready yet.")
            }

            requireActivity().startActivity(Intent(requireActivity(), DeletedNotesActivity::class.java))
        }

        // لإظهار dialog يمكن للمستخدمين التواصل معي من خلال مواقع التواصل الاجتماعي
        settingBinding.cardContact.setOnClickListener {
            val bottomDialog = ContactDialogFragment()
            bottomDialog.show(requireActivity().supportFragmentManager, "contactWithMe")
        }

        settingBinding.cardPrivacy.setOnClickListener {
            val privacy = "https://sites.google.com/view/msa-my-notes/%D8%A7%D9%84%D8%B5%D9%81%D8%AD%D8%A9-%D8%A7%D9%84%D8%B1%D8%A6%D9%8A%D8%B3%D9%8A%D8%A9"
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(privacy)))
        }

        // كود البحث عن التطبيق في المتجر لتقييمه
        settingBinding.cardRate.setOnClickListener {
            val uri = Uri.parse("market://details?id=com.msa.mynotes")
            val myAppLinkToMarket = Intent(Intent.ACTION_VIEW, uri)
            myAppLinkToMarket.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY or
                    Intent.FLAG_ACTIVITY_NEW_DOCUMENT or Intent.FLAG_ACTIVITY_MULTIPLE_TASK)
            try {
                startActivity(myAppLinkToMarket)
            } catch (e: Exception) {
                try{
                    val uri = Uri.parse("https://play.google.com/store/apps/details?id=com.msa.mynotes")
                    val myAppLinkToMarket = Intent(Intent.ACTION_VIEW, uri)
                    startActivity(myAppLinkToMarket)
                }catch (e: Exception) {
                    Toast.makeText(requireContext(), R.string.errorFoundApp, Toast.LENGTH_SHORT).show()
                }
             }
        }

        settingBinding.cardLogout.setOnClickListener {
            val alert = AlertDialog.Builder(requireActivity())
            alert.setTitle(R.string.logout)
            alert.setMessage(R.string.sureLogout)
            alert.setIcon(R.drawable.ic_logout)
            alert.setCancelable(true)

            alert.setPositiveButton(R.string.yes){d, i ->
                val rememberMe = requireActivity().getSharedPreferences("Remember", Context.MODE_PRIVATE)
                val remember = rememberMe.edit()
                remember.remove("remember").apply()

                startActivity(Intent(activity, LoginActivity::class.java))
                requireActivity()!!.finish()
            }

            alert.setNegativeButton(R.string.cancel){d, i ->
                d.cancel()
            }
            alert.create().show()
        }

        return settingBinding.root
    }

}