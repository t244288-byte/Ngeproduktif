package com.nalar.reroll

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
import androidx.compose.foundation.layout.weight
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
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

val BrandOrange = Color(0xFFFC4C02)
val BrandOrangeSoft = Color(0xFFFFE7DA)
val Ink = Color(0xFF171717)
val MutedInk = Color(0xFF666666)
val AppBackground = Color(0xFFF6F6F4)
val CardWhite = Color(0xFFFFFFFF)
val Divider = Color(0xFFE7E7E3)
val SkillPurple = Color(0xFF6D5DFB)
val HealthGreen = Color(0xFF2E9D62)
val SideBlue = Color(0xFF2D6CDF)
val PersonalRose = Color(0xFFB35C7C)
val BreakGrey = Color(0xFF8A8A8A)

val NgeproduktifColors = lightColorScheme(
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

private fun categoryColor(category: String): Color = when (category) {
    "Career" -> BrandOrange
    "Skill" -> SkillPurple
    "Health" -> HealthGreen
    "Side Hustle" -> SideBlue
    "Personal" -> PersonalRose
    else -> BreakGrey
}

@Composable
fun NgeproduktifApp(store: NgeStore) {
    val context = LocalContext.current
    var selectedTab by rememberSaveable { mutableStateOf("Today") }
    var plan by remember { mutableStateOf(store.loadTodayPlan()) }
    var history by remember { mutableStateOf(store.loadHistory()) }
    var profile by remember { mutableStateOf(store.loadProfile()) }
    var setupEnergy by rememberSaveable { mutableStateOf(plan?.energy ?: "Normal") }
    var setupFocus by rememberSaveable { mutableStateOf(plan?.focus ?: profile.mainGoal) }
    var setupEndHour by rememberSaveable { mutableIntStateOf(plan?.endHour ?: 18) }

    fun persistPlan(updated: DayPlan) {
        plan = updated
        store.savePlan(updated)
        history = store.loadHistory()
        if (profile.notificationsEnabled) {
            NotificationScheduler.schedulePlan(context, updated)
        }
    }

    Scaffold(
        containerColor = AppBackground,
        bottomBar = {
            AppBottomNav(selected = selectedTab, onSelected = { selectedTab = it })
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (selectedTab) {
                "History" -> HistoryScreen(history = history)
                "Profile" -> ProfileScreen(
                    profile = profile,
                    history = history,
                    onSave = { updated ->
                        profile = updated
                        store.saveProfile(updated)
                        if (updated.notificationsEnabled) {
                            plan?.let { NotificationScheduler.schedulePlan(context, it) }
                        } else {
                            NotificationScheduler.cancelAll(context)
                        }
                    }
                )
                else -> {
                    val current = plan
                    if (current == null) {
                        SetupScreen(
                            energy = setupEnergy,
                            onEnergyChange = { setupEnergy = it },
                            focus = setupFocus,
                            onFocusChange = { setupFocus = it },
                            endHour = setupEndHour,
                            onEndHourChange = { setupEndHour = it },
                            onGenerate = {
                                persistPlan(generateDay(setupEnergy, setupFocus, setupEndHour))
                            }
                        )
                    } else {
                        TodayScreen(
                            plan = current,
                            onDone = { index ->
                                persistPlan(
                                    current.copy(tasks = current.tasks.toMutableList().also {
                                        it[index] = it[index].copy(status = TaskStatus.DONE)
                                    })
                                )
                            },
                            onSkip = { index ->
                                persistPlan(
                                    current.copy(tasks = current.tasks.toMutableList().also {
                                        it[index] = it[index].copy(status = TaskStatus.SKIPPED)
                                    })
                                )
                            },
                            onReroll = { index ->
                                if (current.rerollsUsed < 3) {
                                    val task = current.tasks[index]
                                    val alternatives = poolFor(task.category).filterNot { it == task.title }
                                    if (alternatives.isNotEmpty()) {
                                        persistPlan(
                                            current.copy(
                                                rerollsUsed = current.rerollsUsed + 1,
                                                tasks = current.tasks.toMutableList().also {
                                                    it[index] = task.copy(title = alternatives.random(), status = TaskStatus.TODO)
                                                }
                                            )
                                        )
                                    }
                                }
                            },
                            onReplan = {
                                persistPlan(generateDay(current.energy, current.focus, current.endHour))
                            },
                            onEdit = {
                                setupEnergy = current.energy
                                setupFocus = current.focus
                                setupEndHour = current.endHour
                                NotificationScheduler.cancelAll(context)
                                store.clearPlan()
                                plan = null
                            },
                            onShare = { ScheduleShare.share(context, current) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun AppBottomNav(selected: String, onSelected: (String) -> Unit) {
    NavigationBar(containerColor = CardWhite) {
        listOf("Today" to "●", "History" to "▥", "Profile" to "○").forEach { (label, symbol) ->
            NavigationBarItem(
                selected = selected == label,
                onClick = { onSelected(label) },
                icon = {
                    Text(
                        symbol,
                        color = if (selected == label) BrandOrange else MutedInk,
                        fontWeight = FontWeight.Bold
                    )
                },
                label = {
                    Text(
                        label,
                        fontWeight = if (selected == label) FontWeight.Bold else FontWeight.Medium
                    )
                }
            )
        }
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
        modifier = Modifier.fillMaxSize().background(AppBackground),
        contentPadding = PaddingValues(bottom = 28.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item { SetupHero(greeting = greeting) }
        item {
            Column(modifier = Modifier.padding(horizontal = 20.dp)) {
                Text("Build today's route", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                Text("Pilih kondisi hari ini. Ngeproduktif yang atur rutenya sampai sore.", color = MutedInk)
            }
        }
        item {
            SetupChoiceCard(Modifier.padding(horizontal = 20.dp), "01", "Energy", "Seberapa banyak tenaga hari ini?") {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("Low", "Normal", "High").forEach {
                        BrandFilterChip(selected = energy == it, text = it, onClick = { onEnergyChange(it) })
                    }
                }
            }
        }
        item {
            SetupChoiceCard(Modifier.padding(horizontal = 20.dp), "02", "Main focus", "Fokus boleh satu, jadwal tetap seimbang.") {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("Mixed", "Career", "Skill", "Side Hustle", "Health", "Personal").chunked(2).forEach { row ->
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            row.forEach { option ->
                                BrandFilterChip(
                                    modifier = Modifier.weight(1f),
                                    selected = focus == option,
                                    text = option,
                                    onClick = { onFocusChange(option) }
                                )
                            }
                        }
                    }
                }
            }
        }
        item {
            SetupChoiceCard(Modifier.padding(horizontal = 20.dp), "03", "Clock out", "Sore tetap punya waktu buat hidup.") {
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
                    modifier = Modifier.fillMaxWidth().height(60.dp),
                    shape = RoundedCornerShape(18.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = BrandOrange)
                ) {
                    Text("BUILD MY DAY  →", fontWeight = FontWeight.Bold)
                }
                Spacer(Modifier.height(10.dp))
                Text("No perfect day. Just a clear next move.", modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center, color = MutedInk)
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
            .background(Brush.linearGradient(listOf(Ink, Color(0xFF2A1B16), BrandOrange)))
            .padding(horizontal = 22.dp, vertical = 24.dp)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("NGEPRODUKTIF", color = Color.White, fontWeight = FontWeight.Bold)
                Surface(shape = CircleShape, color = Color.White.copy(alpha = .14f)) {
                    Text("v0.4", modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp), color = Color.White, fontWeight = FontWeight.Bold)
                }
            }
            Spacer(Modifier.height(26.dp))
            Text(greeting, color = Color.White.copy(alpha = .76f), style = MaterialTheme.typography.titleMedium)
            Text("Move with\na clear route.", color = Color.White, style = MaterialTheme.typography.displaySmall, fontWeight = FontWeight.Bold)
            Text("Track the day like an activity.", color = Color.White.copy(alpha = .78f))
            Spacer(Modifier.weight(1f))
            RouteIllustration(Modifier.fillMaxWidth().height(100.dp), light = true)
        }
    }
}

@Composable
private fun SetupChoiceCard(
    modifier: Modifier,
    number: String,
    title: String,
    subtitle: String,
    content: @Composable () -> Unit
) {
    Card(modifier = modifier.fillMaxWidth(), shape = RoundedCornerShape(22.dp), colors = CardDefaults.cardColors(containerColor = CardWhite)) {
        Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Column(Modifier.weight(1f)) {
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
private fun BrandFilterChip(modifier: Modifier = Modifier, selected: Boolean, text: String, onClick: () -> Unit) {
    FilterChip(
        modifier = modifier,
        selected = selected,
        onClick = onClick,
        label = {
            Text(text, modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center, fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium)
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
    plan: DayPlan,
    onDone: (Int) -> Unit,
    onSkip: (Int) -> Unit,
    onReroll: (Int) -> Unit,
    onReplan: () -> Unit,
    onEdit: () -> Unit,
    onShare: () -> Unit
) {
    var viewMode by rememberSaveable { mutableStateOf("Timeline") }
    val actionable = plan.tasks.count { !it.isBreak }
    val completed = plan.tasks.count { !it.isBreak && it.status == TaskStatus.DONE }
    val progress = if (actionable == 0) 0f else completed.toFloat() / actionable
    val now = LocalDateTime.now()
    val nowMinutes = now.hour * 60 + now.minute
    val nextTask = plan.tasks.firstOrNull { it.status == TaskStatus.TODO && !it.isBreak && it.endMinutes > nowMinutes }

    LazyColumn(
        modifier = Modifier.fillMaxSize().background(AppBackground),
        contentPadding = PaddingValues(bottom = 28.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item { TodayTopBar(onEdit = onEdit, onShare = onShare) }
        item {
            MomentumHero(
                modifier = Modifier.padding(horizontal = 20.dp),
                progress = progress,
                completed = completed,
                total = actionable,
                focus = plan.focus,
                energy = plan.energy,
                nextTask = nextTask,
                rerollsLeft = (3 - plan.rerollsUsed).coerceAtLeast(0)
            )
        }
        item {
            Row(Modifier.padding(horizontal = 20.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                BrandFilterChip(Modifier.weight(1f), viewMode == "Timeline", "Timeline") { viewMode = "Timeline" }
                BrandFilterChip(Modifier.weight(1f), viewMode == "Table", "Table") { viewMode = "Table" }
            }
        }
        item {
            Row(Modifier.fillMaxWidth().padding(horizontal = 20.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Column {
                    Text(if (viewMode == "Timeline") "Today's route" else "Today's schedule", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                    Text(if (viewMode == "Timeline") "One block at a time." else "See the whole day at a glance.", color = MutedInk)
                }
                Text("${plan.tasks.size} blocks", color = BrandOrange, fontWeight = FontWeight.Bold)
            }
        }

        if (plan.tasks.isEmpty()) {
            item {
                EmptyTimeCard(modifier = Modifier.padding(horizontal = 20.dp), onEdit = onEdit)
            }
        } else if (viewMode == "Timeline") {
            itemsIndexed(plan.tasks) { index, task ->
                TimelineTaskRow(
                    modifier = Modifier.padding(horizontal = 20.dp),
                    task = task,
                    index = index,
                    taskCount = plan.tasks.size,
                    nowMinutes = nowMinutes,
                    canReroll = plan.rerollsUsed < 3 && !task.isBreak,
                    onDone = { onDone(index) },
                    onSkip = { onSkip(index) },
                    onReroll = { onReroll(index) }
                )
            }
        } else {
            item { ScheduleTable(Modifier.padding(horizontal = 20.dp), plan.tasks, nowMinutes) }
        }

        item {
            Column(Modifier.padding(horizontal = 20.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = onShare,
                    modifier = Modifier.fillMaxWidth().height(54.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = BrandOrange)
                ) {
                    Text("SHARE SCHEDULE AS IMAGE", fontWeight = FontWeight.Bold)
                }
                OutlinedButton(onClick = onReplan, modifier = Modifier.fillMaxWidth().height(54.dp), shape = RoundedCornerShape(16.dp)) {
                    Text("RE-PLAN TODAY  ↻", fontWeight = FontWeight.Bold, color = Ink)
                }
            }
        }
    }
}

@Composable
private fun TodayTopBar(onEdit: () -> Unit, onShare: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 18.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text("NGEPRODUKTIF", fontWeight = FontWeight.Bold)
            Text(DateTimeFormatter.ofPattern("EEE, d MMM").format(LocalDateTime.now()), color = MutedInk, style = MaterialTheme.typography.bodySmall)
        }
        Row {
            TextButton(onClick = onShare) { Text("Share", color = BrandOrange, fontWeight = FontWeight.Bold) }
            TextButton(onClick = onEdit) { Text("Edit", color = Ink, fontWeight = FontWeight.SemiBold) }
        }
    }
}

@Composable
private fun MomentumHero(
    modifier: Modifier,
    progress: Float,
    completed: Int,
    total: Int,
    focus: String,
    energy: String,
    nextTask: TaskBlock?,
    rerollsLeft: Int
) {
    Box(
        modifier = modifier.fillMaxWidth().clip(RoundedCornerShape(26.dp))
            .background(Brush.linearGradient(listOf(Ink, Color(0xFF26211F), Color(0xFF3A2218))))
            .padding(20.dp)
    ) {
        Column {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Top) {
                Column {
                    Text("TODAY'S MOMENTUM", color = Color.White.copy(alpha = .62f), style = MaterialTheme.typography.labelMedium)
                    Text("${(progress * 100).toInt()}%", color = Color.White, style = MaterialTheme.typography.displayMedium, fontWeight = FontWeight.Bold)
                    Text("$completed of $total productive blocks done", color = Color.White.copy(alpha = .72f))
                }
                Surface(shape = RoundedCornerShape(14.dp), color = BrandOrange) {
                    Text("$rerollsLeft rerolls", modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp), color = Color.White, fontWeight = FontWeight.Bold)
                }
            }
            Spacer(Modifier.height(16.dp))
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier.fillMaxWidth().height(7.dp).clip(CircleShape),
                color = BrandOrange,
                trackColor = Color.White.copy(alpha = .14f)
            )
            Spacer(Modifier.height(14.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                DarkStatPill("Focus", focus)
                DarkStatPill("Energy", energy)
            }
            Spacer(Modifier.height(18.dp))
            RouteIllustration(Modifier.fillMaxWidth().height(72.dp), light = true)
            Spacer(Modifier.height(10.dp))
            Text("NEXT MOVE", color = BrandOrange, fontWeight = FontWeight.Bold)
            Text(nextTask?.title ?: "Route completed. Nice work.", color = Color.White, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            if (nextTask != null) {
                Text("${formatTime(nextTask.startMinutes)} – ${formatTime(nextTask.endMinutes)} • ${nextTask.category}", color = Color.White.copy(alpha = .62f))
            }
        }
    }
}

@Composable
private fun DarkStatPill(label: String, value: String) {
    Surface(shape = RoundedCornerShape(12.dp), color = Color.White.copy(alpha = .09f)) {
        Row(Modifier.padding(horizontal = 11.dp, vertical = 7.dp)) {
            Text("$label  ", color = Color.White.copy(alpha = .55f), style = MaterialTheme.typography.labelSmall)
            Text(value, color = Color.White, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun EmptyTimeCard(modifier: Modifier, onEdit: () -> Unit) {
    Card(modifier = modifier.fillMaxWidth(), shape = RoundedCornerShape(22.dp), colors = CardDefaults.cardColors(containerColor = CardWhite)) {
        Column(Modifier.padding(20.dp)) {
            Text("No usable time left", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
            Text("Pilih clock-out yang lebih malam, lalu build ulang hari ini.", color = MutedInk)
            TextButton(onClick = onEdit) { Text("Edit setup", color = BrandOrange, fontWeight = FontWeight.Bold) }
        }
    }
}

@Composable
private fun TimelineTaskRow(
    modifier: Modifier,
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
    Row(modifier = modifier.fillMaxWidth().heightIn(min = 122.dp), verticalAlignment = Alignment.Top) {
        Column(modifier = Modifier.width(58.dp), horizontalAlignment = Alignment.End) {
            Text(formatTime(task.startMinutes), fontWeight = if (isNow) FontWeight.Bold else FontWeight.SemiBold, color = if (isNow) BrandOrange else Ink)
            Text(formatTime(task.endMinutes), color = MutedInk, style = MaterialTheme.typography.labelSmall)
        }
        Box(modifier = Modifier.width(34.dp).height(122.dp), contentAlignment = Alignment.TopCenter) {
            Canvas(Modifier.fillMaxSize()) {
                val x = size.width / 2
                if (index > 0) drawLine(Divider, Offset(x, 0f), Offset(x, 22f), strokeWidth = 4f)
                if (index < taskCount - 1) drawLine(Divider, Offset(x, 22f), Offset(x, size.height), strokeWidth = 4f)
                drawCircle(if (isNow) BrandOrange else accent, radius = if (isNow) 10f else 8f, center = Offset(x, 22f))
                if (isNow) drawCircle(BrandOrange.copy(alpha = .18f), radius = 18f, center = Offset(x, 22f))
            }
        }
        Card(
            modifier = Modifier.weight(1f).padding(bottom = 12.dp),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = if (isNow) BrandOrangeSoft else CardWhite)
        ) {
            Column(Modifier.padding(15.dp)) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    CategoryLabel(task.category, task.isBreak)
                    StatusLabel(task, isNow)
                }
                Spacer(Modifier.height(8.dp))
                Text(task.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                Text("${task.endMinutes - task.startMinutes} min", color = MutedInk, style = MaterialTheme.typography.bodySmall)
                if (task.status == TaskStatus.TODO && !task.isBreak) {
                    Spacer(Modifier.height(12.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(7.dp)) {
                        Button(onClick = onDone, colors = ButtonDefaults.buttonColors(containerColor = Ink), shape = RoundedCornerShape(12.dp), contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp)) { Text("Done", fontWeight = FontWeight.Bold) }
                        OutlinedButton(onClick = onSkip, shape = RoundedCornerShape(12.dp), contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)) { Text("Skip", color = Ink) }
                        if (canReroll) TextButton(onClick = onReroll) { Text("Reroll ↻", color = BrandOrange, fontWeight = FontWeight.Bold) }
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
        Box(Modifier.size(8.dp).clip(CircleShape).background(color))
        Spacer(Modifier.width(7.dp))
        Text(if (isBreak) "BREAK" else category.uppercase(), color = color, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelSmall)
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
        Text(label, modifier = Modifier.padding(horizontal = 9.dp, vertical = 5.dp), color = fg, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelSmall)
    }
}

@Composable
private fun ScheduleTable(modifier: Modifier, tasks: List<TaskBlock>, nowMinutes: Int) {
    Card(modifier = modifier.fillMaxWidth(), shape = RoundedCornerShape(22.dp), colors = CardDefaults.cardColors(containerColor = CardWhite)) {
        Column(Modifier.padding(16.dp)) {
            Row(Modifier.fillMaxWidth()) {
                Text("Time", Modifier.width(76.dp), fontWeight = FontWeight.Bold)
                Text("Activity", Modifier.weight(1f), fontWeight = FontWeight.Bold)
                Text("Status", Modifier.width(58.dp), textAlign = TextAlign.End, fontWeight = FontWeight.Bold)
            }
            Spacer(Modifier.height(8.dp)); HorizontalDivider(color = Divider)
            tasks.forEachIndexed { index, task ->
                val isNow = task.status == TaskStatus.TODO && nowMinutes in task.startMinutes until task.endMinutes
                Row(Modifier.fillMaxWidth().padding(vertical = 11.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text("${formatTime(task.startMinutes)}\n${formatTime(task.endMinutes)}", Modifier.width(76.dp), color = if (isNow) BrandOrange else Ink, fontWeight = if (isNow) FontWeight.Bold else FontWeight.Normal)
                    Column(Modifier.weight(1f)) {
                        Text(task.title, fontWeight = FontWeight.Medium)
                        CategoryLabel(task.category, task.isBreak)
                    }
                    Text(
                        when (task.status) { TaskStatus.DONE -> "Done"; TaskStatus.SKIPPED -> "Skip"; TaskStatus.TODO -> if (isNow) "Now" else "Todo" },
                        Modifier.width(58.dp), textAlign = TextAlign.End, color = if (isNow) BrandOrange else MutedInk, fontWeight = FontWeight.Bold
                    )
                }
                if (index != tasks.lastIndex) HorizontalDivider(color = Divider)
            }
        }
    }
}

@Composable
private fun HistoryScreen(history: List<DayRecord>) {
    val today = LocalDate.now()
    val last7 = (6 downTo 0).map { today.minusDays(it.toLong()) }
    val records = history.associateBy { it.date }
    val weekRecords = last7.map { records[it.toString()] ?: DayRecord(it.toString(), 0, 0, 0) }
    val totalDone = weekRecords.sumOf { it.done }
    val totalMinutes = weekRecords.sumOf { it.productiveMinutes }
    val streak = calculateStreak(history)

    LazyColumn(
        modifier = Modifier.fillMaxSize().background(AppBackground),
        contentPadding = PaddingValues(20.dp, 20.dp, 20.dp, 28.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text("HISTORY", color = BrandOrange, fontWeight = FontWeight.Bold)
            Text("Your weekly momentum", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
            Text("Track consistency, not perfection.", color = MutedInk)
        }
        item { HistoryHero(streak = streak, done = totalDone, minutes = totalMinutes) }
        item {
            Card(shape = RoundedCornerShape(22.dp), colors = CardDefaults.cardColors(containerColor = CardWhite)) {
                Column(Modifier.padding(18.dp)) {
                    Text("Last 7 days", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                    Spacer(Modifier.height(16.dp))
                    WeeklyBars(records = weekRecords)
                }
            }
        }
        itemsIndexed(weekRecords.reversed()) { _, record ->
            HistoryDayRow(record)
        }
    }
}

@Composable
private fun HistoryHero(streak: Int, done: Int, minutes: Int) {
    Box(
        Modifier.fillMaxWidth().clip(RoundedCornerShape(26.dp))
            .background(Brush.linearGradient(listOf(Ink, Color(0xFF3A2218), BrandOrange)))
            .padding(20.dp)
    ) {
        Column {
            Text("CURRENT STREAK", color = Color.White.copy(alpha = .65f), fontWeight = FontWeight.Bold)
            Text("$streak days", color = Color.White, style = MaterialTheme.typography.displayMedium, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(18.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                DarkStatPill("Done", "$done blocks")
                DarkStatPill("Time", formatMinutes(minutes))
            }
        }
    }
}

@Composable
private fun WeeklyBars(records: List<DayRecord>) {
    val maxDone = (records.maxOfOrNull { it.done } ?: 0).coerceAtLeast(1)
    Canvas(modifier = Modifier.fillMaxWidth().height(150.dp)) {
        val gap = size.width / records.size
        records.forEachIndexed { index, record ->
            val ratio = record.done.toFloat() / maxDone
            val barHeight = 105f * ratio
            val left = gap * index + gap * .22f
            val right = gap * index + gap * .78f
            drawRoundRect(
                color = if (record.done > 0) BrandOrange else Color(0xFFE4E4E0),
                topLeft = Offset(left, size.height - barHeight - 24f),
                size = androidx.compose.ui.geometry.Size(right - left, barHeight.coerceAtLeast(8f)),
                cornerRadius = CornerRadius(12f, 12f)
            )
        }
    }
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        records.forEach { record ->
            val date = LocalDate.parse(record.date)
            Text(date.format(DateTimeFormatter.ofPattern("EE")).take(2), modifier = Modifier.weight(1f), textAlign = TextAlign.Center, color = MutedInk, style = MaterialTheme.typography.labelSmall)
        }
    }
}

@Composable
private fun HistoryDayRow(record: DayRecord) {
    val date = LocalDate.parse(record.date)
    Card(shape = RoundedCornerShape(18.dp), colors = CardDefaults.cardColors(containerColor = CardWhite)) {
        Row(Modifier.fillMaxWidth().padding(16.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Column {
                Text(date.format(DateTimeFormatter.ofPattern("EEEE")), fontWeight = FontWeight.Bold)
                Text(date.format(DateTimeFormatter.ofPattern("d MMM yyyy")), color = MutedInk, style = MaterialTheme.typography.bodySmall)
            }
            Column(horizontalAlignment = Alignment.End) {
                Text("${record.done}/${record.total} done", fontWeight = FontWeight.Bold, color = if (record.done > 0) BrandOrange else MutedInk)
                Text(formatMinutes(record.productiveMinutes), color = MutedInk, style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}

@Composable
private fun ProfileScreen(profile: UserProfile, history: List<DayRecord>, onSave: (UserProfile) -> Unit) {
    var name by remember(profile.name) { mutableStateOf(profile.name) }
    var headline by remember(profile.headline) { mutableStateOf(profile.headline) }
    var mainGoal by remember(profile.mainGoal) { mutableStateOf(profile.mainGoal) }
    var notifications by remember(profile.notificationsEnabled) { mutableStateOf(profile.notificationsEnabled) }
    val streak = calculateStreak(history)
    val totalDone = history.sumOf { it.done }
    val activeDays = history.count { it.done > 0 }

    LazyColumn(
        modifier = Modifier.fillMaxSize().background(AppBackground),
        contentPadding = PaddingValues(20.dp, 20.dp, 20.dp, 28.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            ProfileHero(name = name.ifBlank { "You" }, headline = headline, streak = streak)
        }
        item {
            Card(shape = RoundedCornerShape(22.dp), colors = CardDefaults.cardColors(containerColor = CardWhite)) {
                Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Your profile", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                    OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Name") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                    OutlinedTextField(value = headline, onValueChange = { headline = it }, label = { Text("Headline") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                    Text("Main goal", fontWeight = FontWeight.Bold)
                    listOf("Mixed", "Career", "Skill", "Side Hustle", "Health", "Personal").chunked(2).forEach { row ->
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            row.forEach { option ->
                                BrandFilterChip(Modifier.weight(1f), mainGoal == option, option) { mainGoal = option }
                            }
                        }
                    }
                }
            }
        }
        item {
            Card(shape = RoundedCornerShape(22.dp), colors = CardDefaults.cardColors(containerColor = CardWhite)) {
                Row(Modifier.fillMaxWidth().padding(18.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text("Next Move notifications", fontWeight = FontWeight.Bold)
                        Text("Get a notification when the next productive block starts.", color = MutedInk, style = MaterialTheme.typography.bodySmall)
                    }
                    Switch(checked = notifications, onCheckedChange = { notifications = it })
                }
            }
        }
        item {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                ProfileStatCard(Modifier.weight(1f), "$streak", "day streak")
                ProfileStatCard(Modifier.weight(1f), "$totalDone", "blocks done")
                ProfileStatCard(Modifier.weight(1f), "$activeDays", "active days")
            }
        }
        item {
            Button(
                onClick = { onSave(UserProfile(name.ifBlank { "You" }, headline.ifBlank { "Building momentum" }, mainGoal, notifications)) },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = BrandOrange)
            ) {
                Text("SAVE PROFILE", fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun ProfileHero(name: String, headline: String, streak: Int) {
    Box(
        Modifier.fillMaxWidth().clip(RoundedCornerShape(26.dp))
            .background(Brush.linearGradient(listOf(Ink, Color(0xFF26211F), Color(0xFF3A2218))))
            .padding(20.dp)
    ) {
        Column {
            Surface(shape = CircleShape, color = BrandOrange, modifier = Modifier.size(58.dp)) {
                Box(contentAlignment = Alignment.Center) {
                    Text(name.take(1).uppercase(), color = Color.White, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
                }
            }
            Spacer(Modifier.height(16.dp))
            Text(name, color = Color.White, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
            Text(headline, color = Color.White.copy(alpha = .68f))
            Spacer(Modifier.height(16.dp))
            Text("$streak DAY STREAK", color = BrandOrange, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun ProfileStatCard(modifier: Modifier, value: String, label: String) {
    Card(modifier = modifier, shape = RoundedCornerShape(18.dp), colors = CardDefaults.cardColors(containerColor = CardWhite)) {
        Column(Modifier.padding(14.dp)) {
            Text(value, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            Text(label, color = MutedInk, style = MaterialTheme.typography.labelSmall)
        }
    }
}

private fun formatMinutes(minutes: Int): String {
    if (minutes <= 0) return "0m"
    val h = minutes / 60
    val m = minutes % 60
    return if (h > 0) "${h}h ${m}m" else "${m}m"
}

@Composable
private fun RouteIllustration(modifier: Modifier, light: Boolean) {
    val routeColor = if (light) Color.White.copy(alpha = .78f) else BrandOrange
    val ghost = if (light) Color.White.copy(alpha = .12f) else BrandOrange.copy(alpha = .10f)
    Canvas(modifier) {
        val path = Path().apply {
            moveTo(size.width * .03f, size.height * .74f)
            cubicTo(size.width * .20f, size.height * .15f, size.width * .36f, size.height * .92f, size.width * .52f, size.height * .40f)
            cubicTo(size.width * .66f, size.height * .02f, size.width * .80f, size.height * .88f, size.width * .97f, size.height * .18f)
        }
        drawPath(path, ghost, style = Stroke(width = 13f, cap = StrokeCap.Round))
        drawPath(path, routeColor, style = Stroke(width = 5f, cap = StrokeCap.Round))
        listOf(
            Offset(size.width * .04f, size.height * .72f),
            Offset(size.width * .34f, size.height * .61f),
            Offset(size.width * .54f, size.height * .36f),
            Offset(size.width * .77f, size.height * .54f),
            Offset(size.width * .96f, size.height * .20f)
        ).forEachIndexed { index, point ->
            drawCircle(if (index == 4) BrandOrange else routeColor, radius = if (index == 4) 9f else 6f, center = point)
        }
    }
}
