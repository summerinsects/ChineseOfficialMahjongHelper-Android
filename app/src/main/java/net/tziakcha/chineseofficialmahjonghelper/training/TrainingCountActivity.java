package net.tziakcha.chineseofficialmahjonghelper.training;

import android.annotation.SuppressLint;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ListPopupWindow;
import android.widget.RadioButton;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.ToggleButton;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.core.content.ContextCompat;
import androidx.core.widget.TextViewCompat;

import net.tziakcha.chineseofficialmahjonghelper.Mahjong;
import net.tziakcha.chineseofficialmahjonghelper.R;
import net.tziakcha.chineseofficialmahjonghelper.Utils;
import net.tziakcha.chineseofficialmahjonghelper.widget.HandTilesLayout;
import net.tziakcha.chineseofficialmahjonghelper.widget.LoadingDialog;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

@SuppressLint("SetTextI18n")
public class TrainingCountActivity extends AppCompatActivity {

    private static class PuzzleInfo extends Mahjong.HandTiles {
        public int cond;
        public int period;
        public int seat;
        public int fan;
        public long fan_major;
        public long fan_minor1;
        public int fan_minor2;
    }

    private ListPopupWindow mPopupWindow;
    private HandTilesLayout mHandTilesLayout;
    private RadioButton mCLRadioButton;
    private RadioButton mSDRadioButton;
    private CheckBox mLTCheckBox;
    private final RadioButton[] mPWRadioButton = new RadioButton[4];
    private final RadioButton[] mSWRadioButton = new RadioButton[4];
    private TextView mCountText;
    private TextView mRateText;
    private final ToggleButton[] mFanToggleButtons = new ToggleButton[16];
    private ScrollView mFanScrollView;
    private TrainingHistoryManager mTrainingHistoryManager;
    private boolean mAutoJump = true;
    private boolean mNewPuzzle = false;
    private int mPrevFan = 0;
    private boolean mCorrect = false;
    private PuzzleInfo mPuzzle;
    private int mTotalCount = 0;
    private int mCorrectCount = 0;

    private final Handler mHandler = new Handler(Looper.getMainLooper());
    private SQLiteDatabase mDB;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        View contentView = View.inflate(this, R.layout.training_count_layout, null);
        setContentView(contentView);

        ((TextView)contentView.findViewById(R.id.ab_txt)).setText("算番训练");

        contentView.findViewById(R.id.ab_l_btn).setOnClickListener(view ->
                getOnBackPressedDispatcher().onBackPressed());

        View rightButton = contentView.findViewById(R.id.ab_r_btn);
        mPopupWindow = Utils.createPopupMenu(this, rightButton, new String[]{"更新题库"});
        mPopupWindow.setOnItemClickListener((adapterView, view, position, id) -> {
            requestLatestPuzzles();
            mPopupWindow.dismiss();
        });
        rightButton.setOnClickListener(view -> mPopupWindow.show());

        mHandTilesLayout = Utils.createAndLayoutHandTiles(this,
                contentView.findViewById(R.id.tcl_rl_hand));
        mHandTilesLayout.hidePlaceholder();

        final int dp28 = getResources().getDimensionPixelSize(R.dimen.dp28);

        mCLRadioButton = contentView.findViewById(R.id.tcl_rb_cl);
        mSDRadioButton = contentView.findViewById(R.id.tcl_rb_sd);
        mLTCheckBox = contentView.findViewById(R.id.tcl_cb_lt);
        Utils.adaptCompoundButton(mCLRadioButton, dp28);
        Utils.adaptCompoundButton(mSDRadioButton, dp28);
        Utils.adaptCompoundButton(mLTCheckBox, dp28);

        mPWRadioButton[0] = contentView.findViewById(R.id.tcl_rb_pwe);
        mPWRadioButton[1] = contentView.findViewById(R.id.tcl_rb_pws);
        mPWRadioButton[2] = contentView.findViewById(R.id.tcl_rb_pww);
        mPWRadioButton[3] = contentView.findViewById(R.id.tcl_rb_pwn);

        mSWRadioButton[0] = contentView.findViewById(R.id.tcl_rb_swe);
        mSWRadioButton[1] = contentView.findViewById(R.id.tcl_rb_sws);
        mSWRadioButton[2] = contentView.findViewById(R.id.tcl_rb_sww);
        mSWRadioButton[3] = contentView.findViewById(R.id.tcl_rb_swn);

        for (int i = 0; i < 4; ++i) {
            Utils.adaptCompoundButton(mPWRadioButton[i], dp28);
            Utils.adaptCompoundButton(mSWRadioButton[i], dp28);
        }

