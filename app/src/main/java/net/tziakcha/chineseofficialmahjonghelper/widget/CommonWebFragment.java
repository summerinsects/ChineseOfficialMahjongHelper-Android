package net.tziakcha.chineseofficialmahjonghelper.widget;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import net.tziakcha.chineseofficialmahjonghelper.R;

public class CommonWebFragment extends Fragment {

    public static CommonWebFragment newInstance(String title, String url) {
        CommonWebFragment fragment = new CommonWebFragment();
        Bundle bundle = new Bundle();
        bundle.putString("title", title);
        bundle.putString("url", url);
        fragment.setArguments(bundle);
        return fragment;
    }

    @SuppressLint("SetJavaScriptEnabled")
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View contentView = inflater.inflate(R.layout.common_web_layout, container, false);

        contentView.findViewById(R.id.ab_l_btn).setOnClickListener(view ->
                requireActivity().getOnBackPressedDispatcher().onBackPressed());

        contentView.findViewById(R.id.ab_r_btn).setVisibility(View.GONE);

        WebView webView = contentView.findViewById(R.id.cwl_wv);

        WebSettings settings = webView.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(true);
        settings.setAllowFileAccessFromFileURLs(true);
        settings.setAllowUniversalAccessFromFileURLs(true);
        settings.setUseWideViewPort(true);
        settings.setLoadWithOverviewMode(true);

        Bundle bundle = getArguments();
        if (bundle != null) {
            String title = bundle.getString("title");
            if (title != null) {
                ((TextView) contentView.findViewById(R.id.ab_txt)).setText(title);
            }

            String url = bundle.getString("url");
            if (url != null) {
                webView.loadUrl(url);
            }
        }

        return contentView;
    }

}
