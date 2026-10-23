package io.github.asephermann.paydaywidget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
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

            // Mood watermark mengikuti sisa hari: makin dekat payday makin ceria.
            // Dipasang sebagai background resource di widgetRoot (bukan bitmap
            // via Binder); background akan di-stretch penuh mengikuti widget.
            views.setInt(
                R.id.widgetRoot,
                "setBackgroundResource",
                MoodFace.forDaysLeft(daysLeft)
            )

            if (daysLeft == 0) {
                // Payday day - progress penuh (biru semua), label disembunyikan
                views.setViewVisibility(R.id.progressCircle, View.VISIBLE)
                views.setImageViewBitmap(R.id.progressCircle, createProgressBitmap(context, 30, 30))
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
                views.setTextViewText(R.id.txtDays, "1")
                views.setTextViewText(R.id.txtLabel, "day")
                views.setTextViewText(R.id.txtPaydayInfo1, "Until your next")
                views.setTextViewText(R.id.txtPaydayInfo2, "Payday")
                views.setTextViewText(R.id.txtPaydayInfo3, "Tomorrow!")
                views.setViewVisibility(R.id.txtPaydayInfo1, View.VISIBLE)
                views.setViewVisibility(R.id.txtPaydayInfo2, View.VISIBLE)
                views.setViewVisibility(R.id.txtPaydayInfo3, View.VISIBLE)
                views.setImageViewBitmap(R.id.progressCircle, createProgressBitmap(context, 29, 30))
            } else {
                // Normal days - show all elements
                views.setViewVisibility(R.id.progressCircle, View.VISIBLE)
                views.setViewVisibility(R.id.txtLabel, View.VISIBLE)
                views.setTextViewText(R.id.txtLabel, "days")
                views.setViewVisibility(R.id.txtDays, View.VISIBLE)
                views.setTextViewText(R.id.txtDays, daysLeft.toString())
                views.setTextViewText(R.id.txtPaydayInfo1, "Until your next")
                views.setTextViewText(R.id.txtPaydayInfo2, "Payday")
                views.setTextViewText(R.id.txtPaydayInfo3, nextPaydayDate.format(dateFormatter))
                views.setViewVisibility(R.id.txtPaydayInfo1, View.VISIBLE)
                views.setViewVisibility(R.id.txtPaydayInfo2, View.VISIBLE)
                views.setViewVisibility(R.id.txtPaydayInfo3, View.VISIBLE)
                views.setImageViewBitmap(
                    R.id.progressCircle,
                    createProgressBitmap(context, 30 - daysLeft, 30)
                )
            }

            val clickIntent = Intent(context, PaydaySettingActivity::class.java)

            val pendingIntent = PendingIntent.getActivity(
                context,
                0,
                clickIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            // Klik di area mana pun pada widget membuka aplikasi
            views.setOnClickPendingIntent(R.id.widgetRoot, pendingIntent)

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

    /**
     * Menggambar ring progress ke Bitmap (untuk RemoteViews, karena shape "ring"
     * XML tidak mendukung ujung arc membulat). Ujung arc biru dibulatkan
     * lewat Paint.Cap.ROUND, dimulai dari jam 12 searah jarum jam.
     */
    private fun createProgressBitmap(context: Context, progress: Int, max: Int): Bitmap {
        // Resolusi dibatasi 2x agar total bitmap RemoteViews tetap ringan
        // (bitmap dikirim via Binder ke launcher, buffer transaksi ~1MB).
        val density = minOf(context.resources.displayMetrics.density, 2f)
        val size = (140f * density).toInt()   // sama dengan FrameLayout 140dp
        val stroke = 12f * density            // sama dengan thickness ring sebelumnya

        val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            strokeWidth = stroke
            strokeCap = Paint.Cap.ROUND
        }

        val inset = stroke / 2f + 1f
        val rect = RectF(inset, inset, size - inset, size - inset)

        // Track abu-abu penuh
        paint.color = Color.parseColor("#E0E0E0")
        canvas.drawOval(rect, paint)

        // Arc biru sesuai progress
        paint.color = Color.parseColor("#3F51B5")
        canvas.drawArc(rect, -90f, 360f * progress / max, false, paint)

        return bitmap
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
