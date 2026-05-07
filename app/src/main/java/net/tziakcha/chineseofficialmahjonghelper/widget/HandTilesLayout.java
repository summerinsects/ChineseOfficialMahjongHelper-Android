package net.tziakcha.chineseofficialmahjonghelper.widget;

import android.animation.AnimatorInflater;
import android.content.Context;
import android.graphics.Color;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import net.tziakcha.chineseofficialmahjonghelper.Mahjong;
import net.tziakcha.chineseofficialmahjonghelper.R;
import net.tziakcha.chineseofficialmahjonghelper.Utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;

public class HandTilesLayout extends RelativeLayout {

    public interface OnIndexChangedListener {
        void onIndexChanged(HandTilesLayout htl);
    }

    public interface OnTileClickListener {
        void onTileClick(HandTilesLayout htl, int idx, int tile);
    }

    public static final int IMG_WIDTH = 44;  // 牌张宽
    public static final int IMG_HEIGHT = 60;  // 牌张高
    public static final int IMG_GAP = 4;  // 立牌与上牌、副露之间的间隔
    public static final int IMG_SIZE_DELTA = IMG_HEIGHT - IMG_WIDTH;
    public static final int IMG_SIZE_DELTA_HALF = IMG_SIZE_DELTA / 2;

    public static final int DZN_WIDTH = IMG_WIDTH * 14 + IMG_GAP;  // 整体宽
    public static final int DZN_HEIGHT = IMG_HEIGHT + IMG_GAP + IMG_WIDTH * 2;  // 整体高

    private RelativeLayout mFixedRoot;  // 副露牌张的根结点，用来整体缩放
    private RelativeLayout mStandingRoot;  // 立牌张的根结点，用于整体居中

    // 已经使用过的牌，用于判断某牌是否可继续添加，超过4张不可再添加
    private final byte[] mUsedTilesTable = new byte[Mahjong.TILE_TABLE_SIZE];
    // 立牌中有的牌，用于判断可否副露
    private final byte[] mStandingTilesTable = new byte[Mahjong.TILE_TABLE_SIZE];
    private final ArrayList<Integer> mFixedPacks = new ArrayList<>();  // 副露数据
    private int mFixedWidth = 0;  // 副露整体的宽度
    private int mCurrentIdx = 0;  // 当前选中下标
    private final ArrayList<ImageButton> mStandingTileButtons = new ArrayList<>();  // 立牌
    private Button mEmptyButton;  // 占位牌，永远在最后
    private ImageView mHighlightBox;  // 高亮位置
    private OnIndexChangedListener mOnIndexChangedListener;  // 选中下标改变的回调
    private OnTileClickListener mOnTileClickListener;  // 点击牌张回调

    public HandTilesLayout(Context context) {
        super(context);
        init(context);
    }

    private void init(Context context) {
        setLayoutParams(new MarginLayoutParams(DZN_WIDTH, DZN_HEIGHT));
        //setBackgroundColor(Color.RED);  // 测试范围用

        // 副露牌张的根结点，用来整体缩放
        RelativeLayout rl = new RelativeLayout(context);
        rl.setLayoutParams(new MarginLayoutParams(DZN_WIDTH, IMG_WIDTH * 2));
        ((MarginLayoutParams)rl.getLayoutParams()).rightMargin = DZN_HEIGHT - IMG_WIDTH * 12 - IMG_HEIGHT * 4 - IMG_GAP * 3;  // 重要！否则会自动缩放
        addView(rl);
        mFixedRoot = rl;
        //rl.setBackgroundColor(Color.BLUE);  // 测试范围用

        // 立牌张的根结点，用于整体居中
        rl = new RelativeLayout(context);
        rl.setLayoutParams(new MarginLayoutParams(DZN_WIDTH, IMG_HEIGHT));
        ((MarginLayoutParams)rl.getLayoutParams()).topMargin = DZN_HEIGHT - IMG_HEIGHT;
        addView(rl);
        mStandingRoot = rl;

        // 占位牌，永远在最后
        Button btn = new Button(context);
        btn.setLayoutParams(new MarginLayoutParams(IMG_WIDTH, IMG_HEIGHT));
        btn.setBackgroundColor(Color.rgb(0x10, 0x10, 0x10));
        btn.setAlpha(0.125f);
        mStandingRoot.addView(btn);
        btn.setOnClickListener(view -> onEmptyButton());
        mEmptyButton = btn;

        // 高亮位置
        ImageView img = new ImageView(context);
        img.setLayoutParams(new MarginLayoutParams(IMG_WIDTH, IMG_HEIGHT));
        img.setImageResource(R.drawable.tl_hl);
        mStandingRoot.addView(img);
        mHighlightBox = img;
    }

