package com.nalar.reroll

/**
 * v0.4.4: a dedicated job-search/apply category.
 * Career remains broad career preparation; Seek Job is for actually finding and submitting applications.
 */
val SeekJobActivities: List<String> = listOf(
    "Cari 10 lowongan relevan di LinkedIn Jobs dan simpan 3 terbaik",
    "Cari 5 lowongan di LinkedIn Jobs lalu apply 1 yang paling cocok tanpa ubah CV",
    "Cari 5 lowongan di LinkedIn Jobs lalu sesuaikan CV untuk 1 lowongan terbaik",
    "Gunakan LinkedIn Easy Apply untuk 2 lowongan yang requirement-nya paling dekat",
    "Cari lowongan LinkedIn dengan keyword alternatif dari job title utama kamu",
    "Cari lowongan LinkedIn yang diposting dalam 24 jam terakhir dan apply 1",
    "Cari lowongan LinkedIn entry-level atau fresh graduate selama 30 menit",
    "Cari lowongan LinkedIn remote atau hybrid lalu simpan 5 yang realistis",
    "Cari 3 lowongan LinkedIn dan bandingkan requirement sebelum memilih 1 untuk dilamar",
    "Apply 1 lowongan LinkedIn dengan CV utama jika requirement-nya sudah cocok",
    "Cari 10 lowongan relevan di JobStreet dan shortlist 3",
    "Apply 2 lowongan JobStreet menggunakan CV utama tanpa modifikasi jika requirement serupa",
    "Pilih 1 lowongan JobStreet terbaik lalu sesuaikan CV dengan keyword job description",
    "Cari lowongan JobStreet yang baru diposting hari ini dan apply 1",
    "Cari role alternatif di JobStreet yang masih cocok dengan pengalaman kamu",
    "Cari lowongan JobStreet berdasarkan lokasi yang masih realistis untuk kamu",
    "Buka 10 lowongan JobStreet, buang yang tidak cocok, lalu apply minimal 1",
    "Cari lowongan JobStreet fresh graduate dan simpan 5 untuk batch berikutnya",
    "Apply 1 lowongan JobStreet dengan CV khusus role tersebut",
    "Apply 1 lowongan JobStreet tanpa cover letter jika tidak diwajibkan",
    "Cari 10 lowongan relevan di Glints dan pilih 3 yang paling masuk akal",
    "Apply 2 lowongan Glints dengan CV utama jika requirement-nya mirip",
    "Pilih 1 lowongan Glints lalu sesuaikan CV sebelum apply",
    "Cari lowongan Glints entry-level dan apply minimal 1",
    "Cari lowongan Glints remote atau hybrid yang sesuai skill kamu",
    "Cari lowongan Glints menggunakan 3 keyword job title berbeda",
    "Buka 5 lowongan Glints dan cek apakah pengalaman kamu memenuhi mayoritas requirement",
    "Apply 1 lowongan Glints yang paling baru diposting",
    "Simpan 5 lowongan Glints untuk dilamar pada sesi berikutnya",
    "Cari lowongan Glints di perusahaan kecil atau startup yang membuka fresh graduate",
    "Cari 10 lowongan di Dealls dan shortlist 3 yang paling sesuai",
    "Apply 1 lowongan Dealls dengan CV utama tanpa modifikasi",
    "Apply 1 lowongan Dealls setelah menyesuaikan CV dengan keyword utama",
    "Cari lowongan Dealls yang menerima fresh graduate atau pengalaman minim",
    "Cari lowongan di Kalibrr dan apply 1 yang cocok",
    "Cari lowongan di Indeed dan simpan 5 yang relevan",
    "Apply 1 lowongan dari Indeed dengan CV utama jika requirement sudah cocok",
    "Cari lowongan di KitaLulus dan apply 1 role yang realistis",
    "Cari lowongan di Karir.com atau portal kerja sejenis dan simpan 5",
    "Cari lowongan melalui career page 5 perusahaan target dan apply jika ada yang cocok",
    "Cari lowongan langsung di website perusahaan, bukan lewat job portal, selama 30 menit",
    "Cari 5 perusahaan yang sedang hiring lewat Google lalu buka halaman Careers mereka",
    "Apply 1 lowongan dari career page perusahaan dengan CV yang disesuaikan",
    "Apply 1 lowongan dari career page perusahaan menggunakan CV utama jika sudah relevan",
    "Lakukan batch search: LinkedIn, JobStreet, dan Glints masing-masing 10 menit",
    "Lakukan batch apply: pilih masing-masing 1 lowongan terbaik dari LinkedIn, JobStreet, dan Glints",
    "Cari 15 lowongan lintas platform lalu ranking 5 berdasarkan kecocokan",
    "Cari 10 lowongan lintas platform dan apply hanya yang cocok minimal 70%",
    "Cari lowongan dengan keyword Community Development, CSR, Program Officer, atau Project Officer",
    "Cari lowongan dengan keyword Program Admin, Project Admin, Community Engagement, atau Partnership",
    "Cari lowongan dengan keyword Field Officer, Facilitator, Social Development, atau Community Relations",
    "Cari lowongan dengan job title yang berbeda tetapi requirement-nya cocok dengan skill kamu",
    "Cari lowongan yang meminta Excel, reporting, stakeholder coordination, atau community work",
    "Cari 5 lowongan yang cocok dengan CV utama tanpa perlu tailoring besar",
    "Cari 3 lowongan high-fit dan buat satu versi CV tailored untuk semuanya jika keyword-nya mirip",
    "Pilih 1 lowongan dream role lalu buat lamaran paling rapi untuk role tersebut",
    "Pilih 2 lowongan realistic-fit lalu apply cepat menggunakan CV utama",
    "Cek semua lowongan yang kamu simpan kemarin dan apply minimal 2 yang masih aktif",
    "Buka application tracker, pilih 3 lowongan belum dilamar, lalu submit minimal 1 hari ini",
    "Cari lowongan baru selama 45 menit tanpa mengedit CV, fokus hanya discovery dan shortlist"
)

fun activityPoolV3(category: String): List<String> =
    if (category == "Seek Job") SeekJobActivities else activityPoolV2(category)

fun activityCountV3(category: String): Int = activityPoolV3(category).size
fun totalActivityCountV3(): Int = totalActivityCountV2() + SeekJobActivities.size
