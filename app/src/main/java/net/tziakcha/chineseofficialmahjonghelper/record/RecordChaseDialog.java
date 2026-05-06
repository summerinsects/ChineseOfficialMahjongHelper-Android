package net.tziakcha.chineseofficialmahjonghelper.record;

import android.app.AlertDialog;
import android.content.Context;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.core.content.ContextCompat;
import androidx.core.widget.TextViewCompat;

import net.tziakcha.chineseofficialmahjonghelper.Common;
import net.tziakcha.chineseofficialmahjonghelper.R;

public class RecordChaseDialog extends AlertDialog {

    private final int mMode;
    private final String[] mNames = new String[4];
    private final int[] mScores = new int[4];

    public RecordChaseDialog(@NonNull Context context, int mode, String[] names, int[] scores) {
        super(context);
        mMode = mode;

        for (int i = 0; i < 4; ++i) {
            int k = i;
            while (k > 0) {
                if (scores[i] <= mScores[k - 1]) {
                    break;
                }

                mNames[k] = mNames[k - 1];
                mScores[k] = mScores[k - 1];
                --k;
            }
            mNames[k] = names[i];
            mScores[k] = scores[i];
        }
    }

    @Override
    public void show() {
        super.show();

        setCancelable(false);

        Context context = getContext();
        View contentView = View.inflate(context, R.layout.record_chase_layout, null);

        ((TextView)contentView.findViewById(R.id.dhl_txt_title)).setText("追分策略");

        Button button = contentView.findViewById(R.id.dfl_btn_def);
        button.setText("确定");
        button.setOnClickListener(view -> dismiss());

        contentView.findViewById(R.id.rcl_btn_more).setOnClickListener(
                view -> new RecordChase2Dialog(getContext(), mMode).show());

        RelativeLayout rlSheet = contentView.findViewById(R.id.rcl_rl_sheet);
        DisplayMetrics metrics = context.getResources().getDisplayMetrics();
        final int colWidth1 = metrics.widthPixels / 6;
        final int colWidth2 = metrics.widthPixels / 9;
        final int colHeight = metrics.widthPixels / 16;
        final int textSize = (int)Math.ceil(colHeight * 0.95f);
        final int totalWidth = colWidth1 * 2 + colWidth2 * 4;

        rlSheet.getLayoutParams().width = totalWidth + 2;
        rlSheet.getLayoutParams().height = colHeight * 10 + 2;

        final String[] titles = {"追者", "被追", "分差", "自摸", "对点", "旁点"};
        final int[] xPos = {
                0,
                colWidth1,
                colWidth1 * 2,
                colWidth1 * 2 + colWidth2,
                colWidth1 * 2 + colWidth2 * 2,
                colWidth1 * 2 + colWidth2 * 3
        };
        final int[] colors = {
                Common.COLOR_ORANGE,
                Common.COLOR_ORANGE,
                Common.COLOR_GRAY,
                Common.COLOR_RED,
                Common.COLOR_BLUE,
                Common.COLOR_GREEN,
        };
        for (int i = 0; i < 6; ++i) {
            TextView textView = createTextView(context, textSize,
                    xPos[i], 0, i < 2 ? colWidth1 : colWidth2, colHeight);
            textView.setTextColor(colors[i]);
            textView.setText(titles[i]);
            rlSheet.addView(textView);
        }

        // 追者，y位置：2 4.5 8
        // 2倍之后4 9 16，拟合：(x+2)^2
        for (int i = 0; i < 3; ++i) {
            int t = i + 2;
            int yPos = t * t * colHeight / 2;
            TextView textView = createTextView(context, textSize, 0, yPos, colWidth1, colHeight);
            textView.setTextColor(Common.COLOR_ORANGE);
            textView.setText(mNames[i + 1]);
            rlSheet.addView(textView);
        }

        // 被追，y位置2 4 7，拟合：(9x+10) shr 2
        for (int i = 0; i < 3; ++i) {
            int yPos = ((9 * i + 10) >> 2) * colHeight;
            for (int n = 0; n <= i; ++n) {
                int yPos1 = yPos + colHeight * n;
                TextView textView = createTextView(context, textSize,
                        colWidth1, yPos1, colWidth1, colHeight);
                textView.setTextColor(Common.COLOR_ORANGE);
                textView.setText(mNames[i - n]);
                rlSheet.addView(textView);

                int[] res = calcFan(mMode, mScores[i - n] - mScores[i + 1]);
                for (int k = 0; k < 4; ++k) {
                    textView = createTextView(context, textSize,
                            colWidth1 * 2 + colWidth2 * k, yPos1, colWidth2, colHeight);
                    textView.setTextColor(colors[k + 2]);
                    textView.setText(String.valueOf(res[k]));
                    rlSheet.addView(textView);
                }
            }
        }

        final int lineColor = ContextCompat.getColor(context, R.color.text_1);

        // 横线
        final int[] xLength = {
                totalWidth,
                totalWidth,
                totalWidth,
                totalWidth,
                totalWidth,
                totalWidth - colWidth1,
                totalWidth,
                totalWidth,
                totalWidth - colWidth1,
                totalWidth - colWidth1,
                totalWidth
        };
        for (int i = 0; i < 11; ++i) {
            View hzLine = new View(context);
            RelativeLayout.LayoutParams rlp = new RelativeLayout.LayoutParams(xLength[i], 2);
            rlp.topMargin = i * colHeight;
            rlp.leftMargin = totalWidth - xLength[i];
            hzLine.setLayoutParams(rlp);
            hzLine.setBackgroundColor(lineColor);
            rlSheet.addView(hzLine);
        }

        // 竖线（边框）
        for (int i = 0; i < 2; ++i) {
            View vtLine = new View(context);
            RelativeLayout.LayoutParams rlp = new RelativeLayout.LayoutParams(2, colHeight * 10 + 2);
            rlp.leftMargin = i * (colWidth1 * 2 + colWidth2 * 4);
            vtLine.setLayoutParams(rlp);
            vtLine.setBackgroundColor(lineColor);
            rlSheet.addView(vtLine);
        }

        // 竖线
        // 长度1 1 2 3，拟合：(3x+4) shr 2
        // y位置0 2 4 7，拟合：(9x+1) shr 2
        for (int i = 0; i < 5; ++i) {
            for (int n = 0; n < 4; ++n) {
                View vtLine = new View(context);
                RelativeLayout.LayoutParams rlp = new RelativeLayout.LayoutParams(
                        2, ((3 * n + 4) >> 2) * colHeight);
                rlp.leftMargin = xPos[i + 1];
                rlp.topMargin = ((9 * n + 1) >> 2) * colHeight;
                vtLine.setLayoutParams(rlp);
                vtLine.setBackgroundColor(lineColor);
                rlSheet.addView(vtLine);
            }
        }

        setContentView(contentView);

        Window window = getWindow();
        if (window != null) {
            window.setBackgroundDrawableResource(android.R.color.transparent);

            WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams();
            layoutParams.copyFrom(window.getAttributes());
            layoutParams.width = totalWidth + 2 + context.getResources().getDimensionPixelSize(R.dimen.dp20);
            window.setAttributes(layoutParams);
        }

        // 监听返回键
        setOnKeyListener((dialogInterface, keyCode, keyEvent) -> {
            if (keyCode == KeyEvent.KEYCODE_BACK
                    && keyEvent.getAction() == KeyEvent.ACTION_UP) {
                dismiss();
                return true;  // 返回true表示事件已处理
            }
            return false;  // 返回false表示事件未处理，将传递到下一个监听器
        });
    }

