package com.example.taskerkeeper.tasks

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.taskerkeeper.ui.theme.TaskerKeeperTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class TasksFragment: Fragment() {
    val viewModel: TasksViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel.listenForDatabaseUpdates()
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
                                isAutoSortCheckedTasks = (state as TaskState.Content).isAutoSortCheckedTasks,
                                onAddNewTask = { taskId: Int?, parentId: Int? ->
                                    viewModel.addNewTask(taskId, parentId) },
                                onCheckTask = { viewModel.markTaskComplete(it) },
                                onUncheckTask = { viewModel.markTaskIncomplete(it) },
                                onEditTask = { taskId: Int, textChange: String ->
                                    viewModel.editTask(taskId, textChange) },
                                onExpandTask = { viewModel.expandTask(it) },
                                onMinimizeTask = { viewModel.minimizeTask(it) },
                                onDeleteTask = { viewModel.deleteTask(it) },
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
        isAutoSortCheckedTasks: Boolean,
        onAddNewTask: (Int?, Int?) -> Unit,
        onCheckTask: (Int) -> Unit,
        onUncheckTask: (Int) -> Unit,
        onEditTask: (Int, String) -> Unit,
        onExpandTask: (Int) -> Unit,
        onMinimizeTask: (Int) -> Unit,
        onDeleteTask: (Int) -> Unit,
    ) {
        val focusManager = LocalFocusManager.current
        var hideKeyboard by remember { mutableStateOf(false) }
        var addModeEnabled by remember { mutableStateOf(false) }
        var rearrangeModeEnabled by remember { mutableStateOf(false) }
        var deleteModeEnabled by remember { mutableStateOf(false) }

        Column(
            modifier = Modifier
                .fillMaxHeight()
                .fillMaxWidth()
                .clickable(
                    interactionSource = null,
                    indication = null,
                ) {
                    hideKeyboard = true
                }
        ) {
            TasksTopBar(
                onChangeSegmentedButtonMode = {
                    when (it) {
                        "Normal" -> {
                            addModeEnabled = false
                            rearrangeModeEnabled = false
                            deleteModeEnabled = false
                        }
                        "Add" -> {
                            addModeEnabled = true
                            rearrangeModeEnabled = false
                            deleteModeEnabled = false
                        }
                        "Rearrange" -> {
                            addModeEnabled = false
                            rearrangeModeEnabled = true
                            deleteModeEnabled = false
                        }
                        "Delete" -> {
                            addModeEnabled = false
                            rearrangeModeEnabled = false
                            deleteModeEnabled = true
                        }
                    }
                },
                onClickHideKeyboard = { hideKeyboard = true }
            )
            if (taskList.isNotEmpty()) {
                LazyColumn(
                    contentPadding = PaddingValues(
                        start = 16.dp,
                        top = 32.dp,
                        end = 16.dp,
                        bottom = 16.dp
                    ),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    items(count = taskList.size) { taskIndex ->
                        Column {
                            Row {
                                Surface(
                                    tonalElevation = 5.dp,
                                    shadowElevation = 5.dp,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    TaskRow(
                                        task = taskList[taskIndex],
                                        onCheckTask = onCheckTask,
                                        onUncheckTask = onUncheckTask,
                                        onEditTask = onEditTask,
                                        onExpandTask = onExpandTask,
                                        onMinimizeTask = onMinimizeTask,
                                        onClickHideKeyboard = { hideKeyboard = true },
                                    )
                                }
                                TaskExtensions(
                                    taskId = taskList[taskIndex].taskId,
                                    isAddModeEnabled = addModeEnabled,
                                    isAddModeHidden = (isAutoSortCheckedTasks && taskList[taskIndex].isChecked),
                                    isDeleteModeEnabled = deleteModeEnabled,
                                    isRearrangeModeEnabled = rearrangeModeEnabled,
                                    onDeleteTask = onDeleteTask,
                                    onAddNewTask = onAddNewTask,
                                    onClickHideKeyboard = { hideKeyboard = true }
                                )
                            }
                            if (taskList[taskIndex].isExpanded) {
                                Row {
                                    Surface(
                                        shadowElevation = 5.dp,
                                        modifier = Modifier
                                            .padding(start = 48.dp, top = 1.dp)
                                            .weight(1f),
                                    ) {
                                        SubtaskColumn(
                                            parentId = taskList[taskIndex].taskId,
                                            subtaskList = taskList[taskIndex].subtaskList,
                                            onAddNewTask = onAddNewTask,
                                            onCheckTask = onCheckTask,
                                            onUncheckTask = onUncheckTask,
                                            onEditTask = onEditTask,
                                            onClickHideKeyboard = { hideKeyboard = true },
                                        )
                                    }
                                    Column {
                                        if (taskList[taskIndex].subtaskList.isEmpty()) {
                                            // invisible icon buttons to reserve space
                                            TaskExtensions(
                                                taskId = taskList[taskIndex].taskId,
                                                isAddModeEnabled = addModeEnabled,
                                                isDeleteModeEnabled = deleteModeEnabled,
                                                isHidden = true,
                                                isRearrangeModeEnabled = rearrangeModeEnabled,
                                                onDeleteTask = onDeleteTask,
                                                onAddNewTask = onAddNewTask,
                                                onClickHideKeyboard = { hideKeyboard = true }
                                            )
                                        }
                                        for (subtask in taskList[taskIndex].subtaskList) {
                                            TaskExtensions(
                                                taskId = subtask.taskId,
                                                isAddModeEnabled = addModeEnabled,
                                                isAddModeHidden = (isAutoSortCheckedTasks && subtask.isChecked),
                                                isDeleteModeEnabled = deleteModeEnabled,
                                                isRearrangeModeEnabled = rearrangeModeEnabled,
                                                onDeleteTask = onDeleteTask,
                                                onAddNewTask = onAddNewTask,
                                                onClickHideKeyboard = { hideKeyboard = true }
                                            )
                                        }
                                    }
                                }
                            }
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
                    onAddNewTask(null, null)
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
    onChangeSegmentedButtonMode: (String) -> Unit,
    onClickHideKeyboard: () -> Unit
) {
    var selectedIndex by remember { mutableIntStateOf(0) }
    val options = listOf("Normal", "Add", "Rearrange", "Delete")

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
        navigationIcon = {
            IconButton(
                onClick = {
                    onClickHideKeyboard()
                }
            ) {
                Icon(
                    imageVector = Icons.Filled.ArrowBack,
                    contentDescription = "Localized description"
                )
            }
        },
        actions = {
            SingleChoiceSegmentedButtonRow(
                modifier = Modifier.fillMaxWidth(0.4f)
            ) {
                options.forEachIndexed { index, label ->
                    SegmentedButton(
                        selected = index == selectedIndex,
                        onClick = {
                            selectedIndex = index
                            onChangeSegmentedButtonMode(label)
                            onClickHideKeyboard()
                        },
                        shape = SegmentedButtonDefaults.itemShape(
                            index = index,
                            count = options.size
                        ),
                        icon = {},
                        label = {
                            when (label) {
                                "Normal" -> Icon(
                                    imageVector = Icons.Filled.NotInterested,
                                    contentDescription = "Normal Mode",
                                )
                                "Add" -> Icon(
                                    imageVector = Icons.Filled.Add,
                                    contentDescription = "Add Mode",
                                )
                                "Rearrange" -> Icon(
                                    imageVector = Icons.Filled.DragHandle,
                                    contentDescription = "Rearrange Mode",
                                )
                                "Delete" -> Icon(
                                    imageVector = Icons.Filled.DeleteForever,
                                    contentDescription = "Delete Mode",
                                )
                            }
                        }
                    )
                }
            }
            IconButton(
                onClick = {
                    onClickHideKeyboard()
                }
            ) {
                Icon(
                    imageVector = Icons.Filled.MoreVert,
                    contentDescription = "Localized description"
                )
            }
            //TODO "Done" button when keyboard is up, that clears focus and hides keyboard
        },
    )
}

@Composable
fun TaskRow(
    task: Task,
    onCheckTask: (Int) -> Unit,
    onUncheckTask: (Int) -> Unit,
    onEditTask: (Int, String) -> Unit,
    onExpandTask: (Int) -> Unit,
    onMinimizeTask: (Int) -> Unit,
    onClickHideKeyboard: () -> Unit
) {
    Row(
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row {
            Checkbox(
                checked = task.isChecked,
                onCheckedChange = {
                    if (task.isChecked) {
                        onUncheckTask(task.taskId)
                    } else {
                        onCheckTask(task.taskId)
                    }
                    onClickHideKeyboard()
                },
            )
            BasicTextField(
                value = task.taskString,
                onValueChange = { onEditTask(task.taskId, it) },
                keyboardOptions = KeyboardOptions(
                    imeAction = ImeAction.Next
                ),
                modifier = Modifier
                    .align(Alignment.CenterVertically)
                    .fillMaxWidth(0.8f),
            )
        }
        IconButton(
            onClick = {
                if (task.isExpanded) {
                    onMinimizeTask(task.taskId)
                } else {
                    onExpandTask(task.taskId)
                }
                onClickHideKeyboard()
            },
        ) {
            Icon(
                imageVector = if (task.isExpanded) {
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

@Composable
fun SubtaskColumn(
    parentId: Int,
    subtaskList: List<Task>,
    onAddNewTask: (Int?, Int?) -> Unit,
    onCheckTask: (Int) -> Unit,
    onUncheckTask: (Int) -> Unit,
    onEditTask: (Int, String) -> Unit,
    onClickHideKeyboard: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        if (subtaskList.isNotEmpty()) {
            Column {
                for (subtask in subtaskList) {
                    Row(
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Row {
                            Checkbox(
                                checked = subtask.isChecked,
                                onCheckedChange = {
                                    if (subtask.isChecked) {
                                        onUncheckTask(subtask.taskId)
                                    } else {
                                        onCheckTask(subtask.taskId)
                                    }
                                    onClickHideKeyboard()
                                },
                            )
                            BasicTextField(
                                value = subtask.taskString,
                                onValueChange = {
                                    onEditTask(subtask.taskId, it)
                                },
                                keyboardOptions = KeyboardOptions(
                                    imeAction = ImeAction.Next
                                ),
                                modifier = Modifier
                                    .align(Alignment.CenterVertically)
                                    .fillMaxWidth(0.8f),
                            )
                        }
                    }
                }
            }
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable {
                    onAddNewTask(null, parentId)
                    onClickHideKeyboard()
                }
                .padding(all = 8.dp),
        ) {
            Icon(
                imageVector = Icons.Filled.Add,
                contentDescription = "Add Subtask"
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Add Subtask",
                modifier = Modifier.align(Alignment.CenterVertically)
            )
        }
    }
}

@Composable
fun TaskExtensions(
    taskId: Int,
    isAddModeEnabled: Boolean,
    isAddModeHidden: Boolean = false,
    isHidden: Boolean = false,
    isDeleteModeEnabled: Boolean,
    isRearrangeModeEnabled: Boolean,
    onAddNewTask: (Int?, Int?) -> Unit,
    onClickHideKeyboard: () -> Unit,
    onDeleteTask: (Int) -> Unit,
) {
    Row {
        if (isAddModeEnabled) {
            IconButton(
                onClick = {
                    onAddNewTask(taskId, null)
                    onClickHideKeyboard()
                },
                enabled = (!isHidden || !isAddModeHidden),
                modifier = Modifier.alpha(if (isHidden || isAddModeHidden) 0f else 1f)
            ) {
                Icon(
                    imageVector = Icons.Filled.Add,
                    contentDescription = "Add",
                )
            }
        }
        if (isRearrangeModeEnabled) {
            IconButton(
                onClick = {
                    onClickHideKeyboard()
                },
                enabled = !isHidden,
                modifier = Modifier.alpha(if (isHidden) 0f else 1f)
            ) {
                Icon(
                    imageVector = Icons.Filled.DragHandle,
                    contentDescription = "Rearrange",
                )
            }
        }
        if (isDeleteModeEnabled) {
            IconButton(
                onClick = {
                    onDeleteTask(taskId)
                    onClickHideKeyboard()
                },
                enabled = !isHidden,
                modifier = Modifier.alpha(if (isHidden) 0f else 1f)
            ) {
                Icon(
                    imageVector = Icons.Filled.DeleteForever,
                    contentDescription = "Delete",
                )
            }
        }
    }
}