    public void setOnIndexChangedListener(OnIndexChangedListener listener) {
        mOnIndexChangedListener = listener;
    }

    public void setOnTileClickListener(OnTileClickListener listener) {
        mOnTileClickListener = listener;
    }

    public void reset() {
        Arrays.fill(mUsedTilesTable, (byte)0);
        Arrays.fill(mStandingTilesTable, (byte)0);
        mFixedPacks.clear();
        mFixedWidth = 0;
        mCurrentIdx = 0;

        mStandingTileButtons.forEach(img -> ((ViewGroup)img.getParent()).removeView(img));
        mStandingTileButtons.clear();
        mStandingRoot.getLayoutParams().width = DZN_WIDTH;
        ((MarginLayoutParams)mStandingRoot.getLayoutParams()).leftMargin = 0;

        mFixedRoot.removeAllViews();
        ((MarginLayoutParams)mEmptyButton.getLayoutParams()).leftMargin = 0;
        ((MarginLayoutParams)mHighlightBox.getLayoutParams()).leftMargin = 0;
    }

    public void setData(int[] fp, int[] st, int wt) {
        reset();

        // 添加副露
        if (fp != null) {
            for (int i = 0, l = fp.length; i < l; ++i) {
                int pack = fp[i];
                mFixedPacks.add(pack);

                int tile = Mahjong.packTile(pack);
                switch (Mahjong.packType(pack)) {
                    case Mahjong.PACK_TYPE_CHOW:
                        switch (Mahjong.packOffer(pack)) {
                            default: addFixedChowPack(tile - 1, 0); break;
                            case 2: addFixedChowPack(tile, 1); break;
                            case 3: addFixedChowPack(tile + 1, 2); break;
                        }
                        ++mUsedTilesTable[tile - 1];
                        ++mUsedTilesTable[tile];
                        ++mUsedTilesTable[tile + 1];
                        break;
                    case Mahjong.PACK_TYPE_PUNG:
                        switch (Mahjong.packOffer(pack)) {
                            default: addFixedPungPack(tile, 0); break;
                            case 2: addFixedPungPack(tile, 1); break;
                            case 3: addFixedPungPack(tile, 2); break;
                        }
                        mUsedTilesTable[tile] += 3;
                        break;
                    case Mahjong.PACK_TYPE_KONG:
                        if (!Mahjong.packIsPKong(pack)) {
                            switch (Mahjong.packOffer(pack)) {
                                case 0: addFixedCKongPack(tile); break;
                                default: addFixedMKongPack(tile, 0); break;
                                case 2: addFixedMKongPack(tile, 1); break;
                                case 3: addFixedMKongPack(tile, 3); break;
                            }
                        } else {
                            switch (Mahjong.packOffer(pack)) {
                                case 0: addFixedCKongPack(tile); break;
                                default: addFixedPungPack(tile, 0); promoteFixedPungPackToKongPack(tile, i); break;
                                case 2: addFixedPungPack(tile, 1); promoteFixedPungPackToKongPack(tile, i); break;
                                case 3: addFixedPungPack(tile, 2); promoteFixedPungPackToKongPack(tile, i); break;
                            }
                        }
                        mUsedTilesTable[tile] += 4;
                        break;
                    default:
                        break;
                }
            }

        }

        refreshStandingTilesPos();

        // 添加立牌
        if (st != null) {
            for (int tile : st) {
                if (addTile(tile)) ++mUsedTilesTable[tile];
            }
        }

        if (wt != 0) {
            if (addTile(wt)) ++mUsedTilesTable[wt];
        }

        refreshEmptyWidgetPos();
        refreshHighlightPos();
    }

    public Mahjong.HandTiles getData() {
        Mahjong.HandTiles ht = new Mahjong.HandTiles();

        int maxCnt = 13 - mFixedPacks.size() * 3;  // 立牌数最大值（不包括和牌）
        int size = mStandingTileButtons.size();
        if (size > maxCnt) {
            size = maxCnt;
            ht.wt = (int)mStandingTileButtons.get(maxCnt).getTag();
        }

        ht.st = new int[size];
        for (int i = 0; i < size; ++i) {
            ht.st[i] = (int)mStandingTileButtons.get(i).getTag();
        }

        size = mFixedPacks.size();
        ht.fp = new int[size];
        for (int i = 0; i < size; ++i) {
            ht.fp[i] = mFixedPacks.get(i);
        }

        return ht;
    }

    public int getServingTile() {
        int maxCnt = 13 - mFixedPacks.size() * 3;  // 立牌数最大值（不包括和牌）
        int size = mStandingTileButtons.size();
        if (size <= maxCnt) {
            return 0;
        }
        return (int)mStandingTileButtons.get(maxCnt).getTag();
    }