        contentView.findViewById(R.id.tcl_btn_skip).setOnClickListener(view -> onSkipButton());
        contentView.findViewById(R.id.tcl_btn_view).setOnClickListener(view -> onAnswerButton());
        mCountText = contentView.findViewById(R.id.tcl_txt_cnt);
        mRateText = contentView.findViewById(R.id.tcl_txt_rate);

        CheckBox checkBox = contentView.findViewById(R.id.tcl_cb_auto);
        checkBox.setOnCheckedChangeListener((view, checked) -> mAutoJump = checked);
        Utils.adaptCompoundButton(checkBox, dp28);

        mTrainingHistoryManager = new TrainingHistoryManager(this,
                contentView.findViewById(R.id.tcl_rv_res));

        final int[] toggleIds = {
                R.id.tcl_tb_f1, R.id.tcl_tb_f2, R.id.tcl_tb_f3, R.id.tcl_tb_f4,
                R.id.tcl_tb_f5, R.id.tcl_tb_f6, R.id.tcl_tb_f7, R.id.tcl_tb_f8,
                R.id.tcl_tb_f9, R.id.tcl_tb_f10, R.id.tcl_tb_f11, R.id.tcl_tb_f12,
                R.id.tcl_tb_f13, R.id.tcl_tb_f14, R.id.tcl_tb_f15, R.id.tcl_tb_f16,
        };
        for (int i = 0; i < 16; ++i) {
            mFanToggleButtons[i] = contentView.findViewById(toggleIds[i]);
        }
        mFanScrollView = contentView.findViewById(R.id.tcl_sv_ans);

        LoadingDialog dialog = new LoadingDialog();
        dialog.show(getSupportFragmentManager(), "LoadingDialog");

