package com.wildma.idcardcamera.sensor;


import android.app.Activity;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.Log;
import android.widget.ImageView;

import com.wildma.idcardcamera.R;
import com.wildma.idcardcamera.camera.CameraActivity;
import com.wildma.idcardcamera.camera.IDCardCamera;

import java.util.Observable;


public class SensorLevelControler implements SensorEventListener {

    static private final String TAG = SensorLevelControler.class.getSimpleName();
    static private final double GRAVITY = 9.81d;
    static private final double MIN_DEGREE = -8d;
    static private final double MAX_DEGREE = 8d;

    private Sensor           sensor;
    private SensorManager    sensorManager;
    private Context          mContext;


    private Boolean enablePhoto;
    private double thetaX;
    private double thetaY;


    public  SensorLevelControler(Context ctx) {
        this.mContext = ctx;
        this.sensorManager =  (SensorManager) ctx.getSystemService(Activity.SENSOR_SERVICE);
        this.sensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);// TYPE_GRAVITY
        sensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_NORMAL);
    }

    public void restParams(){
        enablePhoto = false;
        thetaX = 0d;
        thetaY = 0d;
    }


    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        double gx = sensorEvent.values[0] > GRAVITY ? GRAVITY : sensorEvent.values[0];
        double gy = sensorEvent.values[1] > GRAVITY ? GRAVITY : sensorEvent.values[1];
        double gz = sensorEvent.values[2];

        gx = gx < -GRAVITY ? -GRAVITY : gx;
        gy = gy < -GRAVITY ? -GRAVITY : gy;

        thetaX = Math.toDegrees(Math.asin(gy/GRAVITY));
        thetaY = Math.toDegrees(Math.asin(gx/GRAVITY));


        if(CameraActivity.mIvCameraCrop != null) {
            if (thetaX >= MIN_DEGREE && thetaX <= MAX_DEGREE && thetaY >= MIN_DEGREE && thetaY <= MAX_DEGREE && gz > 0d) {
                enablePhoto = true;
                switch (CameraActivity.mType) {
                    case IDCardCamera.TYPE_IDCARD_FRONT:
                        CameraActivity.mIvCameraCrop .setImageResource(R.mipmap.camera_idcard_front_ok);
                        break;
                    case IDCardCamera.TYPE_IDCARD_BACK:
                        CameraActivity.mIvCameraCrop .setImageResource(R.mipmap.camera_idcard_back_ok);
                        break;
                }
            } else {
                enablePhoto = false;
                switch (CameraActivity.mType) {
                    case IDCardCamera.TYPE_IDCARD_FRONT:
                        CameraActivity.mIvCameraCrop .setImageResource(R.mipmap.camera_idcard_front);
                        break;
                    case IDCardCamera.TYPE_IDCARD_BACK:
                        CameraActivity.mIvCameraCrop .setImageResource(R.mipmap.camera_idcard_back);
                        break;
                }
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }

    public void onStart() {
        enablePhoto = false;
        sensorManager.registerListener(this, sensor,
                SensorManager.SENSOR_DELAY_NORMAL);
    }

    public void onStop() {
        sensorManager.unregisterListener(this, sensor);
        enablePhoto = false;
    }

    public Boolean isCameraEnabled() {
        return enablePhoto;
    }

    public double[] getPhoneAngles() {
        double [] angles = {thetaX, thetaY};

        return angles;
    }

}