
package com.yourco.dispatchone.net
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import org.json.JSONObject
import java.io.File
object Api {
  private val client = OkHttpClient()
  private var httpBase = "https://mdm.calderionmobile.com/api"
  private var wsBase   = "wss://mdm.calderionmobile.com/api/live"
  fun getStationName(): String {
    val req = Request.Builder().url("$httpBase/v1/config/station").build()
    client.newCall(req).execute().use { rsp -> return JSONObject(rsp.body?.string() ?: "{}").optString("name","Station") }
  }
  fun upload(file: File): String {
    val body = MultipartBody.Builder().setType(MultipartBody.FORM)
      .addFormDataPart("file", file.name, file.asRequestBody("image/jpeg".toMediaTypeOrNull())).build()
    val req = Request.Builder().url("$httpBase/v1/media/upload").post(body).build()
    client.newCall(req).execute().use { rsp ->
      val j = JSONObject(rsp.body?.string() ?: "{}"); return j.optString("path","")
    }
  }
  fun gaugeStart(shiftId: Long, photoPath: String) {
    val j = JSONObject().put("shiftId", shiftId).put("photoPath", photoPath)
    val req = Request.Builder().url("$httpBase/v1/gauge/start").post(j.toString().toRequestBody("application/json".toMediaTypeOrNull())).build()
    client.newCall(req).execute().close()
  }
}
