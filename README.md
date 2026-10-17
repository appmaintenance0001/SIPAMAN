# SI-PAMAN --- Sistem Pantau Maintenance

SI-PAMAN (*Sistem Pantau Maintenance*) adalah aplikasi Android untuk
membantu pencatatan, perencanaan, dan pemantauan pekerjaan maintenance
agar data pekerjaan lebih terstruktur dan mudah ditinjau.

## Fitur

-   **Dashboard** untuk melihat ringkasan pekerjaan maintenance.
-   **Input pekerjaan** dengan informasi seperti ID Project, jenis
    pekerjaan, tanggal mulai, dan *due date*.
-   **Daftar planning maintenance** untuk melihat pekerjaan yang sudah
    dicatat.
-   **Status pekerjaan** untuk membantu memantau pekerjaan berdasarkan
    tanggal jatuh tempo.
-   **Firebase Authentication** untuk autentikasi pengguna.
-   **Firebase Realtime Database** sebagai penyimpanan data aplikasi.

> Catatan: daftar fitur di atas mengikuti ruang lingkup proyek yang
> direncanakan/dibahas. Sesuaikan bagian ini dengan fitur yang sudah
> benar-benar tersedia pada versi aplikasi yang dipublikasikan.

## Teknologi

-   Android Studio
-   Java
-   XML untuk layout antarmuka Android
-   Firebase Authentication
-   Firebase Realtime Database
-   Gradle

## Struktur Proyek

Struktur folder dapat berbeda sesuai implementasi proyek. Secara umum:

``` text
SI-PAMAN/
├── app/
│   ├── src/
│   │   └── main/
│   │       ├── java/       # Kode sumber Java
│   │       ├── res/        # Layout XML, drawable, dan resource
│   │       └── AndroidManifest.xml
│   ├── build.gradle.kts
│   └── google-services.json  # Konfigurasi Firebase lokal
├── gradle/
├── build.gradle.kts
├── settings.gradle.kts
└── README.md
```

## Persyaratan

Sebelum menjalankan proyek, siapkan:

1.  Android Studio dengan Android SDK yang sesuai dengan konfigurasi
    proyek.
2.  JDK yang kompatibel dengan versi Android Gradle Plugin proyek.
3.  Akses ke proyek Firebase yang digunakan aplikasi.
4.  Perangkat Android atau emulator untuk pengujian.

## Cara Menjalankan

1.  **Clone repositori**

    ``` bash
    git clone URL_REPOSITORI_ANDA
    ```

2.  Buka folder proyek melalui Android Studio.

3.  Tunggu proses *Gradle Sync* selesai.

4.  Hubungkan konfigurasi Firebase sesuai petunjuk di bawah.

5.  Pilih perangkat Android atau emulator.

6.  Klik **Run** untuk membangun dan menjalankan aplikasi.

Ganti `URL_REPOSITORI_ANDA` dengan URL repositori GitHub SI-PAMAN.

## Konfigurasi Firebase

1.  Buka [Firebase Console](https://console.firebase.google.com/) dan
    pilih atau buat proyek Firebase.
2.  Daftarkan aplikasi Android menggunakan **application ID/package
    name** yang sama dengan proyek.
3.  Unduh `google-services.json` dari Firebase Console.
4.  Simpan file tersebut di folder `app/` pada komputer lokal.
5.  Aktifkan metode login yang diperlukan di Firebase Authentication.
6.  Siapkan Realtime Database dan buat **Security Rules** sesuai hak
    akses pengguna.
7.  Jalankan aplikasi dan uji proses login serta pembacaan/penyimpanan
    data.

### Keamanan konfigurasi

-   Jangan pernah memasukkan password, token layanan, private key, atau
    kredensial administrator ke repositori.

-   Untuk repositori publik, pertimbangkan agar file konfigurasi lokal
    tidak dilacak oleh Git dengan menambahkan aturan berikut ke
    `.gitignore`:

    ``` gitignore
    app/google-services.json
    ```

-   Jika file sudah pernah di-commit, menambahkan `.gitignore` saja
    tidak menghapusnya dari riwayat Git.

-   API key Firebase yang digunakan pada aplikasi Android bukan
    pengganti aturan keamanan. Batasi API key yang sesuai dan amankan
    data melalui Firebase Security Rules serta pengaturan akses yang
    benar.

-   Jika suatu key terungkap, tinjau penggunaannya dan lakukan
    pembatasan atau rotasi bila diperlukan.

## Alur Status Pekerjaan

Status pekerjaan dapat ditentukan berdasarkan aturan aplikasi. Contoh
aturan yang pernah direncanakan:

-   **ON PROGRESS** --- pekerjaan masih dalam periode sampai tanggal
    jatuh tempo.
-   **EXP** --- pekerjaan sudah melewati tanggal jatuh tempo dan belum
    diselesaikan.

Sesuaikan aturan ini dengan logika status yang benar-benar diterapkan di
kode aplikasi, termasuk penanganan pekerjaan yang telah selesai.

## Pengembangan Berikutnya

Rencana pengembangan dapat mencakup:

-   Filter daftar pekerjaan berdasarkan proyek, jenis pekerjaan, status,
    atau rentang tanggal.
-   Notifikasi pekerjaan yang mendekati atau melewati *due date*.
-   Riwayat perubahan dan penyelesaian pekerjaan.
-   Laporan maintenance yang dapat diekspor.
-   Pengaturan hak akses berdasarkan peran pengguna.

Fitur dalam bagian ini merupakan ide pengembangan dan belum tentu
tersedia pada versi saat ini.

## Kontribusi

Jika ingin mengembangkan proyek:

1.  Buat branch baru untuk perubahan.
2.  Lakukan perubahan dan pengujian.
3.  Pastikan konfigurasi rahasia tidak ikut di-commit.
4.  Ajukan *pull request* dengan ringkasan perubahan.

## Lisensi

Belum ditentukan. Tambahkan informasi lisensi sebelum proyek
didistribusikan atau digunakan ulang oleh pihak lain.

------------------------------------------------------------------------

**SI-PAMAN --- Sistem Pantau Maintenance**\
Membantu pencatatan dan pemantauan pekerjaan maintenance secara lebih
terstruktur.
