package app.sade.ekran;

import android.accessibilityservice.AccessibilityService;
import android.content.Intent;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Acik olan uygulamayi ve tarayicinin adres cubugunu izler.
 * Engelli bir sey acilirsa ana ekrana doner ve engel ekranini gosterir.
 */
public class BlockerService extends AccessibilityService {

    /** Tarayici paket adi -> adres cubugu view id. */
    private static final Map<String, String[]> BROWSERS = new HashMap<>();
    static {
        BROWSERS.put("com.android.chrome", new String[]{"com.android.chrome:id/url_bar"});
        BROWSERS.put("com.chrome.beta", new String[]{"com.chrome.beta:id/url_bar"});
        BROWSERS.put("com.sec.android.app.sbrowser", new String[]{
                "com.sec.android.app.sbrowser:id/location_bar_edit_text",
                "com.sec.android.app.sbrowser:id/custom_tab_toolbar_url_bar_text"});
        BROWSERS.put("com.brave.browser", new String[]{"com.brave.browser:id/url_bar"});
        BROWSERS.put("com.microsoft.emmx", new String[]{"com.microsoft.emmx:id/url_bar"});
        BROWSERS.put("com.opera.browser", new String[]{"com.opera.browser:id/url_field"});
        BROWSERS.put("org.mozilla.firefox", new String[]{
                "org.mozilla.firefox:id/mozac_browser_toolbar_url_view",
                "org.mozilla.firefox:id/url_bar_title"});
    }

    private long lastBlock = 0;
    private long lastUrlCheck = 0;

    @Override
    public void onAccessibilityEvent(AccessibilityEvent e) {
        if (e == null || e.getPackageName() == null) return;
        String pkg = e.getPackageName().toString();
        if (pkg.equals(getPackageName())) return;

        if (e.getEventType() == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
            String reason = Prefs.appBlockReason(this, pkg);
            if (reason != null) {
                block(reason);
                return;
            }
        }

        String[] ids = BROWSERS.get(pkg);
        if (ids == null) return;
        long now = System.currentTimeMillis();
        if (e.getEventType() == AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED && now - lastUrlCheck < 400) return;
        lastUrlCheck = now;

        AccessibilityNodeInfo root = getRootInActiveWindow();
        if (root == null) return;
        for (String id : ids) {
            List<AccessibilityNodeInfo> nodes = root.findAccessibilityNodeInfosByViewId(id);
            if (nodes == null) continue;
            for (AccessibilityNodeInfo n : nodes) {
                CharSequence t = n.getText();
                if (t == null) continue;
                // Kullanici adres cubugunda yaziyorsa yazmasini bitirene kadar bekle.
                if (n.isFocused() && !t.toString().contains("/")) continue;
                String reason = Prefs.urlBlockReason(this, t.toString());
                if (reason != null) {
                    performGlobalAction(GLOBAL_ACTION_BACK);
                    block(reason);
                    return;
                }
            }
        }
    }

    private void block(String reason) {
        long now = System.currentTimeMillis();
        if (now - lastBlock < 1500) return;
        lastBlock = now;
        Prefs.bump(this, "tries");
        performGlobalAction(GLOBAL_ACTION_HOME);
        Intent i = new Intent(this, BlockActivity.class)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK)
                .putExtra("reason", reason);
        startActivity(i);
    }

    @Override
    public void onInterrupt() {}
}
