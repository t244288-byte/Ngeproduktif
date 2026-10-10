package com.nalar.reroll

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import java.time.LocalDateTime
import kotlin.math.min
import kotlin.random.Random

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { MaterialTheme { NgeproduktifApp() } }
    }
}

enum class TaskStatus { TODO, DONE, SKIPPED }

data class TaskBlock(
    val startMinutes: Int,
    val endMinutes: Int,
    val title: String,
    val category: String,
    val status: TaskStatus = TaskStatus.TODO,
    val isBreak: Boolean = false
)

private val activityPools = mapOf(
    "Career" to listOf(
        "Cari 5 lowongan yang benar-benar relevan",
        "Apply 2 lowongan dengan kecocokan tertinggi",
        "Sesuaikan CV untuk satu job description",
        "Rapikan headline dan About LinkedIn",
        "Kirim pesan ke 3 recruiter atau alumni",
        "Latihan 5 pertanyaan interview",
        "Riset 3 perusahaan target",
        "Follow-up lamaran yang sudah 5–10 hari",
        "Tulis ulang 3 bullet CV supaya lebih kuat",
        "Buat cover letter untuk satu perusahaan",
        "Rapikan format dan konsistensi CV",
        "Cari lowongan remote atau hybrid",
        "Tambah satu work sample ke portfolio",
        "Ambil keyword penting dari satu job description",
        "Latihan self-introduction 60 detik",
        "Latihan jawaban soal gaji dan motivasi",
        "Cari internship, contract, dan entry-level role",
        "Kirim satu networking message yang personal",
        "Buat draft follow-up email recruiter",
        "Update job tracker dan next action",
        "Cek career page 5 perusahaan incaran",
        "Cari 10 recruiter di LinkedIn untuk role target",
        "Latihan cerita pengalaman dengan metode STAR",
        "Review profil LinkedIn dari 3 orang di role target",
        "Bikin daftar 10 perusahaan yang mau dikejar minggu ini"
    ),
    "Skill" to listOf(
        "Latihan Excel atau Google Sheets",
        "Belajar English for work",
        "Selesaikan satu lesson course",
        "Latihan desain presentasi",
        "Pelajari satu workflow AI praktis",
        "Tulis satu halaman ringkasan belajar",
        "Tonton satu tutorial dan catat poin penting",
        "Latihan Canva atau dasar desain",
        "Belajar basic data cleaning",
        "Latihan public speaking 20 menit",
        "Perbaiki satu contoh tulisan supaya lebih jelas",
        "Pelajari shortcut yang bisa mempercepat kerja",
        "Review satu case study di bidangmu",
        "Pelajari satu formula Excel baru",
        "Latihan merangkum artikel atau jurnal",
        "Bikin satu mini exercise dari skill yang dipelajari",
        "Baca satu artikel dan tulis 5 takeaways",
        "Pelajari satu sistem produktivitas baru",
        "Latihan editing satu slide deck",
        "Deep work 45 menit tanpa notifikasi",
        "Latihan membuat tabel dan grafik yang rapi",
        "Belajar dasar project management",
        "Belajar dasar monitoring dan evaluation",
        "Latihan menulis email profesional",
        "Buat mini portfolio project dari skill yang dipelajari"
    ),
    "Health" to listOf(
        "Jalan cepat 30–45 menit",
        "Bodyweight workout",
        "Lari atau jogging",
        "Stretching dan mobility",
        "Jalan santai panjang di luar rumah",
        "Core workout singkat",
        "Ikuti beginner home workout",
        "Recovery stretching dan breathing",
        "Jalan kaki tanpa HP",
        "Latihan postur dan punggung",
        "Quick cardio session",
        "Bersepeda atau aktivitas fisik ringan",
        "Workout ringan plus cooldown",
        "Keluar untuk sunlight dan jalan kaki",
        "Mobility reset session",
        "Naik turun tangga atau brisk movement",
        "Latihan kaki dan keseimbangan",
        "Jalan 5.000 langkah bertahap",
        "Foam-roll atau stretching santai",
        "Latihan napas 10 menit lalu jalan ringan"
    ),
    "Personal" to listOf(
        "Rapikan kamar dan reset workspace",
        "Organize file dan folder download",
        "Plan besok dan review prioritas",
        "Baca buku tanpa HP",
        "Journaling untuk clear the head",
        "Selesaikan satu life-admin task",
        "Declutter meja kerja",
        "Rapikan email inbox",
        "Atur pengeluaran minggu ini",
        "Laundry atau clothing reset",
        "Review kebiasaan dan level energi",
        "Tulis refleksi singkat hari ini",
        "Rapikan apps dan notes di HP",
        "Balas chat atau telepon orang penting",
        "Siapkan kebutuhan untuk besok",
        "Rapikan dokumen penting",
        "Backup file penting ke cloud",
        "Bereskan satu area rumah yang berantakan",
        "Buat daftar prioritas minggu ini",
        "No-phone reset selama 30 menit"
    ),
    "Side Hustle" to listOf(
        "Cari 15 calon klien potensial",
        "Kirim 10 outreach personal",
        "Buat satu konten marketing yang berguna",
        "Improve portfolio atau landing page",
        "Follow-up warm leads",
        "Buat before-and-after work sample",
        "Review pricing, offer, dan conversion",
        "Minta testimonial ke satu client lama",
        "Buat satu paket jasa yang lebih jelas",
        "Desain satu carousel atau promo visual",
        "Draft ide konten untuk 7 hari",
        "Riset kompetitor dan positioning mereka",
        "Update bio dan call-to-action",
        "Post satu proof-of-work",
        "Balas semua prospect aktif",
        "Perbaiki order flow atau contact link",
        "Buat satu konten edukasi untuk target market",
        "Buat satu sample slide atau dokumen",
        "Review channel mana yang paling banyak bawa leads",
        "Bikin offer khusus first-time client",
        "Rapikan price list supaya gampang dipahami",
        "Buat template DM outreach baru",
        "Cari 5 komunitas tempat calon klien berkumpul",
        "Audit halaman Instagram atau website jasa",
        "Bikin satu case study dari project lama"
    )
)

