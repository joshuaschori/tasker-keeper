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
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
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
import com.joshuaschori.taskerkeeper.Constants.MAX_LAYERS_OF_SUBTASKS
import com.joshuaschori.taskerkeeper.DragHandler
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
            is TasksDetailAction.RearrangeTasks -> tasksDetailViewModel.rearrangeTasks(
                tasksDetailAction.taskId, tasksDetailAction.aboveDestinationTask, tasksDetailAction.aboveDestinationTaskLayer, tasksDetailAction.belowDestinationTask,
                tasksDetailAction.belowDestinationTaskLayer, tasksDetailAction.requestedLayer
            )
            is TasksDetailAction.ResetClearFocusTrigger -> tasksDetailViewModel.resetClearFocusTrigger()
            is TasksDetailAction.ResetFocusTrigger -> tasksDetailViewModel.resetFocusTrigger()
            is TasksDetailAction.SetDraggedTaskSize -> tasksDetailViewModel.setDraggedTaskSize(tasksDetailAction.size)
            is TasksDetailAction.SetLazyListIndexBeingDragged -> tasksDetailViewModel.setLazyListIndexBeingDragged(tasksDetailAction.index)
            is TasksDetailAction.SetLazyListTargetIndex -> tasksDetailViewModel.setLazyListTargetIndex(tasksDetailAction.index)
            is TasksDetailAction.UpdateDragHandler -> tasksDetailViewModel.updateDragHandler(tasksDetailAction.dragHandler)
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
                                lazyListIndexBeingDragged = (state as TasksDetailState.Content).lazyListIndexBeingDragged,
                                lazyListTargetIndex = (state as TasksDetailState.Content).lazyListTargetIndex,
                                draggedTaskSize = (state as TasksDetailState.Content).draggedTaskSize,
                                dragHandler = (state as TasksDetailState.Content).dragHandler,
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
        lazyListIndexBeingDragged: Int?,
        lazyListTargetIndex: Int?,
        draggedTaskSize: Int?,
        dragHandler: DragHandler,
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
                val lazyListState = rememberLazyListState()
                val lazyTaskList = recursivelyAddLazyTasks(
                    taskList = taskList,
                    taskLayer = 0,
                    focusTaskId = focusTaskId,
                    isAutoSortCheckedTasks = isAutoSortCheckedTasks,
                    selectedTasksDetailExtensionMode = selectedTasksDetailExtensionMode,
                    lazyListIndexBeingDragged = lazyListIndexBeingDragged,
                    lazyListTargetIndex = lazyListTargetIndex,
                    lazyListState = lazyListState,
                    lazyListStartingIndex = 0,
                    lazyListLambda = {},
                    draggedTaskSize = draggedTaskSize,
                    dragHandler = dragHandler,
                    actionHandler = actionHandler,
                )
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
                    items(lazyTaskList) {
                        it()
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

@Composable
private fun recursivelyAddLazyTasks(
    taskList: List<Task>,
    taskLayer: Int,
    focusTaskId: Int?,
    isAutoSortCheckedTasks: Boolean,
    selectedTasksDetailExtensionMode: TasksDetailExtensionMode,
    lazyListIndexBeingDragged: Int?,
    lazyListTargetIndex: Int?,
    lazyListState: LazyListState,
    lazyListStartingIndex: Int,
    lazyListLambda: (Int) -> Unit,
    draggedTaskSize: Int?,
    dragHandler: DragHandler,
    actionHandler: TasksDetailActionHandler,
): List<@Composable () -> Unit> {
    val lazyTaskList: MutableList<@Composable () -> Unit> = mutableListOf()
    var lazyListIndex: Int = lazyListStartingIndex
    // TODO list of task ids with lazy list index, also task layer?
    val listOfTaskIds = mutableListOf<Int>()
    for (task in taskList) {
        val newLazyListIndex = lazyListIndex
        lazyTaskList.add {
            TaskWithSubtasks(
                task = task,
                taskLayer = taskLayer,
                focusTaskId = focusTaskId,
                isAutoSortCheckedTasks = isAutoSortCheckedTasks,
                selectedTasksDetailExtensionMode = selectedTasksDetailExtensionMode,
                lazyListIndexBeingDragged = lazyListIndexBeingDragged,
                lazyListTargetIndex = lazyListTargetIndex,
                lazyListState = lazyListState,
                lazyListIndex = newLazyListIndex,
                draggedTaskSize = draggedTaskSize,
                dragHandler = dragHandler,
                actionHandler = actionHandler,
            )
        }
        listOfTaskIds.add(task.taskId)
        lazyListIndex++
        if (task.subtaskList.isNotEmpty() && task.isExpanded && lazyListIndex - 1 != lazyListIndexBeingDragged && taskLayer < MAX_LAYERS_OF_SUBTASKS) {
            lazyTaskList.addAll(
                recursivelyAddLazyTasks(
                    taskList = task.subtaskList,
                    taskLayer = taskLayer + 1,
                    focusTaskId = focusTaskId,
                    isAutoSortCheckedTasks = isAutoSortCheckedTasks,
                    selectedTasksDetailExtensionMode = selectedTasksDetailExtensionMode,
                    lazyListIndexBeingDragged = lazyListIndexBeingDragged,
                    lazyListTargetIndex = lazyListTargetIndex,
                    lazyListState = lazyListState,
                    lazyListStartingIndex = lazyListIndex,
                    lazyListLambda =  { lazyListIndex = it },
                    draggedTaskSize = draggedTaskSize,
                    dragHandler = dragHandler,
                    actionHandler = actionHandler,
                )
            )
        }
    }
    if (taskLayer == 0) {
        lazyListIndex = 0
    } else {
        lazyListLambda(lazyListIndex)
    }
    return lazyTaskList
}