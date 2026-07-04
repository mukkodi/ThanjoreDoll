package com.thanjavur.wobbledoll

import android.content.pm.ActivityInfo
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment

/**
 * Landscape page showing two rocking figures side by side:
 *  - rocking_men   → rocks side to side (tap left/right)
 *  - rocking_women → rocks to the front and back (tap top/bottom)
 *
 * The activity is locked to portrait in the manifest, so this page forces
 * landscape while it is the visible page and restores portrait when swiped
 * away. (The activity declares configChanges for orientation, so this rotation
 * re-lays-out in place instead of recreating the ViewPager.)
 */
class RockingHorseFragment2 : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.fragment_rocking_horse2, container, false)

    override fun onResume() {
        super.onResume()
        requireActivity().requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
    }

    override fun onPause() {
        super.onPause()
        requireActivity().requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
    }
}