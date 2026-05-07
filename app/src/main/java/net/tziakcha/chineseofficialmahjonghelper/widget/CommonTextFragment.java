package net.tziakcha.chineseofficialmahjonghelper.widget;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import net.tziakcha.chineseofficialmahjonghelper.R;

public class CommonTextFragment extends Fragment {

    public static CommonTextFragment newInstance(String title, String text) {
        CommonTextFragment fragment = new CommonTextFragment();
        Bundle bundle = new Bundle();
        bundle.putString("title", title);
        bundle.putString("text", text);
        fragment.setArguments(bundle);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View contentView = inflater.inflate(R.layout.common_scroll_layout, container, false);

        contentView.findViewById(R.id.ab_l_btn).setOnClickListener(view ->
                requireActivity().getOnBackPressedDispatcher().onBackPressed());

        contentView.findViewById(R.id.ab_r_btn).setVisibility(View.GONE);

        Bundle bundle = getArguments();
        if (bundle != null) {
            String title = bundle.getString("title");
            if (title != null) {
                ((TextView)contentView.findViewById(R.id.ab_txt)).setText(title);
            }

            String text = bundle.getString("text");
            if (text != null) {
                ((TextView)contentView.findViewById(R.id.csl_txt)).setText(text);
            }
        }

        return contentView;
    }

}
