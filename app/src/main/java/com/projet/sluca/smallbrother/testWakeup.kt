package com.projet.sluca.smallbrother

import android.app.KeyguardManager
import android.app.admin.DeviceAdminReceiver
import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.Context
import android.os.Build
import android.widget.Toast

fun unlockAndLaunchApp(context: Context) {
    // Check if the device is secured with a PIN code
    val keyguardManager = context.getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager
    if (!keyguardManager.isDeviceSecure) {
        // The device is not secured with a PIN code, so we can launch the app directly
        launchApp(context)
        return
    }

    // Check if the device has a device owner
    val devicePolicyManager = context.getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager
    val componentName = ComponentName(context, DeviceAdminReceiver::class.java)
    if (!devicePolicyManager.isDeviceOwnerApp(context.packageName)) {
        // The device does not have a device owner, so we cannot unlock it
        Toast.makeText(context, "This device does not have a device owner", Toast.LENGTH_SHORT).show()
        return
    }

    // Unlock the device
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        devicePolicyManager.resetPassword("", DevicePolicyManager.RESET_PASSWORD_REQUIRE_ENTRY)
    } else {
        devicePolicyManager.resetPassword("", DevicePolicyManager.RESET_PASSWORD_REQUIRE_ENTRY)
        devicePolicyManager.lockNow()
    }
    // Launch the app
    launchApp(context)
}

fun launchApp(context: Context) {
    val packageName = "com.projet.sluca.smallbrother"
    val launchIntent = context.packageManager.getLaunchIntentForPackage(packageName)
    if (launchIntent != null) {
        context.startActivity(launchIntent)
    } else {
        Toast.makeText(context, "Unable to launch the app", Toast.LENGTH_SHORT).show()
    }
}
