# ShadeQS (SuperShade) — Developer & AI Guidelines

Custom notification shade replacement for Android (`com.supershade`, v1.9.36, SDK 29–36, JDK 21).
Stack: Kotlin 2.1 + Jetpack Compose (BOM 2026.02.00) + Koin + Shizuku + DataStore.
Path: `/data/data/com.termux/files/home/ShadeQS/`

## Build & Test Commands

| Task | Command |
|------|---------|
| Compile check (fast, no APK) | `bash scripts/dev/check-build.sh` |
| Build + install debug APK via ADB | `bash scripts/dev/build-install-debug.sh` |
| Full debug APK | `./gradlew :app:assembleDebug` |
| Open shade via ADB (test UI) | `adb shell am broadcast -a com.supershade.action.OPEN_SHADE -n com.supershade.debug/com.supershade.service.ShadeOpenReceiver` |
| Verify active service | `adb shell dumpsys activity services \| grep ShadeService` |

CI (`.github/workflows/`): `build.yml` runs on push to `main`/`develop`; `release.yml` builds & publishes APK on `v*` tag push.

## Architecture & Package Map (`app/src/main/java/com/supershade/`)

| Layer | Key Files & Responsibility |
|-------|----------------------------|
| **Entry & DI** | `MainActivity.kt`, `SuperShadeApplication.kt`, `di/AppModule.kt` (Koin singletons) |
| **Overlay & Services** | `overlay/ShadeWindowManager.kt` (`TYPE_APPLICATION_OVERLAY` + `ShadeLifecycleOwner` + status bar touch blocker), `overlay/GestureOverlay.kt`, `service/ShadeService.kt` (FGS), `service/SuperShadeAccessibilityService.kt` (`TYPE_ACCESSIBILITY_OVERLAY` at layer ~33, horizontal brightness scrub, Good Lock OHO+ key/event intercept), `service/HeadsUpOverlay.kt` (2D peek banners + `AmbientMediaCard` with `FLAG_SHOW_WHEN_LOCKED`), `service/NotificationCollector.kt` (NLS), `service/ShadeOpenReceiver.kt`, `service/SuperShadeTileService.kt` |
| **State & Settings** | `viewmodel/ShadeViewModel.kt`, `viewmodel/ShadeState.kt`, `settings/ShadeSettings.kt` (DataStore `supershade_prefs`) |
| **Shizuku IPC** | `shizuku/StatusBarGovernor.kt` (`cmd statusbar`, `svc` toggles), `shizuku/ShadeCommanderService.java` (pure Java `@Keep` UserService), `shizuku/ShizukuPlusConnector.kt` |
| **Notifications** | `domain/notification/NotificationRepository.kt` (active list, pins, snooze, undo, 60-item ring buffer history), `CategoryEngine.kt` (5 independent `ClassificationMode`s: `ONE_UI`, `AOSP`, `ESSENTIAL`, `CYBERPUNK`, `UNIFIED` + per-app overrides), `AdaptivePriorityLoop.kt` (autonomous interaction scoring/decay), `OtpDetector.kt` (OTP copy chip + VIP badge) |
| **Quick Settings** | `domain/tile/TileDefinition.kt` (46+ `CANONICAL_TILES`, vendor alias normalizer, label humanizer), `TileRepository.kt` (0ms reactive observers), `TileToggler.kt` (Shizuku + standard API fallback) |
| **Media, Audio, System** | `domain/media/MediaRepository.kt`, `domain/audio/AudioRepository.kt` (5-stream mixer: Media, Ring, Notif, System, Alarm), `domain/brightness/BrightnessRepository.kt`, `domain/system/SystemStatusRepository.kt`, `haptics/SuperHaptics.kt` (Samsung LRA `semCreateWaveform` 50025–50151 + Android R+ primitives) |
| **UI & Themes** | `ui/shade/ShadeRoot.kt` (120Hz lock, velocity fling, Separate/Together pager), `ui/shade/*` (cards, sliders, detail/history sheets), `ui/theme/*` (`OneUI`, `Pixel`, `Nothing`, `Cyberpunk`, `PureMaterial`, `MonetScheme.kt` strength lerp, `BackdropTheme.kt`) |

## Critical Architecture Rules (Never Violate)

1. **`ShadeViewModel` must stay `single {}` in `AppModule.kt` (never `viewModel {}`)**: It is shared between `ShadeService`, `SuperShadeAccessibilityService`, `ShadeWindowManager`, and `MainActivity`. Separate instances break shade open/close and state sync.
2. **Fresh `ShadeLifecycleOwner` on every `ShadeWindowManager.show()`**: Android `LifecycleRegistry` cannot transition from `DESTROYED` back to `RESUMED`. Never reuse a destroyed owner across `show()`/`hide()` cycles.
3. **`ShadeCommanderService.java` must remain pure Java with `@Keep` constructors**: `ShizukuServiceStarter` instantiates it via reflection in a separate process; converting to Kotlin causes `InstantiationException` on secondary dex linkage. Always guard Shizuku calls with `connector.hasPermission()`.
4. **Preserve 120Hz & main-thread responsiveness**: Keep `StatusBarGovernor` shell calls and `BrightnessRepository.set()` off the main thread (`Dispatchers.IO`). Update `app/src/main/baseline-prof.txt` if renaming core overlay/UI classes.

## Git & Releases
- Stage files explicitly by name (`git add <file>`), never `git add -A`.
- Tag `v*` (e.g. `git tag v1.9.37 && git push origin v1.9.37`) to trigger `release.yml`.
