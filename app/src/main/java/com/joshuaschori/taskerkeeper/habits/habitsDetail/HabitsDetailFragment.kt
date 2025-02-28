package com.joshuaschori.taskerkeeper.habits.habitsDetail

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.joshuaschori.taskerkeeper.NavigationViewModel
import com.joshuaschori.taskerkeeper.diary.diaryDetail.DiaryDetailAction
import com.joshuaschori.taskerkeeper.ui.theme.TaskerKeeperTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlin.properties.Delegates

@AndroidEntryPoint
class HabitsDetailFragment: Fragment() {
    private val navigationViewModel: NavigationViewModel by activityViewModels()
    private val habitsDetailViewModel: HabitsDetailViewModel by viewModels()
    private var parentCategoryId: Int by Delegates.notNull()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        parentCategoryId = requireArguments().getInt(PARENT_CATEGORY_ID)
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

    private fun handleAction(habitsDetailAction: DiaryDetailAction) {
        when (habitsDetailAction) {
            else -> {}
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
                val state by habitsDetailViewModel.uiState.collectAsStateWithLifecycle()
                TaskerKeeperTheme {
                    Surface {
                        when (state) {
                            is HabitsDetailState.Content -> HabitsDetailContent()
                            is HabitsDetailState.Error -> HabitsDetailError()
                            is HabitsDetailState.Loading -> HabitsDetailLoading()
                        }
                    }
                }
            }
        }
    }

    @Composable
    fun HabitsDetailContent() {

    }

    @Composable
    fun HabitsDetailError() {
        Text("HabitsDetailFragment Error")
    }

    @Composable
    fun HabitsDetailLoading() {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
    }

    companion object {
        private const val PARENT_CATEGORY_ID = "parentCategoryId"
        fun newInstance(
            parentCategoryId: Int
        ): HabitsDetailFragment {
            val fragment = HabitsDetailFragment()
            val bundle = bundleOf(
                PARENT_CATEGORY_ID to parentCategoryId
            )
            fragment.arguments = bundle
            return fragment
        }
    }
}