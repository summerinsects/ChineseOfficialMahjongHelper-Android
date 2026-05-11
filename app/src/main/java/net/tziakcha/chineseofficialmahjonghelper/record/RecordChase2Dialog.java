package net.tziakcha.chineseofficialmahjonghelper.record;

import android.annotation.SuppressLint;
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

import net.tziakcha.chineseofficialmahjonghelper.Common;
import net.tziakcha.chineseofficialmahjonghelper.R;

public class RecordChase2Dialog extends AlertDialog {

    private final int mMode;
    private final EditText[] mEditText = new EditText[4];

    public RecordChase2Dialog(@NonNull Context context, int mode) {
        super(context);
        mMode = mode;
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void show() {
        super.show();

        setCancelable(false);

        Context context = getContext();
        View contentView = View.inflate(context, R.layout.record_chase2_layout, null);

        ((TextView)contentView.findViewById(R.id.dhl_txt_title)).setText("追分策略");

        Button button = contentView.findViewById(R.id.dfl_btn_def);
        button.setText("确定");
        button.setOnClickListener(view -> dismiss());

        TextView textView = contentView.findViewById(R.id.rc2l_txt_mode);
        if (context.getSharedPreferences(Common.SHARED_PREF_NAME, Context.MODE_PRIVATE)
                .getBoolean("MorePayment", false) || mMode != RecordInfo.MODE_STANDARD) {
            textView.setText("当前授受制：" + RecordInfo.MODE_NAME_TEXT[mMode]);
            textView.setVisibility(View.VISIBLE);
        } else {
            textView.setVisibility(View.GONE);
        }

        mEditText[0] = contentView.findViewById(R.id.rc2l_et_val0);
        mEditText[1] = contentView.findViewById(R.id.rc2l_et_val1);
        mEditText[2] = contentView.findViewById(R.id.rc2l_et_val2);
        mEditText[3] = contentView.findViewById(R.id.rc2l_et_val3);
        for (int i = 0; i < 4; ++i) {
            mEditText[i].setOnEditorActionListener((view, actionId, event) -> {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    InputMethodManager imm = (InputMethodManager)getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
                    view.clearFocus();
                    return true;
                }
                return false;
            });
        }

        contentView.findViewById(R.id.rc2l_btn_calc0).setOnClickListener(view -> onCalcButton0());
        contentView.findViewById(R.id.rc2l_btn_calc1).setOnClickListener(view -> onCalcButton1());
        contentView.findViewById(R.id.rc2l_btn_calc2).setOnClickListener(view -> onCalcButton2());
        contentView.findViewById(R.id.rc2l_btn_calc3).setOnClickListener(view -> onCalcButton3());

        setContentView(contentView);

        Window window = getWindow();
        if (window != null) {
            window.setBackgroundDrawableResource(android.R.color.transparent);
            window.clearFlags(WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM);

            window.setLayout(context.getResources().getDisplayMetrics().widthPixels * 8 / 10,
                    WindowManager.LayoutParams.WRAP_CONTENT);
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

    private void onCalcButton0() {
        String str = mEditText[0].getText().toString();
        if (str.isEmpty()) {
            return;
        }

        // 用分差计算 自摸、对点、旁点各需要多少番
        int delta = Integer.parseUnsignedInt(str);
        switch (mMode) {
            default: {
                // 标准
                // 自摸：3(f+8)+(f+8)=4f+32>d，即：f>(d-32)/4
                // 对点：(f+24)+(f+8)=2f+32>d，即：f>(d-32)/2
                // 旁点：(f+24)+8=f+32>d，即：f>d-32
                int tmp = delta - 32;
                if (tmp > 0) {
                    mEditText[1].setText(String.valueOf(Math.max(8, (tmp >> 2) + 1)));
                    mEditText[2].setText(String.valueOf(Math.max(8, (tmp >> 1) + 1)));
                    mEditText[3].setText(String.valueOf(Math.max(8, tmp + 1)));
                } else {
                    mEditText[1].setText("8");
                    mEditText[2].setText("8");
                    mEditText[3].setText("8");
                }
                break;
            }
            case RecordInfo.MODE_SPLIT_SELF_DRAWN: {
                // 自摸平摊
                // 自摸：3(⌈‌f/3⌉+8)+(⌈f/3⌉+8)=4⌈f/3⌉+32>d，即：⌈f/3⌉>(d-32)/4，f>(d-32)/4*3
                // 对点：(f+24)+(f+8)=2f+32>d，即：f>(d-32)/2
                // 旁点：(f+24)+8=f+32>d，即：f>d-32
                int tmp = delta - 32;
                if (tmp > 0) {
                    mEditText[1].setText(String.valueOf(Math.max(8, (tmp >> 2) * 3 + 1)));
                    mEditText[2].setText(String.valueOf(Math.max(8, (tmp >> 1) + 1)));
                    mEditText[3].setText(String.valueOf(Math.max(8, tmp + 1)));
                } else {
                    mEditText[1].setText("8");
                    mEditText[2].setText("8");
                    mEditText[3].setText("8");
                }
                break;
            }
            case RecordInfo.MODE_SHOOT_UNDERTAKE: {
                // 点炮承包
                // 自摸：3(f+8)+(f+8)=4f+32>d，即：f>(d-32)/4
                // 对点：(3f+24)+(3f+8)=6f+32>d，即：f>(d-32)/6
                // 旁点：(3f+24)+8=3f+32>d，即：f>(d-32)/3
                int tmp = delta - 32;
                if (tmp > 0) {
                    mEditText[1].setText(String.valueOf(Math.max(8, (tmp >> 2) + 1)));
                    mEditText[2].setText(String.valueOf(Math.max(8, tmp / 6 + 1)));
                    mEditText[3].setText(String.valueOf(Math.max(8, tmp / 3 + 1)));
                } else {
                    mEditText[1].setText("8");
                    mEditText[2].setText("8");
                    mEditText[3].setText("8");
                }
                break;
            }
            case RecordInfo.MODE_INVOLVED_NO_BASE: {
                // 牵连免底
                // 自摸：3f+f=4f>d，即：f>d/4
                // 对点：(f+16)+f=2f+16>d，即：f>(d-16)/2
                // 旁点：(f+16)+8=f+24>d，即：f>d-24
                mEditText[1].setText(String.valueOf(Math.max(8, (delta >> 2) + 1)));
                if (delta > 16) {
                    mEditText[2].setText(String.valueOf(Math.max(8, ((delta - 16) >> 1) + 1)));
                } else {
                    mEditText[2].setText("8");
                }
                mEditText[3].setText(String.valueOf(Math.max(8, delta - 24 + 1)));
                break;
            }
        }
    }

    private void onCalcButton1() {
        String str = mEditText[1].getText().toString();
        if (str.isEmpty()) {
            return;
        }

        // 用自摸番数计算能追多少分
        int fan = Integer.parseUnsignedInt(str);
        switch (mMode) {
            default:
                // 标准/点炮承包：3(f+8)+(f+8)=4f+32
                mEditText[0].setText(String.valueOf((fan << 2) + 32));
                break;
            case RecordInfo.MODE_SPLIT_SELF_DRAWN:
                // 自摸平摊：3(f/3+8)+(f/3+8)=4(f/3)+32
                mEditText[0].setText(String.valueOf(((fan / 3 + (fan % 3 != 0 ? 1 : 0)) << 2) + 32));
                break;
            case RecordInfo.MODE_INVOLVED_NO_BASE:
                // 牵连免底：3f+f=4f
                mEditText[0].setText(String.valueOf(fan << 2));
                break;
        }

        mEditText[2].setText("");
        mEditText[3].setText("");
    }

    private void onCalcButton2() {
        String str = mEditText[2].getText().toString();
        if (str.isEmpty()) {
            return;
        }

        // 用对点番数计算能追多少分
        int fan = Integer.parseUnsignedInt(str);
        switch (mMode) {
            default:
                // 标准/自摸平摊：(f+24)+(f+8)=2f+32
                mEditText[0].setText(String.valueOf((fan << 1) + 32));
                break;
            case RecordInfo.MODE_SHOOT_UNDERTAKE:
                // 点炮承包：(3f+24)+(3f+8)=6f+32
                mEditText[0].setText(String.valueOf(fan * 6 + 32));
                break;
            case RecordInfo.MODE_INVOLVED_NO_BASE:
                // 牵连免底：(f+16)+f=2f+16
                mEditText[0].setText(String.valueOf((fan << 1) + 16));
                break;
        }

        mEditText[1].setText("");
        mEditText[3].setText("");
    }

    private void onCalcButton3() {
        String str = mEditText[3].getText().toString();
        if (str.isEmpty()) {
            return;
        }

        // 用旁点番数计算能追多少分
        int fan = Integer.parseUnsignedInt(str);
        switch (mMode) {
            default:
                // 标准/自摸平摊：(f+24)+8=f+32
                mEditText[0].setText(String.valueOf(fan + 32));
                break;
            case RecordInfo.MODE_SHOOT_UNDERTAKE:
                // 点炮承包：(3f+24)+8=3f+32
                mEditText[0].setText(String.valueOf(fan * 3 + 32));
                break;
            case RecordInfo.MODE_INVOLVED_NO_BASE:
                // 牵连免底：(f+16)+8=f+24
                mEditText[0].setText(String.valueOf(fan + 24));
                break;
        }

        mEditText[1].setText("");
        mEditText[2].setText("");
    }

}
