package net.tziakcha.chineseofficialmahjonghelper.record;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListPopupWindow;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import net.tziakcha.chineseofficialmahjonghelper.Common;
import net.tziakcha.chineseofficialmahjonghelper.Mahjong;
import net.tziakcha.chineseofficialmahjonghelper.R;
import net.tziakcha.chineseofficialmahjonghelper.Utils;
import net.tziakcha.chineseofficialmahjonghelper.widget.CommonConfirmDialog;
import net.tziakcha.chineseofficialmahjonghelper.widget.CommonTextDialog;
import net.tziakcha.chineseofficialmahjonghelper.widget.LoadingDialog;

import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.regex.Pattern;

public class RecordHistoryFragment extends Fragment {

    private static boolean sHasLoaded = false;
    private static final ArrayList<RecordInfo> sRecordList = new ArrayList<>();
    private static final String sFilePath = "record";

    private boolean mMorePayment = false;
    private ListPopupWindow mPopupWindow;
    private TextView mEmptyText;
    private final ArrayList<RecordItemInfo> mOriginalRecordItems = new ArrayList<>();
    private final ArrayList<RecordItemInfo> mFilteredRecordItems = new ArrayList<>();
    private final HistoryRecyclerViewAdapter mHistoryRecyclerViewAdapter = new HistoryRecyclerViewAdapter();
    private RecordFilterDialog.FilterCondition mFilterCondition = new RecordFilterDialog.FilterCondition();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View contentView = inflater.inflate(R.layout.common_recycler_layout, container, false);

        ((TextView)contentView.findViewById(R.id.ab_txt)).setText("历史记录");

        (contentView.findViewById(R.id.ab_l_btn)).setOnClickListener(view ->
                requireActivity().getOnBackPressedDispatcher().onBackPressed());

        Context context = requireContext();
        View rightButton = contentView.findViewById(R.id.ab_r_btn);
        mPopupWindow = Utils.createPopupMenu(context, rightButton, new String[]{
                "筛选条件", "个人汇总", "批量删除", "点对点传输"
        });
        rightButton.setOnClickListener(view -> mPopupWindow.show());
        mPopupWindow.setOnItemClickListener((adapterView, view1, position, id) -> {
            switch (position) {
                case 0: onFilterButton(); break;
                case 1: onSummaryButton(); break;
                case 2: onBatchButton(); break;
                case 3: onTransmitButton(); break;
                default: break;
            }
            mPopupWindow.dismiss();
        });

        RecyclerView rv = contentView.findViewById(R.id.crl_rv);
        rv.setLayoutManager(new LinearLayoutManager(requireContext()));
        rv.setAdapter(mHistoryRecyclerViewAdapter);

        mEmptyText = contentView.findViewById(R.id.crl_txt_empty);
        mEmptyText.setText("无历史记录");

        mMorePayment = context.getSharedPreferences(Common.SHARED_PREF_NAME, Context.MODE_PRIVATE)
                .getBoolean(Common.KEY_MORE_PAYMENT, false);

        if (sHasLoaded) {
            dataChanged();
        } else {
            LoadingDialog loadingDialog = new LoadingDialog();
            loadingDialog.show(getChildFragmentManager(), "LoadingDialog");
            new Thread(() -> {
                ArrayList<RecordInfo> recordList = new ArrayList<>();
                readRecordsFromPath(requireContext(), recordList);
                requireActivity().runOnUiThread(() -> {
                    sRecordList.addAll(recordList);
                    dataChanged();
                    loadingDialog.dismiss();
                });
                sHasLoaded = true;
            }).start();
        }

