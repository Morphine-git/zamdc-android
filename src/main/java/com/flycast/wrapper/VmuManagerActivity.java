package com.flycast.wrapper;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;
import com.flycast.emulator.Core;

public class VmuManagerActivity extends Activity {

    public static final String EXTRA_BIOS_PATH = "EXTRA_BIOS_PATH";

    private String biosPath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vmu_manager);

        Intent intent = getIntent();
        biosPath = intent.getStringExtra(EXTRA_BIOS_PATH);

        Button btnOpen = findViewById(R.id.btnOpenVmuManager);
        btnOpen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openDreamcastFileManager();
            }
        });
    }

    private void openDreamcastFileManager() {
        if (biosPath == null || biosPath.isEmpty()) {
            Toast.makeText(this,
                    "No BIOS image found for VMU manager.",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        // Boot BIOS same as a game – from there user picks File → memory cards
        Core.loadGame(biosPath);

        // Close this screen so emulator is visible
        finish();
    }
}

