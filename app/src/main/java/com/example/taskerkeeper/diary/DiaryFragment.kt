package com.example.taskerkeeper.diary

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.unit.dp
import androidx.fragment.app.Fragment
import com.example.taskerkeeper.MainActivity
import com.example.taskerkeeper.ui.theme.TaskerKeeperTheme

class DiaryFragment: Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            // Dispose of the Composition when the view's LifecycleOwner
            // is destroyed
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                TaskerKeeperTheme {
                    Column {
                        Button(
                            {
                                // TODO just this part is hacky
                                val thisActivity = this@DiaryFragment.requireActivity()
                                (thisActivity as MainActivity).showTaskFragment()
                            },
                            modifier = Modifier
                                .height(100.dp)
                                .width(200.dp)
                        ) {
                            Text("Go to Fragment1")
                        }
                    }
                }
            }
        }
    }
}