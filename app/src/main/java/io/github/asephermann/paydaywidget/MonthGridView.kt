package io.github.asephermann.paydaywidget

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.TextStyle
import java.time.temporal.WeekFields
import java.util.Locale

/**
 * Grid of days for a single month with a weekday header row.
 * Highlights the payday date (filled blue circle) and today's date
 * (white circle with a thin blue outline). Tapping a day selects it.
 */
class MonthGridView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    /** Month being displayed; day count follows this month. */
    var month: YearMonth = YearMonth.now()
        set(value) {
            field = value
            computeOffset()
            invalidate()
        }

    /** Payday day-of-month (1-31), highlighted when it falls in the displayed month. */
    var payday: Int = 25
        set(value) {
            field = value.coerceIn(1, 31)
            invalidate()
        }

    /** Called when the user taps a day; parameter is the day-of-month. */
    var onDaySelected: ((Int) -> Unit)? = null

    private val today: LocalDate = LocalDate.now()
    private val locale: Locale = Locale.getDefault()
    private val blue = context.getColor(R.color.payday_blue)
    private val darkText = Color.parseColor("#212121")
    private val grayText = Color.parseColor("#757575")

    private val headerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        textAlign = Paint.Align.CENTER
        color = grayText
    }
    private val dayPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        textAlign = Paint.Align.CENTER
        color = darkText
    }
    private val fillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        color = blue
    }
    private val strokePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        color = blue
    }

    private var cellSize = 0f
    private var headerHeight = 0f
    private var dayOffset = 0

    init {
        computeOffset()
    }

    private fun computeOffset() {
        dayOffset = (month.atDay(1).dayOfWeek.value -
                WeekFields.of(locale).firstDayOfWeek.value + 7) % 7
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val width = resolveSize(suggestedMinimumWidth, widthMeasureSpec)
        // 1 weekday header row + up to 6 day rows, square cells
        val desiredHeight = (width / 7f * 7).toInt()
        val height = resolveSize(desiredHeight, heightMeasureSpec)
        setMeasuredDimension(width, height)
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        cellSize = w / 7f
        headerHeight = cellSize
        headerPaint.textSize = cellSize * 0.26f
        dayPaint.textSize = cellSize * 0.34f
        strokePaint.strokeWidth = cellSize * 0.05f
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val firstDayOfWeek = WeekFields.of(locale).firstDayOfWeek

        // Weekday header row
        var dow = firstDayOfWeek
        for (col in 0 until 7) {
            val cx = col * cellSize + cellSize / 2f
            val baseline = headerHeight / 2f - (headerPaint.ascent() + headerPaint.descent()) / 2f
            canvas.drawText(dow.getDisplayName(TextStyle.SHORT, locale), cx, baseline, headerPaint)
            dow = dow.plus(1)
        }

        val daysInMonth = month.lengthOfMonth()
        val validPayday = DateUtils.getValidPayday(month.year, month.monthValue, payday)
        val radius = cellSize * 0.42f
        val isCurrentMonth = month == YearMonth.from(today)

        for (day in 1..daysInMonth) {
            val index = dayOffset + day - 1
            val row = index / 7
            val col = index % 7
            val cx = col * cellSize + cellSize / 2f
            val cy = headerHeight + row * cellSize + cellSize / 2f

            val isPayday = day == validPayday
            val isToday = isCurrentMonth && day == today.dayOfMonth

            when {
                isPayday && isToday -> {
                    // Payday and today: dark blue fill with thin white outline
                    canvas.drawCircle(cx, cy, radius, fillPaint)
                    strokePaint.color = Color.WHITE
                    canvas.drawCircle(cx, cy, radius - strokePaint.strokeWidth / 2f, strokePaint)
                    strokePaint.color = blue
                    dayPaint.color = Color.WHITE
                }
                isPayday -> {
                    // Payday: dark (filled blue)
                    canvas.drawCircle(cx, cy, radius, fillPaint)
                    dayPaint.color = Color.WHITE
                }
                isToday -> {
                    // Today: bright (white with thin blue outline)
                    canvas.drawCircle(cx, cy, radius - strokePaint.strokeWidth / 2f, strokePaint)
                    dayPaint.color = darkText
                }
                else -> dayPaint.color = darkText
            }

            dayPaint.typeface = if (isPayday) {
                Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            } else {
                Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            }
            val baseline = cy - (dayPaint.ascent() + dayPaint.descent()) / 2f
            canvas.drawText(day.toString(), cx, baseline, dayPaint)
        }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (event.action == MotionEvent.ACTION_UP) {
            val col = (event.x / cellSize).toInt()
            val row = ((event.y - headerHeight) / cellSize).toInt()
            if (col in 0..6 && row in 0..5) {
                val day = row * 7 + col - dayOffset + 1
                if (day in 1..month.lengthOfMonth()) {
                    payday = day
                    onDaySelected?.invoke(day)
                    return performClick()
                }
            }
        }
        return super.onTouchEvent(event)
    }

    override fun performClick(): Boolean {
        super.performClick()
        return true
    }
}
