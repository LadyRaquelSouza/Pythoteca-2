package br.com.pythoteca.experimental.executor;

import android.app.Service;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.*;
import br.com.pythoteca.protocol.Protocol;
import com.chaquo.python.Python;
import com.chaquo.python.android.AndroidPlatform;
import java.util.concurrent.atomic.AtomicBoolean;

public final class ProbeService extends Service {
    private boolean trusted(int uid) {
        try {
            return getPackageManager().getPackageUid(Protocol.READER, 0) == uid
                && getPackageManager().checkSignatures(uid, android.os.Process.myUid())
                    == PackageManager.SIGNATURE_MATCH;
        } catch (PackageManager.NameNotFoundException e) {
            return false;
        }
    }

    private static final AtomicBoolean running = new AtomicBoolean();
    private final Messenger endpoint = new Messenger(new Handler(Looper.getMainLooper(), msg -> {
        if (!trusted(msg.sendingUid) || (msg.what != Protocol.PING && msg.what != Protocol.PYTHON_PROBE && msg.what != Protocol.PYTHON_FAIL_PROBE && msg.what != Protocol.PYTHON_NETWORK_PROBE && msg.what != Protocol.PYTHON_ENDLESS_PROBE)
                || msg.arg1 != Protocol.VERSION || msg.arg2 <= 0 || msg.replyTo == null) {
            return true;
        }
        if (msg.what != Protocol.PING) {
            if (!running.compareAndSet(false, true)) {
                try { msg.replyTo.send(Message.obtain(null, Protocol.PYTHON_BUSY, Protocol.VERSION, msg.arg2)); }
                catch (RemoteException ignored) {}
                return true;
            }
            final Messenger target = msg.replyTo;
            final int session = msg.arg2;
            final boolean endless = msg.what == Protocol.PYTHON_ENDLESS_PROBE;
            final String function = endless ? "endless" : msg.what == Protocol.PYTHON_FAIL_PROBE ? "fail" : msg.what == Protocol.PYTHON_NETWORK_PROBE ? "network_denied" : "answer";
            new Thread(() -> {
                Message response;
                try {
                    android.util.Log.i("PythonProbe", "session=" + session + " runtime initialization begin");
                    if (!Python.isStarted()) Python.start(new AndroidPlatform(getApplicationContext()));
                    android.util.Log.i("PythonProbe", "session=" + session + " runtime initialization complete");
                    if (endless) {
                        new Handler(Looper.getMainLooper()).postDelayed(() -> android.os.Process.killProcess(android.os.Process.myPid()), 5000);
                        Message started = Message.obtain(null, Protocol.PYTHON_STARTED, Protocol.VERSION, session);
                        Bundle info = new Bundle(); info.putInt("pid", android.os.Process.myPid()); started.setData(info);
                        try { target.send(started); } catch (RemoteException ignored) {}
                    }
                    android.util.Log.i("PythonProbe", "session=" + session + " module call begin");
                    int value = Python.getInstance().getModule("probe").callAttr(function).toInt();
                    android.util.Log.i("PythonProbe", "session=" + session + " module call complete");
                    response = Message.obtain(null, Protocol.PYTHON_RESULT, Protocol.VERSION, session);
                    Bundle data = new Bundle(); data.putInt("value", value); data.putInt("pid", android.os.Process.myPid()); response.setData(data);
                } catch (Exception e) {
                    android.util.Log.e("PythonProbe", "Probe failed", e);
                    response = Message.obtain(null, Protocol.PYTHON_ERROR, Protocol.VERSION, session);
                } finally { running.set(false); }
                try { target.send(response); } catch (RemoteException ignored) {}
            }, "python-probe").start();
            return true;
        }
        Message response = Message.obtain(null, Protocol.PONG, Protocol.VERSION, msg.arg2);
        try { msg.replyTo.send(response); }
        catch (RemoteException ignored) {}
        return true;
    }));

    @Override public IBinder onBind(Intent intent) { return endpoint.getBinder(); }
}
