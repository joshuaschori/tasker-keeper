package com.joshuaschori.taskerkeeper

import android.os.Bundle
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.commit
import androidx.fragment.app.replace
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.joshuaschori.taskerkeeper.databinding.ActivityMainBinding
import com.joshuaschori.taskerkeeper.diary.DiaryFragment
import com.joshuaschori.taskerkeeper.tasks.tasksDetail.TasksDetailFragment
import com.joshuaschori.taskerkeeper.tasks.tasksDetail.TasksDetailViewModel
import com.joshuaschori.taskerkeeper.tasks.tasksMenu.TasksMenuFragment
import com.joshuaschori.taskerkeeper.tasks.tasksMenu.TasksMenuViewModel
import com.joshuaschori.taskerkeeper.ui.theme.TaskerKeeperTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MainActivity : FragmentActivity() {
    private val mainActivityViewModel: MainActivityViewModel by viewModels()
    private val tasksDetailViewModel: TasksDetailViewModel by viewModels()
    private val tasksMenuViewModel: TasksMenuViewModel by viewModels()

    private fun showTasksTab(
        tasksTabState: TasksTabState
    ) {
        when (tasksTabState) {
            is TasksTabState.Detail -> showTasksDetailFragment(tasksTabState.parentCategoryId)
            is TasksTabState.Menu -> showTasksMenuFragment()
        }
    }

    private fun showTasksDetailFragment(
        parentCategoryId: Int
    ) {
        val tasksDetailFragment = TasksDetailFragment.newInstance(parentCategoryId)
        supportFragmentManager.commit {
            setReorderingAllowed(true)
            // Replace whatever is in the fragment_container view with this fragment
            replace(R.id.fragmentContainer, tasksDetailFragment)
        }
    }

    private fun showTasksMenuFragment() {
        supportFragmentManager.commit {
            setReorderingAllowed(true)
            // Replace whatever is in the fragment_container view with this fragment
            replace<TasksMenuFragment>(R.id.fragmentContainer)
        }
    }

    private fun showDiaryTab() {
        supportFragmentManager.commit {
            setReorderingAllowed(true)
            // Replace whatever is in the fragment_container view with this fragment
            replace<DiaryFragment>(R.id.fragmentContainer)
        }
    }

    private fun handleMainActivityAction(mainActivityAction: MainActivityAction) {
        when (mainActivityAction) {
            is MainActivityAction.ChangeBottomNavState -> mainActivityViewModel.changeBottomNavState(mainActivityAction.bottomNavState)
            is MainActivityAction.NavigateToTasksDetail -> mainActivityViewModel.navigateToTasksDetail(mainActivityAction.categoryId)
            is MainActivityAction.NavigateToTasksMenu -> mainActivityViewModel.navigateToTasksMenu()
            is MainActivityAction.ShowDiaryTab -> showDiaryTab()
            is MainActivityAction.ShowTasksTab -> showTasksTab(mainActivityAction.tasksTabState)
        }
    }

    // Contains all the views
    private lateinit var binding: ActivityMainBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    mainActivityViewModel.uiAction.collect {
                        handleMainActivityAction(it)
                    }
                }
                launch {
                    tasksDetailViewModel.mainActivityAction.collect {
                        handleMainActivityAction(it)
                    }
                }
                launch {
                    tasksMenuViewModel.mainActivityAction.collect {
                        handleMainActivityAction(it)
                    }
                }
            }
        }

        binding.bottomNav.setContent {
            val state by mainActivityViewModel.uiState.collectAsStateWithLifecycle()
            TaskerKeeperTheme {
                Surface {
                    when (state) {
                        is MainActivityState.Content -> MainActivityContent(
                            bottomNavState = (state as MainActivityState.Content).bottomNavState,
                            actionHandler = { handleMainActivityAction(it) }
                        )
                        is MainActivityState.Error -> MainActivityError()
                        is MainActivityState.Loading -> MainActivityLoading()
                    }
                }
            }
        }

        showTasksMenuFragment()

        // TODO firebase placeholder
        Firebase.auth.signInAnonymously()
    }

    @Composable
    fun MainActivityContent(
        bottomNavState: BottomNavState,
        actionHandler: MainActivityActionHandler
    ) {
        BottomNav(
            bottomNavState = bottomNavState,
            actionHandler = actionHandler
        )
    }

    @Composable
    fun MainActivityError() {
        Text("Error")
    }

    @Composable
    fun MainActivityLoading() {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
    }
}