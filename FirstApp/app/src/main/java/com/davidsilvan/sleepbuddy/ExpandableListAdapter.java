package com.davidsilvan.sleepbuddy;

import android.content.Context;
import android.graphics.Typeface;
import android.os.Environment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;

/**
 * Created by David on 2/22/2016.
 */
public class ExpandableListAdapter extends BaseExpandableListAdapter {

    private Context context;
    private List<String> headerList;
    private HashMap<String, List<String>> childList;
    private ArrayList<String> checkedList;
    private CheckBox checkBox;
    private int check;

    public ExpandableListAdapter(Context context, List<String> headerList, HashMap<String, List<String>> childList, int check) {
        this.context = context;
        this.headerList = headerList;
        this.childList = childList;
        this.check = check;
        checkedList = new ArrayList<String>();
    }

    @Override
    public Object getChild(int groupPosition, int childPosition) {
        return childList.get(headerList.get(groupPosition)).get(childPosition);
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return childPosition;
    }

    @Override
    public int getChildrenCount(int groupPosition) {
        return childList.get(headerList.get(groupPosition)).size();
    }

    @Override
    public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
        SimpleDateFormat df2 = new SimpleDateFormat("dd");
        SimpleDateFormat df3 = new SimpleDateFormat("EEE");
        final String childString = (String) getChild(groupPosition, childPosition);
        Calendar calendar = Calendar.getInstance();
        File file = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "SleepBuddy"
                + File.separator + childString);
        calendar.setTimeInMillis(file.lastModified());
        String dayOfMonth = df2.format(calendar.getTime());
        String dayOfWeek = df3.format(calendar.getTime());

        if (convertView == null) {
            LayoutInflater layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = layoutInflater.inflate(R.layout.recording_list_item, null);
        }

        TextView textChild = (TextView) convertView.findViewById(R.id.recordingListItem);
        TextView textChildTitle = (TextView) convertView.findViewById(R.id.recordingListItemTitle);
        TextView textDayOfMonth = (TextView) convertView.findViewById(R.id.textViewDayOfMonth);
        TextView textDayOfWeek = (TextView) convertView.findViewById(R.id.textViewDayOfWeek);
        checkBox = (CheckBox) convertView.findViewById(R.id.checkbox);

        checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    checkedList.add(childString);
                }
                else {
                    checkedList.remove(childString);
                }
            }
        });

        textChildTitle.setVisibility(View.GONE);

        if (check == 0)
            hideCheckbox();
        else
            showCheckbox();

        checkBox.setFocusable(false);
        if (childString.substring(childString.length() - 4, childString.length()).equals(".txt")) {
            StringBuilder text = new StringBuilder();
            textChildTitle.setVisibility(View.VISIBLE);
            try {
                BufferedReader br = new BufferedReader(new FileReader(file));
                String line;

                if ((line = br.readLine()) != null) {
                    text.append(line);
                }
                while ((line = br.readLine()) != null) {
                    text.append('\n');
                    text.append(line);
                }
                br.close();
            }
            catch (IOException e) {
                text.append("Error loading file contents :-(");
            }

            textChildTitle.setText("Title:   " + childString.substring(0, childString.length() - 4));
            textChild.setTextSize(16);
            textChild.setMaxLines(3);
            textChild.setEllipsize(TextUtils.TruncateAt.END);
            textChild.setText("\"" + text + "\"");
        }
        else
            textChild.setText(childString);

        textDayOfMonth.setText(dayOfMonth);
        textDayOfWeek.setText(dayOfWeek);
        return convertView;
    }

    @Override
    public Object getGroup(int groupPosition) {
        return headerList.get(groupPosition);
    }

    @Override
    public long getGroupId(int groupPosition) {
        return groupPosition;
    }

    @Override
    public int getGroupCount() {
        return headerList.size();
    }

    @Override
    public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
        String headerString = (String) getGroup(groupPosition);

        if (convertView == null) {
            LayoutInflater layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = layoutInflater.inflate(R.layout.recording_group_list, null);
        }

        TextView textHeader = (TextView) convertView.findViewById(R.id.listHeader);
        textHeader.setTypeface(null, Typeface.BOLD);
        textHeader.setText(headerString);
        return convertView;
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return true;
    }

    void showCheckbox() {
        checkBox.setVisibility(View.VISIBLE);
    }

    void hideCheckbox() {
        checkBox.setVisibility(View.INVISIBLE);
    }

    int getCheck() {
        return check;
    }

    ArrayList<String> returnChecked() {
        return checkedList;
    }
}
