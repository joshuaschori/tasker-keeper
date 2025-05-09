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
import androidx.compose.runtime.remember
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
import com.joshuaschori.taskerkeeper.Constants.DRAG_MODE_SENSITIVITY
import com.joshuaschori.taskerkeeper.Constants.FLOATING_ACTION_BUTTON_PADDING
import com.joshuaschori.taskerkeeper.Constants.LAZY_COLUMN_BOTTOM_PADDING
import com.joshuaschori.taskerkeeper.Constants.LAZY_COLUMN_END_PADDING
import com.joshuaschori.taskerkeeper.Constants.LAZY_COLUMN_START_PADDING
import com.joshuaschori.taskerkeeper.Constants.LAZY_COLUMN_TOP_PADDING
import com.joshuaschori.taskerkeeper.Constants.LAZY_COLUMN_VERTICAL_ARRANGEMENT_SPACING
import com.joshuaschori.taskerkeeper.Constants.MAX_TIER_FOR_SUBTASKS
import com.joshuaschori.taskerkeeper.Constants.TIER_STEP_SIZE
import com.joshuaschori.taskerkeeper.NavigationViewModel
import com.joshuaschori.taskerkeeper.tasks.tasksDetail.ui.TaskWithSubtasks
import com.joshuaschori.taskerkeeper.tasks.tasksDetail.ui.TasksDetailTopBar
import com.joshuaschori.taskerkeeper.ui.theme.TaskerKeeperTheme
import com.joshuaschori.tiered.dragon.drop.DragHandler
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
                onDragModeChangeTriggerDatabase = tasksDetailAction.onDragModeChangeTriggerDatabase,
                dragState = tasksDetailAction.dragState
            )
            is TasksDetailAction.OnDragEnd -> tasksDetailViewModel.onDragEnd(dragState = tasksDetailAction.dragState)
            is TasksDetailAction.OnDragCancel -> tasksDetailViewModel.onDragCancel()
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
                val dragHandler = remember {
                    DragHandler(
                        dragModeSensitivity = DRAG_MODE_SENSITIVITY,
                        maxTierForSubItems = MAX_TIER_FOR_SUBTASKS,
                        tierStepSize = TIER_STEP_SIZE.dp,
                    )
                }

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
                            taskList = taskList,
                            selectedTasksDetailExtensionMode = selectedTasksDetailExtensionMode,
                            focusTaskId = focusTaskId,
                            isAutoSortCheckedTasks = isAutoSortCheckedTasks,
                            lazyListState = lazyListState,
                            onScroll = { /* TODO scrollChannel.trySend(it)*/ },
                            dragHandler = dragHandler,
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