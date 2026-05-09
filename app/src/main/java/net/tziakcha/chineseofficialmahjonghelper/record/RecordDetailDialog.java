package net.tziakcha.chineseofficialmahjonghelper.record;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.core.widget.TextViewCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import net.tziakcha.chineseofficialmahjonghelper.Common;
import net.tziakcha.chineseofficialmahjonghelper.Mahjong;
import net.tziakcha.chineseofficialmahjonghelper.R;
import net.tziakcha.chineseofficialmahjonghelper.Utils;
import net.tziakcha.chineseofficialmahjonghelper.widget.CommonConfirmDialog;
import net.tziakcha.chineseofficialmahjonghelper.widget.CommonTextDialog;

@SuppressLint("SetTextI18n")
public class RecordDetailDialog extends AlertDialog {

    public interface OnSubmitListener {
        void onSubmit(int period, RecordInfo.Detail detail);
    }
    private final int mSeatOrder;
    private final RecordInfo.Detail mDetail = new RecordInfo.Detail();
    private final boolean mModify;  // true=修改，false=新增
    private final int mMode;
    private final int mPeriod;
    private final String[] mNames = new String[4];
    private Button mSubmitButton;
    private EditText mFanEdit;
    private CheckBox mTieCheck;
    private CheckBox mTimeoutCheck;
    private final TextView[] mScoreTexts = new TextView[4];
    private final RadioButton[] mWinRadios = new RadioButton[4];
    private final RadioButton[] mClaimRadios = new RadioButton[4];
    private final TextView[] mPenaltyTexts = new TextView[4];
    private RecyclerView mFanRecyclerView;
    private int mFanValue = 8;
    private int mWinIndex = -1;
    private int mClaimIndex = -1;
    private boolean mExpand = false;
    private View mFoldableView;
    private final OnSubmitListener mOnSubmitListener;

    static private final int[][] REAL_SEAT_INDEX = {
            {0, 1, 2, 3}, {1, 0, 3, 2}, {2, 3, 1, 0}, {3, 2, 0, 1}
    };

    public RecordDetailDialog(@NonNull Context context, int order, int mode, int period,
            String[] names, RecordInfo.Detail detail, OnSubmitListener listener) {
        super(context);

        mSeatOrder = order;
        mModify = (detail != null);
        mMode = mode;
        mPeriod = period;
        mOnSubmitListener = listener;

        if (detail != null) {
            mDetail.fan = detail.fan;
            mDetail.timeout = detail.timeout;
            mDetail.fan_major = detail.fan_major;
            mDetail.fan_minor1 = detail.fan_minor1;
            mDetail.fan_minor2 = detail.fan_minor2;
        }

        // 位置转换
        if (mSeatOrder != 2) {
            final int[] realSeatIndex = REAL_SEAT_INDEX[period >> 2];
            final int offset = mSeatOrder == 0 ? 0 : period & 3;
            for (int i = 0; i < 4; ++i) {
                int idx = realSeatIndex[(i + offset) & 3];
                mNames[i] = names[idx];
            }

            if (detail != null) {
                mDetail.win_flag = 0;
                mDetail.claim_flag = 0;
                for (int i = 0; i < 4; ++i) {
                    int idx = realSeatIndex[(i + offset) & 3];
                    mDetail.penalty[i] = detail.penalty[idx];

                    int mask0 = 1 << idx;
                    int mask1 = 1 << i;
                    if ((detail.win_flag & mask0) != 0) {
                        mDetail.win_flag |= mask1;
                    }
                    if ((detail.claim_flag & mask0) != 0) {
                        mDetail.claim_flag |= mask1;
                    }
                }
            }
        } else {
            System.arraycopy(names, 0, mNames, 0, 4);

            if (detail != null) {
                System.arraycopy(detail.penalty, 0, mDetail.penalty, 0, 4);
                mDetail.win_flag = detail.win_flag;
                mDetail.claim_flag = detail.claim_flag;
            }
        }
    }

    private RecordInfo.Detail convertGameDetailBySeatOrder() {
        RecordInfo.Detail detail = new RecordInfo.Detail();

        detail.fan = mDetail.fan;
        detail.timeout = mDetail.timeout;
        detail.fan_major = mDetail.fan_major;
        detail.fan_minor1 = mDetail.fan_minor1;
        detail.fan_minor2 = mDetail.fan_minor2;

        // 位置转换
        if (mSeatOrder != 2) {
            final int[] realSeatIndex = REAL_SEAT_INDEX[mPeriod >> 2];
            final int offset = mSeatOrder == 0 ? 0 : mPeriod & 3;
            detail.win_flag = 0;
            detail.claim_flag = 0;
            for (int i = 0; i < 4; ++i) {
                int idx = realSeatIndex[(i + offset) & 3];
                detail.penalty[idx] = mDetail.penalty[i];

                int mask0 = 1 << idx;
                int mask1 = 1 << i;
                if ((mDetail.win_flag & mask1) != 0) {
                    detail.win_flag |= mask0;
                }
                if ((mDetail.claim_flag & mask1) != 0) {
                    detail.claim_flag |= mask0;
                }
            }
        } else {
            System.arraycopy(mDetail.penalty, 0, detail.penalty, 0, 4);
            detail.win_flag = mDetail.win_flag;
            detail.claim_flag = mDetail.claim_flag;
        }
        return detail;
    }

