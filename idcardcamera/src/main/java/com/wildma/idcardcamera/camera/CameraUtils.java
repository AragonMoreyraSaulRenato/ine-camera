package com.wildma.idcardcamera.camera;

import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.Camera;

public class CameraUtils {

    private static Camera camera;

    /**
     *
     * Comprueba si hay cámara
     *
     * @param context
     * @return
     */
    public static boolean hasCamera(Context context) {
        if (context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA)) {
            // Este dispositivo tiene una camara
            return true;
        } else {
            // No hay una camara
            return false;
        }
    }

    /**
     * Encienda la camara
     *
     * @return
     */
    public static Camera openCamera() {
        camera = null;
        try {
            camera = Camera.open(); // Obtiene la instancia de la camara
        } catch (Exception e) {
            // Camara no disponible
        }
        return camera; // retorna null si la camara no esta disponible
    }

    public static Camera getCamera() {
        return camera;
    }

    /**
     * Compruebe si hay flash
     *
     * @return true：hay，false：no
     */
    public static boolean hasFlash(Context context) {
        return context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH);
    }
}
