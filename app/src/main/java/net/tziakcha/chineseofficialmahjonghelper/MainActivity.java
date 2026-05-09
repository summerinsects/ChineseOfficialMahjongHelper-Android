package net.tziakcha.chineseofficialmahjonghelper;

import android.annotation.SuppressLint;
import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import androidx.activity.OnBackPressedCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import android.content.IntentFilter;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.view.View;
import android.widget.TextView;

import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
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
import java.io.File;
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
    private boolean mIsPaused = false;
    private boolean mCheckingVersion = false;
    private BroadcastReceiver mDownloadCompleteReceiver;

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
        contentView.findViewById(R.id.am_btn_version).setOnClickListener(view -> checkVersion(true));
        contentView.findViewById(R.id.am_btn_exit).setOnClickListener(
                view -> getOnBackPressedDispatcher().onBackPressed());

        getOnBackPressedDispatcher().addCallback(new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                new CommonConfirmDialog(MainActivity.this, "提示", "确定退出国标小助手？",
                        "确定", "取消", MainActivity.this::finishAffinity).show();
            }
        });

        requestMarqueeNotice();
        checkVersion(false);
    }

    @Override
    protected void onPause() {
        super.onPause();

        mIsPaused = true;
        if (mMarqueeNoticeView.getVisibility() == View.VISIBLE) {
            mMarqueeNoticeView.stopScroll();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        mIsPaused = false;
        if (mMarqueeNoticeView.getVisibility() == View.VISIBLE) {
            mMarqueeNoticeView.startScroll();
        }
    }

    private void requestMarqueeNotice() {
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
                            Utils.printDebugStackTrace(e);
                        }

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
                Utils.printDebugStackTrace(e);
            }
        }).start();
    }

    private void checkVersion(boolean manual) {
        if (mCheckingVersion) {
            return;
        }

        mCheckingVersion = true;
        new Thread(() -> {
            try {
                URL url = new URL("https://api.github.com/repos/summerinsects/ChineseOfficialMahjongHelper-Android/releases/latest");
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
                    if (!str.isEmpty()) {
                        try {
                            JSONObject obj = new JSONObject(str);
                            if (obj.has("tag_name")) {
                                String tag = obj.getString("tag_name");
                                int remoteVersion = parseVersionNumber(tag.substring(1));
                                int localVersion = parseVersionNumber(BuildConfig.VERSION_NAME);
                                if (remoteVersion > localVersion) {
                                    String body = obj.has("body") ? obj.getString("body") : "";
                                    String downloadUrl = null;
                                    if (obj.has("assets")) {
                                        JSONArray assets = obj.getJSONArray("assets");
                                        for (int i = 0, len = assets.length(); i < len; ++i) {
                                            JSONObject asset = assets.getJSONObject(i);
                                            if (asset.has("browser_download_url")) {
                                                downloadUrl = asset.getString("browser_download_url");
                                                break;
                                            }
                                        }
                                    }

                                    if (downloadUrl != null && !downloadUrl.isEmpty()) {
                                        String finalDownloadUrl = downloadUrl;
                                        runOnUiThread(
                                                () -> new CommonConfirmDialog(this, "检测到新版本",
                                                    tag + "，是否下载？\n\n" + body,
                                                    "更新", "取消",
                                                        () -> checkInstallPermissionBeforeDownload(finalDownloadUrl)).show());
                                    }
                                } else {
                                    if (manual && !mIsPaused) {
                                        runOnUiThread(
                                                () -> Utils.showToastShort(this, "已经是最新版本"));
                                    }
                                }
                            }
                        } catch (JSONException e) {
                            if (manual && !mIsPaused) {
                                runOnUiThread(
                                        () -> Utils.showToastShort(this, "获取最新版本失败"));
                            }

                            Utils.printDebugStackTrace(e);
                        }
                    }
                } finally {
                    urlConnection.disconnect();
                }
            } catch (IOException e) {
                if (manual && !mIsPaused) {
                    runOnUiThread(
                            () -> Utils.showToastShort(this, "获取最新版本失败"));
                }

                Utils.printDebugStackTrace(e);
            }

            mCheckingVersion = false;
        }).start();
    }

    private static int parseVersionNumber(String version) {
        String[] parts = version.split("\\.");
        int major = 0;
        int minor = 0;
        int patch = 0;
        if (parts.length >= 1) {
            major = Integer.parseInt(parts[0]);
        }
        if (parts.length >= 2) {
            minor = Integer.parseInt(parts[1]);
        }
        if (parts.length >= 3) {
            patch = Integer.parseInt(parts[2]);
        }
        return (major << 16) | (minor << 8) | patch;
    }

    private String mApkUrl = null;
    private final ActivityResultLauncher<Intent> mInstallPermissionLauncher
            = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
        // 用户从设置页面返回后，再次检查权限
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if (getPackageManager().canRequestPackageInstalls()) {
                startDownload();
            } else {
                Utils.showToastShort(this, "未授予安装权限，无法更新");
            }
        }
    });

    private void checkInstallPermissionBeforeDownload(String apkUrl) {
        mApkUrl = apkUrl;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if (!getPackageManager().canRequestPackageInstalls()) {
                // 跳转到系统设置页面授权
                Intent intent = new Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES);
                intent.setData(Uri.parse("package:" + getPackageName()));
                mInstallPermissionLauncher.launch(intent);
                return;
            }
        }
        startDownload();
    }

    private void startDownload() {
        File downloadDir = getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS);

        String fileName = "app_update_" + System.currentTimeMillis() + ".apk";
        final File apkFile = new File(downloadDir, fileName);

        DownloadManager.Request request = new DownloadManager.Request(Uri.parse(mApkUrl));
        request.setDestinationUri(Uri.fromFile(apkFile));
        request.setTitle("正在下载更新");
        request.setDescription("下载完成后将自动安装");
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
        request.setMimeType("application/vnd.android.package-archive");
        // 允许在漫游时下载（可选）
        request.setAllowedOverRoaming(false);
        // 仅允许 WiFi 下载（可选，根据需求设置）
        // request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI);

        DownloadManager downloadManager = (DownloadManager)getSystemService(Context.DOWNLOAD_SERVICE);
        long downloadId = downloadManager.enqueue(request);

        mDownloadCompleteReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                long completedDownloadId = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1);
                if (downloadId == completedDownloadId) {
                    installApk(apkFile);
                    context.unregisterReceiver(mDownloadCompleteReceiver);
                }
            }
        };

        ContextCompat.registerReceiver(this, mDownloadCompleteReceiver,
                new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE), ContextCompat.RECEIVER_EXPORTED);
    }

    private void installApk(File apkFile) {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        Uri apkUri = FileProvider.getUriForFile(this,
                this.getPackageName() + ".fileprovider", apkFile);
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

        intent.setDataAndType(apkUri, "application/vnd.android.package-archive");
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        startActivity(intent);
    }

    /**
     * A native method that is implemented by the 'chineseofficialmahjonghelper' native library,
     * which is packaged with this application.
     */
    public native String stringFromJNI();
}
