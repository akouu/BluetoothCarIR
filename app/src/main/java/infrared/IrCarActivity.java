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
        irSerial.setCorrectionEnabled(true);

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
    String speed = new String();
    String angle = new String();

    long prevTime = System.currentTimeMillis();

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
                    speed = "f " + (int) positionY;
                }
                if (positionY>0 && positionY<0.5){
                    positionY = (0.5-positionY)*2*255;
                    speed = "b " + (int) positionY;
                }

                if (positionY<=50) {
                    speed = "f 0";
                }

                float positionX = 135 - (event.getX()/WIDTH)*45;
                angle = "" + (int) positionX;

                message = speed + " " + angle;

          //      irSerial.sendSlow(message);

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

        if (System.currentTimeMillis() - prevTime > 300){
            irSerial.send(message);
            prevTime = System.currentTimeMillis();
        }
        return super.onTouchEvent(event);
    }
}
