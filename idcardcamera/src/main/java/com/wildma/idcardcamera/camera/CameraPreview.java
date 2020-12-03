package com.wildma.idcardcamera.camera;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.Configuration;
import android.hardware.Camera;
import android.os.Build;
import android.util.AttributeSet;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import com.wildma.idcardcamera.sensor.SensorFocusControler;
import com.wildma.idcardcamera.sensor.SensorLevelControler;
import com.wildma.idcardcamera.utils.ScreenUtils;

import java.util.List;

public class CameraPreview extends SurfaceView implements SurfaceHolder.Callback {

    private static String TAG = CameraPreview.class.getName();

    private Camera           camera;
    private AutoFocusManager mAutoFocusManager;
    private SensorFocusControler mSensorFocusControler;
    private Context          mContext;
    private SurfaceHolder    mSurfaceHolder;


    private SensorLevelControler bubbleLevel;
    private Boolean enableFocus;

    public CameraPreview(Context context) {
        super(context);
        init(context);
    }

    public CameraPreview(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public CameraPreview(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public CameraPreview(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(context);
    }

    private void init(Context context) {
        mContext = context;
        bubbleLevel = new SensorLevelControler( context.getApplicationContext());
        mSurfaceHolder = getHolder();
        mSurfaceHolder.addCallback(this);
        mSurfaceHolder.setKeepScreenOn(true);
        mSurfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        mSensorFocusControler = SensorFocusControler.getInstance(context.getApplicationContext());

        enableFocus = false;
    }

    public void surfaceCreated(SurfaceHolder holder) {
        camera = CameraUtils.openCamera();
        if (camera != null) {
            try {
                camera.setPreviewDisplay(holder);

                Camera.Parameters parameters = camera.getParameters();
                if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
                    /* 
                        Al tomar fotografías en modo retrato, debe establecer una 
                        rotación de 90 grados; de lo contrario, la dirección de vista 
                        previa de la cámara y la dirección de la interfaz son diferentes 
                    */
                    camera.setDisplayOrientation(90);
                    parameters.setRotation(90);
                } else {
                    camera.setDisplayOrientation(0);
                    parameters.setRotation(0);
                }
                //Obtiene todos los tamaños de vista previa admitidos
                List<Camera.Size> sizeList = parameters.getSupportedPreviewSizes();
                Camera.Size bestSize = getOptimalPreviewSize(sizeList, ScreenUtils.getScreenWidth(mContext), ScreenUtils.getScreenHeight(mContext));
                //Establece el tamaño de vista previa
                parameters.setPreviewSize(bestSize.width, bestSize.height);
                camera.setParameters(parameters);
                camera.startPreview();
                //Enfoca por primera vez
                focus();
            } catch (Exception e) {
                Log.d(TAG, "Error setting camera preview: " + e.getMessage());
                try {
                    Camera.Parameters parameters = camera.getParameters();
                    if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
                        /**
                            Al tomar fotografías en modo retrato, debe establecer una rotación de 90 grados; 
                            de lo contrario, la dirección de vista previa de la cámara y la dirección de 
                            la interfaz son diferentes
                        */
                        camera.setDisplayOrientation(90);
                        parameters.setRotation(90);
                    } else {
                        camera.setDisplayOrientation(0);
                        parameters.setRotation(0);
                    }
                    camera.setParameters(parameters);
                    camera.startPreview();
                    //Enfócate por primera vez
                    focus();
                } catch (Exception e1) {
                    e.printStackTrace();
                    camera = null;
                }
            }
        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i1, int i2) {
        //Debido a que se establece una orientación de pantalla fija, este método no se activará en el uso real
    }

    /**
     * Obtenga el mejor tamaño de vista previa
     *
     * @param sizes Todos los tamaños de vista previa admitidos
     * @param w     SurfaceViewWidth
     * @param h     SurfaceViewHeight
     * @return
     */
    private Camera.Size getOptimalPreviewSize(List<Camera.Size> sizes, int w, int h) {
        final double ASPECT_TOLERANCE = 0.1;
        double targetRatio = (double) w / h;
        if (sizes == null)
            return null;

        Camera.Size optimalSize = null;
        double minDiff = Double.MAX_VALUE;

        int targetHeight = h;

        // Intente encontrar una relación de aspecto y un tamaño que coincida con el tamaño
        for (Camera.Size size : sizes) {
            double ratio = (double) size.width / size.height;
            if (Math.abs(ratio - targetRatio) > ASPECT_TOLERANCE)
                continue;
            if (Math.abs(size.height - targetHeight) < minDiff) {
                optimalSize = size;
                minDiff = Math.abs(size.height - targetHeight);
            }
        }

        //No se puede encontrar el que coincida con la relación de aspecto, ignore el requisito
        if (optimalSize == null) {
            minDiff = Double.MAX_VALUE;
            for (Camera.Size size : sizes) {
                if (Math.abs(size.height - targetHeight) < minDiff) {
                    optimalSize = size;
                    minDiff = Math.abs(size.height - targetHeight);
                }
            }
        }
        return optimalSize;
    }



    public void surfaceDestroyed(SurfaceHolder holder) {
        holder.removeCallback(this);
        //Reciclar y liberar recursos
        release();
    }

    /**
     * Liberar recursos
     */
    private void release() {
        if (camera != null) {
            camera.setPreviewCallback(null);
            camera.stopPreview();
            camera.release();
            camera = null;

            if (mAutoFocusManager != null) {
                mAutoFocusManager.stop();
                mAutoFocusManager = null;
            }
        }
    }

    /**
     * Enfoque, enfoque táctil o enfoque automático en CameraActivity
     */
    public void focus() {
        if (camera != null) {
            try {
                ///camera.autoFocus(null);
                camera.autoFocus(new Camera.AutoFocusCallback() {
                    @Override
                    public void onAutoFocus(boolean success, Camera camera) {
                        if(success) {
                            enableFocus = true;
                            Log.d("CameraPreview", "esta enfocado" );
                           // camera.takePicture(null, null, null);
                        } else {
                            enableFocus = false;
                            //NOT YET FOCUSED
                        }
                    }
                });
            } catch (Exception e) {
                Log.d(TAG, "takePhoto " + e);
            }
        }
    }

    /**
     * Cambiar flash
     *
     * @return Si el flash está encendido
     */
    public boolean switchFlashLight() {
        if (camera != null) {
            Camera.Parameters parameters = camera.getParameters();
            if (parameters.getFlashMode().equals(Camera.Parameters.FLASH_MODE_OFF)) {
                parameters.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);
                camera.setParameters(parameters);
                return true;
            } else {
                parameters.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
                camera.setParameters(parameters);
                return false;
            }
        }
        return false;
    }

    /**
     * Tomando la foto
     *
     * @param pictureCallback 在pictureCallback处理拍照回调
     */
    public void takePhoto(Camera.PictureCallback pictureCallback) {
        if (camera != null) {
            try {
                camera.takePicture(null, null, pictureCallback);
            } catch (Exception e) {
                Log.d(TAG, "takePhoto " + e);
            }
        }
    }

    public void startPreview() {
        if (camera != null) {
            camera.startPreview();
        }
    }

    public void onStart() {
        addCallback();
        if (mSensorFocusControler != null) {

            mSensorFocusControler.onStart();
            mSensorFocusControler.setCameraFocusListener(new SensorFocusControler.CameraFocusListener() {
                @Override
                public void onFocus() {
                    focus();
                }
            });
        }
    }

    public void onStop() {
        if (mSensorFocusControler != null) {
            mSensorFocusControler.onStop();
        }
    }

    public void addCallback() {
        if (mSurfaceHolder != null) {
            mSurfaceHolder.addCallback(this);
        }
    }

    public boolean getfocusandlevel(){

        return (bubbleLevel.isCameraEnabled() && enableFocus) ;
    }


}
