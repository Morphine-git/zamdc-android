package com.flycast.wrapper;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;

public class SettingsActivity extends Activity {

    // Must match MainActivity
    public static final String PREFS = "flycast_settings";
    public static final String KEY_HOME_DIR = "home_dir";
    public static final String KEY_CONTENT_DIRS = "content_dirs";

    private TextView tvHomeDir;
    private TextView tvContentDirs;
    private Button btnRefresh;
    private Button btnReset;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        tvHomeDir = findViewById(R.id.tvHomeDirValue);
        tvContentDirs = findViewById(R.id.tvContentDirsValue);
        btnRefresh = findViewById(R.id.btnRefreshSettings);
        btnReset = findViewById(R.id.btnResetSettings);

        btnRefresh.setOnClickListener(v -> loadAndShow());

        btnReset.setOnClickListener(v -> {
            SharedPreferences sp = getSharedPreferences(PREFS, MODE_PRIVATE);

            // Reset to defaults
            String filesDir = getFilesDir().getAbsolutePath();
            File ext = getExternalFilesDir(null);
            String homeDir = (ext != null) ? ext.getAbsolutePath() : filesDir;

            sp.edit()
                    .putString(KEY_HOME_DIR, homeDir)
                    .putString(KEY_CONTENT_DIRS, "")
                    .apply();

            Toast.makeText(this, "Settings reset.", Toast.LENGTH_SHORT).show();
            loadAndShow();
        });

        loadAndShow();
    }

    private void loadAndShow() {
        SharedPreferences sp = getSharedPreferences(PREFS, MODE_PRIVATE);

        String homeDir = sp.getString(KEY_HOME_DIR, "");
        String contentDirs = sp.getString(KEY_CONTENT_DIRS, "");

        if (homeDir == null || homeDir.trim().isEmpty()) homeDir = "(not set)";
        if (contentDirs == null || contentDirs.trim().isEmpty()) contentDirs = "(none)";

        tvHomeDir.setText(homeDir);
        tvContentDirs.setText(contentDirs);
    }
}

