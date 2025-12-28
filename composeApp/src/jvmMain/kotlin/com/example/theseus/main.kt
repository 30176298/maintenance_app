package com.example.theseus

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import androidx.compose.ui.unit.dp
import com.example.theseus.App
import com.example.theseus.di.appModule
import com.example.theseus.di.platformModule
import org.koin.core.context.startKoin

fun main() = application {
    // Initialize Koin
    startKoin {
        modules(platformModule, appModule)
    }

    val windowState = rememberWindowState(
        width = 1200.dp,
        height = 800.dp
    )

    Window(
        onCloseRequest = ::exitApplication,
        title = "Theseus - Aircraft Maintenance",
        state = windowState
    ) {
        App()
    }
}
