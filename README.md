Proyek Skripsi: SeedLock
Implementasi dan Evaluasi Aplikasi Android untuk Penyimpanan Seed Phrase pada Wallet Non-Custodial Menggunakan Keystore, BiometricPrompt, dan Shamir’s Secret Sharing.

1. Ringkasan Proyek
   SeedLock adalah aplikasi Android prototipe yang dirancang untuk menyimpan seed phrase dari dompet non-kustodial dengan tingkat keamanan tinggi. Aplikasi ini mengimplementasikan pendekatan keamanan berlapis (defense-in-depth) dengan menggabungkan tiga teknologi utama:

Android Keystore: Untuk enkripsi berbasis perangkat keras yang melindungi setiap bagian rahasia.

BiometricPrompt: Untuk autentikasi pengguna yang aman dan terikat dengan kunci kriptografi.

Shamir's Secret Sharing (SSS): Untuk memecah seed phrase menjadi beberapa bagian (shares), menghilangkan single point of failure.

Proyek ini dikembangkan mengikuti proposal skripsi, menggunakan pendekatan Prototype Development secara iteratif dan arsitektur MVVM (Model-View-ViewModel).

2. Arsitektur & Teknologi
   Aplikasi ini dibangun dengan arsitektur MVVM yang bersih dan modular untuk memisahkan tanggung jawab dan meningkatkan kemudahan pengujian serta pemeliharaan.

Lapisan Arsitektur:
View (UI Layer):

Dibangun sepenuhnya menggunakan Jetpack Compose, toolkit UI deklaratif modern dari Android.

Terdiri dari Composable Screens (AuthScreen, HomeScreen, AddSeedScreen, ViewSeedScreen) yang hanya bertanggung jawab untuk menampilkan data dan meneruskan interaksi pengguna ke ViewModel.

Navigasi diatur menggunakan Navigation Compose.

MainActivity bertindak sebagai host untuk navigasi dan mengelola siklus hidup aplikasi, termasuk logika auto-logout saat aplikasi masuk ke latar belakang.

ViewModel (Presentation Logic):

Bertindak sebagai jembatan antara View dan Model (Repository).

Menangani semua logika presentasi, mengelola state UI menggunakan Kotlin StateFlow, dan mengeksposnya ke Composable.

Tidak memiliki referensi langsung ke View, sehingga tahan terhadap perubahan konfigurasi.

Menggunakan Hilt untuk Dependency Injection, menyederhanakan penyediaan dependensi seperti Repository dan Manager.

SessionManager diinjeksikan ke setiap ViewModel untuk mengelola sesi aktif (timeout 5 menit) dan mereset timer pada setiap interaksi pengguna.

Model (Data Layer):

Repository Pattern: SeedRepository menjadi satu-satunya sumber kebenaran (Single Source of Truth) untuk data aplikasi. Ini mengabstraksi sumber data dari seluruh aplikasi.

DataStore: Digunakan untuk penyimpanan data persisten. Shares yang sudah dienkripsi disimpan di sini, menggantikan SharedPreferences yang sudah usang.

KeystoreManager: Mengelola semua operasi dengan Android Keystore System. Ini bertanggung jawab untuk membuat kunci enkripsi/dekripsi, menyimpannya di lingkungan aman (di-backup oleh hardware jika didukung), dan melakukan operasi kriptografi.

ShamirSecretSharingManager: Mengimplementasikan logika Shamir's Secret Sharing (SSS) menggunakan pustaka sss4j. Bertugas untuk memecah seed phrase menjadi 3 shares dan merekonstruksinya kembali dari 2 shares.

BiometricInteractor: Mengelola logika untuk BiometricPrompt, termasuk menampilkan prompt dan menangani callback autentikasi.

Teknologi Utama:
Bahasa: Kotlin

UI: Jetpack Compose

Arsitektur: MVVM (Model-View-ViewModel)

Dependency Injection: Hilt

Asynchronous: Kotlin Coroutines & Flow

Keamanan:

Android Keystore API (untuk enkripsi)

BiometricPrompt API (untuk autentikasi)

Shamir's Secret Sharing (Pustaka sss4j)

