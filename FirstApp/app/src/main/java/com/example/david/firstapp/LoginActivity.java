package com.example.david.firstapp;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by David on 2/6/2016.
 */
public class LoginActivity extends Activity {

    @Bind(R.id.loginButton)
    Button button1;
    @Bind(R.id.registerButton)
    Button button2;
    @Bind(R.id.iconText)
    TextView textIcon;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login_screen);
        ButterKnife.bind(this);

        textIcon.setShadowLayer(25, 0, 0, Color.BLACK);
    }

    @OnClick(R.id.loginButton)
    void onClickLoginButton() {

    }

    @OnClick(R.id.registerButton)
    void onClickRegisterButton() {

    }
}
