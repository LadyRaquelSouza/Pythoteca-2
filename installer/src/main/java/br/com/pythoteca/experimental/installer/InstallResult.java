package br.com.pythoteca.experimental.installer;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInstaller;
import android.os.Build;

public final class InstallResult extends BroadcastReceiver {
    @Override public void onReceive(Context context, Intent intent) {
        if (!"br.com.pythoteca.experimental.INSTALL_RESULT".equals(intent.getAction())) return;
        int status = intent.getIntExtra(PackageInstaller.EXTRA_STATUS, PackageInstaller.STATUS_FAILURE);
        String result;
        if (status == PackageInstaller.STATUS_PENDING_USER_ACTION) {
            Intent confirm = Build.VERSION.SDK_INT >= 33
                ? intent.getParcelableExtra(Intent.EXTRA_INTENT, Intent.class)
                : intent.getParcelableExtra(Intent.EXTRA_INTENT);
            if (confirm != null) {
                try {
                    context.startActivity(confirm.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
                    result = "Confirme a instalação na interface do Android.";
                } catch (RuntimeException e) {
                    result = "Não foi possível abrir a confirmação. Verifique a sessão de instalação.";
                }
            } else result = "Confirmação ausente.";
        } else if (status == PackageInstaller.STATUS_SUCCESS) {
            result = "Componentes instalados. Abra Pythoteca Experimento.";
        } else {
            result = "Instalação não concluída (status " + status + ").";
        }
        context.getSharedPreferences("suite", Context.MODE_PRIVATE).edit().putString("result", result).apply();
    }
}
