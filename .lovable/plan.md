## Catatan penting soal platform

Saya tidak bisa membangun aplikasi Flutter native (APK/IPA). Yang bisa saya bangun: **aplikasi web yang di-install ke home screen HP (PWA)** — dibuka dari ikon aplikasi, layar penuh tanpa address bar, data tersimpan lokal di HP, dan bisa mengirim notifikasi.

Batasan jujur yang perlu Anda tahu sebelum setuju:
- Notifikasi/alarm **andal saat aplikasi sedang dibuka atau baru saja dibuka**. Kalau HP terkunci berjam-jam dan aplikasi tidak berjalan, browser bisa menunda notifikasi. Ini beda dari alarm exact Android native.
- Karena itu setiap ujian juga bisa **di-export ke kalender HP (.ics)** — kalender bawaan Android/iOS yang jadi jaring pengaman alarmnya.
- OCR + AI parsing berjalan **online** (Lovable AI, model Gemini multimodal). Model AI on-device 1.5GB tidak mungkin di web, jadi mode offline penuh untuk parsing tidak tersedia. CRUD, dashboard, dan data tetap jalan offline.

Kalau alarm exact 100% andal saat aplikasi tertutup adalah syarat mutlak, PWA bukan jawabannya dan sebaiknya PRD ini dikerjakan di Flutter di luar Lovable.

---

## Yang akan dibangun

**Bahasa UI: Bahasa Indonesia. Penyimpanan: lokal di HP (IndexedDB/localStorage), tanpa akun.**

### Halaman
```text
/            Dashboard   — ujian terdekat, countdown, badge "Bentrok jadwal"
/upload      Upload      — pilih/foto screenshot -> hasil deteksi AI -> form konfirmasi
/kalender    Kalender    — tampilan bulanan, titik indikator pada tanggal berujian
/riwayat     Riwayat     — status Selesai / Terlewat
/pengaturan  Pengaturan  — izin notifikasi, offset reminder, export/import data, hapus semua
```

### Alur upload (US-01, US-02, US-11)
1. Pilih gambar (galeri atau kamera HP), validasi JPG/PNG ≤10MB.
2. Gambar dikirim ke AI (multimodal, langsung baca tabel) → JSON array dengan schema tetap: `nama_mk, kode_mk, jenis_ujian, kelas, tanggal (ISO), jam_mulai, jam_selesai, confidence` per baris.
3. Form konfirmasi per baris: field kosong ditandai merah (wajib diisi), field confidence <70% highlight kuning "periksa kembali".
4. Validasi tanggal lampau → tolak simpan dengan peringatan.
5. Tombol "Simpan" — tidak ada data tersimpan tanpa konfirmasi eksplisit (FR-05, FR-06).
6. Fallback: AI gagal/timeout → form manual kosong, user tidak pernah stuck loading.

### Alarm & reminder (FR-07 s/d FR-11)
Per ujian dijadwalkan 3 trigger sekali-bunyi:
- H-30 menit sebelum mulai → notifikasi "Ujian [MK] dimulai 30 menit lagi"
- Waktu mulai → alarm berbunyi (suara + getar + notifikasi persisten)
- H-30 menit sebelum selesai → "Sisa waktu ujian tinggal 30 menit"

Setelah dibunyikan/di-dismiss, trigger ditandai terpakai dan tidak pernah berbunyi lagi — termasuk setelah HP restart. Tidak dibunyikan jika sudah terlewat >5 menit; status otomatis jadi `terlewat`. Hapus ujian → semua trigger terkait ikut dibatalkan.

Tombol "Tambah ke Kalender HP" per ujian dan "Export semua" sebagai backup alarm.

### CRUD manual
Tambah/edit/hapus jadwal tanpa screenshot, semua field wajib, validasi sama.

### Data
Semua di HP: `exams` (id, nama_mk, kode_mk, jenis_ujian, kelas, tanggal, jam_mulai, jam_selesai, status, source, created_at) + `triggers` (exam_id, tipe, waktu, sudah_bunyi). Export/import JSON untuk pindah HP. Tidak ada server, tidak ada akun.

### Desain
Mobile-first, target 390px, tema gelap-lembut dengan aksen tegas untuk status ujian (biru = mendatang, kuning = hari ini, hijau = selesai, merah = terlewat). Bukan template ungu-gradien generik. Tipografi besar & kontras ≥4.5:1 (US-10).

---

## Detail teknis
- TanStack Start + Tailwind; state jadwal di IndexedDB via wrapper store.
- PWA manifest + ikon (`display: standalone`) agar bisa "Add to Home Screen"; service worker hanya untuk offline app-shell dan notifikasi terjadwal.
- Parsing AI lewat Lovable AI Gateway di server function — API key tidak pernah menyentuh browser. Output divalidasi ketat dengan Zod (format tanggal ISO, jam 00:00–23:59); output tak sesuai schema → retry 1x lalu fallback manual.
- Scheduler: Notification API + service worker timer, di-rehidrasi setiap app dibuka (kompensasi keterbatasan browser).
- Rate limit sisi klien 20 request AI/hari sesuai PRD.

## Di luar scope
Multi-user, sharing, sync antar device, scraping portal kampus, alarm berulang, model AI on-device.
