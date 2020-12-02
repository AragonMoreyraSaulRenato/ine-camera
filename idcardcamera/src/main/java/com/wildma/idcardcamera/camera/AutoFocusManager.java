package com.wildma.idcardcamera.camera;

import android.annotation.SuppressLint;
import android.hardware.Camera;
import android.os.AsyncTask;
import android.os.Build;
import android.util.Log;

import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.RejectedExecutionException;


/**
 * Esta clase ayuda para simplificar el trabajo de autoenfoque,
 * tambien permite establecer un autoenfoque automatico cada determinado intervalo de tiempo
 * Se axulia de la interfaz AutoFocusCallback
*/

public class AutoFocusManager implements Camera.AutoFocusCallback {

    /**El TAG para debbugear la clase**/
    private static final String TAG = AutoFocusManager.class.getSimpleName();

    /**El intervalo de tiempo para el autoenfoque**/
    private static final long AUTO_FOCUS_INTERVAL_MS = 2000L;

    /**Colleción para los modos de enfoque**/
    private static final Collection<String> FOCUS_MODES_CALLING_AF;

    static {
        FOCUS_MODES_CALLING_AF = new ArrayList<String>(2);
        FOCUS_MODES_CALLING_AF.add(Camera.Parameters.FOCUS_MODE_AUTO);
        FOCUS_MODES_CALLING_AF.add(Camera.Parameters.FOCUS_MODE_MACRO);
    }

    /**Bandera para determinar si se implementa el autoenfoque**/
    private final boolean useAutoFocus;

    /**Objeto Camara de la clase**/
    private final Camera camera;

    /**Bandera para determinar si se detuvo el autoenfoque**/
    private boolean stopped;

    /**Bandera para determinar si se detuvo el autoenfoque**/
    private boolean focusing;

    /**Objeto de tareas pendientes**/
    private  AsyncTask<?, ?, ?> outstandingTask;


    /**
     * Constructor de la clase, establece el tipo de enfoque y lo inicia
     * @Param Camera**/
    public AutoFocusManager(Camera camera) {
        this.camera = camera;
        String currentFocusMode = camera.getParameters().getFocusMode();
        useAutoFocus = FOCUS_MODES_CALLING_AF.contains(currentFocusMode);
        start();
    }

    @Override
    public synchronized void onAutoFocus(boolean success, Camera theCamera) {
        focusing = false;
        autoFocusAgainLater();
    }

    @SuppressLint("NewApi")
    private synchronized void autoFocusAgainLater() {
        if (!stopped && outstandingTask == null) {
            AutoFocusTask newTask = new AutoFocusTask();
            try {
                if (Build.VERSION.SDK_INT >= 11) {
                    newTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                } else {
                    newTask.execute();
                }
                outstandingTask = newTask;
            } catch (RejectedExecutionException ree) {
                Log.w(TAG, "Could not request auto focus", ree);
            }
        }
    }

    public synchronized void start() {
        if (useAutoFocus) {
            outstandingTask = null;
            if (!stopped && !focusing) {
                try {
                    camera.autoFocus(this);
                    Log.w(TAG, "Generating autofocus");
                    focusing = true;
                } catch (RuntimeException re) {
                    Log.w(TAG, "Unexpected exception while focusing", re);
                    autoFocusAgainLater();
                }
            }
        }
    }

    private synchronized void cancelOutstandingTask() {
        if (outstandingTask != null) {
            if (outstandingTask.getStatus() != AsyncTask.Status.FINISHED) {
                outstandingTask.cancel(true);
            }
            outstandingTask = null;
        }
    }

    public synchronized void stop() {
        stopped = true;
        if (useAutoFocus) {
            cancelOutstandingTask();
            try {
                camera.cancelAutoFocus();
            } catch (RuntimeException re) {
                Log.w(TAG, "Unexpected exception while cancelling focusing", re);
            }
        }
    }

    private final class AutoFocusTask extends AsyncTask<Object, Object, Object> {
        @Override
        protected Object doInBackground(Object... voids) {
            try {
                Thread.sleep(AUTO_FOCUS_INTERVAL_MS);
            } catch (InterruptedException e) {
            }
            start();
            return null;
        }
    }

}
