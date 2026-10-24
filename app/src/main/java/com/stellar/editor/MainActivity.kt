package com.stellar.editor

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.stellar.editor.ui.screens.EditorScreen
import com.stellar.editor.ui.theme.StellarTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            StellarTheme {
                EditorScreen()
            }
        }
    }
}
