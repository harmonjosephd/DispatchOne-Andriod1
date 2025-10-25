
package com.yourco.dispatchone.kiosk
import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.Context
import com.yourco.dispatchone.device.AdminReceiver
object Kiosk {
  fun ensureKiosk(ctx: Context) {
    val dpm = ctx.getSystemService(DevicePolicyManager::class.java)
    val admin = ComponentName(ctx, AdminReceiver::class.java)
    try { dpm.setLockTaskPackages(admin, arrayOf(ctx.packageName)) } catch (_: Exception) {}
  }
}
