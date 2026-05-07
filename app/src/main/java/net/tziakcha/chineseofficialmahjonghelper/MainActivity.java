package net.tziakcha.chineseofficialmahjonghelper;

import android.annotation.SuppressLint;
import android.content.Intent;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.TextView;

import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import net.tziakcha.chineseofficialmahjonghelper.calculator.FanCalculatorActivity;
import net.tziakcha.chineseofficialmahjonghelper.drawer.ChangelogActivity;
import net.tziakcha.chineseofficialmahjonghelper.drawer.SettingsActivity;
import net.tziakcha.chineseofficialmahjonghelper.other.OtherBottomSheetDialog;
import net.tziakcha.chineseofficialmahjonghelper.record.RecordMainActivity;
import net.tziakcha.chineseofficialmahjonghelper.rule.RuleBottomSheetDialog;
import net.tziakcha.chineseofficialmahjonghelper.theory.MahjongTheoryActivity;
import net.tziakcha.chineseofficialmahjonghelper.training.TrainingBottomSheetDialog;
import net.tziakcha.chineseofficialmahjonghelper.widget.CommonConfirmDialog;
import net.tziakcha.chineseofficialmahjonghelper.widget.MarqueeNoticeView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    // Used to load the 'chineseofficialmahjonghelper' library on application startup.
    static {
        System.loadLibrary("chineseofficialmahjonghelper");
    }

    private MarqueeNoticeView mMarqueeNoticeView;
    private final Handler mHandler = new Handler(Looper.getMainLooper());

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        View contentView = View.inflate(this, R.layout.activity_main, null);
        setContentView(contentView);

        // Example of a call to a native method
        TextView tv = contentView.findViewById(R.id.am_txt);
        tv.setText("v" + BuildConfig.VERSION_NAME + '\n' + stringFromJNI());

        ((TextView)contentView.findViewById(R.id.am_txt_version)).setText("v" + BuildConfig.VERSION_NAME);

        contentView.findViewById(R.id.am_btn_ext).setOnClickListener(
                view -> ((DrawerLayout)contentView).openDrawer(GravityCompat.START));

        contentView.findViewById(R.id.am_btn_calc).setOnClickListener(view -> {
            Intent intent = new Intent(this, FanCalculatorActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
            startActivity(intent);
        });
        contentView.findViewById(R.id.am_btn_record).setOnClickListener(view -> {
            Intent intent = new Intent(this, RecordMainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
            startActivity(intent);
        });
        contentView.findViewById(R.id.am_btn_rule).setOnClickListener(view -> {
            RuleBottomSheetDialog dialog = new RuleBottomSheetDialog();
            dialog.show(getSupportFragmentManager(), "RuleBottomSheetDialog");
        });
        contentView.findViewById(R.id.am_btn_theory).setOnClickListener(view -> {
            Intent intent = new Intent(this, MahjongTheoryActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
            startActivity(intent);
        });
        contentView.findViewById(R.id.am_btn_train).setOnClickListener(view -> {
            TrainingBottomSheetDialog dialog = new TrainingBottomSheetDialog();
            dialog.show(getSupportFragmentManager(), "TrainingBottomSheetDialog");
        });
        contentView.findViewById(R.id.am_btn_other).setOnClickListener(view -> {
            OtherBottomSheetDialog dialog = new OtherBottomSheetDialog();
            dialog.show(getSupportFragmentManager(), "OtherBottomSheetDialog");
        });

        mMarqueeNoticeView = contentView.findViewById(R.id.am_rv_marquee);

        contentView.findViewById(R.id.am_btn_setting).setOnClickListener(view -> {
            Intent intent = new Intent(this, SettingsActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
            startActivity(intent);
        });
        contentView.findViewById(R.id.am_btn_changelog).setOnClickListener(view -> {
            Intent intent = new Intent(this, ChangelogActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
            startActivity(intent);
        });
        contentView.findViewById(R.id.am_btn_exit).setOnClickListener(
                view -> getOnBackPressedDispatcher().onBackPressed());

        getOnBackPressedDispatcher().addCallback(new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                new CommonConfirmDialog(MainActivity.this, "提示", "确定退出国标小助手？",
                        "确定", "取消", MainActivity.this::finishAffinity).show();
            }
        });

        new Thread(() -> {
            try {
                URL url = new URL("https://gitee.com/summerinsects/ChineseOfficialMahjongHelperDataSource/raw/master/other/tips.json");
                HttpURLConnection urlConnection = (HttpURLConnection)url.openConnection();
                try {
                    BufferedInputStream is = new BufferedInputStream(urlConnection.getInputStream());
                    ByteArrayOutputStream buffer = new ByteArrayOutputStream();
                    int b;
                    while ((b = is.read()) != -1) {
                        buffer.write(b);
                    }

                    String str = buffer.toString("UTF-8");
                    is.close();

                    // 测试数据
                    //str = "[{\"time\":1853616000,\"detail\":\"<font color=\\\"#fe576e\\\">第20届中国麻将牌王赛和大师赛</font><font color=\\\"#555555\\\">将于</font><font color=\\\"#2c79B2\\\">2019年3月22日-27日</font><font color=\\\"#555555\\\">在</font><font color=\\\"#ff7f00\\\">陕西西安</font><font color=\\\"#555555\\\">举办，详见「其他」-「近期赛事」</font>\"}]";

                    if (!str.isEmpty()) {
                        ArrayList<String> tips = parseResponse(str);
                        if (!tips.isEmpty()) {
                            mHandler.post(() -> {
                                mMarqueeNoticeView.setVisibility(View.VISIBLE);
                                mMarqueeNoticeView.setNoticeList(tips);

                                // NOTE: 重要，否则不滚动，改用runOnUiThread也不行
                                mHandler.post(() -> mMarqueeNoticeView.startScroll());
                            });
                        }
                    }
                } finally {
                    urlConnection.disconnect();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();
    }

    @Override
    protected void onPause() {
        super.onPause();

        if (mMarqueeNoticeView.getVisibility() == View.VISIBLE) {
            mMarqueeNoticeView.stopScroll();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (mMarqueeNoticeView.getVisibility() == View.VISIBLE) {
            mMarqueeNoticeView.startScroll();
        }
    }

    private static ArrayList<String> parseResponse(String str) {

        final long now = System.currentTimeMillis() / 1000;
        ArrayList<String> tips = new ArrayList<>();

        try {
            JSONArray arr = new JSONArray(str);
            int len = arr.length();
            for (int i = 0; i < len; ++i) {
                JSONObject obj = arr.getJSONObject(i);

                long time = obj.has("time") ? obj.getLong("time") : 0;
                if (time > now) {
                    tips.add(obj.getString("detail"));
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return tips;
    }

    /**
     * A native method that is implemented by the 'chineseofficialmahjonghelper' native library,
     * which is packaged with this application.
     */
    public native String stringFromJNI();
}
