package com.example.app;

import android.app.Activity;
import android.app.Application;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

import java.util.Timer;
import java.util.TimerTask;

public class SimpActivity extends Activity
{

    private Timer timer;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_simp);
        timer = new Timer();
        timer.schedule(new TimerTask()
        {
            @Override
            public void run()
            {
                startActivity(new Intent(SimpActivity.this,MainActivity.class));
                finish();
            }
        }, 3000);//延时1

    }

    @Override
    protected void onDestroy()
    {

        super.onDestroy();
    }
}