    public byte getUsedTileCount(int tile) {
        return mUsedTilesTable[tile];
    }

    public int putTile(int tile) {
        int prevTile = 0;

        if (mCurrentIdx >= mStandingTileButtons.size()) {
            // 新增牌
            if (!addTile(tile)) return 0;

            ++mUsedTilesTable[tile];

            refreshEmptyWidgetPos();
        } else {
            // 修改牌
            prevTile = (int)mStandingTileButtons.get(mCurrentIdx).getTag();  // 此位置之前的牌

            // 新选的牌与之前的牌不同，更新相关信息
            if (prevTile != tile) {
                replaceTile(tile);
                --mUsedTilesTable[prevTile];
                ++mUsedTilesTable[tile];
            }

            int maxCnt = 13 - mFixedPacks.size() * 3;  // 立牌数最大值（不包括和牌）

            // 根据需要增加下标
            if (mCurrentIdx < maxCnt) {
                ++mCurrentIdx;
            }
        }

        refreshHighlightPos();

        return prevTile;
    }

    public void hideTile(int idx) {
        if (idx < mStandingTileButtons.size()) {
            mStandingTileButtons.get(idx).setVisibility(View.INVISIBLE);
        }
    }

    public void hidePlaceholder() {
        mEmptyButton.setVisibility(View.GONE);
        mHighlightBox.setVisibility(View.GONE);
    }

    private void onEmptyButton() {
        int maxCnt = 13 - mFixedPacks.size() * 3;  // 立牌数最大值（不包括和牌）
        int tileCnt = mStandingTileButtons.size();
        int idx = Math.min(maxCnt, tileCnt);
        if (idx != mCurrentIdx) {
            mCurrentIdx = idx;
            refreshHighlightPos();
        }
    }

    private void onTileButton(int idx) {
        if (mCurrentIdx != idx) {
            mCurrentIdx = idx;
            refreshHighlightPos();
        }

        if (mOnTileClickListener != null) {
            int tile = idx < mStandingTileButtons.size() ? (int)mStandingTileButtons.get(idx).getTag() : 0;
            mOnTileClickListener.onTileClick(this, idx, tile);
        }
    }

    private boolean addTile(int tile) {
        if (mUsedTilesTable[tile] >= 4) return false;

        int maxCnt = 13 - mFixedPacks.size() * 3;  // 立牌数最大值（不包括和牌）
        int tileCnt = mStandingTileButtons.size();
        if (tileCnt < maxCnt) {
            mCurrentIdx = tileCnt + 1;
        }

        MarginLayoutParams mlp = new MarginLayoutParams(IMG_WIDTH, IMG_HEIGHT);
        if (tileCnt < maxCnt) {
            mlp.leftMargin = tileCnt * IMG_WIDTH;
            mEmptyButton.setClickable(true);
        } else {
            mlp.leftMargin = maxCnt * IMG_WIDTH + IMG_GAP;
            mEmptyButton.setClickable(false);
        }

        ImageButton img = new ImageButton(getContext());
        img.setBackgroundResource(Utils.TILE_IMG_SRC[Mahjong.tileIdx(tile)]);
        img.setLayoutParams(mlp);
        img.setStateListAnimator(AnimatorInflater.loadStateListAnimator(getContext(), R.animator.button_scale_animator));
        mStandingRoot.addView(img);

        ++mStandingTilesTable[tile];
        img.setTag(tile);
        mStandingTileButtons.add(img);

        img.setOnClickListener(view -> onTileButton(mStandingTileButtons.indexOf(img)));

        return true;
    }

    private void replaceTile(int tile) {
        if (mStandingTilesTable[tile] >= 4) return;

        ImageButton img = mStandingTileButtons.get(mCurrentIdx);
        img.setBackgroundResource(Utils.TILE_IMG_SRC[Mahjong.tileIdx(tile)]);
        img.setVisibility(View.VISIBLE);

        int prevTile = (int)img.getTag();
        --mStandingTilesTable[prevTile];

        img.setTag(tile);
        ++mStandingTilesTable[tile];
    }

    private void refreshEmptyWidgetPos() {
        int maxCnt = 13 - mFixedPacks.size() * 3;  // 立牌数最大值（不包括和牌）
        int cnt = mStandingTileButtons.size();
        MarginLayoutParams mlp = (MarginLayoutParams)mEmptyButton.getLayoutParams();
        mlp.leftMargin = (cnt < maxCnt) ? IMG_WIDTH * cnt : IMG_WIDTH * maxCnt + IMG_GAP;
    }

