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
        versionCode = 6
        versionName = "0.4.2"
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

// Hotfix retained from v0.4.1: equal-width setup chips on phones.
val patchSetupChipLayout by tasks.registering {
    doLast {
        val source = file("src/main/java/com/nalar/reroll/AppUi.kt")
        if (!source.exists()) return@doLast

        var code = source.readText()

        code = code.replace(
            """listOf(\"Low\", \"Normal\", \"High\").forEach {\n                        BrandFilterChip(selected = energy == it, text = it, onClick = { onEnergyChange(it) })\n                    }""",
            """listOf(\"Low\", \"Normal\", \"High\").forEach { option ->\n                        BrandFilterChip(\n                            modifier = Modifier.weight(1f),\n                            selected = energy == option,\n                            text = option,\n                            onClick = { onEnergyChange(option) }\n                        )\n                    }"""
        )

        code = code.replace(
            """BrandFilterChip(\n                            selected = endHour == hour,\n                            text = String.format(\"%02d:00\", hour),\n                            onClick = { onEndHourChange(hour) }\n                        )""",
            """BrandFilterChip(\n                            modifier = Modifier.weight(1f),\n                            selected = endHour == hour,\n                            text = String.format(\"%02d:00\", hour),\n                            onClick = { onEndHourChange(hour) }\n                        )"""
        )

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
