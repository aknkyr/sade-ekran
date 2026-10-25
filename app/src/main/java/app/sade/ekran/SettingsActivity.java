package app.sade.ekran;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.text.InputType;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

/** Ayarlar: listeler, YouTube saatleri, bekleme suresi, izinler. */
public class SettingsActivity extends Activity {

    private EditText yt, wait;

    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);
        ScrollView sv = new ScrollView(this);
        sv.setBackgroundColor(Ui.BG);
        LinearLayout col = Ui.column(this);
        sv.addView(col);

        col.addView(Ui.title(this, "Ayarlar"));

        col.addView(Ui.label(this, "Uygulamalar"), Ui.gapTop(this, 28));
        col.addView(link("Ana ekrandaki uygulamalar", () -> picker("home", "Ana ekranda görünecekler")), Ui.gapTop(this, 6));
        col.addView(link("Açmadan önce bekletilecekler", () -> picker("wait", "Açmadan önce beklet")), Ui.gapTop(this, 2));
        col.addView(link("Tamamen engelli uygulamalar", () -> picker("blocked", "Engelli uygulamalar")), Ui.gapTop(this, 2));

        col.addView(Ui.label(this, "YouTube izinli saatler"), Ui.gapTop(this, 28));
        yt = field(Prefs.ytHours(this), "21:00-22:00, 12:30-13:00", InputType.TYPE_CLASS_TEXT);
        col.addView(yt, Ui.gapTop(this, 2));
        col.addView(Ui.text(this, "Virgülle birden fazla aralık yazabilirsin. Boş bırakırsan YouTube hep kapalı olur.", 13, Ui.DIM), Ui.gapTop(this, 4));

        col.addView(Ui.label(this, "Bekleme süresi (saniye)"), Ui.gapTop(this, 24));
        wait = field(String.valueOf(Prefs.waitSec(this)), "10", InputType.TYPE_CLASS_NUMBER);
        col.addView(wait, Ui.gapTop(this, 2));

        TextView save = Ui.button(this, "Kaydet", true);
        save.setOnClickListener(v -> save());
        col.addView(save, Ui.wrapGap(this, 20));

        col.addView(Ui.label(this, "Kurulum"), Ui.gapTop(this, 36));
        col.addView(link("Engellemeyi aç (Erişilebilirlik)", () ->
                startActivity(new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))), Ui.gapTop(this, 6));
        col.addView(link("Varsayılan ana ekran yap", () ->
                startActivity(new Intent(Settings.ACTION_HOME_SETTINGS))), Ui.gapTop(this, 2));

        TextView status = Ui.text(this, Prefs.blockerEnabled(this)
                ? "Engelleme açık." : "Engelleme kapalı. Yukarıdan açman gerekiyor.", 13,
                Prefs.blockerEnabled(this) ? Ui.DIM : Ui.STOP);
        col.addView(status, Ui.gapTop(this, 8));

        setContentView(sv);
    }

    private TextView link(String s, Runnable r) {
        TextView t = Ui.text(this, s, 20, Ui.FG);
        int v = Ui.dp(this, 8);
        t.setPadding(0, v, 0, v);
        t.setOnClickListener(x -> r.run());
        return t;
    }

    private EditText field(String value, String hint, int type) {
        EditText e = new EditText(this);
        e.setText(value);
        e.setHint(hint);
        e.setHintTextColor(Ui.LINE);
        e.setTextColor(Ui.FG);
        e.setTextSize(17);
        e.setSingleLine(true);
        e.setInputType(type);
        e.setBackgroundTintList(android.content.res.ColorStateList.valueOf(Ui.LINE));
        return e;
    }

    private void picker(String key, String title) {
        startActivity(new Intent(this, AppPickerActivity.class).putExtra("key", key).putExtra("title", title));
    }

    private void save() {
        Prefs.setYtHours(this, yt.getText().toString().trim());
        int w;
        try { w = Integer.parseInt(wait.getText().toString().trim()); } catch (Exception e) { w = 10; }
        Prefs.setWaitSec(this, Math.max(3, Math.min(60, w)));
        Toast.makeText(this, "Kaydedildi", Toast.LENGTH_SHORT).show();
        finish();
    }
}
