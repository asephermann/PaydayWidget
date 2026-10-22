package io.github.asephermann.paydaywidget

import android.content.Context
import android.util.AttributeSet
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.util.Locale

/**
 * Month calendar: a header showing the active month name with left/right
 * arrows to switch months, and a [MonthGridView] below it.
 * Highlights the payday date (filled blue) and today (white with blue outline).
 */
class MonthCalendarView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {

    /** Payday day-of-month (1-31), highlighted in the displayed month. */
    var payday: Int = 25
        set(value) {
            field = value.coerceIn(1, 31)
            grid.payday = field
        }

    /** Called when the user taps a day; parameter is the day-of-month. */
    var onDaySelected: ((Int) -> Unit)? = null

    /** Month currently shown; starts at the running month. */
    var displayedMonth: YearMonth = YearMonth.now()
        private set

    private val monthLabel: TextView
    private val grid: MonthGridView
    private val locale: Locale = Locale.getDefault()

    init {
        orientation = VERTICAL
        inflate(context, R.layout.view_month_calendar, this)

        monthLabel = findViewById(R.id.monthLabel)
        grid = findViewById(R.id.monthGrid)
        grid.month = displayedMonth
        grid.onDaySelected = { day -> onDaySelected?.invoke(day) }

        findViewById<ImageButton>(R.id.btnPrevMonth).setOnClickListener { shiftMonth(-1) }
        findViewById<ImageButton>(R.id.btnNextMonth).setOnClickListener { shiftMonth(1) }
        updateHeader()
    }

    private fun shiftMonth(delta: Int) {
        displayedMonth = displayedMonth.plusMonths(delta.toLong())
        grid.month = displayedMonth
        updateHeader()
    }

    private fun updateHeader() {
        monthLabel.text = displayedMonth
            .format(DateTimeFormatter.ofPattern("MMMM yyyy", locale))
    }
}
