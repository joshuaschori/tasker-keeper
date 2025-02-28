package com.joshuaschori.taskerkeeper.calendar

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
import com.joshuaschori.taskerkeeper.diary.diaryDetail.DiaryDetailAction
import com.joshuaschori.taskerkeeper.tasks.tasksMenu.TasksMenuFragment
import com.joshuaschori.taskerkeeper.ui.theme.TaskerKeeperTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class CalendarFragment: Fragment() {
    private val navigationViewModel: NavigationViewModel by activityViewModels()
    private val calendarViewModel: CalendarViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        calendarViewModel.listenForDatabaseUpdates()
    }

    // TODO not being used unless we're emitting something
    /*override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                diaryDetailViewModel.uiAction.collect {
                    handleAction(it)
                }
            }
        }
    }*/

    private fun handleAction(calendarAction: DiaryDetailAction) {
        when (calendarAction) {
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
                val state by calendarViewModel.uiState.collectAsStateWithLifecycle()
                TaskerKeeperTheme {
                    Surface {
                        when (state) {
                            is CalendarState.Content -> CalendarContent()
                            is CalendarState.Error -> CalendarError()
                            is CalendarState.Loading -> CalendarLoading()
                        }
                    }
                }
            }
        }
    }

    @Composable
    fun CalendarContent() {

    }

    @Composable
    fun CalendarError() {
        Text("CalendarFragment Error")
    }

    @Composable
    fun CalendarLoading() {
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
        fun newInstance() = CalendarFragment()
    }
}