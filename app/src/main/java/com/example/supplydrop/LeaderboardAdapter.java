package com.example.supplydrop;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.List;

public class LeaderboardAdapter extends BaseAdapter {

    private final Context context;
    private List<String[]> data;

    public LeaderboardAdapter(Context context, List<String[]> data) {
        this.context = context;
        this.data = data;
    }

    public void updateData(List<String[]> newData) {
        this.data.clear();
        this.data.addAll(newData);
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return data.size();
    }

    @Override
    public Object getItem(int position) {
        return data.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(context)
                    .inflate(R.layout.list_leaderboard_row, parent, false);
        }

        String[] row = data.get(position);

        TextView positionTv      = convertView.findViewById(R.id.positionTv);
        TextView usernameTv      = convertView.findViewById(R.id.usernameTv);
        TextView donationCountTv = convertView.findViewById(R.id.donationCountTv);

        positionTv.setText(row[0]);
        usernameTv.setText(row[1]);
        donationCountTv.setText(row[2]);

        return convertView;
    }
}