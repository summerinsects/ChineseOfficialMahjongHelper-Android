package net.tziakcha.chineseofficialmahjonghelper.record;

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
import net.tziakcha.chineseofficialmahjonghelper.Utils;

import java.util.Random;

public class RecordStartDialog extends AlertDialog {
    public interface OnSubmitListener {
        boolean onSubmit(String[] names, String title, int mode);
    }

    private final EditText[] mNameEdit = new EditText[4];
    private EditText mTitleEdit;
    private final String[] mNames;
    private final String mTitle;
    private int mMode;
    private final boolean mMorePayment;
    private final boolean mStarted;
    private final OnSubmitListener mOnSubmitListener;

    public RecordStartDialog(@NonNull Context context, String[] names, String title, int mode,
            boolean morePayment, boolean started, OnSubmitListener listener) {
        super(context);
        mNames = names;
        mTitle = title;
        mMode = mode;
        mMorePayment = morePayment;
        mStarted = started;
        mOnSubmitListener = listener;
    }

    @Override
    public void show() {
        super.show();

        setCancelable(false);

        Context context = getContext();
        View contentView = View.inflate(context, R.layout.record_start_layout, null);

        ((TextView)contentView.findViewById(R.id.dhl_txt_title)).setText("编辑对局信息");

        Button button = contentView.findViewById(R.id.dfl_btn_neg);
        button.setText("取消");
        button.setOnClickListener(view -> dismiss());

        button = contentView.findViewById(R.id.dfl_btn_pos);
        button.setText("确定");
        button.setOnClickListener(view -> {
            String[] names = new String[4];
            for (int i = 0; i < 4; ++i) {
                names[i] = mNameEdit[i].getText().toString();
            }
            if (mOnSubmitListener.onSubmit(names, mTitleEdit.getText().toString(), mMode)) {
                dismiss();
            }
        });

        mNameEdit[0] = contentView.findViewById(R.id.ril_et_name0);
        mNameEdit[1] = contentView.findViewById(R.id.ril_et_name1);
        mNameEdit[2] = contentView.findViewById(R.id.ril_et_name2);
        mNameEdit[3] = contentView.findViewById(R.id.ril_et_name3);
        for (int i = 0; i < 4; ++i) {
            if (mNames[i] != null && !mNames[i].isEmpty()) {
                mNameEdit[i].setText(mNames[i]);
                mNameEdit[i].setSelection(mNames[i].length());
            }
        }

        mTitleEdit = contentView.findViewById(R.id.ril_et_title);
        if (mTitle != null && !mTitle.isEmpty()) {
            mTitleEdit.setText(mTitle);
            mTitleEdit.setSelection(mTitle.length());
        }
        mTitleEdit.setOnEditorActionListener((view, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                InputMethodManager imm = (InputMethodManager)getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
                view.clearFocus();
                return true;
            }
            return false;
        });

        contentView.findViewById(R.id.ril_ib_u1).setOnClickListener(view -> swapName(0, 1));
        contentView.findViewById(R.id.ril_ib_u2).setOnClickListener(view -> swapName(1, 2));
        contentView.findViewById(R.id.ril_ib_u3).setOnClickListener(view -> swapName(2, 3));
        contentView.findViewById(R.id.ril_ib_d0).setOnClickListener(view -> swapName(0, 1));
        contentView.findViewById(R.id.ril_ib_d1).setOnClickListener(view -> swapName(1, 2));
        contentView.findViewById(R.id.ril_ib_d2).setOnClickListener(view -> swapName(2, 3));

        contentView.findViewById(R.id.ril_ll_pay).setVisibility(mMorePayment ? View.VISIBLE : View.GONE);

        Button payButton = contentView.findViewById(R.id.ril_btn_pay);
        payButton.setText(RecordInfo.MODE_NAME_TEXT[mMode]);
        payButton.setOnClickListener(
                view -> new RecordPaymentDialog(getContext(), mMode, mode -> {
                    mMode = mode;
                    ((Button)view).setText(RecordInfo.MODE_NAME_TEXT[mode]);
                }).show());

        if (!mStarted) {
            contentView.findViewById(R.id.ril_btn_clear).setOnClickListener(view -> clearNames());
            contentView.findViewById(R.id.ril_btn_rand).setOnClickListener(view -> shuffleNames());
        } else {
            contentView.findViewById(R.id.ril_btn_clear).setEnabled(false);
            contentView.findViewById(R.id.ril_btn_rand).setEnabled(false);
            payButton.setEnabled(false);
        }

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

    private void swapName(int i0, int i1) {
        String name0 = mNameEdit[i0].getText().toString();
        String name1 = mNameEdit[i1].getText().toString();
        mNameEdit[i0].setText(name1);
        mNameEdit[i0].setSelection(name1.length());
        mNameEdit[i1].setText(name0);
        mNameEdit[i1].setSelection(name0.length());
    }

    private void clearNames() {
        for (int i = 0; i < 4; ++i) {
            mNameEdit[i].setText("");
        }
    }

    private void shuffleNames() {
        String[] names = new String[4];
        for (int i = 0; i < 4; ++i) {
            names[i] = mNameEdit[i].getText().toString();
            if (names[i].isEmpty()) {
                Utils.showToastShort(getContext(), "请先输入四位参赛选手姓名");
                return;
            }
        }

        Random r = new Random();
        int d = 4, p = 0, q = 4;
        for (--q, --d; p < q; ++p, --d) {
            int i = r.nextInt(d + 1);
            if (i != 0) {
                String t = names[p];
                names[p] = names[p + i];
                names[p + i] = t;
            }
        }

        for (int i = 0; i < 4; ++i) {
            mNameEdit[i].setText(names[i]);
            mNameEdit[i].setSelection(names[i].length());
        }
    }

}
