package com.nalar.reroll

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject
import java.time.LocalDate
import java.time.LocalDateTime
import kotlin.math.min
import kotlin.random.Random

enum class TaskStatus { TODO, DONE, SKIPPED }

data class TaskBlock(
    val startMinutes: Int,
    val endMinutes: Int,
    val title: String,
    val category: String,
    val status: TaskStatus = TaskStatus.TODO,
    val isBreak: Boolean = false
)

data class DayPlan(
    val date: String,
    val energy: String,
    val focus: String,
    val endHour: Int,
    val rerollsUsed: Int = 0,
    val tasks: List<TaskBlock>
)

data class DayRecord(
    val date: String,
    val done: Int,
    val total: Int,
    val productiveMinutes: Int
)

data class UserProfile(
    val name: String = "You",
    val headline: String = "Building momentum",
    val mainGoal: String = "Mixed",
    val notificationsEnabled: Boolean = true
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
        "Cari lowongan remote atau hybrid",
        "Tambah satu work sample ke portfolio",
        "Ambil keyword penting dari satu job description",
        "Latihan self-introduction 60 detik",
        "Latihan jawaban soal gaji dan motivasi",
        "Cari internship, contract, dan entry-level role",
        "Kirim satu networking message yang personal",
        "Update job tracker dan next action",
        "Cek career page 5 perusahaan incaran",
        "Latihan cerita pengalaman dengan metode STAR",
        "Review 3 lowongan dan bandingkan requirement-nya",
        "Cari satu event networking atau webinar karier",
        "Tulis 5 pencapaian yang bisa dipakai saat interview",
        "Cari 10 recruiter di LinkedIn untuk role target",
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
        "Deep work 45 menit tanpa notifikasi",
        "Latihan membuat tabel dan grafik yang rapi",
        "Belajar dasar project management",
        "Belajar dasar monitoring dan evaluation",
        "Latihan menulis email profesional",
        "Buat mini portfolio project dari skill yang dipelajari",
        "Belajar satu fitur baru PowerPoint",
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
        "Upper-body workout ringan",
        "Lower-body workout ringan",
        "Yoga atau mobility flow 30 menit",
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

fun poolFor(category: String): List<String> = activityPools[category].orEmpty()

fun formatTime(totalMinutes: Int): String {
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

fun generateDay(
    energy: String,
    focus: String,
    endHour: Int,
    seed: Int = Random.nextInt()
): DayPlan {
    val random = Random(seed)
    val now = LocalDateTime.now()
    var cursor = roundUpToQuarter(now.hour * 60 + now.minute)
    val end = endHour * 60
    val result = mutableListOf<TaskBlock>()

    if (cursor < end) {
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
    }

    return DayPlan(
        date = LocalDate.now().toString(),
        energy = energy,
        focus = focus,
        endHour = endHour,
        rerollsUsed = 0,
        tasks = result
    )
}

class NgeStore(context: Context) {
    private val prefs = context.getSharedPreferences("ngeproduktif_v04", Context.MODE_PRIVATE)

    fun savePlan(plan: DayPlan) {
        prefs.edit().putString("today_plan", planToJson(plan).toString()).apply()
        updateHistory(plan)
    }

    fun loadTodayPlan(): DayPlan? {
        val raw = prefs.getString("today_plan", null) ?: return null
        return runCatching {
            val plan = planFromJson(JSONObject(raw))
            if (plan.date == LocalDate.now().toString()) plan else null
        }.getOrNull()
    }

    fun clearPlan() {
        prefs.edit().remove("today_plan").apply()
    }

    fun saveProfile(profile: UserProfile) {
        val obj = JSONObject()
            .put("name", profile.name)
            .put("headline", profile.headline)
            .put("mainGoal", profile.mainGoal)
            .put("notificationsEnabled", profile.notificationsEnabled)
        prefs.edit().putString("profile", obj.toString()).apply()
    }

    fun loadProfile(): UserProfile {
        val raw = prefs.getString("profile", null) ?: return UserProfile()
        return runCatching {
            val obj = JSONObject(raw)
            UserProfile(
                name = obj.optString("name", "You"),
                headline = obj.optString("headline", "Building momentum"),
                mainGoal = obj.optString("mainGoal", "Mixed"),
                notificationsEnabled = obj.optBoolean("notificationsEnabled", true)
            )
        }.getOrDefault(UserProfile())
    }

    fun loadHistory(): List<DayRecord> {
        val raw = prefs.getString("history", "[]") ?: "[]"
        return runCatching {
            val arr = JSONArray(raw)
            buildList {
                for (i in 0 until arr.length()) {
                    val obj = arr.getJSONObject(i)
                    add(
                        DayRecord(
                            date = obj.getString("date"),
                            done = obj.optInt("done", 0),
                            total = obj.optInt("total", 0),
                            productiveMinutes = obj.optInt("productiveMinutes", 0)
                        )
                    )
                }
            }.sortedByDescending { it.date }
        }.getOrDefault(emptyList())
    }

    private fun updateHistory(plan: DayPlan) {
        val doneTasks = plan.tasks.filter { !it.isBreak && it.status == TaskStatus.DONE }
        val total = plan.tasks.count { !it.isBreak }
        val minutes = doneTasks.sumOf { it.endMinutes - it.startMinutes }
        val record = DayRecord(plan.date, doneTasks.size, total, minutes)

        val existing = loadHistory().associateBy { it.date }.toMutableMap()
        existing[record.date] = record
        val cutoff = LocalDate.now().minusDays(90)
        val filtered = existing.values
            .filter { runCatching { LocalDate.parse(it.date) >= cutoff }.getOrDefault(false) }
            .sortedBy { it.date }

        val arr = JSONArray()
        filtered.forEach {
            arr.put(
                JSONObject()
                    .put("date", it.date)
                    .put("done", it.done)
                    .put("total", it.total)
                    .put("productiveMinutes", it.productiveMinutes)
            )
        }
        prefs.edit().putString("history", arr.toString()).apply()
    }

    private fun planToJson(plan: DayPlan): JSONObject {
        val tasks = JSONArray()
        plan.tasks.forEach { task ->
            tasks.put(
                JSONObject()
                    .put("startMinutes", task.startMinutes)
                    .put("endMinutes", task.endMinutes)
                    .put("title", task.title)
                    .put("category", task.category)
                    .put("status", task.status.name)
                    .put("isBreak", task.isBreak)
            )
        }
        return JSONObject()
            .put("date", plan.date)
            .put("energy", plan.energy)
            .put("focus", plan.focus)
            .put("endHour", plan.endHour)
            .put("rerollsUsed", plan.rerollsUsed)
            .put("tasks", tasks)
    }

    private fun planFromJson(obj: JSONObject): DayPlan {
        val arr = obj.getJSONArray("tasks")
        val tasks = buildList {
            for (i in 0 until arr.length()) {
                val t = arr.getJSONObject(i)
                add(
                    TaskBlock(
                        startMinutes = t.getInt("startMinutes"),
                        endMinutes = t.getInt("endMinutes"),
                        title = t.getString("title"),
                        category = t.getString("category"),
                        status = TaskStatus.valueOf(t.optString("status", TaskStatus.TODO.name)),
                        isBreak = t.optBoolean("isBreak", false)
                    )
                )
            }
        }
        return DayPlan(
            date = obj.getString("date"),
            energy = obj.optString("energy", "Normal"),
            focus = obj.optString("focus", "Mixed"),
            endHour = obj.optInt("endHour", 18),
            rerollsUsed = obj.optInt("rerollsUsed", 0),
            tasks = tasks
        )
    }
}

fun calculateStreak(history: List<DayRecord>): Int {
    val activeDates = history.filter { it.done > 0 }.mapNotNull {
        runCatching { LocalDate.parse(it.date) }.getOrNull()
    }.toSet()
    if (activeDates.isEmpty()) return 0

    var cursor = LocalDate.now()
    if (cursor !in activeDates) cursor = cursor.minusDays(1)
    var streak = 0
    while (cursor in activeDates) {
        streak++
        cursor = cursor.minusDays(1)
    }
    return streak
}
