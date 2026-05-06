package net.tziakcha.chineseofficialmahjonghelper.theory;

import android.animation.AnimatorInflater;
import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import net.tziakcha.chineseofficialmahjonghelper.Common;
import net.tziakcha.chineseofficialmahjonghelper.Mahjong;
import net.tziakcha.chineseofficialmahjonghelper.R;
import net.tziakcha.chineseofficialmahjonghelper.Utils;
import net.tziakcha.chineseofficialmahjonghelper.widget.CommonTextDialog;
import net.tziakcha.chineseofficialmahjonghelper.widget.FlowLayout;
import net.tziakcha.chineseofficialmahjonghelper.widget.HandTilesLayout;
import net.tziakcha.chineseofficialmahjonghelper.widget.InputTileDialog;
import net.tziakcha.chineseofficialmahjonghelper.widget.LoadingDialog;
import net.tziakcha.chineseofficialmahjonghelper.widget.TilePickerDialog;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;

public class MahjongTheoryActivity extends AppCompatActivity {

    static final class TheoryResult extends Mahjong.EnumResult {
        int variety;  // 有效牌种数
        int total;  // 有效牌总数
        long imaginary;  // 是否不存在标记
    }

    static final class StateData {
        int[] fp;  // 副露
        int[] st;  // 立牌
        int wt;  // 上牌
        Mahjong.EnumResult[] results;  // 计算结果
        int[] table;  // 手牌牌表
    }

    private HandTilesLayout mHandTilesLayout;
    private TextView mTileText;
    private Button mUndoButton;
    private Button mRedoButton;
    private TextView mStepText;
    private CheckBox mSPCheckBox;
    private CheckBox mTOCheckBox;
    private CheckBox mHKTCheckBox;
    private CheckBox mKTCheckBox;
    private RecyclerView mResultRecyclerView;
    private final int[] mHandTilesTable = new int[34];  // 手牌牌表，用来计算牌张是否占用了4张
    private Mahjong.EnumResult[] mAllResult;  // 所有结果
    private final ArrayList<TheoryResult> mSourceResult = new ArrayList<>();  // 当前用来显示的结果
    private final ArrayList<StateData> mUndoCache = new ArrayList<>();
    private final ArrayList<StateData> mRedoCache = new ArrayList<>();

    private final ResultRecyclerViewAdapter mResultRecyclerViewAdapter = new ResultRecyclerViewAdapter();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        View contentView = View.inflate(this, R.layout.mahjong_theory_layout, null);
        setContentView(contentView);

        ((TextView)contentView.findViewById(R.id.ab_txt)).setText("牌理");

        contentView.findViewById(R.id.ab_l_btn).setOnClickListener(view ->
                getOnBackPressedDispatcher().onBackPressed());

        contentView.findViewById(R.id.ab_r_btn).setOnClickListener(view -> showInstruction());

        mTileText = contentView.findViewById(R.id.mtl_txt_tile);
        mTileText.setOnClickListener(view -> showPickTileDialog());

        mHandTilesLayout = Utils.createAndLayoutHandTiles(this,
                contentView.findViewById(R.id.mtl_rl_hand));
        mHandTilesLayout.hidePlaceholder();
        mHandTilesLayout.setOnTileClickListener((htl, idx, tile) -> onStandingTile(tile));

        contentView.findViewById(R.id.mtl_btn_random).setOnClickListener(view -> setRandomInput());

        mRedoButton = contentView.findViewById(R.id.mtl_btn_redo);
        mRedoButton.setOnClickListener(view -> onRedoButton());
        mUndoButton = contentView.findViewById(R.id.mtl_btn_undo);
        mUndoButton.setOnClickListener(view -> onUndoButton());

        mStepText = contentView.findViewById(R.id.mtl_txt_step);

        final int dp28 = getResources().getDimensionPixelSize(R.dimen.dp28);

