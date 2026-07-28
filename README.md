# ExamPing AI — Alarm Ujian Sekali Bunyi dari Screenshot

ExamPing AI adalah aplikasi Android native berbasis Kotlin dan Jetpack Compose yang mengubah screenshot jadwal ujian menjadi alarm otomatis sekali bunyi, lengkap dengan pengingat sebelum ujian dimulai dan sebelum berakhir.

## Fitur Utama

1. **Beranda / Dashboard**:
   - Menampilkan daftar seluruh ujian mendatang dan berlangsung.
   - Indikator countdown (misal: "2 hari 3 jam lagi" atau "sedang berlangsung").
   - Deteksi otomatis jadwal yang bentrok.
   - Peringatan izin notifikasi dan tombol penambahan jadwal manual.

2. **Upload & Pemindaian OCR AI**:
   - Pemindaian screenshot tabel jadwal ujian berbasis Gemini AI (Gemini Flash).
   - Ekstraksi otomatis nama mata kuliah, kode MK, jenis ujian, kelas, tanggal, dan jam.
   - Peninjauan draft hasil OCR dengan indikator tingkat keyakinan (confidence warning).
   - Opsi pengisian manual tanpa screenshot.
   - Batas harian pemindaian AI untuk efisiensi biaya.

3. **Kalender Ujian**:
   - Tampilan kalender bulanan interaktif dengan navigasi antar bulan.
   - Indikator titik pada tanggal berujian.
   - Memilih tanggal menampilkan seluruh jadwal ujian pada hari tersebut.

4. **Riwayat Ujian**:
   - Rekam jejak ujian yang telah selesai atau terlewat.

5. **Pengaturan & Cadangan Data**:
   - Pengaturan offset pengingat sebelum mulai dan sebelum selesai (dalam menit).
   - Sakelar suara alarm dan getar.
   - Ekspor jadwal ke format kalender `.ics` untuk diimpor ke aplikasi kalender bawaan HP.
   - Pengelolaan data lokal HP (Room database).

6. **Alarm & Notifikasi System Native**:
   - Penjadwalan `AlarmManager` presisi untuk memicu notifikasi dan layar pemicu alarm dalam aplikasi.

## Teknologi & Arsitektur

- **Bahasa**: Kotlin
- **UI Framework**: Jetpack Compose dengan Material Design 3 (M3)
- **Database**: Room Persistence Library (KSP)
- **AI OCR**: Retrofit & Gemini API (`gemini-3.5-flash`)
- **Penjadwalan Alarm**: Android `AlarmManager` & `BroadcastReceiver`
- **Trik Desain**: Tema gelap "Midnight Exam Hall" dengan aksen amber gandum dan badge status dinamis.