    @Override
    public void show() {
        super.show();

        Context context = getContext();
        View contentView = View.inflate(context, R.layout.record_detail_layout, null);

        contentView.findViewById(R.id.ab_l_btn).setOnClickListener(view -> onBackPressed());
        contentView.findViewById(R.id.ab_r_btn).setOnClickListener(view -> showInstruction());

        ((TextView)contentView.findViewById(R.id.ab_txt)).setText(
                Mahjong.WIND_TEXT[mPeriod >> 2] + "风" + Mahjong.WIND_TEXT[mPeriod & 3]);

        contentView.findViewById(R.id.rdl_btn_d5).setOnClickListener(view -> adjustFan(-5));
        contentView.findViewById(R.id.rdl_btn_d1).setOnClickListener(view -> adjustFan(-1));
        contentView.findViewById(R.id.rdl_btn_i1).setOnClickListener(view -> adjustFan(1));
        contentView.findViewById(R.id.rdl_btn_i5).setOnClickListener(view -> adjustFan(5));

        mFanEdit = contentView.findViewById(R.id.rdl_et_fan);
        mFanEdit.setSelection(1);
        mFanEdit.setOnFocusChangeListener((view, hasFocus) -> {
            if (!hasFocus) {
                String str = ((EditText)view).getText().toString();
                if (!str.isEmpty()) {
                    int fan = Integer.parseUnsignedInt(str);  // 获取输入框里所填番数
                    if (fan < 8) {
                        fan = 8;
                        ((EditText)view).setText(String.valueOf(fan));
                    }
                    if (mFanValue != fan) {
                        mFanValue = fan;
                        refreshScore();
                    }
                } else {
                    ((EditText)view).setText(String.valueOf(mFanValue));
                }
            }
        });
        mFanEdit.setOnEditorActionListener((view, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                InputMethodManager imm = (InputMethodManager)getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
                view.clearFocus();
                return true;
            }
            return false;
        });

        final int dp8 = context.getResources().getDimensionPixelSize(R.dimen.dp8);
        final int dp12 = context.getResources().getDimensionPixelSize(R.dimen.dp12);
        final int dp16 = context.getResources().getDimensionPixelSize(R.dimen.dp16);
        final int dp28 = context.getResources().getDimensionPixelSize(R.dimen.dp28);

        mTieCheck = contentView.findViewById(R.id.rdl_cb_tie);
        Utils.adaptCompoundButton(mTieCheck, dp28);
        mTimeoutCheck = contentView.findViewById(R.id.rdl_cb_to);
        Utils.adaptCompoundButton(mTimeoutCheck, dp28);

        TextView textView = contentView.findViewById(R.id.rdl_txt_order);
        TextViewCompat.setAutoSizeTextTypeUniformWithConfiguration(textView,
                dp8, dp12, 2, TypedValue.COMPLEX_UNIT_PX);
        textView.setText("选手按「"
                + (mSeatOrder == 0 ? "本圈座位" : mSeatOrder == 1 ? "本盘座位" : "开局座位") + "」排列");

        textView = contentView.findViewById(R.id.rdl_txt_name0);
        TextViewCompat.setAutoSizeTextTypeUniformWithConfiguration(textView,
                dp8, dp16, 2, TypedValue.COMPLEX_UNIT_PX);
        textView.setText(mNames[0]);
        textView = contentView.findViewById(R.id.rdl_txt_name1);
        TextViewCompat.setAutoSizeTextTypeUniformWithConfiguration(textView,
                dp8, dp16, 2, TypedValue.COMPLEX_UNIT_PX);
        textView.setText(mNames[1]);
        textView = contentView.findViewById(R.id.rdl_txt_name2);
        TextViewCompat.setAutoSizeTextTypeUniformWithConfiguration(textView,
                dp8, dp16, 2, TypedValue.COMPLEX_UNIT_PX);
        textView.setText(mNames[2]);
        textView = contentView.findViewById(R.id.rdl_txt_name3);
        TextViewCompat.setAutoSizeTextTypeUniformWithConfiguration(textView,
                dp8, dp16, 2, TypedValue.COMPLEX_UNIT_PX);
        textView.setText(mNames[3]);

        mScoreTexts[0] = contentView.findViewById(R.id.rdl_txt_score0);
        mScoreTexts[1] = contentView.findViewById(R.id.rdl_txt_score1);
        mScoreTexts[2] = contentView.findViewById(R.id.rdl_txt_score2);
        mScoreTexts[3] = contentView.findViewById(R.id.rdl_txt_score3);

