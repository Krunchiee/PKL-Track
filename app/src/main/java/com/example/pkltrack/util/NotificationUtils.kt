import android.content.Context

object NotificationUtils {
    private const val PREF_NAME = "notif_pref"
    private const val KEY_OPENED = "notification_opened"

    fun hasOpenedNotificationPage(context: Context): Boolean {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        return prefs.getBoolean(KEY_OPENED, false)
    }

    fun setOpenedNotificationPage(context: Context) {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        prefs.edit().putBoolean(KEY_OPENED, true).apply()
    }
}
