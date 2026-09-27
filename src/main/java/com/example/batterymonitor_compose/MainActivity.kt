package com.example.batterymonitor_compose

import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.batterymonitor_compose.ui.theme.BatteryMonitor_ComposeTheme

const val CUSTOM_ACTION_BATTERY = "com.example.batterymonitor_compose.ACTUALIZAR_BATERIA"

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            BatteryMonitor_ComposeTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    BatteryScreen(modifier = Modifier.padding(innerPadding))
                }
            }
        }
    }
}

@Composable
fun BatteryScreen(modifier: Modifier = Modifier) {

    var porcentaje by remember { mutableStateOf(0) }
    var estaCargando by remember { mutableStateOf(false) }

    val context = LocalContext.current

    DisposableEffect(Unit) {
        val receiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                val nivel = intent?.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) ?: -1
                val escala = intent?.getIntExtra(BatteryManager.EXTRA_SCALE, -1) ?: -1
                val status = intent?.getIntExtra(BatteryManager.EXTRA_STATUS, -1) ?: -1
                if (nivel != -1 && escala != -1) {
                    porcentaje = (nivel * 100) / escala
                }
                estaCargando = status == BatteryManager.BATTERY_STATUS_CHARGING ||
                        status == BatteryManager.BATTERY_STATUS_FULL
            }
        }
        context.registerReceiver(receiver, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
        Log.d("BatteryScreen", "Receiver automático registrado")

        onDispose {
            context.unregisterReceiver(receiver)
            Log.d("BatteryScreen", "Receiver automático desregistrado")
        }
    }

    DisposableEffect(Unit) {
        val manualReceiver = object : BroadcastReceiver() {
            override fun onReceive(ctx: Context?, intent: Intent?) {
                if (intent?.action == CUSTOM_ACTION_BATTERY) {
                    val batteryManager =
                        ctx?.getSystemService(Context.BATTERY_SERVICE) as? BatteryManager
                    val nivel =
                        batteryManager?.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY)
                            ?: -1
                    if (nivel != -1) {
                        porcentaje = nivel
                        Log.d("BatteryScreen", "Actualización manual recibida: $nivel%")
                    }
                    estaCargando = batteryManager?.isCharging ?: false
                }
            }
        }

        val filter = IntentFilter(CUSTOM_ACTION_BATTERY)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            context.registerReceiver(manualReceiver, filter, Context.RECEIVER_NOT_EXPORTED)
        } else {
            context.registerReceiver(manualReceiver, filter)
        }
        Log.d("BatteryScreen", "Receiver manual registrado")

        onDispose {
            context.unregisterReceiver(manualReceiver)
            Log.d("BatteryScreen", "Receiver manual desregistrado")
        }
    }

    Column(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Batería: $porcentaje%",
            fontSize = 32.sp
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = if (estaCargando) "Estado: Cargando ⚡" else "Estado: No cargando",
            fontSize = 18.sp
        )

        Spacer(modifier = Modifier.height(24.dp))

        Button(onClick = {
            val intent = Intent(CUSTOM_ACTION_BATTERY).apply {
                setPackage(context.packageName)
            }
            val pendingIntent = PendingIntent.getBroadcast(
                context,
                0,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            pendingIntent.send()
            Log.d("BatteryScreen", "PendingIntent enviado manualmente")
        }) {
            Text(text = "Actualizar manualmente")
        }
    }
}