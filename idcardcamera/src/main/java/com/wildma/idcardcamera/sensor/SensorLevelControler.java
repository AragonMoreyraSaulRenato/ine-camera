package com.wildma.idcardcamera.sensor;


import android.app.Activity;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.Log;


public class SensorLevelControler implements SensorEventListener{

    static private final String TAG = "BubbleLevel";
    static private final double GRAVITY = 9.81d;
    static private final double MIN_DEGREE = -20d;
    static private final double MAX_DEGREE = 20d;

    private Sensor                  sensor;
    private SensorManager           sensorManager;

    private Boolean enablePhoto;
    private Boolean tonePlayed;
    private double thetaX;
    private double thetaY;


    public SensorLevelControler(Context ctx) {
        this.sensorManager =  (SensorManager) ctx.getSystemService(Activity.SENSOR_SERVICE);
        this.sensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);// TYPE_GRAVITY
        sensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_NORMAL);
        enablePhoto = false;
        tonePlayed = false;
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



        if (thetaX >= MIN_DEGREE && thetaX <= MAX_DEGREE && thetaY >= MIN_DEGREE && thetaY <= MAX_DEGREE && gz > 0d) {
            enablePhoto = true;
            Log.i(TAG, "Camera stable");
        } else {
            Log.i(TAG, "Camera no stable");
            enablePhoto = false;
            tonePlayed = false;
           // userMessage.setBackgroundColor(Color.RED);

            if (thetaY > 0) {
               // userMessage.setText(R.string.phone_up);
            } else {
              //  userMessage.setText(R.string.phone_down);
            }
        }

    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }

    public Boolean isCameraEnabled() {
        return enablePhoto;
    }

    public double[] getPhoneAngles() {
        double [] angles = {thetaX, thetaY};

        return angles;
    }

}