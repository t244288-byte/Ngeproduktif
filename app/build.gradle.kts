plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.compose")
}

android {
    namespace = "com.nalar.reroll"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.nalar.reroll"
        minSdk = 26
        targetSdk = 35
        versionCode = 8
        versionName = "0.4.4"
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }
}

// v0.4.4 source patch:
// - retain equal-width Energy chips and extended Clock-out choices
// - add Seek Job to setup/profile UI and category styling
// - route the randomizer through ActivityLibraryV3 (60 Seek Job activities)
// - include Seek Job inside Mixed mode
val patchV044Sources by tasks.registering {
    doLast {
        val ui = file("src/main/java/com/nalar/reroll/AppUi.kt")
        val core = file("src/main/java/com/nalar/reroll/Core.kt")
        val main = file("src/main/java/com/nalar/reroll/MainActivity.kt")

        if (ui.exists()) {
            var code = ui.readText()

            code = code.replace(
                "BrandFilterChip(selected = energy == it, text = it, onClick = { onEnergyChange(it) })",
                "BrandFilterChip(modifier = Modifier.weight(1f), selected = energy == it, text = it, onClick = { onEnergyChange(it) })"
            )

            val oldClock = """Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf(16, 17, 18, 19).forEach { hour ->
                        BrandFilterChip(
                            selected = endHour == hour,
                            text = String.format("%02d:00", hour),
                            onClick = { onEndHourChange(hour) }
                        )
                    }
                }"""

            val newClock = """Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf(16, 17, 18, 19, 20, 21, 22, 23).chunked(4).forEach { row ->
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            row.forEach { hour ->
                                BrandFilterChip(
                                    modifier = Modifier.weight(1f),
                                    selected = endHour == hour,
                                    text = String.format("%02d:00", hour),
                                    onClick = { onEndHourChange(hour) }
                                )
                            }
                        }
                    }
                }"""
            code = code.replace(oldClock, newClock)

            code = code.replace(
                "listOf(\"Mixed\", \"Career\", \"Skill\", \"Side Hustle\", \"Health\", \"Personal\")",
                "listOf(\"Mixed\", \"Seek Job\", \"Career\", \"Skill\", \"Side Hustle\", \"Health\", \"Personal\")"
            )

            code = code.replace(
                "private fun categoryColor(category: String): Color = when (category) {\n    \"Career\" -> BrandOrange",
                "private fun categoryColor(category: String): Color = when (category) {\n    \"Seek Job\" -> Color(0xFF0F9D8A)\n    \"Career\" -> BrandOrange"
            )

            code = code.replace(
                "\"Fokus boleh satu, jadwal tetap seimbang.\"",
                "\"Pilih mode aktivitas utama hari ini.\""
            )

            code = code.replace("Text(\"v0.4\"", "Text(\"v0.4.4\"")

            check(code.contains("modifier = Modifier.weight(1f), selected = energy == it")) {
                "Energy chip fix did not apply"
            }
            check(code.contains("listOf(16, 17, 18, 19, 20, 21, 22, 23).chunked(4)")) {
                "Clock-out fix did not apply"
            }
            check(code.contains("\"Seek Job\"")) {
                "Seek Job UI patch did not apply"
            }

            ui.writeText(code)
        }

        if (core.exists()) {
            var code = core.readText()
            code = code.replace("activityPoolV2(category)", "activityPoolV3(category)")
            code = code.replace(
                "\"Career\" -> listOf(\"Career\")\n    \"Skill\" -> listOf(\"Skill\")",
                "\"Seek Job\" -> listOf(\"Seek Job\")\n    \"Career\" -> listOf(\"Career\")\n    \"Skill\" -> listOf(\"Skill\")"
            )
            code = code.replace(
                "else -> listOf(\"Career\", \"Skill\", \"Side Hustle\", \"Health\", \"Personal\")",
                "else -> listOf(\"Seek Job\", \"Career\", \"Skill\", \"Side Hustle\", \"Health\", \"Personal\")"
            )

            check(code.contains("\"Seek Job\" -> listOf(\"Seek Job\")")) {
                "Seek Job focus routing patch did not apply"
            }
            check(!code.contains("activityPoolV2(category)")) {
                "Core still uses V2 activity pool"
            }

            core.writeText(code)
        }

        if (main.exists()) {
            var code = main.readText()
            code = code.replace("totalActivityCountV2()", "totalActivityCountV3()")
            code = code.replace("activityCountV2(\"Career\")", "activityCountV3(\"Career\")")
            main.writeText(code)
        }
    }
}

tasks.configureEach {
    if (name.startsWith("compile") && name.endsWith("Kotlin")) {
        dependsOn(patchV044Sources)
    }
}

dependencies {
    implementation(platform("androidx.compose:compose-bom:2024.12.01"))
    implementation("androidx.core:core-ktx:1.15.0")
    implementation("androidx.activity:activity-compose:1.10.0")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.7")
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.foundation:foundation")
    implementation("androidx.compose.material3:material3")
    debugImplementation("androidx.compose.ui:ui-tooling")
}
