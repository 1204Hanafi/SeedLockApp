# SeedLock - Secure Seed Phrase Storage App

SeedLock adalah aplikasi Android yang mengimplementasikan penyimpanan seed phrase aman untuk wallet non-custodial menggunakan kombinasi Android Keystore, BiometricPrompt, dan Shamir's Secret Sharing.

## 🔒 Fitur Keamanan

- **Android Keystore**: Enkripsi hardware-backed untuk melindungi shares
- **BiometricPrompt**: Autentikasi biometrik dengan timeout sesi 5 menit
- **Shamir's Secret Sharing**: Skema 2-of-3 untuk membagi dan merekonstruksi seed phrase menggunakan Bouncy Castle
- **EncryptedSharedPreferences**: Penyimpanan lokal terenkripsi
- **Session Management**: Timeout otomatis untuk mencegah akses tidak sah

## 🏗️ Arsitektur

Aplikasi menggunakan arsitektur MVVM (Model-View-ViewModel) dengan pola Clean Architecture:

```
├── ui/                     # Presentation Layer
│   ├── screens/           # Compose UI Screens
│   ├── components/        # Reusable UI Components
│   ├── navigation/        # Navigation Logic
│   └── theme/            # UI Theme & Styling
├── domain/                # Domain Layer
│   ├── model/            # Domain Models
│   └── interactor/       # Business Logic
├── data/                  # Data Layer
│   ├── repository/       # Repository Implementation
│   ├── local/            # Local Data Sources
│   └── model/            # Data Models
├── session/              # Session Management
└── di/                   # Dependency Injection
```

## 🚀 Cara Build & Run

### Prerequisites

- Android Studio Arctic Fox atau lebih baru
- Android SDK API level 29+
- Device atau emulator dengan Android 10+
- Biometric authentication setup (fingerprint/face)

### Build Instructions

1. Clone repository:
```bash
git clone <repository-url>
cd SeedLockApp
```

1. Open project di Android Studio

2. Sync Gradle files

3. Build dan run:
```bash
./gradlew assembleDebug
./gradlew installDebug
```

### Dependencies

Aplikasi menggunakan dependencies berikut:

- **Jetpack Compose**: Modern UI toolkit
- **Hilt**: Dependency injection
- **Navigation Compose**: Navigation framework
- **Biometric**: Biometric authentication
- **Security Crypto**: EncryptedSharedPreferences
- **Timber**: Logging

## 📱 Cara Penggunaan

### 1. Autentikasi Awal
- Buka aplikasi
- Sistem akan meminta autentikasi biometrik
- Setelah berhasil, sesi akan aktif selama 5 menit

### 2. Menambah Seed Phrase
```kotlin
// Contoh penggunaan AddSeedViewModel
viewModel.onPhraseChanged("abandon abandon abandon...")
viewModel.onAliasChanged("My Wallet")
viewModel.saveSeed()
```

### 3. Melihat Seed Phrase
```kotlin
// Contoh penggunaan ViewSeedViewModel
viewModel.load(seedId)
// Seed phrase akan direkonstruksi dan ditampilkan
```

### 4. Session Management
```kotlin
// Refresh session pada setiap interaksi
sessionManager.refreshInteraction()

// Check status autentikasi
sessionManager.isAuthenticated.collect { isAuth ->
    // Handle authentication state
}
```

## 🔧 API Documentation

### SessionManager

Mengelola sesi pengguna dan timeout otomatis.

```kotlin
class SessionManager {
    // Mengautentikasi pengguna dan memulai timer timeout
    fun authenticate()
    
    // Membatalkan sesi saat ini
    fun invalidateSession()
    
    // Refresh timer timeout pada interaksi pengguna
    fun refreshInteraction()
    
    // StateFlow untuk status autentikasi
    val isAuthenticated: StateFlow<Boolean>
}
```

### BiometricInteractor

Menangani operasi autentikasi biometrik.

```kotlin
class BiometricInteractor {
    // Cek ketersediaan biometrik pada device
    fun isBiometricAvailable(activity: FragmentActivity): Boolean
    
    // Buat BiometricPrompt instance
    fun createBiometricPrompt(
        activity: FragmentActivity,
        callback: BiometricPrompt.AuthenticationCallback
    ): BiometricPrompt
    
    // Buat PromptInfo dengan konfigurasi default
    fun createPromptInfo(
        title: String = "Verifikasi Identitas Anda",
        subtitle: String? = null,
        negativeButtonText: String = "Batal"
    ): BiometricPrompt.PromptInfo
}
```

### KeystoreInteractor

Menangani enkripsi/dekripsi menggunakan Android Keystore.

```kotlin
class KeystoreInteractor {
    // Enkripsi data menggunakan AES/CBC/PKCS7Padding
    fun encrypt(data: ByteArray): Pair<ByteArray, ByteArray>
    
    // Dekripsi data menggunakan AES/CBC/PKCS7Padding
    fun decrypt(encryptedData: ByteArray, iv: ByteArray): ByteArray
}
```

### SSSInteractor

Implementasi Shamir's Secret Sharing.

```kotlin
class SSSInteractor {
    // Split secret menjadi multiple shares (2-of-3)
    fun splitSecret(secret: String): List<Share>
    
    // Rekonstruksi secret dari shares
    fun reconstructSecret(shares: List<Share>): String
    
    // Validasi shares untuk rekonstruksi
    fun validateShares(shares: List<Share>): Boolean
}
```

### SeedRepository

Interface untuk operasi CRUD seed phrases.

