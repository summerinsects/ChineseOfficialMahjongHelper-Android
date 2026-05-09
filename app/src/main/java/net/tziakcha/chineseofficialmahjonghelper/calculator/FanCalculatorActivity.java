package net.tziakcha.chineseofficialmahjonghelper.calculator;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.RadioButton;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.core.content.ContextCompat;
import androidx.core.widget.TextViewCompat;

import net.tziakcha.chineseofficialmahjonghelper.Mahjong;
import net.tziakcha.chineseofficialmahjonghelper.R;
import net.tziakcha.chineseofficialmahjonghelper.Utils;
import net.tziakcha.chineseofficialmahjonghelper.widget.CommonTextDialog;
import net.tziakcha.chineseofficialmahjonghelper.widget.CommonWebFullScreenDialog;
import net.tziakcha.chineseofficialmahjonghelper.widget.InputTileDialog;
import net.tziakcha.chineseofficialmahjonghelper.widget.TilePickerLayout;

@SuppressLint("SetTextI18n")
public class FanCalculatorActivity extends AppCompatActivity {
    private TilePickerLayout mTilePickerLayout = null;
    private ScrollView mFanAreaView = null;
    private RadioButton mCLRadioButton = null;
    private RadioButton mSDRadioButton = null;
    private CheckBox mLTCheckBox = null;
    private CheckBox mORCheckBox = null;
    private CheckBox mRKCheckBox = null;
    private CheckBox mWLCheckBox = null;
    private final RadioButton[] mPWRadioButton = new RadioButton[4];
    private final RadioButton[] mSWRadioButton = new RadioButton[4];
    private int mFlower = 0;
    private int mPrevalentWind = 0;
    private int mSeatWind = 0;
    private boolean mHasKong = false;
    private int mWinTileDetail = 0;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        View contentView = View.inflate(this, R.layout.fan_calculator_layout, null);
        setContentView(contentView);

        ((TextView)contentView.findViewById(R.id.ab_txt)).setText("国标麻将算番器");

        contentView.findViewById(R.id.ab_l_btn).setOnClickListener(view ->
                getOnBackPressedDispatcher().onBackPressed());

        contentView.findViewById(R.id.ab_r_btn).setOnClickListener(view -> showInstruction());

        TilePickerLayout tpl = new TilePickerLayout(this);
        ((RelativeLayout)contentView.findViewById(R.id.fcl_rl_picker)).addView(tpl);
        tpl.setOnClearButtonListener(tpl0 -> mFanAreaView.removeAllViews());
        tpl.setOnHandChangedListener(new TilePickerLayout.OnHandChangedListener() {
            @Override
            public void onPacksChanged(TilePickerLayout tpl) {
                refreshByKong();
            }

            @Override
            public void onTileChanged(TilePickerLayout tpl) {
                refreshByWinTile();
            }
        });
        mTilePickerLayout = tpl;

        mFanAreaView = contentView.findViewById(R.id.fcl_sv_res);

        final int dp28 = getResources().getDimensionPixelSize(R.dimen.dp28);

        // 点和
        (mCLRadioButton = contentView.findViewById(R.id.fcl_rb_cl)).setOnCheckedChangeListener(
                (view, checked) -> { if (checked) refreshByCL(); });
        Utils.adaptCompoundButton(mCLRadioButton, dp28);
        // 自摸
        (mSDRadioButton = contentView.findViewById(R.id.fcl_rb_sd)).setOnCheckedChangeListener(
                (view, checked) -> { if (checked) refreshBySD(); });
        Utils.adaptCompoundButton(mSDRadioButton, dp28);
        // 绝张
        (mLTCheckBox = contentView.findViewById(R.id.fcl_cb_lt)).setOnCheckedChangeListener(
                (view, checked) -> onLTCheckBox(checked));
        Utils.adaptCompoundButton(mLTCheckBox, dp28);
        // 杠开
        mORCheckBox = contentView.findViewById(R.id.fcl_cb_or);
        Utils.adaptCompoundButton(mORCheckBox, dp28);
        // 抢杠
        (mRKCheckBox = contentView.findViewById(R.id.fcl_cb_rk)).setOnCheckedChangeListener(
                (view, checked) -> onRKCheckBox(checked));
        Utils.adaptCompoundButton(mRKCheckBox, dp28);
        // 海底
        (mWLCheckBox = contentView.findViewById(R.id.fcl_cb_wl)).setOnCheckedChangeListener(
                (view, checked) -> onWLCheckBox(checked));
        Utils.adaptCompoundButton(mWLCheckBox, dp28);

