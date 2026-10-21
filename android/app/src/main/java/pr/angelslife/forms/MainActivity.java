package pr.angelslife.forms;

import android.app.Activity;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.Typeface;
import android.view.Gravity;
import android.widget.TextView;
import android.content.Context;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Build;
import android.graphics.Insets;
import android.view.WindowInsets;
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
        // On Android 15+, edge-to-edge otherwise places the Print toolbar
        // beneath status icons and clips the last field behind navigation.
        shell.setOnApplyWindowInsetsListener((view, windowInsets) -> {
            int top, bottom;
            if (Build.VERSION.SDK_INT >= 30) {
                Insets bars = windowInsets.getInsets(WindowInsets.Type.systemBars());
                top = bars.top;
                bottom = bars.bottom;
            } else {
                top = windowInsets.getSystemWindowInsetTop();
                bottom = windowInsets.getSystemWindowInsetBottom();
            }
            view.setPadding(0, top, 0, bottom);
            return windowInsets;
        });

        // Native V5 identity is visible even if WebView JavaScript fails to run.
        LinearLayout nativeBar = new LinearLayout(this);
        nativeBar.setGravity(Gravity.CENTER_VERTICAL);
        nativeBar.setOrientation(LinearLayout.HORIZONTAL);
        nativeBar.setPadding(dp(12), dp(4), dp(8), dp(4));
        nativeBar.setBackgroundColor(Color.rgb(7, 45, 64));

        TextView versionBadge = new TextView(this);
        versionBadge.setText("ANGEL LIFE · V5 TEST");
        versionBadge.setTextColor(Color.WHITE);
        versionBadge.setTextSize(14);
        versionBadge.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        nativeBar.addView(versionBadge, new LinearLayout.LayoutParams(
                0, ViewGroup.LayoutParams.MATCH_PARENT, 1f));
        versionBadge.setGravity(Gravity.CENTER_VERTICAL);

        printButton = new Button(this);
        printButton.setText("Guardar PDF");
        printButton.setAllCaps(false);
        printButton.setTextColor(Color.WHITE);
        printButton.setBackgroundTintList(ColorStateList.valueOf(Color.rgb(0, 129, 125)));
        printButton.setEnabled(false);
        nativeBar.addView(printButton, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.MATCH_PARENT));
        int toolbarHeight = dp(62);
        shell.addView(nativeBar, new LinearLayout.LayoutParams(
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

    private int dp(int value) {
        return (int) (value * getResources().getDisplayMetrics().density + 0.5f);
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
