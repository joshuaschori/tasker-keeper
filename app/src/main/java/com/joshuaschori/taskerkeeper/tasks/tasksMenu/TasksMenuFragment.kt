package com.joshuaschori.taskerkeeper.tasks.tasksMenu

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.unit.dp
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.joshuaschori.taskerkeeper.NavigationViewModel
import com.joshuaschori.taskerkeeper.data.tasks.TaskCategoryEntity
import com.joshuaschori.taskerkeeper.tasks.tasksMenu.ui.CategoryRow
import com.joshuaschori.taskerkeeper.tasks.tasksMenu.ui.TasksMenuTopBar
import com.joshuaschori.taskerkeeper.ui.theme.TaskerKeeperTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class TasksMenuFragment: Fragment() {
    private val navigationViewModel: NavigationViewModel by activityViewModels()
    private val tasksMenuViewModel: TasksMenuViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        tasksMenuViewModel.listenForDatabaseUpdates()
    }

    // TODO not being used unless we're emitting something
    /*override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                tasksMenuViewModel.uiAction.collect {
                    handleAction(it)
                }
            }
        }
    }*/

    private fun handleAction(tasksMenuAction: TasksMenuAction) {
        when (tasksMenuAction) {
            is TasksMenuAction.AddNewCategory -> tasksMenuViewModel.addNewCategory()
            is TasksMenuAction.ClearFocus -> tasksMenuViewModel.clearFocus()
            is TasksMenuAction.EditCategoryTitle -> tasksMenuViewModel.editCategoryTitle(tasksMenuAction.categoryId, tasksMenuAction.titleChange)
            is TasksMenuAction.NavigateToTasksDetail -> navigationViewModel.navigateToTasksDetail(tasksMenuAction.categoryId)
            is TasksMenuAction.ResetClearFocusTrigger -> tasksMenuViewModel.resetClearFocusTrigger()
            is TasksMenuAction.ResetFocusTrigger -> tasksMenuViewModel.resetFocusTrigger()
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
                val state by tasksMenuViewModel.uiState.collectAsStateWithLifecycle()
                TaskerKeeperTheme {
                    Surface {
                        when (state) {
                            is TasksMenuState.Content -> TasksMenuContent(
                                taskCategories = (state as TasksMenuState.Content).taskCategories,
                                clearFocusTrigger = (state as TasksMenuState.Content).clearFocusTrigger,
                                focusCategoryId = (state as TasksMenuState.Content).focusCategoryId,
                                actionHandler = { handleAction(it) }
                            )
                            is TasksMenuState.Error -> TasksMenuError()
                            is TasksMenuState.Loading -> TasksMenuLoading()
                        }
                    }
                }
            }
        }
    }

    @Composable
    fun TasksMenuContent(
        taskCategories: List<TaskCategoryEntity>,
        clearFocusTrigger: Boolean,
        focusCategoryId: Int?,
        actionHandler: TasksMenuActionHandler,
    ) {
        val focusManager = LocalFocusManager.current
        if (clearFocusTrigger) {
            focusManager.clearFocus()
            actionHandler(TasksMenuAction.ResetClearFocusTrigger)
        }

        Column(
            modifier = Modifier
                .fillMaxHeight()
                .fillMaxWidth()
                .clickable(interactionSource = null, indication = null) {
                    actionHandler(TasksMenuAction.ClearFocus)
                }
        ) {
            TasksMenuTopBar(
                actionHandler = actionHandler,
            )
            if (taskCategories.isNotEmpty()) {
                LazyColumn(
                    contentPadding = PaddingValues(
                        start = 16.dp, top = 32.dp, end = 16.dp, bottom = 320.dp
                    ),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .imePadding(),
                ) {
                    items(count = taskCategories.size) {
                        CategoryRow(
                            category = taskCategories[it],
                            focusCategoryId = focusCategoryId,
                            actionHandler = actionHandler,
                        )
                    }
                }
            }
        }
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .fillMaxWidth()
        ) {
            FloatingActionButton(
                onClick = {
                    actionHandler(TasksMenuAction.AddNewCategory)
                    actionHandler(TasksMenuAction.ClearFocus)
                },
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(all = 16.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.Add,
                    contentDescription = "Add Tasks List"
                )
            }
        }
    }

    @Composable
    fun TasksMenuError() {
        Text("TasksMenuFragment Error")
    }

    @Composable
    fun TasksMenuLoading() {
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
        fun newInstance() = TasksMenuFragment()
    }
}