package app.sade.ekran;

import android.content.ComponentName;
import android.content.Context;
import android.content.SharedPreferences;
import android.provider.Settings;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

/** Tum ayarlar ve gunluk sayac burada tutulur. */
public final class Prefs {
    private Prefs() {}

    public static final String YOUTUBE = "com.google.android.youtube";

    static final Set<String> DEFAULT_BLOCKED = new HashSet<>(Arrays.asList(
            "com.instagram.android",
            "com.instagram.lite",
            "com.zhiliaoapp.musically",
            "com.ss.android.ugc.trill",
            "com.twitter.android"
    ));

    static final String[] BLOCKED_DOMAINS = {
            "instagram.com", "tiktok.com", "twitter.com", "x.com"
    };
    static final String[] YOUTUBE_DOMAINS = { "youtube.com", "youtu.be" };

    static SharedPreferences p(Context c) {
        return c.getSharedPreferences("sade", Context.MODE_PRIVATE);
    }

    // ---------- uygulama listeleri ----------
    public static Set<String> getSet(Context c, String key) {
        Set<String> def = key.equals("blocked") ? DEFAULT_BLOCKED : new HashSet<>();
        return new HashSet<>(p(c).getStringSet(key, def));
    }

    public static void putSet(Context c, String key, Set<String> v) {
        p(c).edit().putStringSet(key, new HashSet<>(v)).apply();
    }

    // ---------- basit degerler ----------
    public static String ytHours(Context c) { return p(c).getString("yt", "21:00-22:00"); }
    public static void setYtHours(Context c, String v) { p(c).edit().putString("yt", v).apply(); }

    public static int waitSec(Context c) { return p(c).getInt("wait", 10); }
    public static void setWaitSec(Context c, int v) { p(c).edit().putInt("wait", v).apply(); }

    public static String focus(Context c) { return p(c).getString("focus", ""); }
    public static void setFocus(Context c, String v) { p(c).edit().putString("focus", v).apply(); }

    // ---------- YouTube saatleri ----------
    static List<int[]> windows(Context c) {
        List<int[]> out = new ArrayList<>();
        for (String part : ytHours(c).split(",")) {
            String s = part.trim();
            String[] ab = s.split("-");
            if (ab.length != 2) continue;
            int a = toMin(ab[0]), b = toMin(ab[1]);
            if (a >= 0 && b >= 0) out.add(new int[]{a, b});
        }
        return out;
    }

    static int toMin(String hm) {
        try {
            String[] x = hm.trim().split(":");
            int h = Integer.parseInt(x[0].trim());
            int m = x.length > 1 ? Integer.parseInt(x[1].trim()) : 0;
            if (h < 0 || h > 24 || m < 0 || m > 59) return -1;
            return h * 60 + m;
        } catch (Exception e) {
            return -1;
        }
    }

    public static boolean ytAllowed(Context c) {
        Calendar cal = Calendar.getInstance();
        int t = cal.get(Calendar.HOUR_OF_DAY) * 60 + cal.get(Calendar.MINUTE);
        for (int[] w : windows(c)) {
            if (w[0] <= w[1] ? (t >= w[0] && t < w[1]) : (t >= w[0] || t < w[1])) return true;
        }
        return false;
    }

    public static String nextYtStart(Context c) {
        List<int[]> ws = windows(c);
        if (ws.isEmpty()) return null;
        Calendar cal = Calendar.getInstance();
        int t = cal.get(Calendar.HOUR_OF_DAY) * 60 + cal.get(Calendar.MINUTE);
        int best = -1, first = 24 * 60 + 1;
        for (int[] w : ws) {
            if (w[0] > t && (best < 0 || w[0] < best)) best = w[0];
            if (w[0] < first) first = w[0];
        }
        int s = best >= 0 ? best : first;
        return String.format(Locale.ROOT, "%02d:%02d", (s / 60) % 24, s % 60);
    }

    // ---------- engelleme kurallari ----------
    /** Uygulama engelliyse sebebini, degilse null dondurur. */
    public static String appBlockReason(Context c, String pkg) {
        if (getSet(c, "blocked").contains(pkg)) {
            return "Bu uygulama engelli.";
        }
        if (YOUTUBE.equals(pkg) && !ytAllowed(c)) {
            return ytClosedText(c);
        }
        return null;
    }

    static String ytClosedText(Context c) {
        String n = nextYtStart(c);
        return n == null
                ? "YouTube için izinli saat yok. Ayarlar'dan ekleyebilirsin."
                : "YouTube şimdi kapalı. İzinli saatler: " + ytHours(c) + ". Bir sonraki açılış " + n + ".";
    }

    /** Tarayici adres cubugundaki yazi engelli bir siteyse sebebini dondurur. */
    public static String urlBlockReason(Context c, String raw) {
        String host = hostOf(raw);
        if (host == null) return null;
        for (String d : BLOCKED_DOMAINS) {
            if (matches(host, d)) return d + " engelli.";
        }
        if (!ytAllowed(c)) {
            for (String d : YOUTUBE_DOMAINS) {
                if (matches(host, d)) return ytClosedText(c);
            }
        }
        return null;
    }

    static boolean matches(String host, String domain) {
        return host.equals(domain) || host.endsWith("." + domain);
    }

    static String hostOf(String raw) {
        String s = raw.trim().toLowerCase(Locale.ROOT);
        if (s.isEmpty() || s.contains(" ")) return null;
        int i = s.indexOf("://");
        if (i >= 0) s = s.substring(i + 3);
        int cut = s.length();
        for (char ch : new char[]{'/', '?', '#', ':'}) {
            int k = s.indexOf(ch);
            if (k >= 0 && k < cut) cut = k;
        }
        s = s.substring(0, cut);
        if (!s.contains(".")) return null;
        return s;
    }

    // ---------- gunluk sayac ----------
    static String today() {
        return new SimpleDateFormat("yyyy-MM-dd", Locale.ROOT).format(new Date());
    }

    static void rollDay(Context c) {
        SharedPreferences sp = p(c);
        if (!today().equals(sp.getString("day", ""))) {
            sp.edit().putString("day", today()).putInt("tries", 0).putInt("opens", 0).putInt("quits", 0).apply();
        }
    }

    public static void bump(Context c, String key) {
        rollDay(c);
        SharedPreferences sp = p(c);
        sp.edit().putInt(key, sp.getInt(key, 0) + 1).apply();
    }

    public static int count(Context c, String key) {
        rollDay(c);
        return p(c).getInt(key, 0);
    }

    // ---------- erisilebilirlik servisi acik mi ----------
    public static boolean blockerEnabled(Context c) {
        String on = Settings.Secure.getString(c.getContentResolver(), Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES);
        if (on == null) return false;
        String me = new ComponentName(c, BlockerService.class).flattenToString();
        String meShort = new ComponentName(c, BlockerService.class).flattenToShortString();
        for (String s : on.split(":")) {
            if (s.equalsIgnoreCase(me) || s.equalsIgnoreCase(meShort)) return true;
        }
        return false;
    }
}
