package com.nalar.reroll

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        createNotificationChannel()
        if (Build.VERSION.SDK_INT >= 33 && checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != android.content.pm.PackageManager.PERMISSION_GRANTED) {
            requestPermissions(arrayOf(Manifest.permission.POST_NOTIFICATIONS), 1001)
        }

        setContent {
            MaterialTheme(colorScheme = NgeproduktifColors) {
                val store = remember { NgeStore(applicationContext) }
                Surface(modifier = Modifier.fillMaxSize(), color = AppBackground) {
                    NgeproduktifApp(store = store)
                }
            }
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                NotificationScheduler.CHANNEL_ID,
                "Next Move",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Pengingat aktivitas berikutnya dari Ngeproduktif"
            }
            getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
        }
    }
}
