package io.github.asephermann.paydaywidget

/**
 * Scale of 31 emojis for the payday countdown.
 * Index 0 (31 days to payday) = worst mood, index 30 (1 day to payday) = happiest.
 */
object MoodEmoji {

    private val EMOJIS = listOf(
        "😫", "😩", "😭", "😢", "😥",   // 31-27 days
        "😞", "😔", "🙁", "☹️", "😟",   // 26-22 days
        "😕", "😑", "😐", "😶", "😬",   // 21-17 days
        "😳", "😖", "🙄", "😏", "🙂",   // 16-12 days
        "😊", "😌", "😁", "😄", "😃",   // 11-7 days
        "😂", "🤭", "😆", "🤗", "😍",   // 6-2 days
        "🤩"                             // 1 day to payday
    )

    /** Emoji for the given days left until payday; 0 means payday is today. */
    fun forDaysLeft(daysLeft: Int): String = when {
        daysLeft <= 0 -> "🤑"
        else -> EMOJIS[(daysLeft - 1).coerceIn(EMOJIS.indices)]
    }
}
