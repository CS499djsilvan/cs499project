package com.davidsilvan.sleepbuddy;

import android.app.TabActivity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.TabHost;

public class MainActivity extends TabActivity {

    private static final String INTENT_KEY = "Intent Key";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.content_main);

        TabHost host = getTabHost();

        int num = getIntent().getIntExtra(INTENT_KEY, 0);
        Intent journalActivityIntent = new Intent(this, JournalActivity.class);
        if (num == 5) {
            journalActivityIntent.putExtra(INTENT_KEY, 5);
        }
        else {
            journalActivityIntent.putExtra(INTENT_KEY, 0);
        }

        //Tab 1
        TabHost.TabSpec spec = host.newTabSpec("Tab1");
        spec.setContent(new Intent(this, TipsActivity.class));
        spec.setIndicator("Tips");
        host.addTab(spec);

        //Tab 2
        spec = host.newTabSpec("Tab2");
        spec.setContent(journalActivityIntent);
        spec.setIndicator("Journal");
        host.addTab(spec);

        //Tab 3
        spec = host.newTabSpec("Tab3");
        spec.setContent(new Intent(this, NightlightActivity.class));
        spec.setIndicator("Nightlight");
        host.addTab(spec);

        host.setCurrentTab(1);
        //host.setOnTouch
    }
}
