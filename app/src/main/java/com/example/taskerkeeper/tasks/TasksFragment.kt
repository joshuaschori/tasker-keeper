package com.example.taskerkeeper.tasks

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DragHandle
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LargeFloatingActionButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.unit.dp
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.taskerkeeper.Subtask
import com.example.taskerkeeper.Task
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
                                onAddNewTask = { viewModel.addNewTask() },
                                onCheckTask = { viewModel.markTaskComplete(it) },
                                onUncheckTask = { viewModel.markTaskIncomplete(it) },
                                onEditTask = { taskIndex: Int, textChange: String ->
                                    viewModel.editTask(taskIndex, textChange) },
                                onExpandTask = { viewModel.expandTask(it) },
                                onMinimizeTask = { viewModel.minimizeTask(it) },
                                onAddNewSubtask = { viewModel.addNewSubtask(it) },
                                onCheckSubtask = { taskIndex: Int, subtaskIndex: Int ->
                                    viewModel.markSubtaskComplete(taskIndex, subtaskIndex) },
                                onUncheckSubtask = { subtaskIndex: Int, taskIndex: Int ->
                                    viewModel.markSubtaskIncomplete(taskIndex, subtaskIndex) },
                                onEditSubtask = { taskIndex: Int, subtaskIndex: Int, textChange: String ->
                                    viewModel.editSubtask(taskIndex, subtaskIndex, textChange)},
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
        onAddNewTask: () -> Unit,
        onCheckTask: (Int) -> Unit,
        onUncheckTask: (Int) -> Unit,
        onEditTask: (Int, String) -> Unit,
        onExpandTask: (Int) -> Unit,
        onMinimizeTask: (Int) -> Unit,
        onAddNewSubtask: (Int) -> Unit,
        onCheckSubtask: (Int, Int) -> Unit,
        onUncheckSubtask: (Int, Int) -> Unit,
        onEditSubtask: (Int, Int, String) -> Unit,
    ) {
        Column(
            modifier = Modifier
                .fillMaxHeight()
                .fillMaxWidth()
        ) {
            if (taskList.isNotEmpty()) {
                LazyColumn(
                    contentPadding = PaddingValues(
                        start = 16.dp,
                        top = 72.dp,
                        end = 16.dp,
                        bottom = 16.dp
                    ),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    items(count = taskList.size) { taskIndex ->
                        Column {
                            Surface(
                                tonalElevation = 5.dp,
                                shadowElevation = 5.dp
                            ) {
                                TaskRow(
                                    task = taskList[taskIndex],
                                    taskIndex = taskIndex,
                                    onAddNewTask = onAddNewTask,
                                    onCheckTask = onCheckTask,
                                    onUncheckTask = onUncheckTask,
                                    onEditTask = onEditTask,
                                    onExpandTask = onExpandTask,
                                    onMinimizeTask = onMinimizeTask
                                )
                            }
                            if (taskList[taskIndex].isExpanded) {
                                Surface(
                                    modifier = Modifier
                                        .padding(start = 48.dp, top = 1.dp),
                                    shadowElevation = 5.dp,
                                ) {
                                    SubtaskColumn(
                                        taskIndex = taskIndex,
                                        subtaskList = taskList[taskIndex].subtaskList,
                                        onAddNewSubtask = onAddNewSubtask,
                                        onCheckSubtask = onCheckSubtask,
                                        onUncheckSubtask = onUncheckSubtask,
                                        onEditSubtask = onEditSubtask
                                    )
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
            LargeFloatingActionButton(
                onClick = {
                    onAddNewTask()
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

@Composable
fun TaskRow(
    task: Task,
    taskIndex: Int,
    onAddNewTask: () -> Unit,
    onCheckTask: (Int) -> Unit,
    onUncheckTask: (Int) -> Unit,
    onEditTask: (Int, String) -> Unit,
    onExpandTask: (Int) -> Unit,
    onMinimizeTask: (Int) -> Unit,
) {
    Row(
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier
            .fillMaxWidth(),
    ) {
        Row {
            Checkbox(
                checked = task.isChecked,
                onCheckedChange = { onCheckTask(taskIndex) },
            )
            BasicTextField(
                value = task.taskString,
                onValueChange = { onEditTask(taskIndex, it) },
                modifier = Modifier
                    .align(Alignment.CenterVertically)
                    .fillMaxWidth(0.9f),
            )
        }
        IconButton(
            onClick = {
                if (task.isExpanded) {
                    onMinimizeTask(taskIndex)
                } else {
                    onExpandTask(taskIndex)
                }
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
    onAddNewSubtask: (Int) -> Unit,
    onCheckSubtask: (Int, Int) -> Unit,
    onUncheckSubtask: (Int, Int) -> Unit,
    onEditSubtask: (Int, Int, String) -> Unit,
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
                                    onCheckSubtask(taskIndex, subtaskIndex)
                                },
                            )
                            BasicTextField(
                                value = subtask.subtaskString,
                                onValueChange = {
                                    onEditSubtask(taskIndex, subtaskIndex, it)
                                },
                                modifier = Modifier
                                    .align(Alignment.CenterVertically),
                            )
                        }
                        IconButton(
                            onClick = {},
                        ) {
                            Icon(
                                imageVector = Icons.Filled.DragHandle,
                                contentDescription = "Move Subtask"
                            )
                        }
                    }
                }
            }
        }
        IconButton(
            onClick = { onAddNewSubtask(taskIndex) },
        ) {
            Icon(
                imageVector = Icons.Filled.Add,
                contentDescription = "Add Subtask"
            )
        }
    }
}