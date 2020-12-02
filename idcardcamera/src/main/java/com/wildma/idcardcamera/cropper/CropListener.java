package com.wildma.idcardcamera.cropper;

import android.graphics.Bitmap;
 
 /** Interfaz para definir el CropListener
  */
public interface CropListener {

    void onFinish(Bitmap bitmap);

}
