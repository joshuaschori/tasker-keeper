package com.example.taskerkeeper

import android.os.Bundle
import androidx.compose.material3.Surface
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.commit
import androidx.fragment.app.replace
import com.example.taskerkeeper.databinding.ActivityMainBinding
import com.example.taskerkeeper.diary.DiaryFragment
import com.example.taskerkeeper.tasks.TasksFragment
import com.example.taskerkeeper.ui.theme.TaskerKeeperTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : FragmentActivity() {
    // Contains all the views
    private lateinit var binding: ActivityMainBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)
        binding.bottomNav.setContent {
            TaskerKeeperTheme {
                Surface {
                    BottomNav {
                        when (it) {
                            0 -> { showDiaryFragment() }
                            1 -> { showTaskFragment() }
                            2 -> { showDiaryFragment() }
                            3 -> { showDiaryFragment() }
                            else -> { throw IllegalStateException("Unknown Fragment Int") }
                        }
                    }
                }
            }
        }
        showTaskFragment()
    }

    fun showTaskFragment() {
        supportFragmentManager.commit {
            setReorderingAllowed(true)
            // Replace whatever is in the fragment_container view with this fragment
            replace<TasksFragment>(R.id.fragmentContainer)
        }
    }

    fun showDiaryFragment() {
        supportFragmentManager.commit {
            setReorderingAllowed(true)
            // Replace whatever is in the fragment_container view with this fragment
            replace<DiaryFragment>(R.id.fragmentContainer)
        }
    }
}