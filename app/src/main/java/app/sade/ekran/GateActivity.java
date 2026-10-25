package app.sade.ekran;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

/** Dikkat dagitan bir uygulamayi acmadan once bekleme ekrani. */
public class GateActivity extends Activity {

    private final Handler h = new Handler(Looper.getMainLooper());
    private int left;
    private TextView count;
    private TextView go;
    private String pkg;

    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);
        pkg = getIntent().getStringExtra("pkg");
        String name = getIntent().getStringExtra("name");
        Prefs.bump(this, "tries");

        LinearLayout col = Ui.column(this);
        col.setGravity(Gravity.CENTER_VERTICAL);

        col.addView(Ui.title(this, "Bir nefes al."));
        String f = Prefs.focus(this);
        TextView q = Ui.text(this, f.isEmpty()
                ? (name + " uygulamasını gerçekten şimdi mi açmak istiyorsun?")
                : ("Hedefin: " + f + ". " + name + " buna yardım ediyor mu?"), 17, Ui.DIM);
        col.addView(q, Ui.gapTop(this, 16));

        count = Ui.text(this, "", 96, Ui.FG);
        count.setTypeface(Typeface.create(Typeface.SERIF, Typeface.NORMAL));
        col.addView(count, Ui.gapTop(this, 16));

        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        TextView quit = Ui.button(this, "Vazgeç", true);
        quit.setOnClickListener(v -> giveUp());
        row.addView(quit);
        go = Ui.button(this, name + " aç", false);
        go.setVisibility(View.INVISIBLE);
        go.setOnClickListener(v -> open());
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        lp.leftMargin = Ui.dp(this, 12);
        row.addView(go, lp);
        col.addView(row, Ui.gapTop(this, 24));

        setContentView(col);
        left = Prefs.waitSec(this);
        tick();
    }

    private void tick() {
        if (left <= 0) {
            count.setText("");
            go.setVisibility(View.VISIBLE);
            return;
        }
        count.setText(String.valueOf(left));
        left--;
        h.postDelayed(this::tick, 1000);
    }

    private void giveUp() {
        h.removeCallbacksAndMessages(null);
        Prefs.bump(this, "quits");
        finish();
    }

    private void open() {
        Prefs.bump(this, "opens");
        Intent i = getPackageManager().getLaunchIntentForPackage(pkg);
        if (i != null) startActivity(i);
        finish();
    }

    @Override
    public void onBackPressed() {
        giveUp();
    }

    @Override
    protected void onDestroy() {
        h.removeCallbacksAndMessages(null);
        super.onDestroy();
    }
}
