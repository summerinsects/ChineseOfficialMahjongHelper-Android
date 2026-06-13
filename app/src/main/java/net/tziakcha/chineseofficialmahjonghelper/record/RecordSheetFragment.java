package net.tziakcha.chineseofficialmahjonghelper.record;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ListPopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.core.content.ContextCompat;
import androidx.core.widget.TextViewCompat;
import androidx.fragment.app.Fragment;

import net.tziakcha.chineseofficialmahjonghelper.Common;
import net.tziakcha.chineseofficialmahjonghelper.Mahjong;
import net.tziakcha.chineseofficialmahjonghelper.R;
import net.tziakcha.chineseofficialmahjonghelper.Utils;
import net.tziakcha.chineseofficialmahjonghelper.widget.CommonConfirmDialog;
import net.tziakcha.chineseofficialmahjonghelper.widget.CommonTextDialog;

import java.util.Arrays;
import java.util.Calendar;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@SuppressLint("SetTextI18n")
public class RecordSheetFragment extends Fragment {

    private final TextView[] mNameTexts = new TextView[4];
    private final TextView[] mTotalTexts = new TextView[5];
    private final TextView[][] mScoreTexts = new TextView[16][4];
    private final TextView[] mFanTexts = new TextView[16];
    private final TextView[] mRankTexts = new TextView[4];
    private final TextView[] mNormTexts = new TextView[4];
    private final View[] mRecordButtonViews = new View[16];
    private TextView mModeText;

    private ListPopupWindow mPopupWindow;
    private TextView mTitleText;
    private View mStartButtonView;
    private View mFinishButtonView;
    private TextView mTimeText;
    private RecordInfo mRecordInfo = sRecordInfo;
    private final int[] mTotalScores = new int[4];
    private int mSeatOrder = 0;  // 座位转顺序：0=本圈，1=本盘，2=开局，3=固定选手
    private int mHeroIndex = 0;
    private boolean mSingleMode = true;
    private boolean mMorePayment = false;
    private boolean mIsActive = true;  // 当前是记录(true)还是查看(false)
    private ScheduledExecutorService mScheduledExecutorService = null;
    private long mLastRefreshTime = 0;

    private static final RecordInfo sRecordInfo = new RecordInfo();
    private static final String[] sPrevName = new String[4];
    private static String sPrevTitle = "";
    private static final String sFilePath = "record";
    private static final String sFileName = "active.json";

    public static RecordSheetFragment newInstance(RecordInfo record) {
        RecordSheetFragment fragment = new RecordSheetFragment();
        Bundle bundle = new Bundle();
        bundle.putString("record", RecordInfo.recordToString(record));
        fragment.setArguments(bundle);
        return fragment;
    }