        mWinRadios[0] = contentView.findViewById(R.id.rdl_rb_win0);
        mWinRadios[1] = contentView.findViewById(R.id.rdl_rb_win1);
        mWinRadios[2] = contentView.findViewById(R.id.rdl_rb_win2);
        mWinRadios[3] = contentView.findViewById(R.id.rdl_rb_win3);

        mClaimRadios[0] = contentView.findViewById(R.id.rdl_rb_clm0);
        mClaimRadios[1] = contentView.findViewById(R.id.rdl_rb_clm1);
        mClaimRadios[2] = contentView.findViewById(R.id.rdl_rb_clm2);
        mClaimRadios[3] = contentView.findViewById(R.id.rdl_rb_clm3);

        for (int i = 0; i < 4; ++i) {
            Utils.adaptCompoundButton(mWinRadios[i], dp28);
            Utils.adaptCompoundButton(mClaimRadios[i], dp28);
        }

        contentView.findViewById(R.id.rdl_ib_pen0).setOnClickListener(view -> showPenaltyDialog());
        contentView.findViewById(R.id.rdl_ib_pen1).setOnClickListener(view -> showPenaltyDialog());
        contentView.findViewById(R.id.rdl_ib_pen2).setOnClickListener(view -> showPenaltyDialog());
        contentView.findViewById(R.id.rdl_ib_pen3).setOnClickListener(view -> showPenaltyDialog());

        mPenaltyTexts[0] = contentView.findViewById(R.id.rdl_txt_pen0);
        mPenaltyTexts[1] = contentView.findViewById(R.id.rdl_txt_pen1);
        mPenaltyTexts[2] = contentView.findViewById(R.id.rdl_txt_pen2);
        mPenaltyTexts[3] = contentView.findViewById(R.id.rdl_txt_pen3);

        mFoldableView = contentView.findViewById(R.id.rdl_ll_foldable);
        contentView.findViewById(R.id.rdl_btn_exp).setOnClickListener(view -> onExpandButton((Button)view));

        mFanRecyclerView = contentView.findViewById(R.id.rdl_rv_fan);

        (mSubmitButton = contentView.findViewById(R.id.rdl_btn_sbm)).setOnClickListener(view -> onSubmitButton());

        contentView.findViewById(R.id.rdl_btn_minor).setOnClickListener(view -> showMinorFanDialog(false));

        // 修改
        if (mModify) {
            for (int i = 0; i < 4; ++i) {
                if ((mDetail.win_flag & (1 << i)) != 0) {
                    mWinIndex = i;
                    mWinRadios[i].setChecked(true);
                } else {
                    mWinRadios[i].setChecked(false);
                }
            }

            for (int i = 0; i < 4; ++i) {
                if ((mDetail.claim_flag & (1 << i)) != 0) {
                    mClaimIndex = i;
                    mClaimRadios[i].setChecked(true);
                } else {
                    mClaimRadios[i].setChecked(false);
                }

                mClaimRadios[i].setText(i != mWinIndex ? "点和" : "自摸");
            }

            if (mDetail.fan != 0) {
                String str = String.valueOf(mFanValue = Math.max(8, mDetail.fan));
                mFanEdit.setText(str);
                mFanEdit.setSelection(str.length());
            } else {
                if (mDetail.timeout) {
                    mTieCheck.setEnabled(false);
                    mTimeoutCheck.setChecked(true);
                } else {
                    mTieCheck.setChecked(true);
                    mTimeoutCheck.setEnabled(false);
                }
                for (int i = 0; i < 4; ++i) {
                    mWinRadios[i].setEnabled(false);
                    mClaimRadios[i].setEnabled(false);
                }
            }
        }

        // NOTE: setCheck会回调，所以最后再设置回调
        mTieCheck.setOnCheckedChangeListener((view, checked) -> onTieCheckBox(checked));
        mTimeoutCheck.setOnCheckedChangeListener((view, checked) -> onTimeoutCheckBox(checked));

        for (int i = 0; i < 4; ++i) {
            final int idx = i;
            mWinRadios[i].setOnCheckedChangeListener((view, checked) -> onWinRadioButton(idx, checked));
            mClaimRadios[i].setOnCheckedChangeListener((view, checked) -> onClaimRadioButton(idx, checked));
        }

        refreshScore();
        refreshPenalty();

        LinearLayoutManager llm = new LinearLayoutManager(getContext());
        mFanRecyclerView.setLayoutManager(llm);
        mFanRecyclerView.setAdapter(new FanRecyclerViewAdapter());

        contentView.findViewById(R.id.rdl_btn_skip6).setOnClickListener(
                view -> llm.scrollToPositionWithOffset(2, 0));
        contentView.findViewById(R.id.rdl_btn_skip8).setOnClickListener(
                view -> llm.scrollToPositionWithOffset(3, 0));
        contentView.findViewById(R.id.rdl_btn_skip12).setOnClickListener(
                view -> llm.scrollToPositionWithOffset(4, 0));
        contentView.findViewById(R.id.rdl_btn_skip16).setOnClickListener(
                view -> llm.scrollToPositionWithOffset(5, 0));
        contentView.findViewById(R.id.rdl_btn_skip24).setOnClickListener(
                view -> llm.scrollToPositionWithOffset(6, 0));