private fun poolFor(category: String): List<String> = activityPools[category].orEmpty()

private fun formatTime(totalMinutes: Int): String {
    val h = (totalMinutes / 60).coerceIn(0, 23)
    val m = totalMinutes % 60
    return String.format("%02d:%02d", h, m)
}

private fun roundUpToQuarter(totalMinutes: Int): Int {
    val remainder = totalMinutes % 15
    return if (remainder == 0) totalMinutes else totalMinutes + (15 - remainder)
}

private fun categoriesForFocus(focus: String): List<String> = when (focus) {
    "Career" -> listOf("Career", "Career", "Skill", "Career", "Health", "Personal")
    "Skill" -> listOf("Skill", "Career", "Skill", "Health", "Personal", "Side Hustle")
    "Side Hustle" -> listOf("Side Hustle", "Side Hustle", "Career", "Skill", "Health", "Personal")
    "Health" -> listOf("Health", "Career", "Skill", "Personal", "Side Hustle")
    "Personal" -> listOf("Personal", "Career", "Skill", "Health", "Side Hustle")
    else -> listOf("Career", "Skill", "Side Hustle", "Health", "Personal")
}

private fun durationChoices(energy: String, category: String): List<Int> {
    if (category == "Health") {
        return when (energy) {
            "Low" -> listOf(30, 45)
            "High" -> listOf(60, 75, 90)
            else -> listOf(45, 60)
        }
    }
    return when (energy) {
        "Low" -> listOf(30, 45, 45)
        "High" -> listOf(60, 75, 90)
        else -> listOf(45, 60, 60)
    }
}

