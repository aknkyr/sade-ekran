package app.sade.ekran;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.util.TypedValue;
import android.view.Gravity;
import android.widget.LinearLayout;
import android.widget.TextView;

/** Ortak renkler ve kucuk yardimcilar. */
final class Ui {
    private Ui() {}

    static final int BG = Color.parseColor("#0B0B0A");
    static final int FG = Color.parseColor("#ECEBE6");
    static final int DIM = Color.parseColor("#7D7B74");
    static final int LINE = Color.parseColor("#2A2926");
    static final int WARN = Color.parseColor("#D9A54A");
    static final int STOP = Color.parseColor("#C9574B");

    static int dp(Context c, float v) {
        return Math.round(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, v, c.getResources().getDisplayMetrics()));
    }

    static LinearLayout column(Activity a) {
        LinearLayout l = new LinearLayout(a);
        l.setOrientation(LinearLayout.VERTICAL);
        l.setBackgroundColor(BG);
        int pad = dp(a, 24);
        l.setPadding(pad, dp(a, 36), pad, dp(a, 24));
        return l;
    }

    static TextView text(Context c, String s, float sp, int color) {
        TextView t = new TextView(c);
        t.setText(s);
        t.setTextSize(TypedValue.COMPLEX_UNIT_SP, sp);
        t.setTextColor(color);
        t.setTypeface(Typeface.create("sans-serif-light", Typeface.NORMAL));
        return t;
    }

    static TextView title(Context c, String s) {
        TextView t = text(c, s, 40, FG);
        t.setTypeface(Typeface.create(Typeface.SERIF, Typeface.NORMAL));
        return t;
    }

    static TextView label(Context c, String s) {
        TextView t = text(c, s.toUpperCase(new java.util.Locale("tr")), 11, DIM);
        t.setLetterSpacing(0.14f);
        return t;
    }

    static TextView button(Context c, String s, boolean primary) {
        TextView b = text(c, s, 16, primary ? BG : FG);
        b.setTypeface(Typeface.create("sans-serif", Typeface.NORMAL));
        b.setGravity(Gravity.CENTER);
        int h = dp(c, 18), v = dp(c, 12);
        b.setPadding(h, v, h, v);
        GradientDrawable d = new GradientDrawable();
        d.setCornerRadius(dp(c, 3));
        d.setColor(primary ? FG : BG);
        d.setStroke(dp(c, 1), primary ? FG : LINE);
        b.setBackground(d);
        b.setClickable(true);
        b.setFocusable(true);
        return b;
    }

    static LinearLayout.LayoutParams gapTop(Context c, int dpTop) {
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        lp.topMargin = dp(c, dpTop);
        return lp;
    }

    static LinearLayout.LayoutParams wrapGap(Context c, int dpTop) {
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        lp.topMargin = dp(c, dpTop);
        return lp;
    }
}
