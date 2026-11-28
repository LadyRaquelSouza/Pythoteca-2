package br.com.pythoteca.experimental.installer;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageInstaller;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import java.io.*;
import java.security.MessageDigest;
import java.util.*;

public final class MainActivity extends Activity {
    private static final String PREFIX = "br.com.pythoteca.experimental.";
    private TextView status;
    private Button install;
    private boolean running;
    @Override public void onCreate(Bundle state) {
        super.onCreate(state);
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(32, 48, 32, 32);
        status = new TextView(this);
        status.setText("Pythoteca Suite experimental. Instala dois aplicativos separados. Não substitui a Pythoteca original.");
        install = new Button(this);
        install.setText("Verificar e instalar");
        install.setOnClickListener(v -> begin());
        layout.addView(status);
        layout.addView(install);
        setContentView(layout);
    }
    @Override public void onResume() {
        super.onResume();
        String result = getSharedPreferences("suite", MODE_PRIVATE).getString("result", "");
        if (!result.isEmpty()) status.setText(result);
    }
    private void begin() {
        if (running) return;
        if (!getPackageManager().canRequestPackageInstalls()) {
            startActivity(new Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES, Uri.parse("package:" + getPackageName())));
            return;
        }
        running = true;
        install.setEnabled(false);
        status.setText("Verificando os componentes...");
        new Thread(() -> {
            try {
                installSuite();
                runOnUiThread(() -> status.setText("Aguardando confirmação do Android."));
            } catch (Exception e) {
                runOnUiThread(() -> status.setText("Instalação interrompida: " + e.getMessage()));
            } finally {
                runOnUiThread(() -> { running = false; install.setEnabled(true); });
            }
        }).start();
    }
    private static String hex(byte[] bytes) {
        StringBuilder s = new StringBuilder();
        for (byte b : bytes) s.append(String.format(Locale.ROOT, "%02x", b & 255));
        return s.toString();
    }
    private static String signer(PackageInfo info) throws Exception {
        if (info == null || info.signingInfo == null) throw new SecurityException("Assinatura ausente");
        Signature[] signatures = info.signingInfo.getApkContentsSigners();
        if (signatures.length != 1) throw new SecurityException("Assinatura múltipla inesperada");
        return hex(MessageDigest.getInstance("SHA-256").digest(signatures[0].toByteArray()));
    }
    private File checkedApk(String module, Properties props, String trusted, File dir) throws Exception {
        File file = new File(dir, module + ".apk");
        MessageDigest hash = MessageDigest.getInstance("SHA-256");
        try (InputStream in = getAssets().open(module + ".apk"); OutputStream out = new FileOutputStream(file)) {
            byte[] buf = new byte[65536];
            int n;
            while ((n = in.read(buf)) != -1) { hash.update(buf, 0, n); out.write(buf, 0, n); }
        }
        if (!hex(hash.digest()).equals(props.getProperty(module + ".sha256"))) throw new SecurityException("Hash divergente: " + module);
        if (!trusted.equals(props.getProperty(module + ".cert"))) throw new SecurityException("Certificado esperado divergente");
        PackageManager pm = getPackageManager();
        PackageInfo info = pm.getPackageArchiveInfo(file.getAbsolutePath(), PackageManager.GET_SIGNING_CERTIFICATES);
        if (info == null || !(PREFIX + module).equals(info.packageName) || info.getLongVersionCode() != 1 ||
            !"1".equals(props.getProperty(module + ".version")) || !trusted.equals(signer(info)))
            throw new SecurityException("Identidade divergente: " + module);
        Set<String> supported = new HashSet<>(Arrays.asList(Build.SUPPORTED_ABIS));
        Set<String> actual = new HashSet<>();
        try (java.util.zip.ZipFile zip = new java.util.zip.ZipFile(file)) {
            zip.stream().forEach(entry -> {
                String[] parts = entry.getName().split("/");
                if (parts.length == 3 && parts[0].equals("lib") && parts[2].endsWith(".so")) actual.add(parts[1]);
            });
        }
        if (!actual.equals(new HashSet<>(Arrays.asList(props.getProperty(module + ".abis", "").split(",")))) ||
            Collections.disjoint(supported, actual)) throw new SecurityException("ABI incompatível");
        try {
            PackageInfo old = pm.getPackageInfo(info.packageName, PackageManager.GET_SIGNING_CERTIFICATES);
            if (!trusted.equals(signer(old)) || old.getLongVersionCode() > info.getLongVersionCode())
                throw new SecurityException("Instalação existente incompatível: " + module);
        } catch (PackageManager.NameNotFoundException ignored) {}
        return file;
    }
    private void installSuite() throws Exception {
        PackageManager pm = getPackageManager();
        String trusted = signer(pm.getPackageInfo(getPackageName(), PackageManager.GET_SIGNING_CERTIFICATES));
        Properties props = new Properties();
        try (InputStream in = getAssets().open("suite.properties")) { props.load(in); }
        File dir = new File(getCacheDir(), UUID.randomUUID().toString());
        if (!dir.mkdir()) throw new IOException("Cache indisponível");
        PackageInstaller pi = pm.getPackageInstaller();
        List<Integer> sessions = new ArrayList<>();
        boolean committed = false;
        try {
            File reader = checkedApk("reader", props, trusted, dir);
            File executor = checkedApk("executor", props, trusted, dir);
            PackageInstaller.SessionParams parentParams = new PackageInstaller.SessionParams(PackageInstaller.SessionParams.MODE_FULL_INSTALL);
            parentParams.setMultiPackage();
            int parentId = pi.createSession(parentParams);
            sessions.add(parentId);
            try (PackageInstaller.Session parent = pi.openSession(parentId)) {
                for (File apk : new File[]{reader, executor}) {
                    PackageInstaller.SessionParams params = new PackageInstaller.SessionParams(PackageInstaller.SessionParams.MODE_FULL_INSTALL);
                    params.setAppPackageName(PREFIX + apk.getName().replace(".apk", ""));
                    params.setSize(apk.length());
                    if (Build.VERSION.SDK_INT >= 31) params.setRequireUserAction(PackageInstaller.SessionParams.USER_ACTION_REQUIRED);
                    int childId = pi.createSession(params);
                    sessions.add(childId);
                    try (PackageInstaller.Session child = pi.openSession(childId);
                         InputStream in = new FileInputStream(apk);
                         OutputStream out = child.openWrite("base.apk", 0, apk.length())) {
                        byte[] buf = new byte[65536]; int n;
                        while ((n = in.read(buf)) != -1) out.write(buf, 0, n);
                        child.fsync(out);
                    }
                    parent.addChildSessionId(childId);
                }
                Intent callback = new Intent(this, InstallResult.class).setAction(PREFIX + "INSTALL_RESULT");
                PendingIntent pending = PendingIntent.getBroadcast(this, parentId, callback, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_MUTABLE);
                getSharedPreferences("suite", MODE_PRIVATE).edit().putString("result", "Aguardando confirmação do Android.").apply();
                parent.commit(pending.getIntentSender());
                committed = true;
            }
        } finally {
            if (!committed) for (int i = sessions.size() - 1; i >= 0; i--) {
                try { pi.abandonSession(sessions.get(i)); } catch (RuntimeException ignored) {}
            }
            new File(dir, "reader.apk").delete();
            new File(dir, "executor.apk").delete();
            dir.delete();
        }
    }
}