    private void parseArguments() {
        mIsActive = true;

        Bundle bundle = getArguments();
        if (bundle != null) {
            String str = bundle.getString("record");
            RecordInfo record = new RecordInfo();
            if (RecordInfo.parseRecord(str, record)) {
                mRecordInfo = record;
                mIsActive = false;
            }
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View contentView = inflater.inflate(R.layout.record_sheet_layout, container, false);

        parseArguments();

        (mTitleText = contentView.findViewById(R.id.ab_txt)).setText("国标麻将计分器");

        contentView.findViewById(R.id.ab_l_btn).setOnClickListener(view ->
                requireActivity().getOnBackPressedDispatcher().onBackPressed());

        Context context = requireContext();

        mTimeText = contentView.findViewById(R.id.rsl_txt_time);

        // 画表格
        RelativeLayout rlSheet = contentView.findViewById(R.id.rsl_rl_sheet);
        DisplayMetrics metrics = getResources().getDisplayMetrics();
        final int colWidth = metrics.widthPixels / 6;

        // 标题38+时间12=50
        final int siblingHeight = getResources().getDimensionPixelSize(R.dimen.dp50)
                + Utils.getStatusBarHeight(context);
        final int maxSheetHeight = metrics.heightPixels - siblingHeight;

        // 选手姓名+开局座位+每圈座位+累计+名次+标准分 16+6=22
        final int colMaxHeight = maxSheetHeight / 22;
        final int colHeight = Math.min(colMaxHeight, colWidth * 4 / 10);
        final int textSize = colHeight * 95 / 100;

        final int sheetHeight = colHeight * 22 + 2;
        rlSheet.getLayoutParams().height = sheetHeight;

        // 是否有空间放四个按钮
        int dp38;
        if (!context.getSharedPreferences(Common.SHARED_PREF_NAME, Context.MODE_PRIVATE)
                .getBoolean(Common.KEY_FOLD_SHEET_BUTTON, false)
                && maxSheetHeight - sheetHeight >= (dp38 = context.getResources().getDimensionPixelSize(R.dimen.dp38))) {

            View rootView = contentView.findViewById(R.id.rsl_cl_root);
            ViewGroup.MarginLayoutParams mlp = (ViewGroup.MarginLayoutParams)rootView.getLayoutParams();
            mlp.topMargin = Math.min((maxSheetHeight - sheetHeight - dp38) / 2,
                    context.getResources().getDimensionPixelSize(R.dimen.dp10));

            rootView.setVisibility(View.VISIBLE);

            contentView.findViewById(R.id.rsl_btn_chase).setOnClickListener(view -> onChaseButton());
            contentView.findViewById(R.id.rsl_btn_clear).setOnClickListener(view -> onResetButton());
            contentView.findViewById(R.id.rsl_btn_history).setOnClickListener(view -> onHistoryButton());
            contentView.findViewById(R.id.rsl_btn_edit).setOnClickListener(view -> onEditButton());

            View rightButton = contentView.findViewById(R.id.ab_r_btn);
            mPopupWindow = Utils.createPopupMenu(context, rightButton, new String[]{
                    "更多设置", "使用说明"
            });
            rightButton.setOnClickListener(view -> mPopupWindow.show());
            mPopupWindow.setOnItemClickListener((adapterView, view1, position, id) -> {
                switch (position) {
                    case 0: onSettingButton(); break;
                    case 1: onInstructionButton(); break;
                    default: break;
                }
                mPopupWindow.dismiss();
            });
        } else {
            ViewGroup.MarginLayoutParams mlp = (ViewGroup.MarginLayoutParams)rlSheet.getLayoutParams();
            mlp.topMargin = Math.min((maxSheetHeight - sheetHeight) / 2,
                    context.getResources().getDimensionPixelSize(R.dimen.dp10));

            contentView.findViewById(R.id.rsl_cl_root).setVisibility(View.GONE);

            View rightButton = contentView.findViewById(R.id.ab_r_btn);
            mPopupWindow = Utils.createPopupMenu(context, rightButton, new String[]{
                    "历史记录", "清空表格", "追分策略", "编辑信息", "更多设置", "使用说明"
            });
            rightButton.setOnClickListener(view -> mPopupWindow.show());
            mPopupWindow.setOnItemClickListener((adapterView, view1, position, id) -> {
                switch (position) {
                    case 0: onHistoryButton(); break;
                    case 1: onResetButton(); break;
                    case 2: onChaseButton(); break;
                    case 3: onEditButton(); break;
                    case 4: onSettingButton(); break;
                    case 5: onInstructionButton(); break;
                    default: break;
                }
                mPopupWindow.dismiss();
            });
        }

        final int textColor1 = ContextCompat.getColor(context, R.color.text_1);
        final int textColor2 = ContextCompat.getColor(context, R.color.text_3);

        TextView[] temp = new TextView[6];

        // 最后一格特殊：由于宽度不一定被6整除，整数除法会取整，剩余的都归最后一格
        final int remainWidth = metrics.widthPixels - colWidth * 5;

        // 说明：分隔线2px
        // 横线属于格子上端，所以内部文字/按钮需要+2
        // 竖线属于格子右端，除最后一格外，其他格内部文字宽度需要-2

        // 第1行：选手姓名
        for (int i = 0; i < 5; ++i) {
            TextView textView = createTextView(textSize, colWidth * i, 2, colWidth - 2, colHeight - 2);
            textView.setTextColor(Common.COLOR_ORANGE);
            rlSheet.addView(textView);
            temp[i] = textView;
        }
        temp[0].setText("选手姓名");
        System.arraycopy(temp, 1, mNameTexts, 0, 4);

        // 开始按钮
        mStartButtonView = createButtonView(textSize, colWidth * 5, 2, remainWidth, colHeight - 2,
                "开始", view1 -> onStartButton());
        rlSheet.addView(mStartButtonView);

        // 结束按钮
        mFinishButtonView = createButtonView(textSize, colWidth * 5, colHeight * 21 + 2, remainWidth, colHeight - 2,
                "强制结束", view1 -> onFinishButton());
        rlSheet.addView(mFinishButtonView);
        mFinishButtonView.setVisibility(View.GONE);

        // 第2行：开局座位
        int yPos = colHeight;
        for (int i = 0; i < 6; ++i) {
            TextView textView = createTextView(textSize, colWidth * i, yPos + 2, colWidth - 2, colHeight - 2);
            textView.setTextColor(textColor1);
            rlSheet.addView(textView);
            temp[i] = textView;
        }
        temp[0].setText("开局座位");
        for (int i = 0; i < 4; ++i) {
            temp[i + 1].setText(Mahjong.WIND_TEXT[i]);
        }
        temp[5].setTextColor(textColor2);
        temp[5].getLayoutParams().width = remainWidth;
        mModeText = temp[5];

        // 第3行：每圈座位、校验
        yPos = colHeight * 2;
        for (int i = 0; i < 6; ++i) {
            TextView textView = createTextView(textSize, colWidth * i, yPos + 2, colWidth - 2, colHeight - 2);
            textView.setTextColor(textColor1);
            rlSheet.addView(textView);
            temp[i] = textView;
        }
        temp[0].setText("每圈座位");
        temp[1].setText("东南北西");
        temp[2].setText("南东西北");
        temp[3].setText("西北东南");
        temp[4].setText("北西南东");
        temp[5].setText("校验");
        temp[5].setTextColor(textColor2);
        temp[5].getLayoutParams().width = remainWidth;

        // 第4行：累计、校验值
        yPos = colHeight * 3;
        for (int i = 0; i < 6; ++i) {
            TextView textView = createTextView(textSize, colWidth * i, yPos + 2, colWidth - 2, colHeight - 2);
            textView.setTextColor(Common.COLOR_ORANGE);
            rlSheet.addView(textView);
            temp[i] = textView;
        }
        temp[0].setText("累计");
        System.arraycopy(temp, 1, mTotalTexts, 0, 5);
        temp[1].setText("+0");
        temp[2].setText("+0");
        temp[3].setText("+0");
        temp[4].setText("+0");
        temp[5].setText("0");
        temp[5].setTextColor(textColor2);
        temp[5].getLayoutParams().width = remainWidth;

        // 第5~20行：东风东~北风北
        for (int k = 0; k < 16; ++k) {
            yPos = colHeight * (k + 4);
            for (int i = 0; i < 6; ++i) {
                TextView textView = createTextView(textSize, colWidth * i, yPos + 2, colWidth - 2, colHeight - 2);
                textView.setTextColor(textColor2);
                rlSheet.addView(textView);
                textView.setVisibility(View.GONE);
                temp[i] = textView;
            }
            temp[0].setText(Mahjong.WIND_TEXT[k >> 2] + "风" + Mahjong.WIND_TEXT[k & 3]);
            temp[0].setVisibility(View.VISIBLE);
            System.arraycopy(temp, 1, mScoreTexts[k], 0, 4);
            mFanTexts[k] = temp[5];
            temp[5].getLayoutParams().width = remainWidth;

            // 计分按钮
            final int idx = k;
            View view = createButtonView(textSize, colWidth * 5, yPos + 2, remainWidth, colHeight - 2,
                    "计分", view1 -> onRecordButton(idx));
            rlSheet.addView(view);
            view.setVisibility(View.GONE);
            mRecordButtonViews[k] = view;

            mFanTexts[k].setOnClickListener(view1 -> onFanText(idx));
        }

        // 第21行：名次
        yPos = colHeight * 20;
        for (int i = 0; i < 5; ++i) {
            TextView textView = createTextView(textSize, colWidth * i, yPos + 2, colWidth - 2, colHeight - 2);
            textView.setTextColor(Common.COLOR_ORANGE);
            rlSheet.addView(textView);
            temp[i] = textView;
        }
        temp[0].setText("名次");
        System.arraycopy(temp, 1, mRankTexts, 0, 4);
        temp[1].setVisibility(View.GONE);
        temp[2].setVisibility(View.GONE);
        temp[3].setVisibility(View.GONE);
        temp[4].setVisibility(View.GONE);

        // 第22行：标准分
        yPos = colHeight * 21;
        for (int i = 0; i < 5; ++i) {
            TextView textView = createTextView(textSize, colWidth * i, yPos + 2, colWidth - 2, colHeight - 2);
            textView.setTextColor(Common.COLOR_ORANGE);
            rlSheet.addView(textView);
            temp[i] = textView;
        }
        temp[0].setText("标准分");
        System.arraycopy(temp, 1, mNormTexts, 0, 4);
        temp[1].setVisibility(View.GONE);
        temp[2].setVisibility(View.GONE);
        temp[3].setVisibility(View.GONE);
        temp[4].setVisibility(View.GONE);

        // 横线需要多1条
        for (int i = 0; i < 23; ++i) {
            View hzLine = new View(context);
            RelativeLayout.LayoutParams rlp = new RelativeLayout.LayoutParams(metrics.widthPixels, 2);
            rlp.topMargin = i * colHeight;
            hzLine.setLayoutParams(rlp);
            hzLine.setBackgroundColor(textColor1);
            rlSheet.addView(hzLine);
        }

        // 竖线少一条
        for (int k = 0; k < 5; ++k) {
            View vtLine = new View(context);
            RelativeLayout.LayoutParams rlp = new RelativeLayout.LayoutParams(2, colHeight * 22);
            rlp.leftMargin = (k + 1) * colWidth - 2;
            vtLine.setLayoutParams(rlp);
            vtLine.setBackgroundColor(textColor1);
            rlSheet.addView(vtLine);
        }

        SharedPreferences sharedPreferences = context.getSharedPreferences(Common.SHARED_PREF_NAME, Context.MODE_PRIVATE);
        mSeatOrder = sharedPreferences.getInt(Common.KEY_SEAT_ORDER, 0);
        mSingleMode = !sharedPreferences.getBoolean(Common.KEY_TOTAL_MODE, false);
        mMorePayment = sharedPreferences.getBoolean(Common.KEY_MORE_PAYMENT, false);
        mHeroIndex = sharedPreferences.getInt(Common.KEY_HERO_INDEX, 0);

        if (mIsActive) {
            String str = Utils.getStringFromFile(context, sFilePath, sFileName);
            if (!str.isEmpty() && RecordInfo.parseRecord(str, mRecordInfo)) {
                recover();
            } else {
                startRefreshCurrentTimeTask();
            }
        } else {
            recover();
        }

        return contentView;
    }

    private TextView createTextView(int textSize, int x, int y, int width, int height) {
        TextView textView = new AppCompatTextView(requireContext());
        TextViewCompat.setAutoSizeTextTypeUniformWithConfiguration(textView,
                10, textSize, 2, TypedValue.COMPLEX_UNIT_PX);
        textView.setGravity(Gravity.CENTER);
        textView.setPadding(5, 5, 5, 5);
        RelativeLayout.LayoutParams rlp = new RelativeLayout.LayoutParams(width, height);
        rlp.leftMargin = x;
        rlp.topMargin = y;
        textView.setLayoutParams(rlp);
        return textView;
    }

    // NOTE: Button上的文本启用AutoSize需要API26，而当前为API24（本人只有这台机）
    // 变通方案是在同样位置使用一个AppCompatTextView，但无论是Button还是ImageButton，均不支持添加子View
    // 所以只能将TextView添加到与Button同样的父结点上
    // 而Button的渲染优先级高，即使TextView在后面添加，也是会被覆盖，调用bringToFront也无效果
    // 所以这里改用ImageButton
    private View createButtonView(int textSize, int x, int y, int width, int height, String face, View.OnClickListener listener) {
        Context context = requireContext();
        RelativeLayout rl = new RelativeLayout(context);
        RelativeLayout.LayoutParams rlp = new RelativeLayout.LayoutParams(width, height);
        rlp.leftMargin = x;
        rlp.topMargin = y;
        rl.setLayoutParams(rlp);

        ImageButton button = new ImageButton(context);
        button.setBackgroundResource(R.drawable.btn_square);
        button.setLayoutParams(new RelativeLayout.LayoutParams(width, height));
        button.setOnClickListener(listener);
        rl.addView(button);

        TextView textView = new AppCompatTextView(context);
        TextViewCompat.setAutoSizeTextTypeUniformWithConfiguration(textView,
                8, textSize - 4, 2, TypedValue.COMPLEX_UNIT_PX);
        textView.setGravity(Gravity.CENTER);
        textView.setPadding(8, 8, 8, 8);
        textView.setTextColor(Color.WHITE);
        textView.setText(face);
        textView.setLayoutParams(new RelativeLayout.LayoutParams(width, height));
        rl.addView(textView);

        return rl;
    }

    private void writeRecordToFile() {
        if (mIsActive) {
            String str = RecordInfo.recordToString(mRecordInfo);
            if (str != null && !str.isEmpty()) {
                Utils.saveStringToFile(requireContext(), sFilePath, sFileName, str);
            }
        }
    }

    private void onHistoryButton() {
        // 如果是查看历史记录时，再点击只需要回退即可
        if (mIsActive) {
            RecordHistoryFragment fragment = new RecordHistoryFragment();
            requireActivity().getSupportFragmentManager().beginTransaction()
                    .replace(R.id.cml_fl_root, fragment)
                    .addToBackStack(null)
                    .commit();
        } else {
            requireActivity().getSupportFragmentManager().popBackStack();
        }
    }

    private void onResetButton() {
        if (!mIsActive) {
            new CommonTextDialog(requireContext(),
                    "提示", "查看历史记录时，不能清空表格").show();
            return;
        }

        // 未开始或已结束时，随便清空
        if (mRecordInfo.period == 0 || mRecordInfo.period == 16) {
            reset();
            return;
        }

        new RecordResetDialog(requireContext(), save -> {
            if (save) {
                while (mRecordInfo.period < 16) {
                    mRecordInfo.details[mRecordInfo.period++].timeout = true;
                }
                RecordHistoryFragment.saveRecord(requireContext(), mRecordInfo);
            }
            reset();
        }).show();
    }

    private void onChaseButton() {
        if (!mIsActive) {
            new CommonTextDialog(requireContext(),
                    "提示", "查看历史记录时，无法使用追分策略").show();
            return;
        }

        // 比赛未开始时，直接显示更多追分界面
        if (mRecordInfo.start_time == 0 || mRecordInfo.period >= 16) {
            new RecordChase2Dialog(requireContext(), mRecordInfo.mode).show();
        } else {
            new RecordChaseDialog(requireContext(), mRecordInfo.mode,
                    mRecordInfo.names, mTotalScores).show();
        }
    }

    private void onEditButton() {
        if (mRecordInfo.start_time == 0) {
            editStartInfo();
            return;
        }

        new CommonConfirmDialog(requireContext(), "提示",
                mRecordInfo.period < 16 ? "对局已经开始，是否要修改对局信息？" : "对局已经结束，是否要修改对局信息？",
                "是", "否", this::editStartInfo).show();
    }

    private void onSettingButton() {
        new RecordSettingDialog(requireContext(),
                mSingleMode ? 0 : 1, mSeatOrder, (mode, order) -> {
            boolean prevMode = mSingleMode;
            mSingleMode = mode;
            mSeatOrder = order;

            requireContext().getSharedPreferences(Common.SHARED_PREF_NAME, Context.MODE_PRIVATE)
                    .edit()
                    .putInt(Common.KEY_SEAT_ORDER, mSeatOrder)
                    .putBoolean(Common.KEY_TOTAL_MODE, !mSingleMode)
                    .apply();

            if (prevMode != mSingleMode && mRecordInfo.start_time != 0) {
                refreshScores();
            }
        }).show();
    }

    private void onInstructionButton() {
        new CommonTextDialog(requireContext(),
                "使用说明", getString(R.string.rs_instruction)).show();
    }

    private void onStartButton() {
        // 如果名字有空，则弹出编辑对局信息
        for (int i = 0; i < 4; ++i) {
            if (mRecordInfo.names[i].isEmpty()) {
                editStartInfo();
                return;
            }
        }

        for (int i = 0; i < 4; ++i) {
            mTotalScores[i] = 0;
            mNameTexts[i].setText(mRecordInfo.names[i]);
            mTotalTexts[i].setText("+0");
            mRankTexts[i].setVisibility(View.GONE);
            mNormTexts[i].setVisibility(View.GONE);
        }
        mTotalTexts[4].setText("0");

        for (int k = 0; k < 16; ++k) {
            for (int i = 0; i < 4; ++i) {
                mScoreTexts[k][i].setVisibility(View.GONE);
            }
            mFanTexts[k].setVisibility(View.GONE);
            mRecordButtonViews[k].setVisibility(View.GONE);
        }

        mRecordButtonViews[0].setVisibility(View.VISIBLE);
        mStartButtonView.setVisibility(View.GONE);

        mModeText.setText(mMorePayment || mRecordInfo.mode != RecordInfo.MODE_STANDARD ?
                RecordInfo.MODE_NAME_TEXT[mRecordInfo.mode] : "");

        mRecordInfo.start_time = System.currentTimeMillis();
        startRefreshDurationTask();

        writeRecordToFile();
    }

    private void onFinishButton() {
        new CommonConfirmDialog(requireContext(), "警告",
                "强制结束会将未打完盘数标记为「超时」",
                "结束", "取消", this::forceFinish).show();
    }

    private void forceFinish() {
        mFinishButtonView.setVisibility(View.GONE);

        for (int i = mRecordInfo.period; i < 16; ++i) {
            mRecordInfo.details[i].timeout = true;
            mRecordButtonViews[i].setVisibility(View.GONE);
            mFanTexts[i].setText("超时");
            mFanTexts[i].setVisibility(View.VISIBLE);
        }
        mRecordInfo.period = 16;

        refreshScores();

        mRecordInfo.finish_time = System.currentTimeMillis();
        refreshFinishTime();
        RecordHistoryFragment.saveRecord(requireContext(), mRecordInfo);

        writeRecordToFile();
    }

    private void editStartInfo() {
        new RecordStartDialog(requireContext(), sPrevName, sPrevTitle, mRecordInfo.mode,
                mMorePayment, mSeatOrder == 3, mHeroIndex, mRecordInfo.start_time != 0,
                this::onSubmitStartInfo).show();
    }

    private boolean onSubmitStartInfo(String[] names, String title, int mode, int heroIndex) {
        // 修改的限制更多
        Context context = requireContext();
        if (mRecordInfo.start_time != 0) {
            if (mRecordInfo.mode != mode) {
                Utils.showToastLong(context, "对局开始后不允许更改授受制");
                return false;
            }

            // 检查空输入
            for (int i = 0; i < 4; ++i) {
                if (names[i].isEmpty()) {
                    Utils.showToastLong(context, "对局开始后不允许清空选手姓名");
                    return false;
                }
            }
        }

        // 检查重名
        for (int i = 0; i < 4; ++i) {
            if (!names[i].isEmpty()) {
                for (int k = i + 1; k < 4; ++k) {
                    if (!names[k].isEmpty() && names[k].equalsIgnoreCase(names[i])) {
                        Utils.showToastLong(context, "选手姓名不能相同");
                        return false;
                    }
                }
            }
        }

        for (int i = 0; i < 4; ++i) {
            mNameTexts[i].setText(names[i]);
            mRecordInfo.names[i] = names[i];
            mNameTexts[i].setVisibility(View.VISIBLE);
            sPrevName[i] = names[i];
        }
        mTitleText.setText(title.isEmpty() ? "国标麻将计分器" : title);
        mRecordInfo.title = title;
        mRecordInfo.mode = mMorePayment ? mode : RecordInfo.MODE_STANDARD;
        sPrevTitle = title;
        if (mHeroIndex != heroIndex) {
            mHeroIndex = heroIndex;
            requireContext().getSharedPreferences(Common.SHARED_PREF_NAME, Context.MODE_PRIVATE)
                    .edit()
                    .putInt(Common.KEY_HERO_INDEX, heroIndex)
                    .apply();
        }

        if (mRecordInfo.period >= 16) {
            RecordHistoryFragment.saveRecord(requireContext(), mRecordInfo);
        }

        writeRecordToFile();

        return true;
    }

    private void onRecordButton(int period) {
        new RecordDetailDialog(requireContext(), mSeatOrder, mHeroIndex, mRecordInfo.mode,
                period, mRecordInfo.names, null, this::onDetailSubmit).show();
    }

    private static void appendFanText(StringBuilder str, long fan_major, int fan_minor2, long fan_minor1) {
        final int prevLength = str.length();
        for (int n = Mahjong.BIG_FOUR_WINDS; n < Mahjong.DRAGON_PUNG; ++n) {
            if ((fan_major & (1L << (Mahjong.LAST_TILE - n))) != 0) {
                str.append("「");
                str.append(Mahjong.FAN_NAME[n]);
                str.append("」");
            }
        }
        for (int n = 0; n < 10; ++n) {
            int cnt = (fan_minor2 >> (n << 1)) & 0x3;
            if (cnt > 0) {
                str.append("「");
                str.append(Mahjong.FAN_NAME[Mahjong.DRAGON_PUNG + n]);
                if (cnt > 1) {
                    str.append("\u00d7");
                    str.append(cnt);
                }
                str.append("」");
            }
        }
        for (int n = 0; n < 13; ++n) {
            int cnt = (int)((fan_minor1 >> (n << 2)) & 0xf);
            if (cnt > 0) {
                str.append("「");
                str.append(Mahjong.FAN_NAME[Mahjong.PURE_DOUBLE_CHOW + n]);
                if (cnt > 1) {
                    str.append("\u00d7");
                    str.append(cnt);
                }
                str.append("」");
            }
        }

        if (str.length() != prevLength) {
            str.append("等");
        }
    }

    private void onFanText(int period) {
        final RecordInfo.Detail detail = mRecordInfo.details[period];

        StringBuilder str = new StringBuilder();
        if (detail.fan != 0) {
            int winFlag = detail.win_flag, claimFlag = detail.claim_flag;
            int winIndex = -1, claimIndex = -1;
            for (int i = 0; i < 4; ++i) {
                if ((winFlag & (1 << i)) != 0) {
                    winIndex = i;
                }
                if ((claimFlag & (1 << i)) != 0) {
                    claimIndex = i;
                }
            }
            if (winIndex != -1 && claimIndex != -1) {
                str.append("「").append(mRecordInfo.names[winIndex]).append("」");
                if (winIndex != claimIndex) {
                    str.append("和");
                } else {
                    str.append("自摸");
                }
                appendFanText(str, detail.fan_major, detail.fan_minor2, detail.fan_minor1);
                str.append(detail.fan);
                str.append("番");
                if (winIndex != claimIndex) {
                    str.append("，「").append(mRecordInfo.names[claimIndex]).append("」点炮。");
                } else {
                    str.append("。");
                }
            } else {
                str.append("数据错误");
            }
        } else {
            str.append(detail.timeout ? "超时" : "荒庄");
        }
        str.append("\n\n是否需要修改这盘的记录？");

        new CommonConfirmDialog(requireContext(),
                Mahjong.WIND_TEXT[period >> 2] + "风" + Mahjong.WIND_TEXT[period & 3] + "详情",
                str.toString(),
                "确定", "取消", () -> modifyDetail(period)).show();
    }

    private void modifyDetail(int period) {
        new RecordDetailDialog(requireContext(), mSeatOrder, mHeroIndex, mRecordInfo.mode,
                period, mRecordInfo.names, mRecordInfo.details[period], this::onDetailSubmit).show();
    }

    private void onDetailSubmit(int period, RecordInfo.Detail detail) {
        // TODO: 超时问题，旧版同样也未处理
        mRecordInfo.details[period] = detail;
        refreshPeriod(period);
    }

    @SuppressLint("DefaultLocale")
    private void refreshPeriod(int period) {
        // 加总分
        int[] totalScores = {0, 0, 0, 0};
        for (int i = 0; i < period; ++i) {
            addUpScores(i, totalScores);
        }

        mRecordButtonViews[period].setVisibility(View.GONE);
        setFanText(period);

        int currentIdx = mRecordInfo.period;
        boolean isModify = (period != currentIdx);

        // 填充
        if (mSingleMode) {
            fillScoresForSingleMode(period, totalScores);

            // 单盘模式直接统计之后的行
            for (int i = period + 1; i < currentIdx; ++i) {
                addUpScores(i, totalScores);
            }
        } else {
            fillScoresForTotalMode(period, totalScores);

            // 累计模式下还需要修改随后的所有行
            for (int i = period + 1; i < currentIdx; ++i) {
                fillScoresForTotalMode(i, totalScores);
            }
        }

        // 更新总分
        for (int i = 0; i < 4; ++i) {
            mTotalScores[i] = totalScores[i];
            mTotalTexts[i].setText(String.format("%+d", totalScores[i]));
        }
        mTotalTexts[4].setText(
                String.valueOf(totalScores[0] + totalScores[1] + totalScores[2] + totalScores[3]));

        // 更新名次和标准分
        refreshRank(totalScores);

        if (isModify) {
            if (mRecordInfo.finish_time != 0) {
                RecordHistoryFragment.saveRecord(requireContext(), mRecordInfo);
            }
        } else {
            if (currentIdx == 0) {  // 东风东过后，显示强制结束按钮
                mFinishButtonView.setVisibility(View.VISIBLE);
            }

            // 如果不是北风北，则显示下一行的计分按钮，否则一局结束，并增加新的历史记录
            if (++mRecordInfo.period < 16) {
                mRecordButtonViews[currentIdx + 1].setVisibility(View.VISIBLE);
            } else {
                mFinishButtonView.setVisibility(View.GONE);

                mRecordInfo.finish_time = System.currentTimeMillis();
                refreshFinishTime();
                RecordHistoryFragment.saveRecord(requireContext(), mRecordInfo);
            }
        }

        writeRecordToFile();
    }

    // 累计一盘的分
    private void addUpScores(int period, int[] totalScores) {
        final RecordInfo.Detail detail = mRecordInfo.details[period];
        int[] scores = RecordInfo.translateToScores(mRecordInfo.mode, detail.fan,
                detail.win_flag, detail.claim_flag, detail.penalty);
        for (int k = 0; k < 4; ++k) {
            totalScores[k] += scores[k];
        }
    }

    @SuppressLint("DefaultLocale")
    private void fillScoresForSingleMode(int period, int[] totalScores) {
        final RecordInfo.Detail detail = mRecordInfo.details[period];
        final TextView[] scoreTexts = mScoreTexts[period];
        int[] scores = RecordInfo.translateToScores(mRecordInfo.mode, detail.fan,
                detail.win_flag, detail.claim_flag, detail.penalty);

        // 填入这一盘四位选手的得分
        for (int i = 0; i < 4; ++i) {
            totalScores[i] += scores[i];  // 更新总分
            scoreTexts[i].setText(String.format("%+d", scores[i]));
            scoreTexts[i].setVisibility(View.VISIBLE);
        }

        // 使用不同颜色
        RecordDetailDialog.setScoreTextColor(scoreTexts, scores,
                detail.win_flag, detail.claim_flag, detail.penalty);
    }

    @SuppressLint("DefaultLocale")
    private void fillScoresForTotalMode(int period, int[] totalScores) {
        final RecordInfo.Detail detail = mRecordInfo.details[period];
        final TextView[] scoreTexts = mScoreTexts[period];
        int[] scores = RecordInfo.translateToScores(mRecordInfo.mode, detail.fan,
                detail.win_flag, detail.claim_flag, detail.penalty);

        // 填入这一盘之后四位选手的总分
        for (int i = 0; i < 4; ++i) {
            totalScores[i] += scores[i];  // 更新总分
            scoreTexts[i].setText(String.format("%+d", totalScores[i]));
            scoreTexts[i].setVisibility(View.VISIBLE);
        }

        // 使用不同颜色
        RecordDetailDialog.setScoreTextColor(scoreTexts, scores,
                detail.win_flag, detail.claim_flag, detail.penalty);
    }

    private void refreshScores() {
        // 逐行填入数据
        int[] totalScores = {0, 0, 0, 0};
        if (mSingleMode) {
            for (int i = 0, cnt = mRecordInfo.period; i < cnt; ++i) {
                fillScoresForSingleMode(i, totalScores);
            }
        } else {
            for (int i = 0, cnt = mRecordInfo.period; i < cnt; ++i) {
                fillScoresForTotalMode(i, totalScores);
            }
        }
    }

    private static final String[] RANK_TEXT = {"一", "二", "三", "四"};

    private void refreshRank(final int[] totalScores) {
        int[] rank = RecordInfo.calcRankFromScore(totalScores);
        int[] norm = RecordInfo.calcNorm12FromRank(rank);

        for (int i = 0; i < 4; ++i) {
            mRankTexts[i].setVisibility(View.VISIBLE);
            mRankTexts[i].setText(RANK_TEXT[rank[i]]);
            mNormTexts[i].setVisibility(View.VISIBLE);
            mNormTexts[i].setText(RecordInfo.normStringFrom12(norm[i]));
        }
    }

    private static void appendTime(StringBuilder str, long time) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(time);

        str.append(calendar.get(Calendar.YEAR)).append("年");
        str.append(calendar.get(Calendar.MONTH) + 1).append("月");
        str.append(calendar.get(Calendar.DAY_OF_MONTH)).append("日");

        int h = calendar.get(Calendar.HOUR_OF_DAY);
        if (h < 10) str.append('0');
        str.append(h).append(':');

        int m = calendar.get(Calendar.MINUTE);
        if (m < 10) str.append('0');
        str.append(m);
    }

