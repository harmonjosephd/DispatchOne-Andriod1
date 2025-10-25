
package com.yourco.dispatchone.boot
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
class BootReceiver : BroadcastReceiver() {
  override fun onReceive(context: Context, intent: Intent) {
    if (Intent.ACTION_BOOT_COMPLETED == intent.action) {
      val launch = context.packageManager.getLaunchIntentForPackage(context.packageName)
      launch?.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
      context.startActivity(launch)
    }
  }
}
