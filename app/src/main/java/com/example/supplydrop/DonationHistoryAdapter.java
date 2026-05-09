package com.example.supplydrop;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

public class DonationHistoryAdapter extends ArrayAdapter<String[]> {

    private Context context;
    private List<String[]> donations;

    public DonationHistoryAdapter(Context context, List<String[]> donations) {
        super(context, 0);
        this.context = context;
        this.donations = donations;
    }

    @Override
    public int getCount() { return donations.size(); }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(context)
                    .inflate(R.layout.list_donation_history_row, parent, false);
        }

        String[] donation = donations.get(position);

        TextView orgTv = convertView.findViewById(R.id.organisationNameTv);
        TextView itemTv = convertView.findViewById(R.id.donatedItemTv);
        TextView amountTv = convertView.findViewById(R.id.donatedAmountTv);

        orgTv.setText(donation[0]);    // organisation name
        itemTv.setText(donation[1]);   // item donated
        amountTv.setText(donation[2]); // amount donated

        return convertView;
    }
}