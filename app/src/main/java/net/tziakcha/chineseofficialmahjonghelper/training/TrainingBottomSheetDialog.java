package net.tziakcha.chineseofficialmahjonghelper.training;

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

public class TrainingBottomSheetDialog extends BottomSheetDialogFragment {
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View contentView = inflater.inflate(R.layout.training_bottom_sheet_layout, container, false);
        contentView.findViewById(R.id.tbs_txt_count).setOnClickListener(view -> {
            Intent intent = new Intent(getActivity(), TrainingCountActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
            startActivity(intent);

            dismiss();
        });
        contentView.findViewById(R.id.tbs_txt_discard).setOnClickListener(view -> {
            Intent intent = new Intent(getActivity(), TrainingDiscardActivity.class);
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
                WindowManager.LayoutParams params = window.getAttributes();
                params.width = WindowManager.LayoutParams.MATCH_PARENT;
                params.height = (int)(getResources().getDisplayMetrics().heightPixels * 0.7);
                params.gravity = Gravity.BOTTOM;
                window.setAttributes(params);
            }
        }
    }
}
