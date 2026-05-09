package net.tziakcha.chineseofficialmahjonghelper;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CompoundButton;
import android.widget.ListPopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import net.tziakcha.chineseofficialmahjonghelper.widget.HandTilesLayout;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

public final class Utils {

    public static void printDebugStackTrace(Exception e) {
        if (BuildConfig.DEBUG) {
            e.printStackTrace();
        }
    }

    public static String copyAssetToInternalStorage(Context context, String assetPath) {
        final int lastSlashIndex = assetPath.lastIndexOf('/');
        String dirPath;
        String fileName;
        if (lastSlashIndex != -1) {
            dirPath = assetPath.substring(0, lastSlashIndex);
            fileName = assetPath.substring(lastSlashIndex + 1);
        } else {
            dirPath = "";
            fileName = assetPath;
        }

        File outputPath = new File(context.getFilesDir(), dirPath);
        if (!outputPath.exists()) {
            if (!outputPath.mkdirs()) {
                return null;
            }
        }

        File outputFile = new File(outputPath, fileName);
        if (outputFile.exists()) {
            return outputFile.getAbsolutePath();
        }

        FileOutputStream os = null;
        try {
            InputStream is = context.getAssets().open(assetPath);
            os = new FileOutputStream(outputFile);

            byte[] buffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = is.read(buffer)) != -1) {
                os.write(buffer, 0, bytesRead);
            }

            os.flush();
            return outputFile.getAbsolutePath();
        } catch (IOException e) {
            return null;
        } finally {
            if (os != null) {
                try {
                    os.close();
                } catch (IOException e) {
                    printDebugStackTrace(e);
                }
            }
        }
    }

    public static String getStringFromAsset(Context context, String assetPath) {
        try {
            InputStream is = context.getAssets().open(assetPath);
            BufferedReader reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8));
            StringBuilder str = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                str.append(line).append('\n');
            }
            reader.close();
            return str.toString();
        } catch (IOException e) {
            printDebugStackTrace(e);
            return "";
        }
    }

    public static String getStringFromFile(File file) {
        try {
            FileInputStream is = new FileInputStream(file);
            BufferedReader reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8));
            StringBuilder str = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                str.append(line).append('\n');
            }
            reader.close();
            return str.toString();
        } catch (IOException e) {
            printDebugStackTrace(e);
            return "";
        }
    }

    public static String getStringFromFile(Context context, String dirName, String fileName) {
        File dir = context.getDir(dirName, Context.MODE_PRIVATE);
        File file = new File(dir, fileName);
        if (file.exists()) {
            return getStringFromFile(file);
        }
        return "";
    }

    public static boolean saveStringToFile(File file, String str) {
        try {
            FileOutputStream os = new FileOutputStream(file);
            os.write(str.getBytes(StandardCharsets.UTF_8));
            os.close();
            return true;
        } catch (IOException e) {
            printDebugStackTrace(e);
            return false;
        }
    }

    public static boolean saveStringToFile(Context context, String dirName, String fileName, String str) {
        File dir = context.getDir(dirName, Context.MODE_PRIVATE);
        return saveStringToFile(new File(dir, fileName), str);
    }

    public static int[] TILE_IMG_SRC = {
            R.drawable.ic_m1, R.drawable.ic_m2, R.drawable.ic_m3, R.drawable.ic_m4, R.drawable.ic_m5, R.drawable.ic_m6, R.drawable.ic_m7, R.drawable.ic_m8, R.drawable.ic_m9,
            R.drawable.ic_s1, R.drawable.ic_s2, R.drawable.ic_s3, R.drawable.ic_s4, R.drawable.ic_s5, R.drawable.ic_s6, R.drawable.ic_s7, R.drawable.ic_s8, R.drawable.ic_s9,
            R.drawable.ic_p1, R.drawable.ic_p2, R.drawable.ic_p3, R.drawable.ic_p4, R.drawable.ic_p5, R.drawable.ic_p6, R.drawable.ic_p7, R.drawable.ic_p8, R.drawable.ic_p9,
            R.drawable.ic_w1, R.drawable.ic_w2, R.drawable.ic_w3, R.drawable.ic_w4, R.drawable.ic_d1, R.drawable.ic_d2, R.drawable.ic_d3
    };

    public static HandTilesLayout createAndLayoutHandTiles(Context context, RelativeLayout outsizeRl) {
        // 适配手牌部分
        DisplayMetrics metrics = context.getResources().getDisplayMetrics();
        final float padding = context.getResources().getDimension(R.dimen.dp5);

        // 按宽度适配
        final float scale = ((float)metrics.widthPixels - padding * 2) / (float)HandTilesLayout.DZN_WIDTH;

        // 需要的高度
        int targetHeight = (int)Math.ceil(HandTilesLayout.DZN_HEIGHT * scale + padding * 2);

        // 设置外层尺寸
        ViewGroup.LayoutParams lp = outsizeRl.getLayoutParams();
        lp.width = metrics.widthPixels;
        lp.height = targetHeight;

        // 内层缩放
        HandTilesLayout htl = new HandTilesLayout(context);
        outsizeRl.addView(htl);

        RelativeLayout.LayoutParams rlp = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT);
        rlp.leftMargin = (int)padding;
        rlp.topMargin = (int)padding;
        htl.setLayoutParams(rlp);

        htl.setPivotX(0.0f);
        htl.setPivotY(0.0f);
        htl.setScaleX(scale);
        htl.setScaleY(scale);

        return htl;
    }

    public static ListPopupWindow createPopupMenu(Context context, View anchorView, final String[] items) {
        DisplayMetrics metrics = context.getResources().getDisplayMetrics();

        ListPopupWindow popup = new ListPopupWindow(context);
        popup.setAnchorView(anchorView);
        popup.setWidth(metrics.widthPixels * 2 / 5);
        popup.setHeight(ViewGroup.LayoutParams.WRAP_CONTENT);
        popup.setModal(true);
        popup.setAdapter(new BaseAdapter() {
            @Override
            public int getCount() {
                return items.length;
            }

            @Override
            public Object getItem(int i) {
                return null;
            }

            @Override
            public long getItemId(int i) {
                return 0;
            }

            @Override
            public View getView(int i, View view, ViewGroup viewGroup) {
                if (view == null) {
                    view = View.inflate(context, R.layout.popup_window_item_layout, null);
                    ((TextView)view.findViewById(R.id.pwi_txt)).setText(items[i]);
                }
                view.findViewById(R.id.pwi_div).setVisibility(i != 0 ? View.VISIBLE : View.GONE);
                return view;
            }
        });

        return popup;
    }

    public static void adaptCompoundButton(CompoundButton button, int side) {
        Drawable drawable = button.getButtonDrawable();
        if (drawable != null) {
            button.setButtonDrawable(null);
            drawable.setBounds(0, 0, side, side);
            button.setCompoundDrawables(drawable, null, null, null);
            //button.setBackground(null);
        }
    }

    private static void showToast(Context context, String str, int duration) {
        View contentView = View.inflate(context, R.layout.common_toast_layout, null);
        ((TextView)contentView.findViewById(R.id.chl_txt)).setText(str);

        Toast toast = new Toast(context);
        toast.setDuration(duration);
        toast.setView(contentView);
        toast.show();
    }

    public static void showToastShort(Context context, String str) {
        showToast(context, str, Toast.LENGTH_SHORT);
    }

    public static void showToastLong(Context context, String str) {
        showToast(context, str, Toast.LENGTH_LONG);
    }

}
