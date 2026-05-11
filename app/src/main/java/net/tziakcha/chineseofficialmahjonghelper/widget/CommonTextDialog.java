package net.tziakcha.chineseofficialmahjonghelper.widget;

import android.app.AlertDialog;
import android.content.Context;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;

import net.tziakcha.chineseofficialmahjonghelper.R;

public class CommonTextDialog extends AlertDialog {

    private final String mTitle;
    private final String mContent;

    public CommonTextDialog(@NonNull Context context, String title, String content) {
        super(context);
        mTitle = title;
        mContent = content;
    }

    @Override
    public void show() {
        super.show();

        Context context = getContext();
        View contentView = View.inflate(context, R.layout.common_text_layout, null);

        ((TextView)contentView.findViewById(R.id.dhl_txt_title)).setText(mTitle);
        ((TextView)contentView.findViewById(R.id.ctl_txt)).setText(mContent);
        Button button = contentView.findViewById(R.id.dfl_btn_def);
        button.setText("确定");
        button.setOnClickListener(view -> dismiss());

        setContentView(contentView);

        Window window = getWindow();
        if (window != null) {
            window.setBackgroundDrawableResource(android.R.color.transparent);

            window.setLayout(context.getResources().getDisplayMetrics().widthPixels * 8 / 10,
                    WindowManager.LayoutParams.WRAP_CONTENT);
        }
    }

}
