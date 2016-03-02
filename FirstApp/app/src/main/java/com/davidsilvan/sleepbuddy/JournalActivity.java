package com.davidsilvan.sleepbuddy;

import android.app.AlertDialog;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ExpandableListView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import android.support.v7.app.AppCompatActivity;
import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by David on 2/6/2016.
 */
public class JournalActivity extends AppCompatActivity {

    ExpandableListAdapter listAdapter;
    ExpandableListView expandableListView;
    List<String> headerList;
    HashMap<String, List<String>> childList;

    private Timer timer;
    private File[] files;
    private MediaRecorder myRecorder = null;
    private MediaPlayer myPlayer = null;
    private boolean recording = true;
    private String fileName;
    private File file;
    private TextView currentTime;
    private TextView totalTime;

    @Bind(R.id.recordButton)
    Button button1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.journal_screen);
        ButterKnife.bind(this);

        expandableListView = (ExpandableListView) findViewById(R.id.expandableListView);
    }

    @Override
    protected void onResume() {
        super.onResume();
        prepareLists();
        timer = new Timer();
        expandableListView.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {
            @Override
            public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id) {
                timer.cancel();
                final String name = childList.get(headerList.get(groupPosition)).get(childPosition);
                final String fullName = getFileName() + File.separator + name;
                final String nameNoFileExtension = name.substring(0, name.length() - 4);

                /*final Dialog dialog = new Dialog(JournalActivity.this);
                dialog.setContentView(R.layout.dialog_main);
                dialog.setTitle("Main");
                dialog.show();*/

                AlertDialog.Builder builder = new AlertDialog.Builder(JournalActivity.this);
                builder.setView(R.layout.dialog_main);
                AlertDialog dialog = builder.create();
                dialog.show();

                currentTime = (TextView) dialog.findViewById(R.id.currentTime);
                totalTime = (TextView) dialog.findViewById(R.id.totalTime);
                SeekBar progressBar = (SeekBar) dialog.findViewById(R.id.progressSeekBar);
                Button playButton = (Button) dialog.findViewById(R.id.playButton);

                currentTime.setText("00:00");
                playButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        startPlaying(fullName);
                    }
                });

                /*AlertDialog.Builder builder1 = new AlertDialog.Builder(JournalActivity.this);
                builder1.setMessage("Play?");
                builder1.setCancelable(true);

                builder1.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        startPlaying(fullName);
                        dialog.cancel();
                    }
                });

                builder1.setNegativeButton("No", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });

                builder1.setNeutralButton("Rename", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        final Dialog dialog2 = new Dialog(JournalActivity.this);
                        dialog2.setContentView(R.layout.dialog_rename);
                        dialog2.setTitle("Rename file");
                        dialog2.show();

                        final EditText editFileName = (EditText) dialog2.findViewById(R.id.editTextFileName);
                        Button cancelButton = (Button) dialog2.findViewById(R.id.dialog_cancel);
                        Button okButton = (Button) dialog2.findViewById(R.id.dialog_ok);

                        editFileName.setText(nameNoFileExtension);
                        okButton.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                File file = new File(fullName);
                                String newName = getFileName() + File.separator + editFileName.getText().toString() + ".amr";
                                File file2 = new File(newName);
                                if (file2.exists()) {
                                    Log.i("TEST", "File already exists.");
                                } else {
                                    boolean success = file.renameTo(file2);
                                    if (!success) {
                                        Log.i("TEST", "Error renaming file.");
                                    } else {
                                        prepareLists();
                                    }
                                }
                                dialog2.cancel();
                            }
                        });

                        cancelButton.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                dialog2.cancel();
                            }
                        });
                    }
                });

                AlertDialog dialog = builder1.create();
                dialog.show();*/
                return false;
            }
        });
    }

    public String getFileName() {
        return Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "SleepBuddy";
    }

    private void prepareLists() {
        headerList = new ArrayList<String>();
        childList = new HashMap<String, List<String>>();
        SimpleDateFormat sdf = new SimpleDateFormat("MMMM yyyy");
        List<String> newList = new ArrayList<String>();
        String temp = "";
        int num = 0;

        files = new File(getFileName()).listFiles();
        if (files != null) {
            Arrays.sort(files, new Comparator<File>(){
                public int compare(File f1, File f2)
                {
                    return Long.valueOf(f2.lastModified()).compareTo(f1.lastModified());
                }
            });
            Calendar calendar = Calendar.getInstance();
            for (int i = 0; i < files.length; i++) {
                calendar.setTimeInMillis(files[i].lastModified());
                String header = sdf.format(calendar.getTime());
                if (header.equals(temp)) {
                    newList.add(files[i].getName());
                    childList.put(headerList.get(num - 1), newList);
                } else {
                    headerList.add(header);
                    newList = new ArrayList<String>();
                    newList.add(files[i].getName());
                    childList.put(headerList.get(num), newList);
                    num++;
                }
                temp = header;
            }
        }
        else {
            headerList.add("No files found");
            newList.add("oops");
            childList.put(headerList.get(0), newList);
        }

        listAdapter = new ExpandableListAdapter(JournalActivity.this, headerList, childList);
        expandableListView.setAdapter(listAdapter);
        for (int i = 0; i < headerList.size(); i++)
            expandableListView.expandGroup(i);
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
            prepareLists();
        }
    }

    public void startRecord() {
        SimpleDateFormat sdf = new SimpleDateFormat("MMM_dd_yyyy");
        Calendar calendar = Calendar.getInstance();
        String date = sdf.format(calendar.getTime());
        int num = 0;

        fileName = getFileName();
        file = new File(fileName);
        if (!file.exists() || !file.isDirectory())
            file.mkdir();

        String fileNameEnd = File.separator + date + "_" + num + ".amr";
        fileName = getFileName() + fileNameEnd;
        file = new File(fileName);

        while (file.exists() || file.isFile()) {
            num++;
            fileNameEnd = File.separator + date + "_" + num + ".amr";
            fileName = getFileName() + fileNameEnd;
            file = new File(fileName);
        }
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

    public void startPlaying(String name) {
        myPlayer = new MediaPlayer();
        final SimpleDateFormat sdf = new SimpleDateFormat("mm:ss");
        final Calendar calendar = Calendar.getInstance();

        try {
            myPlayer.setDataSource(name);
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
        timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        calendar.setTimeInMillis(myPlayer.getCurrentPosition());
                        currentTime.setText(sdf.format(calendar.getTime()));
                    }
                });
            }
        }, 0, 1000);
    }

    public void stopPlaying() {
        myPlayer.stop();
        myPlayer.release();
        myPlayer = null;
        Toast.makeText(getApplicationContext(), "Audio playback terminated", Toast.LENGTH_SHORT).show();
    }
}
