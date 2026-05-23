package com.flycast.wrapper;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.RandomAccessFile;
import java.util.Locale;

/**
 * DiscTitleReader
 *
 * Tries to extract a Dreamcast DiscId from:
 *  - GDI: first data track at (LBA + 16) * sectorSize
 *  - Others (CDI, bin): deep scan for "SEGA SEGAKATANA" / "SEGA SEGADREAMCAST"
 *  - Fallback: scan around that region for something that looks like an ID.
 *
 * NOTE: If your images do not store IP.BIN in a normal readable way,
 * this will still return null. That is a limitation of the images, not the code.
 */
public class DiscTitleReader {

    private static final int IP_BIN_SIZE = 0x800; // 2048 bytes
    private static final long MAX_SCAN_BYTES = 256L * 1024L * 1024L; // 256 MB

    // Magic markers
    private static final byte[] MAGIC_KATANA = "SEGA SEGAKATANA".getBytes();
    private static final byte[] MAGIC_DREAMCAST = "SEGA SEGADREAMCAST".getBytes();

    /** Public entry point: return DiscId or null. */
    public static String readDiscId(File file) {
        if (file == null || !file.isFile()) return null;

        String name = file.getName().toLowerCase(Locale.US);

        try {
            // 1) GDI with track table
            if (name.endsWith(".gdi")) {
                String id = fromGdi(file);
                if (id != null) return id;
            }

            // 2) Deep scan for SEGA header
            String id = deepScanForMagic(file);
            if (id != null) return id;

        } catch (Exception e) {
            // Ignore; return null if anything goes wrong
        }

        return null;
    }

    // ------------------------------------------------------------
    // GDI handling: find first data track and read IP.BIN at (LBA+16)*sector
    // ------------------------------------------------------------
    private static String fromGdi(File gdiFile) throws Exception {
        File dataTrack = null;
        int firstLba = Integer.MAX_VALUE;
        int sectorSize = 2048;

        File parent = gdiFile.getParentFile();

        BufferedReader br = new BufferedReader(
                new InputStreamReader(new FileInputStream(gdiFile), "US-ASCII"));
        try {
            String line = br.readLine(); // track count line
            if (line == null) return null;

            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (line.length() == 0) continue;

                // Format: track#  LBA  type  sectorSize  "filename"
                String[] parts = line.split("\\s+");
                if (parts.length < 5) continue;

                int lba;
                int sec;
                try {
                    lba = Integer.parseInt(parts[1]);
                    sec = Integer.parseInt(parts[3]);
                } catch (NumberFormatException nfe) {
                    continue;
                }

                String trackName = parts[4].replace("\"", "");
                File f = new File(parent, trackName);
                if (!f.isFile()) continue;

                if (lba < firstLba) {
                    firstLba = lba;
                    dataTrack = f;
                    if (sec > 0) sectorSize = sec;
                }
            }
        } finally {
            br.close();
        }

        if (dataTrack == null) return null;