    private TextView createTextView(Context context, int textSize, int x, int y, int width, int height) {
        TextView textView = new AppCompatTextView(context);
        TextViewCompat.setAutoSizeTextTypeUniformWithConfiguration(textView,
                10, textSize, 2, TypedValue.COMPLEX_UNIT_PX);
        textView.setGravity(Gravity.CENTER);
        textView.setPadding(5, 5, 5, 5);
        RelativeLayout.LayoutParams rlp = new RelativeLayout.LayoutParams(width, height);
        rlp.leftMargin = x;
        rlp.topMargin = y;
        textView.setLayoutParams(rlp);
        return textView;
    }

    private static int[] calcFan(int mode, int delta) {
        int[] res = {delta, 8, 8, 8};
        switch (mode) {
            default: {
                // 标准
                // 自摸：3(f+8)+(f+8)=4f+32>d，即：f>(d-32)/4
                // 对点：(f+24)+(f+8)=2f+32>d，即：f>(d-32)/2
                // 旁点：(f+24)+8=f+32>d，即：f>d-32
                int tmp = delta - 32;
                if (tmp > 0) {
                    res[1] = Math.max(8, (tmp >> 2) + 1);
                    res[2] = Math.max(8, (tmp >> 1) + 1);
                    res[3] = Math.max(8, tmp + 1);
                }
                break;
            }
            case RecordInfo.MODE_SPLIT_SELF_DRAWN: {
                // 自摸平摊
                // 自摸：3(⌈‌f/3⌉+8)+(⌈f/3⌉+8)=4⌈f/3⌉+32>d，即：⌈f/3⌉>(d-32)/4，f>(d-32)/4*3
                // 对点：(f+24)+(f+8)=2f+32>d，即：f>(d-32)/2
                // 旁点：(f+24)+8=f+32>d，即：f>d-32
                int tmp = delta - 32;
                if (tmp > 0) {
                    res[1] = Math.max(8, (tmp >> 2) * 3 + 1);
                    res[2] = Math.max(8, (tmp >> 1) + 1);
                    res[3] = Math.max(8, tmp + 1);
                }
                break;
            }
            case RecordInfo.MODE_SHOOT_UNDERTAKE: {
                // 点炮承包
                // 自摸：3(f+8)+(f+8)=4f+32>d，即：f>(d-32)/4
                // 对点：(3f+24)+(3f+8)=6f+32>d，即：f>(d-32)/6
                // 旁点：(3f+24)+8=3f+32>d，即：f>(d-32)/3
                int tmp = delta - 32;
                if (tmp > 0) {
                    res[1] = Math.max(8, (tmp >> 2) + 1);
                    res[2] = Math.max(8, tmp / 6 + 1);
                    res[3] = Math.max(8, tmp / 3 + 1);
                }
                break;
            }
            case RecordInfo.MODE_INVOLVED_NO_BASE: {
                // 牵连免底
                // 自摸：3f+f=4f>d，即：f>d/4
                // 对点：(f+16)+f=2f+16>d，即：f>(d-16)/2
                // 旁点：(f+16)+8=f+24>d，即：f>d-24
                res[1] = Math.max(8, (delta >> 2) + 1);
                if (delta > 16) {
                    res[2] = Math.max(8, ((delta - 16) >> 1) + 1);
                }
                res[3] = Math.max(8, delta - 24 + 1);
                break;
            }
        }

        return res;
    }

}
