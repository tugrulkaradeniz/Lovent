package com.tkara.lovent.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment

class ProfileFragment : Fragment() {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = TextView(requireContext())
        view.text = "👤 Profil\n\nYakında:\n• Profil düzenleme\n• Ayarlar\n• Çıkış yap\n• İstatistikler"
        view.textSize = 18f
        view.setPadding(48, 48, 48, 48)
        return view
    }
}