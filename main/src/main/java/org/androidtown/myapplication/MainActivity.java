package org.androidtown.myapplication;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.SurfaceTexture;
import android.net.Uri;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.TextureView;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;


public class MainActivity extends AppCompatActivity implements TextureView.SurfaceTextureListener,View.OnClickListener {
    CSMMdiaCodec2 codec=null;
    File path;
    File input;
    File testInput;
    FileInputStream is=null;
    byte[] cbuf;
    Surface sf;
    SurfaceTexture st;
    TextureView mTextureView;

    SurfaceView sv;
    SurfaceHolder sh;
    Button btn;
    byte[] rawBuf=new byte[1];
    byte[] isEnd=new byte[10];
    int offset=0;
    Socket sock=null;
    OutputStream out=null;
    BufferedOutputStream bout=null;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)!= PackageManager.PERMISSION_GRANTED&&ActivityCompat.checkSelfPermission(this,Manifest.permission.INTERNET) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA,Manifest.permission.WRITE_EXTERNAL_STORAGE,Manifest.permission.READ_EXTERNAL_STORAGE,Manifest.permission.RECORD_AUDIO,Manifest.permission.INTERNET},1);

            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        else{
            Toast.makeText(this,"permission OK",Toast.LENGTH_LONG).show();
            mTextureView=(TextureView)findViewById(R.id.texture);
            mTextureView.setSurfaceTextureListener(this);
        }

        btn=(Button)findViewById(R.id.button);

        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                // startThread();
                sendServThread();
            }
        });





    }



    public void sendServThread(){


        new Thread(new Runnable() {
            @Override
            public void run() {

                path= Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES);
                // String ph="storage//10FB-3714//Android//data//MUSIC//";
                //input = new File(path, "iframe_1280x720_p4test.h264");
                testInput=new File(path,"testh264stream");
                //int size=(int)testInput.length();
                int size=500000;
                cbuf=new byte[size];
                int count;

                try {
                    sock=new Socket("192.168.1.147",3179);
                    out=sock.getOutputStream();
                    bout=new BufferedOutputStream(out);
                    is=new FileInputStream(testInput);
                    String send="sendClient";

                    out.write(send.getBytes());
                    Log.i("kkk","send");




                } catch (IOException e) {
                    e.printStackTrace();
                }

                try {
                    BufferedInputStream bin=new BufferedInputStream(is);


                    while((count=bin.read(cbuf,0,size))!=-1) {
                        Log.i("kkk",String.valueOf(count));
                        bout.write(cbuf,0,count);


                    }








                } catch (IOException e) {
                    e.printStackTrace();
                }finally{
                    try {
                        bout.close();
                        out.close();
                        sock.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                }
            }
        }).start();
    }
    public void startThread(){

        new Thread(new Runnable() {
            @Override
            public void run() {

                path= Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES);
                // String ph="storage//10FB-3714//Android//data//MUSIC//";
                //input = new File(path, "iframe_1280x720_p4test.h264");
                testInput=new File(path,"testh264stream");
                //int size=(int)testInput.length();
                int size=500000;
                cbuf=new byte[size];
                int count;

                try {

                    is=new FileInputStream(testInput);

                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }

                // Log.i("kkk","length: "+input.getTotalSpace());




                try {
                    BufferedInputStream bin=new BufferedInputStream(is);

                    while((count=bin.read(cbuf,0,size))>=size) {

                        Log.i("kkk", "count:" + cbuf.length + " element:" + Arrays.toString(cbuf));
                        byte[] pattern = {(byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x01, (byte) 0x09};
                        Log.i("kkk", Arrays.toString(pattern));
                        //  byte[] input={(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x01,(byte)0x21,(byte)0x11,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x01,(byte)0x21,(byte)0x31,(byte)0x33,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x01,(byte)0x21,(byte)0x99,(byte)0x22};
                        // Log.i("kkk",Arrays.toString(input));
                        List<byte[]> list = split(pattern, cbuf);
                        Log.i("kkk", String.valueOf(list.size()));
                        for (int i = 1; i < list.size(); i++) {
                            byte[] data = new byte[pattern.length + list.get(i).length];
                            System.arraycopy(pattern, 0, data, 0, pattern.length);
                            System.arraycopy(list.get(i), 0, data, pattern.length, list.get(i).length);
                            Log.i("kkk", "size:" + data.length);
                            codec.InputYUVData(data);


                        }
                    }






                } catch (IOException e) {
                    e.printStackTrace();
                }
                codec.endCodec();
                //codec.outputData();




            }
        }).start();




    }

    public static boolean isMatch(byte[] pattern, byte[] input, int pos) {
        for(int i=0; i< pattern.length; i++) {
            if(pattern[i] != input[pos+i]) {
                return false;
            }
        }
        return true;
    }

    public static List<byte[]> split(byte[] pattern, byte[] input) {
        List<byte[]> l = new LinkedList<byte[]>();
        int blockStart = 0;
        for(int i=0; i<input.length; i++) {
            if(isMatch(pattern,input,i)) {
                l.add(Arrays.copyOfRange(input, blockStart, i));
                blockStart = i+pattern.length;
                i = blockStart;
            }
        }
        l.add(Arrays.copyOfRange(input, blockStart, input.length ));
        return l;
    }
    @Override
    public void onSurfaceTextureAvailable(SurfaceTexture surfaceTexture, int i, int i1) {

        st=mTextureView.getSurfaceTexture();

        sv=(SurfaceView)findViewById(R.id.preview);
        sh=sv.getHolder();
        sf=sh.getSurface();
        codec=new CSMMdiaCodec2(sf);


        Log.i("kkk","serfaceAvailable"+sv.getWidth()+","+sv.getHeight());

    }

    @Override
    public void onSurfaceTextureSizeChanged(SurfaceTexture surfaceTexture, int i, int i1) {

    }

    @Override
    public boolean onSurfaceTextureDestroyed(SurfaceTexture surfaceTexture) {
        return false;
    }

    @Override
    public void onSurfaceTextureUpdated(SurfaceTexture surfaceTexture) {
        Log.i("kkk","update");
    }

    @Override
    public void onClick(View view) {

    }
}
