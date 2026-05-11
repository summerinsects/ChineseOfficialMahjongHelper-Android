package net.tziakcha.chineseofficialmahjonghelper.record;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.util.TypedValue;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.widget.TextViewCompat;

import net.tziakcha.chineseofficialmahjonghelper.Common;
import net.tziakcha.chineseofficialmahjonghelper.R;

public class RecordPenaltyDialog extends AlertDialog {
    public interface OnSubmitListener {
        void onSubmit(int[] penalty);
    }

    private final String[] mNames;
    private final int[] mPenalty = new int[4];
    private final TextView[] mScoreTexts = new TextView[4];
    private TextView mCheckText;
    private final OnSubmitListener mOnSubmitListener;

    public RecordPenaltyDialog(@NonNull Context context, String[] names, int[] penalty, OnSubmitListener listener) {
        super(context);
        mNames = names;
        System.arraycopy(penalty, 0, mPenalty, 0, 4);
        mOnSubmitListener = listener;
    }

    @Override
    public void show() {
        super.show();

        setCancelable(false);

        Context context = getContext();
        View contentView = View.inflate(context, R.layout.record_penalty_layout, null);

        ((TextView)contentView.findViewById(R.id.dhl_txt_title)).setText("判罚调整");

        Button button = contentView.findViewById(R.id.dfl_btn_neg);
        button.setText("取消");
        button.setOnClickListener(view -> dismiss());

        button = contentView.findViewById(R.id.dfl_btn_pos);
        button.setText("确定");
        button.setOnClickListener(view -> {
            mOnSubmitListener.onSubmit(mPenalty);
            dismiss();
        });

        final int dp6 = context.getResources().getDimensionPixelSize(R.dimen.dp6);
        final int dp14 = context.getResources().getDimensionPixelSize(R.dimen.dp14);

        TextView textView;
        textView = contentView.findViewById(R.id.rpl_txt_name0);
        TextViewCompat.setAutoSizeTextTypeUniformWithConfiguration(textView,
                dp6, dp14, 2, TypedValue.COMPLEX_UNIT_PX);
        textView.setText(mNames[0]);
        textView = contentView.findViewById(R.id.rpl_txt_name1);
        TextViewCompat.setAutoSizeTextTypeUniformWithConfiguration(textView,
                dp6, dp14, 2, TypedValue.COMPLEX_UNIT_PX);
        textView.setText(mNames[1]);
        textView = contentView.findViewById(R.id.rpl_txt_name2);
        TextViewCompat.setAutoSizeTextTypeUniformWithConfiguration(textView,
                dp6, dp14, 2, TypedValue.COMPLEX_UNIT_PX);
        textView.setText(mNames[2]);
        textView = contentView.findViewById(R.id.rpl_txt_name3);
        TextViewCompat.setAutoSizeTextTypeUniformWithConfiguration(textView,
                dp6, dp14, 2, TypedValue.COMPLEX_UNIT_PX);
        textView.setText(mNames[3]);

        contentView.findViewById(R.id.rpl_btn_d100).setOnClickListener(view -> adjustScore(0, -10));
        contentView.findViewById(R.id.rpl_btn_d101).setOnClickListener(view -> adjustScore(1, -10));
        contentView.findViewById(R.id.rpl_btn_d102).setOnClickListener(view -> adjustScore(2, -10));
        contentView.findViewById(R.id.rpl_btn_d103).setOnClickListener(view -> adjustScore(3, -10));
        contentView.findViewById(R.id.rpl_btn_d50).setOnClickListener(view -> adjustScore(0, -5));
        contentView.findViewById(R.id.rpl_btn_d51).setOnClickListener(view -> adjustScore(1, -5));
        contentView.findViewById(R.id.rpl_btn_d52).setOnClickListener(view -> adjustScore(2, -5));
        contentView.findViewById(R.id.rpl_btn_d53).setOnClickListener(view -> adjustScore(3, -5));
        contentView.findViewById(R.id.rpl_btn_d10).setOnClickListener(view -> adjustScore(0, -1));
        contentView.findViewById(R.id.rpl_btn_d11).setOnClickListener(view -> adjustScore(1, -1));
        contentView.findViewById(R.id.rpl_btn_d12).setOnClickListener(view -> adjustScore(2, -1));
        contentView.findViewById(R.id.rpl_btn_d13).setOnClickListener(view -> adjustScore(3, -1));
        contentView.findViewById(R.id.rpl_btn_i10).setOnClickListener(view -> adjustScore(0, 1));
        contentView.findViewById(R.id.rpl_btn_i11).setOnClickListener(view -> adjustScore(1, 1));
        contentView.findViewById(R.id.rpl_btn_i12).setOnClickListener(view -> adjustScore(2, 1));
        contentView.findViewById(R.id.rpl_btn_i13).setOnClickListener(view -> adjustScore(3, 1));
        contentView.findViewById(R.id.rpl_btn_i50).setOnClickListener(view -> adjustScore(0, 5));
        contentView.findViewById(R.id.rpl_btn_i51).setOnClickListener(view -> adjustScore(1, 5));
        contentView.findViewById(R.id.rpl_btn_i52).setOnClickListener(view -> adjustScore(2, 5));
        contentView.findViewById(R.id.rpl_btn_i53).setOnClickListener(view -> adjustScore(3, 5));
        contentView.findViewById(R.id.rpl_btn_i100).setOnClickListener(view -> adjustScore(0, 10));
        contentView.findViewById(R.id.rpl_btn_i101).setOnClickListener(view -> adjustScore(1, 10));
        contentView.findViewById(R.id.rpl_btn_i102).setOnClickListener(view -> adjustScore(2, 10));
        contentView.findViewById(R.id.rpl_btn_i103).setOnClickListener(view -> adjustScore(3, 10));

        mScoreTexts[0] = contentView.findViewById(R.id.rpl_txt_pen0);
        mScoreTexts[1] = contentView.findViewById(R.id.rpl_txt_pen1);
        mScoreTexts[2] = contentView.findViewById(R.id.rpl_txt_pen2);
        mScoreTexts[3] = contentView.findViewById(R.id.rpl_txt_pen3);
        mCheckText = contentView.findViewById(R.id.rpl_txt_check);

        for (int i = 0; i < 4; ++i) {
            setPenaltyTextValue(mScoreTexts[i], mPenalty[i]);
        }
        refreshCheck();

        setContentView(contentView);

        Window window = getWindow();
        if (window != null) {
            window.setBackgroundDrawableResource(android.R.color.transparent);

            window.setLayout(context.getResources().getDisplayMetrics().widthPixels * 8 / 10,
                    WindowManager.LayoutParams.WRAP_CONTENT);
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

    @SuppressLint("DefaultLocale")
    public static void setPenaltyTextValue(TextView textView, int penalty) {
        textView.setText(String.format("%+d", penalty));
        if (penalty > 0) {
            textView.setTextColor(Common.COLOR_RED);
        } else if (penalty < 0) {
            textView.setTextColor(Common.COLOR_PURPLE);
        } else {
            textView.setTextColor(Common.COLOR_GRAY);
        }
    }

    @SuppressLint("DefaultLocale")
    private void refreshCheck() {
        int sum = mPenalty[0] + mPenalty[1] + mPenalty[2] + mPenalty[3];
        mCheckText.setText(String.format("%+d", sum));
        mCheckText.setTextColor(sum == 0 ? Common.COLOR_GRAY : Common.COLOR_RED);
    }

    private void adjustScore(int idx, int value) {
        setPenaltyTextValue(mScoreTexts[idx], mPenalty[idx] += value);
        refreshCheck();
    }

}
