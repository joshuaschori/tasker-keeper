package com.joshuaschori.taskerkeeper.tasks.tasksDetail

import android.os.Bundle
import android.util.Log
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
import com.joshuaschori.taskerkeeper.Constants.FLOATING_ACTION_BUTTON_PADDING
import com.joshuaschori.taskerkeeper.Constants.LAZY_COLUMN_BOTTOM_PADDING
import com.joshuaschori.taskerkeeper.Constants.LAZY_COLUMN_END_PADDING
import com.joshuaschori.taskerkeeper.Constants.LAZY_COLUMN_START_PADDING
import com.joshuaschori.taskerkeeper.Constants.LAZY_COLUMN_TOP_PADDING
import com.joshuaschori.taskerkeeper.Constants.LAZY_COLUMN_VERTICAL_ARRANGEMENT_SPACING
import com.joshuaschori.taskerkeeper.NavigationViewModel
import com.joshuaschori.taskerkeeper.DragMode
import com.joshuaschori.taskerkeeper.YDirection
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
            is TasksDetailAction.AddNewTask -> tasksDetailViewModel.addNewTask(
                selectedTaskId = tasksDetailAction.selectedTaskId,
                parentId = tasksDetailAction.parentId
            )
            is TasksDetailAction.ChangeTasksDetailExtensionMode -> tasksDetailViewModel.changeTasksDetailExtensionMode(tasksDetailExtensionMode = tasksDetailAction.tasksDetailExtensionMode)
            is TasksDetailAction.ClearFocus -> tasksDetailViewModel.clearFocus()
            is TasksDetailAction.DeleteTask -> tasksDetailViewModel.deleteTask(taskId = tasksDetailAction.taskId)
            is TasksDetailAction.EditTaskDescription -> tasksDetailViewModel.editTaskDescription(
                taskId = tasksDetailAction.taskId,
                descriptionChange = tasksDetailAction.descriptionChange
            )
            is TasksDetailAction.ExpandTask -> tasksDetailViewModel.expandTask(taskId = tasksDetailAction.taskId)
            is TasksDetailAction.MarkTaskComplete -> tasksDetailViewModel.markTaskComplete(taskId = tasksDetailAction.taskId)
            is TasksDetailAction.MarkTaskIncomplete -> tasksDetailViewModel.markTaskIncomplete(taskId = tasksDetailAction.taskId)
            is TasksDetailAction.MinimizeTask -> tasksDetailViewModel.minimizeTask(taskId = tasksDetailAction.taskId)
            is TasksDetailAction.NavigateToTasksMenu -> navigationViewModel.navigateToTasksMenu()
            is TasksDetailAction.OnDrag -> tasksDetailViewModel.onDrag(
                task = tasksDetailAction.task,
                dragAmount = tasksDetailAction.dragAmount,
                dragOffsetTotal = tasksDetailAction.dragOffsetTotal,
                lazyListState = tasksDetailAction.lazyListState,
                requestedLayerChange = tasksDetailAction.requestedLayerChange
            )
            is TasksDetailAction.OnDragEnd -> tasksDetailViewModel.onDragEnd()
            is TasksDetailAction.OnDragStart -> tasksDetailViewModel.onDragStart(
                task = tasksDetailAction.task,
                size = tasksDetailAction.size
            )
            is TasksDetailAction.ResetClearFocusTrigger -> tasksDetailViewModel.resetClearFocusTrigger()
            is TasksDetailAction.ResetDragHandlers -> tasksDetailViewModel.resetDragHandlers()
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
                                draggedTask = (state as TasksDetailState.Content).draggedTask,
                                draggedTaskSize = (state as TasksDetailState.Content).draggedTaskSize,
                                dragMode = (state as TasksDetailState.Content).dragMode,
                                dragTargetIndex = (state as TasksDetailState.Content).dragTargetIndex,
                                dragYDirection = (state as TasksDetailState.Content).dragYDirection,
                                dragRequestedLayerChange = (state as TasksDetailState.Content).dragRequestedLayerChange,
                                dragMaxExceeded = (state as TasksDetailState.Content).dragMaxExceeded,
                                dragLeftPossible = (state as TasksDetailState.Content).dragLeftPossible,
                                dragRightPossible = (state as TasksDetailState.Content).dragRightPossible,
                                actionHandler = { handleAction(it) },
                            )
                            is TasksDetailState.Error -> TasksError(
                                string = (state as TasksDetailState.Error).string,
                            )
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
        draggedTask: Task?,
        draggedTaskSize: Int?,
        dragMode: DragMode?,
        dragTargetIndex: Int?,
        dragYDirection: YDirection?,
        dragRequestedLayerChange: Int?,
        dragMaxExceeded: Boolean,
        dragLeftPossible: Boolean,
        dragRightPossible: Boolean,
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

                // TODO scroll
                /*val scrollChannel: Channel<Float> = Channel(
                    capacity = 10,
                    onBufferOverflow = BufferOverflow.DROP_OLDEST,
                    onUndeliveredElement = { println("scrollChannel onUndeliveredElement") }
                )
                LaunchedEffect(lazyListState) {
                    while (true) {
                        val value = scrollChannel.receive()
                        lazyListState.scrollBy(value)
                    }
                }*/

                LazyColumn(
                    contentPadding = PaddingValues(
                        start = LAZY_COLUMN_START_PADDING.dp,
                        top = LAZY_COLUMN_TOP_PADDING.dp,
                        end = LAZY_COLUMN_END_PADDING.dp,
                        bottom = LAZY_COLUMN_BOTTOM_PADDING.dp
                    ),
                    verticalArrangement = Arrangement.spacedBy(LAZY_COLUMN_VERTICAL_ARRANGEMENT_SPACING.dp),
                    state = lazyListState,
                    modifier = Modifier
                        .fillMaxWidth()
                        .imePadding()
                ) {
                    // TODO look more into key???
                    items(taskList) { task ->
                        TaskWithSubtasks(
                            task = task,
                            selectedTasksDetailExtensionMode = selectedTasksDetailExtensionMode,
                            focusTaskId = focusTaskId,
                            isAutoSortCheckedTasks = isAutoSortCheckedTasks,
                            lazyListState = lazyListState,
                            draggedLazyListIndex = draggedTask?.lazyListIndex,
                            isDraggedTask = task == draggedTask,
                            draggedTaskSize = draggedTaskSize,
                            dragMode = dragMode,
                            dragTargetIndex = dragTargetIndex,
                            dragYDirection = dragYDirection,
                            dragRequestedLayerChange = dragRequestedLayerChange,
                            dragMaxExceeded = dragMaxExceeded,
                            dragLeftPossible = dragLeftPossible,
                            dragRightPossible = dragRightPossible,
                            onScroll = { /* TODO scrollChannel.trySend(it)*/ },
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
                    .padding(all = FLOATING_ACTION_BUTTON_PADDING.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.Add,
                    contentDescription = "Add Task"
                )
            }
        }
    }

    @Composable
    fun TasksError(string: String) {
        Text("TasksDetail Error")
        Log.e("TasksDetail", string)
    }

    @Composable
    fun TasksLoading() {
        // TODO add top bar while loading
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