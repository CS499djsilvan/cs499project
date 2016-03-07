package com.davidsilvan.sleepbuddy;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.Typeface;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ExpandableListView;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TabHost;
import android.widget.TabWidget;
import android.widget.TextView;
import android.widget.Toast;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

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
    List<String> deleteList;
    HashMap<String, List<String>> childList;

    private Calendar calendar;
    private File[] files;
    private SimpleDateFormat sdf;
    private MediaRecorder myRecorder = null;
    private MediaPlayer myPlayer = null;
    private boolean recording, playing, loop;
    private String fileName;
    private File file;
    private TabWidget tabWidget;
    private TextView currentTime, totalTime;
    private EditText editFileName, fileNameEditText;
    private SeekBar progressBar;
    private Button playButton;
    private AlertDialog dialog, dialogRecording;
    private int currentProgress;
    private Handler handler = new Handler();
    private Runnable runnable = new Runnable() {
        @Override
        public void run() {
                play();
                handler.postDelayed(this, 50);
        }
    };

    @Bind(R.id.noFilesFoundText)
    TextView noFilesFound;
    @Bind(R.id.deleteButtonsContainer)
    LinearLayout deleteButtonsContainer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.journal_screen);
        ButterKnife.bind(this);

        Toolbar myToolbar = (Toolbar) findViewById(R.id.my_toolbar);
        setSupportActionBar(myToolbar);

        Typeface titleFont = Typeface.createFromAsset(getAssets(), "fonts/LobsterTwo-BoldItalic.otf");

        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayShowTitleEnabled(false);
        TextView toolbarTitle= (TextView) findViewById(R.id.toolbarTitle);
        toolbarTitle.setTypeface(titleFont);
        toolbarTitle.setShadowLayer(10, 0, 0, Color.BLACK);
        toolbarTitle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i("TEST", "yo");
            }
        });

        MainActivity activity = (MainActivity) this.getParent();
        TabHost tabHost = activity.getTabHost();
        tabWidget = tabHost.getTabWidget();

        recording = false;
        calendar = Calendar.getInstance();
        expandableListView = (ExpandableListView) findViewById(R.id.expandableListView);
        sdf = new SimpleDateFormat("mm:ss");
        AlertDialog.Builder builder = new AlertDialog.Builder(JournalActivity.this);
        LayoutInflater inflater = getLayoutInflater();
        builder.setView(inflater.inflate(R.layout.dialog_main, null));
        dialog = builder.create();
        dialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                playing = false;
                handler.removeCallbacks(runnable);
                stopPlaying();
            }
        });

        AlertDialog.Builder builder2 = new AlertDialog.Builder(JournalActivity.this);
        builder2.setView(inflater.inflate(R.layout.dialog_record, null));
        dialogRecording = builder2.create();
        dialogRecording.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                if (recording) {
                    recording = false;
                    myRecorder.stop();
                    myRecorder.release();
                    myRecorder = null;
                    String str = getFileName() + File.separator + fileNameEditText.getText().toString() + ".m4a";
                    File deleteFile = new File(str);
                    deleteFile.delete();
                }
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        prepareLists();
        expandableListView.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {
            @Override
            public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id) {
                final String name = childList.get(headerList.get(groupPosition)).get(childPosition);
                final String fullName = getFileName() + File.separator + name;
                final String nameNoFileExtension = name.substring(0, name.length() - 4);
                if (listAdapter.getCheck() == 0) {
                    currentProgress = 0;
                    myPlayer = new MediaPlayer();
                    playing = false;
                    loop = false;

                    dialog.show();
                    currentTime = (TextView) dialog.findViewById(R.id.currentTime);
                    totalTime = (TextView) dialog.findViewById(R.id.totalTime);
                    progressBar = (SeekBar) dialog.findViewById(R.id.progressSeekBar);
                    playButton = (Button) dialog.findViewById(R.id.playButton);
                    editFileName = (EditText) dialog.findViewById(R.id.editTextFileName);
                    Button renameButton = (Button) dialog.findViewById(R.id.renameButton);
                    Button closeButton = (Button) dialog.findViewById(R.id.closeButton);

                    editFileName.setText(nameNoFileExtension);
                    playButton.setText("Play");
                    try {
                        myPlayer.setDataSource(fullName);
                    } catch (IOException e) {
                        Log.e("AudioRecordTest", "setDataSource() failed");
                    }

                    try {
                        myPlayer.prepare();
                    } catch (IOException e) {
                        Log.e("AudioRecordTest", "prepare() failed");
                    }

                    progressBar.setMax(50);
                    progressBar.setProgress(0);
                    progressBar.setProgress(20);
                    currentTime.setText("00:00");
                    calendar.setTimeInMillis(myPlayer.getDuration());
                    totalTime.setText(sdf.format(calendar.getTime()));
                    progressBar.setMax(myPlayer.getDuration());
                    progressBar.setProgress(0);
                    dialog.findViewById(android.R.id.content).invalidate();

                    progressBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                        @Override
                        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                            if (fromUser) {
                                loop = false;
                                myPlayer.seekTo(progress);
                                currentProgress = progress;
                                calendar.setTimeInMillis(progress);
                                currentTime.setText(sdf.format(calendar.getTime()));
                            }
                        }

                        @Override
                        public void onStartTrackingTouch(SeekBar seekBar) {

                        }

                        @Override
                        public void onStopTrackingTouch(SeekBar seekBar) {

                        }
                    });

                    playButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            playing = !playing;
                            if (playing) {
                                if (loop) {
                                    currentProgress = 0;
                                    loop = false;
                                }
                                playButton.setText("Pause");
                                myPlayer.seekTo(currentProgress);
                                myPlayer.start();
                                handler.postDelayed(runnable, 0);
                            } else {
                                pause();
                            }
                        }
                    });

                    renameButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            File file = new File(fullName);
                            String newName = editFileName.getText().toString();
                            String newNameAbsolute = getFileName() + File.separator + newName + ".m4a";
                            File file2 = new File(newNameAbsolute);
                            if (file2.exists()) {
                                Toast.makeText(getApplicationContext(), "Error: file already exists", Toast.LENGTH_SHORT).show();
                            } else {
                                boolean success = file.renameTo(file2);
                                if (!success) {
                                    Toast.makeText(getApplicationContext(), "Error: couldn't rename file", Toast.LENGTH_SHORT).show();
                                } else {
                                    prepareLists();
                                    Toast.makeText(getApplicationContext(), "File renamed to: \"" + newName +
                                            ".m4a\"", Toast.LENGTH_SHORT).show();
                                }
                            }
                        }
                    });

                    closeButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            dialog.cancel();
                        }
                    });
                } else {
                    CheckBox cb = (CheckBox) v.findViewById(R.id.checkbox);
                    if (cb.isChecked()) {
                        cb.setChecked(false);
                        deleteList.remove(name);
                    } else {
                        cb.setChecked(true);
                        deleteList.add(name);
                    }
                }
                return false;
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_actionbar, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_delete:
                if (filesExist()) {
                    deleteList = new ArrayList<String>();
                    setListAdapter(1);
                    tabWidget.setVisibility(View.GONE);
                    deleteButtonsContainer.setVisibility(View.VISIBLE);
                }
                else
                    Toast.makeText(getApplicationContext(), "No files to delete", Toast.LENGTH_SHORT).show();

                return true;

            case R.id.action_record:
                dialogRecording.show();
                fileNameEditText = (EditText) dialogRecording.findViewById(R.id.editRecordingName);
                ImageButton startRecordingButton = (ImageButton) dialogRecording.findViewById(R.id.dialog_startRecording);
                ImageButton stopRecordingButton = (ImageButton) dialogRecording.findViewById(R.id.dialog_stopRecording);
                Button closeButtonRecordingDialog = (Button) dialogRecording.findViewById(R.id.closeButtonRecordDialog);
                createNewFileName();

                startRecordingButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        File file = new File(getFileName() + File.separator + fileNameEditText.getText().toString() + ".m4a");
                        if (file.exists() || file.isFile())
                            Toast.makeText(getApplicationContext(), "Error: filename already exists", Toast.LENGTH_SHORT).show();
                        else {
                            recording = true;
                            startRecord();
                        }
                    }
                });

                closeButtonRecordingDialog.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dialogRecording.cancel();
                    }
                });

                stopRecordingButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (recording) {
                            recording = false;
                            stopRecording();
                            prepareLists();
                            dialogRecording.cancel();
                        }
                        else
                            Toast.makeText(getApplicationContext(), "Recording hasn't been started yet", Toast.LENGTH_SHORT).show();
                    }
                });
                return true;

            default:
                return super.onOptionsItemSelected(item);

        }
    }

    @OnClick(R.id.deleteButtonCancel)
    void onDeleteButtonCancel() {
        setListAdapter(0);
        deleteList = null;
        tabWidget.setVisibility(View.VISIBLE);
        deleteButtonsContainer.setVisibility(View.GONE);
    }

    @OnClick(R.id.deleteButtonOk)
    void onDeleteButtonOk() {
        File deleteFile;
        int check = 0, numFiles = deleteList.size();
        for (int i = 0; i < numFiles; i++) {
            String str = getFileName() + File.separator + deleteList.get(i);
            deleteFile = new File(str);
            boolean deleted = deleteFile.delete();
            if (!deleted && check == 0) {
                Toast.makeText(getApplicationContext(), "Error deleting file(s)", Toast.LENGTH_SHORT).show();
                check = 1;
            }
        }
        if (check == 0)
            Toast.makeText(getApplicationContext(), numFiles + " file(s) deleted", Toast.LENGTH_SHORT).show();
        deleteList = null;
        prepareLists();
        tabWidget.setVisibility(View.VISIBLE);
        deleteButtonsContainer.setVisibility(View.GONE);
    }

    public String getFileName() {
        return Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "SleepBuddy";
    }

    private boolean filesExist() {
        boolean filesExist = false;
        files = new File(getFileName()).listFiles();
        for (int i = 0; i < files.length; i++) {
            String str = files[i].getName();
            String sub = str.substring(str.length() - 4, str.length());
            if (sub.equals(".m4a"))
                filesExist = true;
        }
        return filesExist;
    }

    private void prepareLists() {
        headerList = new ArrayList<String>();
        childList = new HashMap<String, List<String>>();
        SimpleDateFormat sdf2 = new SimpleDateFormat("MMMM yyyy");
        List<String> newList = new ArrayList<String>();
        String temp = "";
        int num = 0;

        boolean filesExist = filesExist();
        if (filesExist) {
            noFilesFound.setVisibility(View.GONE);
            expandableListView.setVisibility(View.VISIBLE);
            Arrays.sort(files, new Comparator<File>(){
                public int compare(File f1, File f2)
                {
                    return Long.valueOf(f2.lastModified()).compareTo(f1.lastModified());
                }
            });
            Calendar calendar = Calendar.getInstance();
            for (int i = 0; i < files.length; i++) {
                calendar.setTimeInMillis(files[i].lastModified());
                String header = sdf2.format(calendar.getTime());
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
            setListAdapter(0);
        }
        else {
            noFilesFound.setVisibility(View.VISIBLE);
            expandableListView.setVisibility(View.GONE);
        }
    }

    public void setListAdapter(int check) {
        if (check == 0)
            listAdapter = new ExpandableListAdapter(JournalActivity.this, headerList, childList, 0);
        else
            listAdapter = new ExpandableListAdapter(JournalActivity.this, headerList, childList, 1);
        expandableListView.setAdapter(listAdapter);
        for (int i = 0; i < headerList.size(); i++)
            expandableListView.expandGroup(i);
    }

    public void createNewFileName() {
        SimpleDateFormat sdf2 = new SimpleDateFormat("MMM_dd_yyyy");
        calendar = Calendar.getInstance();
        String date = sdf2.format(calendar.getTime());
        int num = 0;

        fileName = getFileName();
        file = new File(fileName);
        if (!file.exists() || !file.isDirectory())
            file.mkdir();

        String fileNameEndNoExtension = date + "_" + num;
        fileName = getFileName() + File.separator + fileNameEndNoExtension + ".m4a";
        file = new File(fileName);

        while (file.exists() || file.isFile()) {
            num++;
            fileNameEndNoExtension = date + "_" + num;
            fileName = getFileName() + File.separator + fileNameEndNoExtension + ".m4a";
            file = new File(fileName);
        }

        fileNameEditText.setText(fileNameEndNoExtension);
    }

    public void startRecord() {
        myRecorder = new MediaRecorder();
        myRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        myRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
        myRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
        myRecorder.setAudioEncodingBitRate(96000);
        myRecorder.setAudioSamplingRate(44100);
        myRecorder.setOutputFile(getFileName() + File.separator + fileNameEditText.getText().toString() + ".m4a");

        try {
            myRecorder.prepare();
        } catch (IOException e) {
            Toast.makeText(getApplicationContext(), "Error: recording couldn't be started", Toast.LENGTH_SHORT).show();
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

    public void play() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                calendar.setTimeInMillis(myPlayer.getCurrentPosition());
                currentTime.setText(sdf.format(calendar.getTime()));
                progressBar.setProgress(myPlayer.getCurrentPosition());
                currentProgress = myPlayer.getCurrentPosition();
                if (!myPlayer.isPlaying()) {
                    pause();
                }
            }
        });
    }

    public void pause() {
        playing = false;
        if (myPlayer.getDuration() - currentProgress <= 26) {
            loop = true;
            playButton.setText("Replay");
        }
        else
            playButton.setText("Play");
        myPlayer.pause();
    }

    public void stopPlaying() {
        myPlayer.stop();
        myPlayer.release();
        myPlayer = null;
    }
}
