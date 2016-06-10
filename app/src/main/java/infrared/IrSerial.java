package infrared;

import android.content.Context;
import android.hardware.ConsumerIrManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import java.lang.Integer;
import java.lang.System;
import java.util.ArrayList;
import java.util.InputMismatchException;
import java.util.List;

/**
 * Created by monte on 06/06/16.
 */
public class IrSerial {

    //vars
    private int DEBUG = 1;

    private ConsumerIrManager irManager;
    private Context context;
    private int minFreq;
    private int maxFreq;
    private ConsumerIrManager.CarrierFrequencyRange[] irFrequencies;
    private boolean irSupported;
    private List<Integer> listPulses = new ArrayList<>();
    private int freq;
    public final static int DEFAULT_FREQ = 38400;
    public final static int DEFAULT_BAUD = 2400;
    public final static int MAX_BAUD = 4800;
    public final static int RS232_BITS = 8;
    private int baud;

    private boolean correctionEnabled = true;
    public final static int CORRECTION_DELAY = 10000;

    public final static int CORRECTION_TEST_VALUE = 0x99;

    private boolean evenParity = false;
    private boolean oddParity = true;

    public void setEvenParity(boolean evenParity) {
        if (evenParity)
            setOddParity(false);
        this.evenParity = evenParity;
    }

    public void setOddParity(boolean oddParity) {
        if (oddParity)
            setEvenParity(false);
        this.oddParity = oddParity;
    }

    public boolean isEvenParity() {
        return evenParity;
    }
    public boolean isOddParity() {
        return oddParity;
    }

    /**
     * Initialise the IR transmission with default values of frequency of 38400 Hz and
     * baud rate of 2400 bits/s.
     *
     * @param context
     */

    //constructor ------------------------------
    public IrSerial (Context context){
        initialise(context);
        this.freq = DEFAULT_FREQ;
        this.baud = DEFAULT_BAUD;
    }

    /**
     * Initialise the IR transmission and specify your desired frequency and baud rate.
     * @param context
     * @param freq
     * @param baud
     */
    public IrSerial (Context context, int freq, int baud){
        initialise(context);

        if (baud > MAX_BAUD)
            Log.e("Baud Rate Warning", "Baud Rate is too high for Infrared. Was set Anyway.");
        if (freq > maxFreq || freq < minFreq){
            Log.e("Frequency Warning", "Specified frequency is not within the boundary. Was set DEFAULT.");
            this.freq = DEFAULT_FREQ;
        }

        this.freq = freq;
        this.baud = baud;
    }

    //Part of the constructor. This will have to be called for any type of construction as it
    //also initialises the IrManager.
    private void initialise (Context context){
        this.irManager = (ConsumerIrManager) context.getSystemService(AppCompatActivity.CONSUMER_IR_SERVICE);
        this.context = context;

        //usual infrared manager for sending the data
        if (irManager.hasIrEmitter()){
            this.irFrequencies = irManager.getCarrierFrequencies();
            this.minFreq = irFrequencies[0].getMinFrequency();
            this.maxFreq = irFrequencies[0].getMaxFrequency();
            this.irSupported = true;
        } else {
            this.irSupported = false;
        }
    }

    /**
     * Checks what is the Minimum modulation frequency.
     * @return
     */
    public int getMinFreq() {
        return minFreq;
    }

    /**
     * Checks what is the Maximum modulation frequency.
     * @return
     */
    public int getMaxFreq() {
        return maxFreq;
    }

    /**
     * Checks the current modulation frequency.
     * @return
     */
    public int getFreq() {
        return freq;
    }

    /**
     * Checks if IR blaster is supported on your phone.
     * @return
     */
    public boolean isIrSupported() {
        return irSupported;
    }

    /**
     * Checks the current baud rate.
     * @return
     */
    public int getBaud() {
        return baud;
    }

