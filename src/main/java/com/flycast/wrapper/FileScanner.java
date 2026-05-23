package com.flycast.wrapper;

import android.content.Context;
import android.net.Uri;
import android.util.Log;

import androidx.documentfile.provider.DocumentFile;

import java.util.ArrayList;
import java.util.List;

public class FileScanner {
   private static final String TAG = "FileScanner";

   public interface ProgressCallback {
      void onProgress(int percent);
   }

   public static class ScanResult {
      public final List<GameEntry> games = new ArrayList<>();
      public String detectedBiosFolder = null; // folder where BIOS found
   }

   // ------------------------------------------------------------
   // MAIN ENTRY: Scan Dreamcast games using SAF folder URI
   // ------------------------------------------------------------
   public static ScanResult scanForDreamcastGames(Context ctx, Uri pickedFolder, ProgressCallback callback) {
      ScanResult out = new ScanResult();

      DocumentFile root = DocumentFile.fromTreeUri(ctx, pickedFolder);
      if (root == null || !root.exists() || !root.isDirectory()) {
         Log.e(TAG, "Invalid folder URI: " + pickedFolder);
         return out;
      }

      // SAF scanning: recursive
      long[] counter = new long[]{0};
      scanDocDir(ctx, root, out, callback, counter);

      // If BIOS folder was detected, auto-save it as HOME DIR
      if (out.detectedBiosFolder != null) {
         SystemFiles.setHomeDir(ctx, out.detectedBiosFolder);
      }

      if (callback != null) callback.onProgress(100);
      return out;
   }

   // ------------------------------------------------------------
   // Recursive scan using DocumentFile
   // ------------------------------------------------------------
   private static void scanDocDir(Context ctx, DocumentFile dir, ScanResult out,
                                  ProgressCallback cb, long[] counter) {

      DocumentFile[] children = dir.listFiles();
      if (children == null) return;

      for (DocumentFile child : children) {
         counter[0]++;

         if (cb != null && counter[0] % 25 == 0) {
            int percent = (int) (counter[0] % 100);
            cb.onProgress(percent);
         }

         if (child.isDirectory()) {
            // ✅ Detect GDI folder
            if (containsGdiFile(child)) {
               String uri = child.getUri().toString();
               GameEntry t = new GameEntry("GDI Folder", uri, null);
               t.setType(GameEntry.Type.GAME);
               out.games.add(t);
               continue;
            }

            // Recurse
            scanDocDir(ctx, child, out, cb, counter);
            continue;
         }

         if (!child.isFile()) continue;

         String name = child.getName();
         if (name == null) continue;

         String lower = name.toLowerCase();

         // ✅ Dreamcast games
         if (lower.endsWith(".cdi") || lower.endsWith(".gdi") || lower.endsWith(".chd")) {
            String uri = child.getUri().toString();
            GameEntry t = new GameEntry(name, uri, null);
            t.setType(GameEntry.Type.GAME);
            out.games.add(t);
            continue;
         }

         // ✅ BIOS detection
         if (lower.equals("dc_boot.bin") || lower.equals("dc_flash.bin") || lower.equals("dc_nvmem.bin")) {
            DocumentFile parent = child.getParentFile();
            if (parent != null) {
               out.detectedBiosFolder = parent.getUri().toString(); // SAF folder URI
               try {
                  SystemFiles.addBiosPath(ctx, parent.getUri().toString());
               } catch (Throwable ignored) {}
            }
         }
      }
   }

   // ------------------------------------------------------------
   // Helper: check if a folder contains .gdi
   // ------------------------------------------------------------
   private static boolean containsGdiFile(DocumentFile folder) {
      try {
         DocumentFile[] kids = folder.listFiles();
         if (kids == null) return false;

         for (DocumentFile f : kids) {
            if (!f.isFile()) continue;
            String n = f.getName();
            if (n != null && n.toLowerCase().endsWith(".gdi")) return true;
         }
      } catch (Throwable ignored) {}
      return false;
   }
}

