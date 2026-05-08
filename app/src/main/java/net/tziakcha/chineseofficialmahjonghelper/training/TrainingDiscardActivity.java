package net.tziakcha.chineseofficialmahjonghelper.training;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.ListPopupWindow;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import net.tziakcha.chineseofficialmahjonghelper.Mahjong;
import net.tziakcha.chineseofficialmahjonghelper.R;
import net.tziakcha.chineseofficialmahjonghelper.Utils;
import net.tziakcha.chineseofficialmahjonghelper.widget.HandTilesLayout;
import net.tziakcha.chineseofficialmahjonghelper.widget.LoadingDialog;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Objects;
import java.util.Random;

@SuppressLint("SetTextI18n")
public class TrainingDiscardActivity extends AppCompatActivity {
    private static final class PuzzleTemplate {
        public String[] question;
        public String[] solution;
        public String[] meldable;
        public int shanten;
    }
    private static final ArrayList<PuzzleTemplate> sPuzzleTemplates = new ArrayList<>();

    private static final class PuzzleInfo extends Mahjong.HandTiles {
        public long solution;
    }

    private ListPopupWindow mPopupWindow;
    private HandTilesLayout mHandTilesLayout;
    private TextView mCountText;
    private TextView mRateText;
    private final ImageView[] mAnswerImage = new ImageView[2];
    private TrainingHistoryManager mTrainingHistoryManager;
    private boolean mAutoJump = true;
    private boolean mNewPuzzle = false;
    private boolean mCorrect = false;
    private long mSolution = 0;
    private int mTotalCount = 0;
    private int mCorrectCount = 0;

    private final Handler mHandler = new Handler(Looper.getMainLooper());

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        View contentView = View.inflate(this, R.layout.training_discard_layout, null);
        setContentView(contentView);

