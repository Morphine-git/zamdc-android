package com.flycast.wrapper;

import android.content.ContentResolver;
import android.content.Context;
import android.net.Uri;

import androidx.annotation.Nullable;
import androidx.documentfile.provider.DocumentFile;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * SAF-safe BIOS/FLASH finder:
 * - scans a picked folder (tree uri)
 * - finds dc_boot.bin and dc_flash.bin anywhere under it (depth-limited)
 * - copies them into <homeDir>/data/
 * - registers real file paths into SystemFiles
 */
public class BiosReader {

    public static class Result {
        public String homeDir;     // the home dir we used
        public String bootPath;    // copied destination path
        public String flashPath;   // copied destination path
        public boolean foundBoot;
        public boolean foundFlash;
    }

    /**
     * Scan inside a SAF tree folder (Uri from ACTION_OPEN_DOCUMENT_TREE),
     * copy BIOS/FLASH into homeDir/data, and save into SystemFiles.
     */
    public static Result scanAndInstallFromTreeUri(Context ctx, Uri treeUri, String homeDir) {
        Result r = new Result();
        r.homeDir = homeDir;

        if (ctx == null || treeUri == null || homeDir == null || homeDir.trim().isEmpty()) return r;

        // Ensure data folder exists
        File dataDir = new File(homeDir, "data");
        //noinspection ResultOfMethodCallIgnored
        dataDir.mkdirs();

        DocumentFile root = DocumentFile.fromTreeUri(ctx, treeUri);
        if (root == null || !root.canRead()) return r;

        // Find the source docs (SAF) anywhere under the picked folder
        DocumentFile bootDoc  = findFirst(root, "dc_boot.bin",  8);
        DocumentFile flashDoc = findFirst(root, "dc_flash.bin", 8);

        // Copy into real filesystem paths Flycast can use
        if (bootDoc != null && bootDoc.isFile()) {
            File dstBoot = new File(dataDir, "dc_boot.bin");
            if (copyDocumentToFile(ctx, bootDoc, dstBoot)) {
                r.foundBoot = true;
                r.bootPath = dstBoot.getAbsolutePath();
                // Store REAL FILE path
                SystemFiles.addBiosPath(ctx, r.bootPath);
            }
        }

        if (flashDoc != null && flashDoc.isFile()) {
            File dstFlash = new File(dataDir, "dc_flash.bin");
            if (copyDocumentToFile(ctx, flashDoc, dstFlash)) {
                r.foundFlash = true;
                r.flashPath = dstFlash.getAbsolutePath();
                // Store REAL FILE path
                SystemFiles.setFlashPath(ctx, r.flashPath);
            }
        }

        // Optional: if we successfully installed BIOS/FLASH, make sure HomeDir is saved
        if (r.foundBoot || r.foundFlash) {
            SystemFiles.setHomeDir(ctx, homeDir);
        }

        return r;
    }

    // -------------------------
    // Internals
    // -------------------------

    @Nullable
    private static DocumentFile findFirst(DocumentFile dir, String targetName, int maxDepth) {
        if (dir == null || maxDepth < 0) return null;

        // If dir is a file, just check name
        if (dir.isFile()) {
            String n = safeName(dir);
            return targetName.equalsIgnoreCase(n) ? dir : null;
        }

        DocumentFile[] kids = dir.listFiles();
        if (kids == null) return null;

        for (DocumentFile f : kids) {
            String n = safeName(f);
            if (f.isFile() && targetName.equalsIgnoreCase(n)) return f;
        }

        if (maxDepth == 0) return null;

        for (DocumentFile f : kids) {
            if (f.isDirectory()) {
                DocumentFile hit = findFirst(f, targetName, maxDepth - 1);
                if (hit != null) return hit;
            }
        }
        return null;
    }

    private static String safeName(DocumentFile f) {
        try {
            String n = f.getName();
            return (n == null) ? "" : n;
        } catch (Throwable t) {
            return "";
        }
    }

    private static boolean copyDocumentToFile(Context ctx, DocumentFile srcDoc, File dstFile) {
        if (ctx == null || srcDoc == null || dstFile == null) return false;

        ContentResolver cr = ctx.getContentResolver();
        Uri srcUri = srcDoc.getUri();

        try (InputStream in = cr.openInputStream(srcUri);
             OutputStream out = new FileOutputStream(dstFile, false)) {

            if (in == null) return false;

            byte[] buf = new byte[64 * 1024];
            int read;
            while ((read = in.read(buf)) > 0) {
                out.write(buf, 0, read);
            }
            out.flush();
            return true;

        } catch (Throwable t) {
            return false;
        }
    }
}

