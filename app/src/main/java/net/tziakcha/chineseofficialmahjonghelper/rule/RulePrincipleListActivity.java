package net.tziakcha.chineseofficialmahjonghelper.rule;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import net.tziakcha.chineseofficialmahjonghelper.R;
import net.tziakcha.chineseofficialmahjonghelper.widget.CommonWebFullScreenDialog;

public class RulePrincipleListActivity extends AppCompatActivity {

    private static final String[] sPrincipleText = {
            "不重复原则",
            "不拆移原则",
            "不得相同原则",
            "就高不就低原则",
            "套算一次原则"
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        View contentView = View.inflate(this, R.layout.common_recycler_layout, null);
        setContentView(contentView);

        ((TextView)contentView.findViewById(R.id.ab_txt)).setText("国标麻将计分原则");

        contentView.findViewById(R.id.ab_l_btn).setOnClickListener(view ->
                getOnBackPressedDispatcher().onBackPressed());

        contentView.findViewById(R.id.ab_r_btn).setVisibility(View.GONE);

        RecyclerView rv = contentView.findViewById(R.id.crl_rv);
        rv.setLayoutManager(new LinearLayoutManager(this));
        rv.setAdapter(new PrincipleRecyclerViewAdapter());
    }

    private void onPrincipleClick(int itemIndex) {
        if (itemIndex != -1) {
            new CommonWebFullScreenDialog(this, sPrincipleText[itemIndex],
                    "file:///android_asset/www/rule/principle/" + itemIndex + ".html").show();
        }
    }

    private final class PrincipleRecyclerViewHolder extends RecyclerView.ViewHolder {
        private final TextView mTextView;
        private int mIndex = -1;

        public PrincipleRecyclerViewHolder(@NonNull View itemView) {
            super(itemView);
            mTextView = itemView.findViewById(R.id.rit_txt);
            itemView.setOnClickListener(view -> onPrincipleClick(mIndex));
        }

        @SuppressLint("SetTextI18n")
        public void setup(int idx) {
            mIndex = idx;
            mTextView.setText((idx + 1) + ". " + sPrincipleText[idx]);
        }
    }

    private final class PrincipleRecyclerViewAdapter extends RecyclerView.Adapter<PrincipleRecyclerViewHolder> {

        @NonNull
        @Override
        public PrincipleRecyclerViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.rule_item_layout,
                    parent, false);
            return new PrincipleRecyclerViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull PrincipleRecyclerViewHolder holder, int position) {
            holder.setup(position);
        }

        @Override
        public int getItemCount() {
            return sPrincipleText.length;
        }
    }

}