    /**
     * Checks if currently the correction is enabled.
     * @return
     */
    public boolean isCorrectionEnabled() {
        return correctionEnabled;
    }

    /**
     * Sets the new modulation frequency.
     * @param freq
     */
    public void setFreq(int freq) {
        if (freq > maxFreq || freq < minFreq){
            Log.e("Frequency Warning", "Specified frequency is not within the boundary. Was discarded.");
            return;
        }
        this.freq = freq;
    }

    /**
     * sets new baud rate for infrared transmission.
     * @param baud
     */
    public void setBaud(int baud) {
        if (baud > MAX_BAUD)
            Log.e("Baud Rate Warning", "Baud Rate is too high for Infrared. Was set anyway.");
        this.baud = baud;
    }

    /**
     * some phones skip the last pulse for some data inputs (0x99, 0x55), thus they need correction enabled.
     * Correction is done by adding an additional pulse to the end of the pulse list, which will be skipped anyway.
     * @param correctionEnabled
     */
    public void setCorrectionEnabled(boolean correctionEnabled) {
        this.correctionEnabled = correctionEnabled;
    }

    /**
     * Correction test sends single hex value 0x99 (dec 153). If you receive this value on the receiver side,
     * your phone doesn't need a correction enabled, otherwise you would need to enable correction.
     */
    public void performCorrectionTest (){
        send(CORRECTION_TEST_VALUE);
    }

    /**
     * Sets both modulation frequency and baud rate.
     * @param freq
     * @param baud
     */
    public void begin (int freq, int baud){
        if (baud > MAX_BAUD) {
            Log.e("Baud Rate Warning", "Baud Rate is too high for Infrared. Was set anyway.");
        }

        if (freq > maxFreq || freq < minFreq){
            Log.e("Frequency Warning", "Specified frequency is not within the boundary. Was discarded.");
            return;
        }
        this.baud = baud;
        this.freq = freq;
    }

    /**
     * Sends raw data coming from an integer array. An array can be constructed using construct ( data ) method.
     * @param data
     * @return
     */
    public boolean sendRaw (int[] data){
        if (!irSupported)
            return false;
        try {
            irManager.transmit(freq, data);
        } catch (Exception e){
            Log.e("Transmission Error:", "You don't have IR blaster, your baud rate is too small or message too big");
            return false;
        }
        return true;
    }

    /**
     * Sends single integer, which should be withing 0 to 255, otherwise only the lower 8 bits will be sent.
     * @param data
     * @return
     */
    public boolean send (int data){
        Log.e("Value hex: ", "0x" + Integer.toHexString(data));
        Log.e("Sent: ", data + "");
        return sendRaw(construct(data));
    }


    //Working on that
//    public boolean sendHex (String hexData){
//        if (!irSupported)
//            return false;
//
//        sendRaw(construct((int) hexToLong(hexData)));
//        return true;
//    }

    /**
     * Sends single char.
     * @param data
     * @return
     */
    public boolean send (char data){
        return sendRaw(construct(data));
    }

    /**
     * send string data, might be prone to errors when large strings are sent.
     * @param data
     * @return
     */
    public boolean send (String data){
        return sendRaw(construct(data));
    }

    /**
     * sends Strings in slower speeds, but less prone to errors.
     * @param data
     * @return
     */
    public boolean sendSlow (String data) {
        Log.e("Sent: ", data);
        for (int i = 0; i < data.length(); i++) {
            char letter = data.charAt(i);
            Log.e("Sent: ", String.valueOf(letter));
            if (!sendRaw(construct(data.charAt(i)))) {
                return false;
            }
        }

        return true;
    }


    /**
     * Used to construct an integer array of pulse length values.
     * @param data
     * @return
     */
    public int[] construct (String data){
        listPulses.clear();

        for (int i = 0; i < data.length(); i++){
            constructSequence(Long.toBinaryString(data.charAt(i) ^ 0xFF));
        }

        //some phones skip the last pulse for some data types, thus they need correction enabled
        if (correctionEnabled)
            listPulses.add(CORRECTION_DELAY);
        return listToArray(listPulses);
    }

