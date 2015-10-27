package com.github.hhannu.spdcam;

import android.media.MediaRecorder;
import android.os.Environment;
import android.util.Log;
import android.view.Surface;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;


public class Recorder {

    private static final String TAG = "SpdCam:Recorder";
    private MediaRecorder mMediaRecorder;

    public Recorder() {}

    public boolean prepare(int width, int height){
        mMediaRecorder = new MediaRecorder();

        // Set sources
        //mMediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mMediaRecorder.setVideoSource(MediaRecorder.VideoSource.SURFACE);

        // Set output formats
        mMediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.DEFAULT);
        mMediaRecorder.setVideoEncoder(MediaRecorder.VideoEncoder.DEFAULT);
        mMediaRecorder.setVideoSize(width, height);

        // Set output file
        mMediaRecorder.setOutputFile(getOutputMediaFile().toString());

        // Prepare configured MediaRecorder
        try {
            mMediaRecorder.prepare();
        } catch (IllegalStateException e) {
            Log.d(TAG, "prepare() IllegalStateException: " + e.getMessage());
            release();
            return false;
        } catch (IOException e) {
            Log.d(TAG, "prepare() IOException: " + e.getMessage());
            release();
            return false;
        }
        Log.d(TAG, "prepare() OK");
        return true;
    }

    public Surface getSurface() {
        Log.d(TAG, "getSurface()");
        return mMediaRecorder.getSurface();
    }

    public void start() {
        Log.d(TAG, "start()");

    }

    public void stop() {
        Log.d(TAG, "stop()");
    }

    private static File getOutputMediaFile() {

        File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_MOVIES), "SpdCam");

        // Create the storage directory if it does not exist
        if (!mediaStorageDir.exists()){
            if (!mediaStorageDir.mkdirs()){
                Log.d(TAG, "Failed to create directory " + mediaStorageDir.getPath());
                return null;
            }
        }

        // Create a media file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        File mediaFile = new File(mediaStorageDir.getPath() + File.separator + timeStamp + ".mp4");

        Log.d(TAG, "getOutputMediaFile() " + mediaFile.toString());
        return mediaFile;
    }

    public void release(){
        Log.d(TAG, "release()");
        if (mMediaRecorder != null) {
            mMediaRecorder.reset();
            mMediaRecorder.release();
            mMediaRecorder = null;
        }
    }
}
