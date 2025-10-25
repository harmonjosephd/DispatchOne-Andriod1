
package com.yourco.dispatchone.camera
import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.camera.core.CameraSelector
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import androidx.compose.ui.unit.dp
@Composable
fun PhotoCapture(onCaptured: (File) -> Unit) {
  val ctx = LocalContext.current
  var hasPerm by remember { mutableStateOf(false) }
  val permLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { ok -> hasPerm = ok }
  LaunchedEffect(Unit) { permLauncher.launch(Manifest.permission.CAMERA) }
  if (!hasPerm) { Column(Modifier.fillMaxWidth()) { Text("Camera permission required") }; return }
  val cameraProviderFuture = remember { ProcessCameraProvider.getInstance(ctx) }
  val preview = remember { Preview.Builder().build() }
  val imageCapture = remember { ImageCapture.Builder().build() }
  val selector = remember { CameraSelector.DEFAULT_BACK_CAMERA }
  Column(Modifier.fillMaxWidth()) {
    AndroidView( modifier = Modifier.fillMaxWidth().height(360.dp), factory = { context ->
      val pv = PreviewView(context)
      cameraProviderFuture.addListener({
        val cameraProvider = cameraProviderFuture.get()
        try {
          cameraProvider.unbindAll()
          preview.setSurfaceProvider(pv.surfaceProvider)
          cameraProvider.bindToLifecycle(context as androidx.lifecycle.LifecycleOwner, selector, preview, imageCapture)
        } catch (_: Exception) {}
      }, ContextCompat.getMainExecutor(context)); pv
    })
    Spacer(Modifier.height(8.dp))
    Button(onClick = {
      val photoFile = createFile(ctx)
      val output = ImageCapture.OutputFileOptions.Builder(photoFile).build()
      imageCapture.takePicture(output, ContextCompat.getMainExecutor(ctx),
        object: ImageCapture.OnImageSavedCallback {
          override fun onError(exc: ImageCaptureException) { }
          override fun onImageSaved(output: ImageCapture.OutputFileResults) { onCaptured(photoFile) }
        })
    }) { Text("Capture Photo") }
  }
}
private fun createFile(ctx: android.content.Context): File {
  val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
  val dir = File(ctx.cacheDir, "photos"); if (!dir.exists()) dir.mkdirs()
  return File(dir, "IMG_${'$'}{timeStamp}.jpg")
}
