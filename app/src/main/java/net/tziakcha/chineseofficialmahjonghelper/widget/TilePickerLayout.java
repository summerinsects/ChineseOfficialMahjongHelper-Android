package net.tziakcha.chineseofficialmahjonghelper.widget;

import android.animation.AnimatorInflater;
import android.content.Context;
import android.graphics.Color;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.drawable.Drawable;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import androidx.appcompat.content.res.AppCompatResources;

import net.tziakcha.chineseofficialmahjonghelper.Mahjong;
import net.tziakcha.chineseofficialmahjonghelper.R;
import net.tziakcha.chineseofficialmahjonghelper.Utils;

public class TilePickerLayout extends RelativeLayout {

    public interface OnClearButtonListener {
        void onClearButton(TilePickerLayout tpl);
    }

    public interface OnHandChangedListener {
        void onPacksChanged(TilePickerLayout tpl);
        void onTileChanged(TilePickerLayout tpl);
    }

    private HandTilesLayout mHandTilesLayout;
    private final ImageButton[] mTileButtons = new ImageButton[34];
    private final Button[] mActionButtons = new Button[8];
    private OnClearButtonListener mOnClearButtonListener;
    private OnHandChangedListener mOnHandChangedListener;

    private static final int GAP = 20;

    public TilePickerLayout(Context context) {
        super(context);
        init(context);
    }

    private void init(Context context) {
        DisplayMetrics metrics = getResources().getDisplayMetrics();
        int dznWidth = metrics.widthPixels;

        // 按手牌宽度适配
        float scale = (dznWidth - GAP * 2.0f) / HandTilesLayout.DZN_WIDTH;
        int imgWidth = (int)Math.ceil(HandTilesLayout.IMG_WIDTH * scale);
        int imgHeight = (int)Math.ceil(HandTilesLayout.IMG_HEIGHT * scale);
        int dznHeight = imgHeight * 4 + (int)Math.ceil(HandTilesLayout.DZN_HEIGHT * scale) + GAP * 3;

        setLayoutParams(new MarginLayoutParams(dznWidth, dznHeight));
        //setBackgroundColor(Color.GRAY);  // 测试范围用

        // 手牌部分
        HandTilesLayout htl = new HandTilesLayout(context);
        addView(htl);
        htl.setPivotX(0.0f);
        htl.setPivotY(0.0f);
        htl.setScaleX(scale);
        htl.setScaleY(scale);
        ((MarginLayoutParams)htl.getLayoutParams()).setMargins(GAP, imgHeight * 4 + GAP * 2, 0, 0);
        htl.setOnIndexChangedListener(htl0 -> refreshActionButtons());
        mHandTilesLayout = htl;

        // 牌张选择按钮
        for (int i = 0; i < 34; ++i) {
            MarginLayoutParams mlp = new MarginLayoutParams(imgWidth, imgHeight);
            mlp.setMargins(i % 9 * imgWidth + GAP, i / 9 * imgHeight + GAP, 0, 0);

            ImageButton img = new ImageButton(context);
            Drawable drawable = AppCompatResources.getDrawable(context, Utils.TILE_IMG_SRC[i]);
            if (drawable != null) {
                img.setBackground(drawable.mutate());
            }
            img.setLayoutParams(mlp);
            img.setStateListAnimator(AnimatorInflater.loadStateListAnimator(context, R.animator.button_scale_animator));

            addView(img);

            final int idx = i;
            img.setOnClickListener(view -> onTileBtn(Mahjong.ALL_TILES[idx]));
            mTileButtons[i] = img;
        }

        // 操作按钮部分
        // 尺寸、位置
        int btnWidth = (dznWidth - imgWidth * 9 - GAP * 4) / 2;
        int btnHeight = btnWidth / 2;
        int btnLeft = imgWidth * 9 + GAP * 2;
        int btnLW = btnWidth + GAP;
        int btnLH = (int)((imgHeight - btnHeight) * 4 / 3.0f) + btnHeight;

        String[] btnFace = {
                "吃 _XX", "吃 X_X", "吃 XX_", "排序", "碰", "明杠", "暗杠", "清空"
        };
        for (int i = 0; i < 8; ++i) {
            MarginLayoutParams mlp = new MarginLayoutParams(btnWidth, btnHeight);
            mlp.setMargins(btnLeft + i / 4 * btnLW, GAP + i % 4 * btnLH, 0, 0);

            Button btn = new Button(context);
            btn.setBackgroundResource(R.drawable.btn_square);
            btn.setPadding(0, 0, 0, 0);
            btn.setTextColor(Color.WHITE);
            btn.setTextSize(TypedValue.COMPLEX_UNIT_PX, btnHeight * 0.5f);
            btn.setText(btnFace[i]);
            btn.setLayoutParams(mlp);

            addView(btn);
            btn.setEnabled(false);
            mActionButtons[i] = btn;
        }
        mActionButtons[0].setOnClickListener(view -> {
            int wt = mHandTilesLayout.getServingTile();
            boolean res = mHandTilesLayout.makeFixedChowPack(0);
            if (res) {
                notifyHandChanged(wt);
            }
        });
        mActionButtons[1].setOnClickListener(view -> {
            int wt = mHandTilesLayout.getServingTile();
            boolean res = mHandTilesLayout.makeFixedChowPack(1);
            if (res) {
                notifyHandChanged(wt);
            }
        });
        mActionButtons[2].setOnClickListener(view -> {
            int wt = mHandTilesLayout.getServingTile();
            boolean res = mHandTilesLayout.makeFixedChowPack(2);
            if (res) {
                notifyHandChanged(wt);
            }
        });
        mActionButtons[3].setOnClickListener(view -> sortStandingTiles());
        mActionButtons[3].setEnabled(true);
        mActionButtons[4].setOnClickListener(view -> {
            int wt = mHandTilesLayout.getServingTile();
            boolean res = mHandTilesLayout.makeFixedPungPack();
            if (res) {
                notifyHandChanged(wt);
            }
        });
        mActionButtons[5].setOnClickListener(view -> {
            int wt = mHandTilesLayout.getServingTile();
            boolean res = mHandTilesLayout.makeFixedMKongPack();
            if (res) {
                notifyHandChanged(wt);
            }
        });
        mActionButtons[6].setOnClickListener(view -> {
            int wt = mHandTilesLayout.getServingTile();
            boolean res = mHandTilesLayout.makeFixedCKongPack();
            if (res) {
                notifyHandChanged(wt);
            }
        });
        mActionButtons[7].setOnClickListener(view -> reset());
        mActionButtons[7].setEnabled(true);
    }