    /**
     * Used to construct an integer array of pulse length values.
     * @param data
     * @return
     */
    public int[] construct (char data){
        listPulses.clear();
        constructSequence(Long.toBinaryString(data ^ 0xFF));
        if (correctionEnabled)
            listPulses.add(CORRECTION_DELAY);
        return listToArray(listPulses);
    }

    /**
     * Used to construct an integer array of pulse length values.
     * @param data
     * @return
     */
    public int[] construct (int data){
        listPulses.clear();
//        long mask= (long) (Math.pow(2, 8) - 1);
//        String binaryData = Long.toBinaryString(data ^ mask);
        String binaryData = Long.toBinaryString(data ^ 0xFF);

        if (data > (long) Math.pow(2, RS232_BITS)-1){
//        if (binaryData.length() > RS232_BITS){
            Log.e("Construct Warning", "Data Not fitting in 8 bits. Sent the lower 8 bits only.");
        }

        constructSequence(binaryData);
        if (correctionEnabled)
            listPulses.add(CORRECTION_DELAY);
        return listToArray(listPulses);
    }

    private void constructSequence (String binaryData){
        int rs232_mark = (int) (Math.pow(10, 6) / baud);

        binaryData = addLeadingZeros(binaryData, RS232_BITS);        //need to add leading 0 to make sure that big enough binary value is used

        if (DEBUG == 1)
            Log.e("binaryData", binaryData);

        StringBuilder tmp = new StringBuilder(binaryData);

        if (evenParity || oddParity) {
            int parityBit = 0;
            for (char t : tmp) {
                parityBit ^= Integer.parseInt(t);
            }
            if (evenParity)
                tmp.append(parityBit & 0x01);
            if (oddParity)
                tmp.append( (~parityBit) & 0x01)
        }

        tmp.reverse();

        if (evenParity || oddParity) {
            int parityBit = 0;
            for (int i = 0; i < tmp.length(); i++){
                parityBit ^= Integer.parseInt(String.valueOf(tmp.charAt(i)));
                if (DEBUG == 1)
                    Log.e("parity bit", ""+parityBit);
            }

            if (evenParity)
                tmp.append(parityBit);
            if (oddParity)
                tmp.append((~parityBit) & 0x01);
        }

        tmp.insert(0, '1');
        tmp.append('0');

        if (DEBUG == 1)
            Log.e("string build", tmp.toString());

        addDataRS232(tmp.toString(), rs232_mark);

        //!!!!!!! MIGHT HAVE TO MOVE THIS SOMEWHERE ELSE
        if (listPulses.size() % 2 != 0)     //mostly used for strings as when one is built, we need to make sure the
            listPulses.add(rs232_mark);     //final pulse goes to LOW

        if (DEBUG == 1)
            Log.e("dataToSend", listPulses.toString());
    }

    private void addDataRS232 (String binary, int mark){
        int pulse = 0;
        char prevChar = '1';
        for (int i = 0; i < binary.length(); i++){
            if (prevChar != binary.charAt(i)){
                listPulses.add(pulse*mark);
                prevChar = binary.charAt(i);
                pulse = 0;
            }
            pulse++;
        }
        if (pulse > 0)
            listPulses.add(pulse * mark);
    }

    private String addLeadingZeros (String binary, int length){
        if (binary.length() < length)
            binary = String.format("%0" + (length-binary.length()) + "d", 0).replace("0", "0") + binary; //add zeroes at the begining
        return binary;
    }

    private int[] listToArray (List<Integer> myData){
        int[] newData = new int[myData.size()];
        int index = 0;
        for (Integer entry: myData){
            newData[index] = entry.intValue();
            index++;
        }
        return newData;
    }
}
