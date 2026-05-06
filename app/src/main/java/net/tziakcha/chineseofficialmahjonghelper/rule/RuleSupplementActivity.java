package net.tziakcha.chineseofficialmahjonghelper.rule;

import android.os.Bundle;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import net.tziakcha.chineseofficialmahjonghelper.R;
import net.tziakcha.chineseofficialmahjonghelper.widget.CommonWebFragment;

public class RuleSupplementActivity extends AppCompatActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        View contentView = View.inflate(this, R.layout.common_main_layout, null);
        setContentView(contentView);

        CommonWebFragment fragment = CommonWebFragment.newInstance("相关补充",
                "file:///android_asset/www/rule/supplement/index.html");

        getSupportFragmentManager().beginTransaction().add(
                R.id.cml_fl_root,
                fragment).commit();
    }

}
