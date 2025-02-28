package com.joshuaschori.taskerkeeper.habits.habitsMenu

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.joshuaschori.taskerkeeper.NavigationViewModel
import com.joshuaschori.taskerkeeper.diary.diaryMenu.DiaryMenuAction
import com.joshuaschori.taskerkeeper.habits.habitsMenu.HabitsMenuState
import com.joshuaschori.taskerkeeper.habits.habitsMenu.HabitsMenuViewModel
import com.joshuaschori.taskerkeeper.tasks.tasksMenu.TasksMenuFragment
import com.joshuaschori.taskerkeeper.ui.theme.TaskerKeeperTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class HabitsMenuFragment: Fragment() {
    private val navigationViewModel: NavigationViewModel by activityViewModels()
    private val habitsMenuViewModel: HabitsMenuViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        habitsMenuViewModel.listenForDatabaseUpdates()
    }

    // TODO not being used unless we're emitting something
    /*override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                diaryMenuViewModel.uiAction.collect {
                    handleAction(it)
                }
            }
        }
    }*/

    private fun handleAction(habitsMenuAction: DiaryMenuAction) {
        when (habitsMenuAction) {
            else -> {}
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            // Dispose of the Composition when the view's LifecycleOwner is destroyed
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                val state by habitsMenuViewModel.uiState.collectAsStateWithLifecycle()
                TaskerKeeperTheme {
                    Surface {
                        when (state) {
                            is HabitsMenuState.Content -> HabitsMenuContent()
                            is HabitsMenuState.Error -> HabitsMenuError()
                            is HabitsMenuState.Loading -> HabitsMenuLoading()
                        }
                    }
                }
            }
        }
    }

    @Composable
    fun HabitsMenuContent() {

    }

    @Composable
    fun HabitsMenuError() {
        Text("HabitsMenuFragment Error")
    }

    @Composable
    fun HabitsMenuLoading() {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
    }

    companion object {
        fun newInstance() = HabitsMenuFragment()
    }
}