package net.tziakcha.chineseofficialmahjonghelper.record;

import android.app.AlertDialog;
import android.content.Context;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.TextView;

import androidx.annotation.NonNull;

import net.tziakcha.chineseofficialmahjonghelper.R;
import net.tziakcha.chineseofficialmahjonghelper.Utils;

public class RecordPaymentDialog extends AlertDialog {
    public interface OnSubmitListener {
        void onSubmit(int mode);
    }

    private int mMode;
    private final OnSubmitListener mOnSubmitListener;

    public RecordPaymentDialog(@NonNull Context context, int mode, OnSubmitListener listener) {
        super(context);
        mMode = mode;
        mOnSubmitListener = listener;
    }

    @Override
    public void show() {
        super.show();

        setCancelable(false);

        Context context = getContext();
        View contentView = View.inflate(context, R.layout.record_payment_layout, null);

        ((TextView)contentView.findViewById(R.id.dhl_txt_title)).setText("授受制选择");

        Button button = contentView.findViewById(R.id.dfl_btn_neg);
        button.setText("取消");
        button.setOnClickListener(view -> dismiss());

        button = contentView.findViewById(R.id.dfl_btn_pos);
        button.setText("确定");
        button.setOnClickListener(view -> {
            mOnSubmitListener.onSubmit(mMode);
            dismiss();
        });

        final TextView instText = contentView.findViewById(R.id.ryl_txt_inst);
        final int[] stringIds = {
                R.string.rs_standard_instruction,
                R.string.rs_split_self_drawn_instruction,
                R.string.rs_shoot_undertake_instruction,
                R.string.rs_involved_no_base_instruction,
        };
        instText.setText(stringIds[mMode]);

        final int dp25 = context.getResources().getDimensionPixelSize(R.dimen.dp25);
        final int[] radioButtonIds = {
                R.id.ryl_rb_mode0,
                R.id.ryl_rb_mode1,
                R.id.ryl_rb_mode2,
                R.id.ryl_rb_mode3,
        };
        for (int i = 0; i < 4; ++i) {
            RadioButton radioButton = contentView.findViewById(radioButtonIds[i]);

            Utils.adaptCompoundButton(radioButton, dp25);
            radioButton.setChecked(mMode == i);

            final int idx = i;
            radioButton.setOnClickListener(view -> {
                mMode = idx;
                instText.setText(stringIds[mMode]);
            });
        }

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

}
