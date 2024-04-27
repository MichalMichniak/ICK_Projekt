package com.example.test2;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.EditText;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import java.io.IOException;
import java.io.OutputStream;
import java.net.*;
import java.util.concurrent.locks.ReentrantLock;

public class MainActivity extends AppCompatActivity implements SensorEventListener{
    private SensorManager sensorManager;
    private Sensor sensor;
    // mutex na dane
    private ReentrantLock data_lock;
    // najnowsze dane zakcelerometru
    private float[] data_to_send;
    // flaga czy dane się zmieniły
    private boolean new_data_flag;
    private Socket clientSocket;
    @Override
    protected void onCreate(Bundle savedInstanceState)  {
        super.onCreate(savedInstanceState);
        // inicjalizacja zmiennych
        data_to_send = new float[3];
        data_lock = new ReentrantLock();
        new_data_flag = false;
        // nowy wątek socketa inicjalizacja i uruchomienie
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    // inicjalizacja socket-a
                    try {
//                        clientSocket = new Socket("192.168.0.66", 5050);
                        clientSocket = new Socket("192.168.1.102", 5050);                    }//
                    catch (UnknownHostException e) {
                        throw new RuntimeException(e);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                    try {
                        try{
                            // główna pętla wysyłania danych
                            while(true) {
                                float temp[] = new float[3];
                                boolean data_flag = false;
                                // czekanie na mutexa
                                data_lock.lock();
                                if(new_data_flag) {
                                    // przepisanie danych
                                    temp[0] = data_to_send[0];
                                    temp[1] = data_to_send[1];
                                    temp[2] = data_to_send[2];
                                    data_flag = true;
                                    new_data_flag = false;
                                }
                                // odblokowanie mutexa
                                data_lock.unlock();
                                // jeżeli są nowe dane:
                                if(data_flag) {
                                    // inicjalizacja buffora
                                    OutputStream output2 = clientSocket.getOutputStream();
                                    String inputString2 = Float.toString(temp[0]) + "," + Float.toString(temp[1]) + "," + Float.toString(temp[2]);
                                    byte[] data2 = inputString2.getBytes();
                                    // wartość header-a (ilość bajtów do przesłania)
                                    int len_b2 = data2.length;
                                    String len_s2 = Integer.toString(len_b2);
                                    // padowanie header-a do 64 bajtów
                                    while (len_s2.length() > 64) len_s2 = len_s2 + " ";
                                    byte[] header2 = new byte[64];
                                    byte[] new_len = len_s2.getBytes();
                                    // przepisanie stringa do tablicy bajtów
                                    for (int i = 0; i < new_len.length; i++) {
                                        header2[i] = new_len[i];
                                    }
                                    // dopełnienie reszty bajtów spacjami
                                    for (int i = new_len.length; i < 64; i++) {
                                        header2[i] = 32;
                                    }
                                    // zamiana danych na bajty
                                    data2 = inputString2.getBytes();
                                    // wysyłanie headera i danych przez socket do serwera
                                    output2.write(header2);
                                    output2.write(data2);
                                    // resetowanie flagi
                                    data_flag = false;
                                }else{
                                    // opóźnienie 10 ms
                                    Thread.sleep(10);
                                }

                            }
                        }finally {
                            // Procedura rozłączania
                            OutputStream output2 = clientSocket.getOutputStream();
                            String inputString2 = "DISCOqNECT!";
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


        // dodanie sensora akcelerometru do SensorManager-a
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        sensor = sensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);
        setContentView(R.layout.activity_main);
        sensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_NORMAL);

    }
    private EditText mInputMessageView;

    private void attemptSend() {
        String message = mInputMessageView.getText().toString().trim();
        if (TextUtils.isEmpty(message)) {
            return;
        }
        mInputMessageView.setText("");
    }

    public void onAccuracyChanged(Sensor s, int t){

    }
    public void onSensorChanged(SensorEvent event) {
        String text = "";
        if (event.sensor.getType() == Sensor.TYPE_LINEAR_ACCELERATION) {
            // wyświetlanie odczytów akcelerometru na ekranie
            text = "X: " + Float.toString(event.values[0]) +
                    ", Y: " + Float.toString(event.values[1]) +
                    ", Z: " + Float.toString(event.values[2]);
            setContentView(R.layout.activity_main);
            // mutex na dane
            if (data_lock.tryLock()) {
                try {
                    // przepisanie danych
                    data_to_send[0] = event.values[0];
                    data_to_send[1] = event.values[1];
                    data_to_send[2] = event.values[2];
                    new_data_flag = true;
                } finally {
                    data_lock.unlock();
                }
            } else {
                // próbki przepadaja
            }

            ((TextView)findViewById(R.id.textid1)).setText(text);

        }
    }

}