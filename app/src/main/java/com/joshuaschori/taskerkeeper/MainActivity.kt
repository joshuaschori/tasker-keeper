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
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.commit
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.joshuaschori.taskerkeeper.calendar.CalendarFragment
import com.joshuaschori.taskerkeeper.databinding.ActivityMainBinding
import com.joshuaschori.taskerkeeper.diary.diaryDetail.DiaryDetailFragment
import com.joshuaschori.taskerkeeper.diary.diaryMenu.DiaryMenuFragment
import com.joshuaschori.taskerkeeper.habits.habitsDetail.HabitsDetailFragment
import com.joshuaschori.taskerkeeper.habits.habitsMenu.HabitsMenuFragment
import com.joshuaschori.taskerkeeper.tasks.tasksDetail.TasksDetailFragment
import com.joshuaschori.taskerkeeper.tasks.tasksMenu.TasksMenuFragment
import com.joshuaschori.taskerkeeper.ui.theme.TaskerKeeperTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MainActivity : FragmentActivity() {
    private val navigationViewModel: NavigationViewModel by viewModels()

    private fun showDiaryTab(diaryTabState: TabState) {
        when (diaryTabState) {
            is TabState.Detail -> showFragment(DiaryDetailFragment.newInstance(diaryTabState.detailId))
            is TabState.Menu -> showFragment(DiaryMenuFragment())
        }
    }

    private fun showFragment(fragment: Fragment) {
        supportFragmentManager.commit {
            setReorderingAllowed(true)
            replace(R.id.fragmentContainer, fragment)
        }
    }

    private fun showHabitsTab(habitsTabState: TabState) {
        when (habitsTabState) {
            is TabState.Detail -> showFragment(HabitsDetailFragment.newInstance(habitsTabState.detailId))
            is TabState.Menu -> showFragment(HabitsMenuFragment.newInstance())
        }
    }

    private fun showTasksTab(tasksTabState: TabState) {
        when (tasksTabState) {
            is TabState.Detail -> showFragment(TasksDetailFragment.newInstance(tasksTabState.detailId))
            is TabState.Menu -> showFragment(TasksMenuFragment.newInstance())
        }
    }

    private fun handleNavigationAction(navigationAction: NavigationAction) {
        when (navigationAction) {
            is NavigationAction.ChangeBottomNavState -> this.navigationViewModel.changeBottomNavState(navigationAction.bottomNavState)
            is NavigationAction.NavigateToTasksDetail -> this.navigationViewModel.navigateToTasksDetail(navigationAction.categoryId)
            is NavigationAction.NavigateToTasksMenu -> this.navigationViewModel.navigateToTasksMenu()
            is NavigationAction.ShowCalendarTab -> showFragment(CalendarFragment.newInstance())
            is NavigationAction.ShowDiaryTab -> showDiaryTab(navigationAction.diaryTabState)
            is NavigationAction.ShowHabitsTab -> showHabitsTab(navigationAction.habitsTabState)
            is NavigationAction.ShowTasksTab -> showTasksTab(navigationAction.tasksTabState)
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
                    navigationViewModel.uiAction.collect {
                        handleNavigationAction(it)
                    }
                }
            }
        }

        binding.bottomNav.setContent {
            val state by this.navigationViewModel.uiState.collectAsStateWithLifecycle()
            TaskerKeeperTheme {
                Surface {
                    when (state) {
                        is NavigationState.Content -> NavigationContent(
                            bottomNavState = (state as NavigationState.Content).bottomNavState,
                            actionHandler = { handleNavigationAction(it) }
                        )
                        is NavigationState.Error -> NavigationError()
                        is NavigationState.Loading -> NavigationLoading()
                    }
                }
            }
        }

        //TODO
        showTasksTab(tasksTabState = TabState.Menu)

        // TODO firebase placeholder
        Firebase.auth.signInAnonymously()
    }

    @Composable
    fun NavigationContent(
        bottomNavState: BottomNavState,
        actionHandler: NavigationActionHandler
    ) {
        BottomNav(
            bottomNavState = bottomNavState,
            actionHandler = actionHandler
        )
    }

    @Composable
    fun NavigationError() {
        Text("Error")
    }

    @Composable
    fun NavigationLoading() {
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