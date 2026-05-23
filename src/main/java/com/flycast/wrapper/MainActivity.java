package com.flycast.wrapper;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.flycast.emulator.NativeGLActivity;

import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public class MainActivity extends Activity implements GameGridAdapter.OnGameClickListener {

    private static final String TAG = "MainActivity";
    private static final int PERMISSION_REQUEST_LEGACY = 1001;
    private static final int REQ_ALL_FILES_ACCESS = 1002;

    private TextView tvTitle;
    private TextView tvEmpty;
    private TextView tvScanStatus;
    private LinearLayout scanProgressContainer;
    private LinearLayout bottomButtons;
    private ProgressBar progressScan;
    private RecyclerView rvGames;
    private Button btnAddGamesFolder;
    private Button btnAutoScan;
    private ImageButton btnSettingsCircle;

    private final List<GameEntry> gameList = new ArrayList<>();
    private GameGridAdapter gameAdapter;

    private String biosRomPath;
    private String flashPath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().setFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN
        );

        setContentView(R.layout.activity_main);

        tvTitle = findViewById(R.id.tvTitle);
        tvEmpty = findViewById(R.id.tvEmpty);
        tvScanStatus = findViewById(R.id.tvScanStatus);
        scanProgressContainer = findViewById(R.id.scanProgressContainer);
        bottomButtons = findViewById(R.id.bottomButtons);
        progressScan = findViewById(R.id.progressScan);
        rvGames = findViewById(R.id.rvGames);
        btnAddGamesFolder = findViewById(R.id.btnAddGamesFolder);
        btnAutoScan = findViewById(R.id.btnAutoScan);
        btnSettingsCircle = findViewById(R.id.btn_settings_circle);

        rvGames.setLayoutManager(new GridLayoutManager(this, 4));
        gameAdapter = new GameGridAdapter(gameList, this);
        rvGames.setAdapter(gameAdapter);

        btnAutoScan.setOnClickListener(v -> checkPermissionsThenScan());

        btnAddGamesFolder.setOnClickListener(v ->
                Toast.makeText(this, "Folder picker coming later.", Toast.LENGTH_SHORT).show()
        );

        btnSettingsCircle.setOnClickListener(v ->
                Toast.makeText(this, "Settings TBD", Toast.LENGTH_SHORT).show()
        );

        btnSettingsCircle.setOnLongClickListener(v -> {
            openVmuTool();
            return true;
        });

        checkPermissionsThenScan();
    }

    private void checkPermissionsThenScan() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            if (!Environment.isExternalStorageManager()) {
                requestAllFilesAccess();
                return;
            }
            autoScanStorage();
            return;
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(
                        this,
                        new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                        PERMISSION_REQUEST_LEGACY
                );
                return;
            }
        }

        autoScanStorage();
    }

    private void requestAllFilesAccess() {
        try {
            Toast.makeText(this, "Please allow All Files Access for Auto Scan.", Toast.LENGTH_LONG).show();
            Intent intent = new Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION);
            intent.setData(Uri.parse("package:" + getPackageName()));
            startActivityForResult(intent, REQ_ALL_FILES_ACCESS);
        } catch (Throwable t) {
            Intent intent = new Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION);
            startActivityForResult(intent, REQ_ALL_FILES_ACCESS);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQ_ALL_FILES_ACCESS) {
            checkPermissionsThenScan();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == PERMISSION_REQUEST_LEGACY) {
            boolean granted = grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED;
            if (!granted) {
                Toast.makeText(this, "Storage permission denied.", Toast.LENGTH_SHORT).show();
                return;
            }
            autoScanStorage();
        }
    }

    private void autoScanStorage() {
        scanProgressContainer.setVisibility(View.VISIBLE);
        progressScan.setIndeterminate(true);
        tvScanStatus.setText("Scan files 0%");

        gameList.clear();
        biosRomPath = null;
        flashPath = null;
        gameAdapter.notifyDataSetChanged();
        tvEmpty.setVisibility(View.GONE);

        try {
            File out = new File(getFilesDir(), "scan_report.txt");
            if (out.exists()) out.delete();
        } catch (Throwable ignored) {
        }

        appendScanLogLine("=== AUTO SCAN START ===");
        appendScanLogLine("API=" + Build.VERSION.SDK_INT);

        new Thread(() -> {
            Set<String> roots = getScanRoots();
            for (String root : roots) {
                appendScanLogLine("SCAN_ROOT: " + root);
                scanFolderForGames(new File(root), gameList);
            }

            appendScanLogLine("BIOS=" + (biosRomPath == null ? "NOT FOUND" : biosRomPath));
            appendScanLogLine("FLASH=" + (flashPath == null ? "NOT FOUND" : flashPath));
            appendScanLogLine("GAMES_FOUND=" + gameList.size());
            appendScanLogLine("=== AUTO SCAN END ===");

            runOnUiThread(() -> {
                progressScan.setIndeterminate(false);
                progressScan.setProgress(100);
                tvScanStatus.setText("Scan files 100%");
                scanProgressContainer.setVisibility(View.GONE);

                gameAdapter.notifyDataSetChanged();

                if (gameList.isEmpty()) {
                    tvEmpty.setText("Found 0 games");
                } else {
                    tvEmpty.setText("Found " + gameList.size() + " games");
                }
                tvEmpty.setVisibility(View.VISIBLE);

                Toast.makeText(
                        MainActivity.this,
                        "Scan done. scan_report.txt saved in app files.",
                        Toast.LENGTH_SHORT
                ).show();
            });
        }).start();
    }

    private Set<String> getScanRoots() {
        Set<String> roots = new LinkedHashSet<>();

        try {
            File[] dirs = getExternalFilesDirs(null);
            if (dirs != null) {
                for (File dir : dirs) {
                    if (dir == null) continue;

                    String fullPath = dir.getAbsolutePath();
                    String rootPath = fullPath;

                    int androidIdx = fullPath.indexOf("/Android/");
                    if (androidIdx > 0) {
                        rootPath = fullPath.substring(0, androidIdx);
                    }

                    if (new File(rootPath).exists()) {
                        roots.add(rootPath);
                        Log.i(TAG, "Scan root from getExternalFilesDirs: " + rootPath);
                    }
                }
            }
        } catch (Throwable t) {
            Log.w(TAG, "getExternalFilesDirs failed", t);
        }

        try {
            File internalRoot = Environment.getExternalStorageDirectory();
            if (internalRoot != null && internalRoot.exists()) {
                roots.add(internalRoot.getAbsolutePath());
                Log.i(TAG, "Scan root from Environment: " + internalRoot.getAbsolutePath());
            }
        } catch (Throwable t) {
            Log.w(TAG, "Environment external storage root failed", t);
        }

        return roots;
    }

    private void scanFolderForGames(File folder, List<GameEntry> out) {
        if (folder == null || !folder.exists() || !folder.isDirectory()) return;

        File[] files = folder.listFiles();
        if (files == null) return;

        for (File f : files) {
            if (f.isDirectory()) {
                scanFolderForGames(f, out);
                continue;
            }

            String lower = f.getName().toLowerCase(Locale.US);

            if (isFlashFile(lower)) {
                flashPath = f.getAbsolutePath();
                appendScanLogLine("FOUND_FLASH: " + flashPath);
                try {
                    SystemFiles.setFlashPath(MainActivity.this, flashPath);
                } catch (Throwable ignored) {
                }
                continue;
            }

            if (isBiosFile(lower)) {
                biosRomPath = f.getAbsolutePath();
                appendScanLogLine("FOUND_BIOS: " + biosRomPath);
                try {
                    SystemFiles.addBiosPath(MainActivity.this, biosRomPath);
                } catch (Throwable ignored) {
                }
                continue;
            }

            if (!lower.endsWith(".cdi") && !lower.endsWith(".gdi")) continue;

            String romPath = f.getAbsolutePath();
            out.add(new GameEntry(cleanTitle(f.getName()), romPath, null));
            appendScanLogLine("FOUND_GAME: " + romPath);
        }
    }

    private boolean isBiosFile(String name) {
        return name.equals("dc_boot.bin")
                || name.contains("dc_boot")
                || name.contains("dreamcast")
                || name.contains("bios");
    }

    private boolean isFlashFile(String name) {
        return name.equals("dc_flash.bin")
                || name.contains("dc_flash")
                || name.contains("flash");
    }

    private String cleanTitle(String name) {
        int dot = name.lastIndexOf('.');
        if (dot > 0) {
            name = name.substring(0, dot);
        }
        return name.replace('_', ' ').trim();
    }

    private void appendScanLogLine(String line) {
        try {
            File out = new File(getFilesDir(), "scan_report.txt");
            try (FileWriter fw = new FileWriter(out, true)) {
                fw.write(line + "\n");
            }
        } catch (Throwable t) {
            Log.w(TAG, "scan_report write failed", t);
        }
    }

    private void openGame(String romPath) {
        if (romPath == null || romPath.isEmpty()) return;

        try {
            Toast.makeText(this, "Launching game...", Toast.LENGTH_SHORT).show();
            Log.i(TAG, "openGame(): " + romPath);

            Intent i = new Intent(this, NativeGLActivity.class);
            i.setData(Uri.fromFile(new File(romPath)));
            i.putExtra(NativeGLActivity.EXTRA_GAME_PATH, romPath);

            if (biosRomPath != null) {
                i.putExtra("bios_path", biosRomPath);
            }
            if (flashPath != null) {
                i.putExtra("flash_path", flashPath);
            }

            // IMPORTANT: no CLEAR_TOP / NEW_TASK flags here

            startActivity(i);
            finish();

        } catch (Throwable t) {
            Log.e(TAG, "openGame failed", t);
            Toast.makeText(this, "Failed to launch game", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onGameClick(GameEntry entry) {
        if (entry == null || entry.getRomPath() == null) return;
        openGame(entry.getRomPath());
    }

    @Override
    public void onGameLongClick(GameEntry entry, int position) {
        if (entry != null) {
            Toast.makeText(this, "Hold long for features soon: " + entry.getTitle(), Toast.LENGTH_SHORT).show();
        }
    }

    private void openVmuTool() {
        if (biosRomPath == null) {
            Toast.makeText(this, "No BIOS found. VMU optional.", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            Toast.makeText(this, "Booting Dreamcast BIOS / VMU...", Toast.LENGTH_SHORT).show();

            Intent i = new Intent(this, NativeGLActivity.class);
            i.putExtra("boot_bios", true);
            i.putExtra("bios_path", biosRomPath);

            if (flashPath != null) {
                i.putExtra("flash_path", flashPath);
            }

            i.putExtra(NativeGLActivity.EXTRA_GAME_PATH, biosRomPath);
            i.setData(Uri.fromFile(new File(biosRomPath)));

            // IMPORTANT: no CLEAR_TOP / NEW_TASK flags here

            startActivity(i);
            finish();

        } catch (Throwable t) {
            Log.e(TAG, "openVmuTool failed", t);
            Toast.makeText(this, "Failed to boot BIOS / VMU", Toast.LENGTH_SHORT).show();
        }
    }
}
