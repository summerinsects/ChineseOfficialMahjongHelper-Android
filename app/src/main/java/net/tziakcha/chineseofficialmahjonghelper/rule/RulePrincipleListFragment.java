package net.tziakcha.chineseofficialmahjonghelper.rule;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import net.tziakcha.chineseofficialmahjonghelper.R;
import net.tziakcha.chineseofficialmahjonghelper.widget.CommonWebFragment;

public class RulePrincipleListFragment extends Fragment {

    private static final String[] sPrincipleText = {
            "不重复原则",
            "不拆移原则",
            "不得相同原则",
            "就高不就低原则",
            "套算一次原则"
    };

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View contentView = inflater.inflate(R.layout.common_recycler_layout, container, false);

        ((TextView)contentView.findViewById(R.id.ab_txt)).setText("国标麻将计分原则");

        contentView.findViewById(R.id.ab_l_btn).setOnClickListener(view ->
                requireActivity().getOnBackPressedDispatcher().onBackPressed());

        contentView.findViewById(R.id.ab_r_btn).setVisibility(View.GONE);

        RecyclerView rv = contentView.findViewById(R.id.crl_rv);
        rv.setLayoutManager(new LinearLayoutManager(requireContext()));
        rv.setAdapter(new PrincipleRecyclerViewAdapter());

        return contentView;
    }

    private void onPrincipleClick(int itemIndex) {
        if (itemIndex != -1) {
            CommonWebFragment fragment = CommonWebFragment.newInstance(sPrincipleText[itemIndex],
                    "file:///android_asset/www/rule/principle/" + itemIndex + ".html");
            requireActivity().getSupportFragmentManager().beginTransaction()
                    .replace(R.id.cml_fl_root, fragment)
                    .addToBackStack(null)
                    .commit();
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
