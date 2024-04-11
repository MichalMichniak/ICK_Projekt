package com.example.test2;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.StrictMode;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.*;
import io.socket.emitter.Emitter;
//import io.socket.client.Socket;
import io.socket.client.IO;
import java.net.URISyntaxException;
import java.lang.Math;
import java.util.concurrent.locks.ReentrantLock;

public class MainActivity extends AppCompatActivity implements SensorEventListener{
    private SensorManager sensorManager;
    private Sensor sensor;

    private ReentrantLock data_lock;

    private float[] data_to_send;

    private boolean new_data_flag;
//    private Socket mSocket;

//    {
//        try {
//            mSocket = IO.socket("http://192.168.56.1:5050");
//        } catch (URISyntaxException e) {}
//    }
    private Socket clientSocket;
    private float FIR_x[];
    private float FIR_y[];
    private float FIR_z[];
    private int next_idx = 0;
    @Override
    protected void onCreate(Bundle savedInstanceState)  {
        super.onCreate(savedInstanceState);
//        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
//
//        StrictMode.setThreadPolicy(policy);
        FIR_x = new float[6];
        FIR_y = new float[6];
        FIR_z = new float[6];
        data_to_send = new float[3];
        data_lock = new ReentrantLock();
        new_data_flag = false;
//        try {
//            InetAddress addr = InetAddress.getByName("localhost");
//            clientSocket = new Socket("192.168.56.1", 5050);
//        } catch (IOException e) {
//            throw new RuntimeException(e);
//        }
        // sockets
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    try {
//                        clientSocket = new Socket("192.168.0.66", 5050);
                        clientSocket = new Socket("172.20.10.7", 5050);                    }//
                    catch (UnknownHostException e) {

                        throw new RuntimeException(e);
                    } catch (IOException e) {

                        throw new RuntimeException(e);
                    }
                    try {
//                        OutputStream output = clientSocket.getOutputStream();
//                        String inputString = "Hello!!!!!";
//                        byte[] data = inputString.getBytes();
//                        int len_b = data.length;
//
//                        String len_s = Integer.toString(len_b);
//                        while(len_s.length() > 64) len_s = len_s + " ";
//                        byte[] header = len_s.getBytes();
//                        output.write(header);
//                        output.write(data);
                        try{
                            while(true) {
                                float temp[] = new float[3];
                                boolean data_flag = false;
                                data_lock.lock();
                                if(new_data_flag) {
                                    temp[0] = data_to_send[0];
                                    temp[1] = data_to_send[1];
                                    temp[2] = data_to_send[2];
                                    data_flag = true;
                                    new_data_flag = false;
                                }
                                data_lock.unlock();
                                if(data_flag) {
                                    OutputStream output2 = clientSocket.getOutputStream();
                                    String inputString2 = Float.toString(temp[0]) + "," + Float.toString(temp[1]) + "," + Float.toString(temp[2]);
                                    byte[] data2 = inputString2.getBytes();
                                    int len_b2 = data2.length;

                                    String len_s2 = Integer.toString(len_b2);
                                    while (len_s2.length() > 64) len_s2 = len_s2 + " ";
                                    byte[] header2 = new byte[64];
                                    byte[] new_len = len_s2.getBytes();
                                    for (int i = 0; i < new_len.length; i++) {
                                        header2[i] = new_len[i];
                                    }
                                    for (int i = new_len.length; i < 64; i++) {
                                        header2[i] = 32;
                                    }
                                    data2 = inputString2.getBytes();

                                    output2.write(header2);
                                    output2.write(data2);
                                    data_flag = false;
                                }else{
                                    Thread.sleep(10);
                                }

                            }
                        }finally {
                            // Disconect
                            OutputStream output2 = clientSocket.getOutputStream();
                            String inputString2 = "DISCONECT!";
                            byte[] data2 = inputString2.getBytes();
                            int len_b2 = data2.length;

                            String len_s2 = Integer.toString(len_b2);
                            while(len_s2.length() > 64) len_s2 = len_s2 + " ";
                            byte[] header2 = new byte[64];
                            byte[] new_len = len_s2.getBytes();
                            for(int i = 0; i<new_len.length ; i++){
                                header2[i] = new_len[i];
                            }
                            for(int i = new_len.length; i< 64; i++) {
                                header2[i] = 32;
                            }
                            data2 = inputString2.getBytes();

                            output2.write(header2);
                            output2.write(data2);
                            clientSocket.close();
                        }






                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }

                }
                catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        }).start();



        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        sensor = sensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);
        final int[] temp = {0};
        setContentView(R.layout.activity_main);
        ((TextView)findViewById(R.id.textid1)).setText("test");

        sensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_NORMAL);

        ((Button)findViewById(R.id.button_test)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                temp[0]++;
                ((TextView)findViewById(R.id.textid1)).setText("test"+ temp[0]);
            }
        });
//        mSocket.connect();


    }
    private EditText mInputMessageView;

    private void attemptSend() {
        String message = mInputMessageView.getText().toString().trim();
        if (TextUtils.isEmpty(message)) {
            return;
        }

        mInputMessageView.setText("");
//        mSocket.emit("new message", message);
    }

    public void onAccuracyChanged(Sensor s, int t){

    }
    public void onSensorChanged(SensorEvent event) {
        String text = "";

        if (event.sensor.getType() == Sensor.TYPE_LINEAR_ACCELERATION) {
            String text_lr = "";
//            FIR_x[next_idx] = event.values[0];
//            FIR_y[next_idx] = event.values[1];
//            FIR_z[next_idx] = event.values[2];
//            next_idx=(next_idx+1)%6;
//
//            float sum_x = 0;
//            float sum_y = 0;
//            float sum_z = 0;
//            for(int i = 0; i<6; i++) {
//                sum_x += FIR_x[i];
//                sum_y += FIR_y[i];
//                sum_z += FIR_z[i];
//            }
//            sum_x = sum_x/6;
//            sum_y = sum_y/6;
//            sum_z = sum_z/6;
//            if(sum > 0.7) text_lr = "Prawo";
//            else if(sum < -0.7) text_lr = "Lewo";
//            else text_lr = "___";
            text = "X: " + Float.toString(event.values[0]) +
                    ", Y: " + Float.toString(event.values[1]) +
                    ", Z: " + Float.toString(event.values[2]);
            setContentView(R.layout.activity_main);

            if (data_lock.tryLock()) {
                try {
                    data_to_send[0] = event.values[0];
                    data_to_send[1] = event.values[1];
                    data_to_send[2] = event.values[2];
                    new_data_flag = true;
//                    ((TextView)findViewById(R.id.lr_text)).setText(Float.toString(0.0));
                } finally {
                    data_lock.unlock();
                }
            } else {
                // próbki przepadaja
                // perform alternative actions
            }

            ((TextView)findViewById(R.id.textid1)).setText(text);
            //((TextView)findViewById(R.id.lr_text)).setText(text_lr);
            

        }
    }

}