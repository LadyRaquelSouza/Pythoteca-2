package br.com.pythoteca.experimental.executor;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import java.io.ByteArrayInputStream;
import java.util.function.Consumer;

/** Fixed synthetic expression only. Not an arbitrary-code execution API. */
final class JsProbe {
    static void run(Context context, Consumer<Boolean> done) {
        Handler handler = new Handler(Looper.getMainLooper());
        WebView view = null;
        try {
            view = new WebView(context);
            final WebView active = view;
            android.webkit.WebSettings s = active.getSettings();
            s.setJavaScriptEnabled(true);
            s.setAllowFileAccess(false);
            s.setAllowContentAccess(false);
            s.setBlockNetworkLoads(true);
            s.setDomStorageEnabled(false);
            s.setJavaScriptCanOpenWindowsAutomatically(false);
            s.setSupportMultipleWindows(false);
            active.setWebViewClient(new WebViewClient() {
                @Override public boolean shouldOverrideUrlLoading(WebView v, WebResourceRequest r) { return true; }
                @Override public WebResourceResponse shouldInterceptRequest(WebView v, WebResourceRequest r) {
                    return new WebResourceResponse("text/plain", "UTF-8", new ByteArrayInputStream(new byte[0]));
                }
            });
            final boolean[] finished = {false};
            Consumer<Boolean> finish = ok -> {
                if (finished[0]) return;
                finished[0] = true;
                try { active.destroy(); } finally { done.accept(ok); }
            };
            handler.postDelayed(() -> finish.accept(false), 10000);
            // No user-controlled source, URL, HTML, file, JavaScript bridge or network proxy.
            active.evaluateJavascript("(function(){let n=0;for(let i=1;i<=6;i++)n+=i;return n*2;})()", result ->
                finish.accept("42".equals(result)));
        } catch (RuntimeException e) {
            if (view != null) view.destroy();
            done.accept(false);
        }
    }
}
