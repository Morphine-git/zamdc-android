package com.flycast.emulator;

import android.app.Activity;
import android.util.Log;

public class HomeMover {
    private final Activity activity;
    private final AndroidStorage storage;

    // ✅ their AndroidStorage expects this function
    private boolean reloadConfigOnCompletion = true;

    public HomeMover(Activity activity, AndroidStorage storage) {
        this.activity = activity;
        this.storage = storage;
    }

    // ✅ REQUIRED by AndroidStorage
    public void setReloadConfigOnCompletion(boolean enabled) {
        reloadConfigOnCompletion = enabled;
    }

    /**
     * Minimal implementation (compile + safe)
     * Your fork can implement real move/copy later.
     */
    public void copyHome(String fromUri, String toUri) {
        Log.i("flycast", "HomeMover.copyHome (stub) from=" + fromUri + " to=" + toUri);

        // optional config reload after completion
        if (reloadConfigOnCompletion) {
            try {
                storage.reloadConfig();
            } catch (Throwable ignored) {}
        }
    }

    // overload (some versions call with title)
    public void copyHome(String fromUri, String toUri, String title) {
        copyHome(fromUri, toUri);
    }
}

