package com.example.supplydrop;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.CheckBox;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
public class DonationExpandableAdapter extends BaseExpandableListAdapter {

    private Context context;
    private List<String> categoryList;                      // group headers
    private Map<String, List<String>> itemMap;              // items per category
    private Map<String, Boolean> checkedItems;             // tracks checkboxes

    public DonationExpandableAdapter(Context context,
                                     List<String> categoryList,
                                     Map<String, List<String>> itemMap) {
        this.context = context;
        this.categoryList = categoryList;
        this.itemMap = itemMap;
        this.checkedItems = new HashMap<>();
    }

    // --- Required overrides ---

    @Override
    public int getGroupCount() {
        return categoryList.size();
    }

    @Override
    public int getChildrenCount(int groupPosition) {
        return itemMap.get(categoryList.get(groupPosition)).size();
    }

    @Override
    public Object getGroup(int groupPosition) {
        return categoryList.get(groupPosition);
    }

    @Override
    public Object getChild(int groupPosition, int childPosition) {
        return itemMap.get(categoryList.get(groupPosition)).get(childPosition);
    }

    @Override
    public long getGroupId(int groupPosition) { return groupPosition; }

    @Override
    public long getChildId(int groupPosition, int childPosition) { return childPosition; }

    @Override
    public boolean hasStableIds() { return false; }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) { return true; }

    // --- Category header view ---
    @Override
    public View getGroupView(int groupPosition, boolean isExpanded,
                             View convertView, ViewGroup parent) {
        String categoryName = (String) getGroup(groupPosition);
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.list_group, parent, false);
        }
        TextView header = convertView.findViewById(R.id.categoryGroupName);
        // Arrow to show open/closed state
        header.setText((isExpanded ? "▼  " : "▶  ") + categoryName);
        return convertView;
    }

    // --- Item row view ---
    @Override
    public View getChildView(int groupPosition, int childPosition, boolean isLastChild,
                             View convertView, ViewGroup parent) {
        String itemName = (String) getChild(groupPosition, childPosition);
        String key = groupPosition + "_" + childPosition; // unique key per item

        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.list_item, parent, false);
        }

        TextView tv = convertView.findViewById(R.id.itemName);
        CheckBox cb = convertView.findViewById(R.id.itemCheckBox);

        tv.setText(itemName);

        // Restore checkbox state without triggering listener
        cb.setOnCheckedChangeListener(null);
        cb.setChecked(checkedItems.containsKey(key) && checkedItems.get(key));

        // Save checkbox state when tapped
        cb.setOnCheckedChangeListener((buttonView, isChecked) -> {
            checkedItems.put(key, isChecked);
        });

        return convertView;
    }

    // --- Call this from your Activity to get all selected items ---
    public List<String> getSelectedItems() {
        List<String> selected = new ArrayList<>();
        for (Map.Entry<String, Boolean> entry : checkedItems.entrySet()) {
            if (entry.getValue()) {
                String[] parts = entry.getKey().split("_");
                int g = Integer.parseInt(parts[0]);
                int c = Integer.parseInt(parts[1]);
                selected.add(categoryList.get(g) + " → " + itemMap.get(categoryList.get(g)).get(c));
            }
        }
        return selected;
    }
}
