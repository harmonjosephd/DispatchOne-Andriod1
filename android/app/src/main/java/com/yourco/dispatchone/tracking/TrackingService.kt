
package com.yourco.dispatchone.tracking
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.location.Location
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.google.android.gms.location.*
import kotlinx.coroutines.*
import okhttp3.*
import org.json.JSONObject
class TrackingService : Service() {
  private lateinit var fused: FusedLocationProviderClient
  private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
  private val client = OkHttpClient()
  private val apiBase = "https://mdm.calderionmobile.com/api"
  override fun onCreate() {
    super.onCreate()
    fused = LocationServices.getFusedLocationProviderClient(this)
    val chanId = "tracking"
    val nm = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
      nm.createNotificationChannel(NotificationChannel(chanId, "Tracking", NotificationManager.IMPORTANCE_LOW))
    }
    val notif = NotificationCompat.Builder(this, chanId)
      .setContentTitle("DispatchOne").setContentText("Tracking active")
      .setSmallIcon(android.R.drawable.ic_menu_mylocation).setOngoing(true).build()
    startForeground(42, notif)
    scope.launch {
      while (isActive) {
        try {
          fused.getCurrentLocation(Priority.PRIORITY_BALANCED_POWER_ACCURACY, CancellationTokenSource().token)
            .addOnSuccessListener { loc -> if (loc != null) postLocation(loc) }
        } catch (_: SecurityException) {}
        delay(60000L)
      }
    }
  }
  private fun postLocation(loc: Location) {
    val json = JSONObject().put("deviceId", 1).put("shiftId", 0)
      .put("lat", loc.latitude).put("lon", loc.longitude).put("ts", System.currentTimeMillis())
    val req = Request.Builder().url("$apiBase/v1/locations")
      .post(RequestBody.create("application/json".toMediaTypeOrNull(), json.toString())).build()
    client.newCall(req).enqueue(object: Callback { override fun onFailure(call: Call, e: java.io.IOException) {}
      override fun onResponse(call: Call, response: Response) { response.close() } })
  }
  override fun onDestroy() { super.onDestroy(); scope.cancel() }
  override fun onBind(intent: Intent?): IBinder? = null
}
