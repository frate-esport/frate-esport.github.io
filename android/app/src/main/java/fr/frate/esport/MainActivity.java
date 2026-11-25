package fr.frate.esport;

import android.app.Activity;
import android.app.DownloadManager;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.webkit.URLUtil;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

// Appli Fraté Esport : affiche le site officiel en plein écran.
// Les liens externes (Discord, Twitch, Instagram, boutique Yoko) s'ouvrent dans leur appli ou le navigateur.
public class MainActivity extends Activity {
    private static final String HOME = "https://frate-esport.github.io/?app=1";
    private static final String HOST = "frate-esport.github.io";
    private WebView web;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        web = new WebView(this);
        web.setBackgroundColor(Color.parseColor("#06070c"));
        web.setVisibility(View.INVISIBLE);
        setContentView(web);

        WebSettings s = web.getSettings();
        s.setJavaScriptEnabled(true);
        s.setDomStorageEnabled(true);
        s.setMediaPlaybackRequiresUserGesture(false);
        s.setLoadWithOverviewMode(true);
        s.setUseWideViewPort(true);
        s.setUserAgentString(s.getUserAgentString() + " FrateApp/1.0");

        web.setWebChromeClient(new WebChromeClient());
        web.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest req) {
                Uri u = req.getUrl();
                if (HOST.equals(u.getHost()) && !u.getPath().endsWith(".apk")) return false;
                if ("file".equals(u.getScheme())) return false;
                try { startActivity(new Intent(Intent.ACTION_VIEW, u)); } catch (Exception e) { /* aucune appli pour ce lien */ }
                return true;
            }

            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) { }

            @Override
            public void onPageFinished(WebView view, String url) { view.setVisibility(View.VISIBLE); }

            @Override
            public void onReceivedError(WebView view, WebResourceRequest req, WebResourceError err) {
                if (req.isForMainFrame()) { view.loadUrl("file:///android_asset/offline.html"); }
            }
        });

        // Téléchargement des fonds d'écran (images et vidéos)
        web.setDownloadListener((url, ua, cd, mime, len) -> {
            try {
                String name = URLUtil.guessFileName(url, cd, mime);
                DownloadManager.Request r = new DownloadManager.Request(Uri.parse(url));
                r.setMimeType(mime);
                r.setTitle(name);
                r.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
                r.setDestinationInExternalPublicDir(Environment.DIRECTORY_PICTURES, "Frate/" + name);
                ((DownloadManager) getSystemService(DOWNLOAD_SERVICE)).enqueue(r);
                Toast.makeText(this, "Téléchargement de " + name, Toast.LENGTH_SHORT).show();
            } catch (Exception e) {
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
            }
        });

        Uri data = getIntent() != null ? getIntent().getData() : null;
        if (savedInstanceState != null) web.restoreState(savedInstanceState);
        else web.loadUrl(data != null && HOST.equals(data.getHost()) ? data.toString() : HOME);
    }

    @Override
    protected void onSaveInstanceState(Bundle out) { super.onSaveInstanceState(out); web.saveState(out); }

    @Override
    public void onBackPressed() {
        if (web.canGoBack()) web.goBack(); else super.onBackPressed();
    }
}
