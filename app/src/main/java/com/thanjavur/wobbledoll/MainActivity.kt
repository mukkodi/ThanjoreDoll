package com.thanjavur.wobbledoll

import android.os.Bundle
import android.view.WindowManager
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        setContentView(R.layout.activity_main)

        val viewPager = findViewById<ViewPager2>(R.id.viewPager)
        val dots = listOf(
            findViewById<TextView>(R.id.dot0),
            findViewById<TextView>(R.id.dot1),
            findViewById<TextView>(R.id.dot2),
            findViewById<TextView>(R.id.dot3),
            findViewById<TextView>(R.id.dot4),
            findViewById<TextView>(R.id.dot5),
        )

        viewPager.adapter = object : FragmentStateAdapter(this) {
            override fun getItemCount() = 6
            override fun createFragment(position: Int): Fragment = when (position) {
                0 -> TanjavurDollFragment()
                1 -> PattiFragment()
                2 -> ThataFragment()
//                3 -> ConicalPendulumFragment()
                3 -> RockingHorseFragment()
                else -> RockingHorseFragment2()
            }
        }
        
        // Open to the 3rd page (ThataFragment) by default
        viewPager.setCurrentItem(2, false)

        val goldColor = ContextCompat.getColor(this, R.color.gold_light)
        val dimColor = ContextCompat.getColor(this, R.color.gold_dim)

        viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                dots.forEachIndexed { index, dot ->
                    dot.setTextColor(if (index == position) goldColor else dimColor)
                }
            }
        })
    }
}