    private void refreshCurrentTime() {
        long now = System.currentTimeMillis();
        if (now - mLastRefreshTime < 60000) {
            return;
        }
        mLastRefreshTime = now - now % 60000;

        StringBuilder str = new StringBuilder();
        str.append("当前时间：");
        appendTime(str, now);

        mTimeText.setText(str.toString());
    }

    private void refreshDuration() {
        long now = System.currentTimeMillis();
        if (now - mLastRefreshTime < 60000) {
            return;
        }
        mLastRefreshTime = now;

        StringBuilder str = new StringBuilder();
        str.append("开始时间：");
        appendTime(str, mRecordInfo.start_time);

        long diff = now - mRecordInfo.start_time;
        long min_rem = diff % 60000;
        long min = (diff - min_rem) / 60000;
        str.append("（已用时：").append(min).append("\u02b9）");

        mTimeText.setText(str.toString());
    }

    private void startRefreshDurationTask() {
        mLastRefreshTime = 0;
        if (mScheduledExecutorService != null) {
            mScheduledExecutorService.shutdownNow();
        }
        mScheduledExecutorService = Executors.newScheduledThreadPool(1);
        mScheduledExecutorService.scheduleAtFixedRate(
                () -> requireActivity().runOnUiThread(this::refreshDuration), 0, 1, TimeUnit.SECONDS);
    }

