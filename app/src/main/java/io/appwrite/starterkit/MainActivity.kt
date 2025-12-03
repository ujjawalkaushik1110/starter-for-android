package io.appwrite.starterkit

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.WindowCompat
import io.appwrite.starterkit.extensions.edgeToEdgeWithStyle
import io.appwrite.starterkit.ui.schedule.ScheduleWiseApp

/**
 * MainActivity serves as the entry point for the application.
 * It configures the system's edge-to-edge settings, splash screen, and initializes the composable layout.
 */
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)

        edgeToEdgeWithStyle()
        WindowCompat.setDecorFitsSystemWindows(window, false)

        setContent { ScheduleWiseApp() }
    }
}