private fun generateDay(energy: String, focus: String, endHour: Int, seed: Int = Random.nextInt()): List<TaskBlock> {
    val random = Random(seed)
    val now = LocalDateTime.now()
    var cursor = roundUpToQuarter(now.hour * 60 + now.minute)
    val end = endHour * 60
    if (cursor >= end) return emptyList()

    val result = mutableListOf<TaskBlock>()
    val categories = categoriesForFocus(focus)
    var categoryIndex = random.nextInt(categories.size)
    var workSinceBreak = 0
    var lunchAdded = false
    val usedByCategory = mutableMapOf<String, MutableSet<String>>()

    while (cursor < end) {
        val remaining = end - cursor
        if (remaining < 20) break

        if (!lunchAdded && cursor in (12 * 60)..(13 * 60 + 45) && remaining >= 45) {
            val duration = min(60, remaining)
            result += TaskBlock(cursor, cursor + duration, "Makan + istirahat beneran", "Break", isBreak = true)
            cursor += duration
            lunchAdded = true
            workSinceBreak = 0
            continue
        }

        if (workSinceBreak >= 105 && remaining >= 30) {
            val duration = if (energy == "Low") 30 else 15
            result += TaskBlock(cursor, cursor + duration, "Phone-free break", "Break", isBreak = true)
            cursor += duration
            workSinceBreak = 0
            continue
        }

        val category = categories[categoryIndex % categories.size]
        categoryIndex++
        val duration = min(durationChoices(energy, category).random(random), remaining)
        if (duration < 20) break

        val used = usedByCategory.getOrPut(category) { mutableSetOf() }
        val pool = poolFor(category)
        val available = pool.filterNot { it in used }.ifEmpty { used.clear(); pool }
        val title = available.random(random)
        used += title

        result += TaskBlock(cursor, cursor + duration, title, category)
        cursor += duration
        workSinceBreak += duration
    }

    if (end - cursor >= 15) {
        result += TaskBlock(cursor, end, "Daily wrap-up: catat progress dan siapkan besok", "Personal")
    }
    return result
}

@Composable
fun NgeproduktifApp() {
    var energy by remember { mutableStateOf("Normal") }
    var focus by remember { mutableStateOf("Mixed") }
    var endHour by remember { mutableIntStateOf(18) }
    var tasks by remember { mutableStateOf<List<TaskBlock>>(emptyList()) }
    var rerolls by remember { mutableIntStateOf(0) }

    if (tasks.isEmpty()) {
        SetupScreen(energy, { energy = it }, focus, { focus = it }, endHour, { endHour = it }) {
            tasks = generateDay(energy, focus, endHour)
            rerolls = 0
        }
    } else {
        TodayScreen(
            tasks = tasks,
            rerolls = rerolls,
            onDone = { index -> tasks = tasks.toMutableList().also { it[index] = it[index].copy(status = TaskStatus.DONE) } },
            onSkip = { index -> tasks = tasks.toMutableList().also { it[index] = it[index].copy(status = TaskStatus.SKIPPED) } },
            onReroll = { index ->
                if (rerolls < 3) {
                    val task = tasks[index]
                    val alternatives = poolFor(task.category).filterNot { it == task.title }
                    if (alternatives.isNotEmpty()) {
                        tasks = tasks.toMutableList().also { it[index] = task.copy(title = alternatives.random(), status = TaskStatus.TODO) }
                        rerolls++
                    }
                }
            },
            onReplan = { tasks = generateDay(energy, focus, endHour); rerolls = 0 },
            onEdit = { tasks = emptyList() }
        )
    }
}

