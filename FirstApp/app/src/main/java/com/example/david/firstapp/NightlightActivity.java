package com.example.david.firstapp;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.pes.androidmaterialcolorpickerdialog.ColorPicker;

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
    @Bind(R.id.nightlight)
    LinearLayout light;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.nightlight_screen);
        ButterKnife.bind(this);

        colorRed = 0;
        colorGreen = 0;
        colorBlue = 0;
    }

    @Override
    protected void onResume() {
        super.onResume();
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
                selectedColor = cp.getColor();
                colorRed = cp.getRed();
                colorGreen = cp.getGreen();
                colorBlue = cp.getBlue();

                cp.dismiss();
                light.setBackgroundColor(selectedColor);
            }
        });
    }
}