        loadRecentFans();

        setContentView(contentView);

        Window window = getWindow();
        if (window != null) {
            window.setBackgroundDrawableResource(android.R.color.transparent);
            window.clearFlags(WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM);

            window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
            WindowManager.LayoutParams lp = window.getAttributes();
            lp.dimAmount = 0.0f;
            window.setAttributes(lp);

            // NOTE: 部分手机状态栏会黑变，需要通过以下代码改变
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        }
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

    private void showInstruction() {
        Context context = getContext();
        new CommonTextDialog(context, "使用说明", context.getString(R.string.rd_instruction)).show();
    }

    private void onSubmitButton() {
        long fanBit = mDetail.fan_major;
        if (fanBit != 0 || mDetail.fan_minor2 != 0 || mDetail.fan_minor1 != 0) {  // 标记了番种
            if (mTieCheck.isChecked() || mTimeoutCheck.isChecked()) {  // 荒庄或者超时
                showConflictDialog();
                return;
            }
        } else {  // 未标记番种
            if (mWinIndex != -1 && !getContext().getSharedPreferences(Common.SHARED_PREF_NAME,
                    Context.MODE_PRIVATE).getBoolean("NoHintMinor", false)) {
                showMinorFanDialog(true);
                return;
            }
        }

        mOnSubmitListener.onSubmit(mPeriod, convertGameDetailBySeatOrder());

        if (fanBit != 0) {
            updateRecentFans(fanBit);
        }

        onBackPressed();
    }

    private void adjustFan(int value) {
        int prev = mFanValue;
        int fan = prev + value;
        if (fan > 332) {
            fan = 332;
        } else if (fan < 8) {
            fan = 8;
        }

        if (fan != prev) {
            String str = String.valueOf(fan);
            mFanValue = fan;
            mFanEdit.setText(str);
            mFanEdit.setSelection(str.length());
            refreshScore();
        }
    }

    private void onTieCheckBox(boolean checked) {
        if (checked) {
            mWinIndex = -1;
            mClaimIndex = -1;

            // 勾选荒庄，所有人的和牌、点炮禁用
            for (int i = 0; i < 4; ++i) {
                mWinRadios[i].setChecked(false);
                mWinRadios[i].setEnabled(false);
                mClaimRadios[i].setChecked(false);
                mClaimRadios[i].setEnabled(false);
            }
        } else {
            // 取消勾选荒庄，所有人的和牌、点炮启用
            for (int i = 0; i < 4; ++i) {
                mWinRadios[i].setChecked(false);
                mWinRadios[i].setEnabled(true);
                mClaimRadios[i].setChecked(false);
                mClaimRadios[i].setEnabled(true);
            }
        }
        mTimeoutCheck.setEnabled(!checked);
        refreshScore();
    }

    private void onTimeoutCheckBox(boolean checked) {
        if (checked) {
            mWinIndex = -1;
            mClaimIndex = -1;

            // 勾选超时，所有人的和牌、点炮禁用
            for (int i = 0; i < 4; ++i) {
                mWinRadios[i].setChecked(false);
                mWinRadios[i].setEnabled(false);
                mClaimRadios[i].setChecked(false);
                mClaimRadios[i].setEnabled(false);
                mClaimRadios[i].setText("点炮");
            }
        } else {
            // 取消勾选超时，所有人的和牌、点炮启用
            for (int i = 0; i < 4; ++i) {
                mWinRadios[i].setChecked(false);
                mWinRadios[i].setEnabled(true);
                mClaimRadios[i].setChecked(false);
                mClaimRadios[i].setEnabled(true);
                mClaimRadios[i].setText("点炮");
            }
        }
        mTieCheck.setEnabled(!checked);
        refreshScore();
    }

    private void onWinRadioButton(int idx, boolean checked) {
        if (checked) {
            mWinIndex = idx;

            // 把和牌对应的改为自摸，其他家点炮，并取消其他家和牌勾选
            for (int i = 0; i < 4; ++i) {
                if (i != idx) {
                    mClaimRadios[i].setText("点炮");
                    mWinRadios[i].setChecked(false);
                } else {
                    mClaimRadios[i].setText("自摸");
                }
            }
            refreshScore();
        }
    }

    private void onClaimRadioButton(int idx, boolean checked) {
        if (checked) {
            mClaimIndex = idx;

            // 取消其他家点炮勾选
            for (int i = 0; i < 4; ++i) {
                if (i != idx) {
                    mClaimRadios[i].setChecked(false);
                }
            }

            if (mWinIndex != -1) {
                refreshScore();
            }
        }
    }