        // 圈风
        mPWRadioButton[0] = contentView.findViewById(R.id.fcl_rb_pwe);
        mPWRadioButton[1] = contentView.findViewById(R.id.fcl_rb_pws);
        mPWRadioButton[2] = contentView.findViewById(R.id.fcl_rb_pww);
        mPWRadioButton[3] = contentView.findViewById(R.id.fcl_rb_pwn);
        for (int i = 0; i < 4; ++i) {
            final int idx = i;
            mPWRadioButton[i].setOnCheckedChangeListener((view, checked) -> {
                if (checked) {
                    mPrevalentWind = idx;
                    for (int k = 0; k < 4; ++k) {
                        if (k != idx) {
                            mPWRadioButton[k].setChecked(false);
                        }
                    }
                }
            });
            Utils.adaptCompoundButton(mPWRadioButton[i], dp28);
        }

        // 门风
        mSWRadioButton[0] = contentView.findViewById(R.id.fcl_rb_swe);
        mSWRadioButton[1] = contentView.findViewById(R.id.fcl_rb_sws);
        mSWRadioButton[2] = contentView.findViewById(R.id.fcl_rb_sww);
        mSWRadioButton[3] = contentView.findViewById(R.id.fcl_rb_swn);
        for (int i = 0; i < 4; ++i) {
            final int idx = i;
            mSWRadioButton[i].setOnCheckedChangeListener((view, checked) -> {
                if (checked) {
                    mSeatWind = idx;
                    for (int k = 0; k < 4; ++k) {
                        if (k != idx) {
                            mSWRadioButton[k].setChecked(false);
                        }
                    }
                }
            });
            Utils.adaptCompoundButton(mSWRadioButton[i], dp28);
        }

        // 花牌
        contentView.findViewById(R.id.fcl_btn_flw).setOnClickListener(view -> {
            mFlower = (mFlower + 1) % 9;
            ((Button)view).setText("\ud83c\udf38 \u00d7 " + mFlower);
        });

