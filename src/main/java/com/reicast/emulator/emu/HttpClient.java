package com.reicast.emulator.emu;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.List;
import java.util.Map;

public class HttpClient {

    // Native will call this to cache method IDs (and often store a global ref to this instance)
    private native void nativeInit();

    public HttpClient() {
        // lib is already loaded by JNIdc, but this is safe if you ever use HttpClient standalone
        try { System.loadLibrary("flycast"); } catch (Throwable ignored) {}
        try { nativeInit(); } catch (Throwable ignored) {}
    }

    // ---------------------------------------------------------------------
    // REQUIRED BY libflycast.so  (DO NOT CHANGE NAME OR PARAM TYPES)
    // Signature: (Ljava/lang/String;[[B[Ljava/lang/String;)I
    // Returns: int (typically HTTP status code; 0 on failure)
    //
    // url        = URL to open (GET)
    // outBody    = container to receive response bytes: outBody[0] = byte[]
    // outHeaders = optional container to receive "Key: Value" header lines
    // ---------------------------------------------------------------------
    public int openUrl(String url, byte[][] outBody, String[] outHeaders) {
        HttpURLConnection conn = null;
        try {
            conn = (HttpURLConnection) new URL(url).openConnection();
            conn.setInstanceFollowRedirects(true);
            conn.setRequestMethod("GET");
            conn.setConnectTimeout(15000);
            conn.setReadTimeout(25000);
            conn.setRequestProperty("User-Agent", "Flycast-Android");
            conn.setRequestProperty("Accept", "*/*");

            int code = conn.getResponseCode();

            // Read body
            InputStream in = (code >= 200 && code < 400) ? conn.getInputStream() : conn.getErrorStream();
            byte[] body = readAllBytes(in);

            if (outBody != null && outBody.length > 0) {
                outBody[0] = body;
            }

            // Fill headers if caller provided a string array
            if (outHeaders != null && outHeaders.length > 0) {
                int idx = 0;
                for (Map.Entry<String, List<String>> e : conn.getHeaderFields().entrySet()) {
                    if (idx >= outHeaders.length) break;
                    String k = e.getKey();
                    List<String> v = e.getValue();
                    if (k == null) k = "Status";
                    String val = (v == null) ? "" : join(v, ", ");
                    outHeaders[idx++] = k + ": " + val;
                }
                // Null out the rest (helps native avoid stale values)
                for (; idx < outHeaders.length; idx++) outHeaders[idx] = null;
            }

            return code;
        } catch (Throwable t) {
            if (outBody != null && outBody.length > 0) outBody[0] = new byte[0];
            if (outHeaders != null) {
                for (int i = 0; i < outHeaders.length; i++) outHeaders[i] = null;
            }
            return 0;
        } finally {
            if (conn != null) conn.disconnect();
        }
    }

    // ---------------------------------------------------------------------
    // REQUIRED BY libflycast.so  (DO NOT CHANGE NAME OR PARAM TYPES)
    // Signature: (Ljava/lang/String;[Ljava/lang/String;[Ljava/lang/String;[Ljava/lang/String;)I
    // Returns: int (typically HTTP status code; 0 on failure)
    //
    // url        = URL to POST to
    // keys       = form field names (can be null)
    // values     = form field values (can be null)
    // headers    = optional "Key: Value" header lines (can be null)
    // ---------------------------------------------------------------------
    public int post(String url, String[] keys, String[] values, String[] headers) {
        HttpURLConnection conn = null;
        try {
            conn = (HttpURLConnection) new URL(url).openConnection();
            conn.setInstanceFollowRedirects(true);
            conn.setRequestMethod("POST");
            conn.setDoOutput(true);
            conn.setConnectTimeout(15000);
            conn.setReadTimeout(25000);
            conn.setRequestProperty("User-Agent", "Flycast-Android");
            conn.setRequestProperty("Accept", "*/*");

            // Apply custom headers if provided
            if (headers != null) {
                for (String h : headers) {
                    if (h == null) continue;
                    int p = h.indexOf(':');
                    if (p > 0) {
                        String k = h.substring(0, p).trim();
                        String v = h.substring(p + 1).trim();
                        if (!k.isEmpty()) conn.setRequestProperty(k, v);
                    }
                }
            }

            // Build x-www-form-urlencoded body
            byte[] payload = buildFormPayload(keys, values);
            conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8");
            conn.setRequestProperty("Content-Length", String.valueOf(payload.length));

            OutputStream os = conn.getOutputStream();
            os.write(payload);
            os.flush();
            os.close();

            int code = conn.getResponseCode();

            // Consume response stream so connection finishes cleanly
            InputStream in = (code >= 200 && code < 400) ? conn.getInputStream() : conn.getErrorStream();
            readAllBytes(in);

            return code;
        } catch (Throwable t) {
            return 0;
        } finally {
            if (conn != null) conn.disconnect();
        }
    }

    // -------------------------- helpers --------------------------

    private static byte[] buildFormPayload(String[] keys, String[] values) throws Exception {
        if (keys == null || values == null) return new byte[0];
        int n = Math.min(keys.length, values.length);

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < n; i++) {
            String k = keys[i];
            String v = values[i];
            if (k == null) k = "";
            if (v == null) v = "";
            if (i > 0) sb.append('&');
            sb.append(URLEncoder.encode(k, "UTF-8"));
            sb.append('=');
            sb.append(URLEncoder.encode(v, "UTF-8"));
        }
        return sb.toString().getBytes("UTF-8");
    }

    private static byte[] readAllBytes(InputStream in) throws Exception {
        if (in == null) return new byte[0];
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        byte[] buf = new byte[16 * 1024];
        int r;
        while ((r = in.read(buf)) != -1) bos.write(buf, 0, r);
        try { in.close(); } catch (Throwable ignored) {}
        return bos.toByteArray();
    }

    private static String join(List<String> items, String sep) {
        if (items == null || items.isEmpty()) return "";
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < items.size(); i++) {
            if (i > 0) sb.append(sep);
            sb.append(items.get(i));
        }
        return sb.toString();
    }
}


