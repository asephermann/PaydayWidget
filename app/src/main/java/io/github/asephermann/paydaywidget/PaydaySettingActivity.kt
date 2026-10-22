package io.github.asephermann.paydaywidget

import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Intent
import android.os.Bundle
import android.util.DisplayMetrics
import android.widget.Button
import android.widget.NumberPicker
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.content.edit

class PaydaySettingActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_payday_setting)

        val prefs = getSharedPreferences("payday_pref", MODE_PRIVATE)
        val picker = findViewById<NumberPicker>(R.id.numberPicker)
        val calendar = findViewById<MonthCalendarView>(R.id.monthCalendar)
        val txtMood = findViewById<TextView>(R.id.txtMood)
        val btnSave = findViewById<Button>(R.id.btnSave)

        picker.minValue = 1
        picker.maxValue = 31
        val payday = prefs.getInt("payday", 25)
        picker.value = payday
        calendar.payday = payday

        val calculator = PaydayWidget()

        // Ikon launcher dinamis mengikuti sisa hari
        LauncherIconUpdater.update(this, calculator.calculateDaysToPayday(payday))

        // Mood emoji at the bottom-left, follows the currently selected payday
        fun updateMood() {
            txtMood.text = MoodEmoji.forDaysLeft(calculator.calculateDaysToPayday(picker.value))
        }

        // Set text size for NumberPicker based on screen width
        val displayMetrics = DisplayMetrics()
        windowManager.defaultDisplay.getMetrics(displayMetrics)
        val screenWidth = displayMetrics.widthPixels
        val textSize = screenWidth / 20f // Adjust divider for desired size
        setNumberPickerTextSize(picker, textSize)

        // NumberPicker -> MonthCalendarView
        picker.setOnValueChangedListener { _, _, newVal ->
            calendar.payday = newVal
            updateMood()
        }

        // MonthCalendarView -> NumberPicker (+ mood ikut ter-update)
        calendar.onDaySelected = { day ->
            if (picker.value != day) picker.value = day
            updateMood()
        }

        updateMood()

        btnSave.setOnClickListener {
            prefs.edit {
                putInt("payday", picker.value)
            }

            updateWidget()
            finish()
        }
    }

    private fun setNumberPickerTextSize(picker: NumberPicker, textSize: Float) {
        val count = picker.childCount
        for (i in 0 until count) {
            val child = picker.getChildAt(i)
            if (child is TextView) {
                child.textSize = textSize
            }
        }
        // Refresh the picker to apply changes
        picker.value = picker.value
    }

    private fun updateWidget() {
        val intent = Intent(this, PaydayWidget::class.java).apply {
            action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
        }

        val ids = AppWidgetManager.getInstance(this)
            .getAppWidgetIds(ComponentName(this, PaydayWidget::class.java))

        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, ids)
        sendBroadcast(intent)
    }
}