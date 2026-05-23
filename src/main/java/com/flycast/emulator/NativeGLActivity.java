package com.flycast.emulator;

import android.app.Activity;
import android.nfc.Tag;
import android.os.Bundle;
import android.util.Log;
import android.view.WindowManager;
import android.widget.FrameLayout;
import com.flycast.emulator.VirtualControllerView;

import com.reicast.emulator.emu.JNIdc;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.Locale;
import android.content.Context;

public class NativeGLActivity extends Activity {

    private static final String TAG = "NativeGLActivity";

    public static final String EXTRA_GAME_PATH = "gamePath";

    private NativeGLView glView;
    private String gamePath;
    private boolean gameUriSent = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Log.i(TAG, "onCreate()");

        gamePath = getIntent().getStringExtra(EXTRA_GAME_PATH);
        Log.i(TAG, "gamePath=" + gamePath);

        getWindow().addFlags(
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
                        | WindowManager.LayoutParams.FLAG_FULLSCREEN
                        | WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN
                        | WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
        );


        String filesDir = getFilesDir().getAbsolutePath();
        File ext = getExternalFilesDir(null);
        String homeDir = (ext != null) ? ext.getAbsolutePath() : filesDir;

        copyAssetIfMissing("buttons.png", new File(homeDir, "data/buttons.png"));
        copyAssetIfMissing("buttons.png", new File(filesDir, "data/buttons.png"));
        copyAssetIfMissing("buttons.png", new File(homeDir, "buttons.png"));

        try {
            JNIdc.initEnvironment(
                    this,
                    filesDir,
                    homeDir,
                    Locale.getDefault().toString()
            );
            Log.i(TAG, "initEnvironment OK");
        } catch (Throwable t) {
            Log.e(TAG, "initEnvironment failed", t);
        }

        FrameLayout root = new FrameLayout(this);

        glView = new NativeGLView(this);
        root.addView(glView, new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT
        ));

        VirtualControllerView controller = new VirtualControllerView(this);
        root.addView(controller, new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT
        ));

        setContentView(root);
    }


    @Override
    protected void onResume() {
        super.onResume();

        Log.i(TAG, "onResume()");

        if (glView != null) {
            glView.onHostResume();
        }

        if (!gameUriSent && gamePath != null && !gamePath.isEmpty()) {
            try {
                JNIdc.setGameUri(gamePath);
                gameUriSent = true;
                Log.i(TAG, "setGameUri sent: " + gamePath);
            } catch (Throwable t) {
                Log.e(TAG, "setGameUri failed", t);
            }
        }
    }

    @Override
    protected void onPause() {
        Log.i(TAG, "onPause()");

        if (glView != null) {
            glView.onHostPause();
        }

        super.onPause();
    }


    private void copyAssetIfMissing(String assetName, File outFile) {
        try {
            if (outFile.exists() && outFile.length() > 0) {
                Log.i(TAG, "Asset already exists: " + outFile.getAbsolutePath());
                return;
            }

            File parent = outFile.getParentFile();
            if (parent != null && !parent.exists()) {
                parent.mkdirs();
            }

            try (InputStream in = getAssets().open(assetName);
                 FileOutputStream out = new FileOutputStream(outFile)) {

                byte[] buffer = new byte[8192];
                int read;

                while ((read = in.read(buffer)) != -1) {
                    out.write(buffer, 0, read);
                }

                out.flush();
            }

            Log.i(TAG, "Copied asset: " + assetName + " -> " + outFile.getAbsolutePath());

        } catch (Throwable t) {
            Log.e(TAG, "Failed to copy asset: " + assetName + " -> " + outFile.getAbsolutePath(), t);
        }
    }

    public void SaveAndroidSettings(String settings) {
        Log.i(TAG, "SaveAndroidSettings: " + settings);
    }

    public String LoadAndroidSettings() {
        Log.i(TAG, "LoadAndroidSettings");
        return "";
    }

    public int getScreenDpi() {
        return getResources().getDisplayMetrics().densityDpi;
    }

    public int getScreenWidth() {
        return getResources().getDisplayMetrics().widthPixels;
    }

    public int getScreenHeight() {
        return getResources().getDisplayMetrics().heightPixels;
    }

    public int getScreenRefreshRate() {
        try {
            return Math.round(getWindowManager().getDefaultDisplay().getRefreshRate());
        } catch (Throwable t) {
            Log.e(TAG, "getScreenRefreshRate failed", t);
            return 60;
        }
    }
}