    public static void setScoreTextColor(TextView[] scoreText, int[] scores, int winFlag, int claimFlag, int[] penalty) {
        for (int i = 0; i < 4; ++i) {
            if (scores[i] != 0) {
                if ((winFlag & (1 << i)) != 0) {  // 和牌：红色
                    scoreText[i].setTextColor(Common.COLOR_RED);
                } else {
                    if (penalty[i] < 0) {  // 罚分：紫色
                        scoreText[i].setTextColor(Common.COLOR_PURPLE);
                    } else if ((claimFlag & (1 << i)) != 0) {  // 点炮：蓝色
                        scoreText[i].setTextColor(Common.COLOR_BLUE);
                    } else {  // 其他：绿色
                        scoreText[i].setTextColor(Common.COLOR_GREEN);
                    }
                }
            } else {
                scoreText[i].setTextColor(Common.COLOR_GRAY);
            }
        }
    }

    @SuppressLint("DefaultLocale")
    private void refreshScore() {
        mDetail.win_flag = 0;
        mDetail.claim_flag = 0;

        int claimIndex = -1;
        if (mWinIndex != -1) {  // 有人和牌
            mDetail.fan = mFanValue;
            claimIndex = mClaimIndex;

            // 记录和牌和点炮
            mDetail.win_flag |= (1 << mWinIndex);
            if (mClaimIndex != -1) {
                mDetail.claim_flag |= (1 << claimIndex);
            }
        } else {  // 荒庄或者超时
            mDetail.fan = 0;
            mDetail.timeout = mTimeoutCheck.isChecked();
        }

        int[] scores = RecordInfo.translateToScores(mMode, mDetail.fan,
                mDetail.win_flag, mDetail.claim_flag, mDetail.penalty);

        for (int i = 0; i < 4; ++i) {
            mScoreTexts[i].setText(String.format("%+d", scores[i]));
        }

        // 使用不同颜色
        setScoreTextColor(mScoreTexts, scores, mDetail.win_flag, mDetail.claim_flag, mDetail.penalty);

        // 未选择和牌
        if (mWinIndex == -1) {
            // 荒庄或者超时时允许确定
            mSubmitButton.setEnabled(mTieCheck.isChecked() || mTimeoutCheck.isChecked());
        } else {
            // 未选择是点炮还是自摸时，不允许确定
            mSubmitButton.setEnabled(claimIndex != -1);
        }
    }

    private void refreshPenalty() {
        for (int i = 0; i < 4; ++i) {
            RecordPenaltyDialog.setPenaltyTextValue(mPenaltyTexts[i], mDetail.penalty[i]);
        }
    }

    private void showPenaltyDialog() {
        new RecordPenaltyDialog(getContext(), mNames, mDetail.penalty, penalty -> {
            System.arraycopy(penalty, 0, mDetail.penalty, 0, 4);
            refreshPenalty();
            refreshScore();
        }).show();
    }

    private void showConflictDialog() {
        new CommonConfirmDialog(getContext(), "标记条件冲突",
                "选择「荒庄」或者「超时」将忽略标记的番种", "确定", "取消", () -> {
            mDetail.fan_major = 0;
            mDetail.fan_minor2 = 0;
            mDetail.fan_minor1 = 0;

            mOnSubmitListener.onSubmit(mPeriod, convertGameDetailBySeatOrder());

            onBackPressed();
        }).show();
    }

    private void showMinorFanDialog(boolean callFromSubmitting) {
        new RecordMinorDialog(getContext(),
                mDetail.fan_minor2, mDetail.fan_minor1, (minor2, minor1) -> {
            mDetail.fan_minor2 = minor2;
            mDetail.fan_minor1 = minor1;

            if (callFromSubmitting) {
                mOnSubmitListener.onSubmit(mPeriod, convertGameDetailBySeatOrder());
                onBackPressed();
            }
        }).show();
    }

    private void onExpandButton(Button button) {
        if (mExpand) {
            mExpand = false;
            mFoldableView.setVisibility(View.VISIBLE);
            button.setText("\u2b06\ufe0e 展开");
        } else {
            mExpand = true;
            mFoldableView.setVisibility(View.GONE);
            button.setText("\u2b07\ufe0e 收起");
        }
    }