@Composable
private fun SetupScreen(
    energy: String,
    onEnergyChange: (String) -> Unit,
    focus: String,
    onFocusChange: (String) -> Unit,
    endHour: Int,
    onEndHourChange: (Int) -> Unit,
    onGenerate: () -> Unit
) {
    val now = LocalDateTime.now()
    val greeting = when (now.hour) {
        in 5..11 -> "Good morning."
        in 12..16 -> "Good afternoon."
        else -> "Let's salvage the day."
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text("NGEPRODUKTIF", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(8.dp))
            Text(greeting, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
            Text("Bingung hari ini mau ngapain? Kasih batasannya. Biar aplikasi yang susun harimu.")
        }
        item {
            Card(shape = RoundedCornerShape(18.dp)) {
                Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("1. Energi hari ini", fontWeight = FontWeight.Bold)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf("Low", "Normal", "High").forEach {
                            FilterChip(selected = energy == it, onClick = { onEnergyChange(it) }, label = { Text(it) })
                        }
                    }
                }
            }
        }
        item {
            Card(shape = RoundedCornerShape(18.dp)) {
                Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("2. Fokus utama", fontWeight = FontWeight.Bold)
                    listOf("Mixed", "Career", "Skill", "Side Hustle", "Health", "Personal").forEach {
                        FilterChip(selected = focus == it, onClick = { onFocusChange(it) }, label = { Text(it) })
                    }
                }
            }
        }
        item {
            Card(shape = RoundedCornerShape(18.dp)) {
                Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("3. Selesai produktif jam", fontWeight = FontWeight.Bold)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf(16, 17, 18, 19).forEach { hour ->
                            FilterChip(selected = endHour == hour, onClick = { onEndHourChange(hour) }, label = { Text(String.format("%02d:00", hour)) })
                        }
                    }
                }
            }
        }
        item {
            Card(shape = RoundedCornerShape(18.dp)) {
                Column(Modifier.padding(16.dp)) {
                    Text("Today's setup", fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(6.dp))
                    Text("Energy: $energy")
                    Text("Focus: $focus")
                    Text("Clock out: ${String.format("%02d:00", endHour)}")
                }
            }
        }
        item {
            Button(onClick = onGenerate, modifier = Modifier.fillMaxWidth().height(58.dp), shape = RoundedCornerShape(18.dp)) {
                Text("BUILD MY DAY  🎲", fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun TodayScreen(
    tasks: List<TaskBlock>,
    rerolls: Int,
    onDone: (Int) -> Unit,
    onSkip: (Int) -> Unit,
    onReroll: (Int) -> Unit,
    onReplan: () -> Unit,
    onEdit: () -> Unit
) {
    val actionableTasks = tasks.count { !it.isBreak }
    val completedTasks = tasks.count { !it.isBreak && it.status == TaskStatus.DONE }
    val progress = if (actionableTasks == 0) 0f else completedTasks.toFloat() / actionableTasks

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Top) {
                Column(Modifier.weight(1f)) {
                    Text("TODAY", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(4.dp))
                    Text("Ini jadwal lu hari ini.", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
                    Text("Jangan mikir harus ngapain. Tinggal ikutin jadwalnya.")
                }
                TextButton(onClick = onEdit) { Text("Edit") }
            }
        }
        item {
            Card(shape = RoundedCornerShape(18.dp)) {
                Column(Modifier.padding(16.dp)) {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Progress", fontWeight = FontWeight.Bold)
                        Text("$completedTasks / $actionableTasks")
                    }
                    Spacer(Modifier.height(10.dp))
                    LinearProgressIndicator(progress = { progress }, modifier = Modifier.fillMaxWidth())
                    Spacer(Modifier.height(8.dp))
                    Text("Reroll tersisa: ${(3 - rerolls).coerceAtLeast(0)}", style = MaterialTheme.typography.bodySmall)
                }
            }
        }
        item { ScheduleTable(tasks) }
        item {
            Text("Detail aktivitas", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Text("Done, skip, atau reroll dari card di bawah.", style = MaterialTheme.typography.bodySmall)
        }
        itemsIndexed(tasks) { index, task ->
            ActivityCard(task, rerolls < 3 && !task.isBreak, { onDone(index) }, { onSkip(index) }, { onReroll(index) })
        }
        item {
            OutlinedButton(onClick = onReplan, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp)) {
                Text("RE-PLAN TODAY  ↻")
            }
        }
    }
}

