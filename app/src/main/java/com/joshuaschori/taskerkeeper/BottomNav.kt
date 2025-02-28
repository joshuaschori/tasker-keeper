package com.joshuaschori.taskerkeeper

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoStories
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Checklist
import androidx.compose.material.icons.filled.Loop
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable

@Composable
fun BottomNav(
    bottomNavState: BottomNavState,
    actionHandler: NavigationActionHandler
) {
    NavigationBar {
        BottomNavState.entries.forEachIndexed { index, label ->
            NavigationBarItem(
                icon = {
                    Icon(
                        imageVector = when (label) {
                            BottomNavState.HABITS -> Icons.Filled.Loop
                            BottomNavState.TASKS -> Icons.Filled.Checklist
                            BottomNavState.DIARY -> Icons.Filled.AutoStories
                            BottomNavState.CALENDAR -> Icons.Filled.CalendarMonth
                        },
                        contentDescription = label.contentDescription
                    )
                },
                label = { Text(label.contentDescription) },
                selected = bottomNavState == label,
                onClick = {
                    actionHandler(NavigationAction.ChangeBottomNavState(label))
                }
            )
        }
    }
}