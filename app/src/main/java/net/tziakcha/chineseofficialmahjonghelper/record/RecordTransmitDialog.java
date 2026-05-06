package net.tziakcha.chineseofficialmahjonghelper.record;

import android.app.AlertDialog;
import android.content.Context;
import android.view.View;
import android.view.Window;
import android.widget.TextView;

import androidx.annotation.NonNull;

import net.tziakcha.chineseofficialmahjonghelper.R;

public class RecordTransmitDialog extends AlertDialog {

    public interface OnButtonListener {
        void onButton();
    }

    private final OnButtonListener mOnSendButtonListener;
    private final OnButtonListener mOnReceiveButtonListener;

    public RecordTransmitDialog(@NonNull Context context, OnButtonListener onSendButtonListener,
            OnButtonListener onReceiveButtonListener) {
        super(context);
        mOnSendButtonListener = onSendButtonListener;
        mOnReceiveButtonListener = onReceiveButtonListener;
    }

    @Override
    public void show() {
        super.show();

        Context context = getContext();
        View contentView = View.inflate(context, R.layout.record_transmit_layout, null);

        ((TextView)contentView.findViewById(R.id.dhl_txt_title)).setText("点对点传输");

        contentView.findViewById(R.id.rtl_btn_send).setOnClickListener(view -> {
            mOnSendButtonListener.onButton();
            dismiss();
        });
        contentView.findViewById(R.id.rtl_btn_recv).setOnClickListener(view -> {
            mOnReceiveButtonListener.onButton();
            dismiss();
        });

        setContentView(contentView);

        Window window = getWindow();
        if (window != null) {
            window.setBackgroundDrawableResource(android.R.color.transparent);
        }
    }

}
