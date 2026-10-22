package io.github.asephermann.paydaywidget

import android.content.ComponentName
import android.content.Context
import android.content.pm.PackageManager

/**
 * Mengganti ikon launcher secara dinamis.
 *
 * Android tidak punya API untuk mengubah ikon launcher langsung, jadi
 * dipakai trik 31 activity-alias (LauncherDay0..LauncherDay30) yang
 * masing-masing punya ikon berbeda. Cukup satu alias yang aktif; ikon
 * launcher mengikuti alias aktif.
 *
 * Dipanggil dari [PaydayWidget.onUpdate] (widget refresh harian) dan
 * [MainActivity.onCreate].
 */
object LauncherIconUpdater {

    private const val PREF_KEY = "launcher_alias"

    // Alias yang aktif di AndroidManifest.xml secara default
    private const val DEFAULT_ALIAS = ".LauncherDay18"

    fun update(context: Context, daysLeft: Int) {
        val day = daysLeft.coerceIn(0, 30)
        val targetClassName = "${context.packageName}.LauncherDay$day"

        val prefs = context.getSharedPreferences("payday_pref", Context.MODE_PRIVATE)
        val current = prefs.getString(PREF_KEY, null)
            ?: context.packageName + DEFAULT_ALIAS
        if (current == targetClassName) return

        val pm = context.packageManager

        // Aktifkan alias baru dulu, baru matikan yang lama,
        // agar ikon launcher tidak pernah hilang di sela pergantian.
        pm.setComponentEnabledSetting(
            ComponentName(context, targetClassName),
            PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
            PackageManager.DONT_KILL_APP
        )
        pm.setComponentEnabledSetting(
            ComponentName(context, current),
            PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
            PackageManager.DONT_KILL_APP
        )

        prefs.edit().putString(PREF_KEY, targetClassName).apply()
    }
}
