package com.example.forensics_project;

import android.app.Activity;
import android.os.Bundle;

public class FlappyActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(new FlappyView(this));
    }
}