        return contentView;
    }

    private void onFilterButton() {
        new RecordFilterDialog(requireContext(), mFilterCondition, filterCondition -> {
            if (filterConditionChanged(filterCondition)) {
                mFilterCondition = filterCondition;
                filter();
                refreshRecyclerView();
            } else {
                mFilterCondition = filterCondition;
            }
        }).show();
    }

    private void onSummaryButton() {
        new RecordSummaryDialog(requireContext(), mFilteredRecordItems, selections ->
            new RecordSummaryResultDialog(requireContext(), mFilteredRecordItems, selections).show()
        ).show();
    }

    private void onBatchButton() {
        new RecordSelectionDialog(requireContext(), mFilteredRecordItems,
                "批量删除", "删除", this::batchDelete).show();
    }

    private void onTransmitButton() {
        new RecordTransmitDialog(requireContext(),
                this::showSendSelectionDialog, this::showReceiveDialog).show();
    }

    private void showSendSelectionDialog() {
        new RecordSelectionDialog(requireContext(), mFilteredRecordItems,
                "点对点传输・发送", "确定", (dialog, count, selections) -> {
            if (count > 0) {
                showSendDialog(selections);
            } else {
                Utils.showToastLong(getContext(), "请选择需要发送的对局");
            }
        }).show();
    }

    private void showSendDialog(final boolean[] selections) {
        ArrayList<RecordInfo> recordList = new ArrayList<>();
        for (int i = 0, length = selections.length; i < length; ++i) {
            if (selections[i]) {
                recordList.add(mFilteredRecordItems.get(i).record);
            }
        }

        if (!recordList.isEmpty()) {
            new RecordSendDialog(requireContext(), recordList, (count, total) -> {
                if (total != 0) {
                    new CommonTextDialog(requireContext(), "提示",
                            "数据发送完毕，共" + count + "/" + total + "条记录").show();
                } else {
                    new CommonTextDialog(requireContext(), "提示", "连接中止，请退出后重试").show();
                }
            }).show();
        }
    }

    private void showReceiveDialog() {
        new RecordReceiveDialog(requireContext(), (total, recordList) -> {
            if (total != 0) {
                for (RecordInfo record : recordList) {
                    insertRecord(RecordHistoryFragment.sRecordList, record);
                }
                new Thread(() -> {
                    Context context = requireContext();
                    for (RecordInfo record : recordList) {
                        String str = RecordInfo.recordToString(record);
                        if (str != null && !str.isEmpty()) {
                            Utils.saveStringToFile(context, sFilePath,
                                    RecordInfo.fileNameFromStartTime(record.start_time), str);
                        }
                    }
                }).start();
                dataChanged();

                new CommonTextDialog(requireContext(), "提示",
                        "数据接收完毕，共" + recordList.size() + "/" + total + "条记录").show();
            } else {
                new CommonTextDialog(requireContext(), "提示", "连接中止，请退出后重试").show();
            }
        }).show();
    }

    private void dataChanged() {
        if (!sRecordList.isEmpty()) {
            updateHistoryInfo();
            filter();
            refreshRecyclerView();
        } else {
            mEmptyText.setVisibility(View.VISIBLE);
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    private void refreshRecyclerView() {
        mEmptyText.setVisibility(!mFilteredRecordItems.isEmpty() ? View.GONE : View.VISIBLE);
        mHistoryRecyclerViewAdapter.notifyDataSetChanged();
    }

    private static String buildTitleText(String title, boolean morePayment, int mode) {
        StringBuilder str = new StringBuilder();
        if (title == null || title.isEmpty()) {
            str.append("(无标题对局)");
        } else {
            str.append(title);
        }

        if (morePayment || mode != RecordInfo.MODE_STANDARD) {
            str.append(' ');
            str.append('[');
            str.append(RecordInfo.MODE_NAME_TEXT[mode]);
            str.append(']');
        }

        return str.toString();
    }

    public static StringBuilder buildTimeText(long start_time, long finish_time) {
        Calendar calendar1 = Calendar.getInstance();
        calendar1.setTimeInMillis(start_time);
        int y1 = calendar1.get(Calendar.YEAR);
        int m1 = calendar1.get(Calendar.MONTH) + 1;
        int d1 = calendar1.get(Calendar.DAY_OF_MONTH);
        int h1 = calendar1.get(Calendar.HOUR_OF_DAY);
        int n1 = calendar1.get(Calendar.MINUTE);

        Calendar calendar2 = Calendar.getInstance();
        calendar2.setTimeInMillis(finish_time);
        int y2 = calendar2.get(Calendar.YEAR);
        int m2 = calendar2.get(Calendar.MONTH) + 1;
        int d2 = calendar2.get(Calendar.DAY_OF_MONTH);
        int h2 = calendar2.get(Calendar.HOUR_OF_DAY);
        int n2 = calendar2.get(Calendar.MINUTE);

        StringBuilder str = new StringBuilder();
        str.append(y1).append("年").append(m1).append("月").append(d1).append("日");
        if (h1 < 10) str.append('0');
        str.append(h1).append(':');
        if (n1 < 10) str.append('0');
        str.append(n1);
        str.append(" ~ ");
        if (y2 != y1) {
            str.append(y2).append("年").append(m2).append("月").append(d2).append("日");
        } else {
            if (m2 != m1) {
                str.append(m2).append("月").append(d2).append("日");
            } else {
                if (d2 != d1) {
                    str.append(d2).append("日");
                }
            }
        }
        if (h2 < 10) str.append('0');
        str.append(h2).append(':');
        if (n2 < 10) str.append('0');
        str.append(n2);
        return str;
    }

    @SuppressLint("DefaultLocale")
    private void updateHistoryInfo() {
        mOriginalRecordItems.clear();
        for (RecordInfo record : sRecordList) {
            RecordItemInfo item = new RecordItemInfo();
            mOriginalRecordItems.add(item);

            item.record = record;
            item.title = buildTitleText(record.title, mMorePayment, record.mode);
            item.time = buildTimeText(record.start_time, record.finish_time).toString();

            // 按分数排序
            final class SeatScore {
                int seat;
                int score;
            }

            SeatScore[] seatScores = new SeatScore[4];
            for (int i = 0; i < 4; ++i) {
                seatScores[i] = new SeatScore();
                seatScores[i].seat = i;
                seatScores[i].score = 0;
            }

            for (int i = 0, period = record.period; i < period; ++i) {
                RecordInfo.Detail detail = record.details[i];
                int[] scores = RecordInfo.translateToScores(record.mode, detail.fan,
                        detail.win_flag, detail.claim_flag, detail.penalty);
                seatScores[0].score += scores[0];
                seatScores[1].score += scores[1];
                seatScores[2].score += scores[2];
                seatScores[3].score += scores[3];
            }

            // 插入排序
            for (int i = 1; i < 4; ++i) {
                SeatScore temp = seatScores[i];

                int k = i;
                do {
                    if (seatScores[k - 1].score >= temp.score) {
                        break;
                    }
                    seatScores[k] = seatScores[k - 1];
                    --k;
                } while (k > 0);

                if (k != i) {
                    seatScores[k] = temp;
                }
            }

            for (int i = 0; i < 4; ++i) {
                int seat = seatScores[i].seat;
                item.players[i] = String.format("%s: %s (%+d)",
                        Mahjong.WIND_TEXT[seat], record.names[seat], seatScores[i].score);
                item.seats[i] = seat;
            }
        }
    }

    private boolean filterConditionChanged(RecordFilterDialog.FilterCondition filterCondition) {
        String name1 = mFilterCondition.name;
        String name2 = filterCondition.name;
        if (name1 == null) name1 = "";
        if (name2 == null) name2 = "";
        if (!name1.equals(name2)) {
            return true;
        }

        if (!name1.isEmpty()
                && (mFilterCondition.regular_enabled != filterCondition.regular_enabled
                || mFilterCondition.ignore_case != filterCondition.ignore_case
                || mFilterCondition.whole_word != filterCondition.whole_word)) {
            return true;
        }

        String title1 = mFilterCondition.title;
        String title2 = filterCondition.title;
        if (title1 == null) title1 = "";
        if (title2 == null) title2 = "";
        if (!title1.equals(title2)) {
            return true;
        }

        if (!title1.isEmpty()
                && (mFilterCondition.regular_enabled != filterCondition.regular_enabled
                || mFilterCondition.ignore_case != filterCondition.ignore_case
                || mFilterCondition.whole_word != filterCondition.whole_word)) {
            return true;
        }

        if (mFilterCondition.time_enabled != filterCondition.time_enabled) {
            return true;
        }

        if (filterCondition.time_enabled) {
            return mFilterCondition.start_time != filterCondition.start_time
                    || mFilterCondition.finish_time != filterCondition.finish_time;
        }

        return false;
    }

    private void filter() {
        mFilteredRecordItems.clear();

        if (mOriginalRecordItems.isEmpty()) {
            return;
        }

        mFilteredRecordItems.addAll(mOriginalRecordItems);
        for (int i = 0, size = mFilteredRecordItems.size(); i < size; ++i) {
            mFilteredRecordItems.get(i).match = 0;
        }

        ArrayList<RecordItemInfo> filteredRecordItems = new ArrayList<>();

        if (mFilterCondition.time_enabled) {
            // 筛选起始时间
            final long start_time = mFilterCondition.start_time;
            if (start_time != 0) {
                for (int i = 0, size = mFilteredRecordItems.size(); i < size; ++i) {
                    RecordItemInfo item = mFilteredRecordItems.get(i);
                    if (item.record.finish_time >= start_time) {
                        filteredRecordItems.add(item);
                    }
                }

                mFilteredRecordItems.clear();
                if (filteredRecordItems.isEmpty()) {
                    return;
                }
                mFilteredRecordItems.addAll(filteredRecordItems);
            }

            // 筛选截止时间
            final long finish_time = mFilterCondition.finish_time;
            if (finish_time != 0) {
                filteredRecordItems.clear();
                for (int i = 0, size = mFilteredRecordItems.size(); i < size; ++i) {
                    RecordItemInfo item = mFilteredRecordItems.get(i);
                    if (item.record.start_time <= finish_time) {
                        filteredRecordItems.add(item);
                    }
                }

                mFilteredRecordItems.clear();
                if (filteredRecordItems.isEmpty()) {
                    return;
                }
                mFilteredRecordItems.addAll(filteredRecordItems);
            }
        }

        final String name = mFilterCondition.name;
        final String title = mFilterCondition.title;
        if (mFilterCondition.regular_enabled) {
            if (name != null && !name.isEmpty()) {
                filteredRecordItems.clear();

                Pattern pattern;
                if (mFilterCondition.ignore_case) {
                    if (mFilterCondition.whole_word) {
                        pattern = Pattern.compile("\\b(" + name + ")\\b", Pattern.CASE_INSENSITIVE);
                    } else {
                        pattern = Pattern.compile(name, Pattern.CASE_INSENSITIVE);
                    }
                } else {
                    if (mFilterCondition.whole_word) {
                        pattern = Pattern.compile("\\b(" + name + ")\\b");
                    } else {
                        pattern = Pattern.compile(name);
                    }
                }

                for (int i = 0, size = mFilteredRecordItems.size(); i < size; ++i) {
                    int match = 0;
                    RecordItemInfo item = mFilteredRecordItems.get(i);
                    for (int k = 0; k < 4; ++k) {
                        if (pattern.matcher(item.record.names[k]).find()) {
                            match |= 1 << k;
                        }
                    }
                    if (match != 0) {
                        filteredRecordItems.add(item);
                        item.match = match;
                    }
                }

                mFilteredRecordItems.clear();
                if (filteredRecordItems.isEmpty()) {
                    return;
                }
                mFilteredRecordItems.addAll(filteredRecordItems);
            }

            if (title != null && !title.isEmpty()) {
                Pattern pattern;
                if (mFilterCondition.ignore_case) {
                    if (mFilterCondition.whole_word) {
                        pattern = Pattern.compile("\\b(" + title + ")\\b", Pattern.CASE_INSENSITIVE);
                    } else {
                        pattern = Pattern.compile(title, Pattern.CASE_INSENSITIVE);
                    }
                } else {
                    if (mFilterCondition.whole_word) {
                        pattern = Pattern.compile("\\b(" + title + ")\\b");
                    } else {
                        pattern = Pattern.compile(title);
                    }
                }

                filteredRecordItems.clear();

                for (int i = 0, size = mFilteredRecordItems.size(); i < size; ++i) {
                    RecordItemInfo item = mFilteredRecordItems.get(i);
                    if (item.record.title != null && pattern.matcher(item.record.title).find()) {
                        filteredRecordItems.add(item);
                    }
                }

                mFilteredRecordItems.clear();
                if (filteredRecordItems.isEmpty()) {
                    return;
                }
                mFilteredRecordItems.addAll(filteredRecordItems);
            }
        } else {
            if (name != null && !name.isEmpty()) {
                filteredRecordItems.clear();
                if (mFilterCondition.ignore_case) {
                    if (mFilterCondition.whole_word) {
                        for (int i = 0, size = mFilteredRecordItems.size(); i < size; ++i) {
                            int match = 0;
                            RecordItemInfo item = mFilteredRecordItems.get(i);
                            for (int k = 0; k < 4; ++k) {
                                if (item.record.names[k].equalsIgnoreCase(name)) {
                                    match |= 1 << k;
                                }
                            }
                            if (match != 0) {
                                filteredRecordItems.add(item);
                                item.match = match;
                            }
                        }
                    } else {
                        for (int i = 0, size = mFilteredRecordItems.size(); i < size; ++i) {
                            int match = 0;
                            RecordItemInfo item = mFilteredRecordItems.get(i);
                            for (int k = 0; k < 4; ++k) {
                                if (item.record.names[k].toLowerCase().contains(name.toLowerCase())) {
                                    match |= 1 << k;
                                }
                            }
                            if (match != 0) {
                                filteredRecordItems.add(item);
                                item.match = match;
                            }
                        }
                    }
                } else {
                    if (mFilterCondition.whole_word) {
                        for (int i = 0, size = mFilteredRecordItems.size(); i < size; ++i) {
                            int match = 0;
                            RecordItemInfo item = mFilteredRecordItems.get(i);
                            for (int k = 0; k < 4; ++k) {
                                if (item.record.names[k].equals(name)) {
                                    match |= 1 << k;
                                }
                            }
                            if (match != 0) {
                                filteredRecordItems.add(item);
                                item.match = match;
                            }
                        }
                    } else {
                        for (int i = 0, size = mFilteredRecordItems.size(); i < size; ++i) {
                            int match = 0;
                            RecordItemInfo item = mFilteredRecordItems.get(i);
                            for (int k = 0; k < 4; ++k) {
                                if (item.record.names[k].contains(name)) {
                                    match |= 1 << k;
                                }
                            }
                            if (match != 0) {
                                filteredRecordItems.add(item);
                                item.match = match;
                            }
                        }
                    }
                }

                mFilteredRecordItems.clear();
                if (filteredRecordItems.isEmpty()) {
                    return;
                }
                mFilteredRecordItems.addAll(filteredRecordItems);
            }

            if (title != null && !title.isEmpty()) {
                filteredRecordItems.clear();
                if (mFilterCondition.ignore_case) {
                    if (mFilterCondition.whole_word) {
                        for (int i = 0, size = mFilteredRecordItems.size(); i < size; ++i) {
                            RecordItemInfo item = mFilteredRecordItems.get(i);
                            if (item.record.title != null && item.record.title.equalsIgnoreCase(title)) {
                                filteredRecordItems.add(item);
                            }
                        }
                    } else {
                        for (int i = 0, size = mFilteredRecordItems.size(); i < size; ++i) {
                            RecordItemInfo item = mFilteredRecordItems.get(i);
                            if (item.record.title != null && item.record.title.toLowerCase().contains(title.toLowerCase())) {
                                filteredRecordItems.add(item);
                            }
                        }
                    }
                } else {
                    if (mFilterCondition.whole_word) {
                        for (int i = 0, size = mFilteredRecordItems.size(); i < size; ++i) {
                            RecordItemInfo item = mFilteredRecordItems.get(i);
                            if (item.record.title != null && item.record.title.equals(title)) {
                                filteredRecordItems.add(item);
                            }
                        }
                    } else {
                        for (int i = 0, size = mFilteredRecordItems.size(); i < size; ++i) {
                            RecordItemInfo item = mFilteredRecordItems.get(i);
                            if (item.record.title != null && item.record.title.contains(title)) {
                                filteredRecordItems.add(item);
                            }
                        }
                    }
                }

                mFilteredRecordItems.clear();
                if (filteredRecordItems.isEmpty()) {
                    return;
                }
                mFilteredRecordItems.addAll(filteredRecordItems);
            }
        }
    }

    private final class HistoryRecyclerViewHolder extends RecyclerView.ViewHolder {
        private final View mRoot;
        private final TextView mTitleText;
        private final TextView mTimeText;
        private final TextView[] mPlayerTexts = new TextView[4];
        private int mIndex = -1;

        public HistoryRecyclerViewHolder(@NonNull View itemView) {
            super(itemView);

            mRoot = itemView.findViewById(R.id.rhi_ll);
            mTitleText = itemView.findViewById(R.id.rhi_txt_title);
            mTimeText = itemView.findViewById(R.id.rhi_txt_time);
            mPlayerTexts[0] = itemView.findViewById(R.id.rhi_txt_pl0);
            mPlayerTexts[1] = itemView.findViewById(R.id.rhi_txt_pl1);
            mPlayerTexts[2] = itemView.findViewById(R.id.rhi_txt_pl2);
            mPlayerTexts[3] = itemView.findViewById(R.id.rhi_txt_pl3);
            itemView.findViewById(R.id.rhi_btn_del).setOnClickListener(view -> {
                if (mIndex != -1) {
                    onDeleteButton(mIndex);
                }
            });
        }

        public void setup(int idx) {
            mIndex = idx;

            mRoot.setBackgroundColor(
                    (idx & 1) != 0
                            ? Color.argb(0x10, 0xc0, 0xc0, 0xc0)
                            : Color.argb(0x10, 0x10, 0x10, 0x10));

            itemView.setOnClickListener(view -> onRecyclerItem(idx));

            RecordItemInfo item = mFilteredRecordItems.get(idx);
            mTitleText.setText(item.title);
            mTimeText.setText(item.time);

            final int match = item.match;
            final int[] seats = item.seats;
            for (int i = 0; i < 4; ++i) {
                mPlayerTexts[i].setText(item.players[i]);
                mPlayerTexts[i].setTextColor(
                        (match & (1 << seats[i])) == 0 ? Common.COLOR_GRAY : Common.COLOR_RED);
            }
        }

    }

    private final class HistoryRecyclerViewAdapter extends RecyclerView.Adapter<HistoryRecyclerViewHolder> {

        @NonNull
        @Override
        public HistoryRecyclerViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.record_history_item_layout,
                    parent, false);
            return new HistoryRecyclerViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull HistoryRecyclerViewHolder holder, int position) {
            holder.setup(position);
        }

        @Override
        public int getItemCount() {
            return mFilteredRecordItems.size();
        }
    }

    private void onRecyclerItem(int idx) {
        RecordInfo record = mFilteredRecordItems.get(idx).record;
        RecordSheetFragment fragment = RecordSheetFragment.newInstance(record);
        requireActivity().getSupportFragmentManager().beginTransaction()
                .replace(R.id.cml_fl_root, fragment)
                .addToBackStack(null)
                .commit();
    }

    private void onDeleteButton(int idx) {
        RecordItemInfo item = mFilteredRecordItems.get(idx);

        StringBuilder str = new StringBuilder();
        str.append("你将删除记录：\n\n");
        str.append(item.title);
        str.append('\n');
        str.append(item.time);
        for (int i = 0; i < 4; ++i) {
            str.append('\n');
            str.append(item.players[i]);
        }
        str.append("\n\n删除后无法找回，确认删除？");

        new CommonConfirmDialog(requireContext(), "警告", str.toString(),
                "确定", "取消", () -> {
            if (removeRecord(item.record)) {
                mFilteredRecordItems.remove(idx);
                sRecordList.remove(item.record);
                mHistoryRecyclerViewAdapter.notifyItemRemoved(idx);
            } else {
                Utils.showToastShort(requireContext(), "删除记录失败");
            }
        }).show();
    }

    private void batchDelete(RecordSelectionDialog dialog, int count, boolean[] selections) {
        if (count > 0) {
            int cnt = 0;
            for (int i = selections.length; i > 0; --i) {
                int idx = i - 1;
                if (selections[idx]) {
                    RecordItemInfo item = mFilteredRecordItems.get(idx);
                    if (removeRecord(item.record)) {
                        mFilteredRecordItems.remove(idx);
                        sRecordList.remove(item.record);
                        mHistoryRecyclerViewAdapter.notifyItemRemoved(idx);
                        ++cnt;
                    }
                }
            }
            Utils.showToastLong(getContext(), "成功删除" + cnt + "/" + count + "局");
            dialog.dismiss();
        } else {
            Utils.showToastLong(getContext(), "请选择需要删除的对局");
        }
    }

    private boolean removeRecord(RecordInfo record) {
        File dir = requireContext().getDir(sFilePath, Context.MODE_PRIVATE);
        File file = new File(dir, RecordInfo.fileNameFromStartTime(record.start_time));
        if (file.exists()) {
            return file.delete();
            //return true;
        }
        return false;
    }

    private static void insertRecord(ArrayList<RecordInfo> recordList, RecordInfo record) {
        // 我们认为开始时间相同的为同一个记录
        long start_time = record.start_time;
        for (int i = 0, size = recordList.size(); i < size; ++i) {
            RecordInfo record1 = recordList.get(i);
            if (record1.start_time == start_time) {
                recordList.set(i, record);
                return;
            }
        }

        recordList.add(record);

        // 调整到合适位置
        int k = recordList.size() - 1;
        while (k > 0) {
            RecordInfo record1 = recordList.get(k - 1);
            if (record1.start_time > start_time) {
                break;
            }

            recordList.set(k, record1);
            --k;
        }

        if (k != recordList.size() - 1) {
            recordList.set(k, record);
        }
    }

    private static void readRecordsFromPath(Context context, ArrayList<RecordInfo> recordList) {
        File dir = context.getDir(sFilePath, Context.MODE_PRIVATE);
        File[] files = dir.listFiles();
        if (files == null) {
            return;
        }

        for (File file : files) {
            if (file.isFile()) {
                String fileName = file.getName();
                if (fileName.equals("active.json")) {
                    continue;
                }

                if (fileName.endsWith(".json")) {
                    String str = Utils.getStringFromFile(file);
                    RecordInfo record = new RecordInfo();
                    if (RecordInfo.parseRecord(str, record)) {
                        if (fileName.equals(RecordInfo.fileNameFromStartTime(record.start_time))) {
                            insertRecord(recordList, record);
                        }
                    }
                }
            }
        }
    }

    public static void saveRecord(Context context, RecordInfo record) {
        String str = RecordInfo.recordToString(record);
        if (str != null && !str.isEmpty()) {
            Utils.saveStringToFile(context, sFilePath,
                    RecordInfo.fileNameFromStartTime(record.start_time), str);
        }

        if (sHasLoaded) {
            insertRecord(RecordHistoryFragment.sRecordList, record);
        }
    }

}
