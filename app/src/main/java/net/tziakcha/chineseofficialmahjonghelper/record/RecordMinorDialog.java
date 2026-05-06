package net.tziakcha.chineseofficialmahjonghelper.record;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.TypedValue;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.widget.TextViewCompat;

import net.tziakcha.chineseofficialmahjonghelper.Common;
import net.tziakcha.chineseofficialmahjonghelper.Mahjong;
import net.tziakcha.chineseofficialmahjonghelper.R;
import net.tziakcha.chineseofficialmahjonghelper.Utils;

public class RecordMinorDialog extends AlertDialog {
    public interface OnSubmitListener {
        void onSubmit(int minor2, long minor1);
    }

    private final OnSubmitListener mOnSubmitListener;

    private static final int[] sLimitCount = {
            1, 1, 1, 1, 1, 3, 2, 1, 1, 1, 2, 2, 2, 2, 4, 1, 3, 1, 1, 1, 1, 1, 8
    };

    private final CheckBox[] mFanCheck = new CheckBox[23];
    private final TextView[] mFanText = new TextView[23];
    private CheckBox mNoHintCheck;
    private final int[] mFanCount = new int[23];

    public RecordMinorDialog(@NonNull Context context, int minor2, long minor1, OnSubmitListener listener) {
        super(context);
        for (int i = 0; i < 10; ++i) {
            mFanCount[i] = ((minor2 >> (i * 2)) & 3);
        }
        for (int i = 0; i < 13; ++i) {
            mFanCount[i + 10] = (int)((minor1 >> (i * 4)) & 0xf);
        }
        mOnSubmitListener = listener;
    }

    @SuppressLint("CutPasteId")
    @Override
    public void show() {
        super.show();

        setCancelable(false);

        Context context = getContext();
        View contentView = View.inflate(context, R.layout.record_minor_layout, null);

        ((TextView)contentView.findViewById(R.id.dhl_txt_title)).setText("标记小番");

        Button button = contentView.findViewById(R.id.dfl_btn_neg);
        button.setText("取消");
        button.setOnClickListener(view -> dismiss());

        button = contentView.findViewById(R.id.dfl_btn_pos);
        button.setText("确定");
        button.setOnClickListener(view -> {
            SharedPreferences sharedPreferences = getContext().getSharedPreferences(
                    Common.SHARED_PREF_NAME, Context.MODE_PRIVATE);
            sharedPreferences.edit().putBoolean("NoHintMinor", mNoHintCheck.isChecked()).apply();

            int minor2 = 0;
            for (int i = 0; i < 10; ++i) {
                minor2 |= (mFanCount[i] & 3) << i * 2;
            }

            long minor1 = 0;
            for (int i = 0; i < 13; ++i) {
                minor1 |= (long)(mFanCount[i + 10] & 0xf) << i * 4;
            }

            mOnSubmitListener.onSubmit(minor2, minor1);
            dismiss();
        });

        mNoHintCheck = contentView.findViewById(R.id.rml_cb_hint);
        mNoHintCheck.setChecked(context.getSharedPreferences(Common.SHARED_PREF_NAME,
                Context.MODE_PRIVATE).getBoolean("NoHintMinor", false));
        Utils.adaptCompoundButton(mNoHintCheck, getContext().getResources().getDimensionPixelSize(R.dimen.dp25));

        final int[] fan2Ids = {
                R.id.rml_fl_20, R.id.rml_fl_21, R.id.rml_fl_22, R.id.rml_fl_23,
                R.id.rml_fl_24, R.id.rml_fl_25, R.id.rml_fl_26, R.id.rml_fl_27,
                R.id.rml_fl_28, R.id.rml_fl_29,
        };
        for (int i = 0; i < 10; ++i) {
            View view = contentView.findViewById(fan2Ids[i]);
            setupButton(view.findViewById(R.id.mfb_cb), view.findViewById(R.id.mfb_txt), i);
        }

        final int[] fan1Ids = {
                R.id.rml_fl_10, R.id.rml_fl_11, R.id.rml_fl_12, R.id.rml_fl_13,
                R.id.rml_fl_14, R.id.rml_fl_15, R.id.rml_fl_16, R.id.rml_fl_17,
                R.id.rml_fl_18, R.id.rml_fl_19, R.id.rml_fl_110, R.id.rml_fl_111,
                R.id.rml_fl_112,
        };
        for (int i = 0; i < 13; ++i) {
            View view = contentView.findViewById(fan1Ids[i]);
            setupButton(view.findViewById(R.id.mfb_cb), view.findViewById(R.id.mfb_txt), i + 10);
        }

        setContentView(contentView);

        Window window = getWindow();
        if (window != null) {
            window.setBackgroundDrawableResource(android.R.color.transparent);
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

    @SuppressLint("SetTextI18n")
    private void setupButton(CheckBox checkBox, TextView textView, int idx) {
        TextViewCompat.setAutoSizeTextTypeUniformWithConfiguration(textView,
                getContext().getResources().getDimensionPixelSize(R.dimen.dp8),
                getContext().getResources().getDimensionPixelSize(R.dimen.dp16),
                2, TypedValue.COMPLEX_UNIT_PX);

        if (mFanCount[idx] < 2) {
            textView.setText(Mahjong.FAN_NAME[Mahjong.DRAGON_PUNG + idx]);
        } else {
            textView.setText(Mahjong.FAN_NAME[Mahjong.DRAGON_PUNG + idx] + "\u00d7" + mFanCount[idx]);
        }

        checkBox.setChecked(mFanCount[idx] != 0);
        checkBox.setOnCheckedChangeListener(getFanCheckedChangeListener(idx));
        mFanCheck[idx] = checkBox;
        mFanText[idx] = textView;
    }

    private CheckBox.OnCheckedChangeListener getFanCheckedChangeListener(int idx) {
        return (view, checked) -> onFanChecked(checked, idx);
    }

    @SuppressLint("SetTextI18n")
    private void onFanChecked(boolean checked, int idx) {
        // 叠加超过上限，回到0
        if (++mFanCount[idx] > sLimitCount[idx]) {
            mFanCount[idx] = 0;

            if (sLimitCount[idx] > 1) {
                // 如果本来是可叠加的，文本需要去掉xN
                mFanText[idx].setText(Mahjong.FAN_NAME[Mahjong.DRAGON_PUNG + idx]);
            }
        } else {
            if (!checked) {
                mFanCheck[idx].setOnCheckedChangeListener(null);
                mFanCheck[idx].setChecked(true);
                mFanCheck[idx].setOnCheckedChangeListener(getFanCheckedChangeListener(idx));
            }

            // 有数量，设置
            if (mFanCount[idx] > 1 && sLimitCount[idx] > 1) {
                mFanText[idx].setText(Mahjong.FAN_NAME[Mahjong.DRAGON_PUNG + idx] + "\u00d7" + mFanCount[idx]);
            }
        }
    }

}
