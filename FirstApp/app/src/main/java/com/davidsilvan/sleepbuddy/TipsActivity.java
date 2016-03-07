package com.davidsilvan.sleepbuddy;

import android.app.Activity;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import butterknife.ButterKnife;

/**
 * Created by David on 2/6/2016.
 */
public class TipsActivity extends Activity {

    private ListView listView;
    private int[] passcode;
    private List<Tip> tipList = new ArrayList<Tip>() {{
        add(new Tip("Stretch 5 minutes before going to sleep."));
        add(new Tip("Try to go to sleep and get up at the same time every day. This will " +
                "promote your body's natural sleep schedule."));
        add(new Tip("Avoid sleeping in, even after nights that you've stayed up late. Instead, " +
                "try a daytime nap."));
        add(new Tip("If you tend to have a hard time falling and/or staying asleep at night, " +
                "don't nap for too long during the day. Aim for 15-20 minute naps, or just " +
                "eliminate naps altogether."));
        add(new Tip("Avoid bright screens within 2 hours of your bedtime. This includes phones, " +
                "tablets, computers and TVs. Turn the brightness down if you can!"));
        add(new Tip("When it's time to sleep, make sure the room is dark. Consider a sleep mask if " +
                "you cannot control the light in the room."));
        add(new Tip("Keep noise to a minimal while trying to fall asleep. If you need to block out " +
                "outside noise, try running a fan or listening to soothing sounds."));
        add(new Tip("Keep your room slightly cool and well ventilated."));
        add(new Tip("Make sure your bed and pillow are comfortable. If you find yourself waking up " +
                "with a sore neck and/or back, try investing in a new pillow or bed."));
        add(new Tip("Exercise regularly to get better sleep at night and feel less drowsy during " +
                "the day."));
        add(new Tip("Avoid drinking too many liquids near your bedtime, especially if they're " +
                "caffeinated."));
        add(new Tip("Cut down on coffee and other caffeinated drinks, even during the day."));
        add(new Tip("Avoid eating large meals within 2 hours of your bedtime. If it helps, have a " +
                "light snack before bed."));
        add(new Tip("Relax yourself before getting into bed."));
        add(new Tip("If you are unable to fall asleep, try not to think about it. Focus on staying " +
                "relaxed and comfortable."));
        add(new Tip("If none of these tips helped, you might want to consider visiting a doctor. It's " +
                "possible that you may have a sleep disorder."));
    }};

    private TipAdapter tipAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.tips_screen);
        ButterKnife.bind(this);

        passcode = new int[] {0, 0, 0, 0, 0, 0, 0};
        listView = (ListView) findViewById(R.id.listView);
        tipAdapter = new TipAdapter(this, R.layout.tip_list_item, tipList);
        listView.setAdapter(tipAdapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                for (int i = 5; i >= 0; i--) {
                    passcode[i + 1] = passcode[i];
                }
                passcode[0] = position;

                if (passcode[6] == 4 && passcode[5] == 13 && passcode[4] == 3 && passcode[3] == 13
                        && passcode[2] == 3 && passcode[1] == 13 && passcode[0] == 3) {
                    Toast.makeText(getApplicationContext(), "4D3D3D3 Engaged", Toast.LENGTH_LONG).show();
                    MediaPlayer mediaPlayer = MediaPlayer.create(TipsActivity.this, R.raw.celeryman);
                    mediaPlayer.start();
                }
            }
        });
    }
}
