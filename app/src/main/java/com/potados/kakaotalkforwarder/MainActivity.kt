package com.potados.kakaotalkforwarder

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.potados.kakaotalkforwarder.ui.main.MainScreen
import com.potados.kakaotalkforwarder.ui.theme.KakaoTalkForwarderTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            KakaoTalkForwarderTheme {
                MainScreen()
            }
        }
    }
}
