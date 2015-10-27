package com.github.hhannu.spdcam;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.hardware.Camera;
import android.hardware.display.DisplayManager;
import android.hardware.display.VirtualDisplay;
import android.media.projection.MediaProjection;
import android.media.projection.MediaProjectionManager;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;


public class MainActivity extends ActionBarActivity {

    private static final String TAG = "SpdCam";
    private static final int PERMISSION_CODE = 000706; // http://goo.gl/F49HUP
    private Camera mCamera;
    private CameraPreview mPreview;
    private Button recordButton;

    private boolean isRecording = false;
    private MediaProjectionManager mProjectionManager;
    private MediaProjection mMediaProjection;
    private VirtualDisplay mVirtualDisplay;
    
    private Recorder mRecorder;

    private int mDisplayWidth;
    private int mDisplayHeight;
    private int mScreenDensity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Create an instance of Camera
        mCamera = getCameraInstance();
        
        mRecorder = new Recorder();

        // Create our Preview view and set it as the content of our activity.
        mPreview = new CameraPreview(this, mCamera);
        FrameLayout preview = (FrameLayout) findViewById(R.id.cameraPreview);
        preview.addView(mPreview);

        mProjectionManager =
            (MediaProjectionManager) getSystemService(Context.MEDIA_PROJECTION_SERVICE);

        final DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);
        mScreenDensity = metrics.densityDpi;

        shareScreen();

        recordButton = (Button) findViewById(R.id.recordButton);
        recordButton.setText("Rec");

        recordButton.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onClick() " + (isRecording ? "Stop recording" : "Start recording"));
                if (isRecording) {
                    setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
                    // Stop recording
                    mRecorder.stop();
                    mRecorder.release();

                    stopScreenSharing();
                    recordButton.setText("Rec");
                    isRecording = false;
                } else {
                    getWindowManager().getDefaultDisplay().getMetrics(metrics);
                    mDisplayWidth = metrics.widthPixels;
                    mDisplayHeight = metrics.heightPixels;
                    //  Start recording.
                    if (mRecorder.prepare(mDisplayWidth, mDisplayHeight)) {
                        if (getResources().getConfiguration().orientation ==
                                Configuration.ORIENTATION_PORTRAIT) {
                            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
                        }
                        else {
                            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
                        }
                        mVirtualDisplay = createVirtualDisplay();
                        Log.d(TAG, mVirtualDisplay.toString());
                        mRecorder.start();
                        recordButton.setText("Stop");
                        isRecording = true;
                    }
                }
            }
        });
    }

    @Override
    protected void onPause() {
        Log.d(TAG, "onPause() isFinishing == " + isFinishing());
        super.onPause();

        if(isFinishing()) {
            if (isRecording) {
                mRecorder.stop();
                mRecorder.release();
                isRecording = false;
            }
            stopScreenSharing();
            if (mMediaProjection != null) {
                mMediaProjection.stop();
                mMediaProjection = null;
            }
            releaseCamera();
        }
    }

    private void shareScreen() {
        if (mMediaProjection == null) {
            Log.d(TAG, "shareScreen() mMediaProjection == null");
            startActivityForResult(mProjectionManager.createScreenCaptureIntent(), PERMISSION_CODE);
            return;
        }
        Log.d(TAG, "shareScreen()");
    }

    private void stopScreenSharing() {
        Log.d(TAG, "stopScreenSharing()");
        if (mVirtualDisplay == null) {
            return;
        }
        mVirtualDisplay.release();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        //Log.d(TAG, "onActivityResult()");
        if (requestCode != PERMISSION_CODE) {
            Log.e(TAG, "Unknown request code: " + requestCode);
            return;
        }
        if (resultCode != RESULT_OK) {
            Log.e(TAG, "Screen sharing denied");
            return;
        }
        mMediaProjection = mProjectionManager.getMediaProjection(resultCode, data);

        //mVirtualDisplay = createVirtualDisplay();
    }

    private VirtualDisplay createVirtualDisplay() {
        Log.d(TAG, "createVirtualDisplay()");

        return mMediaProjection.createVirtualDisplay(TAG,
                mDisplayWidth, mDisplayHeight, mScreenDensity,
                DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR,
                null, null, null);
                //mRecorder.getSurface(), null, null);
        // The phone crashes when the surface is set other than null.
    }

    public static Camera getCameraInstance(){
        //Log.d(TAG, "getCameraInstance()");
        Camera c = null;
        try {
            c = Camera.open();
        }
        catch (Exception e){
            Log.d(TAG, "Failed to get camera instance: " + e.getMessage());
        }
        if (c != null) {
            Camera.Parameters parameters = c.getParameters();
            parameters.setRecordingHint(true);
            c.setParameters(parameters);
        }
        return c;
    }

    private void releaseCamera(){
        Log.d(TAG, "releaseCamera()");
        if (mCamera != null){
            mCamera.release();
            mCamera = null;
        }
    }
}