    private void refreshHighlightPos() {
        int maxCnt = 13 - mFixedPacks.size() * 3;  // 立牌数最大值（不包括和牌）
        MarginLayoutParams mlp = (MarginLayoutParams)mHighlightBox.getLayoutParams();
        mlp.leftMargin = (mCurrentIdx < maxCnt) ? IMG_WIDTH * mCurrentIdx : IMG_WIDTH * maxCnt + IMG_GAP;
        mHighlightBox.bringToFront();
        if (mOnIndexChangedListener != null) {
            mOnIndexChangedListener.onIndexChanged(this);
        }
    }

    public boolean canChow(int meldedIdx) {
        if (mCurrentIdx >= mStandingTileButtons.size()) return false;
        int maxCnt = 13 - mFixedPacks.size() * 3;  // 立牌数最大值（不包括和牌）
        if (mCurrentIdx == maxCnt) return false;  // 不允许对和牌张进行副露
        int tile = (int)mStandingTileButtons.get(mCurrentIdx).getTag();

        // meldedIdx == 0: _XX 23吃1 tile, tile+1, tile+2
        // meldedIdx == 1: X_X 13吃2 tile-1, tile, tile+1
        // meldedIdx == 2: XX_ 12吃3 tile-2, tile-1, tile
        return (!(tile > 0x40 && tile < 0x48)
                && mStandingTilesTable[tile - meldedIdx] > 0
                && mStandingTilesTable[tile - meldedIdx + 1] > 0
                && mStandingTilesTable[tile - meldedIdx + 2] > 0);
    }

    public boolean canPung() {
        if (mCurrentIdx >= mStandingTileButtons.size()) return false;
        int maxCnt = 13 - mFixedPacks.size() * 3;  // 立牌数最大值（不包括和牌）
        if (mCurrentIdx == maxCnt) return false;  // 不允许对和牌张进行副露
        int tile = (int)mStandingTileButtons.get(mCurrentIdx).getTag();
        return mStandingTilesTable[tile] >= 3;
    }

    public boolean canDKong() {
        if (mCurrentIdx >= mStandingTileButtons.size()) return false;
        int maxCnt = 13 - mFixedPacks.size() * 3;  // 立牌数最大值（不包括和牌）
        if (mCurrentIdx == maxCnt) return false;  // 不允许对和牌张进行副露
        int tile = (int)mStandingTileButtons.get(mCurrentIdx).getTag();
        return mStandingTilesTable[tile] >= 4;
    }

    public boolean canPKong() {
        if (mCurrentIdx >= mStandingTileButtons.size()) return false;
        int maxCnt = 13 - mFixedPacks.size() * 3;  // 立牌数最大值（不包括和牌）
        if (mCurrentIdx == maxCnt) return false;  // 不允许对和牌张进行副露
        int tile = (int)mStandingTileButtons.get(mCurrentIdx).getTag();
        for (int i = 0, l = mFixedPacks.size(); i < l; ++i) {
            int pack = mFixedPacks.get(i);
            if (Mahjong.packType(pack) == Mahjong.PACK_TYPE_PUNG && Mahjong.packTile(pack) == tile) {
                return true;
            }
        }
        return false;
    }

    private int indexOfStandingTile(int tile, int startIdx) {
        for (int i = startIdx, size = mStandingTileButtons.size(); i < size; ++i) {
            if ((int)mStandingTileButtons.get(i).getTag() == tile) {
                return i;
            }
        }
        return -1;
    }

    public boolean makeFixedChowPack(int meldedIdx) {
        if (!canChow(meldedIdx)) return false;

        // meldedIdx == 0: _XX 23吃1 tile+1
        // meldedIdx == 1: X_X 13吃2 tile+0
        // meldedIdx == 2: XX_ 12吃3 tile-1
        int tile = (int)mStandingTileButtons.get(mCurrentIdx).getTag();
        int pack = Mahjong.makePack(meldedIdx + 1, Mahjong.PACK_TYPE_CHOW, tile - meldedIdx + 1);
        mFixedPacks.add(pack);

        // 移除手牌中已经副露的3张牌
        for (int i = 0; i < 3; ++i) {
            int idx = indexOfStandingTile(tile - meldedIdx + i, 0);
            if (idx != -1) {
                ImageButton img = mStandingTileButtons.remove(idx);
                ((ViewGroup)img.getParent()).removeView(img);
                --mStandingTilesTable[tile - meldedIdx + i];
            }
        }

        addFixedChowPack(tile, meldedIdx);
        refreshStandingTiles();

        return true;
    }

    // 计算刻子需要横放的是哪张下标
    private int calcPungHzIdx(int tile) {
        ArrayList<Integer> indexTable = new ArrayList<>();
        for (int i = 0, size = mStandingTileButtons.size(); i < size; ++i) {
            if ((int)mStandingTileButtons.get(i).getTag() == tile) {
                indexTable.add(i);
            }
        }
        return indexTable.indexOf(mCurrentIdx);
    }

