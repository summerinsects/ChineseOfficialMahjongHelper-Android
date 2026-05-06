package net.tziakcha.chineseofficialmahjonghelper.rule;

import android.os.Bundle;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import net.tziakcha.chineseofficialmahjonghelper.R;

public class RulePrincipleMainActivity extends AppCompatActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        View contentView = View.inflate(this, R.layout.common_main_layout, null);
        setContentView(contentView);

        getSupportFragmentManager().beginTransaction().add(
                R.id.cml_fl_root,
                new RulePrincipleListFragment()).commit();
    }
}
