package br.com.pythoteca.experimental.reader;
import android.app.*;
import android.content.*;
import android.os.*;
import br.com.pythoteca.protocol.Protocol;
import java.util.concurrent.*;
public class ProtocolTest extends Instrumentation {
    @Override public void onCreate(Bundle args) { super.onCreate(args); start(); }
    @Override public void onStart() {
        Bundle result = new Bundle();
        HandlerThread thread = new HandlerThread("probe");
        thread.start();
        Context c = getTargetContext();
        CountDownLatch connected = new CountDownLatch(1);
        BlockingQueue<Message> replies = new LinkedBlockingQueue<>();
        Messenger[] remote = new Messenger[1];
        ServiceConnection conn = new ServiceConnection() {
            public void onServiceConnected(ComponentName n, IBinder b) { remote[0] = new Messenger(b); connected.countDown(); }
            public void onServiceDisconnected(ComponentName n) {}
        };
        boolean bound = false;
        try {
            bound = c.bindService(new Intent().setClassName(Protocol.EXECUTOR, Protocol.SERVICE), conn, Context.BIND_AUTO_CREATE);
            if (!bound || !connected.await(10, TimeUnit.SECONDS)) throw new AssertionError("Bind failed");
            Messenger reply = new Messenger(new Handler(thread.getLooper(), msg -> { replies.add(Message.obtain(msg)); return true; }));
            int[][] invalid = {{999,Protocol.VERSION,1},{Protocol.PING,999,1},{Protocol.PING,Protocol.VERSION,0}};
            for (int[] a : invalid) {
                Message m = Message.obtain(null,a[0],a[1],a[2]); m.replyTo=reply; remote[0].send(m);
                if (replies.poll(2,TimeUnit.SECONDS)!=null) throw new AssertionError("Invalid message accepted");
            }
            remote[0].send(Message.obtain(null,Protocol.PING,Protocol.VERSION,2));
            Message valid=Message.obtain(null,Protocol.PING,Protocol.VERSION,3); valid.replyTo=reply; remote[0].send(valid);
            Message pong=replies.poll(5,TimeUnit.SECONDS);
            if(pong==null || pong.what!=Protocol.PONG || pong.arg1!=Protocol.VERSION || pong.arg2!=3 || pong.sendingUid!=c.getPackageManager().getPackageUid(Protocol.EXECUTOR,0)) throw new AssertionError("Valid control failed");
            result.putString("stream","PYTHOTECA_INVALID_MESSAGES_DENIED");
            finish(Activity.RESULT_OK,result);
        } catch(Throwable e) { result.putString("stream",e.toString()); finish(Activity.RESULT_CANCELED,result); }
        finally { if(bound)c.unbindService(conn); thread.quitSafely(); }
    }
}
