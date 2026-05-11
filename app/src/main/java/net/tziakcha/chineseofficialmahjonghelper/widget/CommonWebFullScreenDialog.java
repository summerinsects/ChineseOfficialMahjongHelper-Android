package net.tziakcha.chineseofficialmahjonghelper.widget;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.TextView;

import androidx.annotation.NonNull;

import net.tziakcha.chineseofficialmahjonghelper.R;

public class CommonWebFullScreenDialog extends AlertDialog {
    private final String mTitle;
    private final String mUrl;
    public CommonWebFullScreenDialog(@NonNull Context context, String title, String url) {
        super(context);
        mTitle = title;
        mUrl = url;
    }

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    public void show() {
        super.show();

        Context context = getContext();
        View contentView = View.inflate(context, R.layout.common_web_layout, null);

        contentView.findViewById(R.id.ab_l_btn).setOnClickListener(view -> onBackPressed());
        contentView.findViewById(R.id.ab_r_btn).setVisibility(View.GONE);

        ((TextView)contentView.findViewById(R.id.ab_txt)).setText(mTitle);

        WebView webView = contentView.findViewById(R.id.cwl_wv);

        WebSettings settings = webView.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(true);
        settings.setAllowFileAccessFromFileURLs(true);
        settings.setAllowUniversalAccessFromFileURLs(true);
        settings.setUseWideViewPort(true);
        settings.setLoadWithOverviewMode(true);

        webView.loadUrl(mUrl);

        setContentView(contentView);

        Window window = getWindow();
        if (window != null) {
            window.setBackgroundDrawableResource(android.R.color.transparent);
            window.clearFlags(WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM);

            window.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.MATCH_PARENT);
            window.setDimAmount(0.0f);

            // NOTE: 部分手机状态栏会黑变，需要通过以下代码改变
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        }
    }
}
