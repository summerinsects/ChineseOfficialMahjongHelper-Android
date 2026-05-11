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
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;

import net.tziakcha.chineseofficialmahjonghelper.CalendarUtils;
import net.tziakcha.chineseofficialmahjonghelper.R;
import net.tziakcha.chineseofficialmahjonghelper.Utils;
import net.tziakcha.chineseofficialmahjonghelper.widget.DatePickerDialog;

import java.util.Calendar;

public class RecordFilterDialog extends AlertDialog {
    public static final class FilterCondition {
        public long start_time;
        public long finish_time;
        public boolean time_enabled;
        public boolean ignore_case;
        public boolean whole_word;
        public boolean regular_enabled;
        public String name;
        public String title;
    }

    public interface OnSubmitListener {
        void onSubmit(FilterCondition filterCondition);
    }

    private final FilterCondition mFilterCondition = new FilterCondition();
    private final OnSubmitListener mOnSubmitListener;
    private final Button[] mTimeButton = new Button[2];
    private EditText mNameEdit;
    private EditText mTitleEdit;
    private final CalendarUtils.GregorianDate mStartDate;
    private final CalendarUtils.GregorianDate mFinishDate;

    public RecordFilterDialog(@NonNull Context context, FilterCondition filterCondition, OnSubmitListener listener) {
        super(context);
        mFilterCondition.start_time = filterCondition.start_time;
        mFilterCondition.finish_time = filterCondition.finish_time;
        mFilterCondition.time_enabled = filterCondition.time_enabled;
        mFilterCondition.ignore_case = filterCondition.ignore_case;
        mFilterCondition.whole_word = filterCondition.whole_word;
        mFilterCondition.regular_enabled = filterCondition.regular_enabled;
        mFilterCondition.name = filterCondition.name;
        mFilterCondition.title = filterCondition.title;
        mOnSubmitListener = listener;

        long now = System.currentTimeMillis();
        Calendar calendar = Calendar.getInstance();
        if (mFilterCondition.start_time != 0) {
            calendar.setTimeInMillis(mFilterCondition.start_time);
        } else {
            calendar.setTimeInMillis(now - 7 * 86400000);
        }
        mStartDate = new CalendarUtils.GregorianDate(calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH) + 1,
                calendar.get(Calendar.DAY_OF_MONTH));

        if (mFilterCondition.finish_time != 0) {
            calendar.setTimeInMillis(mFilterCondition.finish_time - 86400000);
        } else {
            calendar.setTimeInMillis(now);
        }
        mFinishDate = new CalendarUtils.GregorianDate(calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH) + 1,
                calendar.get(Calendar.DAY_OF_MONTH));
    }

    @Override
    public void show() {
        super.show();

        setCancelable(false);

        Context context = getContext();
        View contentView = View.inflate(context, R.layout.record_filter_layout, null);

        ((TextView)contentView.findViewById(R.id.dhl_txt_title)).setText("筛选条件");
        Button button = contentView.findViewById(R.id.dfl_btn_neg);
        button.setText("取消");
        button.setOnClickListener(view -> dismiss());

        button = contentView.findViewById(R.id.dfl_btn_pos);
        button.setText("确定");
        button.setOnClickListener(view -> {
            mFilterCondition.name = mNameEdit.getText().toString();
            mFilterCondition.title = mTitleEdit.getText().toString();

            Calendar calendar = Calendar.getInstance();
            calendar.set(mStartDate.year, mStartDate.month - 1, mStartDate.day);
            mFilterCondition.start_time = calendar.getTimeInMillis();

            calendar.set(mFinishDate.year, mFinishDate.month - 1, mFinishDate.day);
            mFilterCondition.finish_time = calendar.getTimeInMillis() + 86400000;

            mOnSubmitListener.onSubmit(mFilterCondition);
            dismiss();
        });

        button = contentView.findViewById(R.id.rfl_btn_st);
        button.setOnClickListener(view ->
            new DatePickerDialog(getContext(), mStartDate, date -> {
                mStartDate.assign(date);
                mTimeButton[0].setText(formatDate(date));
            }).show());
        mTimeButton[0] = button;

        button = contentView.findViewById(R.id.rfl_btn_ft);
        button.setOnClickListener(view ->
            new DatePickerDialog(getContext(), mFinishDate, date -> {
                mFinishDate.assign(date);
                mTimeButton[1].setText(formatDate(date));
            }).show());
        mTimeButton[1] = button;

        final int dp25 = context.getResources().getDimensionPixelSize(R.dimen.dp25);
        final int dp20 = context.getResources().getDimensionPixelSize(R.dimen.dp20);

        CheckBox checkBox = contentView.findViewById(R.id.rfl_cb_time);
        Utils.adaptCompoundButton(checkBox, dp25);
        checkBox.setOnCheckedChangeListener((view, isChecked) -> {
            if (isChecked) {
                mFilterCondition.time_enabled = true;
                mTimeButton[0].setEnabled(true);
                mTimeButton[1].setEnabled(true);
            } else {
                mFilterCondition.time_enabled = false;
                mTimeButton[0].setEnabled(false);
                mTimeButton[1].setEnabled(false);
            }
        });
        checkBox.setChecked(mFilterCondition.time_enabled);
        mTimeButton[0].setText(formatDate(mStartDate));
        mTimeButton[1].setText(formatDate(mFinishDate));

        mNameEdit = contentView.findViewById(R.id.rfl_et_name);
        if (mFilterCondition.name != null) {
            mNameEdit.setText(mFilterCondition.name);
            mNameEdit.setSelection(mFilterCondition.name.length());
        }

        mTitleEdit = contentView.findViewById(R.id.rfl_et_title);
        if (mFilterCondition.title != null) {
            mTitleEdit.setText(mFilterCondition.title);
            mTitleEdit.setSelection(mFilterCondition.title.length());
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

        checkBox = contentView.findViewById(R.id.rfl_cb_case);
        Utils.adaptCompoundButton(checkBox, dp25);
        checkBox.setChecked(mFilterCondition.ignore_case);
        checkBox.setOnCheckedChangeListener((view, isChecked) -> mFilterCondition.ignore_case = isChecked);

        checkBox = contentView.findViewById(R.id.rfl_cb_whole);
        Utils.adaptCompoundButton(checkBox, dp25);
        checkBox.setChecked(mFilterCondition.whole_word);
        checkBox.setOnCheckedChangeListener((view, isChecked) -> mFilterCondition.whole_word = isChecked);

        checkBox = contentView.findViewById(R.id.rfl_cb_regular);
        Utils.adaptCompoundButton(checkBox, dp20);
        checkBox.setChecked(mFilterCondition.regular_enabled);
        checkBox.setOnCheckedChangeListener((view, isChecked) -> mFilterCondition.regular_enabled = isChecked);

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

    private static String formatDate(int y, int m, int d) {
        StringBuilder str = new StringBuilder();
        str.append(y).append('-');
        if (m < 10) str.append('0');
        str.append(m).append('-');
        if (d < 10) str.append('0');
        str.append(d);
        return str.toString();
    }

    private static String formatDate(final CalendarUtils.GregorianDate date) {
        return formatDate(date.year, date.month, date.day);
    }

}
