package com.bekacodes.novapos

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.bekacodes.novapos.data.NovaPosDatabase
import com.bekacodes.novapos.ui.NovaPosViewModel
import com.example.novapos.presentation.NovaPosScreen

class MainActivity : ComponentActivity() {

    private val database by lazy { NovaPosDatabase.getInstance(applicationContext) }
    private val viewModel: NovaPosViewModel by viewModels {
        NovaPosViewModel.provideFactory(database.dao())
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                val uiState by viewModel.uiState.collectAsStateWithLifecycle()
                NovaPosScreen(
                    uiState = uiState,
                    onIntent = viewModel::onIntent
                )
            }
        }
    }
}