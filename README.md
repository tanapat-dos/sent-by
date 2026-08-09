# ReelShelf

Shared Clips Inbox — Android local MVP (`com.reelshelf.app`).

## Requirements

- JDK 17+
- Android SDK Platform 35
- Android Studio Ladybug+ or command-line Gradle

## Setup

1. Install JDK 17 and point `JAVA_HOME` at it.
2. Install the Android SDK (or Android Studio).
3. Create `local.properties` with your SDK path:

```properties
sdk.dir=C\:\\Users\\<you>\\AppData\\Local\\Android\\Sdk
```

## Build and run

```powershell
.\gradlew.bat assembleDebug
.\gradlew.bat installDebug
.\gradlew.bat testDebugUnitTest
```

Debug APK: `app/build/outputs/apk/debug/app-debug.apk`

## Web demo (Netlify)

A browser version lives in [`web/`](web/):

```powershell
cd web
npm install
npm run dev
```

Deploy: set Netlify base directory to `web`, build `npm run build`, publish `dist`. See [web/README.md](web/README.md).

## What works in the MVP

- Share `text/plain` from other apps into ReelShelf (quick save + sender)
- Paste links in-app
- Canonical URL dedupe with per-sender share records
- Inbox filters: Unwatched / Watched / Needs reply / Completed
- Search by sender, platform, title, URL
- Watch toggle (auto on open) and per-sender reply tracking
- Reply presets / recent customs → clipboard + always choose chat app
- Categories create/assign/filter
- Favorite senders (star) pinned in quick save
- Batch catch-up for unwatched or needs-reply
- YouTube oEmbed metadata when online; URL/placeholder fallback otherwise
- Local-only Room storage; privacy screen in-app

## Known limitations

- LINE/Messenger payload behavior still needs device confirmation (`docs/FEASIBILITY.md`)
- Live preview metadata is intentionally disabled until platforms are allow-listed; clips save with URL-only fallback
- Reply shortcuts do not deep-link into the original chat
- No cloud sync, iOS, or AI features
- 10k-clip scroll performance should be validated on a real device

## Docs

- Product requirements: `REQUIREMENTS.md`
- Task tracker: `TASKS.md`
- Feasibility notes: `docs/FEASIBILITY.md`
