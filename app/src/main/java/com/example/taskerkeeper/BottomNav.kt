package com.example.taskerkeeper

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoStories
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Checklist
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.ListAlt
import androidx.compose.material.icons.filled.Loop
import androidx.compose.material.icons.twotone.AutoStories
import androidx.compose.material.icons.twotone.CalendarMonth
import androidx.compose.material.icons.twotone.Favorite
import androidx.compose.material.icons.twotone.ListAlt
import androidx.compose.material.icons.twotone.Loop
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue

@Composable
fun BottomNav(onClick: (Int) -> Unit) {
    //TODO selectedItem lift out of BottomNav?
    var selectedItem by remember { mutableIntStateOf(1) }
    val items = listOf("Habits", "Tasks", "Diary", "Calendar")
    val icons = listOf(Icons.Filled.Loop, Icons.Filled.Checklist, Icons.Filled.AutoStories, Icons.Filled.CalendarMonth)

    NavigationBar {
        items.forEachIndexed { index, item ->
            NavigationBarItem(
                icon = {
                    Icon(
                        imageVector = icons[index],
                        contentDescription = item
                    )
                },
                label = { Text(item) },
                selected = selectedItem == index,
                onClick = {
                    selectedItem = index
                    onClick(selectedItem)
                }
            )
        }
    }
}