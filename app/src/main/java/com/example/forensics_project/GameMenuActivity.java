package com.example.forensics_project;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class GameMenuActivity extends Activity {
    private TextView statusTextView;
    private Button flappyButton;
    private Button centipedeButton;
    private Button asteroidButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game_menu);

        statusTextView = findViewById(R.id.statusTextView);
        flappyButton = findViewById(R.id.flappyButton);
        centipedeButton = findViewById(R.id.centipedeButton);
        asteroidButton = findViewById(R.id.asteroidButton);

        updateLockStateUI();

        flappyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(GameMenuActivity.this, FlappyActivity.class);
                startActivity(i);
            }
        });

        centipedeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(GameMenuActivity.this, WebCentipedeActivity.class);
                startActivity(i);
            }
        });

        asteroidButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(GameMenuActivity.this, WebAsteroidActivity.class);
                startActivity(i);
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Refresh UI in case level state changed while away
        updateLockStateUI();
    }

    private void updateLockStateUI() {
        statusTextView.setText(getString(R.string.all_games_unlocked));

        // Keep buttons enabled
        centipedeButton.setEnabled(true);
        asteroidButton.setEnabled(true);
    }
}



