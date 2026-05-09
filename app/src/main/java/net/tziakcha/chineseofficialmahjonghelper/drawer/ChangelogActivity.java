package net.tziakcha.chineseofficialmahjonghelper.drawer;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import net.tziakcha.chineseofficialmahjonghelper.R;
import net.tziakcha.chineseofficialmahjonghelper.Utils;

public class ChangelogActivity extends AppCompatActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        View contentView = View.inflate(this, R.layout.common_scroll_layout, null);
        setContentView(contentView);

        ((TextView)contentView.findViewById(R.id.ab_txt)).setText("更新日志");

        contentView.findViewById(R.id.ab_r_btn).setVisibility(View.GONE);
        contentView.findViewById(R.id.ab_l_btn).setOnClickListener(view ->
                getOnBackPressedDispatcher().onBackPressed());

        ((TextView)contentView.findViewById(R.id.csl_txt)).setText(
                Utils.getStringFromAsset(this, "drawer/changelog.txt"));
    }

}
