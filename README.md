# Ngeproduktif v0.1 MVP

Ngeproduktif is an Android MVP for people who wake up with free time but no structure. Instead of asking the user to build a to-do list, Ngeproduktif creates a controlled-randomized schedule from the current time until a chosen clock-out time.

Core idea: reduce decision fatigue. The user chooses energy, goals, and end time, then the app builds a realistic day with work blocks, breaks, lunch, and a daily wrap-up.

## MVP features

- Current-time-aware day planning.
- Energy: Low / Normal / High.
- Goals: Career, Skill, Health, Personal, Side Hustle, Mixed.
- Controlled randomization rather than arbitrary task shuffling.
- Automatic lunch and phone-free breaks.
- Supports **Done**, **Skip**, and up to **3 Rerolls/day**.
- Re-plan the remaining day when the schedule slips.
- Offline local persistence.
- No account or backend required for v0.1.

## Tech

Kotlin + Jetpack Compose, Android native.

## Build

The repository includes a GitHub Actions workflow at `.github/workflows/build-apk.yml`. A push to `main` builds a debug APK and uploads it as an Actions artifact named `Ngeproduktif-v0.1-APK`.
