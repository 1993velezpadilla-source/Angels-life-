package pr.angelslife.forms;
import android.app.Activity;
import android.os.Bundle;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.webkit.WebSettings;
import android.webkit.WebChromeClient;
import android.print.PrintManager;
import android.content.Context;
public class MainActivity extends Activity {
 private WebView web;
 @Override public void onCreate(Bundle state) {
  super.onCreate(state);
  web = new WebView(this);
  web.setWebViewClient(new WebViewClient());
  web.setWebChromeClient(new WebChromeClient());
  WebSettings settings=web.getSettings();
  settings.setJavaScriptEnabled(true);
  settings.setAllowFileAccess(true);
  settings.setAllowContentAccess(false);
  settings.setDomStorageEnabled(false);
  web.addJavascriptInterface(new Object() {}, "UnusedBridge"); // No callable methods exposed
  setContentView(web);
  web.loadUrl("file:///android_asset/index.html");
 }
 @Override public void onBackPressed() {
  if(web.canGoBack()) web.goBack(); else super.onBackPressed();
 }
 @Override protected void onDestroy(){web.destroy();super.onDestroy();}
}
