package com.nalar.reroll

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

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
                    Column(modifier = Modifier.fillMaxSize()) {
                        Surface(
                            color = BrandOrangeSoft,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 14.dp, vertical = 7.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "${totalActivityCountV2()} activities  •  ${activityCountV2("Career")}/category  •  no repeat for ${RandomizerGuard.COOLDOWN_CLICKS} rolls",
                                    color = BrandOrange,
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                        Box(modifier = Modifier.weight(1f)) {
                            NgeproduktifApp(store = store)
                        }
                    }
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
