package com.davidsilvan.sleepbuddy;

import android.Manifest;
import android.app.AlertDialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.Typeface;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
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
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
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

    private static final int MY_PERMISSIONS_REQUEST = 50;
    private static final String INTENT_KEY = "Intent Key";
    private Typeface titleFont, boldTitleFont;
    private Calendar calendar;
    private File[] files;
    private SimpleDateFormat sdf;
    private MediaRecorder myRecorder = null;
    private MediaPlayer myPlayer = null;
    private boolean recording, playing, loop, actionDelete, actionDelete2;
    private String fileName;
    private File file;
    private TabWidget tabWidget;
    private TextView currentTime, totalTime;
    private EditText editFileName, fileNameEditText, dialogNewEditTextTitle, dialogNewEditTextBody;
    private SeekBar progressBar;
    private Button playButton;
    private AlertDialog dialog, dialogRecording, dialogEditTextFile, dialogNewTextFile, dialogChoose;
    private AlertDialog dialogAbout;
    private int currentProgress;
    private Handler handler = new Handler();
    private Runnable runnable = new Runnable() {
        @Override
        public void run() {
            play();
            handler.postDelayed(this, 50);
        }
    };
    private String aboutText = "Hi, my name is David Silvan and I'm currently a student studying Computer Science" +
                               " at Cal Poly Pomona University. This is my first Android app. Hope you enjoy it! " +
                               "Also I'd really appreciate it if you took the time to rate my app in the Google " +
                               "Play Store and/or share the app link with your friends! :-)";

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

        titleFont = Typeface.createFromAsset(getAssets(), "fonts/LobsterTwo-Italic.otf");
        boldTitleFont = Typeface.createFromAsset(getAssets(), "fonts/LobsterTwo-BoldItalic.otf");
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayShowTitleEnabled(false);

        TextView toolbarTitle = (TextView) findViewById(R.id.toolbarTitle);
        toolbarTitle.setTypeface(titleFont);
        toolbarTitle.setShadowLayer(10, 0, 0, Color.BLACK);

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

        AlertDialog.Builder builder3 = new AlertDialog.Builder(JournalActivity.this);
        builder3.setView(inflater.inflate(R.layout.dialog_edittextfile, null));
        dialogEditTextFile = builder3.create();
        dialogEditTextFile.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                prepareLists();
            }
        });

        AlertDialog.Builder builder4 = new AlertDialog.Builder(JournalActivity.this);
        builder4.setView(inflater.inflate(R.layout.dialog_newtextfile, null));
        dialogNewTextFile = builder4.create();
        dialogNewTextFile.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                dialogNewEditTextTitle.setText("");
                dialogNewEditTextBody.setText("");
                prepareLists();
            }
        });

        AlertDialog.Builder builder5 = new AlertDialog.Builder(JournalActivity.this);
        builder5.setView(inflater.inflate(R.layout.dialog_newchoosefile, null));
        dialogChoose = builder5.create();
        dialogChoose.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                //
            }
        });

        AlertDialog.Builder builder6 = new AlertDialog.Builder(JournalActivity.this);
        builder6.setView(inflater.inflate(R.layout.dialog_about, null));
        dialogAbout = builder6.create();
        dialogAbout.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                //
            }
        });

        toolbarTitle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialogAbout.show();
                Button rateAppButton = (Button) dialogAbout.findViewById(R.id.rateAppButton);
                Button shareAppButton = (Button) dialogAbout.findViewById(R.id.shareAppButton);
                Button exitAboutDialogButton = (Button) dialogAbout.findViewById(R.id.exitButton);
                TextView aboutTitle = (TextView) dialogAbout.findViewById(R.id.aboutTextview);
                TextView aboutBody = (TextView) dialogAbout.findViewById(R.id.aboutTextviewBody);
                aboutBody.setText(aboutText);
                aboutTitle.setTypeface(titleFont);
                rateAppButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=com.davidsilvan.sleepbuddy"));
                        startActivity(intent);
                    }
                });
                shareAppButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(Intent.ACTION_SEND);
                        intent.setType("text/plain");
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        intent.putExtra(Intent.EXTRA_TEXT, "Check out this cool dream journal app! \n" +
                                "https://play.google.com/store/apps/details?id=com.davidsilvan.sleepbuddy");
                        startActivity(Intent.createChooser(intent, "Share"));
                    }
                });
                exitAboutDialogButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dialogAbout.cancel();
                    }
                });
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        prepareLists();
        actionDelete = false;
        actionDelete2 = false;
        expandableListView.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {
            @Override
            public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id) {
                final String name = childList.get(headerList.get(groupPosition)).get(childPosition);
                final String fullName = getFileName() + File.separator + name;
                final String nameNoFileExtension = name.substring(0, name.length() - 4);
                String fileExtension = name.substring(name.length() - 4, name.length());
                if (listAdapter.getCheck() == 0) {
                    currentProgress = 0;
                    myPlayer = new MediaPlayer();
                    playing = false;
                    loop = false;

                    if (fileExtension.equals(".txt")) {
                        dialogEditTextFile.show();
                        TextView dialogEditLabel = (TextView) dialogEditTextFile.findViewById(R.id.editJournalLabel);
                        final EditText dialogEditTextTitle = (EditText) dialogEditTextFile.findViewById(R.id.dialogEditTitle);
                        final EditText dialogEditTextBody = (EditText) dialogEditTextFile.findViewById(R.id.dialogEditBody);
                        Button dialogCancelButtonTextfile = (Button) dialogEditTextFile.findViewById(R.id.dialogCancelButtonTextfile);
                        final Button dialogConfirmButtonTextfile = (Button) dialogEditTextFile.findViewById(R.id.saveButton);
                        dialogEditTextTitle.setText(nameNoFileExtension);
                        dialogEditLabel.setTypeface(titleFont);
                        final File tempFile = new File(fullName);
                        StringBuilder text = new StringBuilder();
                        try {
                            BufferedReader br = new BufferedReader(new FileReader(tempFile));
                            String line;

                            while ((line = br.readLine()) != null) {
                                text.append(line);
                                text.append('\n');
                            }
                            br.close();
                        }
                        catch (IOException e) {
                            text.append("Error loading file contents :-(");
                        }
                        dialogEditTextBody.setText(text);
                        dialogConfirmButtonTextfile.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                requestWritePermissions();
                                String title = dialogEditTextTitle.getText().toString();
                                String body = dialogEditTextBody.getText().toString();
                                File newFile = new File(getFileName() + File.separator + title + ".txt");
                                if (!newFile.exists()) {
                                    try {
                                        newFile.createNewFile();
                                    } catch (IOException e) {
                                        Toast.makeText(getApplicationContext(), "Error while creating file", Toast.LENGTH_SHORT).show();
                                    }
                                    boolean temp = tempFile.delete();
                                    if (!temp) {
                                        Log.i("TEST", "file not deleted when renamed");
                                    }
                                }
                                try {
                                    BufferedWriter bw = new BufferedWriter(new FileWriter(newFile));
                                    bw.write(body);
                                    bw.close();
                                    Toast.makeText(getApplicationContext(), "Journal entry saved successfully", Toast.LENGTH_SHORT).show();
                                } catch (IOException e) {
                                    Toast.makeText(getApplicationContext(), "Error while saving file", Toast.LENGTH_SHORT).show();
                                }
                                dialogEditTextFile.cancel();
                            }
                        });
                        dialogCancelButtonTextfile.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                dialogEditTextFile.cancel();
                            }
                        });

                    } else {
                        dialog.show();
                        currentTime = (TextView) dialog.findViewById(R.id.currentTime);
                        totalTime = (TextView) dialog.findViewById(R.id.totalTime);
                        progressBar = (SeekBar) dialog.findViewById(R.id.progressSeekBar);
                        playButton = (Button) dialog.findViewById(R.id.playButton);
                        editFileName = (EditText) dialog.findViewById(R.id.editTextFileName);
                        Button renameButton = (Button) dialog.findViewById(R.id.renameButton);
                        Button closeButton = (Button) dialog.findViewById(R.id.closeButton);

                        progressBar.getProgressDrawable().setColorFilter(Color.rgb(0, 0, 150), PorterDuff.Mode.SRC_IN);
                        progressBar.getThumb().setColorFilter(Color.rgb(0, 0, 150), PorterDuff.Mode.SRC_IN);

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
                                File beforeFile = new File(fullName);
                                String newName = editFileName.getText().toString();
                                String newNameAbsolute = getFileName() + File.separator + newName + ".m4a";
                                File afterFile = new File(newNameAbsolute);
                                if (afterFile.exists()) {
                                    Toast.makeText(getApplicationContext(), "Error: file already exists", Toast.LENGTH_SHORT).show();
                                } else {
                                    boolean success = beforeFile.renameTo(afterFile);
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
                    }

                } else {
                    CheckBox cb = (CheckBox) v.findViewById(R.id.checkbox);
                    if (cb.isChecked()) {
                        cb.setChecked(false);
                    } else {
                        cb.setChecked(true);
                    }
                }
                return false;
            }
        });

        int num = getIntent().getIntExtra(INTENT_KEY, 0);
        Log.i("HOLY", "journal: " + num);
        if (num == 0) {
            //createNotification();
        }
        else if (num == 5) {
            openChooseDialog();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_actionbar, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_delete:
                if (filesExist()) {
                    if (actionDelete) {
                        onDeleteButtonOk();
                    }
                    else if (actionDelete2) {
                        //do nothing
                    }
                    else {
                        actionDelete = true;
                        deleteList = new ArrayList<String>();
                        setListAdapter(1);
                        tabWidget.setVisibility(View.GONE);
                        deleteButtonsContainer.setVisibility(View.VISIBLE);
                    }
                }
                else
                    Toast.makeText(getApplicationContext(), "No files to delete", Toast.LENGTH_SHORT).show();

                return true;

            case R.id.action_record:
                if (!actionDelete) {
                    openChooseDialog();
                }

                return true;

            case R.id.action_settings:
                Intent settings_intent = new Intent(this, SettingsActivity.class);
                startActivity(settings_intent);
                return true;

            default:
                return super.onOptionsItemSelected(item);

        }
    }

    public void openChooseDialog() {
        dialogChoose.show();
        TextView newChooseLabel = (TextView) dialogChoose.findViewById(R.id.newChooseLabel);
        Button chooseButtonAudio = (Button) dialogChoose.findViewById(R.id.chooseButtonAudio);
        Button chooseButtonText = (Button) dialogChoose.findViewById(R.id.chooseButtonText);
        Button chooseButtonCancel = (Button) dialogChoose.findViewById(R.id.chooseButtonCancel);
        chooseButtonAudio.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialogChoose.cancel();
                dialogRecording.show();
                fileNameEditText = (EditText) dialogRecording.findViewById(R.id.editRecordingName);
                TextView newRecordingLabel = (TextView) dialogRecording.findViewById(R.id.newRecordingLabel);
                ImageButton startRecordingButton = (ImageButton) dialogRecording.findViewById(R.id.dialog_startRecording);
                ImageButton stopRecordingButton = (ImageButton) dialogRecording.findViewById(R.id.dialog_stopRecording);
                Button closeButtonRecordingDialog = (Button) dialogRecording.findViewById(R.id.closeButtonRecordDialog);
                createNewFileName();

                newRecordingLabel.setTypeface(titleFont);

                startRecordingButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        File newFile = new File(getFileName() + File.separator + fileNameEditText.getText().toString() + ".m4a");
                        if (newFile.exists() || newFile.isFile())
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
                        } else
                            Toast.makeText(getApplicationContext(), "Recording hasn't been started yet", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
        chooseButtonText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialogChoose.cancel();
                dialogNewTextFile.show();
                TextView newJournalLabel = (TextView) dialogNewTextFile.findViewById(R.id.newJournalLabel);
                dialogNewEditTextTitle = (EditText) dialogNewTextFile.findViewById(R.id.dialogNewEditTitle);
                dialogNewEditTextBody = (EditText) dialogNewTextFile.findViewById(R.id.dialogNewEditBody);
                Button dialogCancelButtonTextfileNew = (Button) dialogNewTextFile.findViewById(R.id.dialogCancelButtonTextfileNew);
                Button dialogSaveButtonTextfileNew = (Button) dialogNewTextFile.findViewById(R.id.dialogSaveButtonTextfileNew);
                dialogSaveButtonTextfileNew.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        requestWritePermissions();
                        String title = dialogNewEditTextTitle.getText().toString();
                        String body = dialogNewEditTextBody.getText().toString();
                        File newFile = new File(getFileName() + File.separator + title + ".txt");
                        if (!newFile.exists()) {
                            try {
                                newFile.createNewFile();
                            } catch (IOException e) {
                                Toast.makeText(getApplicationContext(), "Error while creating file", Toast.LENGTH_SHORT).show();
                            }
                        }
                        try {
                            BufferedWriter bw = new BufferedWriter(new FileWriter(newFile));
                            bw.write(body);
                            bw.close();
                            Toast.makeText(getApplicationContext(), "Journal entry saved successfully", Toast.LENGTH_SHORT).show();
                        } catch (IOException e) {
                            Toast.makeText(getApplicationContext(), "Error while saving file", Toast.LENGTH_SHORT).show();
                        }
                        dialogNewTextFile.cancel();
                    }
                });
                dialogCancelButtonTextfileNew.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dialogNewTextFile.cancel();
                    }
                });
                newJournalLabel.setTypeface(titleFont);
            }
        });
        chooseButtonCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialogChoose.cancel();
            }
        });
        newChooseLabel.setTypeface(titleFont);
    }

    @OnClick(R.id.deleteButtonCancel)
    void onDeleteButtonCancel() {
        actionDelete = false;
        setListAdapter(0);
        deleteList = null;
        tabWidget.setVisibility(View.VISIBLE);
        deleteButtonsContainer.setVisibility(View.GONE);
    }

    @OnClick(R.id.deleteButtonOk)
    void onDeleteButtonOk() {
        requestWritePermissions();
        final View coordinatorLayoutView = findViewById(R.id.snackbarlocation);
        actionDelete = false;
        int num = moveFilesToDelete();
        Snackbar snackbarNoFilesDeleted = Snackbar.make(coordinatorLayoutView, num + " file(s) deleted!", Snackbar.LENGTH_SHORT);
        Snackbar snackbar = Snackbar.make(coordinatorLayoutView, num + " file(s) deleted", Snackbar.LENGTH_LONG)
                .setAction("UNDO", new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        int numRestored = undoDeleteFiles();
                        Snackbar snackbar2 = Snackbar.make(coordinatorLayoutView, numRestored + " file(s) restored!", Snackbar.LENGTH_SHORT);
                        snackbar2.show();
                        prepareLists();
                    }
                });
        snackbar.setCallback(new Snackbar.Callback() {
            @Override
            public void onDismissed(Snackbar snackbar, int event) {
                super.onDismissed(snackbar, event);
                deleteFiles();
                actionDelete2 = false;
            }

            @Override
            public void onShown(Snackbar snackbar) {
                super.onShown(snackbar);
                actionDelete2 = true;
            }
        });
        if (num == 0) {
            snackbarNoFilesDeleted.show();
        }
        else
            snackbar.show();
        prepareLists();
        tabWidget.setVisibility(View.VISIBLE);
        deleteButtonsContainer.setVisibility(View.GONE);
    }

    public String getFileName() {
        return Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "SleepBuddy";
    }

    public int moveFilesToDelete() {
        deleteList.addAll(listAdapter.returnChecked());
        File beforeMoveFile;
        File movedFile;
        String newDir = getFileName() + File.separator + "deleted";
        file = new File(newDir);
        if (!file.exists() || !file.isDirectory())
            file.mkdir();
        int numFiles = deleteList.size();
        int numFilesDeleted = numFiles;
        for (int i = 0; i < numFiles; i++) {
            String currentFile = getFileName() + File.separator + deleteList.get(i);
            String newFileDir = newDir + File.separator + deleteList.get(i);
            beforeMoveFile = new File(currentFile);
            movedFile = new File(newFileDir);
            boolean moved = beforeMoveFile.renameTo(movedFile);
            if (!moved)
                numFilesDeleted--;
        }
        return numFilesDeleted;
    }

    public int undoDeleteFiles() {
        File beforeMoveFile;
        File movedFile;
        String newDir = getFileName() + File.separator + "deleted";
        int numFiles = deleteList.size();
        int numFilesRestored = numFiles;
        for (int i = 0; i < numFiles; i++) {
            String currentFile = newDir + File.separator + deleteList.get(i);
            String newFileDir = getFileName() + File.separator + deleteList.get(i);
            beforeMoveFile = new File(currentFile);
            movedFile = new File(newFileDir);
            boolean moved = beforeMoveFile.renameTo(movedFile);
            if (!moved)
                numFilesRestored--;
        }
        deleteList = null;
        return numFilesRestored;
    }

    public void deleteFiles() {
        File deleteFileDir = new File(getFileName() + File.separator + "deleted");
        if (deleteFileDir.isDirectory()) {
            File[] contents = deleteFileDir.listFiles();
            if (contents != null) {
                for (File currentFile : contents) {
                    currentFile.delete();
                }
            }
            new File(getFileName() + File.separator + "deleted" + File.separator).delete();
        }
        deleteList = null;
    }

    private boolean filesExist() {
        boolean filesExist = false;
        files = new File(getFileName()).listFiles();
        if (files != null) {
            for (int i = 0; i < files.length; i++) {
                String str = files[i].getName();
                String sub = str.substring(str.length() - 4, str.length());
                if (sub.equals(".m4a") || sub.equals(".txt")) {
                    filesExist = true;
                    break;
                }
            }
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
            Arrays.sort(files, new Comparator<File>() {
                public int compare(File f1, File f2) {
                    return Long.valueOf(f2.lastModified()).compareTo(f1.lastModified());
                }
            });
            Calendar calendar = Calendar.getInstance();
            for (int i = 0; i < files.length; i++) {
                if (!files[i].isDirectory()) {
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
            }
            setListAdapter(0);
        } else {
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
        if (myRecorder != null) {
            myRecorder.release();
        }
        requestRecordPermissions();
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
        } else
            playButton.setText("Play");
        myPlayer.pause();
    }

    public void stopPlaying() {
        myPlayer.stop();
        myPlayer.release();
        myPlayer = null;
    }

    public void createNotification() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.putExtra(INTENT_KEY, 5);
        PendingIntent pIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        Notification notif = new Notification.Builder(this)
                .setContentTitle("Sleep Buddy")
                .setContentText("Press for new dream journal entry")
                .setSmallIcon(R.drawable.plus)
                .setContentIntent(pIntent).build();

        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        notif.flags |= Notification.FLAG_AUTO_CANCEL;

        notificationManager.notify(0, notif);
    }

    public void requestRecordPermissions() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO}, MY_PERMISSIONS_REQUEST);
        }
        requestWritePermissions();
    }

    public void requestWritePermissions() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, MY_PERMISSIONS_REQUEST);
        }
    }
}
