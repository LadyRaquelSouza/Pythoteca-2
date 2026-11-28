package br.com.pythoteca.experimental.reader;
import android.app.*;
import android.content.*;
import android.os.*;
import br.com.pythoteca.protocol.Protocol;
import java.util.concurrent.*;
public class ProtocolTest extends Instrumentation {
    private String mode;
    @Override public void onCreate(Bundle args) { super.onCreate(args); mode=args.getString("mode", "full"); start(); }
    @Override public void onStart() {
        Bundle result = new Bundle();
        HandlerThread thread = new HandlerThread("probe");
        thread.start();
        Context c = getTargetContext();
        BlockingQueue<Messenger> connections = new LinkedBlockingQueue<>();
        CountDownLatch disconnected = new CountDownLatch(1);
        CountDownLatch connected = new CountDownLatch(1);
        BlockingQueue<Message> replies = new LinkedBlockingQueue<>();
        Messenger[] remote = new Messenger[1];
        ServiceConnection conn = new ServiceConnection() {
            public void onServiceConnected(ComponentName n, IBinder b) { remote[0] = new Messenger(b); connections.add(remote[0]); connected.countDown(); }
            public void onServiceDisconnected(ComponentName n) { disconnected.countDown(); }
        };
        boolean bound = false;
        try {
            bound = c.bindService(new Intent().setClassName(Protocol.EXECUTOR, Protocol.SERVICE), conn, Context.BIND_AUTO_CREATE);
            if (!bound || !connected.await(10, TimeUnit.SECONDS)) throw new AssertionError("Bind failed");
            Messenger reply = new Messenger(new Handler(thread.getLooper(), msg -> { replies.add(Message.obtain(msg)); return true; }));
            if ("ping".equals(mode) || "python".equals(mode)) {
                int uid=c.getPackageManager().getPackageUid(Protocol.EXECUTOR,0);
                boolean python="python".equals(mode);
                Message request=Message.obtain(null,python?Protocol.PYTHON_PROBE:Protocol.PING,Protocol.VERSION,1);
                request.replyTo=reply; remote[0].send(request);
                Message response=replies.poll(30,TimeUnit.SECONDS);
                if(response==null || response.what!=(python?Protocol.PYTHON_RESULT:Protocol.PONG) || response.arg1!=Protocol.VERSION || response.arg2!=1 || response.sendingUid!=uid || (python && (!response.getData().containsKey("value") || response.getData().getInt("value")!=42))) throw new AssertionError("Isolated probe failed: "+mode);
                result.putString("stream",python?"PYTHOTECA_ISOLATED_PYTHON_OK":"PYTHOTECA_ISOLATED_PING_OK");
                finish(Activity.RESULT_OK,result);
                return;
            }
            if (!"full".equals(mode)) throw new AssertionError("Unknown mode");
            int[][] invalid = {{999,Protocol.VERSION,1},{Protocol.PING,999,1},{Protocol.PING,Protocol.VERSION,0}};
            for (int[] a : invalid) {
                Message m = Message.obtain(null,a[0],a[1],a[2]); m.replyTo=reply; remote[0].send(m);
                if (replies.poll(2,TimeUnit.SECONDS)!=null) throw new AssertionError("Invalid message accepted");
            }
            remote[0].send(Message.obtain(null,Protocol.PING,Protocol.VERSION,2));
            Message valid=Message.obtain(null,Protocol.PING,Protocol.VERSION,3); valid.replyTo=reply; remote[0].send(valid);
            Message pong=replies.poll(5,TimeUnit.SECONDS);
            if(pong==null || pong.what!=Protocol.PONG || pong.arg1!=Protocol.VERSION || pong.arg2!=3 || pong.sendingUid!=c.getPackageManager().getPackageUid(Protocol.EXECUTOR,0)) throw new AssertionError("Valid control failed");
            Message js = Message.obtain(null, Protocol.JS_PROBE, Protocol.VERSION, 9);
            js.replyTo = reply; remote[0].send(js);
            Message jsResult = replies.poll(20, TimeUnit.SECONDS);
            if (jsResult == null || jsResult.what != Protocol.JS_RESULT || jsResult.arg1 != Protocol.VERSION || jsResult.arg2 != 9 || jsResult.sendingUid != c.getPackageManager().getPackageUid(Protocol.EXECUTOR,0) || jsResult.getData().getInt("value") != 42)
                throw new AssertionError("Fixed JavaScript probe failed");
            android.util.Log.i("PythotecaProbe", "PYTHOTECA_JS_42_OK");
            int[][] probes = {{Protocol.PYTHON_FAIL_PROBE,Protocol.PYTHON_ERROR,0},{Protocol.PYTHON_PROBE,Protocol.PYTHON_RESULT,42},{Protocol.PYTHON_NETWORK_PROBE,Protocol.PYTHON_RESULT,1},{Protocol.PYTHON_PROBE,Protocol.PYTHON_RESULT,42}};
            int session = 10;
            for (int[] p : probes) {
                Message request = Message.obtain(null,p[0],Protocol.VERSION,session);
                request.replyTo=reply; remote[0].send(request);
                Message response=replies.poll(25,TimeUnit.SECONDS);
                if(response==null || response.what!=p[1] || response.arg1!=Protocol.VERSION || response.arg2!=session || response.sendingUid!=c.getPackageManager().getPackageUid(Protocol.EXECUTOR,0)) throw new AssertionError("Probe failed: "+p[0]);
                if(p[1]==Protocol.PYTHON_RESULT && (!response.getData().containsKey("value") || response.getData().getInt("value")!=p[2])) throw new AssertionError("Unexpected probe value: "+p[0]);
                session++;
            }
            connections.clear();
            Message loop = Message.obtain(null,Protocol.PYTHON_ENDLESS_PROBE,Protocol.VERSION,30);
            loop.replyTo=reply; remote[0].send(loop);
            Message started=replies.poll(25,TimeUnit.SECONDS);
            int uid=c.getPackageManager().getPackageUid(Protocol.EXECUTOR,0);
            if(started==null || started.what!=Protocol.PYTHON_STARTED || started.arg1!=Protocol.VERSION || started.arg2!=30 || started.sendingUid!=uid) throw new AssertionError("Loop not started");
            int oldPid=started.getData().getInt("pid");
            long start=SystemClock.elapsedRealtime();
            Message concurrent=Message.obtain(null,Protocol.PYTHON_PROBE,Protocol.VERSION,31);
            concurrent.replyTo=reply; remote[0].send(concurrent);
            Message busy=replies.poll(3,TimeUnit.SECONDS);
            if(busy==null || busy.what!=Protocol.PYTHON_BUSY || busy.arg1!=Protocol.VERSION || busy.arg2!=31 || busy.sendingUid!=uid) throw new AssertionError("Concurrent request not rejected");
            if(!disconnected.await(12,TimeUnit.SECONDS)) throw new AssertionError("Watchdog did not terminate service");
            if(SystemClock.elapsedRealtime()-start<3000) throw new AssertionError("Premature death");
            Messenger recovered=connections.poll(25,TimeUnit.SECONDS);
            if(recovered==null) throw new AssertionError("No reconnect");
            Message again=Message.obtain(null,Protocol.PYTHON_PROBE,Protocol.VERSION,32);
            again.replyTo=reply; recovered.send(again);
            Message answer=replies.poll(25,TimeUnit.SECONDS);
            if(answer==null || answer.what!=Protocol.PYTHON_RESULT || answer.arg1!=Protocol.VERSION || answer.arg2!=32 || answer.sendingUid!=uid || answer.getData().getInt("value")!=42 || oldPid<=0 || answer.getData().getInt("pid")<=0 || answer.getData().getInt("pid")==oldPid) throw new AssertionError("Recovery failed");
            result.putString("stream","PYTHOTECA_WATCHDOG_OK\\nPYTHOTECA_BUSY_OK\\nPYTHOTECA_PROCESS_RECOVERY_OK\\nPYTHOTECA_INVALID_MESSAGES_DENIED\nPYTHOTECA_PYTHON_EXCEPTION_RECOVERED\nPYTHOTECA_PYTHON_NETWORK_DENIED");
            finish(Activity.RESULT_OK,result);
        } catch(Throwable e) { result.putString("stream",e.toString()); finish(Activity.RESULT_CANCELED,result); }
        finally { if(bound)c.unbindService(conn); thread.quitSafely(); }
    }
}
