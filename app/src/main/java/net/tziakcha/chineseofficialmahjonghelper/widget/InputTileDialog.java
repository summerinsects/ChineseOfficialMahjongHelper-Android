package net.tziakcha.chineseofficialmahjonghelper.widget;

import android.app.AlertDialog;
import android.content.Context;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;

import net.tziakcha.chineseofficialmahjonghelper.R;

public class InputTileDialog extends AlertDialog {

    public interface OnConfirmListener {
        void onConfirm(InputTileDialog dialog);
    }

    private EditText mEditText;
    private final int mExampleString;
    private final OnConfirmListener mOnConfirmListener;

    public InputTileDialog(@NonNull Context context, int exampleString, OnConfirmListener listener) {
        super(context);
        mExampleString = exampleString;
        mOnConfirmListener = listener;
    }

    @Override
    public void show() {
        super.show();

        setCancelable(false);

        Context context = getContext();
        View contentView = View.inflate(context, R.layout.input_tile_layout, null);

        ((TextView)contentView.findViewById(R.id.dhl_txt_title)).setText("直接输入");

        if (mExampleString != 0) {
            ((TextView)contentView.findViewById(R.id.itl_txt_xmp)).setText(mExampleString);
        }
        mEditText = contentView.findViewById(R.id.itd_et_input);
        mEditText.setOnEditorActionListener((view, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                InputMethodManager imm = (InputMethodManager)getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
                view.clearFocus();
                return true;
            }
            return false;
        });

        Button button;
        button = contentView.findViewById(R.id.dfl_btn_pos);
        button.setText("确定");
        button.setOnClickListener(view -> mOnConfirmListener.onConfirm(this));
        button = contentView.findViewById(R.id.dfl_btn_neg);
        button.setText("取消");
        button.setOnClickListener(view -> dismiss());

        setContentView(contentView);

        Window window = getWindow();
        if (window != null) {
            window.setBackgroundDrawableResource(android.R.color.transparent);
            window.clearFlags(WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM);
        }

        // 监听返回键
        setOnKeyListener((dialogInterface, keyCode, keyEvent) -> {
            if (keyCode == KeyEvent.KEYCODE_BACK
                    && keyEvent.getAction() == KeyEvent.ACTION_UP) {
                dismiss();
                return true;  // 返回true表示事件已处理
            }
            return false;  // 返回false表示事件未处理，将传递到下一个监听器
        });
    }

    @Override
    public boolean dispatchTouchEvent(@NonNull MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            View view = getCurrentFocus();
            if (view instanceof EditText) {
                int[] location = {0, 0};
                view.getLocationOnScreen(location);
                int left = location[0];
                int top = location[1];
                int right = left + view.getWidth();
                int bottom = top + view.getHeight();
                int x = (int)event.getRawX();
                int y = (int)event.getRawY();
                if (x < left || x > right || y < top || y > bottom) {
                    InputMethodManager imm = (InputMethodManager)getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
                    view.clearFocus();
                }
            }
        }
        return super.dispatchTouchEvent(event);
    }

    public String getInput() {
        return mEditText.getText().toString();
    }

}
