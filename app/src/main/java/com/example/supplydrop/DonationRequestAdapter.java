package com.example.supplydrop;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

public class DonationRequestAdapter extends ArrayAdapter<String[]> {

    private Context context;
    private List<String[]> requests;
    private int selectedPosition = -1; // tracks which row is selected

    public DonationRequestAdapter(Context context, List<String[]> requests) {
        super(context, 0);
        this.context = context;
        this.requests = requests;
    }

    @Override
    public int getCount() { return requests.size(); }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(context)
                    .inflate(R.layout.list_donation_request_row, parent, false);
        }

        String[] request = requests.get(position);

        TextView itemTv = convertView.findViewById(R.id.itemNameTv);
        TextView categoryTv = convertView.findViewById(R.id.categoryTv);
        TextView quantityTv = convertView.findViewById(R.id.quantityTv);

        itemTv.setText(request[0]);     // item name
        categoryTv.setText(request[1]); // category
        quantityTv.setText(request[2]); // quantity

        // Highlight selected row
        if (position == selectedPosition) {
            convertView.setBackgroundColor(0xFF2255AA); // blue highlight
        } else {
            convertView.setBackgroundColor(0xBB111111); // default dark
        }

        return convertView;
    }

    public void setSelectedPosition(int position) {
        selectedPosition = position;
        notifyDataSetChanged();
    }

    public int getSelectedPosition() { return selectedPosition; }

    public void updateData(List<String[]> newData) {
        requests = newData;
        selectedPosition = -1;
        notifyDataSetChanged();
    }

    public String[] getSelectedItem() {
        if (selectedPosition == -1) return null;
        return requests.get(selectedPosition);
    }
}