    private void onFanCheckedChange(int itemIndex, int which, boolean checked) {
        int fan = FAN_ITEM_INDEX[itemIndex][which];
        if (checked) {
            mDetail.fan_major |= (1L << (Mahjong.LAST_TILE - fan));

            // 如果输入框里的番数小于选择该番的番数，则校正数据
            int value = 0;
            for (int n = Mahjong.BIG_FOUR_WINDS; n < Mahjong.DRAGON_PUNG; ++n) {
                if ((mDetail.fan_major & (1L << (Mahjong.LAST_TILE - n))) != 0) {
                    value += Mahjong.FAN_VALUE_TABLE[n];
                }
            }

            if (mFanValue < value) {
                String str = String.valueOf(value);
                mFanValue = value;
                mFanEdit.setText(str);
                mFanEdit.setSelection(str.length());
                refreshScore();
            }
        } else {
            mDetail.fan_major &= ~(1L << (Mahjong.LAST_TILE - fan));
        }

        int position = -1;

        // 点击的不是「最近使用」
        if (itemIndex != 0) {
            // 如果该番在「最近使用」里，则更新
            for (int f : sRecentFanIndex) {
                if (fan == f) {
                    position = 0;
                    break;
                }
            }
        } else {
            // 点击「最近使用」，更新找到对应的番，更新
            int i = FAN_TO_POSITION[fan];
            if (i != 0) {
                position = i;
            }
        }

        if (position != -1) {
            RecyclerView.ViewHolder viewHolder = mFanRecyclerView.findViewHolderForAdapterPosition(position);
            if (viewHolder != null) {
                ((FanRecyclerViewHolder)viewHolder).refresh(fan, checked);
            }
        }
    }

    private static final String[] LIST_ITEM_TITLE = {
            "最近使用", "4番", "6番", "8番", "12番", "16番", "24番", "32番", "48番", "64番", "88番"
    };

    // 默认的最近使用：
    // 三色三步高23.15%、五门齐10.88%、三色三同顺9.78%、花龙9.17%、
    // 混一色8.18%、清龙7.74%、碰碰和5.68%、七对3.32%
    private static final int[] sRecentFanIndex = {
            Mahjong.MIXED_SHIFTED_CHOWS,
            Mahjong.ALL_TYPES,
            Mahjong.MIXED_TRIPLE_CHOW,
            Mahjong.MIXED_STRAIGHT,
            Mahjong.HALF_FLUSH,
            Mahjong.PURE_STRAIGHT,
            Mahjong.ALL_PUNGS,
            Mahjong.SEVEN_PAIRS
    };

    // 表格各行内容包含的番种索引
    private static final int[][] FAN_ITEM_INDEX = {
            sRecentFanIndex,  // 最近使用
            {
                    Mahjong.OUTSIDE_HAND,  // 全带幺
                    Mahjong.FULLY_CONCEALED_HAND,  // 不求人
                    Mahjong.TWO_MELDED_KONGS,  // 双明杠
                    Mahjong.LAST_TILE,  // 和绝张
            },
            {
                    Mahjong.ALL_PUNGS,  // 碰碰和
                    Mahjong.HALF_FLUSH,  // 混一色
                    Mahjong.MIXED_SHIFTED_CHOWS,  // 三色三步高
                    Mahjong.ALL_TYPES,  // 五门齐
                    Mahjong.MELDED_HAND,  // 全求人
                    Mahjong.TWO_CONCEALED_KONGS,  // 双暗杠
                    Mahjong.TWO_DRAGONS_PUNGS,  // 双箭刻
            },
            {
                    Mahjong.MIXED_STRAIGHT,  // 花龙
                    Mahjong.REVERSIBLE_TILES,  // 推不倒
                    Mahjong.MIXED_TRIPLE_CHOW,  // 三色三同顺
                    Mahjong.MIXED_SHIFTED_PUNGS,  // 三色三节高
                    Mahjong.CHICKEN_HAND,  // 无番和
                    Mahjong.LAST_TILE_DRAW,  // 妙手回春
                    Mahjong.LAST_TILE_CLAIM,  // 海底捞月
                    Mahjong.OUT_WITH_REPLACEMENT_TILE,  // 杠上开花
                    Mahjong.ROBBING_THE_KONG,  // 抢杠和
            },
            {
                    Mahjong.LESSER_HONORS_AND_KNITTED_TILES,  // 全不靠
                    Mahjong.KNITTED_STRAIGHT,  // 组合龙
                    Mahjong.UPPER_FOUR,  // 大于五
                    Mahjong.LOWER_FOUR,  // 小于五
                    Mahjong.BIG_THREE_WINDS,  // 三风刻
            },
            {
                    Mahjong.PURE_STRAIGHT,  // 清龙
                    Mahjong.THREE_SUITED_TERMINAL_CHOWS,  // 三色双龙会
                    Mahjong.PURE_SHIFTED_CHOWS_1,  // 一色三步高Ⅰ
                    Mahjong.PURE_SHIFTED_CHOWS_2,  // 一色三步高Ⅱ
                    Mahjong.ALL_FIVE,  // 全带五
                    Mahjong.TRIPLE_PUNG,  // 三同刻
                    Mahjong.THREE_CONCEALED_PUNGS,  // 三暗刻
            },
            {
                    Mahjong.SEVEN_PAIRS,  // 七对
                    Mahjong.GREATER_HONORS_AND_KNITTED_TILES,  // 七星不靠
                    Mahjong.ALL_EVEN_PUNGS,  // 全双刻
                    Mahjong.FULL_FLUSH,  // 清一色
                    Mahjong.PURE_TRIPLE_CHOW,  // 一色三同顺
                    Mahjong.PURE_SHIFTED_PUNGS,  // 一色三节高
                    Mahjong.UPPER_TILES,  // 全大
                    Mahjong.MIDDLE_TILES,  // 全中
                    Mahjong.LOWER_TILES,  // 全小
            },
            {
                    Mahjong.FOUR_PURE_SHIFTED_CHOWS_1,  // 一色四步高Ⅰ
                    Mahjong.FOUR_PURE_SHIFTED_CHOWS_2,  // 一色四步高Ⅱ
                    Mahjong.THREE_KONGS,  // 三杠
                    Mahjong.ALL_TERMINALS_AND_HONORS,  // 混幺九
            },
            {
                    Mahjong.QUADRUPLE_CHOW,  // 一色四同顺
                    Mahjong.FOUR_PURE_SHIFTED_PUNGS,  // 一色四节高
            },
            {
                    Mahjong.ALL_TERMINALS,  // 清幺九
                    Mahjong.LITTLE_FOUR_WINDS,  // 小四喜
                    Mahjong.LITTLE_THREE_DRAGONS,  // 小三元
                    Mahjong.ALL_HONORS,  // 字一色
                    Mahjong.FOUR_CONCEALED_PUNGS,  // 四暗刻
                    Mahjong.PURE_TERMINAL_CHOWS,  // 一色双龙会
            },
            {
                    Mahjong.BIG_FOUR_WINDS,  // 大四喜
                    Mahjong.BIG_THREE_DRAGONS,  // 大三元
                    Mahjong.ALL_GREEN,  // 绿一色
                    Mahjong.NINE_GATES,  // 九莲宝灯
                    Mahjong.FOUR_KONGS,  // 四杠
                    Mahjong.SEVEN_SHIFTED_PAIRS,  // 连七对
                    Mahjong.THIRTEEN_ORPHANS,  // 十三幺
            },
    };

