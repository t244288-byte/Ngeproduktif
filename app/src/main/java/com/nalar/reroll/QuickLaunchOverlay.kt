package com.nalar.reroll

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import java.time.LocalDateTime
import kotlin.random.Random

fun buildQuickRandomDay(seed: Int = Random.nextInt()): DayPlan {
    val random = Random(seed)
    val now = LocalDateTime.now()
    val nowMinutes = now.hour * 60 + now.minute

    val energy = listOf("Low", "Normal", "High").random(random)
    val focus = listOf(
        "Mixed",
        "Seek Job",
        "Career",
        "Skill",
        "Side Hustle",
        "Health",
        "Personal"
    ).random(random)

    val preferred = (16..23).filter { hour ->
        hour * 60 - nowMinutes >= 90
    }
    val fallback = (16..23).filter { hour ->
        hour * 60 - nowMinutes >= 20
    }
    val endHour = when {
        preferred.isNotEmpty() -> preferred.random(random)
        fallback.isNotEmpty() -> fallback.random(random)
        else -> 23
    }

    return generateDay(
        energy = energy,
        focus = focus,
        endHour = endHour,
        seed = random.nextInt()
    )
}

@Composable
fun QuickLaunchOverlay(onYuk: () -> Unit) {
    val transition = rememberInfiniteTransition(label = "quick_launch")
    val pulse by transition.animateFloat(
        initialValue = 0.96f,
        targetValue = 1.04f,
        animationSpec = infiniteRepeatable(
            animation = tween(1050, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )
    val floatY by transition.animateFloat(
        initialValue = -8f,
        targetValue = 8f,
        animationSpec = infiniteRepeatable(
            animation = tween(1450, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "floatY"
    )
    val bubbleDrift by transition.animateFloat(
        initialValue = -22f,
        targetValue = 22f,
        animationSpec = infiniteRepeatable(
            animation = tween(2200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "bubbleDrift"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xE6171717)),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawCircle(
                color = BrandOrange.copy(alpha = 0.18f),
                radius = 150f,
                center = Offset(size.width * 0.14f + bubbleDrift, size.height * 0.20f)
            )
            drawCircle(
                color = Color.White.copy(alpha = 0.07f),
                radius = 105f,
                center = Offset(size.width * 0.84f - bubbleDrift, size.height * 0.28f)
            )
            drawCircle(
                color = BrandOrange.copy(alpha = 0.12f),
                radius = 190f,
                center = Offset(size.width * 0.78f + bubbleDrift * 0.4f, size.height * 0.82f)
            )
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .offset(y = floatY.dp)
                .scale(pulse)
                .clip(RoundedCornerShape(34.dp))
                .background(
                    Brush.linearGradient(
                        listOf(
                            Color(0xFF181818),
                            Color(0xFF2A211D),
                            BrandOrange
                        )
                    )
                )
                .padding(horizontal = 26.dp, vertical = 30.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Box(
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.12f))
                        .padding(horizontal = 14.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = "ONE TAP MODE",
                        color = Color.White.copy(alpha = 0.82f),
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(Modifier.height(24.dp))

                Text(
                    text = "NGEPRODUKTIF\nYUKK",
                    color = Color.White,
                    textAlign = TextAlign.Center,
                    fontWeight = FontWeight.Black,
                    style = androidx.compose.material3.MaterialTheme.typography.displaySmall
                )

                Spacer(Modifier.height(12.dp))

                Text(
                    text = "Sekali tap. Energy, fokus, jam selesai, dan route hari ini langsung diacak.",
                    color = Color.White.copy(alpha = 0.78f),
                    textAlign = TextAlign.Center,
                    style = androidx.compose.material3.MaterialTheme.typography.bodyLarge
                )

                Spacer(Modifier.height(26.dp))

                Button(
                    onClick = onYuk,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(58.dp),
                    shape = RoundedCornerShape(18.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.White,
                        contentColor = BrandOrange
                    )
                ) {
                    Text(
                        text = "YUK  →",
                        fontWeight = FontWeight.Black,
                        style = androidx.compose.material3.MaterialTheme.typography.titleMedium
                    )
                }
            }
        }
    }
}