    public void setOnClearButtonListener(OnClearButtonListener listener) {
        mOnClearButtonListener = listener;
    }

    public void setOnHandChangedListener(OnHandChangedListener listener) {
        mOnHandChangedListener = listener;
    }

    public Mahjong.HandTiles getData() {
        return mHandTilesLayout.getData();
    }

    public void setData(int[] fp, int[] st, int wt) {
        mHandTilesLayout.setData(fp, st, wt);
        for (int i = 0; i < 34; ++i) {
            refreshTilesButton(Mahjong.ALL_TILES[i]);
        }
        refreshActionButtons();

        if (mOnHandChangedListener != null) {
            mOnHandChangedListener.onPacksChanged(this);
            mOnHandChangedListener.onTileChanged(this);
        }
    }

    public boolean hasKong() {
        return mHandTilesLayout.hasKong();
    }

    // 获取和牌张详情 0~7位牌张，8~15位出现的总次数，16~23位立牌中出现次数
    public int getWinTileDetail() {
        return mHandTilesLayout.getWinTileDetail();
    }

    private void onTileBtn(int tile) {
        int prevTile = mHandTilesLayout.putTile(tile);
        if (prevTile != 0 && prevTile != tile) {
            // 如果是替换牌，则会删了一张旧的牌
            refreshTilesButton(prevTile);
        }
        refreshTilesButton(tile);

        if (mOnHandChangedListener != null) {
            mOnHandChangedListener.onTileChanged(this);
        }
    }

    // 刷新选牌按钮
    private void refreshTilesButton(int tile) {
        // 如果某张牌已经使用了4张，就禁用相应按钮
        int n = mHandTilesLayout.getUsedTileCount(tile);
        ImageButton img = mTileButtons[Mahjong.tileIdx(tile)];
        if (n < 4) {
            img.getBackground().setColorFilter(null);
        } else {
            // 这是整体变灰
            //img.getBackground().setColorFilter(new PorterDuffColorFilter(Color.GRAY, PorterDuff.Mode.MULTIPLY));

            // 这是灰色滤镜
            ColorMatrix matrix = new ColorMatrix();
            matrix.setSaturation(0.0f);
            img.getBackground().setColorFilter(new ColorMatrixColorFilter(matrix));
        }
    }

    private void refreshActionButtons() {
        for (int i = 0; i < 3; ++i) {
            mActionButtons[i].setEnabled(mHandTilesLayout.canChow(i));
        }
        mActionButtons[4].setEnabled(mHandTilesLayout.canPung());
        mActionButtons[5].setEnabled(mHandTilesLayout.canDKong() || mHandTilesLayout.canPKong());
        mActionButtons[6].setEnabled(mHandTilesLayout.canDKong());
    }

    private void sortStandingTiles() {
        mHandTilesLayout.sortStandingTiles();
    }

    private void reset() {
        mHandTilesLayout.reset();

        for (int i = 0; i < 34; ++i) {
            mTileButtons[i].getBackground().setColorFilter(null);
        }
        for (int i = 0; i < 8; ++i) {
            mActionButtons[i].setEnabled(false);
        }
        mActionButtons[3].setEnabled(true);
        mActionButtons[7].setEnabled(true);

        if (mOnClearButtonListener != null) {
            mOnClearButtonListener.onClearButton(this);
        }

        if (mOnHandChangedListener != null) {
            mOnHandChangedListener.onPacksChanged(this);
            mOnHandChangedListener.onTileChanged(this);
        }
    }

    private void notifyHandChanged(int wt) {
        if (mOnHandChangedListener != null) {
            mOnHandChangedListener.onPacksChanged(this);
            if (wt != 0 && wt != mHandTilesLayout.getServingTile()) {
                mOnHandChangedListener.onTileChanged(this);
            }
        }
    }

}
