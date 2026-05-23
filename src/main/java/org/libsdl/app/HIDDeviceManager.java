package org.libsdl.app;

import android.content.Context;
import android.util.Log;

public class HIDDeviceManager {

   private static final String TAG = "HIDDeviceManager";

   private static HIDDeviceManager instance;

   public static HIDDeviceManager acquire(Context context) {
      Log.i(TAG, "acquire()");
      if (instance == null) {
         instance = new HIDDeviceManager();
      }
      return instance;
   }

   public void release() {
      Log.i(TAG, "release()");
   }

   public void initialize(boolean usb, boolean bluetooth) {
      Log.i(TAG, "initialize usb=" + usb + " bt=" + bluetooth);
   }

   public int sendOutputReport(int deviceId, byte[] report) {
      Log.i(TAG, "sendOutputReport stub deviceId=" + deviceId);
      return -1;
   }

   public int sendFeatureReport(int deviceId, byte[] report) {
      Log.i(TAG, "sendFeatureReport stub deviceId=" + deviceId);
      return -1;
   }
}