@Composable
private fun ScheduleTable(tasks: List<TaskBlock>) {
    Card(shape = RoundedCornerShape(18.dp)) {
        Column(Modifier.padding(16.dp)) {
            Text("Today's Schedule", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(4.dp))
            Text("Full-day view dalam bentuk timetable.", style = MaterialTheme.typography.bodySmall)
            Spacer(Modifier.height(14.dp))
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Text("Waktu", modifier = Modifier.width(78.dp), style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                Text("Aktivitas", modifier = Modifier.weight(1f), style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                Text("Status", modifier = Modifier.width(62.dp), style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, textAlign = TextAlign.End)
            }
            Spacer(Modifier.height(8.dp))
            HorizontalDivider()
            tasks.forEachIndexed { index, task ->
                ScheduleRow(task)
                if (index != tasks.lastIndex) HorizontalDivider()
            }
        }
    }
}

@Composable
private fun ScheduleRow(task: TaskBlock) {
    val nowMinutes = LocalDateTime.now().let { it.hour * 60 + it.minute }
    val isNow = nowMinutes in task.startMinutes until task.endMinutes
    val statusText = when {
        task.isBreak -> "Break"
        task.status == TaskStatus.DONE -> "Done"
        task.status == TaskStatus.SKIPPED -> "Skip"
        isNow -> "Now"
        else -> "Todo"
    }

    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 10.dp), verticalAlignment = Alignment.CenterVertically) {
        Text("${formatTime(task.startMinutes)}\n${formatTime(task.endMinutes)}", modifier = Modifier.width(78.dp), style = MaterialTheme.typography.bodySmall, fontWeight = if (isNow) FontWeight.Bold else FontWeight.Normal)
        Column(Modifier.weight(1f)) {
            Text(task.title, style = MaterialTheme.typography.bodyMedium, fontWeight = if (isNow) FontWeight.Bold else FontWeight.Medium)
            Text(task.category, style = MaterialTheme.typography.bodySmall)
        }
        Box(
            modifier = Modifier.width(62.dp).background(
                color = if (isNow) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant,
                shape = RoundedCornerShape(10.dp)
            ).padding(horizontal = 6.dp, vertical = 6.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(statusText, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
        }
    }
}

@Composable
private fun ActivityCard(task: TaskBlock, canReroll: Boolean, onDone: () -> Unit, onSkip: () -> Unit, onReroll: () -> Unit) {
    val nowMinutes = LocalDateTime.now().let { it.hour * 60 + it.minute }
    val isNow = nowMinutes in task.startMinutes until task.endMinutes
    val chipText = when {
        task.isBreak -> "BREAK"
        task.status == TaskStatus.DONE -> "DONE"
        task.status == TaskStatus.SKIPPED -> "SKIPPED"
        isNow -> "NOW"
        else -> task.category.uppercase()
    }

    Card(shape = RoundedCornerShape(18.dp)) {
        Column(Modifier.padding(16.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text("${formatTime(task.startMinutes)}–${formatTime(task.endMinutes)}", fontWeight = FontWeight.Bold)
                SuggestionChip(onClick = {}, label = { Text(chipText) })
            }
            Spacer(Modifier.height(8.dp))
            Text(task.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            Text("${task.endMinutes - task.startMinutes} min", style = MaterialTheme.typography.bodySmall)
            if (!task.isBreak && task.status == TaskStatus.TODO) {
                Spacer(Modifier.height(12.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(onClick = onDone) { Text("Done") }
                    OutlinedButton(onClick = onSkip) { Text("Skip") }
                    if (canReroll) TextButton(onClick = onReroll) { Text("Reroll 🎲") }
                }
            }
        }
    }
}
