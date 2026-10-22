package io.github.asephermann.paydaywidget

import java.time.LocalDate
import java.time.YearMonth

object DateUtils {

    fun getValidPayday(year: Int, month: Int, payday: Int): Int {
        val lastDay = YearMonth.of(year, month).lengthOfMonth()
        return payday.coerceAtMost(lastDay)
    }
}
