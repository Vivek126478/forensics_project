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
                if (LevelManager.isCentipedeUnlocked(GameMenuActivity.this)) {
                    Intent i = new Intent(GameMenuActivity.this, WebCentipedeActivity.class);
                    startActivity(i);
                } else {
                    statusTextView.setText(getString(R.string.centipede_locked));
                }
            }
        });

        asteroidButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (LevelManager.isAsteroidUnlocked(GameMenuActivity.this)) {
                    Intent i = new Intent(GameMenuActivity.this, WebAsteroidActivity.class);
                    startActivity(i);
                } else {
                    statusTextView.setText(getString(R.string.asteroid_locked));
                }
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
        StringBuilder sb = new StringBuilder();
        sb.append(getString(R.string.level_status_prefix))
          .append(" ")
          .append(LevelManager.getCurrentLevel(this));
        statusTextView.setText(sb.toString());

        // Keep buttons enabled; click handlers enforce lock rules and show messages
        centipedeButton.setEnabled(true);
        asteroidButton.setEnabled(true);
    }
}



