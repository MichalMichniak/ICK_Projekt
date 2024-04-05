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

public class MainActivity extends AppCompatActivity implements SensorEventListener{
    private SensorManager sensorManager;
    private Sensor sensor;
//    private Socket mSocket;

//    {
//        try {
//            mSocket = IO.socket("http://192.168.56.1:5050");
//        } catch (URISyntaxException e) {}
//    }
    private Socket clientSocket;
    private float FIR[];
    private int next_idx = 0;
    @Override
    protected void onCreate(Bundle savedInstanceState)  {
        super.onCreate(savedInstanceState);
//        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
//
//        StrictMode.setThreadPolicy(policy);
        FIR = new float[6];
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
                        clientSocket = new Socket("192.168.0.66", 5050);
                    }//
                    catch (UnknownHostException e) {

                        throw new RuntimeException(e);
                    } catch (IOException e) {

                        throw new RuntimeException(e);
                    }
                    try {
                        OutputStream output = clientSocket.getOutputStream();
                        String inputString = "Hello!!!!!";
                        byte[] data = inputString.getBytes();
                        int len_b = data.length;

                        String len_s = Integer.toString(len_b);
                        while(len_s.length() > 64) len_s = len_s + " ";
                        byte[] header = len_s.getBytes();
                        output.write(header);
                        output.write(data);

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
            FIR[next_idx] = event.values[0];
            next_idx=(next_idx+1)%6;

            float sum = 0;
            for(int i = 0; i<6; i++) {
                sum += FIR[i];
            }
            sum = sum/11;
            if(sum > 0.7) text_lr = "Prawo";
            else if(sum < -0.7) text_lr = "Lewo";
            else text_lr = "___";
            text = "X: " + Float.toString(sum) +
                    ", Y: " + Float.toString(event.values[1]) +
                    ", Z: " + Float.toString(event.values[2]);
            setContentView(R.layout.activity_main);

            ((TextView)findViewById(R.id.textid1)).setText(text);
            ((TextView)findViewById(R.id.lr_text)).setText(text_lr);
        }
    }

}