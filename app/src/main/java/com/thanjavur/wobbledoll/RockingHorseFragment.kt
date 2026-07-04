package com.thanjavur.wobbledoll

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment

/**
 * Page showing a single rocking horse that wobbles when the user taps its
 * left or right side. Touch-driven only, so no sensor/Hilt wiring is needed.
 */
class RockingHorseFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.fragment_rocking_horse, container, false)
}