        mSPCheckBox = contentView.findViewById(R.id.mtl_cb_sp);
        mSPCheckBox.setOnCheckedChangeListener((view, state) -> filterResultsByFlag());
        Utils.adaptCompoundButton(mSPCheckBox, dp28);
        mTOCheckBox = contentView.findViewById(R.id.mtl_cb_to);
        mTOCheckBox.setOnCheckedChangeListener((view, state) -> filterResultsByFlag());
        Utils.adaptCompoundButton(mTOCheckBox, dp28);
        mHKTCheckBox = contentView.findViewById(R.id.mtl_cb_hkt);
        mHKTCheckBox.setOnCheckedChangeListener((view, state) -> filterResultsByFlag());
        Utils.adaptCompoundButton(mHKTCheckBox, dp28);
        mKTCheckBox = contentView.findViewById(R.id.mtl_cb_kt);
        mKTCheckBox.setOnCheckedChangeListener((view, state) -> filterResultsByFlag());
        Utils.adaptCompoundButton(mKTCheckBox, dp28);

        RecyclerView rv = contentView.findViewById(R.id.mtl_rv_result);
        rv.setLayoutManager(new LinearLayoutManager(this));
        rv.setAdapter(mResultRecyclerViewAdapter);
        mResultRecyclerView = rv;

