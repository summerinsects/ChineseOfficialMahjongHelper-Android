package net.tziakcha.chineseofficialmahjonghelper.widget;

import android.app.AlertDialog;
import android.content.Context;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;

import net.tziakcha.chineseofficialmahjonghelper.R;

public class CommonConfirmDialog extends AlertDialog {

    public interface OnConfirmListener {
        void onConfirm();
    }

    private final String mTitle;
    private final String mContent;
    private final String mPositiveText;
    private final String mNegativeText;
    private final OnConfirmListener mOnConfirmListener;

    public CommonConfirmDialog(@NonNull Context context, String title, String content,
            String positive, String negative, OnConfirmListener listener) {
        super(context);
        mTitle = title;
        mContent = content;
        mPositiveText = positive;
        mNegativeText = negative;
        mOnConfirmListener = listener;
    }

    @Override
    public void show() {
        super.show();

        Context context = getContext();
        View contentView = View.inflate(context, R.layout.common_confirm_layout, null);

        ((TextView)contentView.findViewById(R.id.dhl_txt_title)).setText(mTitle);
        ((TextView)contentView.findViewById(R.id.ccl_txt)).setText(mContent);
        Button button = contentView.findViewById(R.id.dfl_btn_pos);
        button.setText(mPositiveText);
        button.setOnClickListener(view -> {
            mOnConfirmListener.onConfirm();
            dismiss();
        });
        button = contentView.findViewById(R.id.dfl_btn_neg);
        button.setText(mNegativeText);
        button.setOnClickListener(view -> dismiss());

        setContentView(contentView);

        Window window = getWindow();
        if (window != null) {
            window.setBackgroundDrawableResource(android.R.color.transparent);
        }
    }

}
