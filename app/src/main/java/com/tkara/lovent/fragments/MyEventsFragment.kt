package com.tkara.lovent.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.tkara.lovent.R

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [MyEventsFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class MyEventsFragment : Fragment() {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = TextView(requireContext())
        view.text = "📅 Etkinliklerim\n\nYakında:\n• Oluşturduğum etkinlikler\n• Katıldığım etkinlikler\n• Geçmiş etkinlikler"
        view.textSize = 18f
        view.setPadding(48, 48, 48, 48)
        return view
    }
}