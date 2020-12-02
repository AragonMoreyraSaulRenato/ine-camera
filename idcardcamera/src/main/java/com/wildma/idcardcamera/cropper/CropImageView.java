package com.wildma.idcardcamera.cropper;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.wildma.idcardcamera.R;

/** Clase encargada de contener la fotografia capturada para realizar su recorte
    Extiende de FrameLayout para apilar las vistas secundarias una encima de la otra.
 */
public class CropImageView extends FrameLayout {

    private ImageView       mImageView;
    private CropOverlayView mCropOverlayView;

/** Contructores inflando el contexto y obteniendo del layout la vista 
    junto con las componentes imagenview y cropoverlayview
    Utilice getContext ()
 */
    public CropImageView(@NonNull Context context) {
        super(context);
    }

    public CropImageView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        LayoutInflater inflater = LayoutInflater.from(context);
        View v = inflater.inflate(R.layout.crop_image_view, this, true);
        mImageView = (ImageView) v.findViewById(R.id.img_crop);
        mCropOverlayView = (CropOverlayView) v.findViewById(R.id.overlay_crop);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
    }

/** Se establece el bitmap de la fotografia a ambos elementos */
    public void setImageBitmap(Bitmap bitmap) {
        mImageView.setImageBitmap(bitmap);
        mCropOverlayView.setBitmap(bitmap);
    }

/** Asignacion de un CropListener para escuchar la actividad de la pantalla */
    public void crop(CropListener listener, boolean needStretch) {
        if (listener == null)
            return;
        mCropOverlayView.crop(listener, needStretch);
    }

}
