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
    public void sendResponse(int sendWord) {
        // Toast.makeText(getApplicationContext(), "Goes into the function: " + sendWord, Toast.LENGTH_SHORT).show();
        if (connectThread.getSocket() != null && connectThread.getSocket().isConnected()) { //check if device is still connected
            if (mmManagegedConnection == null) {
                mmManagegedConnection = new ManageConnectedThread(connectThread.getSocket());   //get bluetooth socket
            }
            Log.e("Send word ", "Int: " + sendWord + ", Hex: 0x" + Integer.toHexString(sendWord));
            Toast.makeText(getApplicationContext(), "Connected: " + sendWord, Toast.LENGTH_SHORT).show();
            mmManagegedConnection.write(sendWord); //send the word
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
            // sendResponse(message);
            // sendResponse(angle);
            // Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
        }
    };


    // String message = new String();
    int speed = 0;
    int direction = 0;
    int angle = 0;
    int val=0;

    long prevTime = System.currentTimeMillis();

    //Magic is done in here!
    @Override
    public boolean onTouchEvent(MotionEvent event) {

        switch(event.getAction()){                  //listen for specific touch events
            case MotionEvent.ACTION_DOWN:           //if pressed down or moved, then send adjusted coordinates
                //return true;
            case MotionEvent.ACTION_MOVE:           //in the form of: x0.35 y0.54

                // Set the speed and the direction (forward/backward)
                double positionY = (1-(event.getY()/HEIGHT));
                if (positionY <=1 && positionY >=0.5) {
                    direction = 1;
                    speed = (int) (((positionY)-0.5)*2*255+0.5);
                }
                if (positionY>0 && positionY<0.5){
                    direction = 0;
                    speed = (int) ((0.5-positionY)*2*255+0.5);
                }

                // Set the angle
                angle = (int) (135 - (event.getX()/WIDTH)*45);

                // Log.e("Speed: ", speed + "");
                // Log.e("Angle: ", angle + "");

                // Set direction: 1 forward, 0 backward
                val = 0;
                if (direction==1) {
                    val |= 0x80;
                }

                // Assign 3 bits to set speed
              //  if (speed >= 80) {
            //        speed -= 80;
          //          val |= ((speed/22) << 4);
                    // Log.e("Val (hex): ", "0x" + Integer.toHexString(val));
        //            int speedval= (val & 0x70) >> 4;
      //              int new_speed = speed+80;
                    // Log.e("Speed: ", new_speed + " " + speedval);

    //            }
  //              else {
//                    val =0;

                //}

                // Assign 3 bits to set angle
                val |= (((135-angle)/6) << 4) ;
                // int angleval = (val & 0x70)>>4;
                // int new_angle = angle + 135;
                // Log.e("Angle: ", angle + " " + angleval);

                // Assign 4 bits to set speed
                if (speed >= 60) {
                    speed -= 60;
                    val |= speed / 12;
                    // Log.e("Val (hex): ", "0x" + Integer.toHexString(val));
                    // int speedval = (val & 0x0f);
                    // int new_speed = speed + 60;
                    // Log.e("Speed: ", new_speed + " " + speedval);
                }
                else {
                    val = val & 0xf0;
                }




                // Assign first 4 bits to set angle
                // val |= (135-angle)/3;

                // Log.e("Value int: ", val + "");
                // Log.e("Value hex: ", "0x" + Integer.toHexString(val));

                // To add in the code
                // mHandler.removeCallbacks(mClickRunnable);
                // mHandler.postDelayed(mClickRunnable, 300);

                sendResponse(val);

                break;

            case MotionEvent.ACTION_UP:
                break;
        }

        // Send the message through IR
        // if (System.currentTimeMillis() - prevTime > 50){
            // irSerial.send(val & 0xff);
           // sendResponse(val);
           // prevTime = System.currentTimeMillis();
        // }
        return super.onTouchEvent(event);
    }
}