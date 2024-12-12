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
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DragHandle
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LargeFloatingActionButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.taskerkeeper.MainActivity
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
                                onCheck = { viewModel.markTaskComplete(it) },
                                onUncheck = { viewModel.markTaskIncomplete(it) },
                                onEditTask = { taskIndex: Int, textChange: String -> viewModel.editTask(taskIndex, textChange) },
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
        onCheck: (Int) -> Unit,
        onUncheck: (Int) -> Unit,
        onEditTask: (Int, String) -> Unit,
    ) {
        Column(
            modifier = Modifier
                .fillMaxHeight()
                .fillMaxWidth()
        ) {
            LazyColumn(
                contentPadding = PaddingValues(start = 16.dp, top = 72.dp, end = 16.dp, bottom = 16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier,
            ) {
                if (taskList.isNotEmpty()) {
                    items(count = taskList.size) {listIndex ->
                        ListItem(
                            headlineContent = {
                                BasicTextField(
                                    value = taskList[listIndex].taskString,
                                    onValueChange = { onEditTask(listIndex, it) },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .wrapContentHeight()
                                )
                            },
                            leadingContent = {
                                Checkbox(
                                    checked = taskList[listIndex].checkedState,
                                    onCheckedChange = { onCheck(listIndex) },
                                )
                            },
                            trailingContent = {
                                Row {
                                    IconButton(
                                        onClick = {}
                                    ) {
                                        Icon(
                                            imageVector = Icons.Filled.ExpandMore,
                                            contentDescription = "Subtasks"
                                        )
                                    }
                                }
                            },
                            tonalElevation = 5.dp,
                            shadowElevation = 5.dp,
                            modifier = Modifier
                                .weight(10f)
                        )
                        
                        /*IconButton(
                            onClick = {},
                            modifier = Modifier
                                .fillMaxWidth()
                                .wrapContentHeight()
                                .weight(1f),
                        ) {
                            Icon(
                                imageVector = Icons.Filled.DragHandle,
                                contentDescription = "Subtasks",
                            )
                        }*/

                        // TODO options for moving, trashing

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