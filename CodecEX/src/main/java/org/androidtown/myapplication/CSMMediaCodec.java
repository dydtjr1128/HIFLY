package org.androidtown.myapplication;

import android.graphics.SurfaceTexture;
import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaExtractor;
import android.media.MediaFormat;
import android.media.MediaMuxer;
import android.os.Environment;
import android.util.Log;
import android.util.Size;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;

import android.media.MediaCodec.BufferInfo;

/**
 * Created by CYSN on 2017-07-11.
 */

public class CSMMediaCodec {
    private MediaFormat format;
    private MediaCodec codec;
    private MediaMuxer muxer;
    private MediaExtractor mExtractor;
    private final int timeoutUs = 10000;
    private final String path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES).toString() + "temp.mp4";
    private boolean finished;
    BufferInfo bufferInfo;

    public CSMMediaCodec(Size size) {
        setupMediaCodec(size);
       // initCodec();
    }

    public Runnable thread = new Runnable() {
        public void run() {
        }
    };

    public void InputYUVData(byte[] bytes) {
        int inputBufferIndex = codec.dequeueInputBuffer(timeoutUs);
        if (inputBufferIndex >= 0) {
            // fill inputBuffers[inputBufferIndex] with valid data
            ByteBuffer inputBuffer = codec.getInputBuffer(inputBufferIndex);
            inputBuffer.reset();
            inputBuffer = ByteBuffer.wrap(bytes);
            codec.queueInputBuffer(inputBufferIndex, 0, bytes.length, timeoutUs, 0);
        }
    }

    public void outputData() {
        bufferInfo = new BufferInfo();
        int outputBufferIndex = codec.dequeueOutputBuffer(bufferInfo, timeoutUs);
        if (outputBufferIndex >= 0) { // 0 이상일 경우에 인코딩/디코딩 데이터가 출력됩니다.
            ByteBuffer outputBuffer = codec.getOutputBuffer(outputBufferIndex);

            codec.releaseOutputBuffer(outputBufferIndex, false);
            int videoTrackIndex = muxer.addTrack(format);
            muxer.start();
            muxer.writeSampleData(outputBufferIndex, outputBuffer, bufferInfo);


        }
    }
   /* public void getMP4(){
        muxer.start();
            while(!finished) {
                // getInputBuffer() will fill the inputBuffer with one frame of encoded
                // sample from either MediaCodec or MediaExtractor, set isAudioSample to
                // true when the sample is audio data, set up all the fields of bufferInfo,
                // and return true if there are no more samples.
                muxer.writeSampleData(outputBufferIndex, outputBuffer, bufferInfo);
            };
muxer.stop();
muxer.release();




    }*/


    public void endCodec() {
        codec.stop();
        codec.release();
        codec = null;
    }

    public void endMuxer() {
        muxer.stop();
        muxer.release();
        muxer = null;
    }

    private void saveFile(byte[] data) {
        OutputStream output = null;
        //Toast.makeText(getApplicationContext(),"받음" + videoBuffer.length,Toast.LENGTH_SHORT).show();

        try {
            File path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES);
            File outputFile = new File(path, "codecTestData.h264"); //파일명까지 포함함 경로의 File 객체 생성

            // SD카드에 저장하기 위한 Output stream
            output = new FileOutputStream(outputFile);


            output.write(data);

            // Flush output
            output.flush();
            // Close streams
            output.close();

        } catch (Exception e) {
            Log.e("Error: ", e.getMessage());
        }
    }
    private void initCodec() {
        try {
            codec = MediaCodec.createEncoderByType("video/avc");
        } catch (IOException e) {
            e.printStackTrace();
        }
        MediaFormat mediaFormat = MediaFormat.createVideoFormat("video/avc",
                1920,
                1080);
        mediaFormat.setInteger(MediaFormat.KEY_BIT_RATE, 125000);
        mediaFormat.setInteger(MediaFormat.KEY_FRAME_RATE, 30);
        mediaFormat.setInteger(MediaFormat.KEY_COLOR_FORMAT,
                MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420Flexible);
        mediaFormat.setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, 5);
        codec.configure(mediaFormat,
                null,
                null,
                MediaCodec.CONFIGURE_FLAG_ENCODE);
        codec.start();
    }
    private void setupMediaCodec(Size size) {

        format = MediaFormat.createVideoFormat("video/avc", size.getWidth(), size.getHeight());
        format.setInteger(MediaFormat.KEY_BIT_RATE, 125000);

        format.setInteger(MediaFormat.KEY_FRAME_RATE, 15);

        format.setInteger(MediaFormat.KEY_COLOR_FORMAT, MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420Flexible);

        format.setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, 5);
        try {

            muxer = new MediaMuxer(path, MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4);

            mExtractor = new MediaExtractor();
            codec = MediaCodec.createEncoderByType("video/avc");

            codec.configure(format, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE);


            codec.start();


        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }


}
