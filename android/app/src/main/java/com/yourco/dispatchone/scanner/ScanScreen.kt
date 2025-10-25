
package com.yourco.dispatchone.scanner
import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.camera.core.CameraSelector
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.google.mlkit.vision.barcode.Barcode
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.common.InputImage
import androidx.compose.ui.unit.dp
@Composable
fun ScanScreen(onResult: (String) -> Unit) {
  val ctx = LocalContext.current
  var hasPerm by remember { mutableStateOf(false) }
  val permLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { ok -> hasPerm = ok }
  LaunchedEffect(Unit) { permLauncher.launch(Manifest.permission.CAMERA) }
  if (!hasPerm) { Column(Modifier.fillMaxWidth()) { Text("Camera permission required") }; return }
  val options = BarcodeScannerOptions.Builder().setBarcodeFormats(Barcode.FORMAT_QR_CODE, Barcode.FORMAT_CODE_39).build()
  val scanner = BarcodeScanning.getClient(options)
  val cameraProviderFuture = remember { ProcessCameraProvider.getInstance(ctx) }
  val preview = remember { Preview.Builder().build() }
  val selector = remember { CameraSelector.DEFAULT_BACK_CAMERA }
  AndroidView(modifier = Modifier.fillMaxWidth().height(360.dp), factory = { context ->
    val pv = PreviewView(context)
    cameraProviderFuture.addListener({
      val provider = cameraProviderFuture.get()
      try {
        provider.unbindAll()
        preview.setSurfaceProvider(pv.surfaceProvider)
        val analysis = ImageAnalysis.Builder().build().apply {
          setAnalyzer(java.util.concurrent.Executors.newSingleThreadExecutor()) { imageProxy ->
            val media = imageProxy.image
            if (media != null) {
              val img = InputImage.fromMediaImage(media, imageProxy.imageInfo.rotationDegrees)
              scanner.process(img).addOnSuccessListener { codes ->
                val v = codes.firstOrNull()?.rawValue
                if (v != null) { onResult(v); provider.unbindAll() }
              }.addOnCompleteListener { imageProxy.close() }
            } else imageProxy.close()
          }
        }
        provider.bindToLifecycle(context as androidx.lifecycle.LifecycleOwner, selector, preview, analysis)
      } catch (_: Exception) {}
    }, ContextCompat.getMainExecutor(context)); pv
  })
}
