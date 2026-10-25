package app.sade.ekran;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.ScrollView;

import java.text.Collator;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

/** Telefondaki uygulamalardan secim listesi. */
public class AppPickerActivity extends Activity {

    private String key;
    private Set<String> chosen;

    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);
        key = getIntent().getStringExtra("key");
        chosen = Prefs.getSet(this, key);

        ScrollView sv = new ScrollView(this);
        sv.setBackgroundColor(Ui.BG);
        LinearLayout col = Ui.column(this);
        sv.addView(col);
        String title = getIntent().getStringExtra("title");
        col.addView(Ui.title(this, title == null ? "Uygulamalar" : title));

        PackageManager pm = getPackageManager();
        Intent main = new Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER);
        List<ResolveInfo> infos = pm.queryIntentActivities(main, 0);
        List<String[]> apps = new ArrayList<>();
        Set<String> seen = new HashSet<>();
        for (ResolveInfo ri : infos) {
            String pkg = ri.activityInfo.packageName;
            if (pkg.equals(getPackageName()) || !seen.add(pkg)) continue;
            apps.add(new String[]{pkg, ri.loadLabel(pm).toString()});
        }
        Collator c = Collator.getInstance(new Locale("tr"));
        apps.sort((x, y) -> c.compare(x[1], y[1]));

        for (String[] app : apps) {
            CheckBox cb = new CheckBox(this);
            cb.setText(app[1]);
            cb.setTextColor(Ui.FG);
            cb.setTextSize(18);
            cb.setButtonTintList(ColorStateList.valueOf(Ui.FG));
            int v = Ui.dp(this, 6);
            cb.setPadding(Ui.dp(this, 8), v, 0, v);
            cb.setChecked(chosen.contains(app[0]));
            String pkg = app[0];
            cb.setOnCheckedChangeListener((btn, on) -> {
                if (on) chosen.add(pkg); else chosen.remove(pkg);
                Prefs.putSet(this, key, chosen);
            });
            col.addView(cb, Ui.gapTop(this, 4));
        }
        setContentView(sv);
    }
}