```kotlin
interface SeedRepository {
    suspend fun saveSeed(seed: Seed)
    suspend fun getSeed(id: String): Seed?
    suspend fun getAllSeeds(): List<Seed>
    suspend fun deleteSeed(id: String)
}
```

## 🧪 Testing

### Unit Tests
```bash
./gradlew test
```

### Instrumentation Tests
```bash
./gradlew connectedAndroidTest
```

### Test Coverage
Target minimal 80% coverage untuk skenario use case utama.

## 🔄 Development Workflow

### Branching Strategy

- `main`: Production-ready code
- `feature/iter1-biometric`: SessionManager + BiometricPrompt
- `feature/iter2-sss`: SSS + Keystore implementation
- `feature/iter3-integration`: Full integration + UI

### Commit Convention
```
feat: add new feature
fix: bug fix
docs: documentation changes
refactor: code refactoring
test: add or update tests
```

## 🛡️ Security Considerations

1. **Hardware-backed Security**: Menggunakan Android Keystore untuk proteksi kunci
2. **Biometric Authentication**: Autentikasi biometrik dengan fallback
3. **Session Timeout**: Otomatis logout setelah 5 menit inaktivitas
4. **Memory Protection**: SecureString untuk membersihkan data sensitif
5. **Encrypted Storage**: EncryptedSharedPreferences untuk penyimpanan lokal

## 📋 TODO & Future Improvements

- [ ] Implementasi unit tests lengkap
- [ ] Instrumentation tests untuk UI
- [ ] Performance benchmarking
- [ ] Backup/restore functionality
- [ ] Multi-device synchronization
- [ ] Enhanced error handling
- [ ] Accessibility improvements

## 📄 License

[Specify your license here]

## 👥 Contributors

- Android Engineer - Implementation
- Security Consultant - Security review
- UI/UX Designer - Interface design

## 📞 Support

Untuk pertanyaan atau dukungan, silakan buat issue di repository ini atau hubungi tim development.



## 🚀 Panduan Implementasi Step-by-Step di Android Studio

Ikuti langkah-langkah berikut untuk mengimpor, membangun, dan menjalankan proyek SeedLock di Android Studio:

### 1. Persiapan Awal

- Pastikan Anda telah menginstal **Android Studio** versi terbaru (disarankan Arctic Fox atau lebih baru).
- Pastikan **Android SDK** dengan API level 29 (Android 10) atau lebih tinggi sudah terinstal.
- Siapkan **emulator Android** atau **perangkat fisik** dengan Android 10+ yang mendukung autentikasi biometrik (sidik jari atau pengenalan wajah).
  - Untuk emulator, Anda bisa membuat AVD (Android Virtual Device) baru dan pastikan mengaktifkan opsi biometrik di pengaturan AVD.

### 2. Mengimpor Proyek ke Android Studio

1.  **Buka Android Studio.**
2.  Dari layar selamat datang, pilih **"Open an existing Android Studio project"** atau jika sudah ada proyek terbuka, pergi ke `File > Open...`.
3.  Navigasikan ke direktori tempat Anda mengkloning atau mengunduh proyek `SeedLockApp` (yaitu, folder `SeedLockApp` yang berisi `app`, `build.gradle.kts`, `settings.gradle.kts`, dll.).
4.  Pilih folder `SeedLockApp` dan klik **"Open"**.

### 3. Sinkronisasi Gradle

- Setelah proyek terbuka, Android Studio akan secara otomatis memulai proses sinkronisasi Gradle.
- Tunggu hingga proses ini selesai. Anda dapat melihat progres di jendela `Build` (biasanya di bagian bawah Android Studio).
- Jika ada masalah sinkronisasi, periksa pesan error di jendela `Build` atau `Event Log` dan pastikan koneksi internet Anda stabil.

### 4. Membangun (Build) Proyek

- Setelah sinkronisasi Gradle berhasil, Anda dapat membangun proyek.
- Pergi ke `Build > Make Project` atau klik ikon palu di toolbar Android Studio.
- Tunggu hingga proses build selesai. Ini akan mengkompilasi kode sumber dan menghasilkan file APK.

### 5. Menjalankan Aplikasi

1.  **Pilih Target Perangkat**: Di toolbar Android Studio, Anda akan melihat dropdown untuk memilih perangkat (emulator atau perangkat fisik).
    - Jika Anda menggunakan emulator, pilih AVD yang sudah Anda siapkan.
    - Jika Anda menggunakan perangkat fisik, pastikan mode `USB debugging` aktif di perangkat Anda dan perangkat terhubung ke komputer.
2.  **Jalankan Aplikasi**: Klik ikon `Run` (segitiga hijau) di toolbar Android Studio.
3.  Android Studio akan menginstal aplikasi di perangkat yang dipilih dan meluncurkannya.

### 6. Mengatur Biometrik di Emulator (Opsional)

Jika Anda menggunakan emulator dan ingin menguji fitur biometrik:

1.  Jalankan emulator Anda.
2.  Di emulator, pergi ke `Settings > Security & location > Fingerprint` (atau opsi biometrik serupa).
3.  Ikuti instruksi untuk menambahkan sidik jari. Untuk simulasi, Anda bisa menggunakan tombol `Enroll Fingerprint` di jendela kontrol emulator (ikon tiga titik di toolbar emulator, lalu `Fingerprint`).
4.  Setelah sidik jari terdaftar, Anda dapat menguji autentikasi biometrik di aplikasi SeedLock.

Dengan mengikuti langkah-langkah ini, Anda seharusnya dapat mengimpor, membangun, dan menjalankan aplikasi SeedLock di lingkungan pengembangan Android Studio Anda.