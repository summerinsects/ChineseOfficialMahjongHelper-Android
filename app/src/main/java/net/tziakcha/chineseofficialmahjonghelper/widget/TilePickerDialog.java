package net.tziakcha.chineseofficialmahjonghelper.widget;

import android.app.AlertDialog;
import android.content.Context;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;

import net.tziakcha.chineseofficialmahjonghelper.Mahjong;
import net.tziakcha.chineseofficialmahjonghelper.R;

public class TilePickerDialog extends AlertDialog {
    public static int BUTTON_CANCEL = 0;
    public static int BUTTON_CONFIRM = 1;

    public interface OnButtonClickListener {
        void onClick(TilePickerDialog dialog, int which);
    }

    private TilePickerLayout mTilePickerLayout;
    private final Mahjong.HandTiles mHandTiles;
    private final OnButtonClickListener mOnButtonClickListener;

    public TilePickerDialog(@NonNull Context context, Mahjong.HandTiles handTiles, OnButtonClickListener listener) {
        super(context);
        mHandTiles = handTiles;
        mOnButtonClickListener = listener;
    }

    @Override
    public void show() {
        super.show();

        setCancelable(false);

        Context context = getContext();
        View contentView = View.inflate(context, R.layout.tile_picker_layout, null);

        ((TextView)contentView.findViewById(R.id.dhl_txt_title)).setText("输入手牌");

        TilePickerLayout tpl = new TilePickerLayout(getContext());
        ((RelativeLayout)contentView.findViewById(R.id.ptl_rl_picker)).addView(tpl);
        mTilePickerLayout = tpl;

        Button button;
        button = contentView.findViewById(R.id.dfl_btn_pos);
        button.setText("确定");
        button.setOnClickListener(view ->
                mOnButtonClickListener.onClick(this, BUTTON_CONFIRM));
        button = contentView.findViewById(R.id.dfl_btn_neg);
        button.setText("取消");
        button.setOnClickListener(view ->
                mOnButtonClickListener.onClick(this, BUTTON_CANCEL));

        tpl.setData(mHandTiles.fp, mHandTiles.st, mHandTiles.wt);

        setContentView(contentView);

        Window window = getWindow();
        if (window != null) {
            window.setBackgroundDrawableResource(android.R.color.transparent);
            window.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.MATCH_PARENT);

            // NOTE: 部分手机状态栏会黑变，需要通过以下代码改变
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
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

    public Mahjong.HandTiles getData() {
        return mTilePickerLayout.getData();
    }

}
