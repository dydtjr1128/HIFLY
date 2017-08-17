package org.androidtown.drone;

/**
 * Created by CYSN on 2017-08-04.
 */

import android.util.Log;

import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;

public class CSMSendSocket {
    Socket sock = null;
    DataOutputStream outputStream = null;
    public CSMSendSocket() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    sock = new Socket("223.194.158.246", 10123);
                    outputStream = new DataOutputStream(sock.getOutputStream());
                    outputStream.writeInt(337733);
                   } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    public void sendSocket(byte[] data) {
        try {
            outputStream.writeInt(data.length);
            outputStream.write(data);
            outputStream.flush();
        } catch (IOException e){
            e.printStackTrace();
        }
    }

    public void endSocket() {
        try {
            outputStream.close();
            sock.close();
        } catch (Exception e) {
            e.getStackTrace();
        }
    }
}