        contentView.findViewById(R.id.fcl_btn_calc).setOnClickListener(view -> calculate());
        contentView.findViewById(R.id.fcl_btn_input).setOnClickListener(view -> showInputDialog());
        contentView.findViewById(R.id.fcl_btn_rule).setOnClickListener(view -> showRule());
    }

    private void calculate() {
        mFanAreaView.removeAllViews();

        int flw = mFlower;
        if (flw > 8) {
            Utils.showToastLong(this, "花牌数的范围为0~8");
            return;
        }

        Mahjong.HandTiles ht = mTilePickerLayout.getData();
        if (ht.wt == 0) {
            Utils.showToastLong(this, "牌张数错误");
            return;
        }

        int[] fan = new int[Mahjong.FAN_NAME.length];
        int cond = 0;
        if (mSDRadioButton.isChecked()) cond |= 1;
        if (mLTCheckBox.isChecked()) cond |= 2;
        if (mORCheckBox.isChecked()) cond |= 4;
        if (mRKCheckBox.isChecked()) cond |= 4;
        if (mWLCheckBox.isChecked()) cond |= 8;
        int res = Mahjong.calculateFan(ht.st, ht.fp, ht.wt, cond, mPrevalentWind, mSeatWind, flw, fan);
        if (res == Mahjong.ERROR_NOT_WIN) {
            Utils.showToastLong(this, "诈和");
            return;
        }
        if (res == Mahjong.ERROR_WRONG_TILES_COUNT) {
            Utils.showToastLong(this, "牌张数错误");
            return;
        }
        if (res == Mahjong.ERROR_TILE_MORE_THAN_4) {
            Utils.showToastLong(this, "同一种牌最多只能使用4枚");
            return;
        }

        RelativeLayout rl = createFanResultLayout(this, fan);
        rl.setGravity(Gravity.CENTER_VERTICAL);
        mFanAreaView.addView(rl);
        mFanAreaView.scrollTo(0, 0);
    }

    private RelativeLayout createFanResultLayout(Context context, final int[] fan) {
        DisplayMetrics metrics = context.getResources().getDisplayMetrics();
        final int dp5 = context.getResources().getDimensionPixelSize(R.dimen.dp5);
        final int lineHeight = context.getResources().getDimensionPixelSize(R.dimen.dp28);

        final int dp12 = context.getResources().getDimensionPixelSize(R.dimen.dp12);
        final int dp18 = context.getResources().getDimensionPixelSize(R.dimen.dp18);

        RelativeLayout root = new RelativeLayout(context);
        root.setLayoutParams(new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        //root.setBackgroundColor(Color.CYAN);  // 测试范围用

        final int textColor = ContextCompat.getColor(context, R.color.text_3);
        TextView textView;
        ViewGroup.MarginLayoutParams mlp;
        int value = 0;
        int cnt = 0;
        for (int i = 0, l = fan.length; i < l; ++i) {
            if (fan[i] == 0) continue;

            int f = Mahjong.FAN_VALUE_TABLE[i];
            int n = fan[i];
            value += f * n;

            StringBuilder str = new StringBuilder();
            str.append(Mahjong.FAN_NAME[i]);
            str.append(" ");
            str.append(f);
            str.append("番");
            if (n > 1) {
                str.append("\u00d7");
                str.append(n);
            }

            textView = new AppCompatTextView(context);
            TextViewCompat.setAutoSizeTextTypeUniformWithConfiguration(textView,
                    dp12, dp18, 2, TypedValue.COMPLEX_UNIT_PX);
            textView.setTextColor(textColor);
            textView.setText(str);
            textView.setPadding(dp5, 0, dp5, 0);
            mlp = new ViewGroup.MarginLayoutParams(metrics.widthPixels / 2, lineHeight);
            mlp.leftMargin = (cnt & 1) == 0 ? 0 : metrics.widthPixels / 2;
            mlp.topMargin = (cnt >> 1) * lineHeight;
            textView.setLayoutParams(mlp);
            root.addView(textView);
            //textView.setBackgroundColor(Color.GREEN);  // 测试范围用

            int finalI = i;
            textView.setOnClickListener(view -> showFanDefinition(finalI));

            TypedValue typedValue = new TypedValue();
            if (getTheme().resolveAttribute(android.R.attr.selectableItemBackground,
                    typedValue, true)) {
                textView.setBackgroundResource(typedValue.resourceId);
            }

            ++cnt;
        }

        textView = new TextView(context);
        textView.setTextSize(TypedValue.COMPLEX_UNIT_PX, dp18);
        textView.setTextColor(ContextCompat.getColor(context, R.color.text_1));
        textView.setText("总计：" + value + "番");
        textView.setPadding(dp5, 0, dp5, 0);
        mlp = new ViewGroup.MarginLayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        mlp.topMargin = ((cnt >> 1) + (cnt & 1)) * lineHeight;
        textView.setLayoutParams(mlp);
        root.addView(textView);

        textView = new TextView(context);
        textView.setTextSize(TypedValue.COMPLEX_UNIT_PX, dp12);
        textView.setTextColor(ContextCompat.getColor(context, R.color.theme_main));
        textView.setText("点击番种名可查看番种介绍");
        textView.setPadding(dp5, 0, dp5, 0);
        mlp = new ViewGroup.MarginLayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        mlp.topMargin = ((cnt >> 1) + (cnt & 1) + 1) * lineHeight;
        textView.setLayoutParams(mlp);
        root.addView(textView);

        return root;
    }

    // 副露有变化时调用，启用或禁用杠开选项
    private void refreshByKong() {
        mHasKong = mTilePickerLayout.hasKong();
        if (mSDRadioButton.isChecked()) {
            // 当副露不包含杠的时候，杠开是禁用状态
            mORCheckBox.setEnabled(mHasKong);
        } else {
            mORCheckBox.setEnabled(false);
        }
    }

    // 和牌张变化时调用，启用或禁用绝张、抢杠、海底选项
    private void refreshByWinTile() {
        int wtd = mTilePickerLayout.getWinTileDetail();
        if (wtd == mWinTileDetail) return;

        mWinTileDetail = wtd;

        // 没有和牌张
        if (wtd == 0) {
            mLTCheckBox.setEnabled(false);
            mRKCheckBox.setEnabled(false);
            mWLCheckBox.setEnabled(true);
            return;
        }

        int utc = (wtd >> 8) & 0xf;
        int stc = (wtd >> 16) & 0xf;

        // 一定为绝张
        if (stc == 1 && utc == 4) {
            mLTCheckBox.setEnabled(true);
            mLTCheckBox.setChecked(true);
            mRKCheckBox.setEnabled(false);
            return;
        }

        // 绝张：可为绝张 && 抢杠没选中
        // 抢杠：可为绝张 && 副露不包含和牌 && 点和 && 绝张没选中 && 海底没选中
        // 海底：抢杠没选中
        mLTCheckBox.setEnabled(stc == 1 && !mRKCheckBox.isChecked());
        mRKCheckBox.setEnabled(stc == 1 && utc == 1
                && mCLRadioButton.isChecked()
                && !mLTCheckBox.isChecked()
                && !mLTCheckBox.isChecked());
        mWLCheckBox.setEnabled(!mRKCheckBox.isChecked());
    }

    // 切换成点和
    private void refreshByCL() {
        mSDRadioButton.setChecked(false);

        int wtd = mWinTileDetail;
        int utc = (wtd >> 8) & 0xf;
        int stc = (wtd >> 16) & 0xf;

        // 绝张：可为绝张 && 抢杠没选中
        // 杠开：禁用
        // 抢杠：可为绝张 && 副露不包含和牌 && 绝张没选中 && 海底没选中
        // 海底：抢杠没选中
        mLTCheckBox.setEnabled(stc == 1 && !mRKCheckBox.isChecked());
        mORCheckBox.setEnabled(false);
        mRKCheckBox.setEnabled(stc == 1 && utc == 1
                && !mLTCheckBox.isChecked() && !mWLCheckBox.isChecked());
        mWLCheckBox.setEnabled(!mRKCheckBox.isChecked());
    }

    // 切换成自摸
    private void refreshBySD() {
        mCLRadioButton.setChecked(false);

        int wtd = mWinTileDetail;
        int stc = (wtd >> 16) & 0xf;

        // 绝张：可为绝张
        // 杠开：有杠
        // 抢杠：禁用
        // 海底：可用
        mLTCheckBox.setEnabled(stc == 1);
        mORCheckBox.setEnabled(mHasKong);
        mRKCheckBox.setEnabled(false);
        mWLCheckBox.setEnabled(true);
    }

    // 绝张选项切换
    private void onLTCheckBox(boolean checked) {
        // 绝张与抢杠互斥
        if (checked) {
            // 抢杠：禁用
            mRKCheckBox.setEnabled(false);
        } else {
            int wtd = mWinTileDetail;
            int utc = (wtd >> 8) & 0xf;
            int stc = (wtd >> 16) & 0xf;

            // 一定是绝张，则不允许取消选中绝张
            if (stc == 1 && utc == 4) {
                mLTCheckBox.setChecked(true);
            } else {
                // 抢杠：可为绝张 && 副露不包含和牌 && 点和 && 海底没选中
                mRKCheckBox.setEnabled(stc == 1 && utc == 1
                        && mCLRadioButton.isChecked()
                        && !mWLCheckBox.isChecked());
            }
        }
    }

    // 抢杠选项切换
    private void onRKCheckBox(boolean checked) {
        // 抢杠与绝张、海底互斥
        if (checked) {
            // 绝张：禁用
            // 海底：禁用
            mLTCheckBox.setEnabled(false);
            mWLCheckBox.setEnabled(false);
        } else {
            int wtd = mWinTileDetail;
            int stc = (wtd >> 16) & 0xf;

            // 绝张：可为绝张
            // 海底：可用
            mLTCheckBox.setEnabled(stc == 1);
            mWLCheckBox.setEnabled(true);
        }
    }

    // 海底选项切换
    private void onWLCheckBox(boolean checked) {
        // 海底与抢杠互斥
        if (checked) {
            // 抢杠：禁用
            mRKCheckBox.setEnabled(false);
        } else {
            int wtd = mWinTileDetail;
            int utc = (wtd >> 8) & 0xf;
            int stc = (wtd >> 16) & 0xf;

            // 抢杠：可为绝张 && 副露不包含和牌 && 点和 && 绝张没选中
            mRKCheckBox.setEnabled(stc == 1 && utc == 1
                    && mCLRadioButton.isChecked()
                    && !mLTCheckBox.isChecked());
        }
    }

    private boolean setStringInput(String str) {
        Mahjong.HandTiles handTiles = new Mahjong.HandTiles();
        int res = Mahjong.parseHandTiles(str, handTiles);
        if (res != Mahjong.PARSE_NO_ERROR) {
            Utils.showToastLong(this, Mahjong.getParseResultString(res));
            return false;
        }
        if (handTiles.wt == 0) {
            Utils.showToastLong(this, "缺少和牌张");
            return false;
        }

        mTilePickerLayout.setData(handTiles.fp, handTiles.st, handTiles.wt);
        return true;
    }

    private void showInputDialog() {
        new InputTileDialog(this, R.string.itl_xmp_fc, dialog -> {
            if (setStringInput(dialog.getInput())) {
                dialog.dismiss();
            }
        }).show();
    }

    private void showInstruction() {
        new CommonTextDialog(this,"使用说明", getString(R.string.fc_instruction)).show();
    }

    private void showRule() {
        new FanCalculatorRuleDialog(this).show();
    }

    private void showFanDefinition(int fan) {
        int idx = (fan != Mahjong.PURE_SHIFTED_CHOWS_2 && fan != Mahjong.FOUR_PURE_SHIFTED_CHOWS_2)
                ? fan : fan - 1;
        new CommonWebFullScreenDialog(this, Mahjong.FAN_NAME[fan],
                "file:///android_asset/www/rule/fan/" + idx + ".html").show();
    }

}
