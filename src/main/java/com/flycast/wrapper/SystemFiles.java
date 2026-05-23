package com.flycast.wrapper;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class SystemFiles {

    private static final String PREFS = "flycast_settings";
    private static final String KEY_HOME_DIR = "home_dir";
    private static final String KEY_FLASH_PATH = "flash_path";
    private static final String KEY_BIOS_PATHS = "bios_paths";

    private static final List<String> biosPaths = new ArrayList<>();
    private static String flashPath = null;

    // -----------------------------
    // HOME DIR
    // -----------------------------
    public static void setHomeDir(Context ctx, String path) {
        if (ctx == null) return;
        if (path == null) return;
        path = path.trim();
        if (path.isEmpty()) return;

        SharedPreferences p = ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
        p.edit().putString(KEY_HOME_DIR, path).apply();
    }

    public static String getHomeDir(Context ctx, String fallback) {
        try {
            if (ctx == null) return fallback;
            SharedPreferences p = ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
            String saved = p.getString(KEY_HOME_DIR, null);
            if (saved != null && !saved.trim().isEmpty()) return saved;
        } catch (Throwable ignored) {}
        return fallback;
    }

    // -----------------------------
    // BIOS PATHS (store FILE paths, e.g. ".../data/dc_boot.bin")
    // -----------------------------
    public static synchronized void addBiosPath(Context ctx, String path) {
        if (path == null) return;
        path = path.trim();
        if (path.isEmpty()) return;

        // avoid duplicates in memory
        if (!biosPaths.contains(path)) biosPaths.add(path);

        // persist
        if (ctx != null) {
            SharedPreferences p = ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
            Set<String> set = new HashSet<>(p.getStringSet(KEY_BIOS_PATHS, new HashSet<>()));
            set.add(path);
            p.edit().putStringSet(KEY_BIOS_PATHS, set).apply();
        }
    }

    public static synchronized List<String> getBiosPaths(Context ctx) {
        // merge persisted -> memory
        if (ctx != null) {
            try {
                SharedPreferences p = ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
                Set<String> set = p.getStringSet(KEY_BIOS_PATHS, null);
                if (set != null) {
                    for (String s : set) {
                        if (s != null && !s.trim().isEmpty() && !biosPaths.contains(s)) {
                            biosPaths.add(s);
                        }
                    }
                }
            } catch (Throwable ignored) {}
        }
        return new ArrayList<>(biosPaths);
    }

    // -----------------------------
    // FLASH PATH (store FILE path, e.g. ".../data/dc_flash.bin")
    // -----------------------------
    public static synchronized void setFlashPath(Context ctx, String path) {
        flashPath = path;

        if (ctx != null) {
            SharedPreferences p = ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
            p.edit().putString(KEY_FLASH_PATH, path).apply();
        }
    }

    public static synchronized String getFlashPath(Context ctx) {
        if (flashPath != null && !flashPath.trim().isEmpty()) return flashPath;

        if (ctx != null) {
            try {
                SharedPreferences p = ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
                String saved = p.getString(KEY_FLASH_PATH, null);
                if (saved != null && !saved.trim().isEmpty()) {
                    flashPath = saved;
                    return saved;
                }
            } catch (Throwable ignored) {}
        }
        return null;
    }

    // ---------------------------------------------------------------------
    // URI -> filesystem path helper (SAFE ONLY for file:// URIs)
    // We DO NOT try to convert content:// SAF URIs into real file paths.
    // ---------------------------------------------------------------------
    public static String getPathFromUri(Context ctx, Uri uri) {
        if (uri == null) return null;

        try {
            String scheme = uri.getScheme();

            // ✅ SAFE: file://...
            if ("file".equalsIgnoreCase(scheme) || scheme == null) {
                return uri.getPath();
            }

            // Some providers give raw paths (rare)
            String raw = uri.toString();
            if (raw.startsWith("/")) return raw;

            // ❌ content:// is not safely convertible to file path
            return null;
        } catch (Throwable ignored) {
            return null;
        }
    }
}

