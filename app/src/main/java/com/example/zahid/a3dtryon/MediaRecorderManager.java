package com.example.zahid.a3dtryon;

/**
 * Created by Zahid on 23-Jul-18.
 */

import java.io.IOException;

import android.content.Context;
import android.graphics.Color;
import android.hardware.Camera;
import android.hardware.Camera.Size;
import android.media.MediaRecorder;
import android.os.Handler;
import android.util.Log;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.hardware.Camera.Size;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import com.example.zahid.a3dtryon.AdaptiveSurfaceView;
import com.example.zahid.a3dtryon.CameraHelper;
import com.example.zahid.a3dtryon.VideoRecordingHandler;
import com.example.zahid.a3dtryon.VideoRecordingManager;
import com.example.zahid.a3dtryon.utils.NotificationUtils;
import com.example.zahid.a3dtryon.utils.StorageUtils;

/*
 * Manages media recorder recording
 */

public class MediaRecorderManager extends Activity implements MediaRecorder.OnInfoListener {
    private static final int VIDEO_W_DEFAULT = 640;
    private static final int VIDEO_H_DEFAULT = 480;
    private static String fileName = null;
    private Connect ct;
    private MediaRecorder recorder;
    private boolean isRecording;
    private Size videoSize = null;
    private ImageView img;

    private VideoRecordingManager recordingManager;

    private VideoRecordingHandler recordingHandler = new VideoRecordingHandler() {
        @Override
        public boolean onPrepareRecording() {
		/*	if (videoSizeSpinner == null) {
	    		initVideoSizeSpinner();
	    		return true;
			}
			return false;*/
            return false;
        }

        @Override
        public Size getVideoSize() {
            return videoSize;
        }

        @Override
        public int getDisplayRotation() {
            return MediaRecorderManager.this.getWindowManager().getDefaultDisplay().getRotation();
        }
    };
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.video_rec);

        final Button button=(Button) findViewById(R.id.button5);
        final ImageView img=(ImageView) findViewById(R.id.imageView);
        if (!StorageUtils.checkExternalStorageAvailable()) {
            NotificationUtils.showInfoDialog(this, getString(R.string.noExtStorageAvailable));
            return;
        }
        fileName = StorageUtils.getFileName(false);
        Log.i("22", fileName);

        AdaptiveSurfaceView videoView = (AdaptiveSurfaceView) findViewById(R.id.videoView);
        recordingManager = new VideoRecordingManager(videoView, recordingHandler);
        if (recordingManager.getCameraManager().hasMultipleCameras()) {
            //	switchBtn.setOnClickListener(new OnClickListener() {
            //@Override
				/*public void onClick(View v) {
					switchCamera();
				}
			});*/
        }


        button.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                recordingManager.startRecording(fileName, videoSize);
                button.setText("Recording");
                button.setBackgroundColor(Color.RED);
                img.setVisibility(View.VISIBLE);
                button.setEnabled(false);

            }
        });

        ct.addMyBooleanListener(new ConnectionBooleanChangedListener() {
            @Override
            public void OnMyBooleanChanged() {
                Intent i = new Intent(MediaRecorderManager.this, UploadActivity.class);
                i.putExtra("filePath", fileName);
                i.putExtra("isImage", false);
                startActivity(i);
            }
        });


    }

    @Override
    protected void onDestroy() {
        recordingManager.dispose();
        recordingHandler = null;

        super.onDestroy();
    }



    public MediaRecorderManager() {
        recorder = new MediaRecorder();
    }

    public boolean startRecording(Camera camera, String fileName, Size sz, int cameraRotationDegree) {
        if (sz == null) {
            sz = camera.new Size(VIDEO_W_DEFAULT, VIDEO_H_DEFAULT);
        }

        try {
            camera.unlock();
            recorder.setCamera(camera);
            recorder.setOrientationHint(cameraRotationDegree);
            recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
            recorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);
            recorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
            recorder.setVideoSize(640, 480);
            recorder.setAudioEncoder(MediaRecorder.AudioEncoder.DEFAULT);
            recorder.setVideoEncoder(MediaRecorder.VideoEncoder.MPEG_4_SP);


            //Quality
            //	recorder.setVideoEncodingBitRate(2500000);
            //	recorder.setVideoFrameRate(30);
            recorder.setOutputFile(fileName);
            recorder.setMaxDuration(5000); // 5 seconds
            recorder.setMaxFileSize(4000000); // Approximately 4 megabytes
            recorder.setOnInfoListener(this);
            recorder.prepare();
            recorder.start();
            isRecording = true;
        } catch (IllegalStateException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }


        return isRecording;
    }

    public void onInfo(MediaRecorder mr, int what, int extra) {
        if (what == MediaRecorder.MEDIA_RECORDER_INFO_MAX_DURATION_REACHED) {
            Log.v("VIDEOCAPTURE","Maximum Duration Reached");
            stopRecording();
            ct.setMyBoolean(true);



        }
    }

    public boolean stopRecording() {
        if (isRecording) {
            isRecording = false;
            try{
                recorder.stop();
                recorder.reset();
                recorder.release();

            }
            catch (Exception e){

            }


            return true;
        }
        return false;
    }

    public void releaseRecorder() {
        recorder.release();
        recorder = null;
    }
    @Override
    public void onBackPressed() {



        recordingManager.dispose();
        recordingHandler = null;
        Intent i = new Intent(MediaRecorderManager.this, MainActivity.class);
        startActivity(i);
    }


    public boolean isRecording() {
        return isRecording;
    }
}
