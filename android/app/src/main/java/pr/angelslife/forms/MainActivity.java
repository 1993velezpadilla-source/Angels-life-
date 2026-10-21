package pr.angelslife.forms;

import android.app.Activity;
import android.content.Context;
import android.content.res.Configuration;
import android.os.Bundle;
import android.print.PrintAttributes;
import android.print.PrintDocumentAdapter;
import android.print.PrintManager;
import android.view.ViewGroup;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.LinearLayout;

public class MainActivity extends Activity {
    private WebView web;
    private Button printButton;

    @Override
    public void onCreate(Bundle state) {
        super.onCreate(state);

        // Native print action needs no addJavascriptInterface or network access.
        LinearLayout shell = new LinearLayout(this);
        shell.setOrientation(LinearLayout.VERTICAL);

        printButton = new Button(this);
        printButton.setText("PDF / Imprimir");
        printButton.setAllCaps(false);
        printButton.setEnabled(false);
        int toolbarHeight = (int) (52 * getResources().getDisplayMetrics().density);
        shell.addView(printButton, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, toolbarHeight));

        web = new WebView(this);
        shell.addView(web, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, 0, 1f));
        setContentView(shell);

        WebSettings settings = web.getSettings();
        settings.setJavaScriptEnabled(true);  // Offline form tabs and signatures.
        settings.setDomStorageEnabled(false);  // No persistent patient data.
        settings.setBlockNetworkLoads(true);
        settings.setAllowFileAccess(true);     // Local bundled app assets only.
        settings.setAllowFileAccessFromFileURLs(false);
        settings.setAllowUniversalAccessFromFileURLs(false);
        settings.setAllowContentAccess(false);
        settings.setJavaScriptCanOpenWindowsAutomatically(false);

        web.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                return !request.getUrl().toString().startsWith("file:///android_asset/");
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                if (url.startsWith("file:///android_asset/")) {
                    view.evaluateJavascript(
                            "document.body.classList.add('android-app')", null);
                    printButton.setEnabled(true);
                }
            }
        });

        printButton.setOnClickListener(v -> printCurrentSheet());
        web.loadUrl("file:///android_asset/index.html");
    }

    private void printCurrentSheet() {
        if (web == null || !printButton.isEnabled()) return;
        PrintManager manager = (PrintManager) getSystemService(Context.PRINT_SERVICE);
        if (manager == null) return;
        String name = "ANGEL'S LIFE - Formulario";
        PrintDocumentAdapter adapter = web.createPrintDocumentAdapter(name);
        PrintAttributes options = new PrintAttributes.Builder()
                .setMediaSize(PrintAttributes.MediaSize.NA_LETTER)
                .setColorMode(PrintAttributes.COLOR_MODE_COLOR)
                .build();
        manager.print(name, adapter, options);
    }

    // Keep unsaved form inputs and canvas signatures in memory when rotating.
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    @Override
    protected void onDestroy() {
        if (web != null) {
            web.destroy();
            web = null;
        }
        super.onDestroy();
    }
}