Penyimpanan: Jetpack DataStore

Navigasi: Navigation Compose

3. Alur Kerja Keamanan
   Autentikasi: Pengguna membuka aplikasi dan harus melakukan autentikasi menggunakan biometrik (sidik jari/wajah). Sesi aktif dimulai selama 5 menit.

Tambah Seed:

Pengguna memasukkan 12 atau 24 kata seed phrase.

Seed phrase dipecah menjadi 3 shares menggunakan SSS (skema 2-of-3).

Untuk setiap share, KeystoreManager membuat kunci enkripsi unik di Android Keystore.

Setiap share dienkripsi dengan kuncinya masing-masing.

Ketiga shares yang sudah terenkripsi beserta IV (Initialization Vector) disimpan ke DataStore.

Lihat Seed:

Pengguna memilih seed yang ingin dilihat.

Aplikasi memuat 3 shares terenkripsi dari DataStore.

Untuk setiap share, KeystoreManager mengambil kunci yang sesuai dari Keystore dan mendekripsinya.

Setelah 2 dari 3 shares berhasil didekripsi, SSS merekonstruksi seed phrase asli.

Seed phrase ditampilkan di layar dan dihapus dari memori saat layar ditutup.

Manajemen Sesi:

Sesi aktif berlangsung selama 5 menit. Setiap interaksi pengguna (klik, scroll) akan mereset timer ini.

Jika tidak ada interaksi selama 5 menit, sesi berakhir. Aksi selanjutnya akan memicu ulang autentikasi biometrik.

Jika aplikasi dipindahkan ke latar belakang, sesi akan langsung berakhir dan memerlukan autentikasi ulang saat kembali.

4. Cara Menjalankan Proyek
   Prasyarat:
   Android Studio Narwhal (2025.x) atau lebih baru.

Perangkat Android fisik atau Emulator dengan API level 29+ dan dukungan biometrik.

Langkah-langkah Build:
Clone Repositori:

git clone <url-proyek-anda>
cd seedlock-app

Buka di Android Studio:

Buka Android Studio.

Pilih File > Open dan arahkan ke folder proyek yang baru saja di-clone.

Tunggu hingga Gradle selesai melakukan sinkronisasi.

Tambahkan Pustaka SSS4J:

Pustaka sss4j tidak tersedia di Maven Central. Anda perlu menambahkannya secara manual.

Download file .jar dari repositori resminya.

Buat folder libs di dalam direktori app.

Salin file sss4j-1.0.0.jar ke dalam folder app/libs.

Buka file app/build.gradle.kts dan tambahkan baris berikut di dalam blok dependencies:

implementation(files("libs/sss4j-1.0.0.jar"))

Lakukan sinkronisasi Gradle lagi.

Jalankan Aplikasi:

Pilih perangkat (emulator atau fisik) dari daftar target.

Klik tombol Run 'app' (Shift + F10).

5. Struktur Proyek
   Berikut adalah struktur direktori utama dalam modul app:

app/
└── src/
└── main/
├── java/
│   └── com/
│       └── app/
│           └── seedlockapp/
│               ├── MainActivity.kt
│               ├── SeedLockApp.kt
│               ├── data/
│               │   ├── local/
│               │   │   └── DataStoreManager.kt
│               │   ├── model/
│               │   │   ├── Seed.kt
│               │   │   └── Share.kt
│               │   └── repository/
│               │       └── SeedRepository.kt
│               ├── di/
│               │   └── AppModule.kt
│               ├── domain/
│               │   ├── interactor/
│               │   │   └── BiometricInteractor.kt
│               │   └── manager/
│               │       ├── KeystoreManager.kt
│               │       ├── SessionManager.kt
│               │       └── ShamirSecretSharingManager.kt
│               ├── ui/
│               │   ├── components/
│               │   ├── navigation/
│               │   │   ├── AppNavigation.kt
│               │   │   └── Screen.kt
│               │   ├── screens/
│               │   │   ├── addseed/
│               │   │   ├── auth/
│               │   │   ├── home/
│               │   │   └── viewseed/
│               │   └── theme/
│               └── util/
└── res/
