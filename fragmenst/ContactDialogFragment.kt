package com.msa.mynotes.fragmenst

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.msa.mynotes.R
import com.msa.mynotes.databinding.FragmentContactDialogBinding

class ContactDialogFragment : BottomSheetDialogFragment() {
    lateinit var contactBinding: FragmentContactDialogBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        contactBinding = FragmentContactDialogBinding.inflate(inflater, container, false)

        // فتح حساباتي على مواقع التواصل الاجتماعي
        contactBinding.cardFacebook.setOnClickListener {
            val facebook = "https://www.facebook.com/MohammedAbdeen2002"
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(facebook)))
        }

        contactBinding.cardInstagram.setOnClickListener {
            val instagram = "https://instagram.com/mo._3bdeen?igshid=YmMyMTA2M2Y="
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(instagram)))
        }

        contactBinding.cardTwitter.setOnClickListener {
            val twitter = "https://twitter.com/moh_3bdeen?t=N7OurV_8vWAyO3ifOLFDuA&s=08"
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(twitter)))
        }

        contactBinding.cardTelegram.setOnClickListener {
            val telegram = "https://t.me/+E4-lbwxE3ENlZWY6"
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(telegram)))
        }

        return contactBinding.root
    }

}