    private void startRefreshCurrentTimeTask() {
        mLastRefreshTime = 0;
        if (mScheduledExecutorService != null) {
            mScheduledExecutorService.shutdownNow();
        }
        mScheduledExecutorService = Executors.newScheduledThreadPool(1);
        mScheduledExecutorService.scheduleAtFixedRate(
                () -> requireActivity().runOnUiThread(this::refreshCurrentTime), 0, 1, TimeUnit.SECONDS);
    }

    private void refreshFinishTime() {
        if (mScheduledExecutorService != null) {
            mScheduledExecutorService.shutdownNow();
            mScheduledExecutorService = null;
        }

        StringBuilder str = RecordHistoryFragment.buildTimeText(
                mRecordInfo.start_time, mRecordInfo.finish_time);
        str.insert(0, "起止时间：");

        mTimeText.setText(str.toString());
    }

    private static final String[] FAN2_SHORT_NAME = {
            "箭刻", "圈风", "门风", "门清", "平和", "四归", "双同", "双暗", "暗杠", "断幺"
    };

    private static String getShortFanText(RecordInfo.Detail detail) {
        if (detail.fan == 0) {
            return detail.timeout ? "超时" : "荒庄";
        }

        long fan_major = detail.fan_major;
        if (fan_major != 0) {
            if ((fan_major & (fan_major - 1)) != 0) {  // 这是快速判断2的幂算法，当fanBits不是2的幂时，说明有多个番
                // 选取标记的最大的两个番种显示出来
                int fan0 = 0, fan1 = 0;
                for (int n = Mahjong.BIG_FOUR_WINDS; n < Mahjong.DRAGON_PUNG; ++n) {
                    if ((fan_major & (1L << (Mahjong.LAST_TILE - n))) != 0) {
                        fan0 = n;
                        break;
                    }
                }
                for (int n = fan0 + 1; n < Mahjong.DRAGON_PUNG; ++n) {
                    if ((fan_major & (1L << (Mahjong.LAST_TILE - n))) != 0) {
                        fan1 = n;
                        break;
                    }
                }

                String res = Mahjong.COMBINE_TWO_FAN_NAME.get(fan0 << 8 | fan1);
                if (res != null) {
                    return res;
                } else {
                    return Mahjong.FAN_NAME[fan0];
                }

            } else {
                for (int n = Mahjong.BIG_FOUR_WINDS; n < Mahjong.DRAGON_PUNG; ++n) {
                    if (((1L << (Mahjong.LAST_TILE - n)) & fan_major) != 0) {
                        return Mahjong.FAN_NAME[n];
                    }
                }
            }
        }

        int fan_minor2 = detail.fan_minor2;
        if (fan_minor2 != 0) {
            int cnt = 0, category = 0;  // 个数、种数
            int[] fan2Table = new int[10];  // 各个2番个数
            for (int i = 0; i < 10; ++i) {
                int n = (fan_minor2 >> (i << 1)) & 0x3;
                if (n != 0) {
                    fan2Table[i] = n;
                    cnt += n;
                    ++category;
                }
            }

            if (cnt == 3) {  // 3个：门断平特殊对待
                if (fan2Table[Mahjong.CONCEALED_HAND - Mahjong.DRAGON_PUNG] == 1
                        && fan2Table[Mahjong.ALL_SIMPLES - Mahjong.DRAGON_PUNG] == 1
                        && fan2Table[Mahjong.ALL_CHOWS - Mahjong.DRAGON_PUNG] == 1) {
                    return "门断平";
                } else {
                    return "2\u00d73";
                }
            } else if (cnt == 2) {  // 2个：不同的组合显示；相同显示×2
                if (category == 2) {
                    StringBuilder str = new StringBuilder();
                    int n = 0;
                    for (int i = 0; i < 10 && n < 2; ++i) {
                        if (fan2Table[i] != 0) {
                            ++n;
                            str.append(FAN2_SHORT_NAME[i]);
                        }
                    }
                    return str.toString();
                } else {
                    for (int i = 0; i < 10; ++i) {
                        if (fan2Table[i] != 0) {
                            return FAN2_SHORT_NAME[i] + "\u00d72";
                        }
                    }
                }
            } else if (cnt == 1) {  // 1个
                for (int i = 0; i < 10; ++i) {
                    if (fan2Table[i] != 0) {
                        return FAN2_SHORT_NAME[i] + "凑番";
                    }
                }
            } else {  // 超过3个显示为：2×N
                return "2\u00d7" + cnt;
            }
        }

        return detail.fan_minor1 == 0 ? "未记录番种" : "其他凑番";
    }

