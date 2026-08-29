package com.naina.ai;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

public class ServerService extends Service {
    Process proc;

    @Override
    public IBinder onBind(Intent i) { return null; }

    @Override
    public void onCreate() {
        super.onCreate();
        NotificationManager nm = getSystemService(NotificationManager.class);
        nm.createNotificationChannel(new NotificationChannel(
                "naina", "Naina", NotificationManager.IMPORTANCE_LOW));
        Notification n = new Notification.Builder(this, "naina")
                .setContentTitle("Naina AI chal raha hai")
                .setSmallIcon(android.R.drawable.stat_notify_chat)

                .build();
        startForeground(1, n);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int id) {
        if (proc == null) {
            try {
                String bin = getApplicationInfo().nativeLibraryDir + "/libllamaserver.so";
                String model = getFilesDir().getAbsolutePath() + "/Naina.gguf";
                



                ProcessBuilder pb = new ProcessBuilder(
                        bin, "-m", model,
                        "--host", "127.0.0.1",
                        "--port", "8888",
                        "-c", "1024",
                        "-t", "6",
                        "--threads-batch", "6");

                pb.redirectErrorStream(true);
                proc = pb.start();
            } catch (Exception ignored) { }
        }
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        if (proc != null) proc.destroy();
        super.onDestroy();
    }
}
