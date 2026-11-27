package br.com.pythoteca.experimental.reader

import android.os.Bundle
import androidx.compose.runtime.mutableStateOf
import androidx.compose.material3.Button
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier

class MainActivity : ComponentActivity() {
    private val status = mutableStateOf("Testar IPC")
    private lateinit var client: ProbeClient
    override fun onDestroy() { client.close(); super.onDestroy() }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        client = ProbeClient(this) { status.value = it }
        setContent {
            MaterialTheme {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Button(onClick = { client.start() }) { Text(status.value) }
                        Button(onClick = { client.start(true) }) { Text("Testar Python") }
                    }
                }
            }
        }
    }
}
