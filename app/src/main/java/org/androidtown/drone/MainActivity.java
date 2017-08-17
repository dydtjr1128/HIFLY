package org.androidtown.drone;

import android.app.Activity;
import android.graphics.SurfaceTexture;
import android.media.MediaCodec;
import android.media.MediaFormat;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.TextureView;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.TextureView.SurfaceTextureListener;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Vector;

import dji.common.camera.SettingsDefinitions;
import dji.common.camera.SystemState;
import dji.common.error.DJIError;
import dji.common.product.Model;
import dji.common.util.CommonCallbacks;
import dji.midware.media.DJIVideoDecoder;
import dji.sdk.base.BaseProduct;
import dji.sdk.camera.Camera;
import dji.sdk.camera.MediaFile;
import dji.sdk.camera.MediaManager;
import dji.sdk.camera.VideoFeeder;
import dji.sdk.codec.DJICodecManager;

public class MainActivity extends Activity implements OnClickListener {

    private static final String TAG = MainActivity.class.getName();
    protected VideoFeeder.VideoDataCallback mReceivedVideoDataCallBack = null;

    // Codec for video live view
    protected DJICodecManager mCodecManager = null;

    protected TextureView mVideoSurface = null;
    private Button mCaptureBtn, mShootPhotoModeBtn, mRecordVideoModeBtn;
    private ToggleButton mRecordBtn;
    private TextView recordingTime;
    private ArrayList<MediaFile> mMediaFiles;
    private MediaFile media;
    private Handler handler;

    private MediaFormat format;
    private CSMMdiaCodec codec = null;
    private Surface hiSurface;
    private SurfaceView sv;
    private SurfaceHolder sh;
    byte[] splitPattern = {(byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x01, (byte) 0x09};
    boolean isfirst = true;
    int maxBufSize = 200000;
    byte[] cbuf;
    int cbufPosition = 0;
    TextureView mTextureView;
    CSMSendSocket csmSocket= new CSMSendSocket();
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        handler = new Handler();

        initUI();
        /*try {
            InputStream inputStream = getApplication().getResources().openRawResource(R.raw.iframe_1280x720_p4);
            int length = inputStream.available();
            byte[] Iframebuffer = new byte[length];
            inputStream.read(Iframebuffer);
        }
        catch (Exception e){

        }*/

        // The callback for receiving the raw H264 video data for camera live view
        mReceivedVideoDataCallBack = new VideoFeeder.VideoDataCallback() {

            @Override
            public void onReceive(byte[] videoBuffer, int size) {
                Log.i("kkkkk",Arrays.toString(videoBuffer));
                if(size == 6){
                    /*Log.i("kkkkk",videoBuffer[0] + " " + videoBuffer[1] + " " + videoBuffer[2] + " " + videoBuffer[3] + " " + videoBuffer[4] + " " + videoBuffer[5]);//AUD
                    Log.i("kkkkk",videoBuffer[6] + " " + videoBuffer[7] + " " + videoBuffer[8] + " " + videoBuffer[9] + " " + videoBuffer[10] + " " + videoBuffer[11]);//SPS
                    Log.i("kkkkk",videoBuffer[12] + " " + videoBuffer[13] + " " + videoBuffer[14] + " " + videoBuffer[15] + " " + videoBuffer[16] + " " + videoBuffer[17]+ "//");//PPS*/
                }
                System.arraycopy(videoBuffer,0,cbuf,cbufPosition,size);
                cbufPosition+=size;
                if(size == 6){
                    byte[] data = new byte[cbufPosition];
                    System.arraycopy(cbuf, 0, data, 0, cbufPosition);
                    Log.i("kkkkk",cbufPosition+" ");
                    try {
                        codec.InputYUVData(data);
                        csmSocket.sendSocket(data);
                    } catch (IOException e) {
                        e.printStackTrace();
                        csmSocket.endSocket();
                    }
                    cbufPosition = 0;
                    Camera c;

                }
                else {
                   // Log.i("kkkdrone","normal size:" + size);
                }

              /*  if (codec != null && videoBuffer != null && size > 0) {
                    if (isfirst) {
                        if (cbufPosition + size >= maxBufSize) {
                            cbufPosition = showVideo(1, cbuf, cbufPosition);
                            System.arraycopy(videoBuffer, 0, cbuf, cbufPosition, size);
                            isfirst = false;
                        } else {
                            System.arraycopy(videoBuffer, 0, cbuf, cbufPosition, size);
                            cbufPosition += size;
                        }
                    } else {
                        if (cbufPosition + size >= maxBufSize) {
                            cbufPosition = showVideo(0, cbuf, cbufPosition);
                            System.arraycopy(videoBuffer, 0, cbuf, cbufPosition, size);
                        } else {
                            System.arraycopy(videoBuffer, 0, cbuf, cbufPosition, size);
                            cbufPosition += size;
                        }
                    }*/
            }
        };

        Camera camera = FPVDemoApplication.getCameraInstance();

        if (camera != null) {

            camera.setSystemStateCallback(new SystemState.Callback() {
                @Override
                public void onUpdate(SystemState cameraSystemState) {
                    if (null != cameraSystemState) {

                        int recordTime = cameraSystemState.getCurrentVideoRecordingTimeInSeconds();
                        int minutes = (recordTime % 3600) / 60;
                        int seconds = recordTime % 60;

                        final String timeString = String.format("%02d:%02d", minutes, seconds);
                        final boolean isVideoRecording = cameraSystemState.isRecording();

                        MainActivity.this.runOnUiThread(new Runnable() {

                            @Override
                            public void run() {

                                recordingTime.setText(timeString);

                                /*
                                 * Update recordingTime TextView visibility and mRecordBtn's check state
                                 */
                                if (isVideoRecording) {
                                    recordingTime.setVisibility(View.VISIBLE);
                                } else {
                                    recordingTime.setVisibility(View.INVISIBLE);
                                }
                            }
                        });
                    }
                }
            });

        }

    }

