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

    private final AtomicBoolean running = new AtomicBoolean();
    private final Messenger endpoint = new Messenger(new Handler(Looper.getMainLooper(), msg -> {
        if (!trusted(msg.sendingUid) || (msg.what != Protocol.PING && msg.what != Protocol.PYTHON_PROBE)
                || msg.arg1 != Protocol.VERSION || msg.arg2 <= 0 || msg.replyTo == null) {
            return true;
        }
        if (msg.what == Protocol.PYTHON_PROBE) {
            if (!running.compareAndSet(false, true)) return true;
            final Messenger target = msg.replyTo;
            final int session = msg.arg2;
            new Thread(() -> {
                Message response;
                try {
                    if (!Python.isStarted()) Python.start(new AndroidPlatform(getApplicationContext()));
                    int value = Python.getInstance().getModule("probe").callAttr("answer").toInt();
                    response = Message.obtain(null, Protocol.PYTHON_RESULT, Protocol.VERSION, session);
                    Bundle data = new Bundle(); data.putInt("value", value); response.setData(data);
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
