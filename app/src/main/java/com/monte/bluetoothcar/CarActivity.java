package com.monte.bluetoothcar;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.os.Handler;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MotionEvent;
import android.widget.Toast;

import java.util.UUID;

import bluetoothStuff.*;


/**
 * Created by Monte on 24/02/16.
 */
public class CarActivity extends Activity {

    public BluetoothDevice device;                          //device to be connected to
    public ConnectThread connectThread;                     //connection thread
    private ManageConnectedThread mmManagegedConnection;    //send & receive thread

    private int WIDTH;                                      //screen width for the aplication
    private int HEIGHT;                                     //same for height

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_car);              //set layout
        this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT); //always keep orientation in portrait
        getScreenSize();                                    //gets the WIDTH and HEIGHT
        initialiseBluetooth();                              //Bluetooth stuff
    }
//Initialise bluetooth stuff
    private void initialiseBluetooth() {
        Intent intent = getIntent();
        String deviceAddress = intent.getStringExtra(BluetoothActivity.DEVICE_ADDRESS); //gets passed device MAC address

        device = BluetoothAdapter.getDefaultAdapter().getRemoteDevice(deviceAddress);
        Log.e("Device Address is", deviceAddress);
    }

    //Need to start connection thread again when awake
    @Override
    protected void onResume() {
        super.onResume();

        UUID deviceUUID = device.getUuids()[0].getUuid();
        Log.e("UUID", deviceUUID + "");
        connectThread = new ConnectThread(device, deviceUUID);
        connectThread.start();
    }

    //stop connection thread to save battery when application closed
    @Override
    protected void onStop() {
        super.onStop();
        mmManagegedConnection.cancel();
        connectThread.cancel();
        try {
            connectThread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    //sends response through bluetooth, firstly gets a socket
    public void sendResponse(String sendWord) {
        // Toast.makeText(getApplicationContext(), "Goes into the function: " + sendWord, Toast.LENGTH_SHORT).show();
        if (connectThread.getSocket() != null && connectThread.getSocket().isConnected()) { //check if device is still connected
            if (mmManagegedConnection == null) {
                mmManagegedConnection = new ManageConnectedThread(connectThread.getSocket());   //get bluetooth socket
            }
            Log.e("Send word: ", sendWord);
            Toast.makeText(getApplicationContext(), "Connected: " + sendWord, Toast.LENGTH_SHORT).show();
            mmManagegedConnection.write(sendWord);                                          //send the word
        }
        //mmManagegedConnection.write(string.getBytes(Charset.forName("UTF-8")));
    }

    //gets screen size
    private void getScreenSize (){
        DisplayMetrics metrics = this.getResources().getDisplayMetrics();
        WIDTH = metrics.widthPixels;
        HEIGHT = metrics.heightPixels;
        Log.e("WIDTH=", WIDTH + "");
        Log.e("HEIGHT=", HEIGHT + "");
    }

    /** Used to delay information to be sent to the Arduino*/
    private Handler mHandler = new Handler();
    private Runnable mClickRunnable = new Runnable() {
        @Override
        public void run() {
            sendResponse(message);
            // sendResponse(angle);
            // Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
        }
    };

    String message = new String();
    String speed = new String();
    String angle = new String();

    //Magic is done in here!
    @Override
    public boolean onTouchEvent(MotionEvent event) {

        switch(event.getAction()){                  //listen for specific touch events
            case MotionEvent.ACTION_DOWN:           //if pressed down or moved, then send adjusted coordinates
                //return true;
            case MotionEvent.ACTION_MOVE:           //in the form of: x0.35 y0.54

                double positionY = (1-(event.getY()/HEIGHT));
                        if (positionY <=1 && positionY >=0.5) {
                            positionY = ((positionY)-0.5)*2*255;

                            speed = "f" + (int) positionY;
                            }
                        if (positionY>0 && positionY<0.5){
                            positionY = (0.5-positionY)*2*255;
                            speed = "b" + (int) positionY;
                        }

                if (positionY<=50) {
                    speed = "f0";
                }

                float positionX = 135 - (event.getX()/WIDTH)*45;

                angle = "" + (int) positionX;

                message = speed + " " + angle;
                mHandler.removeCallbacks(mClickRunnable);
                mHandler.postDelayed(mClickRunnable, 300);
                Log.e("Loop: ", message);
                break;

            case MotionEvent.ACTION_UP:
                // message = "x0.00y0.00\n";
//                message = "x0.00";
                // Log.e(":)", message);               //output this on Logcat
//                sendResponse(message);              //send through bluetooth
               // return false;
//                message = "y0.00";
//                sendResponse(message);
                break;
        }
        return super.onTouchEvent(event);
    }
}
