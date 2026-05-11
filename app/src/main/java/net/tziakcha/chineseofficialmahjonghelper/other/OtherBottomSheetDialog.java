package net.tziakcha.chineseofficialmahjonghelper.other;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import net.tziakcha.chineseofficialmahjonghelper.R;

public class OtherBottomSheetDialog extends BottomSheetDialogFragment {
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View contentView = inflater.inflate(R.layout.other_bottom_sheet_layout, container, false);
        contentView.findViewById(R.id.obs_txt_competition).setOnClickListener(view -> {
            Intent intent = new Intent(getActivity(), OtherCompetitionListActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
            startActivity(intent);

            dismiss();
        });
        contentView.findViewById(R.id.obs_txt_trick).setOnClickListener(view -> {
            Intent intent = new Intent(getActivity(), OtherEntertainmentActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
            startActivity(intent);

            dismiss();
        });
        return contentView;
    }

    @Override
    public void onStart() {
        super.onStart();
        Dialog dialog = getDialog();
        if (dialog != null) {
            Window window = dialog.getWindow();
            if (window != null) {
                window.setLayout(WindowManager.LayoutParams.MATCH_PARENT,
                        getResources().getDisplayMetrics().heightPixels * 7 / 10);
                window.setGravity(Gravity.BOTTOM);
            }
        }
    }
}
