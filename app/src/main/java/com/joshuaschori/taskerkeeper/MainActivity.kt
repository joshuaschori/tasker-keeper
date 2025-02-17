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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.commit
import androidx.fragment.app.replace
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.joshuaschori.taskerkeeper.databinding.ActivityMainBinding
import com.joshuaschori.taskerkeeper.diary.DiaryFragment
import com.joshuaschori.taskerkeeper.tasks.TasksListAction
import com.joshuaschori.taskerkeeper.tasks.TasksListFragment
import com.joshuaschori.taskerkeeper.tasks.TasksMenuFragment
import com.joshuaschori.taskerkeeper.tasks.TasksListViewModel
import com.joshuaschori.taskerkeeper.ui.theme.TaskerKeeperTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : FragmentActivity() {
    private val mainActivityViewModel: MainActivityViewModel by viewModels()
    private val tasksListViewModel: TasksListViewModel by viewModels()

    private fun showTasksTab(
        tasksTabState: TasksTabState
    ) {
        when (tasksTabState) {
            TasksTabState.LIST -> showTasksFragment()
            TasksTabState.MENU -> showTasksMenuFragment()
        }
    }

    private fun showTasksFragment() {
        supportFragmentManager.commit {
            setReorderingAllowed(true)
            // Replace whatever is in the fragment_container view with this fragment
            replace<TasksListFragment>(R.id.fragmentContainer)
        }
    }

    private fun showTasksMenuFragment() {
        supportFragmentManager.commit {
            setReorderingAllowed(true)
            // Replace whatever is in the fragment_container view with this fragment
            replace<TasksMenuFragment>(R.id.fragmentContainer)
        }
    }

    private fun showDiaryFragment() {
        supportFragmentManager.commit {
            setReorderingAllowed(true)
            // Replace whatever is in the fragment_container view with this fragment
            replace<DiaryFragment>(R.id.fragmentContainer)
        }
    }

    private fun handleMainActivityAction(mainActivityAction: MainActivityAction) {
        when (mainActivityAction) {
            is MainActivityAction.ChangeBottomNavState ->
                mainActivityViewModel.changeBottomNavState(mainActivityAction.bottomNavState)
            is MainActivityAction.ShowDiaryTab -> showDiaryFragment()
            is MainActivityAction.ShowTasksTab -> showTasksTab(mainActivityAction.tasksTabState)
        }
    }

    private fun handleTasksListAction(tasksListAction: TasksListAction) {
        when (tasksListAction) {
            is TasksListAction.TellMainActivityToNavigateToTasksMenu ->
                mainActivityViewModel.navigateToTasksMenu()
            else -> { }
        }
    }

    // Contains all the views
    private lateinit var binding: ActivityMainBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)
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
            // TODO collect in a better way? remember difference between this and other handleAction above in lambda
            LaunchedEffect(Unit) {
                mainActivityViewModel.uiAction.collect {
                    handleMainActivityAction(it)
                }
            }
            LaunchedEffect(Unit) {
                tasksListViewModel.uiAction.collect {
                    handleTasksListAction(it)
                }
            }
        }

        showTasksFragment()

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