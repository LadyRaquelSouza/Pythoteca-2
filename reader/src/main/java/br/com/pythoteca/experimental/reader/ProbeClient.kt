package br.com.pythoteca.experimental.reader

import android.content.*
import android.content.pm.PackageManager
import android.os.*
import br.com.pythoteca.protocol.Protocol

class ProbeClient(private val context: Context, private val report: (String) -> Unit) {
    private val handler = Handler(Looper.getMainLooper())
    private var bound = false
    private var session = 0
    private var operation = Protocol.PING
    private val timeout = Runnable { finish("Tempo limite") }
    private fun trusted(uid: Int): Boolean = try {
        context.packageManager.getPackageUid(Protocol.EXECUTOR, 0) == uid &&
            context.packageManager.checkSignatures(uid, android.os.Process.myUid()) == PackageManager.SIGNATURE_MATCH
    } catch (_: PackageManager.NameNotFoundException) { false }

    private val reply = Messenger(Handler(Looper.getMainLooper()) { msg ->
        if (bound && trusted(msg.sendingUid) && msg.arg1 == Protocol.VERSION && msg.arg2 == session) {
            when {
                operation == Protocol.PING && msg.what == Protocol.PONG -> finish("IPC autenticado: PONG")
                operation == Protocol.PYTHON_PROBE && msg.what == Protocol.PYTHON_RESULT && msg.data.containsKey("value") -> finish("Python: ${msg.data.getInt("value")}")
                operation == Protocol.PYTHON_PROBE && msg.what == Protocol.PYTHON_ERROR -> finish("Falha Python")
            }
        }
        true
    })
    private val connection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName, binder: IBinder) {
            if (!bound) return
            try {
                val uid = context.packageManager.getPackageUid(Protocol.EXECUTOR, 0)
                if (!trusted(uid)) { finish("Executor não confiável"); return }
                val msg = Message.obtain(null, operation, Protocol.VERSION, session)
                msg.replyTo = reply
                Messenger(binder).send(msg)
            } catch (_: Exception) { finish("Falha de comunicação") }
        }
        override fun onServiceDisconnected(name: ComponentName) { finish("Executor desconectado") }
        override fun onBindingDied(name: ComponentName) { finish("Conexão encerrada") }
        override fun onNullBinding(name: ComponentName) { finish("Conexão recusada") }
    }
    fun start(python: Boolean = false) {
        if (bound) return
        operation = if (python) Protocol.PYTHON_PROBE else Protocol.PING
        close()
        session = if (session == Int.MAX_VALUE) 1 else session + 1
        try {
            val uid = context.packageManager.getPackageUid(Protocol.EXECUTOR, 0)
            if (!trusted(uid)) { report("Executor não confiável"); return }
            bound = context.bindService(Intent().setClassName(Protocol.EXECUTOR, Protocol.SERVICE),
                connection, Context.BIND_AUTO_CREATE)
            if (bound) { report("Aguardando resposta"); handler.postDelayed(timeout, if (python) 30000L else 5000L) }
            else report("Executor indisponível")
        } catch (_: Exception) { close(); report("Executor indisponível") }
    }
    private fun finish(status: String) { close(); report(status) }
    fun close() {
        handler.removeCallbacks(timeout)
        if (bound) { bound = false; context.unbindService(connection) }
    }
}
