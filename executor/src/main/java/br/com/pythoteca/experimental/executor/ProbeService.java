package br.com.pythoteca.experimental.executor;

import android.app.Service;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.*;
import br.com.pythoteca.protocol.Protocol;

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

    private final Messenger endpoint = new Messenger(new Handler(Looper.getMainLooper(), msg -> {
        if (!trusted(msg.sendingUid) || msg.what != Protocol.PING
                || msg.arg1 != Protocol.VERSION || msg.arg2 <= 0 || msg.replyTo == null) {
            return true;
        }
        Message response = Message.obtain(null, Protocol.PONG, Protocol.VERSION, msg.arg2);
        try { msg.replyTo.send(response); }
        catch (RemoteException ignored) {}
        return true;
    }));

    @Override public IBinder onBind(Intent intent) { return endpoint.getBinder(); }
}
