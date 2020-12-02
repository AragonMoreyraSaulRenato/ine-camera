package com.wildma.idcardcamera.camera;

import android.app.Activity;
import android.content.Intent;
import android.support.v4.app.Fragment;

import java.lang.ref.WeakReference;


public class IDCardCamera {

    public final static int    TYPE_IDCARD_FRONT     = 1;//Frente de la tarjeta de identificación
    public final static int    TYPE_IDCARD_BACK      = 2;//Reverso de la tarjeta de identificación
    public final static int    RESULT_CODE           = 0X11;//Codigo de resultado
    public final static int    PERMISSION_CODE_FIRST = 0x12;//Código de solicitud de permiso
    public final static String TAKE_TYPE             = "take_type";//Marca de tipo de disparo
    public final static String IMAGE_PATH            = "image_path";//Marca de ruta de imagen

    private final WeakReference<Activity> mActivity;
    private final WeakReference<Fragment> mFragment;

    public static IDCardCamera create(Activity activity) {
        return new IDCardCamera(activity);
    }

    public static IDCardCamera create(Fragment fragment) {
        return new IDCardCamera(fragment);
    }

    private IDCardCamera(Activity activity) {
        this(activity, (Fragment) null);
    }

    private IDCardCamera(Fragment fragment) {
        this(fragment.getActivity(), fragment);
    }

    private IDCardCamera(Activity activity, Fragment fragment) {
        this.mActivity = new WeakReference(activity);
        this.mFragment = new WeakReference(fragment);
    }

    /**
     * Encienda la camara
     *
     * @param IDCardDirection Dirección de la tarjeta de identificación（TYPE_IDCARD_FRONT / TYPE_IDCARD_BACK）
     */
    public void openCamera(int IDCardDirection) {
        Activity activity = this.mActivity.get();
        Fragment fragment = this.mFragment.get();
        Intent intent = new Intent(activity, CameraActivity.class);
        intent.putExtra(TAKE_TYPE, IDCardDirection);
        if (fragment != null) {
            fragment.startActivityForResult(intent, IDCardDirection);
        } else {
            activity.startActivityForResult(intent, IDCardDirection);
        }
    }

    /**
     * Obtener la ruta de la imagen
     *
     * @param data Intent
     * @return Ruta de la imagen
     */
    public static String getImagePath(Intent data) {
        if (data != null) {
            return data.getStringExtra(IMAGE_PATH);
        }
        return "";
    }
}

