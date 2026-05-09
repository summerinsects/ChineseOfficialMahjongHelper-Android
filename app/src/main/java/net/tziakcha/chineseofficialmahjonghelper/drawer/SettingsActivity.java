package net.tziakcha.chineseofficialmahjonghelper.drawer;

import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.CheckBox;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import net.tziakcha.chineseofficialmahjonghelper.Common;
import net.tziakcha.chineseofficialmahjonghelper.R;
import net.tziakcha.chineseofficialmahjonghelper.Utils;
import net.tziakcha.chineseofficialmahjonghelper.training.TrainingCountActivity;
import net.tziakcha.chineseofficialmahjonghelper.training.TrainingDiscardActivity;

public class SettingsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        View contentView = View.inflate(this, R.layout.settings_layout, null);
        setContentView(contentView);

        ((TextView)contentView.findViewById(R.id.ab_txt)).setText("设置");

        contentView.findViewById(R.id.ab_l_btn).setOnClickListener(view ->
                getOnBackPressedDispatcher().onBackPressed());

        contentView.findViewById(R.id.ab_r_btn).setVisibility(View.GONE);

        CheckBox checkBox = contentView.findViewById(R.id.sl_record_cb_pay);
        checkBox.setChecked(getSharedPreferences(Common.SHARED_PREF_NAME, Context.MODE_PRIVATE)
                .getBoolean("MorePayment", false));
        checkBox.setOnCheckedChangeListener((view, checked) ->
                getSharedPreferences(Common.SHARED_PREF_NAME, Context.MODE_PRIVATE)
                        .edit().putBoolean("MorePayment", checked).apply());
        Utils.adaptCompoundButton(checkBox, getResources().getDimensionPixelSize(R.dimen.dp28));

        contentView.findViewById(R.id.sl_record_rl_pay).setOnClickListener(view -> checkBox.performClick());

        contentView.findViewById(R.id.sl_train_txt_discard).setOnClickListener(view -> resetTrainDiscard());
        contentView.findViewById(R.id.sl_train_txt_count).setOnClickListener(view -> resetTrainCount());
    }

    private void resetTrainDiscard() {
        Utils.showToastShort(this,
                TrainingDiscardActivity.resetPuzzle(getFilesDir()) ? "三色训练重置成功" : "三色训练重置失败");
    }

    private void resetTrainCount() {
        Utils.showToastShort(this,
                TrainingCountActivity.resetPuzzle(getFilesDir()) ? "算番训练重置成功" : "算番训练重置失败");
    }

}
