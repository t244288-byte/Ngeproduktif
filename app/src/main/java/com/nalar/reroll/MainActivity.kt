package com.nalar.reroll

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import kotlin.random.Random

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { MaterialTheme { NgeproduktifApp() } }
    }
}

data class TaskBlock(val time: String, val title: String, val category: String, val done: Boolean = false)

@Composable
fun NgeproduktifApp() {
    var energy by remember { mutableStateOf("Normal") }
    var focus by remember { mutableStateOf("Mixed") }
    var endHour by remember { mutableIntStateOf(18) }
    var tasks by remember { mutableStateOf<List<TaskBlock>>(emptyList()) }
    var rerolls by remember { mutableIntStateOf(0) }

    val career = listOf("Cari 5 lowongan relevan", "Apply 2 lowongan terbaik", "Rapikan CV untuk satu lowongan", "Update LinkedIn", "Latihan 5 pertanyaan interview", "Follow-up lamaran")
    val skill = listOf("Belajar Excel 45 menit", "Latihan English for work", "Belajar Canva/PowerPoint", "Selesaikan 1 lesson course", "Belajar satu workflow AI")
    val health = listOf("Jalan cepat 30 menit", "Workout ringan", "Lari atau jogging", "Stretching 20 menit", "Jalan santai tanpa HP")
    val personal = listOf("Rapikan kamar", "Rapikan file dan download", "Baca 30 menit", "Journaling", "Selesaikan satu urusan pribadi")
    val side = listOf("Cari 15 calon klien Nalar", "Kirim 10 outreach personal", "Buat 1 konten marketing", "Rapikan portfolio", "Follow-up warm leads", "Review pricing dan conversion")

    fun poolFor(category: String) = when (category) {
        "Career" -> career
        "Skill" -> skill
        "Health" -> health
        "Personal" -> personal
        "Side Hustle" -> side
        else -> career + skill + health + personal + side
    }

    fun generateDay(): List<TaskBlock> {
        val now = LocalDateTime.now()
        var hour = if (now.minute >= 30) now.hour + 1 else now.hour
        var minute = if (now.minute >= 30) 0 else 30
        if (hour >= endHour) return emptyList()
        val result = mutableListOf<TaskBlock>()
        val categories = when (focus) {
            "Career" -> listOf("Career", "Career", "Skill", "Health", "Personal")
            "Side Hustle" -> listOf("Side Hustle", "Side Hustle", "Career", "Health", "Personal")
            "Health" -> listOf("Health", "Career", "Skill", "Personal", "Side Hustle")
            else -> listOf("Career", "Skill", "Health", "Side Hustle", "Personal")
        }
        var idx = 0
        while (hour < endHour) {
            if (hour == 12 || hour == 13) {
                result += TaskBlock(String.format("%02d:%02d", hour, minute), "Makan + istirahat beneran", "Break")
            } else {
                val cat = categories[idx % categories.size]
                result += TaskBlock(String.format("%02d:%02d", hour, minute), poolFor(cat).random(), cat)
                idx++
            }
            val step = if (energy == "Low") 60 else if (energy == "High") 90 else 60
            val total = hour * 60 + minute + step
            hour = total / 60
            minute = total % 60
        }
        return result
    }

    if (tasks.isEmpty()) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Text("NGEPRODUKTIF", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(8.dp))
                Text("Bingung hari ini mau ngapain?", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
                Text("Kasih batasannya. Biar aplikasi yang susun harimu sampai sore.")
            }
            item {
                Card(shape = RoundedCornerShape(18.dp)) {
                    Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("Energi hari ini", fontWeight = FontWeight.Bold)
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            listOf("Low", "Normal", "High").forEach {
                                FilterChip(selected = energy == it, onClick = { energy = it }, label = { Text(it) })
                            }
                        }
                    }
                }
            }
            item {
                Card(shape = RoundedCornerShape(18.dp)) {
                    Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("Fokus utama", fontWeight = FontWeight.Bold)
                        listOf("Mixed", "Career", "Side Hustle", "Health").forEach {
                            FilterChip(selected = focus == it, onClick = { focus = it }, label = { Text(it) })
                        }
                    }
                }
            }
            item {
                Card(shape = RoundedCornerShape(18.dp)) {
                    Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("Selesai produktif jam", fontWeight = FontWeight.Bold)
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            listOf(16,17,18,19).forEach { h ->
                                FilterChip(selected = endHour == h, onClick = { endHour = h }, label = { Text(String.format("%02d:00", h)) })
                            }
                        }
                    }
                }
            }
            item {
                Button(
                    onClick = { tasks = generateDay(); rerolls = 0 },
                    modifier = Modifier.fillMaxWidth().height(58.dp),
                    shape = RoundedCornerShape(18.dp)
                ) { Text("RANDOMIZE MY DAY 🎲", fontWeight = FontWeight.Bold) }
            }
        }
    } else {
        val completed = tasks.count { it.done }
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Text("TODAY", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
                Text("Hari lu udah diputusin.", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
                Text("Progress $completed / ${tasks.size} • Reroll tersisa ${3-rerolls}")
                LinearProgressIndicator(progress = { if(tasks.isEmpty()) 0f else completed.toFloat()/tasks.size }, modifier = Modifier.fillMaxWidth().padding(top=8.dp))
            }
            itemsIndexed(tasks) { index, task ->
                Card(shape = RoundedCornerShape(18.dp)) {
                    Column(Modifier.padding(16.dp)) {
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Text(task.time, fontWeight = FontWeight.Bold)
                            SuggestionChip(onClick = {}, label = { Text(if(task.done) "DONE" else task.category.uppercase()) })
                        }
                        Spacer(Modifier.height(6.dp))
                        Text(task.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                        if (!task.done) {
                            Spacer(Modifier.height(10.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Button(onClick = { tasks = tasks.toMutableList().also { it[index] = task.copy(done = true) } }) { Text("Done") }
                                OutlinedButton(onClick = { tasks = tasks.toMutableList().also { it.removeAt(index) } }) { Text("Skip") }
                                if (rerolls < 3 && task.category != "Break") {
                                    TextButton(onClick = {
                                        tasks = tasks.toMutableList().also { it[index] = task.copy(title = poolFor(task.category).random()) }
                                        rerolls++
                                    }) { Text("Reroll 🎲") }
                                }
                            }
                        }
                    }
                }
            }
            item {
                OutlinedButton(onClick = { tasks = generateDay(); rerolls = 0 }, modifier = Modifier.fillMaxWidth()) { Text("RE-PLAN TODAY ↻") }
                TextButton(onClick = { tasks = emptyList() }, modifier = Modifier.fillMaxWidth()) { Text("Edit setup") }
            }
        }
    }
}
