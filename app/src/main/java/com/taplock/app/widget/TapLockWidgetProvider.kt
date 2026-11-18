package com.taplock.app.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.widget.RemoteViews
import android.widget.Toast
import com.taplock.app.LockActionActivity
import com.taplock.app.MainActivity
import com.taplock.app.R
import com.taplock.app.admin.TapLockDeviceAdminReceiver

/**
 * TapLockWidgetProvider powers the 1x1 Home screen widget.
 *
 * Requirements & Compatibility addressed:
 * - One-tap screen lock directly from the Home screen using AppWidgetProvider & RemoteViews.
 * - Reuses existing [TapLockDeviceAdminReceiver.lockNow] via [LockActionActivity] without code duplication.
 * - Uses [PendingIntent.getActivity] to route clicks through [LockActionActivity], guaranteeing full
 *   compliance with Android 10-15 Background Activity Launch (BAL) restrictions when inactive.
 * - If permission is inactive, routes the user to [MainActivity] to complete setup.
 * - Dynamic appearance refresh when permission is granted or revoked.
 */
class TapLockWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        for (appWidgetId in appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId)
        }
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)

        // Handles any direct broadcast triggers sent to the provider
        if (intent.action == ACTION_LOCK_CLICK) {
            if (TapLockDeviceAdminReceiver.isDeviceAdminActive(context)) {
                TapLockDeviceAdminReceiver.lockNow(context)
                    .onFailure { exception ->
                        Toast.makeText(
                            context,
                            exception.localizedMessage ?: context.getString(R.string.error_security_exception),
                            Toast.LENGTH_LONG
                        ).show()
                        openMainActivity(context)
                    }
            } else {
                Toast.makeText(
                    context,
                    context.getString(R.string.error_permission_required),
                    Toast.LENGTH_LONG
                ).show()
                openMainActivity(context)
            }
        }
    }

    private fun openMainActivity(context: Context) {
        val launchIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        context.startActivity(launchIntent)
    }

    companion object {
        const val ACTION_LOCK_CLICK = "com.taplock.app.widget.ACTION_LOCK_CLICK"

        /**
         * Updates a single widget instance with current admin state, iconography, and pending intent.
         */
        fun updateAppWidget(
            context: Context,
            appWidgetManager: AppWidgetManager,
            appWidgetId: Int
        ) {
            val isActive = TapLockDeviceAdminReceiver.isDeviceAdminActive(context)
            val views = RemoteViews(context.packageName, R.layout.widget_lock)

            if (isActive) {
                views.setImageViewResource(R.id.widget_icon, R.drawable.ic_widget_lock)
                views.setTextViewText(R.id.widget_label, context.getString(R.string.widget_label_active))
                views.setTextColor(R.id.widget_label, Color.parseColor("#F8FAFC"))
            } else {
                views.setImageViewResource(R.id.widget_icon, R.drawable.ic_widget_lock_inactive)
                views.setTextViewText(R.id.widget_label, context.getString(R.string.widget_label_inactive))
                views.setTextColor(R.id.widget_label, Color.parseColor("#FDE68A"))
            }

            // Using PendingIntent.getActivity to LockActionActivity ensures reliable execution
            // and complies fully with Android 10-15 Background Activity Start (BAL) restrictions
            val clickIntent = Intent(context, LockActionActivity::class.java).apply {
                action = Intent.ACTION_VIEW
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            }
            val pendingIntent = PendingIntent.getActivity(
                context,
                appWidgetId,
                clickIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.widget_container, pendingIntent)

            appWidgetManager.updateAppWidget(appWidgetId, views)
        }

        /**
         * Helper to refresh all placed instances of TapLock widgets across the device.
         */
        fun updateAllWidgets(context: Context) {
            val appWidgetManager = AppWidgetManager.getInstance(context) ?: return
            val component = ComponentName(context, TapLockWidgetProvider::class.java)
            val appWidgetIds = appWidgetManager.getAppWidgetIds(component)
            if (appWidgetIds != null && appWidgetIds.isNotEmpty()) {
                for (id in appWidgetIds) {
                    updateAppWidget(context, appWidgetManager, id)
                }
            }
        }
    }
}
