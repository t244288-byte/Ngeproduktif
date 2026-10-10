package com.nalar.reroll

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import kotlin.math.min
import kotlin.random.Random

private val BrandOrange = Color(0xFFFC4C02)
private val BrandOrangeSoft = Color(0xFFFFE7DA)
private val Ink = Color(0xFF171717)
private val MutedInk = Color(0xFF666666)
private val AppBackground = Color(0xFFF6F6F4)
private val CardWhite = Color(0xFFFFFFFF)
private val Divider = Color(0xFFE7E7E3)
private val SkillPurple = Color(0xFF6D5DFB)
private val HealthGreen = Color(0xFF2E9D62)
private val SideBlue = Color(0xFF2D6CDF)
private val PersonalRose = Color(0xFFB35C7C)
private val BreakGrey = Color(0xFF8A8A8A)

private val NgeproduktifColors = lightColorScheme(
    primary = BrandOrange,
    onPrimary = Color.White,
    primaryContainer = BrandOrangeSoft,
    onPrimaryContainer = Ink,
    background = AppBackground,
    onBackground = Ink,
    surface = CardWhite,
    onSurface = Ink,
    surfaceVariant = Color(0xFFEEEEEA),
    onSurfaceVariant = MutedInk,
    outline = Divider
)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme(colorScheme = NgeproduktifColors) {
                Surface(modifier = Modifier.fillMaxSize(), color = AppBackground) {
                    NgeproduktifApp()
                }
            }
        }
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
        "Bikin daftar 10 perusahaan yang mau dikejar minggu ini",
        "Buat jawaban singkat untuk 'kenapa kami harus memilih kamu?'",
        "Review 3 lowongan dan bandingkan requirement-nya",
        "Perbaiki nama file CV dan portfolio biar profesional",
        "Cari satu event networking atau webinar karier",
        "Tulis 5 pencapaian yang bisa dipakai saat interview"
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
        "Buat mini portfolio project dari skill yang dipelajari",
        "Latihan membuat laporan satu halaman",
        "Belajar satu fitur baru PowerPoint",
        "Latihan mengetik dan editing dokumen lebih cepat",
        "Belajar dasar personal branding",
        "Review skill gap untuk role yang kamu incar"
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
        "Latihan napas 10 menit lalu jalan ringan",
        "Upper-body workout ringan",
        "Lower-body workout ringan",
        "Yoga atau mobility flow 30 menit",
        "Jalan ke tempat terdekat daripada naik kendaraan",
        "Hydration break dan stretching singkat"
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
        "No-phone reset selama 30 menit",
        "Rapikan foto dan screenshot yang menumpuk",
        "Buat meal plan sederhana untuk besok",
        "Cek kalender satu minggu ke depan",
        "Hapus satu sumber distraksi dari HP",
        "Rapikan bookmark dan tab browser"
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
        "Bikin satu case study dari project lama",
        "Buat FAQ sederhana untuk calon klien",
        "Cari 10 akun yang bisa jadi partnership",
        "Buat satu konten problem-solution",
        "Review pesan sales yang paling banyak dibalas",
        "Buat satu template follow-up client"
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

