package com.example.david.firstapp;

import android.app.Activity;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.Toast;
import java.io.IOException;
import java.util.Calendar;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by David on 2/6/2016.
 */
public class JournalActivity extends Activity {

    private MediaRecorder myRecorder = null;
    private MediaPlayer myPlayer = null;
    private boolean recording = true;
    private boolean playing = true;
    private int day, month, year;
    private String fileName;

    @Bind(R.id.recordButton)
    Button button1;
    @Bind(R.id.playButton)
    Button button2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.journal_screen);
        ButterKnife.bind(this);
    }

    @OnClick(R.id.recordButton)
    void onClickRecordButton() {
        recording = !recording;
        if (!recording) {
            button1.setText("Stop recording");
            startRecord();
        }
        else {
            button1.setText("Start recording");
            stopRecording();
        }
    }

    @OnClick(R.id.playButton)
    void onClickPlayButton() {
        playing = !playing;
        if (!playing) {
            button2.setText("Stop playback");
            startPlaying();
        }
        else {
            button2.setText("Play audio");
            stopPlaying();
        }
    }

    public void startRecord() {
        year = Calendar.YEAR;
        month = Calendar.MONTH;
        day = Calendar.DAY_OF_MONTH;
        fileName = getFilesDir().getAbsolutePath() + "/" + month + "-" + day + "-" + year + ".amr";

        myRecorder = new MediaRecorder();
        myRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        myRecorder.setOutputFormat(MediaRecorder.OutputFormat.AMR_NB);
        myRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
        myRecorder.setOutputFile(fileName);

        try {
            myRecorder.prepare();
        } catch (IOException e) {
            Log.e("AudioRecordTest", "prepare() failed");
        }

        myRecorder.start();
        Toast.makeText(getApplicationContext(), "Recording started", Toast.LENGTH_SHORT).show();
    }

    public void stopRecording() {
        myRecorder.stop();
        myRecorder.release();
        myRecorder = null;
        Toast.makeText(getApplicationContext(), "Audio recorded successfully", Toast.LENGTH_SHORT).show();
    }

    public void startPlaying() {
        myPlayer = new MediaPlayer();

        try {
            myPlayer.setDataSource(fileName);
        } catch (IOException e) {
            Log.e("AudioRecordTest", "setDataSource() failed");
        }

        try {
            myPlayer.prepare();
        } catch (IOException e) {
            Log.e("AudioRecordTest", "prepare() failed");
        }

        myPlayer.start();
        Toast.makeText(getApplicationContext(), "Playing audio", Toast.LENGTH_SHORT).show();
    }

    public void stopPlaying() {
        myPlayer.stop();
        myPlayer.release();
        myPlayer = null;
        Toast.makeText(getApplicationContext(), "Audio playback terminated", Toast.LENGTH_SHORT).show();
    }
}