        long ipOffset = ((long) firstLba + 16L) * (long) sectorSize;
        RandomAccessFile raf = new RandomAccessFile(dataTrack, "r");
        try {
            if (raf.length() < ipOffset + IP_BIN_SIZE) return null;

            raf.seek(ipOffset);
            byte[] ip = new byte[IP_BIN_SIZE];
            raf.readFully(ip);

            return parseDiscId(ip);
        } finally {
            raf.close();
        }
    }

    // ------------------------------------------------------------
    // Deep scan file for SEGA magic and read a block around it
    // ------------------------------------------------------------
    private static String deepScanForMagic(File file) throws Exception {
        long span = Math.min(file.length(), MAX_SCAN_BYTES);
        if (span <= 0) return null;

        FileInputStream in = new FileInputStream(file);
        try {
            final int BUF = 256 * 1024;
            byte[] buf = new byte[BUF];
            long remaining = span;
            long baseOffset = 0;

            while (remaining > 0) {
                int toRead = (int) Math.min(BUF, remaining);
                int read = in.read(buf, 0, toRead);
                if (read <= 0) break;

                int idx = indexOfMagic(buf, read);
                if (idx >= 0) {
                    long magicOffset = baseOffset + idx;
                    return readDiscIdNear(file, magicOffset);
                }

                remaining -= read;
                baseOffset += read;
            }
        } finally {
            in.close();
        }
        return null;
    }

    // Read a small block around magicOffset and parse as if it's near IP.BIN
    private static String readDiscIdNear(File file, long magicOffset) throws Exception {
        RandomAccessFile raf = new RandomAccessFile(file, "r");
        try {
            long len = raf.length();
            long start = magicOffset - 0x80;
            if (start < 0) start = 0;
            long end = start + IP_BIN_SIZE;
            if (end > len) end = len;

            int size = (int) (end - start);
            if (size <= 0) return null;

            byte[] buf = new byte[size];
            raf.seek(start);
            raf.readFully(buf);

            return parseDiscId(buf);
        } finally {
            raf.close();
        }
    }

    // ------------------------------------------------------------
    // Parse DiscId from a buffer that should contain IP.BIN area
    // ------------------------------------------------------------
    private static String parseDiscId(byte[] ip) {
        if (ip == null || ip.length < 0x80) return null;

        // Try a few common offsets inside IP.BIN
        int[] offsets = {0x50, 0x60, 0x70};

        for (int off : offsets) {
            if (off + 16 <= ip.length) {
                String s = ascii(ip, off, 16).trim();
                String cleaned = cleanId(s);
                if (looksLikeDiscId(cleaned)) {
                    return cleaned;
                }
            }
        }

        // Fallback: search whole block for something that looks like an ID.
        String whole = ascii(ip, 0, ip.length);
        String best = "";
        StringBuilder cur = new StringBuilder();

        for (int i = 0; i < whole.length(); i++) {
            char c = whole.charAt(i);
            if ((c >= 'A' && c <= 'Z')
                    || (c >= '0' && c <= '9')
                    || c == '-' || c == '_' || c == '.') {
                cur.append(c);
            } else {
                if (cur.length() >= 4 && cur.length() <= 24) {
                    String cand = cur.toString();
                    if (looksLikeDiscId(cand) && cand.length() > best.length()) {
                        best = cand;
                    }
                }
                cur.setLength(0);
            }
        }

        if (best.length() == 0) return null;
        return best;
    }

    // ------------------------------------------------------------
    // Small helpers
    // ------------------------------------------------------------
    private static int indexOfMagic(byte[] buf, int len) {
        int idx = indexOf(buf, MAGIC_KATANA, len);
        if (idx >= 0) return idx;
        return indexOf(buf, MAGIC_DREAMCAST, len);
    }

    private static int indexOf(byte[] haystack, byte[] needle, int len) {
        if (needle == null || needle.length == 0) return -1;
        if (len < needle.length) return -1;
        int last = len - needle.length;
        for (int i = 0; i <= last; i++) {
            int j = 0;
            while (j < needle.length && haystack[i + j] == needle[j]) {
                j++;
            }
            if (j == needle.length) return i;
        }
        return -1;
    }

    private static String ascii(byte[] buf, int off, int len) {
        try {
            if (off < 0 || len <= 0 || off >= buf.length) return "";
            int end = off + len;
            if (end > buf.length) end = buf.length;
            return new String(buf, off, end - off, "US-ASCII");
        } catch (Exception e) {
            return "";
        }
    }

    private static String cleanId(String raw) {
        if (raw == null) return "";
        raw = raw.trim().toUpperCase(Locale.US);
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < raw.length(); i++) {
            char c = raw.charAt(i);
            if ((c >= 'A' && c <= 'Z')
                    || (c >= '0' && c <= '9')
                    || c == '-' || c == '_' || c == '.') {
                sb.append(c);
            }
        }
        String out = sb.toString();
        if (out.length() > 24) out = out.substring(0, 24);
        return out;
    }

    private static boolean looksLikeDiscId(String s) {
        if (s == null || s.length() < 4) return false;
        boolean hasLetter = false;
        boolean hasDigit = false;
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (c >= 'A' && c <= 'Z') hasLetter = true;
            if (c >= '0' && c <= '9') hasDigit = true;
        }
        return hasLetter && hasDigit;
    }
}