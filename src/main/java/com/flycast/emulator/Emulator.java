package com.flycast.emulator;

import android.app.Activity;
import android.content.ClipData;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

public class Emulator extends Activity {

    // ---------------------------------------------------------------------
    // REQUIRED by other classes (InputDeviceManager / VibratorThread, etc.)
    // ---------------------------------------------------------------------
    private static Context sAppContext;
    private static Activity sCurrentActivity;

    /** 0..100 (default 80). Some forks read this directly. */
    public static int vibrationPower = 80;

    public static Context getAppContext() {
        return sAppContext;
    }

    public static Activity getCurrentActivity() {
        return sCurrentActivity;
    }

    public static void setCurrentActivity(Activity a) {
        sCurrentActivity = a;
        if (a != null) sAppContext = a.getApplicationContext();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Make sure static context is set early
        sAppContext = getApplicationContext();
        sCurrentActivity = this;

        final Intent src = getIntent();

        // Forward to NativeGLActivity
        Intent dst = new Intent(this, NativeGLActivity.class);

        if (src != null) {
            // 1) Forward ACTION + DATA (Uri)
            dst.setAction(src.getAction());
            Uri gameUri = src.getData();
            if (gameUri != null) dst.setData(gameUri);

            // 2) Forward EXTRAS (romPath, etc.)
            Bundle extras = src.getExtras();
            if (extras != null) dst.putExtras(extras);

            // 3) Forward URI permission flags (CRITICAL for SAF)
            int flags = src.getFlags();
            dst.addFlags(flags);

            // 4) Forward ClipData (some pickers put the Uri here)
            ClipData clip = src.getClipData();
            if (clip != null) {
                dst.setClipData(clip);
                dst.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            }

            // 5) If we got a Uri, ensure read permission
            if (src.getData() != null) {
                dst.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            }
        }

        startActivity(dst);
        finish();
    }

    @Override
    protected void onResume() {
        super.onResume();
        sCurrentActivity = this;
        if (sAppContext == null) sAppContext = getApplicationContext();
    }
}
