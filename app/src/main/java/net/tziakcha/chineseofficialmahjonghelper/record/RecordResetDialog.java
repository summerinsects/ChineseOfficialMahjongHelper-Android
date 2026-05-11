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

public class RecordResetDialog extends AlertDialog {
    public interface OnSubmitListener {
        void onSubmit(boolean save);
    }

    private RadioButton mSaveRadio;
    private final OnSubmitListener mOnSubmitListener;

    public RecordResetDialog(@NonNull Context context, OnSubmitListener listener) {
        super(context);
        mOnSubmitListener = listener;
    }

    @Override
    public void show() {
        super.show();

        setCancelable(false);

        Context context = getContext();
        View contentView = View.inflate(context, R.layout.record_reset_layout, null);

        ((TextView)contentView.findViewById(R.id.dhl_txt_title)).setText("清空表格");

        Button button = contentView.findViewById(R.id.dfl_btn_neg);
        button.setText("取消");
        button.setOnClickListener(view -> dismiss());

        button = contentView.findViewById(R.id.dfl_btn_pos);
        button.setText("确定");
        button.setOnClickListener(view -> {
            mOnSubmitListener.onSubmit(mSaveRadio.isChecked());
            dismiss();
        });

        final int dp28 = context.getResources().getDimensionPixelSize(R.dimen.dp28);
        Utils.adaptCompoundButton(contentView.findViewById(R.id.rrl_rb_abandon), dp28);

        mSaveRadio = contentView.findViewById(R.id.rrl_rb_save);
        Utils.adaptCompoundButton(mSaveRadio, dp28);

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
