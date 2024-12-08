package com.example.taskerkeeper

import android.os.Bundle
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.commit
import androidx.fragment.app.replace

class MainActivity : FragmentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        showFragment1()
    }

    fun showFragment2() {
        supportFragmentManager.commit {
            setReorderingAllowed(true)
            // Replace whatever is in the fragment_container view with this fragment
            replace<Fragment2>(R.id.container)
        }
    }

    fun showFragment1() {
        supportFragmentManager.commit {
            setReorderingAllowed(true)
            // Replace whatever is in the fragment_container view with this fragment
            replace<Fragment1>(R.id.container)
        }
    }
}