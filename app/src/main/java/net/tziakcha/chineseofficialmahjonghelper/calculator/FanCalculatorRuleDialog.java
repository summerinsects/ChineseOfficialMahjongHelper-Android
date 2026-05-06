package net.tziakcha.chineseofficialmahjonghelper.calculator;

import android.app.AlertDialog;
import android.content.Context;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;

import net.tziakcha.chineseofficialmahjonghelper.R;

public class FanCalculatorRuleDialog extends AlertDialog {

    public FanCalculatorRuleDialog(@NonNull Context context) {
        super(context);
    }

    @Override
    public void show() {
        super.show();

        Context context = getContext();
        View contentView = View.inflate(context, R.layout.fan_calculator_rule_layout, null);

        ((TextView)contentView.findViewById(R.id.dhl_txt_title)).setText("规则说明");
        Button button = contentView.findViewById(R.id.dfl_btn_def);
        button.setText("确定");
        button.setOnClickListener(view -> dismiss());

        DisplayMetrics metrics = context.getResources().getDisplayMetrics();

        // 头28+2=30 脚2+80+6+6=94，30+94=124，留150
        int siblings = getContext().getResources().getDimensionPixelSize(R.dimen.dp150);
        contentView.findViewById(R.id.fcrl_sv).getLayoutParams().height =
                metrics.heightPixels - siblings;

        setContentView(contentView);

        Window window = getWindow();
        if (window != null) {
            window.setBackgroundDrawableResource(android.R.color.transparent);
        }
    }

}
