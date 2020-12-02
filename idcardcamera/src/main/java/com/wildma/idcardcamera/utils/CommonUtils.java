package com.wildma.idcardcamera.utils;

public class CommonUtils {

    private static long lastClickTime;

    /**
     * Metodo que calcula si el click fue rapido o no
     */
    public static boolean isFastClick() {
        return isFastClick(1000);
    }

    public static boolean isFastClick(long intervalTime) {
        long time = System.currentTimeMillis();
        if (time - lastClickTime < intervalTime) {
            return true;
        }
        lastClickTime = time;
        return false;
    }
}
