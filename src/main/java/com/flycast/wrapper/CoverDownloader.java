package com.flycast.wrapper;

import android.content.Context;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * CoverDownloader — Fetch Sega Dreamcast box art from IGDB.
 *
 * Called from MainActivity using:
 *
 * String path = CoverDownloader.downloadCoverFor(
 *     MainActivity.this,
 *     searchTitle,
 *     romBaseName
 * );
 *
 * Returns local path to cover image, or null if not found.
 */
public class CoverDownloader {

    private static final String TAG = "CoverDownloader";

    // IGDB endpoints
    private static final String IGDB_GAMES_URL  = "https://api.igdb.com/v4/games";
    private static final String IGDB_COVERS_URL = "https://api.igdb.com/v4/covers";

    // Dreamcast platform = 23
    private static final int DREAMCAST_PLATFORM_ID = 23;

    // -------------------------------------------------------------------------
    // REQUIRED: fill these with your real values
    // -------------------------------------------------------------------------
    private static final String CLIENT_ID = "jlc63r91ecna2yd6p7bg0bpirodt3b";
    private static final String ACCESS_TOKEN = "8x5o6003xpxz7pic44qjymp7ngp5m5";


    // -------------------------------------------------------------------------
    // PUBLIC ENTRY POINT
    // -------------------------------------------------------------------------
    public static String downloadCoverFor(Context ctx,
                                          String displayTitle,
                                          String romBaseName) {
        if (displayTitle == null || ctx == null) return null;

        String normalized = normalizeTitle(displayTitle);
        if (normalized.isEmpty()) {
            normalized = romBaseName != null ? romBaseName : displayTitle.trim();
        }

        Log.d(TAG, "Normalized title: " + normalized);

        String[] candidates = buildCandidates(normalized);

        for (String candidate : candidates) {
            if (candidate == null || candidate.trim().isEmpty()) continue;

            try {
                Log.d(TAG, "Trying IGDB: " + candidate);

                Integer coverId = fetchCoverId(candidate);
                if (coverId == null) continue;

                Log.d(TAG, "Found coverId=" + coverId);

                String imageUrl = fetchImageUrl(coverId);
                if (imageUrl == null) continue;

                Log.d(TAG, "Cover URL: " + imageUrl);

                // File name (safe)
                String safeName = romBaseName != null ? romBaseName : normalized;
                safeName = safeName.replaceAll("[^a-zA-Z0-9._-]", "_");

                String localPath = downloadImage(ctx, imageUrl, safeName);
                if (localPath != null) {
                    Log.d(TAG, "✔ Saved cover: " + localPath);
                    return localPath;
                }

            } catch (Exception e) {
                Log.e(TAG, "Error using candidate: " + candidate, e);
            }
        }

        Log.w(TAG, "⚠ No cover found for: " + displayTitle);
        return null;
    }


    // -------------------------------------------------------------------------
    // TITLE NORMALIZER
    // -------------------------------------------------------------------------
    private static String normalizeTitle(String raw) {
        String t = raw;

        // Strip known extensions
        t = t.replaceAll("(?i)\\.(cdi|gdi|chd|iso)$", "");

        // Remove region tags
        t = t.replaceAll("(?i)\\(usa\\)|\\(us\\)|\\(europe\\)|\\(eur\\)|\\(japan\\)|\\(jpn\\)|\\(pal\\)", "");

        // Remove disc tags
        t = t.replaceAll("(?i)\\(disc ?\\d+\\)|disc ?\\d+", "");

        // Remove any other bracketed text
        t = t.replaceAll("\\(.*?\\)|\\[.*?\\]", "");

        // Replace punctuation
        t = t.replace('_', ' ');
        t = t.replace('-', ' ');
        t = t.replace(':', ' ');

        // Collapse spaces
        t = t.replaceAll("\\s+", " ").trim();

        return t;
    }

    private static String[] buildCandidates(String base) {
        return new String[]{
                base + " (USA)",
                base + " (Europe)",
                base + " (Japan)",
                base
        };
    }


    // -------------------------------------------------------------------------
    // IGDB — Step 1 — Get Cover ID
    // -------------------------------------------------------------------------
    private static Integer fetchCoverId(String title) throws Exception {

        String query = "search \"" + title.replace("\"", "\\\"") + "\"; " +
                "fields id,name,cover,platforms; " +
                "where platforms = " + DREAMCAST_PLATFORM_ID + " & cover != null; " +
                "limit 5;";

        String json = post(IGDB_GAMES_URL, query);
        if (json == null || json.isEmpty()) return null;

        JSONArray arr = new JSONArray(json);
        if (arr.length() == 0) return null;

        Log.d(TAG, "Games → " + arr.toString());

        for (int i = 0; i < arr.length(); i++) {
            JSONObject o = arr.getJSONObject(i);
            if (o.has("cover") && !o.isNull("cover")) {
                return o.getInt("cover");
            }
        }
        return null;
    }