    private void setFanText(int period) {
        final RecordInfo.Detail detail = mRecordInfo.details[period];
        mFanTexts[period].setText(getShortFanText(detail));
        mFanTexts[period].setVisibility(View.VISIBLE);
    }

    private void reset() {
        // 如果选手姓名不为空，则保存上次对局的姓名
        if (!mRecordInfo.names[0].isEmpty() && !mRecordInfo.names[1].isEmpty()
                && !mRecordInfo.names[2].isEmpty() && !mRecordInfo.names[3].isEmpty()) {
            System.arraycopy(mRecordInfo.names, 0, sPrevName, 0, 4);
        }

        // 保存上次对局标题
        sPrevTitle = mRecordInfo.title;

        // 清空
        mRecordInfo.title = null;
        mRecordInfo.mode = RecordInfo.MODE_STANDARD;
        mRecordInfo.period = 0;
        mRecordInfo.start_time = 0;
        mRecordInfo.finish_time = 0;
        for (int i = 0; i < 4; ++i) {
            mRecordInfo.names[i] = "";
        }
        for (int i = 0; i < 16; ++i) {
            RecordInfo.Detail detail = mRecordInfo.details[i];
            detail.win_flag = 0;
            detail.claim_flag = 0;
            detail.fan = 0;
            Arrays.fill(detail.penalty, 0);
            detail.timeout = false;
            detail.fan_major = 0;
            detail.fan_minor1 = 0;
            detail.fan_minor2 = 0;
        }

        writeRecordToFile();

        mTitleText.setText("国标麻将计分器");
        mModeText.setText("");

        for (int i = 0; i < 4; ++i) {
            mTotalScores[i] = 0;
            mNameTexts[i].setVisibility(View.GONE);
            mTotalTexts[i].setText("+0");
            mRankTexts[i].setVisibility(View.GONE);
            mNormTexts[i].setVisibility(View.GONE);
        }
        mTotalTexts[4].setText("0");

        for (int k = 0; k < 16; ++k) {
            for (int i = 0; i < 4; ++i) {
                mScoreTexts[k][i].setVisibility(View.GONE);
            }
            mFanTexts[k].setVisibility(View.GONE);
            mRecordButtonViews[k].setVisibility(View.GONE);
        }

        mStartButtonView.setVisibility(View.VISIBLE);
        mFinishButtonView.setVisibility(View.GONE);
        startRefreshCurrentTimeTask();
    }

