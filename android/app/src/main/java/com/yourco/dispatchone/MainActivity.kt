
package com.yourco.dispatchone
import android.Manifest
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import com.yourco.dispatchone.net.Api
import com.yourco.dispatchone.camera.PhotoCapture
sealed class UIState { object Idle: UIState(); object GaugeStart: UIState() }
class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) { super.onCreate(savedInstanceState)
    ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.POST_NOTIFICATIONS, Manifest.permission.CAMERA), 100)
    setContent { MaterialTheme { AppScreen() } }
  }
}
@Composable
fun AppScreen() {
  var ui by remember { mutableStateOf<UIState>(UIState.Idle) }
  Surface(Modifier.fillMaxSize()) {
    Column(Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.SpaceBetween) {
      Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
        Text("DispatchOne", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(6.dp))
        Text("Station: " + try { Api.getStationName() } catch (_: Exception) { "Unknown" })
      }
      when (ui) {
        is UIState.Idle -> {
          Button(onClick = { ui = UIState.GaugeStart }) { Text("Start Shift → Gauge Photo") }
        }
        is UIState.GaugeStart -> {
          PhotoCapture { file ->
            try {
              val path = Api.upload(file)
              Api.gaugeStart(123, path)
              ui = UIState.Idle
            } catch (_: Exception) {}
          }
        }
      }
    }
  }
}