private fun generateDay(
    energy: String,
    focus: String,
    endHour: Int,
    seed: Int = Random.nextInt()
): List<TaskBlock> {
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
        val available = pool.filterNot { it in used }.ifEmpty {
            used.clear()
            pool
        }
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

private fun categoryColor(category: String): Color = when (category) {
    "Career" -> BrandOrange
    "Skill" -> SkillPurple
    "Health" -> HealthGreen
    "Side Hustle" -> SideBlue
    "Personal" -> PersonalRose
    else -> BreakGrey
}

@Composable
fun NgeproduktifApp() {
    var energy by remember { mutableStateOf("Normal") }
    var focus by remember { mutableStateOf("Mixed") }
    var endHour by remember { mutableIntStateOf(18) }
    var tasks by remember { mutableStateOf<List<TaskBlock>>(emptyList()) }
    var rerolls by remember { mutableIntStateOf(0) }

    if (tasks.isEmpty()) {
        SetupScreen(
            energy = energy,
            onEnergyChange = { energy = it },
            focus = focus,
            onFocusChange = { focus = it },
            endHour = endHour,
            onEndHourChange = { endHour = it },
            onGenerate = {
                tasks = generateDay(energy, focus, endHour)
                rerolls = 0
            }
        )
    } else {
        TodayScreen(
            tasks = tasks,
            rerolls = rerolls,
            focus = focus,
            energy = energy,
            onDone = { index ->
                tasks = tasks.toMutableList().also {
                    it[index] = it[index].copy(status = TaskStatus.DONE)
                }
            },
            onSkip = { index ->
                tasks = tasks.toMutableList().also {
                    it[index] = it[index].copy(status = TaskStatus.SKIPPED)
                }
            },
            onReroll = { index ->
                if (rerolls < 3) {
                    val task = tasks[index]
                    val alternatives = poolFor(task.category).filterNot { it == task.title }
                    if (alternatives.isNotEmpty()) {
                        tasks = tasks.toMutableList().also {
                            it[index] = task.copy(title = alternatives.random(), status = TaskStatus.TODO)
                        }
                        rerolls++
                    }
                }
            },
            onReplan = {
                tasks = generateDay(energy, focus, endHour)
                rerolls = 0
            },
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
        in 5..11 -> "Good morning"
        in 12..16 -> "Good afternoon"
        else -> "Still time to move"
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(AppBackground),
        contentPadding = PaddingValues(bottom = 36.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            SetupHero(greeting = greeting)
        }

        item {
            Column(
                modifier = Modifier.padding(horizontal = 20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    "Build today's route",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = Ink
                )
                Text(
                    "Pilih kondisi hari ini. Ngeproduktif yang atur rutenya sampai sore.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MutedInk
                )
            }
        }

        item {
            SetupChoiceCard(
                modifier = Modifier.padding(horizontal = 20.dp),
                number = "01",
                title = "Energy",
                subtitle = "Seberapa banyak tenaga yang mau dipakai hari ini?"
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("Low", "Normal", "High").forEach { option ->
                        BrandFilterChip(
                            selected = energy == option,
                            text = option,
                            onClick = { onEnergyChange(option) }
                        )
                    }
                }
            }
        }

        item {
            SetupChoiceCard(
                modifier = Modifier.padding(horizontal = 20.dp),
                number = "02",
                title = "Main focus",
                subtitle = "Fokus boleh satu, tapi jadwal tetap dibuat seimbang."
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("Mixed", "Career", "Skill", "Side Hustle", "Health", "Personal")
                        .chunked(2)
                        .forEach { row ->
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                row.forEach { option ->
                                    Box(modifier = Modifier.weight(1f)) {
                                        BrandFilterChip(
                                            modifier = Modifier.fillMaxWidth(),
                                            selected = focus == option,
                                            text = option,
                                            onClick = { onFocusChange(option) }
                                        )
                                    }
                                }
                                if (row.size == 1) Spacer(Modifier.weight(1f))
                            }
                        }
                }
            }
        }

        item {
            SetupChoiceCard(
                modifier = Modifier.padding(horizontal = 20.dp),
                number = "03",
                title = "Clock out",
                subtitle = "Aplikasi tidak akan memenuhi seharian dengan kerja."
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf(16, 17, 18, 19).forEach { hour ->
                        BrandFilterChip(
                            selected = endHour == hour,
                            text = String.format("%02d:00", hour),
                            onClick = { onEndHourChange(hour) }
                        )
                    }
                }
            }
        }

        item {
            Column(modifier = Modifier.padding(horizontal = 20.dp)) {
                Button(
                    onClick = onGenerate,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(60.dp),
                    shape = RoundedCornerShape(18.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = BrandOrange)
                ) {
                    Text("BUILD MY DAY  →", fontWeight = FontWeight.Bold)
                }
                Spacer(Modifier.height(10.dp))
                Text(
                    "No perfect day. Just a clear next move.",
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.bodySmall,
                    color = MutedInk
                )
            }
        }
    }
}

