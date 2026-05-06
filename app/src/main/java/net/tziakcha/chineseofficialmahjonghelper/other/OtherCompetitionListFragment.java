package net.tziakcha.chineseofficialmahjonghelper.other;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.widget.TextViewCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import net.tziakcha.chineseofficialmahjonghelper.R;
import net.tziakcha.chineseofficialmahjonghelper.widget.CommonConfirmDialog;
import net.tziakcha.chineseofficialmahjonghelper.widget.CommonWebFragment;
import net.tziakcha.chineseofficialmahjonghelper.widget.LoadingDialog;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Calendar;

public class OtherCompetitionListFragment extends Fragment {

    private static final int TIME_ACCURACY_UNDETERMINED = 0;
    private static final int TIME_ACCURACY_MONTHS = 1;
    private static final int TIME_ACCURACY_DAYS = 2;
    private static final int TIME_ACCURACY_HOURS = 3;
    private static final int TIME_ACCURACY_MINUTES = 4;

    private static final class CompetitionInfo {
        public String name;
        public String time;
        public String url;
    }

    private TextView mEmptyText;
    private final ArrayList<CompetitionInfo> mCompetitions = new ArrayList<>();
    private final CompetitionRecyclerAdapter mCompetitionRecyclerAdapter = new CompetitionRecyclerAdapter();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View contentView = inflater.inflate(R.layout.common_recycler_layout, container, false);

        ((TextView)contentView.findViewById(R.id.ab_txt)).setText("近期赛事");

        contentView.findViewById(R.id.ab_l_btn).setOnClickListener(view ->
                requireActivity().getOnBackPressedDispatcher().onBackPressed());

        contentView.findViewById(R.id.ab_r_btn).setVisibility(View.GONE);

        RecyclerView rv = contentView.findViewById(R.id.crl_rv);
        rv.setLayoutManager(new LinearLayoutManager(requireContext()));
        rv.setAdapter(mCompetitionRecyclerAdapter);

        mEmptyText = contentView.findViewById(R.id.crl_txt_empty);
        mEmptyText.setText("无近期赛事信息");

        requestCompetitions();

