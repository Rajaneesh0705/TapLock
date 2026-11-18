package com.taplock.app.admin

import android.app.admin.DeviceAdminReceiver
import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.widget.Toast
import com.taplock.app.R
import com.taplock.app.widget.TapLockWidgetProvider

/**
 * TapLockDeviceAdminReceiver handles Device Administrator callbacks and provides
 * safe API helpers for activating, checking, locking, and removing Device Admin privileges.
 *
 * TapLock exclusively requests the `force-lock` policy declared in `res/xml/device_admin_policies.xml`.
 */
class TapLockDeviceAdminReceiver : DeviceAdminReceiver() {

    override fun onEnabled(context: Context, intent: Intent) {
        super.onEnabled(context, intent)
        Toast.makeText(context, R.string.toast_admin_enabled, Toast.LENGTH_SHORT).show()
        // Refresh all home screen widgets to reflect active state
        TapLockWidgetProvider.updateAllWidgets(context)
    }

    override fun onDisabled(context: Context, intent: Intent) {
        super.onDisabled(context, intent)
        Toast.makeText(context, R.string.toast_admin_disabled, Toast.LENGTH_SHORT).show()
        // Refresh all home screen widgets to reflect inactive state
        TapLockWidgetProvider.updateAllWidgets(context)
    }

    companion object {
        /**
         * Returns the [ComponentName] representing this DeviceAdminReceiver.
         */
        fun getComponentName(context: Context): ComponentName {
            return ComponentName(context.applicationContext, TapLockDeviceAdminReceiver::class.java)
        }

        /**
         * Checks whether TapLock currently has active Device Administrator privileges.
         */
        fun isDeviceAdminActive(context: Context): Boolean {
            val dpm = context.getSystemService(Context.DEVICE_POLICY_SERVICE) as? DevicePolicyManager
            return dpm?.isAdminActive(getComponentName(context)) == true
        }

        /**
         * Constructs the system intent requesting Device Administrator activation.
         */
        fun createAddAdminIntent(context: Context): Intent {
            return Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN).apply {
                putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, getComponentName(context))
                putExtra(
                    DevicePolicyManager.EXTRA_ADD_EXPLANATION,
                    context.getString(R.string.device_admin_explanation)
                )
            }
        }

        /**
         * Safely calls [DevicePolicyManager.lockNow] to turn off and lock the display.
         * Returns [Result.success] on completion, or [Result.failure] if permission is missing
         * or a [SecurityException] occurs.
         */
        fun lockNow(context: Context): Result<Unit> {
            val dpm = context.getSystemService(Context.DEVICE_POLICY_SERVICE) as? DevicePolicyManager
                ?: return Result.failure(IllegalStateException("DevicePolicyManager service is unavailable."))

            if (!isDeviceAdminActive(context)) {
                return Result.failure(SecurityException(context.getString(R.string.error_permission_required)))
            }

            return try {
                dpm.lockNow()
                Result.success(Unit)
            } catch (e: SecurityException) {
                Result.failure(e)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

        /**
         * Removes TapLock as an active Device Administrator so the user can freely uninstall the app.
         */
        fun removeAdmin(context: Context): Boolean {
            val dpm = context.getSystemService(Context.DEVICE_POLICY_SERVICE) as? DevicePolicyManager
                ?: return false
            return try {
                dpm.removeActiveAdmin(getComponentName(context))
                TapLockWidgetProvider.updateAllWidgets(context)
                true
            } catch (e: Exception) {
                false
            }
        }
    }
}
