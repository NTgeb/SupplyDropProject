package com.example.supplydrop;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

public class RecipientAdapter extends ArrayAdapter<String[]>
{

    private Context context;
    private List<String[]> recipients;

    public RecipientAdapter(Context context, List<String[]> recipients)
    {
        super(context, 0);
        this.context = context;
        this.recipients = recipients;
    }

    @Override
    public int getCount()
    {
        return recipients.size();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent)
    {
        if (convertView == null) // makes a new row if no reuseable row
        {
            convertView = LayoutInflater.from(context).inflate(R.layout.list_recipient_row, parent, false);
        }

        String[] recipient = recipients.get(position); // gets the recip for row

        TextView nameTV = convertView.findViewById(R.id.recipientName);
        TextView itemsTV = convertView.findViewById(R.id.recipientItems);

        nameTV.setText(recipient[0]);  // sets first thing in arr to name
        itemsTV.setText(recipient[1]); // second to items

        return convertView;
    }

    public void updateData(List<String[]> newData)
    {
        recipients = newData;
        notifyDataSetChanged();
    }
}