        return contentView;
    }

    @SuppressLint("NotifyDataSetChanged")
    private void requestCompetitions() {
        LoadingDialog loadingDialog = new LoadingDialog();
        loadingDialog.show(getChildFragmentManager(), "LoadingDialog");
        new Thread(() -> {
            try {
                URL url = new URL("https://gitee.com/summerinsects/ChineseOfficialMahjongHelperDataSource/raw/master/competition/latest.json");
                HttpURLConnection urlConnection = (HttpURLConnection)url.openConnection();
                try {
                    BufferedInputStream is = new BufferedInputStream(urlConnection.getInputStream());
                    ByteArrayOutputStream buffer = new ByteArrayOutputStream();
                    int b;
                    while ((b = is.read()) != -1) {
                        buffer.write(b);
                    }

                    String str = buffer.toString("UTF-8");
                    is.close();

                    // 测试数据
                    //str = "[{\"name\":\"第20届中国麻将牌王赛和大师赛（西安）\",\"url\":\"https://mp.weixin.qq.com/s/elABqIMDZQl_ToSdkX0vYw\",\"start_time\":1553184000,\"end_time\":1553616000,\"time_accuracy\":2}]";

                    if (!str.isEmpty()) {
                        ArrayList<CompetitionInfo> competitions = parseResponse(str);
                        requireActivity().runOnUiThread(() -> {
                            mCompetitions.clear();
                            mCompetitions.addAll(competitions);
                            mCompetitionRecyclerAdapter.notifyDataSetChanged();
                            mEmptyText.setVisibility(mCompetitions.isEmpty() ? View.VISIBLE : View.GONE);
                            loadingDialog.dismiss();
                        });
                    } else {
                        requireActivity().runOnUiThread(() -> {
                            mEmptyText.setVisibility(View.VISIBLE);
                            loadingDialog.dismiss();
                        });
                    }
                } finally {
                    urlConnection.disconnect();
                }
            } catch (IOException e) {
                e.printStackTrace();

                requireActivity().runOnUiThread(() -> {
                    loadingDialog.dismiss();

                    new CommonConfirmDialog(requireContext(), "提示", "获取近期赛事失败",
                            "重试", "取消", this::requestCompetitions).show();
                });
            }
        }).start();
    }

    private void openDetail(int idx) {
        final CompetitionInfo competition = mCompetitions.get(idx);
        CommonWebFragment fragment = CommonWebFragment.newInstance(competition.name, competition.url);
        requireActivity().getSupportFragmentManager().beginTransaction()
                .replace(R.id.cml_fl_root, fragment)
                .addToBackStack(null)
                .commit();
    }

    private static String buildTimeAccuracyMonths(long start_time, long end_time) {
        StringBuilder str = new StringBuilder();

        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(start_time * 1000);

        int y1 = calendar.get(Calendar.YEAR);
        int m1 = calendar.get(Calendar.MONTH) + 1;

        str.append(y1).append("年");
        str.append(m1).append("月");

        if (end_time != 0) {
            calendar.setTimeInMillis(end_time * 1000);

            int y2 = calendar.get(Calendar.YEAR);
            int m2 = calendar.get(Calendar.MONTH) + 1;

            if (y1 != y2 || m1 != m2) {
                str.append(" ~ ");
                if (y1 != y2) {
                    str.append(y2).append("年");
                }
                str.append(m2).append("月");
            }
        }

        return str.toString();
    }

    private static String buildTimeAccuracyDays(long start_time, long end_time) {
        StringBuilder str = new StringBuilder();

        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(start_time * 1000);

        int y1 = calendar.get(Calendar.YEAR);
        int m1 = calendar.get(Calendar.MONTH) + 1;
        int d1 = calendar.get(Calendar.DAY_OF_MONTH);

        str.append(y1).append("年");
        str.append(m1).append("月");
        str.append(d1).append("日");

        if (end_time != 0) {
            calendar.setTimeInMillis(end_time * 1000);

            int y2 = calendar.get(Calendar.YEAR);
            int m2 = calendar.get(Calendar.MONTH) + 1;
            int d2 = calendar.get(Calendar.DAY_OF_MONTH);

            if (y1 != y2 || m1 != m2 || d1 != d2) {
                str.append(" ~ ");
                if (y1 != y2) {
                    str.append(y2).append("年");
                    str.append(m2).append("月");
                } else {
                    if (m1 != m2) {
                        str.append(m2).append("月");
                    }
                }
                str.append(d2).append("日");
            }
        }

        return str.toString();
    }

    private static String buildTimeAccuracyHours(long start_time, long end_time) {
        StringBuilder str = new StringBuilder();

        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(start_time * 1000);

        int y1 = calendar.get(Calendar.YEAR);
        int m1 = calendar.get(Calendar.MONTH) + 1;
        int d1 = calendar.get(Calendar.DAY_OF_MONTH);
        int h1 = calendar.get(Calendar.HOUR_OF_DAY);

        str.append(y1).append("年");
        str.append(m1).append("月");
        str.append(d1).append("日");
        if (h1 < 10) str.append('0');
        str.append(h1).append("时");

        if (end_time != 0) {
            calendar.setTimeInMillis(end_time * 1000);

            int y2 = calendar.get(Calendar.YEAR);
            int m2 = calendar.get(Calendar.MONTH) + 1;
            int d2 = calendar.get(Calendar.DAY_OF_MONTH);
            int h2 = calendar.get(Calendar.HOUR_OF_DAY);

            if (y1 != y2 || m1 != m2 || d1 != d2 || h1 != h2) {
                str.append(" ~ ");
                if (y1 != y2) {
                    str.append(y2).append("年");
                    str.append(m2).append("月");
                    str.append(d2).append("日");
                } else {
                    if (m1 != m2) {
                        str.append(m2).append("月");
                        str.append(d2).append("日");
                    } else {
                        if (d1 != d2) {
                            str.append(d2).append("日");
                        }
                    }
                }

                if (h2 < 10) str.append('0');
                str.append(h2).append("时");
            }
        }

        return str.toString();
    }

    private static String buildTimeAccuracyMinutes(long start_time, long end_time) {
        StringBuilder str = new StringBuilder();

        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(start_time * 1000);

        int y1 = calendar.get(Calendar.YEAR);
        int m1 = calendar.get(Calendar.MONTH) + 1;
        int d1 = calendar.get(Calendar.DAY_OF_MONTH);
        int h1 = calendar.get(Calendar.HOUR_OF_DAY);
        int n1 = calendar.get(Calendar.MINUTE);

        str.append(y1).append("年");
        str.append(m1).append("月");
        str.append(d1).append("日");
        if (h1 < 10) str.append('0');
        str.append(h1).append(':');
        if (n1 < 10) str.append('0');
        str.append(n1);

        if (end_time != 0) {
            calendar.setTimeInMillis(end_time * 1000);

            int y2 = calendar.get(Calendar.YEAR);
            int m2 = calendar.get(Calendar.MONTH) + 1;
            int d2 = calendar.get(Calendar.DAY_OF_MONTH);
            int h2 = calendar.get(Calendar.HOUR_OF_DAY);
            int n2 = calendar.get(Calendar.MINUTE);

            if (y1 != y2 || m1 != m2 || d1 != d2 || h1 != h2 || n1 != n2) {
                str.append(" ~ ");

                if (y1 != y2) {
                    str.append(y2).append("年");
                    str.append(m2).append("月");
                    str.append(d2).append("日");
                } else {
                    if (m1 != m2) {
                        str.append(m2).append("月");
                        str.append(d2).append("日");
                    } else {
                        if (d1 != d2) {
                            str.append(d2).append("日");
                        }
                    }
                }

                if (h2 < 10) str.append('0');
                str.append(h2).append(':');
                if (n2 < 10) str.append('0');
                str.append(n2);
            }
        }

        return str.toString();
    }

    private static ArrayList<CompetitionInfo> parseResponse(String str) {

        final long now = System.currentTimeMillis() / 1000;
        //final long now = 0;
        ArrayList<CompetitionInfo> competitions = new ArrayList<>();

        try {
            JSONArray arr = new JSONArray(str);
            int len = arr.length();
            for (int i = 0; i < len; ++i) {
                JSONObject obj = arr.getJSONObject(i);

                long start_time = obj.has("start_time") ? obj.getLong("start_time") : 0;
                long end_time = obj.has("end_time") ? obj.getLong("end_time") : 0;
                int time_accuracy = obj.has("time_accuracy") ? obj.getInt("time_accuracy") : 0;
                if (start_time > now) {
                    CompetitionInfo competition = new CompetitionInfo();
                    competition.name = obj.getString("name");
                    if (obj.has("url")) {
                        competition.url = obj.getString("url");
                    }
                    switch (time_accuracy) {
                        case TIME_ACCURACY_UNDETERMINED:
                            competition.time = "具体时间待定";
                            break;
                        case TIME_ACCURACY_MONTHS:
                            competition.time = buildTimeAccuracyMonths(start_time, end_time);
                            break;
                        case TIME_ACCURACY_DAYS:
                            competition.time = buildTimeAccuracyDays(start_time, end_time);
                            break;
                        case TIME_ACCURACY_HOURS:
                            competition.time = buildTimeAccuracyHours(start_time, end_time);
                            break;
                        case TIME_ACCURACY_MINUTES:
                            competition.time = buildTimeAccuracyMinutes(start_time, end_time);
                            break;
                        default:
                            competition.time = "";
                            break;
                    }

                    competitions.add(competition);
                }
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }

        return competitions;
    }

    private final class CompetitionViewHolder extends RecyclerView.ViewHolder {
        private final View mRoot;
        private final TextView mTitleText;
        private final TextView mTimeText;
        private final Button mDetailButton;
        private int mIndex = -1;

        public CompetitionViewHolder(@NonNull View itemView) {
            super(itemView);

            final int dp14 = itemView.getContext().getResources().getDimensionPixelSize(R.dimen.dp14);
            final int dp8 = itemView.getContext().getResources().getDimensionPixelSize(R.dimen.dp8);

            mRoot = itemView.findViewById(R.id.cil_rl);
            mTitleText = itemView.findViewById(R.id.cil_txt_title);
            TextViewCompat.setAutoSizeTextTypeUniformWithConfiguration(mTitleText,
                    dp8, dp14, 2, TypedValue.COMPLEX_UNIT_PX);

            mTimeText = itemView.findViewById(R.id.cil_txt_time);
            TextViewCompat.setAutoSizeTextTypeUniformWithConfiguration(mTimeText,
                    dp8, dp14, 2, TypedValue.COMPLEX_UNIT_PX);

            mDetailButton = itemView.findViewById(R.id.cil_btn);
            mDetailButton.setOnClickListener(view -> {
                if (mIndex != -1) {
                    openDetail(mIndex);
                }
            });
            itemView.setOnClickListener(view -> {
                if (mIndex != -1) {
                    openDetail(mIndex);
                }
            });
        }

        public void setup(int idx) {
            mIndex = idx;

            mRoot.setBackgroundColor(
                    (idx & 1) != 0
                            ? Color.argb(0x10, 0xc0, 0xc0, 0xc0)
                            : Color.argb(0x10, 0x10, 0x10, 0x10));

            final CompetitionInfo competition = mCompetitions.get(idx);

            mTitleText.setText(competition.name);
            mTimeText.setText(competition.time);
            if (competition.url != null && !competition.url.isEmpty()) {
                mDetailButton.setVisibility(View.VISIBLE);
            } else {
                mDetailButton.setVisibility(View.GONE);
            }
        }
    }

    private final class CompetitionRecyclerAdapter extends RecyclerView.Adapter<CompetitionViewHolder> {

        @NonNull
        @Override
        public CompetitionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.competition_item_layout,
                    parent, false);
            return new CompetitionViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull CompetitionViewHolder holder, int position) {
            holder.setup(position);
        }

        @Override
        public int getItemCount() {
            return mCompetitions.size();
        }
    }
}
