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
import androidx.compose.material3.LargeFloatingActionButton
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

class TasksFragment: Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val viewModel: TasksViewModel by viewModels()
        return ComposeView(requireContext()).apply {
            // Dispose of the Composition when the view's LifecycleOwner is destroyed
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                val state by viewModel.uiState.collectAsStateWithLifecycle(
                    initialValue = TaskState.Content()
                )
                TaskerKeeperTheme {
                    Surface {
                        when (state) {
                            is TaskState.Content -> TasksContent(
                                taskList = (state as TaskState.Content).taskList,
                                onAddNewTask = { viewModel.addNewTask(it) },
                                onCheckTask = { viewModel.markTaskComplete(it) },
                                onUncheckTask = { viewModel.markTaskIncomplete(it) },
                                onEditTask = { taskIndex: Int, textChange: String ->
                                    viewModel.editTask(taskIndex, textChange) },
                                onExpandTask = { viewModel.expandTask(it) },
                                onMinimizeTask = { viewModel.minimizeTask(it) },
                                onAddNewSubtask = { taskIndex: Int, subtaskIndex: Int? ->
                                    viewModel.addNewSubtask(taskIndex, subtaskIndex) },
                                onCheckSubtask = { taskIndex: Int, subtaskIndex: Int ->
                                    viewModel.markSubtaskComplete(taskIndex, subtaskIndex) },
                                onUncheckSubtask = { taskIndex: Int, subtaskIndex: Int ->
                                    viewModel.markSubtaskIncomplete(taskIndex, subtaskIndex) },
                                onEditSubtask = { taskIndex: Int, subtaskIndex: Int, textChange: String ->
                                    viewModel.editSubtask(taskIndex, subtaskIndex, textChange)},
                                onDeleteTask = { viewModel.deleteTask(it) },
                                onDeleteSubtask = { taskIndex: Int, subtaskIndex: Int ->
                                    viewModel.deleteSubtask(taskIndex, subtaskIndex) },
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
        onAddNewTask: (Int?) -> Unit,
        onCheckTask: (Int) -> Unit,
        onUncheckTask: (Int) -> Unit,
        onEditTask: (Int, String) -> Unit,
        onExpandTask: (Int) -> Unit,
        onMinimizeTask: (Int) -> Unit,
        onAddNewSubtask: (Int, Int?) -> Unit,
        onCheckSubtask: (Int, Int) -> Unit,
        onUncheckSubtask: (Int, Int) -> Unit,
        onEditSubtask: (Int, Int, String) -> Unit,
        onDeleteTask: (Int) -> Unit,
        onDeleteSubtask: (Int, Int) -> Unit,
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
                                        taskIndex = taskIndex,
                                        onCheckTask = onCheckTask,
                                        onUncheckTask = onUncheckTask,
                                        onEditTask = onEditTask,
                                        onExpandTask = onExpandTask,
                                        onMinimizeTask = onMinimizeTask,
                                        onClickHideKeyboard = { hideKeyboard = true },
                                    )
                                }
                                TaskAndSubtaskExtensions(
                                    taskIndex = taskIndex,
                                    addModeEnabled = addModeEnabled,
                                    rearrangeModeEnabled = rearrangeModeEnabled,
                                    deleteModeEnabled = deleteModeEnabled,
                                    onDeleteTask = onDeleteTask,
                                    onDeleteSubtask = onDeleteSubtask,
                                    onAddNewTask = onAddNewTask,
                                    onAddNewSubtask = onAddNewSubtask,
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
                                            taskIndex = taskIndex,
                                            subtaskList = taskList[taskIndex].subtaskList,
                                            onAddNewSubtask = onAddNewSubtask,
                                            onCheckSubtask = onCheckSubtask,
                                            onUncheckSubtask = onUncheckSubtask,
                                            onEditSubtask = onEditSubtask,
                                            onClickHideKeyboard = { hideKeyboard = true },
                                        )
                                    }
                                    Column {
                                        if (taskList[taskIndex].subtaskList.isEmpty()) {
                                            // invisible icon buttons to reserve space
                                            TaskAndSubtaskExtensions(
                                                taskIndex = taskIndex,
                                                addModeEnabled = addModeEnabled,
                                                rearrangeModeEnabled = rearrangeModeEnabled,
                                                deleteModeEnabled = deleteModeEnabled,
                                                isHidden = true,
                                                onDeleteTask = onDeleteTask,
                                                onDeleteSubtask = onDeleteSubtask,
                                                onAddNewTask = onAddNewTask,
                                                onAddNewSubtask = onAddNewSubtask,
                                                onClickHideKeyboard = { hideKeyboard = true }
                                            )
                                        }
                                        for ((subtaskIndex) in taskList[taskIndex].subtaskList.withIndex()) {
                                            TaskAndSubtaskExtensions(
                                                taskIndex = taskIndex,
                                                subtaskIndex = subtaskIndex,
                                                addModeEnabled = addModeEnabled,
                                                rearrangeModeEnabled = rearrangeModeEnabled,
                                                deleteModeEnabled = deleteModeEnabled,
                                                onDeleteTask = onDeleteTask,
                                                onDeleteSubtask = onDeleteSubtask,
                                                onAddNewTask = onAddNewTask,
                                                onAddNewSubtask = onAddNewSubtask,
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
                    onAddNewTask(null)
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
    taskIndex: Int,
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
                        onUncheckTask(taskIndex)
                    } else {
                        onCheckTask(taskIndex)
                    }
                    onClickHideKeyboard()
                },
            )
            BasicTextField(
                value = task.taskString,
                onValueChange = { onEditTask(taskIndex, it) },
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
                    onMinimizeTask(taskIndex)
                } else {
                    onExpandTask(taskIndex)
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
    taskIndex: Int,
    subtaskList: List<Subtask>,
    onAddNewSubtask: (Int, Int?) -> Unit,
    onCheckSubtask: (Int, Int) -> Unit,
    onUncheckSubtask: (Int, Int) -> Unit,
    onEditSubtask: (Int, Int, String) -> Unit,
    onClickHideKeyboard: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        if (subtaskList.isNotEmpty()) {
            Column {
                for ((subtaskIndex, subtask) in subtaskList.withIndex()) {
                    Row(
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Row {
                            Checkbox(
                                checked = subtask.isChecked,
                                onCheckedChange = {
                                    if (subtask.isChecked) {
                                        onUncheckSubtask(taskIndex, subtaskIndex)
                                    } else {
                                        onCheckSubtask(taskIndex, subtaskIndex)
                                    }
                                    onClickHideKeyboard()
                                },
                            )
                            BasicTextField(
                                value = subtask.subtaskString,
                                onValueChange = {
                                    onEditSubtask(taskIndex, subtaskIndex, it)
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
                    onAddNewSubtask(taskIndex, null)
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
fun TaskAndSubtaskExtensions(
    taskIndex: Int,
    subtaskIndex: Int? = null,
    addModeEnabled: Boolean,
    rearrangeModeEnabled: Boolean,
    deleteModeEnabled: Boolean,
    isHidden: Boolean = false,
    onAddNewSubtask: (Int, Int) -> Unit,
    onAddNewTask: (Int) -> Unit,
    onClickHideKeyboard: () -> Unit,
    onDeleteSubtask: (Int, Int) -> Unit,
    onDeleteTask: (Int) -> Unit,
) {
    Row {
        if (addModeEnabled) {
            IconButton(
                onClick = {
                    if (subtaskIndex == null) {
                        onAddNewTask(taskIndex)
                    } else {
                        onAddNewSubtask(taskIndex, subtaskIndex)
                    }
                    onClickHideKeyboard()
                },
                enabled = !isHidden,
                modifier = Modifier.alpha(if (isHidden) 0f else 1f)
            ) {
                Icon(
                    imageVector = Icons.Filled.Add,
                    contentDescription = "Add",
                )
            }
        }
        if (rearrangeModeEnabled) {
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
        if (deleteModeEnabled) {
            IconButton(
                onClick = {
                    if (subtaskIndex == null) {
                        onDeleteTask(taskIndex)
                    } else {
                        onDeleteSubtask(taskIndex, subtaskIndex)
                    }
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