@Composable
private fun SetupHero(greeting: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(330.dp)
            .background(
                Brush.linearGradient(
                    listOf(
                        Color(0xFF111111),
                        Color(0xFF2A1B16),
                        BrandOrange
                    )
                )
            )
            .padding(horizontal = 22.dp, vertical = 24.dp)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "NGEPRODUKTIF",
                    color = Color.White,
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold
                )
                Surface(
                    shape = CircleShape,
                    color = Color.White.copy(alpha = 0.14f)
                ) {
                    Text(
                        "v0.3",
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
                        color = Color.White,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            Spacer(Modifier.height(26.dp))
            Text(
                greeting,
                color = Color.White.copy(alpha = 0.76f),
                style = MaterialTheme.typography.titleMedium
            )
            Text(
                "Move with\na clear route.",
                color = Color.White,
                style = MaterialTheme.typography.displaySmall,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(4.dp))
            Text(
                "No doomscrolling. No blank day.",
                color = Color.White.copy(alpha = 0.78f),
                style = MaterialTheme.typography.bodyMedium
            )
            Spacer(Modifier.weight(1f))
            RouteIllustration(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp),
                light = true
            )
        }
    }
}

@Composable
private fun SetupChoiceCard(
    modifier: Modifier = Modifier,
    number: String,
    title: String,
    subtitle: String,
    content: @Composable () -> Unit
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = CardWhite)
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(title, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                    Text(subtitle, color = MutedInk, style = MaterialTheme.typography.bodySmall)
                }
                Text(number, color = BrandOrange, fontWeight = FontWeight.Bold)
            }
            content()
        }
    }
}

@Composable
private fun BrandFilterChip(
    modifier: Modifier = Modifier,
    selected: Boolean,
    text: String,
    onClick: () -> Unit
) {
    FilterChip(
        modifier = modifier,
        selected = selected,
        onClick = onClick,
        label = {
            Text(
                text,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center,
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium
            )
        },
        shape = RoundedCornerShape(14.dp),
        colors = FilterChipDefaults.filterChipColors(
            containerColor = Color(0xFFF0F0ED),
            labelColor = Ink,
            selectedContainerColor = BrandOrange,
            selectedLabelColor = Color.White
        ),
        border = FilterChipDefaults.filterChipBorder(
            enabled = true,
            selected = selected,
            borderColor = Color.Transparent,
            selectedBorderColor = Color.Transparent
        )
    )
}

@Composable
private fun TodayScreen(
    tasks: List<TaskBlock>,
    rerolls: Int,
    focus: String,
    energy: String,
    onDone: (Int) -> Unit,
    onSkip: (Int) -> Unit,
    onReroll: (Int) -> Unit,
    onReplan: () -> Unit,
    onEdit: () -> Unit
) {
    var viewMode by remember { mutableStateOf("Timeline") }
    val actionable = tasks.count { !it.isBreak }
    val completed = tasks.count { !it.isBreak && it.status == TaskStatus.DONE }
    val progress = if (actionable == 0) 0f else completed.toFloat() / actionable
    val now = LocalDateTime.now()
    val nowMinutes = now.hour * 60 + now.minute
    val nextTask = tasks.firstOrNull { it.status == TaskStatus.TODO && it.endMinutes > nowMinutes }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(AppBackground),
        contentPadding = PaddingValues(bottom = 36.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            TodayTopBar(onEdit = onEdit)
        }

        item {
            MomentumHero(
                modifier = Modifier.padding(horizontal = 20.dp),
                progress = progress,
                completed = completed,
                total = actionable,
                focus = focus,
                energy = energy,
                nextTask = nextTask,
                rerollsLeft = (3 - rerolls).coerceAtLeast(0)
            )
        }

        item {
            Row(
                modifier = Modifier.padding(horizontal = 20.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                BrandFilterChip(
                    modifier = Modifier.weight(1f),
                    selected = viewMode == "Timeline",
                    text = "Timeline",
                    onClick = { viewMode = "Timeline" }
                )
                BrandFilterChip(
                    modifier = Modifier.weight(1f),
                    selected = viewMode == "Table",
                    text = "Table",
                    onClick = { viewMode = "Table" }
                )
            }
        }

        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        if (viewMode == "Timeline") "Today's route" else "Today's schedule",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        if (viewMode == "Timeline") "One block at a time." else "See the whole day at a glance.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MutedInk
                    )
                }
                Text(
                    "${tasks.size} blocks",
                    style = MaterialTheme.typography.labelLarge,
                    color = BrandOrange,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        if (viewMode == "Timeline") {
            itemsIndexed(tasks) { index, task ->
                TimelineTaskRow(
                    modifier = Modifier.padding(horizontal = 20.dp),
                    task = task,
                    index = index,
                    taskCount = tasks.size,
                    nowMinutes = nowMinutes,
                    canReroll = rerolls < 3 && !task.isBreak,
                    onDone = { onDone(index) },
                    onSkip = { onSkip(index) },
                    onReroll = { onReroll(index) }
                )
            }
        } else {
            item {
                ScheduleTable(
                    modifier = Modifier.padding(horizontal = 20.dp),
                    tasks = tasks,
                    nowMinutes = nowMinutes
                )
            }
        }

        item {
            Column(modifier = Modifier.padding(horizontal = 20.dp)) {
                OutlinedButton(
                    onClick = onReplan,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text("RE-PLAN TODAY  ↻", fontWeight = FontWeight.Bold)
                }
                Spacer(Modifier.height(8.dp))
                Text(
                    "The goal is not to be productive every minute. The goal is to always know the next move.",
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.bodySmall,
                    color = MutedInk
                )
            }
        }
    }
}

