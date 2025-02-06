package com.joshuaschori.taskerkeeper.tasks

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.DragHandle
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.NotInterested
import androidx.compose.material.icons.filled.SubdirectoryArrowRight
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.joshuaschori.taskerkeeper.ui.theme.TaskerKeeperTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

private const val MAX_LAYERS_OF_SUBTASKS = 4

@AndroidEntryPoint
class TasksFragment: Fragment() {
    private val viewModel: TasksViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel.listenForDatabaseUpdates()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiAction.collect {
                    handleAction(it)
                }
            }
        }
    }

    private fun handleAction(taskAction: TaskAction) {
        when (taskAction) {
            is TaskAction.AddNewTask ->
                viewModel.addNewTask(taskAction.selectedTaskId, taskAction.parentId)
            is TaskAction.DeleteTask -> viewModel.deleteTask(taskAction.taskId)
            is TaskAction.EditTask -> viewModel.editTask(taskAction.taskId, taskAction.textChange)
            is TaskAction.ExpandTask -> viewModel.expandTask(taskAction.taskId)
            is TaskAction.MarkTaskComplete -> viewModel.markTaskComplete(taskAction.taskId)
            is TaskAction.MarkTaskIncomplete -> viewModel.markTaskIncomplete(taskAction.taskId)
            is TaskAction.MinimizeTask -> viewModel.minimizeTask(taskAction.taskId)
            is TaskAction.RequestFocus -> viewModel.requestFocus(taskAction.taskId)
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
                val state by viewModel.uiState.collectAsStateWithLifecycle()
                TaskerKeeperTheme {
                    Surface {
                        when (state) {
                            is TaskState.Content -> TasksContent(
                                taskList = (state as TaskState.Content).taskList,
                                focusTaskId = (state as TaskState.Content).focusTaskId,
                                isAutoSortCheckedTasks = (state as TaskState.Content)
                                    .isAutoSortCheckedTasks,
                                actionHandler = { handleAction(it) },
                                onTaskFocused = { viewModel.clearFocusTaskId() },
                            )
                            is TaskState.Error -> TasksError()
                            is TaskState.Loading -> TasksLoading()
                        }
                    }
                }
            }
        }
    }

    @Composable
    fun TasksContent(
        taskList: List<Task>,
        focusTaskId: Int?,
        isAutoSortCheckedTasks: Boolean,
        actionHandler: TaskActionHandler,
        onTaskFocused: () -> Unit,
    ) {
        val focusManager = LocalFocusManager.current
        var hideKeyboard by remember { mutableStateOf(false) }
        var selectedTaskExtensionMode by remember { mutableStateOf(TaskExtensionMode.NORMAL) }

        Column(
            modifier = Modifier
                .fillMaxHeight()
                .fillMaxWidth()
                .clickable(interactionSource = null, indication = null) { hideKeyboard = true }
        ) {
            TasksTopBar(
                selectedTaskExtensionMode = selectedTaskExtensionMode,
                onChangeTaskExtensionMode = { selectedTaskExtensionMode = it },
                onClickHideKeyboard = { hideKeyboard = true },
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
                    items(count = taskList.size) { taskIndex ->
                        Column(
                            modifier = Modifier.animateItem()
                        ) {
                            TaskAndSubtasks(
                                task = taskList[taskIndex],
                                taskLayer = 0,
                                focusTaskId = focusTaskId,
                                isAutoSortCheckedTasks = isAutoSortCheckedTasks,
                                selectedTaskExtensionMode = selectedTaskExtensionMode,
                                actionHandler = actionHandler,
                                onClickHideKeyboard = { hideKeyboard = true },
                                onTaskFocused = onTaskFocused,
                            )
                        }
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
                    actionHandler(TaskAction.AddNewTask(null, null))
                    hideKeyboard = true
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
        if (hideKeyboard) {
            focusManager.clearFocus()
            hideKeyboard = false
        }
    }

    @Composable
    fun TasksError() {
        Text("Error")
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
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TasksTopBar(
    selectedTaskExtensionMode: TaskExtensionMode,
    onChangeTaskExtensionMode: (TaskExtensionMode) -> Unit,
    onClickHideKeyboard: () -> Unit,
) {
    TopAppBar(
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            titleContentColor = MaterialTheme.colorScheme.primary,
        ),
        title = {
            Text(
                "Tasks",
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        },
        // TODO empty button
        navigationIcon = {
            IconButton(
                onClick = {
                    onClickHideKeyboard()
                }
            ) {
                Icon(
                    imageVector = Icons.Filled.ArrowBack,
                    contentDescription = "Back"
                )
            }
        },
        actions = {
            SingleChoiceSegmentedButtonRow(
                modifier = Modifier.fillMaxWidth(0.5f)
            ) {
                TaskExtensionMode.entries.forEachIndexed { index, label ->
                    SegmentedButton(
                        selected = label == selectedTaskExtensionMode,
                        onClick = {
                            onChangeTaskExtensionMode(label)
                            onClickHideKeyboard()
                        },
                        shape = SegmentedButtonDefaults.itemShape(
                            index = index,
                            count = TaskExtensionMode.entries.size
                        ),
                        icon = {},
                        label = {
                            when (label) {
                                TaskExtensionMode.NORMAL -> Icon(
                                    imageVector = Icons.Filled.NotInterested,
                                    contentDescription = TaskExtensionMode.NORMAL.contentDescription,
                                )
                                TaskExtensionMode.ADD_NEW_TASK -> Icon(
                                    imageVector = Icons.Filled.Add,
                                    contentDescription = TaskExtensionMode.ADD_NEW_TASK.contentDescription,
                                )
                                TaskExtensionMode.ADD_NEW_SUBTASK -> Icon(
                                    imageVector = Icons.Filled.SubdirectoryArrowRight,
                                    contentDescription = TaskExtensionMode.ADD_NEW_SUBTASK.contentDescription,
                                )
                                TaskExtensionMode.REARRANGE -> Icon(
                                    imageVector = Icons.Filled.DragHandle,
                                    contentDescription = TaskExtensionMode.REARRANGE.contentDescription,
                                )
                                TaskExtensionMode.DELETE -> Icon(
                                    imageVector = Icons.Filled.DeleteForever,
                                    contentDescription = TaskExtensionMode.DELETE.contentDescription,
                                )
                            }
                        }
                    )
                }
            }
            // TODO empty button
            IconButton(
                onClick = {
                    onClickHideKeyboard()
                }
            ) {
                Icon(
                    imageVector = Icons.Filled.MoreVert,
                    contentDescription = "More Options"
                )
            }
        },
    )
}

@Composable
fun TaskAndSubtasks(
    task: Task,
    taskLayer: Int,
    focusTaskId: Int?,
    selectedTaskExtensionMode: TaskExtensionMode,
    isAutoSortCheckedTasks: Boolean,
    actionHandler: TaskActionHandler,
    onClickHideKeyboard: () -> Unit,
    onTaskFocused: () -> Unit,
) {
    Row {
        Surface(
            tonalElevation = if (taskLayer == 0) {
                10.dp
            } else {
                0.dp
            },
            shadowElevation = 5.dp,
            modifier = Modifier
                .padding(start = (32 * taskLayer).dp, top = 1.dp)
                .weight(1f)
        ) {
            TaskRow(
                task = task,
                focusTaskId = focusTaskId,
                actionHandler = actionHandler,
                onClickHideKeyboard = onClickHideKeyboard,
                onTaskFocused = onTaskFocused,
            )
        }
        TaskExtensions(
            task = task,
            taskLayer = taskLayer,
            actionHandler = actionHandler,
            selectedTaskExtensionMode = selectedTaskExtensionMode,
            isAutoSortCheckedTasks = isAutoSortCheckedTasks,
            onClickHideKeyboard = onClickHideKeyboard,
        )
    }
    if (task.subtaskList.isNotEmpty() && task.isExpanded && taskLayer < MAX_LAYERS_OF_SUBTASKS) {
        for (subtask in task.subtaskList) {
            TaskAndSubtasks(
                task = subtask,
                taskLayer = taskLayer + 1,
                focusTaskId = focusTaskId,
                selectedTaskExtensionMode = selectedTaskExtensionMode,
                isAutoSortCheckedTasks = isAutoSortCheckedTasks,
                actionHandler = actionHandler,
                onClickHideKeyboard = onClickHideKeyboard,
                onTaskFocused = onTaskFocused,
            )
        }
    }
}

@Composable
fun TaskRow(
    task: Task,
    focusTaskId: Int?,
    actionHandler: TaskActionHandler,
    onClickHideKeyboard: () -> Unit,
    onTaskFocused: () -> Unit,
) {
    // focus when task is first created
    val focusRequester = remember { FocusRequester() }
    LaunchedEffect(Unit) {
        if (focusTaskId == task.taskId) {
            focusRequester.requestFocus()
            onTaskFocused()
        }
    }

    // while text field is being interacted with, update UI immediately and not from database
    val activeTextField = remember { mutableStateOf("") }
    val interactionSource = remember { MutableInteractionSource() }
    val isFocused by interactionSource.collectIsFocusedAsState()
    if (!isFocused) {
        activeTextField.value = task.taskString
    }

    Row(
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row {
            Checkbox(
                checked = task.isChecked,
                onCheckedChange = {
                    if (task.isChecked) {
                        actionHandler(TaskAction.MarkTaskIncomplete(task.taskId))
                    } else {
                        actionHandler(TaskAction.MarkTaskComplete(task.taskId))
                    }
                    onClickHideKeyboard()
                },
            )
            BasicTextField(
                value = if (isFocused) {
                    activeTextField.value
                } else {
                    task.taskString
                },
                onValueChange = {
                    activeTextField.value = it
                    actionHandler(TaskAction.EditTask(task.taskId, it))
                },
                keyboardOptions = KeyboardOptions(
                    imeAction = ImeAction.Done
                ),
                modifier = Modifier
                    .align(Alignment.CenterVertically)
                    .fillMaxWidth(0.75f)
                    .focusRequester(focusRequester),
                interactionSource = interactionSource
            )
        }
        if (task.subtaskList.isNotEmpty()) {
            IconButton(
                onClick = {
                    if (task.isExpanded) {
                        actionHandler(TaskAction.MinimizeTask(task.taskId))
                    } else {
                        actionHandler(TaskAction.ExpandTask(task.taskId))
                    }
                    onClickHideKeyboard()
                },
            ) {
                Icon(
                    if (task.isExpanded) {
                        Icons.Filled.ExpandLess
                    } else {
                        Icons.Filled.ExpandMore
                    },
                    contentDescription = if (task.isExpanded) {
                        "Minimize Subtasks"
                    } else {
                        "Expand Subtasks"
                    },
                )
            }
        }
    }
}

@Composable
fun TaskExtensions(
    task: Task,
    taskLayer: Int,
    selectedTaskExtensionMode: TaskExtensionMode,
    isAutoSortCheckedTasks: Boolean,
    actionHandler: TaskActionHandler,
    onClickHideKeyboard: () -> Unit,
) {
    Row {
        when (selectedTaskExtensionMode) {
            TaskExtensionMode.NORMAL -> {}
            TaskExtensionMode.ADD_NEW_TASK -> {
                IconButton(
                    onClick = {
                        actionHandler(TaskAction.AddNewTask(task.taskId, null))
                        onClickHideKeyboard()
                    },
                    enabled = (
                            !(isAutoSortCheckedTasks && task.isChecked)
                            ),
                    modifier = Modifier.alpha(
                        if (isAutoSortCheckedTasks && task.isChecked) 0f else 1f
                    )
                ) {
                    Icon(
                        imageVector = Icons.Filled.Add,
                        contentDescription = "Add Task After",
                    )
                }
            }
            TaskExtensionMode.ADD_NEW_SUBTASK -> {
                IconButton(
                    onClick = {
                        actionHandler(TaskAction.AddNewTask(null, task.taskId))
                        onClickHideKeyboard()
                    },
                    enabled = (
                        !((isAutoSortCheckedTasks && task.isChecked)
                            || taskLayer >= MAX_LAYERS_OF_SUBTASKS)
                    ),
                    modifier = Modifier.alpha(
                        if ((isAutoSortCheckedTasks && task.isChecked)
                            || taskLayer >= MAX_LAYERS_OF_SUBTASKS) 0f else 1f
                    )
                ) {
                    Icon(
                        imageVector = Icons.Filled.SubdirectoryArrowRight,
                        contentDescription = "Add Subtask",
                    )
                }
            }
            TaskExtensionMode.REARRANGE -> {
                IconButton(
                    onClick = {
                        onClickHideKeyboard()
                    }
                ) {
                    Icon(
                        imageVector = Icons.Filled.DragHandle,
                        contentDescription = "Rearrange",
                    )
                }
            }
            TaskExtensionMode.DELETE -> {
                IconButton(
                    onClick = {
                        actionHandler(TaskAction.DeleteTask(task.taskId))
                        onClickHideKeyboard()
                    }
                ) {
                    Icon(
                        imageVector = Icons.Filled.DeleteForever,
                        contentDescription = "Delete",
                    )
                }
            }
        }
    }
}