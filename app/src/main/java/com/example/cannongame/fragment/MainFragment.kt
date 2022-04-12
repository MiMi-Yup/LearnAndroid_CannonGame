package com.example.cannongame.fragment

import android.media.AudioManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.cannongame.R
import com.example.cannongame.sub_class.CannonView

class MainFragment : Fragment() {
    private lateinit var svGame: CannonView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        activity?.volumeControlStream = AudioManager.STREAM_MUSIC
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_main, container, false)
        svGame = view.findViewById<CannonView>(R.id.svGame)
        return view
    }

    override fun onPause() {
        super.onPause()
        svGame.stopGame()
    }

    override fun onDestroy() {
        super.onDestroy()
        svGame.releaseResources()
    }
}