    // 从番种索引查到表格位置
    private static final int[] FAN_TO_POSITION = {
            0,
            10, 10, 10, 10, 10, 10, 10,
            9, 9, 9, 9, 9, 9,
            8, 8,
            7, 7, 7, 7,
            6, 6, 6, 6, 6, 6, 6, 6, 6,
            5, 5, 5, 5, 5, 5, 5,
            4, 4, 4, 4, 4,
            3, 3, 3, 3, 3, 3, 3, 3, 3,
            2, 2, 2, 2, 2, 2, 2,
            1, 1, 1, 1,
    };

    private static float sButtonGapF;
    private static float sButtonWidthF;
    private static float sButtonHeightF;
    private static int sButtonGapI;
    private static int sButtonWidthI;
    private static int sButtonHeightI;

    private final class FanRecyclerViewHolder extends RecyclerView.ViewHolder {

        private TextView mTitleText;
        private final View[] mWrappers = new View[9];
        private final CheckBox[] mCheckBoxes = new CheckBox[9];
        private final TextView[] mFanTexts = new TextView[9];
        private int mIndex = -1;

        FanRecyclerViewHolder(@NonNull View itemView) {
            super(itemView);
            init(itemView.getContext());
        }

        private void init(Context context) {
            if (sButtonWidthI == 0) {
                DisplayMetrics metrics = context.getResources().getDisplayMetrics();
                sButtonGapF = context.getResources().getDimension(R.dimen.dp5);
                sButtonHeightF = context.getResources().getDimension(R.dimen.dp28);
                sButtonWidthF = (metrics.widthPixels - 5 * sButtonGapF) * 0.25f;
                sButtonGapI = (int)sButtonGapF;
                sButtonWidthI = (int)sButtonWidthF;
                sButtonHeightI = (int)sButtonHeightF;
            }

            LinearLayout linearLayout = (LinearLayout)itemView;
            linearLayout.setOrientation(LinearLayout.VERTICAL);
            linearLayout.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT));

            final int dp16 = context.getResources().getDimensionPixelSize(R.dimen.dp16);
            final int dp6 = context.getResources().getDimensionPixelSize(R.dimen.dp6);

