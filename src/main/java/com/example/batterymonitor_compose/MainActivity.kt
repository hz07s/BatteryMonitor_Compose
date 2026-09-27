package com.example.batterymonitor_compose

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import com.example.batterymonitor_compose.ui.theme.BatteryMonitor_ComposeTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            BatteryMonitor_ComposeTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Greeting(
                        name = "Android",
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(text = "Hello $name!", modifier = modifier)
}

@Composable
fun BatteryScreen(modifier: Modifier = Modifier) {

    // Paso 2: Variable de estado para el porcentaje de batería
    var porcentaje by remember { mutableStateOf(0) }

    val context = LocalContext.current

    DisposableEffect(Unit) {
        val receiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                val nivel = intent?.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) ?: -1
                val escala = intent?.getIntExtra(BatteryManager.EXTRA_SCALE, -1) ?: -1
                if (nivel != -1 && escala != -1) {
                    porcentaje = (nivel * 100) / escala
                }
            }
        }

        context.registerReceiver(receiver, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
        Log.d("BatteryScreen", "Receiver registrado")

        onDispose {
            context.unregisterReceiver(receiver)
            Log.d("BatteryScreen", "Receiver desregistrado")
        }
    }

    Text(
        text = "Batería: $porcentaje%",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    BatteryMonitor_ComposeTheme {
        Greeting("Android")
    }
}