    protected void onProductChange() {
        initPreviewer();
    }

    @Override
    public void onResume() {
        Log.e(TAG, "onResume");
        super.onResume();
        initPreviewer();
        onProductChange();

        if (mVideoSurface == null) {
            Log.e(TAG, "mVideoSurface is null");
            Log.e(TAG, "mVideoSurface is null");
        }
    }

    @Override
    public void onPause() {
        Log.e(TAG, "onPause");
        uninitPreviewer();
        super.onPause();
    }

    @Override
    public void onStop() {
        Log.e(TAG, "onStop");
        super.onStop();
    }

    public void onReturn(View view) {
        Log.e(TAG, "onReturn");
        this.finish();
    }

    @Override
    protected void onDestroy() {
        Log.e(TAG, "onDestroy");
        uninitPreviewer();
        super.onDestroy();
    }

    private void initUI() {
        // init mVideoSurface
       // mVideoSurface = (TextureView) findViewById(R.id.video_previewer_surface);

        recordingTime = (TextView) findViewById(R.id.timer);
        mCaptureBtn = (Button) findViewById(R.id.btn_capture);
        mRecordBtn = (ToggleButton) findViewById(R.id.btn_record);
        mShootPhotoModeBtn = (Button) findViewById(R.id.btn_shoot_photo_mode);
        mRecordVideoModeBtn = (Button) findViewById(R.id.btn_record_video_mode);


        mCaptureBtn.setOnClickListener(this);
        mRecordBtn.setOnClickListener(this);
        mShootPhotoModeBtn.setOnClickListener(this);
        mRecordVideoModeBtn.setOnClickListener(this);

        cbuf = new byte[maxBufSize];

        mTextureView=(TextureView)findViewById(R.id.texture);
        mTextureView.setSurfaceTextureListener(new SurfaceTextureListener() {
            @Override
            public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
                sv = (SurfaceView) findViewById(R.id.surface_view);
                Log.i("kkkdrone","sv : " + sv.getWidth() + "," + sv.getHeight() + " " + sv.toString());
                sh = sv.getHolder();
                Log.i("kkkdrone","sh : " + sh.toString());
                hiSurface = sh.getSurface();
                Log.i("kkkdrone","surface : " + hiSurface.toString());
                codec = new CSMMdiaCodec(hiSurface);
                Log.i("kkkdrone","codec : " + codec.toString());
            }

            @Override
            public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {

            }

            @Override
            public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
                return false;
            }

            @Override
            public void onSurfaceTextureUpdated(SurfaceTexture surface) {

            }
        });






        recordingTime.setVisibility(View.INVISIBLE);

        mRecordBtn.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    startRecord();
                } else {
                    stopRecord();
                }
            }
        });
    }

    private void initPreviewer() {

        BaseProduct product = FPVDemoApplication.getProductInstance();

        if (product == null || !product.isConnected()) {
            showToast(getString(R.string.disconnected));
        } else {
            if (!product.getModel().equals(Model.UNKNOWN_AIRCRAFT)) {
                if (VideoFeeder.getInstance().getVideoFeeds() != null
                        && VideoFeeder.getInstance().getVideoFeeds().size() > 0) {
                    VideoFeeder.getInstance().getVideoFeeds().get(0).setCallback(mReceivedVideoDataCallBack);
                }
            }
        }
    }

    private void uninitPreviewer() {
        Camera camera = FPVDemoApplication.getCameraInstance();
        if (camera != null) {
            // Reset the callback
            VideoFeeder.getInstance().getVideoFeeds().get(0).setCallback(null);
        }
}


    public void showToast(final String msg) {
        runOnUiThread(new Runnable() {
            public void run() {
                Toast.makeText(MainActivity.this, msg, Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onClick(View v) {

        switch (v.getId()) {
            case R.id.btn_capture: {
                captureAction();
                break;
            }
            case R.id.btn_shoot_photo_mode: {
                switchCameraMode(SettingsDefinitions.CameraMode.SHOOT_PHOTO);
                break;
            }
            case R.id.btn_record_video_mode: {
                switchCameraMode(SettingsDefinitions.CameraMode.RECORD_VIDEO);
                break;
            }
            default:
                break;
        }

    }

    MediaManager.DownloadListener mm = new MediaManager.DownloadListener<ArrayList<MediaFile>>() {
        @Override
        public void onStart() {

        }

        @Override
        public void onRateUpdate(long l, long l1, long l2) {

        }

        @Override
        public void onProgress(long l, long l1) {

        }

        @Override
        public void onSuccess(ArrayList<MediaFile> medias) {
            if (null != medias) {
                if (!medias.isEmpty()) {
                    media = medias.get(0);

                    mMediaFiles = medias;
                } else {

                }
            }
        }

        @Override
        public void onFailure(DJIError djiError) {

        }
    };


    private void switchCameraMode(SettingsDefinitions.CameraMode cameraMode) {

        Camera camera = FPVDemoApplication.getCameraInstance();
        if (camera != null) {
            camera.setMode(cameraMode, new CommonCallbacks.CompletionCallback() {
                @Override
                public void onResult(DJIError error) {

                    if (error == null) {
                        showToast("Switch Camera Mode Succeeded");
                    } else {
                        showToast(error.getDescription());
                    }
                }
            });
        }
    }

    // Method for taking photo
    private void captureAction() {

        final Camera camera = FPVDemoApplication.getCameraInstance();
        if (camera != null) {

            SettingsDefinitions.ShootPhotoMode photoMode = SettingsDefinitions.ShootPhotoMode.SINGLE; // Set the camera capture mode as Single mode
            camera.setShootPhotoMode(photoMode, new CommonCallbacks.CompletionCallback() {
                @Override
                public void onResult(DJIError djiError) {
                    if (null == djiError) {
                        handler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                camera.startShootPhoto(new CommonCallbacks.CompletionCallback() {
                                    @Override
                                    public void onResult(DJIError djiError) {
                                        if (djiError == null) {
                                            showToast("take photo: success");
                                        } else {
                                            showToast(djiError.getDescription());
                                        }
                                    }
                                });
                            }
                        }, 2000);
                    }
                }
            });
        }
    }

    // Method for starting recording
    private void startRecord() {

        final Camera camera = FPVDemoApplication.getCameraInstance();
        if (camera != null) {

            camera.startRecordVideo(new CommonCallbacks.CompletionCallback() {
                @Override
                public void onResult(DJIError djiError) {
                    if (djiError == null) {
                        showToast("Record video: success");
                    } else {
                        showToast(djiError.getDescription());
                    }
                }
            }); // Execute the startRecordVideo API
        }
    }

    // Method for stopping recording
    private void stopRecord() {

        Camera camera = FPVDemoApplication.getCameraInstance();
        if (camera != null) {
            camera.stopRecordVideo(new CommonCallbacks.CompletionCallback() {

                @Override
                public void onResult(DJIError djiError) {
                    if (djiError == null) {
                        showToast("Stop recording: success");
                    } else {
                        showToast(djiError.getDescription());
                    }
                }
            }); // Execute the stopRecordVideo API
        }

    }
}