            // 几番
            TextView textView0 = new TextView(context);
            textView0.setLayoutParams(new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            textView0.setGravity(Gravity.CENTER_VERTICAL);
            textView0.setTextColor(ContextCompat.getColor(context, R.color.text_1));
            textView0.setTextSize(TypedValue.COMPLEX_UNIT_PX, dp16);
            linearLayout.addView(textView0);
            mTitleText = textView0;

            // 下面所有按钮的根View
            RelativeLayout root = new RelativeLayout(context);
            root.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT));
            linearLayout.addView(root);

            for (int i = 0; i < 9; ++i) {
                View wrapper = View.inflate(context, R.layout.fan_toggle_wrapper_layout, null);
                RelativeLayout.LayoutParams rlp = new RelativeLayout.LayoutParams(sButtonWidthI, sButtonHeightI);
                rlp.topMargin = (int)(sButtonGapF + (sButtonHeightF + sButtonGapF) * (i >> 2));
                rlp.leftMargin = (int)((sButtonWidthF + sButtonGapF) * (i & 3));
                wrapper.setLayoutParams(rlp);
                root.addView(wrapper);
                mWrappers[i] = wrapper;

                CheckBox checkBox = wrapper.findViewById(R.id.ftw_cb);
                checkBox.setOnCheckedChangeListener(getCheckBoxCallback(i));
                mCheckBoxes[i] = checkBox;

                TextView textView1 = wrapper.findViewById(R.id.ftw_txt);
                TextViewCompat.setAutoSizeTextTypeUniformWithConfiguration(textView1,
                        dp6, dp16, 2, TypedValue.COMPLEX_UNIT_PX);
                mFanTexts[i] = textView1;
            }
        }

        public void setup(int idx, long major) {
            mIndex = idx;

            mTitleText.setText(LIST_ITEM_TITLE[idx]);
            ((LinearLayout.LayoutParams)mTitleText.getLayoutParams()).topMargin
                    = idx != 0 ? sButtonGapI : 0;

            final int[] itemIndex = FAN_ITEM_INDEX[idx];
            final int cnt = itemIndex.length;  // 一共有这么多

            // 在这之前的显示
            for (int i = 0; i < cnt; ++i) {
                int n = itemIndex[i];
                mFanTexts[i].setText(Mahjong.FAN_NAME[n]);
                mCheckBoxes[i].setChecked((major & (1L << (Mahjong.LAST_TILE - n))) != 0);
                mWrappers[i].setVisibility(View.VISIBLE);
            }

            // 在这之后的隐藏
            for (int i = cnt; i < 9; ++i) {
                mWrappers[i].setVisibility(View.GONE);
            }
        }

        public void refresh(int fan, boolean checked) {
            final int[] itemIndex = FAN_ITEM_INDEX[mIndex];
            final int cnt = itemIndex.length;  // 一共有这么多
            for (int i = 0; i < cnt; ++i) {
                int k = itemIndex[i];
                if (fan == k) {
                    mCheckBoxes[i].setOnCheckedChangeListener(null);
                    mCheckBoxes[i].setChecked(checked);
                    mCheckBoxes[i].setOnCheckedChangeListener(getCheckBoxCallback(i));
                }
            }
        }

        private CheckBox.OnCheckedChangeListener getCheckBoxCallback(int which) {
            return (view, checked) -> {
                if (mIndex != -1) {
                    onFanCheckedChange(mIndex, which, checked);
                }
            };
        }
    }

    private final class FanRecyclerViewAdapter extends RecyclerView.Adapter<FanRecyclerViewHolder> {

        @NonNull
        @Override
        public FanRecyclerViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new FanRecyclerViewHolder(new LinearLayout(parent.getContext()));
        }

        @Override
        public void onBindViewHolder(@NonNull FanRecyclerViewHolder holder, int position) {
            holder.setup(position, mDetail.fan_major);
        }

        @Override
        public int getItemCount() {
            return 11;
        }
    }

    private void updateRecentFans(long major) {
        int[] fanIndex = new int[8];
        int cnt = 0;

        // 1. 将所有标记的番写到temp
        for (int n = Mahjong.BIG_FOUR_WINDS; n < Mahjong.DRAGON_PUNG; ++n) {
            if ((major & (1L << (Mahjong.LAST_TILE - n))) != 0) {
                fanIndex[cnt++] = n;
                if (cnt >= 8) {
                    break;
                }
            }
        }

        // 2. 对于不满8个的，补充原来recentFans的数据，注意去重
        if (cnt < 8) {
            for (int i = 0; i < 8; ++i) {
                int n = sRecentFanIndex[i];
                if ((major & (1L << (Mahjong.LAST_TILE - n))) != 0) {
                    continue;
                }

                fanIndex[cnt++] = n;
                if (cnt >= 8) {
                    break;
                }
            }
        }

        System.arraycopy(fanIndex, 0, sRecentFanIndex, 0, 8);

        saveRecentFans();
    }

    private void loadRecentFans() {
        SharedPreferences sharedPreferences = getContext().getSharedPreferences(
                Common.SHARED_PREF_NAME, Context.MODE_PRIVATE);
        String str = sharedPreferences.getString("RecentFans", "");
        String[] res = str.split("\\|");
        if (res.length == 8) {
            for (int i = 0; i < 8; ++i) {
                sRecentFanIndex[i] = Integer.parseUnsignedInt(res[i]);
            }
        }
    }

    private void saveRecentFans() {
        StringBuilder str = new StringBuilder();
        str.append(sRecentFanIndex[0]);
        for (int i = 1; i < 8; ++i) {
            str.append('|');
            str.append(sRecentFanIndex[i]);
        }

        SharedPreferences sharedPreferences = getContext().getSharedPreferences(
                Common.SHARED_PREF_NAME, Context.MODE_PRIVATE);
        sharedPreferences.edit().putString("RecentFans", str.toString()).apply();
    }

}
