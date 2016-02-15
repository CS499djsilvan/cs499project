package com.davidsilvan.sleepbuddy;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

/**
 * Created by David on 2/7/2016.
 */
public class TipAdapter extends ArrayAdapter<Tip> {

    private Context context;
    private List<Tip> tipList;

    public TipAdapter(Context context, int resource, List<Tip> objects) {
        super(context, resource, objects);
        this.context = context;
        tipList = objects;
    }


    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Log.i("Test", "getView() called at position " + position);
        LayoutInflater layoutInflater = (LayoutInflater) context.getSystemService(
                Context.LAYOUT_INFLATER_SERVICE);
        View view = layoutInflater.inflate(R.layout.tip_list_item, parent, false);

        final Tip tip = tipList.get(position);
        TextView tipText = (TextView) view.findViewById(R.id.tipText);
        tipText.setText(tip.getTip());

        return view;
    }
}
