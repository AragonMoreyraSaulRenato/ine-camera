package com.wildma.idcardcamera.utils;

import android.content.Context;

public class ScreenUtils {

     /**Clase para obtener el alto y ancho de la pantalla en Pixeles*/
    public static int getScreenWidth(Context context) {
        return context.getResources().getDisplayMetrics().widthPixels;
    }


    public static int getScreenHeight(Context context) {
        return context.getResources().getDisplayMetrics().heightPixels;
    }
}
