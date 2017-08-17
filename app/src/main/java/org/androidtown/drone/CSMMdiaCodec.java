package org.androidtown.drone;

import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaExtractor;
import android.media.MediaFormat;
import android.media.MediaMuxer;
import android.os.Environment;
import android.util.Log;
import android.view.Surface;

import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * Created by CYSN on 2017-08-03.
 */


public class CSMMdiaCodec {

    private MediaFormat format;
    private MediaCodec codec;
    private MediaMuxer muxer;
    private MediaExtractor mExtractor;
    private final int timeoutUs = 10000;
    private final String path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES) + "/temp.mp4";
    private boolean finished;
    private ByteBuffer outputBuffer;
    private MediaCodec.BufferInfo bufferInfo;
    private int videoTrackIndex;
    public static final String VIDEO_ENCODING_FORMAT = "video/avc";
    private ByteBuffer[] inputBuffers;
    private ByteBuffer[] outputBuffers;
    private Surface surface;

    public CSMMdiaCodec(Surface surface) {
        this.surface=surface;
        setupMediaCodec();

        // initCodec();
    }

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


    public void InputYUVData(byte[] bytes) throws IOException {
        int inputBufferIndex=-2;
        try {
            inputBufferIndex = codec.dequeueInputBuffer(10000);
            // Log.i("kkk","index: "+inputBufferIndex);
            Log.i("kkk","in index: "+inputBufferIndex);
        } catch (Exception e) {
            Log.i("kkk", "decodeFrame: dequeue input: " + e);
            codec.stop();
            codec.reset();
            setupMediaCodec();
            e.printStackTrace();
        }
        if (inputBufferIndex >= 0) {

            //ByteBuffer inputBuffer = codec.getInputBuffer(inputBufferIndex);
            //inputBuffer.put(bytes,0,bytes.length);
            //codec.queueInputBuffer(inputBufferIndex, 0, bytes.length, timeoutUs, 0);


            //ByteBuffer buffer = inputBuffers[inputBufferIndex];
            ByteBuffer buffer=codec.getInputBuffer(inputBufferIndex);
           /* buffer.clear();
            buffer.rewind();*/
            buffer.put(bytes);
            Log.i("kkk", "queueintputbuffer");
            codec.queueInputBuffer(inputBufferIndex,0,bytes.length,timeoutUs, 0);
            Log.i("kkk", "queueintputbuffer done");
            buffer.clear();

            //outputData();
            int outputBufferIndex = -2;

            outputBufferIndex = codec.dequeueOutputBuffer(bufferInfo,timeoutUs);
            //  codec.getOutputImage(outIndex);
            Log.i("kkk", "outindex: "+outputBufferIndex);
            // Log.i("kkk","outIndex: "+outIndex);

            if (outputBufferIndex >= 0) { // 0 이상일 경우에 인코딩/디코딩 데이터가 출력됩니다.
                outputBuffer = codec.getOutputBuffer(outputBufferIndex);

                codec.releaseOutputBuffer(outputBufferIndex, true);

                Log.i("kkk", "muxer start " + String.valueOf(outputBufferIndex));
                //muxer.writeSampleData(videoTrackIndex, outputBuffer, bufferInfo);
                Log.i("kkk", "ok data");


            } else if (outputBufferIndex == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED) {

                //MediaFormat newFormat = codec.getOutputFormat();


                //videoTrackIndex=muxer.addTrack(newFormat);
                //muxer.start();



                Log.i("kkk","videoTrackIndex change: "+String.valueOf(videoTrackIndex));

            }





            /////output

            /*int outputBufferIndex = codec.dequeueOutputBuffer(bufferInfo, timeoutUs);
            Log.i("kkk","out index: "+inputBufferIndex);
            if ( outputBufferIndex >= 0 )
            {

                codec.releaseOutputBuffer(outputBufferIndex,true);

            }
            else if ( outputBufferIndex == MediaCodec.INFO_OUTPUT_BUFFERS_CHANGED )
            {
                //outputBuffers = codec.getOutputBuffers();
            }
            else if ( outputBufferIndex == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED )
            {
                MediaFormat format = codec.getOutputFormat();
            }
*/

        }
    }
    public boolean outputData() {


        int outputBufferIndex = codec.dequeueOutputBuffer(bufferInfo, timeoutUs);

        if (outputBufferIndex >= 0) { // 0 이상일 경우에 인코딩/디코딩 데이터가 출력됩니다.
            outputBuffer = codec.getOutputBuffer(outputBufferIndex);

            codec.releaseOutputBuffer(outputBufferIndex, true);

            Log.i("kkk", "muxer start " + String.valueOf(outputBufferIndex));
            //muxer.writeSampleData(videoTrackIndex, outputBuffer, bufferInfo);
            Log.i("kkk", "ok data");

            return true;
        } else if (outputBufferIndex == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED) {

            //MediaFormat newFormat = codec.getOutputFormat();


            //videoTrackIndex=muxer.addTrack(newFormat);
            //muxer.start();



            Log.i("kkk","videoTrackIndex change: "+String.valueOf(videoTrackIndex));

        }
        Log.i("kkk", "no data");
        return false;
    }

    private void setupMediaCodec() {


        format = MediaFormat.createVideoFormat(VIDEO_ENCODING_FORMAT, 1280, 720);
        format.setInteger(MediaFormat.KEY_COLOR_FORMAT, MediaCodecInfo.CodecCapabilities.COLOR_FormatSurface);
        format.setInteger(MediaFormat.KEY_MAX_INPUT_SIZE, 1280*720);

        /*format.setInteger(MediaFormat.KEY_FRAME_RATE, 30);
        format.setInteger(MediaFormat.KEY_BIT_RATE, 1280*720*30/100);

        format.setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, 1);*/
        //format.setInteger(MediaFormat.KEY_BIT_RATE, 125000);
        //format.setInteger(MediaFormat.KEY_MAX_INPUT_SIZE,2000000);
        //format.setInteger(MediaFormat.KEY_FRAME_RATE, 15);

        //format.setInteger(MediaFormat.KEY_COLOR_FORMAT, MediaCodecInfo.CodecCapabilities.COLOR_FormatSurface);

        //format.setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, 5);
        try {



            //mExtractor = new MediaExtractor();
            //codec = MediaCodec.createDecoderByType("video/avc");
            // codec=MediaCodec.createDecoderByType(VIDEO_ENCODING_FORMAT);
            codec= MediaCodec.createByCodecName("OMX.google.h264.decoder");
            // codec.configure(format, surface, null, MediaCodec.CONFIGURE_FLAG_ENCODE);
            codec.configure(format, this.surface, null,0);

            bufferInfo = new MediaCodec.BufferInfo();

            // muxer = new MediaMuxer(path, MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4);
            codec.start();
            Log.i("kkk","codec start");





        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }


    }
}