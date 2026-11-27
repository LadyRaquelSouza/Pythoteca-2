package br.com.pythoteca.experimental.ipctest;
import android.app.*;
import android.content.*;
import android.os.*;
import br.com.pythoteca.protocol.Protocol;
import java.util.concurrent.*;
public class ProbeTest extends Instrumentation {
    @Override public void onCreate(Bundle args) { super.onCreate(args); start(); }
    @Override public void onStart() {
        Bundle result = new Bundle();
        HandlerThread thread = new HandlerThread("probe");
        thread.start();
        Context c = getTargetContext();
        CountDownLatch connected = new CountDownLatch(1);
        CountDownLatch answered = new CountDownLatch(1);
        Messenger[] remote = new Messenger[1];
        ServiceConnection conn = new ServiceConnection() {
            public void onServiceConnected(ComponentName n, IBinder b) { remote[0] = new Messenger(b); connected.countDown(); }
            public void onServiceDisconnected(ComponentName n) {}
        };
        boolean bound = false;
        try {
            if (c.checkSelfPermission(Protocol.PERMISSION) != android.content.pm.PackageManager.PERMISSION_GRANTED)
                throw new AssertionError("Signature permission not granted");
            bound = c.bindService(new Intent().setClassName(Protocol.EXECUTOR, Protocol.SERVICE), conn, Context.BIND_AUTO_CREATE);
            if (!bound || !connected.await(10, TimeUnit.SECONDS)) throw new AssertionError("Bind failed");
            Message m = Message.obtain(null, Protocol.PING, Protocol.VERSION, 1);
            m.replyTo = new Messenger(new Handler(thread.getLooper(), msg -> { answered.countDown(); return true; }));
            remote[0].send(m);
            if (answered.await(3, TimeUnit.SECONDS)) throw new AssertionError("Unauthorized UID received reply");
            result.putString("stream", "PYTHOTECA_FOREIGN_UID_DENIED");
            finish(Activity.RESULT_OK, result);
        } catch (Throwable e) { result.putString("stream", e.toString()); finish(Activity.RESULT_CANCELED, result); }
        finally { if (bound) c.unbindService(conn); thread.quitSafely(); }
    }
}
