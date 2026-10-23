package io.github.asephermann.paydaywidget

/**
 * Scale of 30 emojis for the payday countdown, arranged as a monthly cycle
 * of 5 phases (H-29 s.d. H-0).
 *
 * Urutan dan emojinya SAMA dengan MoodFace (background widget): bila emoji
 * panduan tidak memiliki gambar drawable, dipakai emoji hasil substitusi
 * agar yang tampil di aplikasi sama dengan gambar background widget.
 *
 *  🤑 Fase Euphoria        (H-29..H-23): gajian cair, dompet tebal
 *  🧾 Fase Realita         (H-22..H-16): auto-debet mulai masuk
 *  😐 Fase Bertahan Hidup  (H-15..H-9):  saldo menyusut
 *  🧟 Fase Tanggal Tua     (H-8..H-2):   sekarat mie instan
 *  🌅 Fase Hilal Terlihat  (H-1..H-0):   gajian kelihatan, Hari H senyum
 *
 * Index list = sisa hari (index 0 = H-0). H-30 (kasus langka) clamp ke H-29.
 */
object MoodEmoji {

    private val EMOJIS = listOf(
        "🤑", // H-0  Hari H! Gajian cair (mood_0)
        "😢", // H-1  Menatap layar HP berharap notifikasi transferan masuk (mood_4)
        "😭", // H-2  Menangis meratapi nasib di akhir bulan (mood_3)
        "☹️", // H-3  Kondisi keuangan resmi sekarat (mood_9)
        "😔", // H-4  Bekerja seperti zombie tanpa energi (mood_7)
        "🙄", // H-5  Pusing memikirkan cara bertahan hidup (mood_18)
        "😫", // H-6  Mengeluh karena dompet benar-benar tipis (mood_1)
        "😖", // H-7  Satu minggu menuju gajian terasa sangat berat (mood_17)
        "😩", // H-8  Menderita, menu makan berganti mie instan (mood_2)
        "😞", // H-9  Sedih, keinginan belanja terpaksa ditahan (mood_6)
        "😳", // H-10 Mulai berkeringat dingin melihat angka di rekening (mood_16)
        "😥", // H-11 Menghela napas saat harus beli token listrik (mood_5)
        "🙁", // H-12 Gelisah karena saldo menyusut lebih cepat (mood_8)
        "😟", // H-13 Khawatir ada pengeluaran mendadak (mood_10)
        "😕", // H-14 Mulai bingung mengatur sisa uang belanja (mood_11)
        "😶", // H-15 Tepat pertengahan bulan, memilih diam di rumah (mood_14)
        "😑", // H-16 Makan siang kembali ke menu warteg biasa (mood_12)
        "😐", // H-17 Mode datar, menolak ajakan nongkrong mahal (mood_13)
        "😬", // H-18 Mulai cemas, sadar pertengahan bulan masih jauh (mood_15)
        "🙂", // H-19 Tersenyum getir melihat sisa uang tinggal setengah (mood_20)
        "😌", // H-20 Memeriksa sisa saldo ATM dengan teliti (mood_22)
        "😊", // H-21 Mulai berpikir untuk mengerem pengeluaran (mood_21)
        "😄", // H-22 Terkejut melihat notifikasi auto-debet tagihan (mood_24)
        "🤭", // H-23 Mulai kalap belanja hal-hal tidak penting (mood_27)
        "🤩", // H-24 Mata berbinar melihat barang promosi (mood_31)
        "😆", // H-25 Akhir pekan penuh selebrasi (mood_28)
        "😁", // H-26 Senyum lebar, dompet masih tebal (mood_23)
        "😍", // H-27 Bahagia, semua keranjang belanjaan di-checkout (mood_30)
        "😏", // H-28 Nongkrong tanpa melihat harga (mood_19)
        "😃"  // H-29 Gajian cair, merasa paling makmur (mood_25)
    )

    /** Emoji untuk sisa hari menuju payday; H-30 (kasus langka) clamp ke H-29. */
    fun forDaysLeft(daysLeft: Int): String =
        EMOJIS[daysLeft.coerceIn(EMOJIS.indices)]
}