    // -------------------------------------------------------------------------
    // IGDB — Step 2 — Get Cover Image URL
    // -------------------------------------------------------------------------
    private static String fetchImageUrl(int coverId) throws Exception {

        String query = "fields id,url,image_id; " +
                "where id = " + coverId + "; " +
                "limit 1;";

        String json = post(IGDB_COVERS_URL, query);
        if (json == null || json.isEmpty()) return null;

        JSONArray arr = new JSONArray(json);
        if (arr.length() == 0) return null;

        Log.d(TAG, "Cover Meta → " + arr.toString());

        JSONObject obj = arr.getJSONObject(0);

        if (obj.has("url") && !obj.isNull("url")) {
            String url = obj.getString("url");
            if (url.startsWith("//")) return "https:" + url;
            if (!url.startsWith("http")) return "https://" + url;
            return url;
        }

        if (obj.has("image_id") && !obj.isNull("image_id")) {
            String id = obj.getString("image_id");
            return "https://images.igdb.com/igdb/image/upload/t_cover_big/" + id + ".jpg";
        }

        return null;
    }


    // -------------------------------------------------------------------------
    // IGDB POST Request Helper
    // -------------------------------------------------------------------------
    private static String post(String endpoint, String body) throws Exception {
        HttpURLConnection conn = null;

        try {
            URL url = new URL(endpoint);
            conn = (HttpURLConnection) url.openConnection();
            conn.setConnectTimeout(15000);
            conn.setReadTimeout(15000);
            conn.setRequestMethod("POST");
            conn.setDoOutput(true);

            conn.setRequestProperty("Client-ID", CLIENT_ID);
            conn.setRequestProperty("Authorization", "Bearer " + ACCESS_TOKEN);
            conn.setRequestProperty("Accept", "application/json");
            conn.setRequestProperty("Content-Type", "text/plain");

            Log.d(TAG, "POST → " + endpoint + " : " + body);

            OutputStream out = conn.getOutputStream();
            out.write(body.getBytes("UTF-8"));
            out.flush();
            out.close();

            int code = conn.getResponseCode();
            InputStream in;
            if (code >= 200 && code < 300) {
                in = new BufferedInputStream(conn.getInputStream());
            } else {
                Log.w(TAG, "IGDB HTTP " + code);
                InputStream err = conn.getErrorStream();
                if (err != null) Log.w(TAG, "Error → " + readAll(err));
                return null;
            }

            String json = readAll(in);
            in.close();
            return json;

        } finally {
            if (conn != null) conn.disconnect();
        }
    }

    private static String readAll(InputStream in) throws Exception {
        BufferedReader br = new BufferedReader(new InputStreamReader(in));
        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = br.readLine()) != null)
            sb.append(line);
        br.close();
        return sb.toString();
    }


    // -------------------------------------------------------------------------
    // Save Image in Internal Storage → No Permission Needed!
    // -------------------------------------------------------------------------
    private static String downloadImage(Context ctx, String imageUrl, String safeTitle) throws Exception {

        File coversDir = new File(ctx.getFilesDir(), "covers");
        if (!coversDir.exists()) {
            boolean created = coversDir.mkdirs();
            Log.d(TAG, "Create covers dir=" + created + " path=" + coversDir.getAbsolutePath());
        }

        File outFile = new File(coversDir, safeTitle + ".jpg");
        Log.d(TAG, "Saving → " + outFile.getAbsolutePath());

        HttpURLConnection conn = null;
        try {
            URL url = new URL(imageUrl);
            conn = (HttpURLConnection) url.openConnection();
            conn.setConnectTimeout(15000);
            conn.setReadTimeout(20000);
            conn.setRequestMethod("GET");

            if (conn.getResponseCode() != 200) return null;

            InputStream in = new BufferedInputStream(conn.getInputStream());
            OutputStream out = new BufferedOutputStream(new FileOutputStream(outFile));

            byte[] buffer = new byte[4096];
            int n;
            while ((n = in.read(buffer)) != -1)
                out.write(buffer, 0, n);

            out.flush();
            out.close();
            in.close();

            return outFile.getAbsolutePath();

        } finally {
            if (conn != null) conn.disconnect();
        }
    }
}