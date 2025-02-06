package com.joshuaschori.taskerkeeper.tasks

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

data class Task(
    val taskId: Int,
    val taskString: String,
    val isChecked: Boolean,
    val isExpanded: Boolean,
    val parentId: Int?,
    val subtaskList: List<Task>,
)