    public boolean makeFixedPungPack() {
        if (!canPung()) return false;

        int tile = (int)mStandingTileButtons.get(mCurrentIdx).getTag();
        int hzIdx = Math.min(calcPungHzIdx(tile), 2);
        int pack = Mahjong.makePack(hzIdx + 1, Mahjong.PACK_TYPE_PUNG, tile);
        mFixedPacks.add(pack);

        // 移除手牌中已经副露的3张牌
        for (int i = 0, hint = 0; i < 3; ++i) {
            int idx = indexOfStandingTile(tile, hint);
            if (idx != -1) {
                ImageButton img = mStandingTileButtons.remove(idx);
                ((ViewGroup)img.getParent()).removeView(img);
                --mStandingTilesTable[tile];
                hint = idx;
            }
        }

        addFixedPungPack(tile, hzIdx);
        refreshStandingTiles();

        return true;
    }

    public boolean makeFixedMKongPack() {
        return makeFixedDKongPack() || promoteFixedPungPack();
    }

    private boolean makeFixedDKongPack() {
        if (!canDKong()) return false;

        int tile = (int)mStandingTileButtons.get(mCurrentIdx).getTag();
        int hzIdx = calcPungHzIdx(tile);
        int pack = Mahjong.makePack(Math.min(hzIdx + 1, 3), Mahjong.PACK_TYPE_KONG, tile);
        mFixedPacks.add(pack);

        // 移除手牌中已经副露的4张牌
        for (int i = 0, hint = 0; i < 4; ++i) {
            int idx = indexOfStandingTile(tile, hint);
            if (idx != -1) {
                ImageButton img = mStandingTileButtons.remove(idx);
                ((ViewGroup)img.getParent()).removeView(img);
                --mStandingTilesTable[tile];
                hint = idx;
            }
        }

        addFixedMKongPack(tile, hzIdx);
        refreshStandingTiles();

        return true;
    }

    private boolean promoteFixedPungPack() {
        if (!canPKong()) return false;

        int tile = (int)mStandingTileButtons.get(mCurrentIdx).getTag();
        for (int i = 0, size = mFixedPacks.size(); i < size; ++i) {
            int pack = mFixedPacks.get(i);
            if (Mahjong.packType(pack) == Mahjong.PACK_TYPE_PUNG && Mahjong.packTile(pack) == tile) {
                mFixedPacks.set(i, Mahjong.promotePungToKong(pack));

                // 移除手牌中已经副露的牌
                int idx = indexOfStandingTile(tile, 0);
                if (idx != -1) {
                    ImageButton img = mStandingTileButtons.remove(idx);
                    ((ViewGroup)img.getParent()).removeView(img);
                    --mStandingTilesTable[tile];
                }

                promoteFixedPungPackToKongPack(tile, i);
                refreshStandingTiles();

                return true;
            }

        }

        return false;
    }

    public boolean makeFixedCKongPack() {
        if (!canDKong()) return false;

        int tile = (int)mStandingTileButtons.get(mCurrentIdx).getTag();
        int pack = Mahjong.makePack(0, Mahjong.PACK_TYPE_KONG, tile);
        mFixedPacks.add(pack);

        // 移除手牌中已经副露的4张牌
        for (int i = 0, hint = 0; i < 4; ++i) {
            int idx = indexOfStandingTile(tile, hint);
            if (idx != -1) {
                ImageButton img = mStandingTileButtons.remove(idx);
                ((ViewGroup)img.getParent()).removeView(img);
                --mStandingTilesTable[tile];
                hint = idx;
            }
        }

        addFixedCKongPack(tile);
        refreshStandingTiles();

        return true;
    }

