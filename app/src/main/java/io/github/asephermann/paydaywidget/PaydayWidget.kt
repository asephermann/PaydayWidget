package io.github.asephermann.paydaywidget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.view.View
import android.widget.RemoteViews
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

/**
 * Implementation of App Widget functionality.
 */
class PaydayWidget : AppWidgetProvider() {

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        val prefs = context.getSharedPreferences("payday_pref", Context.MODE_PRIVATE)
        val payday = prefs.getInt("payday", 25)
        val daysLeft = calculateDaysToPayday(payday)

        // Ikon launcher dinamis mengikuti sisa hari
        LauncherIconUpdater.update(context, daysLeft)

        appWidgetIds.forEach { widgetId ->
            val views = RemoteViews(context.packageName, R.layout.payday_widget)
            val nextPaydayDate = calculateNextPaydayDate(payday)
            val dateFormatter = DateTimeFormatter.ofPattern("dd MMM yyyy")

            // Mood emoji at the bottom-left: the closer to payday, the happier
            views.setTextViewText(R.id.txtMood, MoodEmoji.forDaysLeft(daysLeft))

            if (daysLeft == 0) {
                // Payday day - hide progress and label, show celebration
                views.setViewVisibility(R.id.progressCircle, View.GONE)
                views.setViewVisibility(R.id.txtLabel, View.GONE)
                views.setViewVisibility(R.id.txtDays, View.VISIBLE)
                views.setTextViewText(R.id.txtDays, "🎉")
                views.setTextViewText(R.id.txtPaydayInfo1, "Today is")
                views.setTextViewText(R.id.txtPaydayInfo2, "Payday")
                views.setTextViewText(R.id.txtPaydayInfo3, nextPaydayDate.format(dateFormatter))
                views.setViewVisibility(R.id.txtPaydayInfo1, View.VISIBLE)
                views.setViewVisibility(R.id.txtPaydayInfo2, View.VISIBLE)
                views.setViewVisibility(R.id.txtPaydayInfo3, View.VISIBLE)
            } else if (daysLeft == 1) {
                // One day before payday - show all elements
                views.setViewVisibility(R.id.progressCircle, View.VISIBLE)
                views.setViewVisibility(R.id.txtLabel, View.VISIBLE)
                views.setViewVisibility(R.id.txtDays, View.VISIBLE)
                views.setTextViewText(R.id.txtDays, "🎉")
                views.setTextViewText(R.id.txtPaydayInfo1, "Until your next")
                views.setTextViewText(R.id.txtPaydayInfo2, "Payday")
                views.setTextViewText(R.id.txtPaydayInfo3, "Tomorrow!")
                views.setViewVisibility(R.id.txtPaydayInfo1, View.VISIBLE)
                views.setViewVisibility(R.id.txtPaydayInfo2, View.VISIBLE)
                views.setViewVisibility(R.id.txtPaydayInfo3, View.VISIBLE)
                views.setProgressBar(R.id.progressCircle, 30, 29, false)
            } else {
                // Normal days - show all elements
                views.setViewVisibility(R.id.progressCircle, View.VISIBLE)
                views.setViewVisibility(R.id.txtLabel, View.VISIBLE)
                views.setViewVisibility(R.id.txtDays, View.VISIBLE)
                views.setTextViewText(R.id.txtDays, daysLeft.toString())
                views.setTextViewText(R.id.txtPaydayInfo1, "Until your next")
                views.setTextViewText(R.id.txtPaydayInfo2, "Payday")
                views.setTextViewText(R.id.txtPaydayInfo3, nextPaydayDate.format(dateFormatter))
                views.setViewVisibility(R.id.txtPaydayInfo1, View.VISIBLE)
                views.setViewVisibility(R.id.txtPaydayInfo2, View.VISIBLE)
                views.setViewVisibility(R.id.txtPaydayInfo3, View.VISIBLE)
                views.setProgressBar(R.id.progressCircle, 30, 30 - daysLeft, false)
            }

            val clickIntent = Intent(context, PaydaySettingActivity::class.java)

            val pendingIntent = PendingIntent.getActivity(
                context,
                0,
                clickIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            views.setOnClickPendingIntent(R.id.txtDays, pendingIntent)
            views.setOnClickPendingIntent(R.id.progressCircle, pendingIntent)

            appWidgetManager.updateAppWidget(widgetId, views)
        }
    }

    fun calculateDaysToPayday(payday: Int): Int {
        val today = LocalDate.now()

        val thisMonthValidPayday = DateUtils.getValidPayday(
            today.year,
            today.monthValue,
            payday
        )

        val thisMonthPayday = today.withDayOfMonth(thisMonthValidPayday)

        return when {
            today.isEqual(thisMonthPayday) -> 0

            today.isBefore(thisMonthPayday) ->
                ChronoUnit.DAYS.between(today, thisMonthPayday).toInt()

            else -> {
                val nextMonth = today.plusMonths(1)
                val nextMonthValidPayday = DateUtils.getValidPayday(
                    nextMonth.year,
                    nextMonth.monthValue,
                    payday
                )

                val nextMonthPayday =
                    nextMonth.withDayOfMonth(nextMonthValidPayday)

                ChronoUnit.DAYS.between(today, nextMonthPayday).toInt()
            }
        }
    }

    fun calculateNextPaydayDate(payday: Int): LocalDate {
        val today = LocalDate.now()

        val thisMonthValidPayday = DateUtils.getValidPayday(
            today.year,
            today.monthValue,
            payday
        )

        val thisMonthPayday = today.withDayOfMonth(thisMonthValidPayday)

        return when {
            today.isBefore(thisMonthPayday) || today.isEqual(thisMonthPayday) -> thisMonthPayday
            else -> {
                val nextMonth = today.plusMonths(1)
                val nextMonthValidPayday = DateUtils.getValidPayday(
                    nextMonth.year,
                    nextMonth.monthValue,
                    payday
                )
                nextMonth.withDayOfMonth(nextMonthValidPayday)
            }
        }
    }


}
