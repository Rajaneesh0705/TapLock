package com.taplock.app

import android.content.Intent
import android.content.pm.ShortcutInfo
import android.content.pm.ShortcutManager
import android.graphics.drawable.Icon
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.taplock.app.admin.TapLockDeviceAdminReceiver
import com.taplock.app.ui.TapLock
import com.taplock.app.ui.theme.TapLockTheme
import com.taplock.app.widget.TapLockWidgetProvider

/**
 * MainActivity is the primary entry point for TapLock.
 *
 * In Part 4, it ensures:
 * 1. Hosts the setup and settings Compose UI.
 * 2. Manages Device Administrator activation & deactivation cleanly.
 * 3. Keeps Home screen 1x1 widgets synchronized with the permission state.
 * 4. Enables pinning the direct "Lock screen" shortcut to the Home screen without unhandled callbacks.
 */
class MainActivity : ComponentActivity() {

    private var isAdminActive by mutableStateOf(false)
    private var errorMessage by mutableStateOf<String?>(null)

    // Activity result launcher to observe when user returns from the Device Admin activation screen
    private val addAdminLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) {
        refreshAdminState()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        refreshAdminState()

        setContent {
            TapLockTheme {
                TapLock(
                    isDeviceAdminActive = isAdminActive,
                    errorMessage = errorMessage,
                    onClearError = { errorMessage = null },
                    onActivateAdmin = {
                        launchActivateAdmin()
                    },
                    onLockNow = {
                        handleLockNow()
                    },
                    onDeactivateAdmin = {
                        handleDeactivateAdmin()
                    },
                    onPinShortcut = {
                        pinLockShortcut()
                    }
                )
            }
        }
    }

    override fun onResume() {
        super.onResume()
        // Re-check and refresh state whenever the activity resumes (e.g., returning from Settings)
        refreshAdminState()
    }

    private fun refreshAdminState() {
        isAdminActive = TapLockDeviceAdminReceiver.isDeviceAdminActive(this)
        if (isAdminActive) {
            errorMessage = null
        }
        // Keep all Home screen widgets refreshed with current status
        TapLockWidgetProvider.updateAllWidgets(this)
    }

    private fun launchActivateAdmin() {
        errorMessage = null
        val intent = TapLockDeviceAdminReceiver.createAddAdminIntent(this)
        addAdminLauncher.launch(intent)
    }

    private fun handleLockNow() {
        TapLockDeviceAdminReceiver.lockNow(this)
            .onSuccess {
                errorMessage = null
            }
            .onFailure { exception ->
                val message = exception.localizedMessage ?: getString(R.string.error_security_exception)
                errorMessage = message
                Toast.makeText(this, message, Toast.LENGTH_LONG).show()
                refreshAdminState()
            }
    }

    private fun handleDeactivateAdmin() {
        val removed = TapLockDeviceAdminReceiver.removeAdmin(this)
        refreshAdminState()
        if (!removed) {
            val message = "Unable to remove Device Administrator automatically. Please deactivate it in Settings > Security > Device admin apps."
            errorMessage = message
            Toast.makeText(this, message, Toast.LENGTH_LONG).show()
        }
    }

    /**
     * Requests the system launcher (e.g. Samsung One UI Home) to pin the 1-tap "Lock screen" shortcut.
     */
    private fun pinLockShortcut() {
        val shortcutManager = getSystemService(ShortcutManager::class.java)
        if (shortcutManager != null && shortcutManager.isRequestPinShortcutSupported) {
            val shortcutIntent = Intent(this, LockActionActivity::class.java).apply {
                action = Intent.ACTION_VIEW
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            }
            val pinShortcutInfo = ShortcutInfo.Builder(this, "pinned_lock_shortcut")
                .setIcon(Icon.createWithResource(this, R.drawable.ic_shortcut_lock))
                .setShortLabel(getString(R.string.shortcut_lock_short))
                .setLongLabel(getString(R.string.shortcut_lock_long))
                .setIntent(shortcutIntent)
                .build()

            // Passing null for IntentSender lets the system launcher display its native prompt
            shortcutManager.requestPinShortcut(pinShortcutInfo, null)
        } else {
            Toast.makeText(this, R.string.shortcut_not_supported_toast, Toast.LENGTH_LONG).show()
        }
    }
}