    private void addFixedChowPack(int tile, int meldedIdx) {
        int offsetX = mFixedPacks.size() > 1 ? IMG_GAP : 0;
        int startX = mFixedWidth;

        // 超过宽度则需要缩放
        mFixedWidth += IMG_HEIGHT + IMG_WIDTH * 2 + offsetX;
        mFixedRoot.getLayoutParams().width = mFixedWidth;
        if (mFixedWidth < DZN_WIDTH) {
            mFixedRoot.setScaleX(1.0f);
            mFixedRoot.setScaleY(1.0f);
        } else {
            float s = (float)DZN_WIDTH / (float)mFixedWidth;
            mFixedRoot.setScaleX(s);
            mFixedRoot.setScaleY(s);
        }
        ((MarginLayoutParams)mFixedRoot.getLayoutParams()).leftMargin = (DZN_WIDTH - mFixedWidth) / 2;

        // 牌图
        int[] src = {0, 0, 0};
        switch (meldedIdx) {
            default: {
                src[0] = Utils.TILE_IMG_SRC[Mahjong.tileIdx(tile)];
                src[1] = Utils.TILE_IMG_SRC[Mahjong.tileIdx(tile + 1)];
                src[2] = Utils.TILE_IMG_SRC[Mahjong.tileIdx(tile + 2)];
                break;
            }
            case 1: {
                src[0] = Utils.TILE_IMG_SRC[Mahjong.tileIdx(tile)];
                src[1] = Utils.TILE_IMG_SRC[Mahjong.tileIdx(tile - 1)];
                src[2] = Utils.TILE_IMG_SRC[Mahjong.tileIdx(tile + 1)];
                break;
            }
            case 2: {
                src[0] = Utils.TILE_IMG_SRC[Mahjong.tileIdx(tile)];
                src[1] = Utils.TILE_IMG_SRC[Mahjong.tileIdx(tile - 2)];
                src[2] = Utils.TILE_IMG_SRC[Mahjong.tileIdx(tile - 1)];
                break;
            }
        }

        // 位置
        int offsetY = IMG_WIDTH * 2 - IMG_HEIGHT;
        int[] xPos = {IMG_SIZE_DELTA_HALF, IMG_HEIGHT, IMG_HEIGHT + IMG_WIDTH};
        int[] yPos = {offsetY + IMG_SIZE_DELTA_HALF, offsetY, offsetY};

        // 创建并设置
        for (int i = 0; i < 3; ++i) {
            ImageView img = new ImageView(getContext());
            img.setImageResource(src[i]);
            MarginLayoutParams mlp = new MarginLayoutParams(IMG_WIDTH, IMG_HEIGHT);
            mlp.leftMargin = offsetX + startX + xPos[i];
            mlp.topMargin = yPos[i];
            if (i == 0) {
                mlp.bottomMargin = -IMG_SIZE_DELTA;  // NOTE: 重要！否则会自动缩放
                img.setRotation(-90.0f);
            }
            img.setLayoutParams(mlp);
            mFixedRoot.addView(img);
        }
    }

    private void addFixedPungPack(int tile, int hzIdx) {
        int offsetX = mFixedPacks.size() > 1 ? IMG_GAP : 0;
        int startX = mFixedWidth;

        // 超过宽度则需要缩放
        mFixedWidth += IMG_HEIGHT + IMG_WIDTH * 2 + offsetX;
        mFixedRoot.getLayoutParams().width = mFixedWidth;
        if (mFixedWidth < DZN_WIDTH) {
            mFixedRoot.setScaleX(1.0f);
            mFixedRoot.setScaleY(1.0f);
        } else {
            float s = (float)DZN_WIDTH / (float)mFixedWidth;
            mFixedRoot.setScaleX(s);
            mFixedRoot.setScaleY(s);
        }
        ((MarginLayoutParams)mFixedRoot.getLayoutParams()).leftMargin = (DZN_WIDTH - mFixedWidth) / 2;

        // 位置
        int offsetY = IMG_WIDTH * 2 - IMG_HEIGHT;
        int[] xPos = {0, 0, 0};
        int[] yPos = {offsetY, offsetY, offsetY};
        switch (hzIdx) {
            default: {
                xPos[0] = IMG_SIZE_DELTA_HALF;
                xPos[1] = IMG_HEIGHT;
                xPos[2] = IMG_HEIGHT + IMG_WIDTH;
                yPos[0] = offsetY + IMG_SIZE_DELTA_HALF;
                break;
            }
            case 1: {
                xPos[1] = IMG_WIDTH + IMG_SIZE_DELTA_HALF;
                xPos[2] = IMG_HEIGHT + IMG_WIDTH;
                yPos[1] = offsetY + IMG_SIZE_DELTA_HALF;
                break;
            }
            case 2: {
                xPos[1] = IMG_WIDTH;
                xPos[2] = IMG_WIDTH * 2 + IMG_SIZE_DELTA_HALF;
                yPos[2] = offsetY + IMG_SIZE_DELTA_HALF;
                break;
            }
        }

        // 创建并设置
        int src = Utils.TILE_IMG_SRC[Mahjong.tileIdx(tile)];
        for (int i = 0; i < 3; ++i) {
            ImageView img = new ImageView(getContext());
            img.setImageResource(src);
            MarginLayoutParams mlp = new MarginLayoutParams(IMG_WIDTH, IMG_HEIGHT);
            mlp.leftMargin = offsetX + startX + xPos[i];
            mlp.topMargin = yPos[i];
            if (i == hzIdx) {
                mlp.bottomMargin = -IMG_SIZE_DELTA;  // NOTE: 重要！否则会自动缩放
                img.setRotation(hzIdx != 2 ? -90.0f : 90.0f);
            }
            img.setLayoutParams(mlp);
            mFixedRoot.addView(img);
        }
    }