@Composable
private fun TodayTopBar(onEdit: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 18.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                "NGEPRODUKTIF",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                color = Ink
            )
            Text(
                DateTimeFormatter.ofPattern("EEE, d MMM").format(LocalDateTime.now()),
                style = MaterialTheme.typography.bodySmall,
                color = MutedInk
            )
        }
        TextButton(onClick = onEdit) {
            Text("Edit setup", color = Ink, fontWeight = FontWeight.SemiBold)
        }
    }
}

@Composable
private fun MomentumHero(
    modifier: Modifier = Modifier,
    progress: Float,
    completed: Int,
    total: Int,
    focus: String,
    energy: String,
    nextTask: TaskBlock?,
    rerollsLeft: Int
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(26.dp))
            .background(
                Brush.linearGradient(
                    listOf(
                        Ink,
                        Color(0xFF26211F),
                        Color(0xFF3A2218)
                    )
                )
            )
            .padding(20.dp)
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column {
                    Text("TODAY'S MOMENTUM", color = Color.White.copy(alpha = .62f), style = MaterialTheme.typography.labelMedium)
                    Spacer(Modifier.height(5.dp))
                    Text(
                        "${(progress * 100).toInt()}%",
                        color = Color.White,
                        style = MaterialTheme.typography.displayMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text("$completed of $total productive blocks done", color = Color.White.copy(alpha = .72f), style = MaterialTheme.typography.bodySmall)
                }
                Surface(shape = RoundedCornerShape(14.dp), color = BrandOrange) {
                    Text(
                        "$rerollsLeft rerolls",
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        color = Color.White,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(Modifier.height(16.dp))
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(7.dp)
                    .clip(CircleShape),
                color = BrandOrange,
                trackColor = Color.White.copy(alpha = .14f)
            )

            Spacer(Modifier.height(14.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                DarkStatPill("Focus", focus)
                DarkStatPill("Energy", energy)
            }

            Spacer(Modifier.height(18.dp))
            RouteIllustration(modifier = Modifier.fillMaxWidth().height(72.dp), light = true)
            Spacer(Modifier.height(10.dp))

            Text("NEXT MOVE", color = BrandOrange, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
            Text(
                nextTask?.title ?: "Route completed. Nice work.",
                color = Color.White,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            if (nextTask != null) {
                Text(
                    "${formatTime(nextTask.startMinutes)} – ${formatTime(nextTask.endMinutes)} • ${nextTask.category}",
                    color = Color.White.copy(alpha = .62f),
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}

@Composable
private fun DarkStatPill(label: String, value: String) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = Color.White.copy(alpha = .09f)
    ) {
        Row(modifier = Modifier.padding(horizontal = 11.dp, vertical = 7.dp)) {
            Text("$label  ", color = Color.White.copy(alpha = .55f), style = MaterialTheme.typography.labelSmall)
            Text(value, color = Color.White, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun TimelineTaskRow(
    modifier: Modifier = Modifier,
    task: TaskBlock,
    index: Int,
    taskCount: Int,
    nowMinutes: Int,
    canReroll: Boolean,
    onDone: () -> Unit,
    onSkip: () -> Unit,
    onReroll: () -> Unit
) {
    val isNow = task.status == TaskStatus.TODO && nowMinutes in task.startMinutes until task.endMinutes
    val accent = categoryColor(task.category)

    Row(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 122.dp),
        verticalAlignment = Alignment.Top
    ) {
        Column(
            modifier = Modifier.width(58.dp),
            horizontalAlignment = Alignment.End
        ) {
            Text(
                formatTime(task.startMinutes),
                fontWeight = if (isNow) FontWeight.Bold else FontWeight.SemiBold,
                color = if (isNow) BrandOrange else Ink,
                style = MaterialTheme.typography.labelLarge
            )
            Text(
                formatTime(task.endMinutes),
                color = MutedInk,
                style = MaterialTheme.typography.labelSmall
            )
        }

        Box(
            modifier = Modifier
                .width(34.dp)
                .height(122.dp),
            contentAlignment = Alignment.TopCenter
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val x = size.width / 2
                if (index > 0) {
                    drawLine(
                        color = Divider,
                        start = Offset(x, 0f),
                        end = Offset(x, 22f),
                        strokeWidth = 4f
                    )
                }
                if (index < taskCount - 1) {
                    drawLine(
                        color = Divider,
                        start = Offset(x, 22f),
                        end = Offset(x, size.height),
                        strokeWidth = 4f
                    )
                }
                drawCircle(color = if (isNow) BrandOrange else accent, radius = if (isNow) 10f else 8f, center = Offset(x, 22f))
                if (isNow) {
                    drawCircle(color = BrandOrange.copy(alpha = .18f), radius = 18f, center = Offset(x, 22f))
                }
            }
        }

        Card(
            modifier = Modifier
                .weight(1f)
                .padding(bottom = 12.dp),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(
                containerColor = if (isNow) BrandOrangeSoft else CardWhite
            )
        ) {
            Column(modifier = Modifier.padding(15.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    CategoryLabel(category = task.category, isBreak = task.isBreak)
                    StatusLabel(task = task, isNow = isNow)
                }
                Spacer(Modifier.height(8.dp))
                Text(
                    task.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = Ink
                )
                Text(
                    "${task.endMinutes - task.startMinutes} min",
                    style = MaterialTheme.typography.bodySmall,
                    color = MutedInk
                )

                if (task.status == TaskStatus.TODO && !task.isBreak) {
                    Spacer(Modifier.height(12.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(7.dp)) {
                        Button(
                            onClick = onDone,
                            colors = ButtonDefaults.buttonColors(containerColor = Ink),
                            shape = RoundedCornerShape(12.dp),
                            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp)
                        ) {
                            Text("Done", fontWeight = FontWeight.Bold)
                        }
                        OutlinedButton(
                            onClick = onSkip,
                            shape = RoundedCornerShape(12.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
                        ) {
                            Text("Skip", color = Ink)
                        }
                        if (canReroll) {
                            TextButton(onClick = onReroll) {
                                Text("Reroll ↻", color = BrandOrange, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CategoryLabel(category: String, isBreak: Boolean) {
    val color = categoryColor(category)
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(color)
        )
        Spacer(Modifier.width(7.dp))
        Text(
            if (isBreak) "BREAK" else category.uppercase(),
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = color
        )
    }
}

@Composable
private fun StatusLabel(task: TaskBlock, isNow: Boolean) {
    val label = when (task.status) {
        TaskStatus.DONE -> "DONE"
        TaskStatus.SKIPPED -> "SKIPPED"
        TaskStatus.TODO -> if (isNow) "NOW" else "UP NEXT"
    }
    val bg = when {
        task.status == TaskStatus.DONE -> HealthGreen.copy(alpha = .12f)
        task.status == TaskStatus.SKIPPED -> BreakGrey.copy(alpha = .12f)
        isNow -> BrandOrange
        else -> Color(0xFFF0F0ED)
    }
    val fg = if (isNow) Color.White else when (task.status) {
        TaskStatus.DONE -> HealthGreen
        TaskStatus.SKIPPED -> BreakGrey
        TaskStatus.TODO -> MutedInk
    }
    Surface(shape = RoundedCornerShape(10.dp), color = bg) {
        Text(
            label,
            modifier = Modifier.padding(horizontal = 9.dp, vertical = 5.dp),
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = fg
        )
    }
}

@Composable
private fun ScheduleTable(
    modifier: Modifier = Modifier,
    tasks: List<TaskBlock>,
    nowMinutes: Int
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = CardWhite)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(modifier = Modifier.fillMaxWidth()) {
                Text("Time", modifier = Modifier.width(76.dp), style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                Text("Activity", modifier = Modifier.weight(1f), style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                Text("Status", modifier = Modifier.width(58.dp), textAlign = TextAlign.End, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
            }
            Spacer(Modifier.height(8.dp))
            HorizontalDivider(color = Divider)
            tasks.forEachIndexed { index, task ->
                val isNow = task.status == TaskStatus.TODO && nowMinutes in task.startMinutes until task.endMinutes
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 11.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "${formatTime(task.startMinutes)}\n${formatTime(task.endMinutes)}",
                        modifier = Modifier.width(76.dp),
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = if (isNow) FontWeight.Bold else FontWeight.Normal,
                        color = if (isNow) BrandOrange else Ink
                    )
                    Column(modifier = Modifier.weight(1f)) {
                        Text(task.title, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
                        CategoryLabel(category = task.category, isBreak = task.isBreak)
                    }
                    Text(
                        when (task.status) {
                            TaskStatus.DONE -> "Done"
                            TaskStatus.SKIPPED -> "Skip"
                            TaskStatus.TODO -> if (isNow) "Now" else "Todo"
                        },
                        modifier = Modifier.width(58.dp),
                        textAlign = TextAlign.End,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = if (isNow) BrandOrange else MutedInk
                    )
                }
                if (index != tasks.lastIndex) HorizontalDivider(color = Divider)
            }
        }
    }
}

@Composable
private fun RouteIllustration(
    modifier: Modifier = Modifier,
    light: Boolean
) {
    val routeColor = if (light) Color.White.copy(alpha = .78f) else BrandOrange
    val ghost = if (light) Color.White.copy(alpha = .12f) else BrandOrange.copy(alpha = .10f)

    Canvas(modifier = modifier) {
        val path = Path().apply {
            moveTo(size.width * .03f, size.height * .74f)
            cubicTo(
                size.width * .20f, size.height * .15f,
                size.width * .36f, size.height * .92f,
                size.width * .52f, size.height * .40f
            )
            cubicTo(
                size.width * .66f, size.height * .02f,
                size.width * .80f, size.height * .88f,
                size.width * .97f, size.height * .18f
            )
        }
        drawPath(path, color = ghost, style = Stroke(width = 13f, cap = StrokeCap.Round))
        drawPath(path, color = routeColor, style = Stroke(width = 5f, cap = StrokeCap.Round))

        val points = listOf(
            Offset(size.width * .04f, size.height * .72f),
            Offset(size.width * .34f, size.height * .61f),
            Offset(size.width * .54f, size.height * .36f),
            Offset(size.width * .77f, size.height * .54f),
            Offset(size.width * .96f, size.height * .20f)
        )
        points.forEachIndexed { index, point ->
            drawCircle(
                color = if (index == points.lastIndex) BrandOrange else routeColor,
                radius = if (index == points.lastIndex) 9f else 6f,
                center = point
            )
        }
    }
}
