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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import com.joshuaschori.taskerkeeper.tasks.tasksDetail.ui.TaskWithSubtasks
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
            is TasksDetailAction.RearrangeTasks -> tasksDetailViewModel.rearrangeTasks(
                tasksDetailAction.taskId, tasksDetailAction.aboveDestinationTask, tasksDetailAction.aboveDestinationTaskLayer, tasksDetailAction.belowDestinationTask,
                tasksDetailAction.belowDestinationTaskLayer, tasksDetailAction.requestedLayer
            )
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
                var lazyListTaskIdBeingDragged by remember { mutableStateOf<Int?>(null) }
                var lazyListIndexBeingDragged by remember { mutableStateOf<Int?>(null) }
                var lazyListTargetIndex by remember { mutableStateOf<Int?>(null) }
                var draggedTaskSize by remember { mutableStateOf<Int?>(null) }
                var dragDirection by remember { mutableStateOf("") }
                val lazyListState = rememberLazyListState()
                val visibleTaskList = determineVisibleTasks(taskList, lazyListTaskIdBeingDragged)
                val lazyTaskList = unpackTaskAndSubtasks(visibleTaskList)
                LazyColumn(
                    contentPadding = PaddingValues(
                        start = 16.dp, top = 32.dp, end = 16.dp, bottom = 320.dp
                    ),
                    verticalArrangement = Arrangement.spacedBy(1.dp),
                    state = lazyListState,
                    modifier = Modifier
                        .fillMaxWidth()
                        .imePadding()
                ) {
                    itemsIndexed(lazyTaskList) { index, task ->
                        TaskWithSubtasks(
                            task = task,
                            taskLayer = task.taskLayer,
                            selectedTasksDetailExtensionMode = selectedTasksDetailExtensionMode,
                            focusTaskId = focusTaskId,
                            isAutoSortCheckedTasks = isAutoSortCheckedTasks,
                            lazyListIndex = index,
                            lazyListIndexBeingDragged = lazyListIndexBeingDragged,
                            lazyListTargetIndex = lazyListTargetIndex,
                            lazyListState = lazyListState,
                            draggedTaskSize = draggedTaskSize,
                            dragDirection = dragDirection,
                            setDraggedTaskSize = { draggedTaskSize = it },
                            setLazyListIndexBeingDragged = { lazyListIndexBeingDragged = it },
                            setLazyListTaskIdBeingDragged = { lazyListTaskIdBeingDragged = it },
                            setLazyListTargetIndex = { lazyListTargetIndex = it },
                            setDragDirection = { dragDirection = it },
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

private fun determineVisibleTasks (taskList: List<Task>, lazyListTaskIdBeingDragged: Int?): List<Task> {
    val updatedTasks = mutableListOf<Task>()
    fun traverse(task: Task): Task {
        val taskWithSubtasks = task.copy(
            subtaskList = if (task.subtaskList != null && task.isExpanded && task.taskId != lazyListTaskIdBeingDragged) {
                task.subtaskList.map { traverse(task = it) }
            } else if (task.subtaskList == null) {
                null
            } else {
                listOf()
            },
        )
        return taskWithSubtasks
    }
    taskList.forEach{ updatedTasks.add(traverse(it)) }
    return updatedTasks
}

// returns list of Tasks, with the root Task and its subtasks in order, prepped for lazyList
private fun unpackTaskAndSubtasks(taskList: List<Task>): List<Task> {
    val unpackedTaskList = mutableListOf<Task>()
    fun traverse(task: Task) {
        unpackedTaskList.add(task)
        task.subtaskList?.forEach {
            traverse(it)
        }
    }
    taskList.forEach{ traverse(it) }
    return unpackedTaskList
}