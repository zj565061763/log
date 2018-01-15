package com.fanwe.log;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.fanwe.lib.log.FLogger;

import java.io.File;
import java.io.IOException;
import java.util.logging.FileHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.SimpleFormatter;

public class MainActivity extends AppCompatActivity
{

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        File file = getExternalFilesDir("flog");
        String path = file.getAbsolutePath();
        int limit = 1024 * 1024;
        int count = 2;
        try
        {
            Handler handler = new FileHandler(path, limit, count, true);
            handler.setFormatter(new SimpleFormatter());
            FLogger.get().addHandler(handler);
        } catch (IOException e)
        {
            e.printStackTrace();
        }

        FLogger.get().log(Level.INFO, "onCreate");
    }

    @Override
    protected void onResume()
    {
        super.onResume();
        FLogger.get().log(Level.INFO, "onResume");
    }

    @Override
    protected void onPause()
    {
        super.onPause();
        FLogger.get().log(Level.INFO, "onPause");
    }

    @Override
    protected void onStop()
    {
        super.onStop();
        FLogger.get().log(Level.INFO, "onStop");
    }

    @Override
    protected void onDestroy()
    {
        super.onDestroy();
        FLogger.get().log(Level.INFO, "onDestroy");
    }
}
