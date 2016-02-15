package com.davidsilvan.sleepbuddy;

import android.app.Activity;
import android.content.ContentResolver;
import android.graphics.Color;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.provider.Settings.System;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;
import com.pes.androidmaterialcolorpickerdialog.ColorPicker;
import java.util.Timer;
import java.util.TimerTask;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by David on 2/6/2016.
 */
public class NightlightActivity extends Activity {

    private String toast = "Tap screen to change color";
    private ColorPicker cp;
    private int selectedColor;
    private int colorRed;
    private int colorGreen;
    private int colorBlue;
    private int brightness;

    private SeekBar brightnessBar;
    private ContentResolver contentResolver;
    private Window window;
    private Timer timer;

    @Bind(R.id.nightlight)
    RelativeLayout light;
    @Bind(R.id.brightnessText)
    TextView brightnessText;
    @Bind(R.id.multiColorButton)
    ToggleButton toggleButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.nightlight_screen);
        ButterKnife.bind(this);

        brightnessBar = (SeekBar) findViewById(R.id.brightnessBar);
        colorRed = 255;
        colorGreen = 255;
        colorBlue = 255;
        window = getWindow();
        contentResolver = getContentResolver();
        brightnessBar.setMax(255);
        timer = new Timer();
    }

    @Override
    protected void onResume() {
        super.onResume();
        setNightlightColor();

        try {
            brightness = System.getInt(contentResolver, System.SCREEN_BRIGHTNESS);
        }
        catch (Settings.SettingNotFoundException e) {
            Log.e("Error", "Cannot access system brightness");
        }

        brightnessText.setText("Brightness: " + Integer.toString((int) (brightness/ 2.55)));
        brightnessBar.setProgress(brightness);
        brightnessBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                brightnessText.setText("Brightness: " + Integer.toString((int) (progress / 2.55)));
                System.putInt(contentResolver, System.SCREEN_BRIGHTNESS, progress);
                WindowManager.LayoutParams layoutParams = window.getAttributes();
                layoutParams.screenBrightness = progress;
                window.setAttributes(layoutParams);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        Toast.makeText(getApplicationContext(), toast, Toast.LENGTH_SHORT).show();
    }

    @OnClick(R.id.nightlight)
    void onClickScreen() {
        final ColorPicker cp = new ColorPicker(NightlightActivity.this, colorRed, colorGreen, colorBlue);
        cp.show();
        Button okColor = (Button)cp.findViewById(R.id.okColorButton);

        okColor.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //toggleButton.setChecked(false);
                selectedColor = cp.getColor();
                colorRed = cp.getRed();
                colorGreen = cp.getGreen();
                colorBlue = cp.getBlue();

                cp.dismiss();
                setNightlightColor();
            }
        });
    }

    @OnClick(R.id.multiColorButton)
    void onClickMultiColorButton() {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (toggleButton.isChecked()) {
                    timer.scheduleAtFixedRate(new TimerTask() {
                        @Override
                        public void run() {
                            if (colorRed > 0 && colorGreen == 255 && colorBlue == 255)
                                colorRed--;
                            if (colorRed == 0 && colorGreen > 0 && colorBlue == 255)
                                colorGreen--;
                            if (colorRed == 0 && colorGreen == 0 && colorBlue > 0)
                                colorBlue--;
                            if (colorRed < 255 && colorGreen == 0 && colorBlue == 0)
                                colorRed++;
                            if (colorRed == 255 && colorGreen < 255 && colorBlue == 0)
                                colorGreen++;
                            if (colorRed == 255 && colorGreen == 255 && colorBlue < 255)
                                colorBlue++;
                        }
                    }, 0, 100);

                    setNightlightColor();
                }
            }
        });
    }

    public void setNightlightColor() {
        light.setBackgroundColor(Color.rgb(colorRed, colorGreen, colorBlue));
    }
}
