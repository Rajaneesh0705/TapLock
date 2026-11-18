package com.taplock.app

import android.app.Activity
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import com.taplock.app.admin.TapLockDeviceAdminReceiver

/**
 * LockActionActivity is a translucent, zero-UI Activity that handles direct
 * 1-tap screen lock triggers (Home screen widget and shortcut).
 *
 * It reuses [TapLockDeviceAdminReceiver.lockNow] to lock the phone immediately,
 * or routes the user to [MainActivity] if Device Administrator permission is not yet active.
 */
class LockActionActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        disableActivityTransitions()

        if (TapLockDeviceAdminReceiver.isDeviceAdminActive(this)) {
            TapLockDeviceAdminReceiver.lockNow(this)
                .onFailure { exception ->
                    Toast.makeText(
                        this,
                        exception.localizedMessage ?: getString(R.string.error_security_exception),
                        Toast.LENGTH_LONG
                    ).show()
                    // Redirect to MainActivity to resolve setup or permission issues
                    redirectToMainActivity()
                }
        } else {
            // Permission is inactive: direct the user to the setup screen
            Toast.makeText(
                this,
                getString(R.string.error_permission_required),
                Toast.LENGTH_LONG
            ).show()
            redirectToMainActivity()
        }

        // Close this translucent activity immediately
        finish()
        disableActivityTransitions()
    }

    private fun redirectToMainActivity() {
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        startActivity(intent)
    }

    private fun disableActivityTransitions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            overrideActivityTransition(OVERRIDE_TRANSITION_OPEN, 0, 0)
            overrideActivityTransition(OVERRIDE_TRANSITION_CLOSE, 0, 0)
        } else {
            @Suppress("DEPRECATION")
            overridePendingTransition(0, 0)
        }
    }
}
