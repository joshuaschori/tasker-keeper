package com.joshuaschori.taskerkeeper

sealed interface TabState {
    data class Detail(val detailId: Int): TabState
    data object Menu: TabState
}