# Launcher icon source

`jiqiao-jianghu-launcher-master.png` is the canonical launcher artwork for the app.

The production launcher resources generated from it live in
`app/src/main/res/mipmap-*`. Because they are in the `main` source set, debug,
demo, acceptance, and release builds all inherit the same icon automatically.
Normal app builds and version updates do not require manual icon replacement.

The adaptive icon XML files deliberately omit a `monochrome` layer so Android
themed icons cannot fall back to the previous Android Studio “A” artwork.
Only replace these resources when intentionally changing the app brand icon.
