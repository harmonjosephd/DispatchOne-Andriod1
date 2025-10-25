
package com.yourco.dispatchone.nav
import android.content.Context
import android.content.Intent
import android.net.Uri
object NavIntent {
  fun openToStation(ctx: Context, lat: Double, lon: Double) : Boolean {
    try { val uri = Uri.parse("google.navigation:q=$lat,$lon"); val i = Intent(Intent.ACTION_VIEW, uri); i.setPackage("com.google.android.apps.maps"); ctx.startActivity(i); return true } catch (_: Exception) {}
    try { val uri = Uri.parse("waze://?ll=$lat,$lon&navigate=yes"); val i = Intent(Intent.ACTION_VIEW, uri); i.setPackage("com.waze"); ctx.startActivity(i); return true } catch (_: Exception) {}
    return try { val uri = Uri.parse("geo:$lat,$lon?q=Station"); val i = Intent(Intent.ACTION_VIEW, uri); ctx.startActivity(i); true } catch (_: Exception) { false }
  }
}
