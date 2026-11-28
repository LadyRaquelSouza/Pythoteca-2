package br.com.pythoteca.experimental.reader

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.mutableStateOf

class MainActivity : ComponentActivity() {
    private val status = mutableStateOf("Testar IPC")
    private lateinit var client: ProbeClient
    override fun onDestroy() { client.close(); super.onDestroy() }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        client = ProbeClient(this) { status.value = it }
        setContent { StudyShell(status.value, { client.start() }, { client.start(true) }) }
    }
}
