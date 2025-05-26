package com.joshuaschori.taskerkeeper.diary.diaryMenu

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.joshuaschori.taskerkeeper.NavigationViewModel
import com.joshuaschori.taskerkeeper.data.diary.DiaryEntity
import com.joshuaschori.taskerkeeper.diary.diaryDetail.DiaryDetailAction
import com.joshuaschori.taskerkeeper.diary.diaryDetail.DiaryDetailState
import com.joshuaschori.taskerkeeper.diary.diaryDetail.DiaryDetailViewModel
import com.joshuaschori.taskerkeeper.diary.diaryDetail.ui.DiaryDetailTopBar
import com.joshuaschori.taskerkeeper.diary.diaryMenu.ui.DiaryMenuTopBar
import com.joshuaschori.taskerkeeper.ui.theme.TaskerKeeperTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class DiaryMenuFragment: Fragment() {
    private val navigationViewModel: NavigationViewModel by activityViewModels()
    private val diaryMenuViewModel: DiaryMenuViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        diaryMenuViewModel.listenForDatabaseUpdates()
    }

    // TODO not being used unless we're emitting something
    /*override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                diaryDetailViewModel.uiAction.collect {
                    handleAction(it)
                }
            }
        }
    }*/

    private fun handleAction(diaryMenuAction: DiaryMenuAction) {
        when (diaryMenuAction) {
            DiaryMenuAction.ClearFocus -> TODO()
            is DiaryMenuAction.NavigateToDiaryDetail -> navigationViewModel.navigateToDiaryDetail(diaryId = diaryMenuAction.diaryId)
            DiaryMenuAction.ResetClearFocusTrigger -> TODO()
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
                val state by diaryMenuViewModel.uiState.collectAsStateWithLifecycle()
                TaskerKeeperTheme {
                    Surface {
                        when (state) {
                            is DiaryMenuState.Content -> DiaryMenuContent(
                                diaryEntityList = (state as DiaryMenuState.Content).diaryEntityList,
                                clearFocusTrigger = (state as DiaryMenuState.Content).clearFocusTrigger,
                                actionHandler = { handleAction(it) }
                            )
                            is DiaryMenuState.Error -> DiaryMenuError()
                            is DiaryMenuState.Loading -> DiaryMenuLoading()
                        }
                    }
                }
            }
        }
    }

    @Composable
    fun DiaryMenuContent(
        diaryEntityList: List<DiaryEntity>,
        clearFocusTrigger: Boolean,
        actionHandler: DiaryMenuActionHandler
    ) {
        val focusManager = LocalFocusManager.current
        if (clearFocusTrigger) {
            focusManager.clearFocus()
            actionHandler(DiaryMenuAction.ResetClearFocusTrigger)
        }

        Column(
            modifier = Modifier
                .fillMaxHeight()
                .fillMaxWidth()
                .clickable(interactionSource = null, indication = null) {
                    actionHandler(DiaryMenuAction.ClearFocus)
                }
        ) {
            DiaryMenuTopBar(
                actionHandler = actionHandler,
            )
            Column {
                for (diary in diaryEntityList) {
                    Text(
                        text = diary.diaryDate,
                        modifier = Modifier.clickable {
                            actionHandler(DiaryMenuAction.NavigateToDiaryDetail(diary.diaryId))
                        }
                    )
                }
            }
        }
    }

    @Composable
    fun DiaryMenuError() {
        Text("DiaryMenuFragment Error")
    }

    @Composable
    fun DiaryMenuLoading() {
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