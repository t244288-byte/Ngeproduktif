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
        versionCode = 7
        versionName = "0.4.3"
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

// v0.4.3 UI hotfix. BrandFilterChip makes its label fill the chip width,
// so chips in a Row must have explicit weights. We also expose evening
// clock-out choices through 23:00 so the app remains usable later at night.
val patchSetupChipLayout by tasks.registering {
    doLast {
        val source = file("src/main/java/com/nalar/reroll/AppUi.kt")
        if (!source.exists()) return@doLast

        var code = source.readText()

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
        code = code.replace("Text(\"v0.4\"", "Text(\"v0.4.3\"")

        check(code.contains("modifier = Modifier.weight(1f), selected = energy == it")) {
            "Energy chip hotfix did not apply"
        }
        check(code.contains("listOf(16, 17, 18, 19, 20, 21, 22, 23).chunked(4)")) {
            "Clock-out hotfix did not apply"
        }

        source.writeText(code)
    }
}

tasks.configureEach {
    if (name.startsWith("compile") && name.endsWith("Kotlin")) {
        dependsOn(patchSetupChipLayout)
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
