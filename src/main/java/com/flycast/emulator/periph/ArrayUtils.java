package com.flycast.emulator.periph;

public final class ArrayUtils {
    private ArrayUtils() {}

    public static int[] toPrimitive(Integer[] arr) {
        if (arr == null) return new int[0];
        int[] out = new int[arr.length];
        for (int i = 0; i < arr.length; i++) {
            out[i] = (arr[i] != null) ? arr[i] : 0;
        }
        return out;
    }

    public static float[] toPrimitive(Float[] arr) {
        if (arr == null) return new float[0];
        float[] out = new float[arr.length];
        for (int i = 0; i < arr.length; i++) {
            out[i] = (arr[i] != null) ? arr[i] : 0f;
        }
        return out;
    }
}