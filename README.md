# Music Player App

This is a music player application built with Kotlin and Jetpack Compose.

## Project Structure

- `app/build.gradle`: App-level build configuration.
- `settings.gradle`: Project-level settings.
- `build.gradle`: Top-level build configuration.
- `app/src/main/AndroidManifest.xml`: App manifest file.
- `app/src/main/java/com/example/musicplayer/MainActivity.kt`: The main entry point of the application with a basic Music Player UI.
- `app/src/main/java/com/example/musicplayer/ui/theme/Typography.kt`: Custom typography settings for the app.
- `app/src/main/res/values/`: Resource files for strings, themes, and colors.

## Fonts

To use the **Vazirmatn** font:
1. Download the font files (`vazirmatn_regular.ttf`, `vazirmatn_medium.ttf`, `vazirmatn_bold.ttf`).
2. Place them in `app/src/main/res/font/`.
3. Uncomment the `Vazirmatn` FontFamily definition in `Typography.kt`.
