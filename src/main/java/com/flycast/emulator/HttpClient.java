package com.flycast.emulator;

import android.util.Log;

public class HttpClient {

   private static final String TAG = "HttpClient";

   public HttpClient() {
      Log.i(TAG, "HttpClient constructor");
   }

   // JNI:
   // openUrl(String, byte[][], String[]) -> int
   public int openUrl(String url, byte[][] data, String[] headers) {
      Log.i(TAG, "openUrl called: " + url);

      if (headers != null) {
         for (String h : headers) {
            Log.i(TAG, "Header: " + h);
         }
      }

      return 200;
   }

   // JNI:
   // post(String, String[], String[], String[]) -> int
   public int post(String url,
                   String[] keys,
                   String[] values,
                   String[] headers) {

      Log.i(TAG, "post form called: " + url);

      return 200;
   }

   // JNI:
   // post(String, String, String, byte[][]) -> int
   public int post(String url,
                   String contentType,
                   String body,
                   byte[][] data) {

      Log.i(TAG, "post raw called: " + url);

      return 200;
   }
}

