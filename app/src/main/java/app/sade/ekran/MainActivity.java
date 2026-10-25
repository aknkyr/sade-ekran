package app.sade.ekran;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import java.text.Collator;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.Set;

/** Minimalist ana ekran: saat, tek hedef, yazidan olusan uygulama listesi, gunluk sayac. */
public class MainActivity extends Activity {

    private static final String[] DAYS = {"Pazar", "Pazartesi", "Salı", "Çarşamba", "Perşembe", "Cuma", "Cumartesi"};
    private static final String[] MONTHS = {"Ocak", "Şubat", "Mart", "Nisan", "Mayıs", "Haziran",
            "Temmuz", "Ağustos", "Eylül", "Ekim", "Kasım", "Aralık"};

    private final Handler h = new Handler(Looper.getMainLooper());
    private TextView clock, date, stats, warn;
    private LinearLayout list;

    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);

        LinearLayout col = Ui.column(this);

        clock = Ui.text(this, "", 96, Ui.FG);
        clock.setTypeface(Typeface.create(Typeface.SERIF, Typeface.NORMAL));
        clock.setIncludeFontPadding(false);
        col.addView(clock);

        date = Ui.text(this, "", 15, Ui.DIM);
        col.addView(date, Ui.gapTop(this, 4));

        warn = Ui.text(this, "Engelleme kapalı. Açmak için dokun.", 14, Ui.STOP);
        warn.setOnClickListener(v -> startActivity(new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)));
        col.addView(warn, Ui.gapTop(this, 16));

        col.addView(Ui.label(this, "Bugünün tek hedefi"), Ui.gapTop(this, 28));
        EditText focus = new EditText(this);
        focus.setText(Prefs.focus(this));
        focus.setHint("Örn. MSE 307 quiz hazırlığı");
        focus.setHintTextColor(Ui.LINE);
        focus.setTextColor(Ui.FG);
        focus.setTextSize(19);
        focus.setSingleLine(true);
        focus.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_SENTENCES);
        focus.setImeOptions(EditorInfo.IME_ACTION_DONE);
        focus.setBackgroundTintList(android.content.res.ColorStateList.valueOf(Ui.LINE));
        focus.addTextChangedListener(new TextWatcher() {
            public void beforeTextChanged(CharSequence s, int a, int b2, int c) {}
            public void onTextChanged(CharSequence s, int a, int b2, int c) {}
            public void afterTextChanged(Editable s) { Prefs.setFocus(MainActivity.this, s.toString()); }
        });
        col.addView(focus, Ui.gapTop(this, 2));

        ScrollView sv = new ScrollView(this);
        sv.setVerticalScrollBarEnabled(false);
        list = new LinearLayout(this);
        list.setOrientation(LinearLayout.VERTICAL);
        sv.addView(list);
        LinearLayout.LayoutParams svlp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 0, 1f);
        svlp.topMargin = Ui.dp(this, 24);
        col.addView(sv, svlp);

        LinearLayout foot = new LinearLayout(this);
        foot.setOrientation(LinearLayout.HORIZONTAL);
        foot.setGravity(Gravity.CENTER_VERTICAL);
        stats = Ui.text(this, "", 13, Ui.DIM);
        foot.addView(stats, new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f));
        TextView settings = Ui.text(this, "Ayarlar", 14, Ui.DIM);
        settings.setPadding(Ui.dp(this, 12), Ui.dp(this, 10), 0, Ui.dp(this, 10));
        settings.setOnClickListener(v -> startActivity(new Intent(this, SettingsActivity.class)));
        foot.addView(settings);
        col.addView(foot, Ui.gapTop(this, 12));

        setContentView(col);
    }

    @Override
    protected void onResume() {
        super.onResume();
        refresh();
        h.removeCallbacksAndMessages(null);
        h.post(new Runnable() {
            @Override public void run() {
                tickClock();
                h.postDelayed(this, 15000);
            }
        });
    }

    @Override
    protected void onPause() {
        h.removeCallbacksAndMessages(null);
        super.onPause();
    }

    private void tickClock() {
        Calendar c = Calendar.getInstance();
        clock.setText(String.format(Locale.ROOT, "%02d:%02d", c.get(Calendar.HOUR_OF_DAY), c.get(Calendar.MINUTE)));
        date.setText(c.get(Calendar.DAY_OF_MONTH) + " " + MONTHS[c.get(Calendar.MONTH)] + " " + DAYS[c.get(Calendar.DAY_OF_WEEK) - 1]);
        stats.setText("Bugün: " + Prefs.count(this, "tries") + " deneme · "
                + Prefs.count(this, "opens") + " açılış · " + Prefs.count(this, "quits") + " vazgeçiş");
    }

    private void refresh() {
        warn.setVisibility(Prefs.blockerEnabled(this) ? View.GONE : View.VISIBLE);
        list.removeAllViews();

        PackageManager pm = getPackageManager();
        Set<String> home = Prefs.getSet(this, "home");
        Set<String> wait = Prefs.getSet(this, "wait");
        Set<String> blocked = Prefs.getSet(this, "blocked");

        List<String[]> apps = new ArrayList<>();
        for (String pkg : home) {
            try {
                ApplicationInfo ai = pm.getApplicationInfo(pkg, 0);
                apps.add(new String[]{pkg, pm.getApplicationLabel(ai).toString()});
            } catch (PackageManager.NameNotFoundException ignored) {}
        }
        Collator col = Collator.getInstance(new Locale("tr"));
        apps.sort((x, y) -> col.compare(x[1], y[1]));

        if (apps.isEmpty()) {
            TextView hint = Ui.text(this, "Listen boş. Ayarlar'dan ana ekrana koymak istediğin birkaç uygulamayı seç.", 16, Ui.DIM);
            list.addView(hint);
        }

        for (String[] app : apps) {
            String pkg = app[0], name = app[1];
            LinearLayout row = new LinearLayout(this);
            row.setOrientation(LinearLayout.HORIZONTAL);
            row.setGravity(Gravity.CENTER_VERTICAL);
            int v = Ui.dp(this, 9);
            row.setPadding(0, v, 0, v);

            TextView n = Ui.text(this, name, 26, Ui.FG);
            TextView tag = Ui.text(this, "", 12, Ui.DIM);

            if (blocked.contains(pkg)) {
                n.setTextColor(Ui.DIM);
                n.setPaintFlags(n.getPaintFlags() | android.graphics.Paint.STRIKE_THRU_TEXT_FLAG);
                tag.setText("engelli");
                tag.setTextColor(Ui.STOP);
            } else if (Prefs.YOUTUBE.equals(pkg)) {
                if (Prefs.ytAllowed(this)) {
                    tag.setText("açık");
                } else {
                    String nx = Prefs.nextYtStart(this);
                    tag.setText(nx == null ? "kapalı" : nx + "'de açılır");
                    tag.setTextColor(Ui.WARN);
                }
            } else if (wait.contains(pkg)) {
                tag.setText("bekle");
            }

            row.addView(n, new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f));
            row.addView(tag);
            row.setOnClickListener(x -> launch(pkg, name));
            list.addView(row);
        }
    }

    private void launch(String pkg, String name) {
        String reason = Prefs.appBlockReason(this, pkg);
        if (reason != null) {
            Prefs.bump(this, "tries");
            startActivity(new Intent(this, BlockActivity.class).putExtra("reason", reason));
            return;
        }
        if (Prefs.getSet(this, "wait").contains(pkg) || Prefs.YOUTUBE.equals(pkg)) {
            startActivity(new Intent(this, GateActivity.class).putExtra("pkg", pkg).putExtra("name", name));
            return;
        }
        Intent i = getPackageManager().getLaunchIntentForPackage(pkg);
        if (i != null) startActivity(i);
    }

    @Override
    public void onBackPressed() {
        // Ana ekranda geri tusu bir sey yapmaz.
    }
}
