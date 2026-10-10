package com.nalar.reroll

import android.app.AlarmManager
import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.net.Uri
import androidx.core.content.FileProvider
import java.io.File
import java.io.FileOutputStream
import java.time.LocalDate
import java.time.ZoneId

object NotificationScheduler {
    const val CHANNEL_ID = "next_move_channel"
    private const val PREFS = "nge_notification_ids"

    fun schedulePlan(context: Context, plan: DayPlan) {
        cancelAll(context)
        val alarm = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val now = System.currentTimeMillis()
        val ids = mutableSetOf<String>()
        val date = runCatching { LocalDate.parse(plan.date) }.getOrDefault(LocalDate.now())

        plan.tasks.forEachIndexed { index, task ->
            if (task.isBreak || task.status != TaskStatus.TODO) return@forEachIndexed
            val hour = task.startMinutes / 60
            val minute = task.startMinutes % 60
            val triggerAt = date.atTime(hour, minute)
                .atZone(ZoneId.systemDefault())
                .toInstant()
                .toEpochMilli()
            if (triggerAt <= now) return@forEachIndexed

            val requestCode = (date.toEpochDay().toInt() * 100 + index).and(0x7fffffff)
            val intent = Intent(context, NextMoveReceiver::class.java).apply {
                putExtra("title", task.title)
                putExtra("category", task.category)
                putExtra("time", "${formatTime(task.startMinutes)} – ${formatTime(task.endMinutes)}")
                putExtra("requestCode", requestCode)
            }
            val pending = PendingIntent.getBroadcast(
                context,
                requestCode,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            alarm.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAt, pending)
            ids += requestCode.toString()
        }
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit()
            .putStringSet("ids", ids)
            .apply()
    }

    fun cancelAll(context: Context) {
        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        val ids = prefs.getStringSet("ids", emptySet()).orEmpty()
        val alarm = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        ids.forEach { raw ->
            val code = raw.toIntOrNull() ?: return@forEach
            val pending = PendingIntent.getBroadcast(
                context,
                code,
                Intent(context, NextMoveReceiver::class.java),
                PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
            )
            if (pending != null) alarm.cancel(pending)
        }
        prefs.edit().remove("ids").apply()
    }
}

class NextMoveReceiver : android.content.BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val title = intent.getStringExtra("title") ?: "Your next move"
        val category = intent.getStringExtra("category") ?: "Ngeproduktif"
        val time = intent.getStringExtra("time") ?: ""
        val requestCode = intent.getIntExtra("requestCode", 44)

        val openApp = PendingIntent.getActivity(
            context,
            requestCode,
            Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = Notification.Builder(context, NotificationScheduler.CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_popup_reminder)
            .setContentTitle("Next Move · $category")
            .setContentText(title)
            .setStyle(Notification.BigTextStyle().bigText("$time\n$title"))
            .setContentIntent(openApp)
            .setAutoCancel(true)
            .build()

        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.notify(requestCode, notification)
    }
}

object ScheduleShare {
    fun share(context: Context, plan: DayPlan) {
        val width = 1080
        val rowHeight = 168
        val height = 470 + plan.tasks.size * rowHeight
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)

        canvas.drawColor(Color.rgb(246, 246, 244))

        paint.color = Color.rgb(23, 23, 23)
        canvas.drawRoundRect(48f, 48f, width - 48f, 340f, 44f, 44f, paint)
        paint.color = Color.rgb(252, 76, 2)
        canvas.drawRoundRect(70f, 76f, 292f, 130f, 27f, 27f, paint)

        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        paint.textSize = 32f
        paint.color = Color.WHITE
        canvas.drawText("NGEPRODUKTIF", 90f, 113f, paint)

        paint.textSize = 62f
        canvas.drawText("Today's Route", 72f, 205f, paint)
        paint.textSize = 30f
        paint.typeface = Typeface.DEFAULT
        paint.color = Color.rgb(206, 206, 206)
        canvas.drawText("${plan.energy} energy · ${plan.focus} focus", 72f, 255f, paint)

        val done = plan.tasks.count { !it.isBreak && it.status == TaskStatus.DONE }
        val total = plan.tasks.count { !it.isBreak }
        paint.color = Color.rgb(252, 76, 2)
        paint.strokeWidth = 10f
        canvas.drawLine(72f, 298f, 72f + 840f * if (total == 0) 0f else done.toFloat() / total, 298f, paint)
        paint.color = Color.rgb(80, 80, 80)
        paint.strokeWidth = 4f
        canvas.drawLine(72f, 310f, width - 72f, 310f, paint)

        var y = 390f
        plan.tasks.forEach { task ->
            val accent = when (task.category) {
                "Career" -> Color.rgb(252, 76, 2)
                "Skill" -> Color.rgb(109, 93, 251)
                "Health" -> Color.rgb(46, 157, 98)
                "Side Hustle" -> Color.rgb(45, 108, 223)
                "Personal" -> Color.rgb(179, 92, 124)
                else -> Color.rgb(138, 138, 138)
            }
            paint.color = Color.WHITE
            canvas.drawRoundRect(48f, y, width - 48f, y + rowHeight - 18f, 34f, 34f, paint)
            paint.color = accent
            canvas.drawCircle(92f, y + 54f, 14f, paint)

            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            paint.textSize = 28f
            canvas.drawText("${formatTime(task.startMinutes)}–${formatTime(task.endMinutes)}", 122f, y + 62f, paint)

            paint.color = Color.rgb(23, 23, 23)
            paint.textSize = 34f
            drawWrappedText(canvas, task.title, 122f, y + 108f, 780f, paint, 40f, 2)
            y += rowHeight
        }

        val dir = File(context.cacheDir, "shared")
        dir.mkdirs()
        val file = File(dir, "ngeproduktif-${plan.date}.png")
        FileOutputStream(file).use { bitmap.compress(Bitmap.CompressFormat.PNG, 95, it) }
        bitmap.recycle()

        val uri: Uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
        val sendIntent = Intent(Intent.ACTION_SEND).apply {
            type = "image/png"
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_TEXT, "Today's route from Ngeproduktif")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(sendIntent, "Share today's schedule"))
    }

    private fun drawWrappedText(
        canvas: Canvas,
        text: String,
        x: Float,
        firstBaseline: Float,
        maxWidth: Float,
        paint: Paint,
        lineHeight: Float,
        maxLines: Int
    ) {
        val words = text.split(" ")
        var line = ""
        var lineIndex = 0
        for (word in words) {
            val candidate = if (line.isBlank()) word else "$line $word"
            if (paint.measureText(candidate) <= maxWidth) {
                line = candidate
            } else {
                canvas.drawText(line, x, firstBaseline + lineIndex * lineHeight, paint)
                lineIndex++
                if (lineIndex >= maxLines) return
                line = word
            }
        }
        if (lineIndex < maxLines && line.isNotBlank()) {
            canvas.drawText(line, x, firstBaseline + lineIndex * lineHeight, paint)
        }
    }
}
