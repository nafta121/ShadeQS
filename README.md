# ShadeQS (SuperShade)

[![Latest Release](https://img.shields.io/github/v/release/nafta121/ShadeQS?style=flat-square&color=36BCF7&labelColor=1a1b27&label=Latest)](https://github.com/nafta121/ShadeQS/releases/latest)
[![Platform](https://img.shields.io/badge/Android-10%2B%20(API%2029--36)-3DDC84?style=flat-square&logo=android&labelColor=1a1b27)](https://developer.android.com)
[![Kotlin](https://img.shields.io/badge/Kotlin-2.1-7F52FF?style=flat-square&logo=kotlin&labelColor=1a1b27)](https://kotlinlang.org)
[![Compose](https://img.shields.io/badge/Jetpack%20Compose-2026.02.00-4285F4?style=flat-square&logo=jetpackcompose&labelColor=1a1b27)](https://developer.android.com/jetpack/compose)

**ShadeQS** (`com.supershade` / **SuperShade**) adalah pengganti *Notification Shade* & *Quick Settings* kustom penuh untuk Android yang dibangun dengan **Jetpack Compose**, **Koin**, dan **Shizuku** — menghadirkan pengalaman kelas *flagship* tanpa memerlukan akses root.

Tarik ke bawah dari status bar untuk membuka panel kontrol yang menggabungkan Quick Settings interaktif, pusat notifikasi pintar dengan AI *Adaptive Priority Loop*, ekstraksi OTP otomatis, kontrol media multi-tema, serta *slider* kecerahan & volume *spring-physics* berkecepatan 120Hz.

---

## ✨ Fitur Utama

### 🎨 5 Tema OS Flagship & Kustomisasi Visual Mendalam
Beralih secara *real-time* di antara 5 bahasa desain sistem operasi yang berbeda:
- **Samsung One UI 9**: Geometri *squircle* 24dp, *wide connectivity cards* (Wi-Fi & Bluetooth), *pill media scrubber* dengan *haptic detent*, dan tombol *Media Output / Device Control*.
- **Google Pixel (Android 16 M3 Expressive)**: Permukaan tonal tanpa batas (*borderless*), indikator baterai *stadium capsule*, *sinusoidal wavy seekbar* yang bergerak saat musik diputar, dan animasi pegas ekspresif.
- **Nothing OS 3.0**: Kontainer monokrom geometris dengan *wireframe border* 1dp, tipografi *dot-matrix* (`DotMatrixClock`), dan aksen titik merah ikonik (`#D71920`).
- **Cyberpunk 2077 HUD**: Sudut terpotong (`ChamferedCornerShape`), *border* neon cyan (`#00F0FF`) & magenta, telemetri *monospace* (`[PWR // 100%]`, `[SEARCH // TELEMETRY]`), dan pemindai progres siber.
- **Pure Material 3**: Desain bersih berbasis *Dynamic Color* (Material You / Monet) dari *wallpaper* sistem.

Didukung oleh:
- **4 Mode Tema Gelap**: `System`, `Dark`, `Light`, dan `AMOLED Pitch Black (#000000)`.
- **6 Pilihan Warna Aksen**: `Galaxy Blue`, `Emerald`, `Violet`, `Amber`, `Coral`, dan `Dynamic Monet` dengan **Monet Accent Strength Slider (0%–100%)**.
- **5 Tema Latar Belakang (Backdrop)**: `Opaque`, `Blurry`, `Frosted Glass`, `Liquid Glass`, dan `Transparent` dengan *compositor blur* Android 12+ (`FLAG_BLUR_BEHIND`).
- **9 Bentuk Tile (Tile Shapes)**: `Squircle`, `Rounded`, `Circle`, `Stadium Pill`, `Soft Minimal`, `Asymmetric Leaf`, `Sharp Modern`, `Clover 4-Leaf`, dan `M3E SoftBurst`.

### 🔔 Sistem Notifikasi Pintar & Self-Correcting Loop
- **Adaptive Priority Loop (`AdaptivePriorityLoop`)**: Mesin evaluasi mandiri bergaya *feedback loop* yang mempelajari interaksi pengguna (*click* `+2.5`, *inline reply* `+4.0`, *pin* `+5.0`, *snooze* `+0.8`, *fast-dismiss* `-1.8`). Secara otomatis menurunkan aplikasi *spam* ke bagian `Silent` dan mempromosikan kanal penting ke `Alerting` / `Essential`.
- **5 Strategi Klasifikasi Independen (`ClassificationMode`)**: Dapat dipilih bebas terlepas dari tema visual aktif:
  1. `ONE_UI` (Messages, Social, Email, Calls, Tasks, Media, Alarms, System, Apps)
  2. `AOSP` (Conversations, Alerting, Silent)
  3. `ESSENTIAL` (Nothing OS VIP/Essential vs General)
  4. `CYBERPUNK` (Comms, Task Cycles, Audio Feed, Net Kernel)
  5. `UNIFIED` (Urutan kronologis tanpa tab kategori)
- **Smart OTP / 2FA & Deteksi VIP (`OtpDetector`)**: Mengekstrak kode verifikasi 4–8 digit, kode berstrip (`123-456`), dan kode berprefiks (`G-123456`) secara otomatis lalu menampilkan *chip* **"Copy code"** sekali ketuk, serta menandai peringatan keamanan mendesak dengan *badge* **VIP Alert**.
- **In-Shade Live Search & Contextual Clear**: Pencarian instan di dalam panel berdasarkan judul, isi, nama paket, atau tombol aksi, dilengkapi tombol `Clear (N)` yang hanya menghapus hasil pencarian tanpa menyentuh notifikasi yang di-*pin*.
- **Pin, Undo Pill, Snooze Manager & History Sheet**:
  - **Pin to Top**: Sematkan notifikasi penting di urutan teratas (kebal terhadap *Clear All*).
  - **Undo Bar**: Batalkan penghapusan notifikasi dalam waktu 3,5 detik.
  - **Contextual Snooze**: Tunda notifikasi selama `15m`, `1h`, `2h`, `This Evening (6 PM)`, atau `Tomorrow Morning (9 AM)`.
  - **Notification History Sheet**: Simpan hingga 60 riwayat notifikasi yang telah dihapus lengkap dengan filter pencarian.

### ⚡ Quick Settings & Kontrol Sistem
- **46+ Katalog Tile Kanonis**: Mencakup Wi-Fi, Bluetooth, Mobile Data, Airplane Mode, Hotspot, NFC, Flashlight (dengan pengaturan *Torch Strength Level*), Auto-Rotate, DND, Sound Mode, Battery Saver, Location, Dark Mode, Extra Dim, Dolby Atmos, Always On Display, Samsung DeX, Smart View, Quick Share, Caffeinate, Power Menu, Screenshot, serta dukungan penuh untuk *tile* pihak ketiga (`custom(...)`).
- **In-Shade Detail Sheets (`QuickTileDetailSheet`)**:
  - **Wi-Fi & Bluetooth**: Lihat SSID, kecepatan tautan, RSSI, dan hubungkan langsung ke perangkat Bluetooth yang dipasangkan.
  - **Multi-Stream Volume Mixer**: Kontrol penuh atas 5 jalur audio (`Media`, `Ring`, `Notification`, `System`, dan `Alarm`) serta pemilih mode suara (`Sound`, `Vibrate`, `Mute`).
- **Drag-to-Reorder**: Susun ulang posisi *tile* langsung di dalam *shade* dengan umpan balik *tactile arc* (`tileGrab`, `segmentTick`, `tileDrop`).

### 🖐️ Gestur 120Hz, Haptics LRA & Integrasi Samsung Good Lock
- **Kunci 120Hz & Fisika Fling Momentum**: Mengunci *refresh rate* *overlay* pada kecepatan tertinggi layar (120Hz) dan menggunakan `VelocityTracker` untuk mendeteksi ayunan cepat ke bawah (`> 750dp/s`).
- **6 Mode Split Gesture Status Bar**: `Right 30% (70/30)`, `Half & Half (50/50)`, `Left-Handed (30/70)`, `Notifications Only`, `Quick Settings Only`, dan `Together (One UI style)`.
- **Status Bar Horizontal Brightness Scrub**: Geser secara horizontal di sepanjang bilah status untuk mengatur kecerahan layar (4%–100%) secara langsung tanpa harus membuka *shade*.
- **SuperHaptics Engine**: Dirancang khusus untuk aktuator LRA sumbu-X seri Samsung Galaxy S22–S26 Ultra (melalui refleksi `semCreateWaveform` indeks `50025`–`50151`) dan *primitive composition* Android 11–16.
- **Integrasi Ekosistem**: Mendukung penuh Samsung Good Lock **One Hand Operation+ (OHO+)**, Edge Panels, Bixby Routines, dan Tasker melalui `ShadeOpenReceiver` dan *activity shortcuts*.

---

## 📚 Dokumentasi Proyek

| Dokumen | Deskripsi |
|---------|-----------|
| [**CLAUDE.md**](CLAUDE.md) | Panduan arsitektur, peta direktori, perintah *build*, dan aturan kritis untuk pengembangan. |
| [**AI_DEVLOG.md**](AI_DEVLOG.md) | Catatan riwayat sesi pengembangan AI dan *backlog* fitur. |
| [**CHANGELOG.md**](CHANGELOG.md) | Riwayat perubahan versi dari `v1.0.0` hingga `v1.9.36`. |

---

## 📋 Persyaratan Sistem

- **OS**: Android 10 / API 29 ke atas (Android 12+ / One UI 6–9 direkomendasikan untuk fitur *compositor blur* dan *Dynamic Monet*).
- **Izin Standar**:
  - **Notification Access** (`BIND_NOTIFICATION_LISTENER_SERVICE`) — membaca, membalas, dan mengelola notifikasi.
  - **Display Over Other Apps** (`SYSTEM_ALERT_WINDOW`) — menampilkan panel *shade* dan *heads-up banner*.
  - **Accessibility Service** (`SuperShadeAccessibilityService`) — mencegat panel bawaan secara *zero-flicker* dan menangkap gestur status bar di lapisan *window* teratas.
  - **Modify System Settings** (`WRITE_SETTINGS`) — mengatur kecerahan layar dan rotasi otomatis.
- **Opsional / Direkomendasikan**:
  - **[Shizuku](https://github.com/RikkaApps/Shizuku)** (ADB atau Root) — diperlukan untuk menekan ekspansi status bar bawaan (`cmd statusbar send-disable-flag statusbar-expansion`) dan mengeksekusi *toggle* perangkat keras terprivilese (Wi-Fi, Data Seluler, NFC, Hotspot, Battery Saver).

---

## 🛠️ Membangun dari Kode Sumber (Build from Source)

Proyek ini menggunakan **JDK 21** dan **Android SDK 36**.

```bash
# 1. Clone repositori
git clone https://github.com/nafta121/ShadeQS.git
cd ShadeQS

# 2. Pemeriksaan kompilasi Kotlin secara cepat (tanpa membuat APK)
bash scripts/dev/check-build.sh

# 3. Build Debug APK dan pasang langsung via ADB Loopback (127.0.0.1:5555)
bash scripts/dev/build-install-debug.sh

# 4. Atau build APK secara manual menggunakan Gradle
./gradlew :app:assembleDebug
```

Verifikasi bahwa layanan berjalan setelah instalasi:
```bash
adb shell dumpsys activity services | grep ShadeService
```

---

## 📄 Lisensi

Dirilis di bawah lisensi **Apache 2.0**. Dibangun dengan [Jetpack Compose](https://developer.android.com/jetpack/compose), [Koin](https://insert-koin.io/), [Shizuku](https://github.com/RikkaApps/Shizuku), dan [DataStore](https://developer.android.com/topic/libraries/architecture/datastore).