        ((TextView)contentView.findViewById(R.id.ab_txt)).setText("三色训练");

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
                contentView.findViewById(R.id.tdl_rl_hand));
        mHandTilesLayout.hidePlaceholder();
        mHandTilesLayout.setOnTileClickListener((htl, idx, tile) -> onStandingTile(idx, tile));

        contentView.findViewById(R.id.tdl_btn_skip).setOnClickListener(view -> onSkipButton());
        contentView.findViewById(R.id.tdl_btn_view).setOnClickListener(view -> onAnswerButton());
        mCountText = contentView.findViewById(R.id.tdl_txt_cnt);
        mRateText = contentView.findViewById(R.id.tdl_txt_rate);
        mAnswerImage[0] = contentView.findViewById(R.id.tdl_img_ans0);
        mAnswerImage[1] = contentView.findViewById(R.id.tdl_img_ans1);

        CheckBox checkBox = contentView.findViewById(R.id.tdl_cb_auto);
        checkBox.setOnCheckedChangeListener((view, checked) -> mAutoJump = checked);
        Utils.adaptCompoundButton(checkBox, getResources().getDimensionPixelSize(R.dimen.dp28));

        mTrainingHistoryManager = new TrainingHistoryManager(this,
                contentView.findViewById(R.id.tdl_rv_res));

        // 使牌面看上去与手牌的一样大
        float scale = mHandTilesLayout.getScaleX();
        for (int i = 0; i < 2; ++i) {
            ImageView img = mAnswerImage[i];
            ViewGroup.LayoutParams lp = img.getLayoutParams();
            lp.width = (int)(HandTilesLayout.IMG_WIDTH * scale);
            lp.height = (int)(HandTilesLayout.IMG_HEIGHT * scale);
        }

        if (!sPuzzleTemplates.isEmpty()) {
            setPuzzle();
        } else {
            loadPuzzle();
        }
    }

    private void loadPuzzle() {
        LoadingDialog dialog = new LoadingDialog();
        dialog.show(getSupportFragmentManager(), "LoadingDialog");

        new Thread(() -> {
            boolean res = loadPuzzleTemplates();
            mHandler.post(() -> {
                if (res) {
                    setPuzzle();
                } else {
                    Utils.showToastLong(TrainingDiscardActivity.this, "加载题库失败");
                }
                dialog.dismiss();
            });
        }).start();
    }

    private boolean loadPuzzleTemplates() {
        // 先尝试内部存储文件
        try {
            File path = new File(getFilesDir(), "train");
            InputStream is = new FileInputStream(new File(path, "puzzles.txt"));
            return parsePuzzleTemplates(is);
        } catch (FileNotFoundException e) {
            // 再尝试assets打包所带的文件
            try {
                InputStream is = getAssets().open("train/puzzles.txt");
                return parsePuzzleTemplates(is);
            } catch (IOException e1) {
                return false;
            }
        }
    }

    private boolean parsePuzzleTemplates(InputStream is) {
        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        try {
            String line;
            while ((line = reader.readLine()) != null) {
                parseLine(line);
            }
            reader.close();
        } catch (Exception e) {
            //e.printStackTrace();
            Log.e("DTA", Objects.requireNonNull(e.getLocalizedMessage()));
        }
        return true;
    }

    private void parseLine(String line) {
        String[] part = line.split("\\|");
        if (part.length == 4) {
            PuzzleTemplate pt = new PuzzleTemplate();

            pt.question = part[0].split(",");
            if (pt.question.length != 3) return;

            pt.solution = part[1].split(",");
            if (pt.solution.length != 3) return;
            for (int i = 0; i < 3; ++i) {
                if (pt.solution[i].compareTo("0") == 0) {
                    pt.solution[i] = "";
                }
            }

            pt.meldable = part[2].split(",");
            if (pt.meldable.length != 3) return;
            for (int i = 0; i < 3; ++i) {
                if (pt.meldable[i].compareTo("0") == 0) {
                    pt.meldable[i] = "";
                }
            }

            pt.shanten = Integer.parseInt(part[3]);

            sPuzzleTemplates.add(pt);
        }
    }

    // 添加副露
    static int makeFixedPack(Random random, int[] table, int suit, char rank) {
        int t = Mahjong.makeTile(suit, rank - '0');
        --table[t - 1];
        --table[t];
        --table[t + 1];
        return Mahjong.makePack(1 + random.nextInt(3), Mahjong.PACK_TYPE_CHOW, t);
    }

    // 对于3张牌的花色，生成1组副露
    static void makeFixedPackWith3Tiles(Random random, int[] table, final PuzzleTemplate tpl, ArrayList<Integer> packs, final int[] suitOrder) {
        for (int i = 0; i < 3; ++i) {
            String meldable = tpl.meldable[i];
            if (!meldable.isEmpty() && tpl.question[i].length() == 3) {
                packs.add(makeFixedPack(random, table, suitOrder[i], meldable.charAt(0)));
                break;
            }
        }
    }

    // 对于6张牌的花色，生成2组副露
    static void makeFixedPacksWith6Tiles(Random random, int[] table, final PuzzleTemplate tpl, ArrayList<Integer> packs, final int[] suitOrder) {
        for (int i = 0; i < 3; ++i) {
            String meldable = tpl.meldable[i];
            if (meldable.length() == 2 && tpl.question[i].length() == 6) {
                packs.add(makeFixedPack(random, table, suitOrder[i], meldable.charAt(0)));
                packs.add(makeFixedPack(random, table, suitOrder[i], meldable.charAt(1)));
                break;
            }
        }
    }

    static int maxDistanceIndex(Random random, int[] dist) {
        int k = 0, max_dist = dist[0];
        for (int i = 1; i < 6; ++i) {
            // 如果相同，则一半概率选择后面的
            if (dist[i] > max_dist || (dist[i] == max_dist && random.nextInt(100) < 50)) {
                k = i;
                max_dist = dist[i];
            }
        }
        return k;
    }

    // 求每张数牌19的距离
    static void distanceFromTable(final int[] table, int[] dist) {
        for (int s = 0; s < 3; ++s) {
            int i, d1, d9;

            // 1的距离
            for (i = 0, d1 = 0; i < 9; ++i, ++d1) {
                if (table[Mahjong.makeTile(s + 1, i + 1)] > 0) break;
            }
            dist[s << 1] = d1;

            // 9的距离
            for (i = 0, d9 = 0; i < 9; ++i, ++d9) {
                if (table[Mahjong.makeTile(s + 1, 9 - i)] > 0) break;
            }
            dist[(s << 1) + 1] = d9;
        }
    }

    // 随机选择一张幺九牌
    static int pickRandomTerminal(Random random, final int[] table) {
        int[] dist = new int[6];
        distanceFromTable(table, dist);
        int k = maxDistanceIndex(random, dist);
        return Mahjong.makeTile((k >> 1) + 1, (k & 1) * 8 + 1);  // 0 2 4为1；1 3 5为9
    }

    // 随机选择一张断幺牌
    static int pickRandomSimple(Random random, final int[] table) {
        int[] dist = new int[6];
        distanceFromTable(table, dist);
        int k = maxDistanceIndex(random, dist);
        int max_dist = dist[k], rank;

        // 先尝试避免全带幺，需生成一个断幺的雀头
        int a, b;
        if ((k & 1) != 0) {
            // 对于9而言，最远情况是有23距离6，最近距离0
            // 下界=10-(距离-2)=12-距离 上界=9或者8
            a = 12 - max_dist;
            b = max_dist < 6 ? 9 : 8;
        } else {
            // 对于1而言，最远情况是有78距离6，最近距离0
            // 下界=1或者2 上界=距离-2
            a = max_dist < 6 ? 1 : 2;
            b = max_dist - 2;
        }
        if (a < b) {
            rank = a + random.nextInt(b - a + 1);
        } else {
            // 实在没法就这么办吧
            rank = (k & 1) * 8 + 1;
        }

        return Mahjong.makeTile((k >> 1) + 1, rank);
    }

    static PuzzleInfo convertTable(Random random, final int[] table, int randomPair) {
        PuzzleInfo puzzle = new PuzzleInfo();

        // 收集立牌
        int[] tmp = new int[14];
        int[] st = new int[14];
        int cnt = 0;
        for (int i = Mahjong.TILE_1m; i < Mahjong.TILE_TABLE_SIZE; ++i) {
            for (int k = table[i]; k > 0; --k) {
                tmp[cnt++] = i;
            }
        }

        // 随机选一张作为摸上来的牌
        int k = random.nextInt(cnt);

        // 选中的牌
        puzzle.wt = tmp[k];

        // 移出之后的牌放进st里
        // 之前的牌
        System.arraycopy(tmp, 0, st, 0, k);

        // 之后的牌
        if (k + 1 < cnt) System.arraycopy(tmp, k + 1, st, k, cnt - (k + 1));
        --cnt;

        // 插入雀头
        if (randomPair != 0) {
            int s = 0;

            // 找到位置
            while (s < cnt && st[s] < randomPair) {
                ++s;
            }

            // 后移
            for (int i = cnt; i > s; --i) {
                st[i + 1] = st[i - 1];
            }

            // 插入
            st[s] = randomPair;
            st[s + 1] = randomPair;

            cnt += 2;
        }

        puzzle.st = Arrays.copyOfRange(st, 0, cnt);

        return puzzle;
    }

    private static void shuffleArray(final int[] a, Random r) {
        int d = a.length;
        if (d > 1) {
            int p = 0, q = d;
            for (--q, --d; p < q; ++p, --d) {
                int i = r.nextInt(d + 1);
                if (i != 0) {
                    int t = a[p];
                    a[p] = a[p + i];
                    a[p + i] = t;
                }
            }
        }
    }

    private static PuzzleInfo generatePuzzle() {
        Random random = new Random();

        // 选择一个题目模板
        final PuzzleTemplate tpl = sPuzzleTemplates.get(random.nextInt(sPuzzleTemplates.size()));

        ArrayList<Integer> packs = new ArrayList<>();
        long solution = 0;
        int randomPair = 0;  // 随机雀头
        final int[] suitOrder = {1, 2, 3};
        shuffleArray(suitOrder, random);

        // 牌表
        int[] table = new int[Mahjong.TILE_P + 1];
        int cnt = 0;
        for (int i = 0; i < 3; ++i) {
            int s = suitOrder[i];

            String ques = tpl.question[i];
            for (int j = 0, l = ques.length(); j < l; ++j) {
                char c = ques.charAt(j);
                ++table[Mahjong.makeTile(s, c - '0')];
                ++cnt;
            }

            String sln = tpl.solution[i];
            for (int j = 0, l = sln.length(); j < l; ++j) {
                char c = sln.charAt(j);
                solution |= (1L << Mahjong.makeTile(s, c - '0'));
            }
        }

        if (cnt < 12) {
            // 用副露补齐无关第四组
            int dragon = Mahjong.TILE_C + random.nextInt(3);
            int offer = 1 + random.nextInt(3);
            packs.add(Mahjong.makePack(offer, Mahjong.PACK_TYPE_PUNG, dragon));
        }

        if (cnt == 14) {
            // 1. 副露仅三张的那门
            makeFixedPackWith3Tiles(random, table, tpl, packs, suitOrder);

            // 2. 副露仅六张的那门，断幺或独幺时避免门断平听牌型，其他情况20%概率
            if (packs.isEmpty() &&
                    (table[Mahjong.TILE_1m] + table[Mahjong.TILE_9m] + table[Mahjong.TILE_1s]
                            + table[Mahjong.TILE_9s] + table[Mahjong.TILE_1p] + table[Mahjong.TILE_9p] < 2
                            || random.nextInt(10) < 2)) {
                makeFixedPacksWith6Tiles(random, table, tpl, packs, suitOrder);
            }
        } else if (cnt == 12) {
            // 补上随机雀头
            randomPair = pickRandomTerminal(random, table);

            // 1. 副露仅三张的那门
            makeFixedPackWith3Tiles(random, table, tpl, packs, suitOrder);

            // 2. 副露仅六张的那门，20%概率
            if (packs.isEmpty() && random.nextInt(10) < 2) {
                makeFixedPacksWith6Tiles(random, table, tpl, packs, suitOrder);
            }

            // 门清的听牌可以用字牌作雀头（50%概率）
            if (tpl.shanten == 0 && packs.isEmpty() && random.nextInt(10) < 5) {
                randomPair = Mahjong.TILE_E + random.nextInt(7);
            }
        } else if (cnt == 9) {
            // 补上随机雀头
            randomPair = pickRandomSimple(random, table);

            // 20%概率再增加一组副露
            if (random.nextInt(10) < 2) {
                makeFixedPackWith3Tiles(random, table, tpl, packs, suitOrder);
            }
        }

        PuzzleInfo puzzle = convertTable(random, table, randomPair);
        puzzle.fp = new int[packs.size()];
        for (int i = 0, size = packs.size(); i < size; ++i) {
            puzzle.fp[i] = packs.get(i);
        }
        shuffleArray(puzzle.fp, random);  // 副露随机排序
        puzzle.solution = solution;

        return puzzle;
    }

    private void setPuzzle() {
        PuzzleInfo puzzle = generatePuzzle();
        mHandTilesLayout.setData(puzzle.fp, puzzle.st, puzzle.wt);
        mSolution = puzzle.solution;
        mNewPuzzle = true;
        mCorrect = false;
        mAnswerImage[0].setVisibility(View.GONE);
        mAnswerImage[1].setVisibility(View.GONE);
        mTrainingHistoryManager.addResult();
    }

    private void onStandingTile(int idx, int tile) {
        // 正确过，就不再可点击了
        if (mCorrect) return;

        if (tile == 0 || mHandTilesLayout.getServingTile() == 0) {
            return;
        }

        if ((mSolution & (1L << tile)) != 0) {
            // 答对了
            mCorrect = true;
            if (mNewPuzzle) {
                ++mCorrectCount;
            }

            mHandTilesLayout.hideTile(idx);

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

    @SuppressLint("DefaultLocale")
    private void refreshRate() {
        mCountText.setText(mCorrectCount + "/" + mTotalCount);
        mRateText.setText(String.format("%.2f%%", mTotalCount != 0 ? mCorrectCount * 100.0f / mTotalCount : 0.0f));
    }

    private void onAnswerButton() {
        // 新题，记一个错误
        if (mNewPuzzle) {
            mNewPuzzle = false;

            ++mTotalCount;
            refreshRate();
            mTrainingHistoryManager.setResult(false);
        }

        for (int i = 0, c = 0; i < 34; ++i) {
            if ((mSolution & (1L << Mahjong.ALL_TILES[i])) != 0) {
                ImageView img = mAnswerImage[c++];
                img.setBackgroundResource(Utils.TILE_IMG_SRC[i]);
                img.setVisibility(View.VISIBLE);
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

    private void requestLatestPuzzles() {
        LoadingDialog loadingDialog = new LoadingDialog();
        loadingDialog.show(getSupportFragmentManager(), "LoadingDialog");
        new Thread(() -> {
            try {
                URL url = new URL("https://gitee.com/summerinsects/ChineseOfficialMahjongHelperDataSource/raw/master/training/puzzles.txt");
                HttpURLConnection urlConnection = (HttpURLConnection)url.openConnection();
                try {
                    BufferedInputStream is = new BufferedInputStream(urlConnection.getInputStream());

                    File path = new File(getFilesDir(), "train");
                    File outputFile = new File(path, "tmp.txt");

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
                    File oldFile = new File(path, "puzzles.txt");
                    if (oldFile.exists()) {
                        if (oldFile.delete()) {
                            res = outputFile.renameTo(new File(path, "puzzles.txt"));
                        }
                    } else {
                        res = outputFile.renameTo(new File(path, "puzzles.txt"));
                    }

                    if (res) {
                        mHandler.post(() -> {
                            loadingDialog.dismiss();

                            sPuzzleTemplates.clear();
                            mTrainingHistoryManager.reset();
                            loadPuzzle();
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
                    Utils.showToastLong(this, "更新题库失败！");
                });
            }
        }).start();
    }

    public static boolean resetPuzzle(File dir) {
        File file = new File(new File(dir, "train"), "puzzles.txt");
        if (!file.exists()) {
            return true;
        }

        if (file.delete()) {
            sPuzzleTemplates.clear();
            return true;
        }
        return false;
    }

}
