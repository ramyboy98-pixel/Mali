package com.player.mali

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import com.player.mali.ui.MaliApp
import com.player.mali.viewmodel.MaliViewModel

class MainActivity : ComponentActivity() {
    private val viewModel: MaliViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MaliApp(viewModel = viewModel)
        }
    }
}
