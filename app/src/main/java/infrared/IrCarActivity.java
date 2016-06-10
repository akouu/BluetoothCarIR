package infrared;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.os.SystemClock;
import android.provider.Settings;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MotionEvent;
import android.widget.Toast;

import com.monte.bluetoothcar.R;



/**
 * Created by Monte on 24/02/16.
 */
public class IrCarActivity extends Activity {

    private int WIDTH;                                      //screen width for the aplication
    private int HEIGHT;                                     //same for height
    private IrSerial irSerial;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_car);              //set layout
        this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT); //always keep orientation in portrait
        getScreenSize(); //gets the WIDTH and HEIGHT
        irSerial = new IrSerial(this, IrSerial.DEFAULT_FREQ, IrSerial.DEFAULT_BAUD);
        // irSerial.setCorrectionEnabled(false);

    }
    //Initialise bluetooth stuff

    //Need to start connection thread again when awake
    @Override
    protected void onResume() {
        super.onResume();
    }

    //stop connection thread to save battery when application closed
    @Override
    protected void onStop() {
        super.onStop();
            }

    //gets screen size
    private void getScreenSize (){
        DisplayMetrics metrics = this.getResources().getDisplayMetrics();
        WIDTH = metrics.widthPixels;
        HEIGHT = metrics.heightPixels;
        Log.e("WIDTH=", WIDTH + "");
        Log.e("HEIGHT=", HEIGHT + "");
    }

    String message = new String();
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
                if (speed >= 80) {
                    val |= ((speed/22) << 4);
                }
                else {
                    val =0;

                }


                // Assign first 4 bits to set angle
                val |= (135-angle)/3;

                // Log.e("Value int: ", val + "");
                // Log.e("Value hex: ", "0x" + Integer.toHexString(val));
                break;

            case MotionEvent.ACTION_UP:
                break;
        }

        // Send the message through IR
        if (System.currentTimeMillis() - prevTime > 300){
            irSerial.send(val & 0xff);
            prevTime = System.currentTimeMillis();
        }
        return super.onTouchEvent(event);
    }
}
