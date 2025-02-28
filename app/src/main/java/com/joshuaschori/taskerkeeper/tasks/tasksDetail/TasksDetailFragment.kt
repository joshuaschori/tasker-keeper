package com.joshuaschori.taskerkeeper.tasks.tasksDetail

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
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.joshuaschori.taskerkeeper.NavigationViewModel
import com.joshuaschori.taskerkeeper.tasks.tasksDetail.ui.TaskAndSubtasks
import com.joshuaschori.taskerkeeper.tasks.tasksDetail.ui.TasksDetailTopBar
import com.joshuaschori.taskerkeeper.ui.theme.TaskerKeeperTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlin.properties.Delegates

@AndroidEntryPoint
class TasksDetailFragment: Fragment() {
    private val navigationViewModel: NavigationViewModel by activityViewModels()
    private val tasksDetailViewModel: TasksDetailViewModel by viewModels()
    private var parentCategoryId: Int by Delegates.notNull()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        parentCategoryId = requireArguments().getInt(PARENT_CATEGORY_ID)
        tasksDetailViewModel.listenForDatabaseUpdates(parentCategoryId)
    }

    // TODO not being used unless we're emitting something
    /*override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                tasksDetailViewModel.uiAction.collect {
                    handleAction(it)
                }
            }
        }
    }*/

    private fun handleAction(tasksDetailAction: TasksDetailAction) {
        when (tasksDetailAction) {
            is TasksDetailAction.AddNewTask -> tasksDetailViewModel.addNewTask(tasksDetailAction.selectedTaskId, tasksDetailAction.parentId)
            is TasksDetailAction.ChangeTasksDetailExtensionMode -> tasksDetailViewModel.changeTasksDetailExtensionMode(tasksDetailAction.tasksDetailExtensionMode)
            is TasksDetailAction.ClearFocus -> tasksDetailViewModel.clearFocus()
            is TasksDetailAction.DeleteTask -> tasksDetailViewModel.deleteTask(tasksDetailAction.taskId)
            is TasksDetailAction.EditTaskDescription -> tasksDetailViewModel.editTaskDescription(tasksDetailAction.taskId, tasksDetailAction.descriptionChange)
            is TasksDetailAction.ExpandTask -> tasksDetailViewModel.expandTask(tasksDetailAction.taskId)
            is TasksDetailAction.MarkTaskComplete -> tasksDetailViewModel.markTaskComplete(tasksDetailAction.taskId)
            is TasksDetailAction.MarkTaskIncomplete -> tasksDetailViewModel.markTaskIncomplete(tasksDetailAction.taskId)
            is TasksDetailAction.MinimizeTask -> tasksDetailViewModel.minimizeTask(tasksDetailAction.taskId)
            is TasksDetailAction.NavigateToTasksMenu -> navigationViewModel.navigateToTasksMenu()
            is TasksDetailAction.ResetClearFocusTrigger -> tasksDetailViewModel.resetClearFocusTrigger()
            is TasksDetailAction.ResetFocusTrigger -> tasksDetailViewModel.resetFocusTrigger()
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
                val state by tasksDetailViewModel.uiState.collectAsStateWithLifecycle()
                TaskerKeeperTheme {
                    Surface {
                        when (state) {
                            is TasksDetailState.Content -> TasksContent(
                                taskList = (state as TasksDetailState.Content).taskList,
                                selectedTasksDetailExtensionMode = (state as TasksDetailState.Content)
                                    .selectedTasksDetailExtensionMode,
                                clearFocusTrigger = (state as TasksDetailState.Content).clearFocusTrigger,
                                focusTaskId = (state as TasksDetailState.Content).focusTaskId,
                                isAutoSortCheckedTasks = (state as TasksDetailState.Content)
                                    .isAutoSortCheckedTasks,
                                actionHandler = { handleAction(it) },
                            )
                            is TasksDetailState.Error -> TasksError()
                            is TasksDetailState.Loading -> TasksLoading()
                        }
                    }
                }
            }
        }
    }

    @Composable
    fun TasksContent(
        taskList: List<Task>,
        selectedTasksDetailExtensionMode: TasksDetailExtensionMode,
        clearFocusTrigger: Boolean,
        focusTaskId: Int?,
        isAutoSortCheckedTasks: Boolean,
        actionHandler: TasksDetailActionHandler,
    ) {
        val focusManager = LocalFocusManager.current
        if (clearFocusTrigger) {
            focusManager.clearFocus()
            actionHandler(TasksDetailAction.ResetClearFocusTrigger)
        }

        Column(
            modifier = Modifier
                .fillMaxHeight()
                .fillMaxWidth()
                .clickable(interactionSource = null, indication = null) {
                    actionHandler(TasksDetailAction.ClearFocus)
                }
        ) {
            TasksDetailTopBar(
                selectedTasksDetailExtensionMode = selectedTasksDetailExtensionMode,
                actionHandler = actionHandler,
            )
            if (taskList.isNotEmpty()) {
                LazyColumn(
                    contentPadding = PaddingValues(
                        start = 16.dp, top = 32.dp, end = 16.dp, bottom = 320.dp
                    ),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .imePadding(),
                ) {
                    items(count = taskList.size) {
                        TaskAndSubtasks(
                            task = taskList[it],
                            taskLayer = 0,
                            focusTaskId = focusTaskId,
                            isAutoSortCheckedTasks = isAutoSortCheckedTasks,
                            selectedTasksDetailExtensionMode = selectedTasksDetailExtensionMode,
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
                    actionHandler(TasksDetailAction.AddNewTask(null, null))
                    actionHandler(TasksDetailAction.ClearFocus)
                },
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(all = 16.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.Add,
                    contentDescription = "Add Task"
                )
            }
        }
    }

    @Composable
    fun TasksError() {
        Text("TasksDetail Fragment Error")
    }

    @Composable
    fun TasksLoading() {
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
        private const val PARENT_CATEGORY_ID = "parentCategoryId"
        fun newInstance(
            parentCategoryId: Int
        ): TasksDetailFragment {
            val fragment = TasksDetailFragment()
            val bundle = bundleOf(
                PARENT_CATEGORY_ID to parentCategoryId
            )
            fragment.arguments = bundle
            return fragment
        }
    }
}