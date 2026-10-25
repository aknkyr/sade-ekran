package app.sade.ekran;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Gravity;
import android.widget.LinearLayout;
import android.widget.TextView;

/** "Engelli" ekrani. */
public class BlockActivity extends Activity {

    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);
        LinearLayout col = Ui.column(this);
        col.setGravity(Gravity.CENTER_VERTICAL);

        col.addView(Ui.title(this, "Dur."));
        String reason = getIntent().getStringExtra("reason");
        TextView why = Ui.text(this, reason == null ? "Bu engelli." : reason, 17, Ui.DIM);
        col.addView(why, Ui.gapTop(this, 16));

        String f = Prefs.focus(this);
        TextView goal = Ui.text(this, f.isEmpty() ? "Bugün ne yapacaktın?" : "Hedefin: " + f, 20, Ui.FG);
        col.addView(goal, Ui.gapTop(this, 24));

        TextView ok = Ui.button(this, "Ana ekrana dön", true);
        ok.setOnClickListener(v -> goHome());
        col.addView(ok, Ui.wrapGap(this, 32));

        setContentView(col);
    }

    private void goHome() {
        startActivity(new Intent(this, MainActivity.class).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
        finish();
    }

    @Override
    public void onBackPressed() {
        goHome();
    }
}