        new Thread(() -> {
            String path = Utils.copyAssetToInternalStorage(this, "train/count.db");
            if (path != null) {
                mDB = SQLiteDatabase.openDatabase(path, null, SQLiteDatabase.OPEN_READONLY);
            }
            boolean res = path != null;

            mHandler.post(() -> {
                if (res) {
                    setPuzzle();
                } else {
                    Utils.toastMakeText(TrainingCountActivity.this, "加载题库失败", 1).show();
                }
                dialog.dismiss();
            });
        }).start();
    }

    static final String[] sQueryColumns = {
            "text", "cond", "period", "seat", "fan_pure", "fan_major", "fan_minor2", "fan_minor1"
    };

    private PuzzleInfo generatePuzzle() {
        PuzzleInfo puzzle = new PuzzleInfo();
        if (mDB != null) {
            Cursor cursor = mDB.query("puzzle", sQueryColumns, "", null,
                    "", "", "RANDOM()", "1");
            if (cursor.moveToNext()) {
                String text = cursor.getString(0);
                puzzle.cond = cursor.getInt(1);
                puzzle.period = cursor.getInt(2);
                puzzle.seat = cursor.getInt(3);
                puzzle.fan = cursor.getInt(4);
                puzzle.fan_major = cursor.getInt(5);
                puzzle.fan_minor2 = cursor.getInt(6);
                puzzle.fan_minor1 = cursor.getInt(7);

                Mahjong.parseHandTiles(text, puzzle);
            }
            cursor.close();
        }

        return puzzle;
    }

    private void setPuzzle() {
        PuzzleInfo puzzle = generatePuzzle();
        mHandTilesLayout.setData(puzzle.fp, puzzle.st, puzzle.wt);

        boolean selfDrawn = (puzzle.cond & 1) != 0;
        mCLRadioButton.setChecked(!selfDrawn);
        mSDRadioButton.setChecked(selfDrawn);
        mLTCheckBox.setChecked((puzzle.cond & 2) != 0);

        int pw = puzzle.period >> 2;
        int sw = puzzle.seat;
        for (int i = 0; i < 4; ++i) {
            mPWRadioButton[i].setChecked(pw == i);
            mSWRadioButton[i].setChecked(sw == i);
        }

        for (int i = 0; i < 16; ++i) {
            mFanToggleButtons[i].setOnCheckedChangeListener(null);
            mFanToggleButtons[i].setChecked(false);
            mFanToggleButtons[i].setOnCheckedChangeListener(getFanToggleButtonCallback(i));
        }
        mPrevFan = 0;

        mFanScrollView.setVisibility(View.GONE);

        mPuzzle = puzzle;
        mNewPuzzle = true;
        mCorrect = false;
        mTrainingHistoryManager.addResult();
    }

    @SuppressLint("DefaultLocale")
    private void refreshRate() {
        mCountText.setText(mCorrectCount + "/" + mTotalCount);
        mRateText.setText(String.format("%.2f%%", mTotalCount != 0 ? mCorrectCount * 100.0f / mTotalCount : 0.0f));
    }

    private static int[] zippedFanToTable(long major, int minor2, long minor1) {
        int[] table = new int[Mahjong.FAN_NAME.length];

        // 主番
        for (int i = Mahjong.OUTSIDE_HAND; i < Mahjong.DRAGON_PUNG; ++i) {
            if ((major & (1L << (Mahjong.LAST_TILE - i))) != 0) {
                table[i] = 1;
            }
        }

        // 2番
        for (int i = 0; i < 10; ++i) {
            if ((minor2 & (1 << i)) != 0) {
                table[Mahjong.DRAGON_PUNG + i] = 1;
            }
        }
        // 四归一
        if (table[Mahjong.TILE_HOG] != 0) {
            table[Mahjong.TILE_HOG] += ((minor2 >> 10) & 3);
        }
        // 双同刻
        if (table[Mahjong.DOUBLE_PUNG] != 0) {
            table[Mahjong.DOUBLE_PUNG] += ((minor2 >> 12) & 1);
        }

        // 1番
        for (int i = 0; i < 12; ++i) {
            if ((minor1 & (1L << i)) != 0) {
                table[Mahjong.PURE_DOUBLE_CHOW + i] = 1;
            }
        }

        // 一般高
        if (table[Mahjong.PURE_DOUBLE_CHOW] != 0) {
            table[Mahjong.PURE_DOUBLE_CHOW] += (int)((minor1 >> 18) & 1);
        }
        // 喜相逢
        if (table[Mahjong.MIXED_DOUBLE_CHOW] != 0) {
            table[Mahjong.MIXED_DOUBLE_CHOW] += (int)((minor1 >> 19) & 1);
        }
        // 连六
        if (table[Mahjong.SHORT_STRAIGHT] != 0) {
            table[Mahjong.SHORT_STRAIGHT] += (int)((minor1 >> 20) & 1);
        }
        // 老少副
        if (table[Mahjong.TWO_TERMINAL_CHOWS] != 0) {
            table[Mahjong.TWO_TERMINAL_CHOWS] += (int)((minor1 >> 21) & 1);
        }
        // 幺九刻
        if (table[Mahjong.PUNG_OF_TERMINALS_OR_HONORS] != 0) {
            table[Mahjong.PUNG_OF_TERMINALS_OR_HONORS] += (int)((minor1 >> 16) & 3);
        }
        return table;
    }

    private RelativeLayout createFanResultLayout() {
        int[] fan = zippedFanToTable(mPuzzle.fan_major, mPuzzle.fan_minor2, mPuzzle.fan_minor1);

        RelativeLayout root = new RelativeLayout(this);
        root.setLayoutParams(new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        DisplayMetrics metrics = getResources().getDisplayMetrics();
        final int dp5 = getResources().getDimensionPixelSize(R.dimen.dp5);
        final int lineHeight = getResources().getDimensionPixelSize(R.dimen.dp28);
        final int resultAreaWidth = metrics.widthPixels - dp5 * 2;

        final int dp12 = getResources().getDimensionPixelSize(R.dimen.dp12);
        final int dp18 = getResources().getDimensionPixelSize(R.dimen.dp18);

        final int textColor = ContextCompat.getColor(this, R.color.text_3);
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

            textView = new AppCompatTextView(this);
            TextViewCompat.setAutoSizeTextTypeUniformWithConfiguration(textView,
                    dp12, dp18, 2, TypedValue.COMPLEX_UNIT_PX);
            textView.setTextColor(textColor);
            textView.setText(str);
            textView.setPadding(0, 0, 0, 0);
            mlp = new ViewGroup.MarginLayoutParams(resultAreaWidth / 2 - dp5 * 2, lineHeight);
            mlp.leftMargin = (cnt & 1) == 0 ? dp5 : resultAreaWidth / 2;
            mlp.topMargin = (cnt >> 1) * lineHeight;
            textView.setLayoutParams(mlp);
            root.addView(textView);

            ++cnt;
        }

        textView = new TextView(this);
        textView.setTextSize(TypedValue.COMPLEX_UNIT_PX, dp18);
        textView.setTextColor(ContextCompat.getColor(this, R.color.text_1));
        textView.setText("总计：" + value + "番");
        textView.setPadding(0, 0, 0, 0);
        mlp = new ViewGroup.MarginLayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        mlp.leftMargin = dp5;
        mlp.topMargin = ((cnt >> 1) + (cnt & 1)) * lineHeight + dp5;
        textView.setLayoutParams(mlp);
        root.addView(textView);

        return root;
    }

    private void onAnswerButton() {
        // 新题，记一个错误
        if (mNewPuzzle) {
            mNewPuzzle = false;

            ++mTotalCount;
            refreshRate();
            mTrainingHistoryManager.setResult(false);
        }

        if (mFanScrollView.getVisibility() != View.VISIBLE) {
            mFanScrollView.removeAllViews();

            if (mPuzzle != null) {
                RelativeLayout root = createFanResultLayout();
                root.setGravity(Gravity.CENTER_VERTICAL);
                mFanScrollView.addView(root);
                mFanScrollView.scrollTo(0, 0);
                mFanScrollView.setVisibility(View.VISIBLE);
            }
        }
    }

    private void onSkipButton() {
        // 新题，记一个错误
        if (mNewPuzzle) {
            mNewPuzzle = false;

            ++mTotalCount;
            refreshRate();
            mTrainingHistoryManager.setResult(false);
        }

        mHandler.post(this::setPuzzle);
    }

    private CheckBox.OnCheckedChangeListener getFanToggleButtonCallback(int idx) {
        return (view, checked) -> onFanToggleButton(idx);
    }

    private void onFanToggleButton(int idx) {
        if (mPuzzle == null) {
            return;
        }

        int highlight = idx;
        if (!mCorrect) {  // 未正确时才允许答题
            if (mPrevFan == idx + 1) {
                submitAnswer();
                // 如果答错，则清空所有高亮
                if (!mCorrect) {
                    highlight = -1;
                    mPrevFan = 0;
                }
            } else {
                mPrevFan = idx + 1;
            }
        } else {  // 已经正确时，无论怎么点击，都只停留在正确答案上
            highlight = mPuzzle.fan - 1;
        }

        for (int i = 0; i <= highlight; ++i) {
            mFanToggleButtons[i].setOnCheckedChangeListener(null);
            mFanToggleButtons[i].setChecked(true);
            mFanToggleButtons[i].setOnCheckedChangeListener(getFanToggleButtonCallback(i));
        }
        for (int i = highlight + 1; i < 16; ++i) {
            mFanToggleButtons[i].setOnCheckedChangeListener(null);
            mFanToggleButtons[i].setChecked(false);
            mFanToggleButtons[i].setOnCheckedChangeListener(getFanToggleButtonCallback(i));
        }
    }

    private void submitAnswer() {
        if (mPuzzle.fan == mPrevFan) {
            // 答对了
            mCorrect = true;
            if (mNewPuzzle) {
                ++mCorrectCount;
            }

            // 自动跳转
            if (mAutoJump) {
                mHandler.post(this::setPuzzle);
            }
        }

        if (mNewPuzzle) {
            ++mTotalCount;
            refreshRate();
            mTrainingHistoryManager.setResult(mCorrect);
        }

        // 答过一次就不是新题了
        mNewPuzzle = false;
    }

    private void requestLatestPuzzles() {
        LoadingDialog loadingDialog = new LoadingDialog();
        loadingDialog.show(getSupportFragmentManager(), "LoadingDialog");
        new Thread(() -> {
            try {
                URL url = new URL("https://gitee.com/summerinsects/ChineseOfficialMahjongHelperDataSource/raw/master/training/count.db");
                HttpURLConnection urlConnection = (HttpURLConnection)url.openConnection();
                try {
                    BufferedInputStream is = new BufferedInputStream(urlConnection.getInputStream());

                    File path = new File(getFilesDir(), "train");
                    File outputFile = new File(path, "tmp.db");

                    FileOutputStream os = null;
                    try {
                        os = new FileOutputStream(outputFile);

                        byte[] data = new byte[1024];
                        int count;
                        while ((count = is.read(data)) != -1) {
                            os.write(data, 0, count);
                        }
                        os.flush();
                    } finally {
                        if (os != null) {
                            try {
                                os.close();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    }

                    is.close();

                    boolean res = false;
                    File oldFile = new File(path, "count.db");
                    if (oldFile.exists()) {
                        if (oldFile.delete()) {
                            res = outputFile.renameTo(new File(path, "count.db"));
                        }
                    } else {
                        res = outputFile.renameTo(new File(path, "count.db"));
                    }

                    if (res) {
                        File file = new File(path, "count.db");
                        mDB = SQLiteDatabase.openDatabase(file.getAbsolutePath(), null, SQLiteDatabase.OPEN_READONLY);

                        mHandler.post(() -> {
                            loadingDialog.dismiss();

                            mTrainingHistoryManager.reset();
                            setPuzzle();
                        });
                    } else {
                        mHandler.post(loadingDialog::dismiss);
                    }

                } finally {
                    urlConnection.disconnect();
                }
            } catch (IOException e) {
                e.printStackTrace();

                mHandler.post(() -> {
                    loadingDialog.dismiss();
                    Utils.toastMakeText(this, "更新题库失败！", 1).show();
                });
            }
        }).start();
    }

    public static boolean resetPuzzle(File dir) {
        File file = new File(new File(dir, "train"), "count.db");
        if (!file.exists()) {
            return true;
        }
        return file.delete();
    }

}
