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

/**
 * Keeps activities from appearing again for seven randomizer actions.
 * An action is one Generate / Re-plan / Reroll click.
 */
object RandomizerGuard {
    const val COOLDOWN_CLICKS = 7

    var cycle: Int = 0
        private set
    var generating: Boolean = false
        private set

    private val lastUsedCycle = mutableMapOf<String, Int>()

    fun restore(savedCycle: Int, raw: String?) {
        cycle = savedCycle.coerceAtLeast(0)
        lastUsedCycle.clear()
        if (raw.isNullOrBlank()) return
        runCatching {
            val obj = JSONObject(raw)
            val keys = obj.keys()
            while (keys.hasNext()) {
                val key = keys.next()
                lastUsedCycle[key] = obj.optInt(key, -9999)
            }
        }
    }

    fun beginGenerate() {
        cycle += 1
        generating = true
    }

    fun endGenerate() {
        generating = false
    }

    fun beginRerollIfNeeded() {
        if (!generating) cycle += 1
    }

    fun safeCandidates(pool: List<String>): List<String> {
        return pool.filter { title ->
            val last = lastUsedCycle[title]
            last == null || cycle - last > COOLDOWN_CLICKS
        }
    }

    fun leastRecentlyUsed(pool: List<String>, count: Int = 12): List<String> {
        return pool.sortedBy { lastUsedCycle[it] ?: Int.MIN_VALUE }.take(count.coerceAtMost(pool.size))
    }

    fun markSelected(titles: Collection<String>) {
        titles.forEach { title ->
            if (title.isNotBlank()) lastUsedCycle[title] = cycle
        }
    }

    fun snapshotJson(): String {
        val obj = JSONObject()
        lastUsedCycle.forEach { (title, usedCycle) -> obj.put(title, usedCycle) }
        return obj.toString()
    }
}

fun poolFor(category: String): List<String> {
    val fullPool = activityPoolV2(category)
    if (fullPool.isEmpty()) return emptyList()

    RandomizerGuard.beginRerollIfNeeded()
    val safe = RandomizerGuard.safeCandidates(fullPool)
    return if (safe.isNotEmpty()) safe else RandomizerGuard.leastRecentlyUsed(fullPool)
}

fun formatTime(totalMinutes: Int): String {
    val h = (totalMinutes / 60).coerceIn(0, 23)
    val m = totalMinutes % 60
    return String.format("%02d:%02d", h, m)
}

private fun roundUpToQuarter(totalMinutes: Int): Int {
    val remainder = totalMinutes % 15
    return if (remainder == 0) totalMinutes else totalMinutes + (15 - remainder)
}

/**
 * Mixed really mixes all categories.
 * A specific focus now stays in that category instead of silently behaving like Mixed.
 */
private fun categoriesForFocus(focus: String): List<String> = when (focus) {
    "Career" -> listOf("Career")
    "Skill" -> listOf("Skill")
    "Side Hustle" -> listOf("Side Hustle")
    "Health" -> listOf("Health")
    "Personal" -> listOf("Personal")
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

    RandomizerGuard.beginGenerate()
    try {
        if (cursor < end) {
            val categories = categoriesForFocus(focus)
            var categoryIndex = if (categories.size > 1) random.nextInt(categories.size) else 0
            var workSinceBreak = 0
            var lunchAdded = false
            val usedByCategory = mutableMapOf<String, MutableSet<String>>()

            while (cursor < end) {
                val remaining = end - cursor
                if (remaining < 20) break

                if (!lunchAdded && cursor in (12 * 60)..(13 * 60 + 45) && remaining >= 45) {
                    val duration = min(60, remaining)
                    result += TaskBlock(
                        cursor,
                        cursor + duration,
                        "Makan + istirahat beneran",
                        "Break",
                        isBreak = true
                    )
                    cursor += duration
                    lunchAdded = true
                    workSinceBreak = 0
                    continue
                }

                if (workSinceBreak >= 105 && remaining >= 30) {
                    val duration = if (energy == "Low") 30 else 15
                    result += TaskBlock(
                        cursor,
                        cursor + duration,
                        "Phone-free break",
                        "Break",
                        isBreak = true
                    )
                    cursor += duration
                    workSinceBreak = 0
                    continue
                }

                val category = categories[categoryIndex % categories.size]
                categoryIndex++
                val duration = min(durationChoices(energy, category).random(random), remaining)
                if (duration < 20) break

                val used = usedByCategory.getOrPut(category) { mutableSetOf() }
                val guardedPool = poolFor(category)
                val freshCandidates = guardedPool.filterNot { it in used }
                val fallbackCandidates = activityPoolV2(category).filterNot { it in used }
                val candidates = when {
                    freshCandidates.isNotEmpty() -> freshCandidates
                    fallbackCandidates.isNotEmpty() -> fallbackCandidates
                    else -> activityPoolV2(category)
                }
                if (candidates.isEmpty()) break

                val title = candidates.random(random)
                used += title

                result += TaskBlock(cursor, cursor + duration, title, category)
                cursor += duration
                workSinceBreak += duration
            }

            if (end - cursor >= 15) {
                val wrapCategory = if (focus == "Mixed") "Personal" else focus
                result += TaskBlock(
                    cursor,
                    end,
                    "Daily wrap-up: catat progress dan siapkan langkah berikutnya",
                    wrapCategory
                )
            }
        }
    } finally {
        RandomizerGuard.endGenerate()
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

    init {
        RandomizerGuard.restore(
            savedCycle = prefs.getInt("randomizer_cycle", 0),
            raw = prefs.getString("randomizer_last_used", null)
        )
    }

    fun savePlan(plan: DayPlan) {
        prefs.edit().putString("today_plan", planToJson(plan).toString()).apply()

        RandomizerGuard.markSelected(
            plan.tasks
                .filter { !it.isBreak }
                .map { it.title }
        )
        persistRandomizerState()
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

    private fun persistRandomizerState() {
        prefs.edit()
            .putInt("randomizer_cycle", RandomizerGuard.cycle)
            .putString("randomizer_last_used", RandomizerGuard.snapshotJson())
            .apply()
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
