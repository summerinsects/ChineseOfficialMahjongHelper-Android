package net.tziakcha.chineseofficialmahjonghelper.drawer;

import android.os.Bundle;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import net.tziakcha.chineseofficialmahjonghelper.R;
import net.tziakcha.chineseofficialmahjonghelper.Utils;
import net.tziakcha.chineseofficialmahjonghelper.widget.CommonTextFragment;

public class ChangelogActivity extends AppCompatActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        View contentView = View.inflate(this, R.layout.common_main_layout, null);
        setContentView(contentView);

        CommonTextFragment fragment = CommonTextFragment.newInstance("更新日志",
                Utils.getStringFromAsset(this, "drawer/changelog.txt"));

        getSupportFragmentManager().beginTransaction().add(
                R.id.cml_fl_root,
                fragment).commit();
    }

}
