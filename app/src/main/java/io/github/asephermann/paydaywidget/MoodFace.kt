package io.github.asephermann.paydaywidget

/**
 * Background mood widget (mood_0 .. mood_31 di res/drawable-nodpi) yang
 * diterapkan pada widgetRoot, mengikuti siklus 5 fase bulanan:
 *
 *  🤑 Fase Euphoria        (H-29..H-23): gajian cair, dompet tebal
 *  🧾 Fase Realita         (H-22..H-16): auto-debet mulai masuk
 *  😐 Fase Bertahan Hidup  (H-15..H-9):  saldo menyusut
 *  🧟 Fase Tanggal Tua     (H-8..H-2):   sekarat mie instan
 *  🌅 Fase Hilal Terlihat  (H-1..H-0):   gajian kelihatan, Hari H senyum
 *
 * Index array = sisa hari (daysLeft). Setiap hari memakai drawable berbeda
 * (unik). Karena tidak semua emoji panduan punya gambar, dipakai ekspresi
 * terdekat yang tersedia (ditandai komentar). H-30 (kasus langka) clamp
 * ke H-29.
 *
 * ID ditulis eksplisit satu per satu agar tetap dipertahankan oleh
 * resource shrinker pada release build. Karena dikirim sebagai ID resource
 * (bukan bitmap via Binder), update widget tetap ringan.
 */
object MoodFace {

    private val BACKGROUNDS = intArrayOf(
        R.drawable.mood_0,  // H-0  😃 Hari H! (wajib mood_0, wajah 🤑 gajian)
        R.drawable.mood_4,  // H-1  🥺 -> gambar 😢 (menatap notifikasi transferan)
        R.drawable.mood_3,  // H-2  😭 menangis di akhir bulan
        R.drawable.mood_9,  // H-3  💀 -> gambar ☹️ (keuangan resmi sekarat)
        R.drawable.mood_7,  // H-4  🧟 -> gambar 😔 (bekerja tanpa energi)
        R.drawable.mood_18, // H-5  😵 -> gambar 🙄 (pusing, mata gulir)
        R.drawable.mood_1,  // H-6  😫 dompet benar-benar tipis
        R.drawable.mood_17, // H-7  😖 seminggu menuju gajian terasa berat
        R.drawable.mood_2,  // H-8  😣 -> gambar 😩 (menu mie instan)
        R.drawable.mood_6,  // H-9  😞 keinginan belanja ditahan
        R.drawable.mood_16, // H-10 😓 -> gambar 😳 (berkeringat dingin)
        R.drawable.mood_5,  // H-11 😥 hela napas beli token listrik
        R.drawable.mood_8,  // H-12 😰 -> gambar 🙁 (gelisah saldo menyusut)
        R.drawable.mood_10, // H-13 😟 khawatir pengeluaran mendadak
        R.drawable.mood_11, // H-14 😕 bingung atur sisa uang
        R.drawable.mood_14, // H-15 😶 pertengahan bulan, diam di rumah
        R.drawable.mood_12, // H-16 😑 kembali ke menu warteg
        R.drawable.mood_13, // H-17 😐 menolak nongkrong mahal
        R.drawable.mood_15, // H-18 😬 mulai cemas
        R.drawable.mood_20, // H-19 🙃 -> gambar 🙂 (senyum getir)
        R.drawable.mood_22, // H-20 🧐 -> gambar 😌 (periksa saldo tenang-teliti)
        R.drawable.mood_21, // H-21 🤔 -> gambar 😊 (mulai berpikir mengerem)
        R.drawable.mood_24, // H-22 😮 -> gambar 😄 (kaget lihat auto-debet)
        R.drawable.mood_27, // H-23 🤪 -> gambar 🤭 (mulai kalap belanja)
        R.drawable.mood_31, // H-24 🤩 berbinar lihat promo
        R.drawable.mood_28, // H-25 🥳 -> gambar 😆 (akhir pekan selebrasi)
        R.drawable.mood_23, // H-26 😁 senyum lebar dompet masih tebal
        R.drawable.mood_30, // H-27 🥰 -> gambar 😍 (semua keranjang dicheckout)
        R.drawable.mood_19, // H-28 😎 -> gambar 😏 (nongkrong tanpa lihat harga)
        R.drawable.mood_25  // H-29 🤑 -> gambar 😃 (gajian cair, paling makmur)
    )

    fun forDaysLeft(daysLeft: Int): Int =
        BACKGROUNDS[daysLeft.coerceIn(BACKGROUNDS.indices)]
}
