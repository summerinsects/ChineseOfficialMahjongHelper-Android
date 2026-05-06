package net.tziakcha.chineseofficialmahjonghelper.record;

import android.app.AlertDialog;
import android.content.Context;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.TextView;

import androidx.annotation.NonNull;

import net.tziakcha.chineseofficialmahjonghelper.R;
import net.tziakcha.chineseofficialmahjonghelper.Utils;

public class RecordSettingDialog extends AlertDialog {
    public interface OnSubmitListener {
        void onSubmit(boolean mode, int order);
    }

    private final int mMode;
    private final int mOrder;
    private final RadioButton[] mModeRadios = new RadioButton[2];
    private final RadioButton[] mOrderRadios = new RadioButton[3];
    private final OnSubmitListener mOnSubmitListener;

    public RecordSettingDialog(@NonNull Context context, int mode, int order, OnSubmitListener listener) {
        super(context);
        mMode = mode;
        mOrder = order;
        mOnSubmitListener = listener;
    }

    @Override
    public void show() {
        super.show();

        setCancelable(false);

        Context context = getContext();
        View contentView = View.inflate(context, R.layout.record_setting_layout, null);

        ((TextView)contentView.findViewById(R.id.dhl_txt_title)).setText("更多设置");

        Button button = contentView.findViewById(R.id.dfl_btn_neg);
        button.setText("取消");
        button.setOnClickListener(view -> dismiss());

        button = contentView.findViewById(R.id.dfl_btn_pos);
        button.setText("确定");
        button.setOnClickListener(view -> {
            mOnSubmitListener.onSubmit(mModeRadios[0].isChecked(),
                    mOrderRadios[0].isChecked() ? 0 : mOrderRadios[1].isChecked() ? 1 : 2);
            dismiss();
        });

        final int dp25 = context.getResources().getDimensionPixelSize(R.dimen.dp25);

        mModeRadios[0] = contentView.findViewById(R.id.rel_rb_mode0);
        mModeRadios[1] = contentView.findViewById(R.id.rel_rb_mode1);
        mModeRadios[mMode].setChecked(true);
        Utils.adaptCompoundButton(mModeRadios[0], dp25);
        Utils.adaptCompoundButton(mModeRadios[1], dp25);

        mOrderRadios[0] = contentView.findViewById(R.id.rel_rb_order0);
        mOrderRadios[1] = contentView.findViewById(R.id.rel_rb_order1);
        mOrderRadios[2] = contentView.findViewById(R.id.rel_rb_order2);
        mOrderRadios[mOrder].setChecked(true);
        Utils.adaptCompoundButton(mOrderRadios[0], dp25);
        Utils.adaptCompoundButton(mOrderRadios[1], dp25);
        Utils.adaptCompoundButton(mOrderRadios[2], dp25);

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

}