    private void addFixedMKongPack(int tile, int hzIdx) {
        int offsetX = mFixedPacks.size() > 1 ? IMG_GAP : 0;
        int startX = mFixedWidth;

        // 超过宽度则需要缩放
        mFixedWidth += IMG_HEIGHT + IMG_WIDTH * 3 + offsetX;
        mFixedRoot.getLayoutParams().width = mFixedWidth;
        if (mFixedWidth < DZN_WIDTH) {
            mFixedRoot.setScaleX(1.0f);
            mFixedRoot.setScaleY(1.0f);
        } else {
            float s = (float)DZN_WIDTH / (float)mFixedWidth;
            mFixedRoot.setScaleX(s);
            mFixedRoot.setScaleY(s);
        }
        ((MarginLayoutParams)mFixedRoot.getLayoutParams()).leftMargin = (DZN_WIDTH - mFixedWidth) / 2;

        // 位置
        int offsetY = IMG_WIDTH * 2 - IMG_HEIGHT;
        int[] xPos = {0, 0, 0, 0};
        int[] yPos = {offsetY, offsetY, offsetY, offsetY};
        switch (hzIdx) {
            default: {
                xPos[0] = IMG_SIZE_DELTA_HALF;
                xPos[1] = IMG_HEIGHT;
                xPos[2] = IMG_HEIGHT + IMG_WIDTH;
                xPos[3] = IMG_HEIGHT + IMG_WIDTH * 2;
                yPos[0] = offsetY + IMG_SIZE_DELTA_HALF;
                break;
            }
            case 1: case 2: {
                xPos[1] = IMG_WIDTH + IMG_SIZE_DELTA_HALF;
                xPos[2] = IMG_HEIGHT + IMG_WIDTH;
                xPos[3] = IMG_HEIGHT + IMG_WIDTH * 2;
                yPos[1] = offsetY + IMG_SIZE_DELTA_HALF;
                break;
            }
            case 3: {
                xPos[1] = IMG_WIDTH;
                xPos[2] = IMG_WIDTH * 2;
                xPos[3] = IMG_WIDTH * 3 + IMG_SIZE_DELTA_HALF;
                yPos[3] = offsetY + IMG_SIZE_DELTA_HALF;
                break;
            }
        }

        // 创建并设置
        int src = Utils.TILE_IMG_SRC[Mahjong.tileIdx(tile)];
        if (hzIdx == 2) hzIdx = 1;
        for (int i = 0; i < 4; ++i) {
            ImageView img = new ImageView(getContext());
            img.setImageResource(src);
            MarginLayoutParams mlp = new MarginLayoutParams(IMG_WIDTH, IMG_HEIGHT);
            mlp.leftMargin = offsetX + startX + xPos[i];
            mlp.topMargin = yPos[i];
            if (i == hzIdx) {
                mlp.bottomMargin = -IMG_SIZE_DELTA;  // NOTE: 重要！否则会自动缩放
                img.setRotation(hzIdx != 3 ? -90.0f : 90.0f);
            }
            img.setLayoutParams(mlp);
            mFixedRoot.addView(img);
        }
    }

    private void addFixedCKongPack(int tile) {
        int offsetX = mFixedPacks.size() > 1 ? IMG_GAP : 0;
        int startX = mFixedWidth;

        // 超过宽度则需要缩放
        mFixedWidth += IMG_WIDTH * 4 + offsetX;
        mFixedRoot.getLayoutParams().width = mFixedWidth;
        if (mFixedWidth < DZN_WIDTH) {
            mFixedRoot.setScaleX(1.0f);
            mFixedRoot.setScaleY(1.0f);
        } else {
            float s = (float)DZN_WIDTH / (float)mFixedWidth;
            mFixedRoot.setScaleX(s);
            mFixedRoot.setScaleY(s);
        }
        ((MarginLayoutParams)mFixedRoot.getLayoutParams()).leftMargin = (DZN_WIDTH - mFixedWidth) / 2;

        // 牌图
        int idx = Mahjong.tileIdx(tile);
        int[] src = {R.drawable.ic_bg, Utils.TILE_IMG_SRC[idx], Utils.TILE_IMG_SRC[idx], R.drawable.ic_bg};

        // 位置
        int offsetY = IMG_WIDTH * 2 - IMG_HEIGHT;

        // 创建并设置
        for (int i = 0; i < 4; ++i) {
            ImageView img = new ImageView(getContext());
            img.setImageResource(src[i]);
            MarginLayoutParams mlp = new MarginLayoutParams(IMG_WIDTH, IMG_HEIGHT);
            mlp.leftMargin = offsetX + startX + IMG_WIDTH * i;
            mlp.topMargin = offsetY;
            img.setLayoutParams(mlp);
            mFixedRoot.addView(img);
        }
    }

