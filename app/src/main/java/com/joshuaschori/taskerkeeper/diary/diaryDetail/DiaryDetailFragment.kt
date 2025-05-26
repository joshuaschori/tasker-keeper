package com.joshuaschori.taskerkeeper.diary.diaryDetail

import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
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
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.joshuaschori.taskerkeeper.NavigationViewModel
import com.joshuaschori.taskerkeeper.diary.diaryDetail.ui.DiaryDetailTopBar
import com.joshuaschori.taskerkeeper.ui.theme.TaskerKeeperTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlin.properties.Delegates

@AndroidEntryPoint
class DiaryDetailFragment: Fragment() {
    private val navigationViewModel: NavigationViewModel by activityViewModels()
    private val diaryDetailViewModel: DiaryDetailViewModel by viewModels()
    private var diaryId: Int by Delegates.notNull()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        diaryId = requireArguments().getInt(DIARY_ID)
        diaryDetailViewModel.getForecast()
        diaryDetailViewModel.listenForDatabaseUpdates(diaryId)
    }

    // TODO not being used unless we're emitting something
    /*override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {n
                diaryDetailViewModel.uiAction.collect {
                    handleAction(it)
                }
            }
        }
    }*/

    private fun handleAction(diaryDetailAction: DiaryDetailAction) {
        when (diaryDetailAction) {
            is DiaryDetailAction.ClearFocus -> diaryDetailViewModel.clearFocus()
            is DiaryDetailAction.EditDiaryText -> diaryDetailViewModel.editDiaryText(diaryDetailAction.diaryId, diaryDetailAction.textChange)
            is DiaryDetailAction.NavigateToDiaryMenu -> navigationViewModel.navigateToDiaryMenu()
            is DiaryDetailAction.ResetClearFocusTrigger -> diaryDetailViewModel.resetClearFocusTrigger()
        }
    }

    // TODO
  /*  @RequiresApi(Build.VERSION_CODES.N)
    fun requestPermissions() {
        val locationPermissionRequest = registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) { permissions ->
            when {
                permissions.getOrDefault(Manifest.permission.ACCESS_COARSE_LOCATION, false) -> {
                    // Only approximate location access granted.
                }
                else -> {
                    // No location access granted.
                }
            }
        }

        // Before you perform the actual permission request, check whether your app
        // already has the permissions, and whether your app needs to show a permission
        // rationale dialog. For more details, see Request permissions:
        // https://developer.android.com/training/permissions/requesting#request-permission
        locationPermissionRequest.launch(
            arrayOf(
                Manifest.permission.ACCESS_COARSE_LOCATION
            )
        )
    }*/

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            // Dispose of the Composition when the view's LifecycleOwner is destroyed
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                val state by diaryDetailViewModel.uiState.collectAsStateWithLifecycle()
                TaskerKeeperTheme {
                    Surface {
                        when (state) {
                            is DiaryDetailState.Content -> DiaryDetailContent(
                                diaryDate = (state as DiaryDetailState.Content).diaryDate,
                                diaryText = (state as DiaryDetailState.Content).diaryText,
                                clearFocusTrigger = (state as DiaryDetailState.Content).clearFocusTrigger,
                                actionHandler = { handleAction(it) }
                            )
                            is DiaryDetailState.Error -> DiaryDetailError()
                            is DiaryDetailState.Loading -> DiaryDetailLoading()
                        }
                    }
                }
            }
        }
    }

    @Composable
    fun DiaryDetailContent(
        diaryDate: String,
        diaryText: String,
        clearFocusTrigger: Boolean,
        actionHandler: DiaryDetailActionHandler
    ) {
        val focusManager = LocalFocusManager.current
        if (clearFocusTrigger) {
            focusManager.clearFocus()
            actionHandler(DiaryDetailAction.ResetClearFocusTrigger)
        }

        Column(
            modifier = Modifier
                .fillMaxHeight()
                .fillMaxWidth()
                .clickable(interactionSource = null, indication = null) {
                    actionHandler(DiaryDetailAction.ClearFocus)
                }
        ) {
            DiaryDetailTopBar(
                diaryDate = diaryDate,
                actionHandler = actionHandler,
            )

            // while text field is being interacted with, update UI immediately and not from database
            val activeTextField = remember { mutableStateOf("") }
            val interactionSource = remember { MutableInteractionSource() }
            val isFocused by interactionSource.collectIsFocusedAsState()
            if (!isFocused) {
                activeTextField.value = diaryText
            }

            Row {
                BasicTextField(
                    value = if (isFocused) {
                        activeTextField.value
                    } else {
                        diaryText
                    },
                    onValueChange = {
                        activeTextField.value = it
                        actionHandler(DiaryDetailAction.EditDiaryText(diaryId, it))
                    },
                    keyboardOptions = KeyboardOptions(
                        imeAction = ImeAction.Default
                    ),
                    interactionSource = interactionSource,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(start = 16.dp, top = 32.dp, end = 16.dp, bottom = 320.dp)
                        .weight(1f)
                )
                // TODO
                /*when () {

                }*/
            }
        }
    }

    @Composable
    fun DiaryDetailError() {
        Text("DiaryDetailFragment Error")
    }

    @Composable
    fun DiaryDetailLoading() {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
    }

    @Composable
    fun ForecastApiContent() {

    }

    @Composable
    fun ForecastApiError() {
        Text("ForecastApiError in DiaryDetailFragment")
    }

    @Composable
    fun ForecastApiLoading() {
        CircularProgressIndicator()
    }

    companion object {
        private const val DIARY_ID = "diaryId"
        fun newInstance(
            diaryId: Int
        ): DiaryDetailFragment {
            val fragment = DiaryDetailFragment()
            val bundle = bundleOf(
                DIARY_ID to diaryId
            )
            fragment.arguments = bundle
            return fragment
        }
    }
}