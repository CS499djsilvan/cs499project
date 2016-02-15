package com.davidsilvan.sleepbuddy;

import android.app.Activity;
import android.os.Bundle;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.List;

import butterknife.ButterKnife;

/**
 * Created by David on 2/6/2016.
 */
public class TipsActivity extends Activity {

    private ListView listView;
    private List<Tip> tipList = new ArrayList<Tip>() {{
        add(new Tip("Stretch 5 minutes before going to sleep"));
        add(new Tip("tip number 2"));
        add(new Tip("tip number 3"));
        add(new Tip("testing testing 1 2 3"));
        add(new Tip("tip number 5"));
        add(new Tip("tip number 6"));
        add(new Tip("tip number 7"));
        add(new Tip("tip number 8"));
        add(new Tip("more tips yo"));
        add(new Tip("last tip"));
    }};

    private TipAdapter tipAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.tips_screen);
        ButterKnife.bind(this);

        listView = (ListView) findViewById(R.id.listView);
        tipAdapter = new TipAdapter(this, R.layout.tip_list_item, tipList);
        listView.setAdapter(tipAdapter);
    }
}
