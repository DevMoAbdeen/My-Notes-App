package com.msa.mynotes.app

import android.app.Application
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import com.akexorcist.localizationactivity.core.LanguageSetting
import java.util.*

class App : Application() {
// بعمل الكلاس عشان اتحكم بالTheme واللغة أول ما يفتح التطبيق

    override fun onCreate() {
        super.onCreate()

        val sharedSetting = getSharedPreferences("SettingsApp", AppCompatActivity.MODE_PRIVATE)

        if(sharedSetting.getString("language", "arabic") == "english"){
            LanguageSetting.setLanguage(this, Locale("en"))
        }else{
            LanguageSetting.setLanguage(this, Locale("ar"))
        }

        if(sharedSetting.getString("theme", "light") == "dark"){
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        }else{
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        }

    }
}