        setRandomInput();
    }

    private void showInstruction() {
        new CommonTextDialog(this,
                "使用说明", getString(R.string.mt_instruction)).show();
    }

    private void showPickTileDialog() {
        new TilePickerDialog(this, mHandTilesLayout.getData(), (dialog, which) -> {
            if (which == TilePickerDialog.BUTTON_CONFIRM) {
                setSpecifiedInput(dialog.getData());
            } else {
                showInputTileDialog();
            }
            dialog.dismiss();
        }).show();
    }

    private void showInputTileDialog() {
        new InputTileDialog(this, R.string.itl_xmp_mt, dialog -> {
            if (setSpecifiedStringInput(dialog.getInput())) {
                dialog.dismiss();
            }
        }).show();
    }

    private void setRandomInput() {
        // 随机生成手牌
        Random random = new Random();
        int[] st = new int[13];
        int[] table = mHandTilesTable;
        Arrays.fill(table, 0);
        int cnt = 0;
        do {
            int n = random.nextInt(34);
            if (table[n] < 4) {
                ++table[n];
                st[cnt++] = Mahjong.ALL_TILES[n];
            }
        } while (cnt < 13);

        int wt = 0;
        do {
            int n = random.nextInt(34);
            if (table[n] < 4) {
                ++table[n];
                wt = Mahjong.ALL_TILES[n];
            }
        } while (wt == 0);

        // 设置
        Arrays.sort(st);
        mHandTilesLayout.setData(null, st, wt);
        mTileText.setText(Mahjong.handTilesToString(null, st, wt));

        // 清空再计算
        mSourceResult.clear();
        mUndoCache.clear();
        mRedoCache.clear();
        mUndoButton.setEnabled(false);
        mRedoButton.setEnabled(false);
        mStepText.setText(String.valueOf(0));

        asyncCalc(st, wt);
    }

    // 校正手牌
    private static boolean adjustHandTiles(Mahjong.HandTiles handTiles) {
        final int stLen = handTiles.st.length;
        if (stLen < 13) {
            int[] fp = handTiles.fp;
            switch (stLen % 3) {
                case 2:
                    // 将最后一张作为上牌，不需要break
                    handTiles.wt = handTiles.st[stLen - 1];
                    handTiles.st = Arrays.copyOfRange(handTiles.st, 0, stLen - 1);
                case 1:
                    // 非满手的情况
                    // 修正副露组数，以便正确绘制
                    handTiles.fp = new int[4 - stLen / 3];
                    if (fp != null && fp.length > 0) {
                        System.arraycopy(fp, 0, handTiles.fp, 0, fp.length);
                    }
                    break;
                default:
                    return false;
            }
        }
        return true;
    }

    private void setSpecifiedInput(Mahjong.HandTiles handTiles) {
        if (!adjustHandTiles(handTiles)) {
            Utils.toastMakeText(this, "错误的牌数目", 1).show();
            return;
        }

        // 获取之前的数据
        Mahjong.HandTiles prev = mHandTilesLayout.getData();

        // 如果实质相同，则不需要计算，直接返回即可
        if (Mahjong.substantiallyEqual(handTiles, prev)) {
            mHandTilesLayout.setData(handTiles.fp, handTiles.st, handTiles.wt);
            mTileText.setText(Mahjong.handTilesToString(handTiles.fp, handTiles.st, handTiles.wt));
            return;
        }

        // 打表
        int[] table = mHandTilesTable;
        Arrays.fill(table, 0);
        for (int p : handTiles.fp) {
            int i = Mahjong.tileIdx(Mahjong.packTile(p));
            switch (Mahjong.packType(p)) {
                case Mahjong.PACK_TYPE_CHOW:
                    ++table[i - 1];
                    ++table[i];
                    ++table[i + 1];
                    break;
                case Mahjong.PACK_TYPE_PUNG:
                    ++table[i];
                    ++table[i];
                    ++table[i];
                    break;
                case Mahjong.PACK_TYPE_KONG:
                    ++table[i];
                    ++table[i];
                    ++table[i];
                    ++table[i];
                    break;
            }
        }
        for (int t : handTiles.st) {
            ++table[Mahjong.tileIdx(t)];
        }
        if (handTiles.wt != 0) {
            ++table[Mahjong.tileIdx(handTiles.wt)];
        } else {
            // 随机上牌
            int t = serveRandomTile(table, 0);
            ++table[Mahjong.tileIdx(t)];
            handTiles.wt = t;
        }

        // 设置新数据
        mHandTilesLayout.setData(handTiles.fp, handTiles.st, handTiles.wt);
        mTileText.setText(Mahjong.handTilesToString(handTiles.fp, handTiles.st, handTiles.wt));

        // 清空再计算
        mSourceResult.clear();
        mUndoCache.clear();
        mRedoCache.clear();
        mUndoButton.setEnabled(false);
        mRedoButton.setEnabled(false);
        mStepText.setText(String.valueOf(0));

        asyncCalc(handTiles.st, handTiles.wt);
    }

    private boolean setSpecifiedStringInput(String str) {
        Mahjong.HandTiles handTiles = new Mahjong.HandTiles();
        int res = Mahjong.parseHandTiles(str, handTiles);
        if (res != Mahjong.PARSE_NO_ERROR) {
            Utils.toastMakeText(this, Mahjong.getParseResultString(res), 1).show();
            return false;
        }

        setSpecifiedInput(handTiles);
        return true;
    }

    // 异步计算
    private void asyncCalc(final int[] st, final int wt) {
        mResultRecyclerView.setVisibility(View.INVISIBLE);

        LoadingDialog dialog = new LoadingDialog();
        dialog.show(getSupportFragmentManager(), "LoadingDialog");

        new Thread(() -> {
            mAllResult = Mahjong.enumDiscardTile(st, wt);
            runOnUiThread(() -> {
                filterResultsByFlag();
                mResultRecyclerView.setVisibility(View.VISIBLE);
                dialog.dismiss();
            });
        }).start();
    }

    private static final String[] WIN_FORM_TEXT = {"基本和型", "七对", "十三幺", "全不靠", "组合龙"};
    private static final String[] BROKEN_TEXTS = {"打「", "」摸「", "」听「", "」", "共", "", "种，", "", "张"};

    private static StringBuilder getTypeString(int shanten, int form) {
        StringBuilder str = new StringBuilder();
        switch (shanten) {
            case 0: str.append("听牌 ("); break;
            case -1: str.append("和了 ("); break;
            default: str.append(shanten).append("上听 ("); break;
        }

        boolean needCaesuraSign = false;
        for (int i = 0; i < 5; ++i) {
            if ((form & (1 << i)) != 0) {
                if (needCaesuraSign) str.append("、");
                str.append(WIN_FORM_TEXT[i]);
                needCaesuraSign = true;
            }
        }
        str.append(")");

        return str;
    }

    private static int sPadding;
    private static int sImgWidth;
    private static int sImgHeight;
    private static int sFlowMargin;

    private final class ResultRecyclerViewHolder extends RecyclerView.ViewHolder {

        private int mIndex = -1;
        private TextView mTxtType;
        private ImageButton mImgDiscard;
        private final TextView[] mTxtUseful = new TextView[2];  // 摸/听
        private final ImageButton[] mImgUseful = new ImageButton[34];
        private final TextView[] mTxtRemains = new TextView[6];  // 共xxx种，xxx张

        public ResultRecyclerViewHolder(@NonNull View itemView) {
            super(itemView);
            init(itemView.getContext());
        }

        private void init(Context context) {
            if (sPadding == 0) {
                DisplayMetrics metrics = context.getResources().getDisplayMetrics();
                sPadding = context.getResources().getDimensionPixelSize(R.dimen.dp5);

                // 满屏横向排18张牌
                float scale = (float)metrics.widthPixels / (float)(HandTilesLayout.IMG_WIDTH * 18);
                sImgWidth = (int)Math.ceil(HandTilesLayout.IMG_WIDTH * scale);
                sImgHeight = (int)Math.ceil(HandTilesLayout.IMG_HEIGHT * scale);

                sFlowMargin = context.getResources().getDimensionPixelSize(R.dimen.dp25);
            }

            RelativeLayout relativeLayout = (RelativeLayout)itemView;
            relativeLayout.setLayoutParams(new ViewGroup.MarginLayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            relativeLayout.setPadding(sPadding, sPadding, sPadding, sPadding);

            final int dp14 = context.getResources().getDimensionPixelSize(R.dimen.dp14);

            TextView tv = new TextView(context);
            tv.setLayoutParams(new ViewGroup.MarginLayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT));
            tv.setTextSize(TypedValue.COMPLEX_UNIT_PX, dp14);
            relativeLayout.addView(tv);
            mTxtType = tv;

            // 文字拆开，便于在浮动布局自动换行
            final int textColor = ContextCompat.getColor(context, R.color.text_3);
            TextView[] textViews = new TextView[9];
            for (int i = 0; i < 9; ++i) {
                tv = new TextView(context);
                tv.setLayoutParams(new ViewGroup.MarginLayoutParams(
                        ViewGroup.LayoutParams.WRAP_CONTENT, sImgHeight));
                ((ViewGroup.MarginLayoutParams)tv.getLayoutParams()).topMargin = sPadding;
                tv.setGravity(Gravity.BOTTOM);
                tv.setTextColor(textColor);
                tv.setTextSize(TypedValue.COMPLEX_UNIT_PX, dp14);
                tv.setText(BROKEN_TEXTS[i]);
                textViews[i] = tv;
            }

            ImageButton img;
            ImageButton[] imageButtons = new ImageButton[35];
            for (int i = 0; i < 35; ++i) {
                img = new ImageButton(context);
                img.setLayoutParams(new ViewGroup.MarginLayoutParams(sImgWidth, sImgHeight));
                img.setStateListAnimator(AnimatorInflater.loadStateListAnimator(context,
                        R.animator.button_scale_animator));
                ((ViewGroup.MarginLayoutParams)img.getLayoutParams()).topMargin = sPadding;
                final int which = i - 1;
                img.setOnClickListener(view -> {
                    if (mIndex != -1) {
                        onTileButton(mIndex, which);
                    }
                });
                imageButtons[i] = img;
            }

            FlowLayout fl = new FlowLayout(context);
            fl.setLayoutParams(new ViewGroup.MarginLayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT));
            ((ViewGroup.MarginLayoutParams)fl.getLayoutParams()).topMargin = sFlowMargin;
            relativeLayout.addView(fl);
            //fl.setBackgroundColor(Color.CYAN);

            // 按顺序添加到浮动布局中
            fl.addView(textViews[0]);
            fl.addView(((mImgDiscard = imageButtons[0])));
            fl.addView((mTxtUseful[0] = textViews[1]));
            fl.addView((mTxtUseful[1] = textViews[2]));
            for (int i = 0; i < 34; ++i) {
                img = imageButtons[i + 1];
                img.setBackgroundResource(Utils.TILE_IMG_SRC[i]);
                fl.addView((mImgUseful[i] = img));
            }
            for (int i = 0; i < 6; ++i) {
                tv = textViews[i + 3];
                fl.addView((mTxtRemains[i] = tv));
            }
        }

        public void setup(int idx, TheoryResult result) {
            mIndex = idx;
            itemView.setBackgroundColor(
                    (idx & 1) != 0
                            ? Color.argb(0x10, 0xc0, 0xc0, 0xc0)
                            : Color.argb(0x10, 0x10, 0x10, 0x10));

            int shanten = result.shanten;
            mTxtType.setText(getTypeString(shanten, result.form));
            mTxtType.setTextColor(shanten != -1 ? Common.COLOR_GRAY : Common.COLOR_ORANGE);

            mImgDiscard.setBackgroundResource(Utils.TILE_IMG_SRC[Mahjong.tileIdx(result.discard)]);

            if (shanten > 0) {
                mTxtUseful[0].setVisibility(View.VISIBLE);
                mTxtUseful[1].setVisibility(View.GONE);
            } else {
                mTxtUseful[1].setVisibility(View.VISIBLE);
                mTxtUseful[0].setVisibility(View.GONE);
            }

            for (int i = 0; i < 34; ++i) {
                if (((1L << i) & result.useful) != 0) {
                    mImgUseful[i].setVisibility(View.VISIBLE);
                } else {
                    mImgUseful[i].setVisibility(View.GONE);
                }
            }
            mTxtRemains[2].setText(String.valueOf(result.variety));
            mTxtRemains[4].setText(String.valueOf(result.total));
        }
    }

    private final class ResultRecyclerViewAdapter extends RecyclerView.Adapter<ResultRecyclerViewHolder> {

        @NonNull
        @Override
        public ResultRecyclerViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new ResultRecyclerViewHolder(new RelativeLayout(parent.getContext()));
        }

        @Override
        public void onBindViewHolder(@NonNull ResultRecyclerViewHolder holder, int position) {
            holder.setup(position, mSourceResult.get(position));
        }

        @Override
        public int getItemCount() {
            return mSourceResult.size();
        }
    }

    // 过滤计算结果
    @SuppressLint("NotifyDataSetChanged")
    private void filterResultsByFlag() {
        int form = Mahjong.FORM_FLAG_REGULAR;  // 基本和型不能被过滤掉
        if (mSPCheckBox.isChecked()) form |= Mahjong.FORM_FLAG_SEVEN_PAIRS;
        if (mTOCheckBox.isChecked()) form |= Mahjong.FORM_FLAG_THIRTEEN_ORPHANS;
        if (mHKTCheckBox.isChecked()) form |= Mahjong.FORM_FLAG_HONORS_AND_KNITTED_TILES;
        if (mKTCheckBox.isChecked()) form |= Mahjong.FORM_FLAG_KNITTED_STRAIGHT;

        mSourceResult.clear();

        if (mAllResult == null || mAllResult.length == 0) {
            return;
        }

        // 从all里面过滤、合并
        for (Mahjong.EnumResult result2 : mAllResult) {
            if ((result2.form & form) == 0) {
                continue;
            }

            // 相同出牌有相同上听数的两个result
            boolean found = false;
            for (TheoryResult result : mSourceResult) {
                if (result.discard == result2.discard
                        && result.shanten == result2.shanten) {
                    found = true;

                    // 找到，则合并mSourceResult与mAllResult的和牌形式标记及有效牌
                    result.form |= result2.form;
                    result.useful |= result2.useful;
                    break;
                }
            }

            if (!found) {
                // 未找到，直接复制到mSourceResult
                TheoryResult result = new TheoryResult();
                result.discard = result2.discard;
                result.form = result2.form;
                result.shanten = result2.shanten;
                result.useful = result2.useful;
                mSourceResult.add(result);
            }
        }

        // 计算几种、几张，是否已用4张
        for (TheoryResult result : mSourceResult) {
            result.variety = 0;
            result.total = 0;
            result.imaginary = 0;
            for (int i = 0; i < 34; ++i) {
                long mask = 1L << i;
                if ((mask & result.useful) != 0) {
                    ++result.variety;
                    int remain = 4 - mHandTilesTable[i];
                    if (remain > 0) {
                        result.total += remain;
                    } else {
                        result.imaginary |= mask;
                    }
                }
            }
        }

        if (mSourceResult.isEmpty()) {
            return;
        }

        // 排序：上听数、有效牌总张数、有效牌种数、类型、牌张
        mSourceResult.sort((a, b) -> {
            int v = a.shanten - b.shanten;
            if (v != 0) return v;
            v = b.total - a.total;
            if (v != 0) return v;
            v = b.variety - a.variety;
            if (v != 0) return v;
            v = a.form - b.form;
            if (v != 0) return v;
            return a.discard - b.discard;
        });

        // 以第一个为标准，过滤掉上听数高的
        final int minShanten = mSourceResult.get(0).shanten;
        mSourceResult.removeIf(result -> result.shanten > minShanten);

        mResultRecyclerViewAdapter.notifyDataSetChanged();
    }

    // 撤销
    private void onUndoButton() {
        if (!mUndoCache.isEmpty()) {
            // 把当前数据保存到重做缓存
            Mahjong.HandTiles handTiles = mHandTilesLayout.getData();
            StateData state = new StateData();
            state.fp = handTiles.fp;
            state.st = handTiles.st;
            state.wt = handTiles.wt;
            state.results = mAllResult;
            state.table = Arrays.copyOf(mHandTilesTable, mHandTilesTable.length);
            mRedoCache.add(state);
            mRedoButton.setEnabled(true);

            // 从撤销缓存里取出
            recoverFromState(mUndoCache.remove(mUndoCache.size() - 1));
            mUndoButton.setEnabled(!mUndoCache.isEmpty());
            mStepText.setText(String.valueOf(mUndoCache.size()));
        }
    }

    // 重做
    private void onRedoButton() {
        if (!mRedoCache.isEmpty()) {
            // 把当前数据保存到撤销缓存
            Mahjong.HandTiles handTiles = mHandTilesLayout.getData();
            StateData state = new StateData();
            state.fp = handTiles.fp;
            state.st = handTiles.st;
            state.wt = handTiles.wt;
            state.results = mAllResult;
            state.table = Arrays.copyOf(mHandTilesTable, mHandTilesTable.length);
            mUndoCache.add(state);
            mUndoButton.setEnabled(true);
            mStepText.setText(String.valueOf(mUndoCache.size()));

            // 从重做缓存里取出
            recoverFromState(mRedoCache.remove(mRedoCache.size() - 1));
            mRedoButton.setEnabled(!mRedoCache.isEmpty());
        }
    }

    private void recoverFromState(final StateData state) {
        mHandTilesLayout.setData(state.fp, state.st, state.wt);
        mTileText.setText(Mahjong.handTilesToString(state.fp, state.st, state.wt));
        System.arraycopy(state.table, 0, mHandTilesTable, 0, 34);
        mAllResult = state.results;
        filterResultsByFlag();
    }

    // 根据已经用到的牌，随机发一张牌
    static private int serveRandomTile(final int[] usedTable, int discard) {
        // 没用到的牌
        int[] remainTiles = new int[136];
        int remainSize = 0;
        for (int i = 0; i < 34; ++i) {
            int n = 4 - usedTable[i];
            if (n > 0) {
                Arrays.fill(remainTiles, remainSize, remainSize + n, i);
                remainSize += n;
            }
        }

        // 随机给一张牌
        Random random = new Random();
        int t;
        do {
            t = Mahjong.ALL_TILES[remainTiles[random.nextInt(remainSize)]];
        } while (t == discard);

        return t;
    }

    private void onTileButton(int itemIndex, int which) {
        int discard = mSourceResult.get(itemIndex).discard;
        int serving = which != -1 ? Mahjong.ALL_TILES[which] : serveRandomTile(mHandTilesTable, discard);
        deduce(discard, serving);
    }

    private void onStandingTile(int tile) {
        if (tile == 0) return;
        int serving = serveRandomTile(mHandTilesTable, tile);
        deduce(tile, serving);
    }

    private void deduce(int discard, int serving) {
        if (discard == serving) return;

        // 清空重做
        mRedoCache.clear();
        mRedoButton.setEnabled(false);

        // 获取牌
        Mahjong.HandTiles handTiles = mHandTilesLayout.getData();
        int[] st = handTiles.st;

        // 保存旧数据
        StateData state = new StateData();
        state.fp = handTiles.fp;
        state.st = Arrays.copyOf(st, st.length);
        state.wt = handTiles.wt;
        state.results = mAllResult;
        state.table = Arrays.copyOf(mHandTilesTable, mHandTilesTable.length);
        mUndoCache.add(state);

        // 设置撤销
        mUndoButton.setEnabled(true);
        mStepText.setText(String.valueOf(mUndoCache.size()));

        // 手打，替换打出的牌，再排序
        if (handTiles.wt != discard) {
            for (int i = 0, l = st.length; i < l; ++i) {
                if (st[i] == discard) {
                    st[i] = handTiles.wt;
                    break;
                }
            }
            Arrays.sort(st);
        }
        --mHandTilesTable[Mahjong.tileIdx(discard)];
        ++mHandTilesTable[Mahjong.tileIdx(serving)];

        // 设置回去
        mHandTilesLayout.setData(handTiles.fp, st, serving);
        mTileText.setText(Mahjong.handTilesToString(handTiles.fp, st, serving));

        // 清空再计算
        mSourceResult.clear();

        asyncCalc(st, serving);
    }

}