    @SuppressLint("DefaultLocale")
    private void recover() {
        String title = mRecordInfo.title;
        mTitleText.setText(title == null || title.isEmpty() ? "国标麻将计分器" : title);

        String[] names = mRecordInfo.names;
        for (int i = 0; i < 4; ++i) {
            mNameTexts[i].setText(names[i]);
            mNameTexts[i].setVisibility(View.VISIBLE);
            sPrevName[i] = names[i];
        }

        // 如果开始时间为0，说明未开始，清空其他数据
        if (mRecordInfo.start_time == 0) {
            mModeText.setText("");
            for (int i = 0; i < 16; ++i) {
                RecordInfo.Detail detail = mRecordInfo.details[i];
                detail.win_flag = 0;
                detail.claim_flag = 0;
                detail.fan = 0;
                Arrays.fill(detail.penalty, 0);
                detail.timeout = false;
                detail.fan_major = 0;
                detail.fan_minor1 = 0;
                detail.fan_minor2 = 0;
            }
            mRecordInfo.period = 0;
            mRecordInfo.finish_time = 0;
            startRefreshCurrentTimeTask();
            return;
        }

        mModeText.setText(mMorePayment || mRecordInfo.mode != RecordInfo.MODE_STANDARD ?
                        RecordInfo.MODE_NAME_TEXT[mRecordInfo.mode] : "");

        // 隐藏开始按钮
        mStartButtonView.setVisibility(View.GONE);

        int period = mRecordInfo.period;
        int[] totalScores = {0, 0, 0, 0};
        if (mSingleMode) {
            for (int i = 0; i < period; ++i) {
                fillScoresForSingleMode(i, totalScores);
                setFanText(i);
            }
        } else {
            for (int i = 0; i < period; ++i) {
                fillScoresForTotalMode(i, totalScores);
                setFanText(i);
            }
        }

        for (int k = period; k < 16; ++k) {
            for (int i = 0; i < 4; ++i) {
                mScoreTexts[k][i].setVisibility(View.GONE);
            }
            mFanTexts[k].setVisibility(View.GONE);
            mRecordButtonViews[k].setVisibility(View.GONE);
        }

        // 刷新总分和名次
        // 更新总分
        for (int i = 0; i < 4; ++i) {
            mTotalScores[i] = totalScores[i];
            mTotalTexts[i].setText(String.format("%+d", totalScores[i]));
        }
        mTotalTexts[4].setText(
                String.valueOf(totalScores[0] + totalScores[1] + totalScores[2] + totalScores[3]));

        // 更新名次和标准分
        if (period > 0) {
            refreshRank(totalScores);
        }

        // 如果不是北风北，则显示下一行的计分按钮
        if (period < 16) {
            if (period > 0) {  // 已经过了东风东，则显示强制结束按钮
                mFinishButtonView.setVisibility(View.VISIBLE);
            }
            mRecordButtonViews[period].setVisibility(View.VISIBLE);
            startRefreshDurationTask();
        } else {
            mFinishButtonView.setVisibility(View.GONE);
            refreshFinishTime();
        }
    }

}