    private void promoteFixedPungPackToKongPack(int tile, int idx) {
        // 迭代之前的副露，累加应该添加的位置
        int startX = 0;
        for (int i = 0; i < idx; ++i) {
            int pack = mFixedPacks.get(i);
            switch (Mahjong.packType(pack)) {
                case Mahjong.PACK_TYPE_CHOW:
                case Mahjong.PACK_TYPE_PUNG: {
                    startX += IMG_HEIGHT + IMG_WIDTH * 2;
                    break;
                }
                case Mahjong.PACK_TYPE_KONG: {
                    if (Mahjong.packIsMelded(pack)) {
                        startX += IMG_HEIGHT + IMG_WIDTH * 2;
                        if (!Mahjong.packIsPKong(pack)) startX += IMG_WIDTH;
                    } else {
                        startX += IMG_WIDTH * 4;
                    }
                    break;
                }
            }
            startX += IMG_GAP;
        }

        int hzIdx = Mahjong.packOffer(mFixedPacks.get(idx)) - 1;
        startX += hzIdx * IMG_WIDTH;

        ImageView img = new ImageView(getContext());
        img.setImageResource(Utils.TILE_IMG_SRC[Mahjong.tileIdx(tile)]);
        MarginLayoutParams mlp = new MarginLayoutParams(IMG_WIDTH, IMG_HEIGHT);
        mlp.leftMargin = startX + IMG_SIZE_DELTA_HALF;
        mlp.topMargin = -IMG_SIZE_DELTA_HALF;
        mlp.bottomMargin = -IMG_SIZE_DELTA;  // NOTE: 重要！否则会自动缩放
        img.setRotation(hzIdx != 2 ? -90.0f : 90.0f);
        img.setLayoutParams(mlp);
        mFixedRoot.addView(img);
    }

    // 重排立牌位置
    private void refreshStandingTilesPos() {
        int maxCnt = 13 - mFixedPacks.size() * 3;  // 立牌数最大值（不包括和牌）
        int width = IMG_WIDTH * (maxCnt + 1) + IMG_GAP;
        MarginLayoutParams mlp = (MarginLayoutParams)mStandingRoot.getLayoutParams();
        mlp.width = width;
        mlp.leftMargin = (DZN_WIDTH - width) / 2;

        for (int i = 0, l = mStandingTileButtons.size(); i < l; ++i) {
            ((MarginLayoutParams)mStandingTileButtons.get(i).getLayoutParams()).leftMargin =
                    i < maxCnt ? IMG_WIDTH * i : IMG_WIDTH * maxCnt + IMG_GAP;
        }
    }

    // 刷新立牌
    private void refreshStandingTiles() {
        refreshStandingTilesPos();

        int prevIdx = mCurrentIdx;
        mCurrentIdx = mStandingTileButtons.size();
        if (prevIdx < mCurrentIdx) {
            mCurrentIdx = prevIdx;
        }

        int maxCnt = 13 - mFixedPacks.size() * 3;  // 立牌数最大值（不包括和牌）
        if (mCurrentIdx > maxCnt) {
            mCurrentIdx = maxCnt;
        }

        refreshEmptyWidgetPos();
        refreshHighlightPos();
    }

    public void sortStandingTiles() {
        if (mStandingTileButtons.isEmpty()) return;

        // 最后一张不参与排序，如果有，先暂存
        int maxCnt = 13 - mFixedPacks.size() * 3;  // 立牌数最大值（不包括和牌）
        ImageButton last = null;
        if (mStandingTileButtons.size() >= maxCnt + 1) {
            last = mStandingTileButtons.remove(mStandingTileButtons.size() - 1);
        }
        mStandingTileButtons.sort(Comparator.comparingInt(a -> (int) a.getTag()));

        if (last != null) {
            mStandingTileButtons.add(last);
        }

        refreshStandingTiles();

        mHighlightBox.bringToFront();
        if (mOnIndexChangedListener != null) {
            mOnIndexChangedListener.onIndexChanged(this);
        }
    }

    public boolean hasKong() {
        for (int pack : mFixedPacks) {
            if (Mahjong.packType(pack) == Mahjong.PACK_TYPE_KONG) {
                return true;
            }
        }
        return false;
    }

    // 获取和牌张详情 0~7位牌张，8~15位出现的总次数，16~23位立牌中出现次数
    public int getWinTileDetail() {
        int wt = getServingTile();
        if (wt == 0) return 0;

        int utc = mUsedTilesTable[wt];
        int stc = mStandingTilesTable[wt];
        return wt | (utc << 8) | (stc << 16);
    }

}
