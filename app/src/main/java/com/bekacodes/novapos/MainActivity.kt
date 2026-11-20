package com.bekacodes.novapos

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.bekacodes.novapos.hardware.nfc.SoftPosNfcReader
import com.bekacodes.novapos.ui.NovaPosViewModel
import com.example.novapos.presentation.NovaPosScreen
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var nfcReader: SoftPosNfcReader

    private val viewModel: NovaPosViewModel by viewModels()

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

    override fun onResume() {
        super.onResume()
        nfcReader.startReaderMode(this)
    }

    override fun onPause() {
        super.onPause()
        nfcReader.stopReaderMode(this)
    }
}