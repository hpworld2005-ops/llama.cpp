package com.naina.ai;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

public class MainActivity extends Activity {

    static final int PICK_MODEL = 1;
    LinearLayout menu;
    WebView web;
    File modelFile;

    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);
        modelFile = new File(getFilesDir(), "Naina.gguf");

        menu = new LinearLayout(this);
        menu.setOrientation(LinearLayout.VERTICAL);
        menu.setGravity(Gravity.CENTER);
        menu.setPadding(40, 40, 40, 40);

        Button pick = new Button(this);
        pick.setText("1. Model Import karo (Naina.gguf)");
        pick.setOnClickListener(v -> {
            Intent i = new Intent(Intent.ACTION_OPEN_DOCUMENT);
            i.addCategory(Intent.CATEGORY_OPENABLE);
            i.setType("*/*");
            startActivityForResult(i, PICK_MODEL);
        });

        Button start = new Button(this);
        start.setText("2. Naina START karo");
        start.setOnClickListener(v -> {
            if (!modelFile.exists()) {
                Toast.makeText(this, "Pehle model import karo!", Toast.LENGTH_LONG).show();
                return;
            }
            startForegroundService(new Intent(this, ServerService.class));
            Toast.makeText(this, "Server start ho raha hai... 10-20 sec", Toast.LENGTH_LONG).show();
            showWeb();
        });

        Button stop = new Button(this);
        stop.setText("3. Naina BAND karo");
        stop.setOnClickListener(v -> {
            stopService(new Intent(this, ServerService.class));
            finish();
        });

        menu.addView(pick);
        menu.addView(start);
        menu.addView(stop);

        web = new WebView(this);
        web.getSettings().setJavaScriptEnabled(true);
        web.getSettings().setDomStorageEnabled(true);
        web.setWebViewClient(new WebViewClient());
        web.setVisibility(View.GONE);

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.addView(menu);
        root.addView(web, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, 0, 1f));
        setContentView(root);
    }

    void showWeb() {
        menu.setVisibility(View.GONE);
        web.setVisibility(View.VISIBLE);
        new Thread(() -> {
            for (int i = 0; i < 120; i++) {
                try {
                    java.net.Socket s = new java.net.Socket();
                    s.connect(new java.net.InetSocketAddress("127.0.0.1", 8888), 1000);
                    s.close();
                    runOnUiThread(() -> web.loadUrl("http://127.0.0.1:8888"));
                    return;
                } catch (Exception e) {
                    try { Thread.sleep(1000); } catch (Exception ignore) {}
                }
            }
        }).start();
    }

    @Override
    protected void onActivityResult(int req, int res, Intent data) {
        super.onActivityResult(req, res, data);
        if (req == PICK_MODEL && res == RESULT_OK && data != null) {
            try {
                InputStream in = getContentResolver().openInputStream(data.getData());
                FileOutputStream out = new FileOutputStream(modelFile);
                byte[] buf = new byte[1024 * 1024];
                int n;
                while ((n = in.read(buf)) > 0) out.write(buf, 0, n);
                out.close(); in.close();
                Toast.makeText(this, "Model import ho gaya!", Toast.LENGTH_LONG).show();
            } catch (Exception e) {
                Toast.makeText(this, "Import fail: " + e.getMessage(), Toast.LENGTH_LONG).show();
            }
        }
    }

    @Override
    public void onBackPressed() {
        if (web.getVisibility() == View.VISIBLE && web.canGoBack()) {
            web.goBack();
        } else if (web.getVisibility() == View.VISIBLE) {
            web.setVisibility(View.GONE);
            menu.setVisibility(View.VISIBLE);
        } else {
            super.onBackPressed();
        }
    }
}
