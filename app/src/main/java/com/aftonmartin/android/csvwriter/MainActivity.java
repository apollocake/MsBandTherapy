package com.aftonmartin.android.csvwriter;

import android.app.Activity;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Environment;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;


import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.List;

public class MainActivity extends Activity {

    private static final String TAG = "MEDIA";
    private TextView tv;
    SensorManager mSensorManager = null;
    TextView textView1 = null;
    TextView textView2 = null;
    List accel_list;
    List gyro_list;
    private int max_entries = 1000;
    PrintWriter pWriter = null;
    FileOutputStream mFileOutputStream = null;
    File mFile = null;
    boolean flip = true;

    SensorEventListener mSensorEventListener = new SensorEventListener() {
        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {
        }

        @Override
        public void onSensorChanged(SensorEvent event) {

            if (pWriter != null) {
                float[] values = event.values;
                //textView1.setText("x: " + values[0] + "\ny: " + values[1] + "\nz: " + values[2]);
                if (max_entries > 0 ) {
                    if (flip && event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
                        writeToSDFile("" + values[0] + "," + values[1] + "," + values[2]);
                        flip = false;
                        max_entries--;
                    }else if(!flip && event.sensor.getType() == Sensor.TYPE_GYROSCOPE){
                        writeToSDFile("," + values[0] + "," + values[1] + "," + values[2] + "\n");
                        flip = true;
                        max_entries--;
                    }
                } else {
                    readRaw();
                    unregisterListeners();
                }
            }
        }
    };


    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
                /* Get a SensorManager instance */
        mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);

        textView1 = (TextView) findViewById(R.id.textView1);
        textView2 = (TextView) findViewById(R.id.textView2);

        accel_list = mSensorManager.getSensorList(Sensor.TYPE_ACCELEROMETER);
        gyro_list = mSensorManager.getSensorList(Sensor.TYPE_GYROSCOPE);

        if (accel_list.size() + gyro_list.size() > 1) {
            mSensorManager.registerListener(mSensorEventListener, (Sensor) accel_list.get(0), SensorManager.SENSOR_DELAY_FASTEST);
            mSensorManager.registerListener(mSensorEventListener, (Sensor) gyro_list.get(0), SensorManager.SENSOR_DELAY_FASTEST);

        } else {
            Toast.makeText(getBaseContext(), "Error: No Accelerometer or Gyroscope.", Toast.LENGTH_LONG).show();
        }
        tv = (TextView) findViewById(R.id.text_view);
        checkExternalMedia();
        openSDFile();
    }

    @Override
    protected void onStop() {
        unregisterListeners();
        closeSDFile();
        super.onStop();
    }

    private void unregisterListeners() {
        if (accel_list.size() + gyro_list.size() > 0) {
            mSensorManager.unregisterListener(mSensorEventListener);
        }
    }



    private void checkExternalMedia() {
        boolean mExternalStorageAvailable = false;
        boolean mExternalStorageWriteable = false;
        String state = Environment.getExternalStorageState();

        if (Environment.MEDIA_MOUNTED.equals(state)) {
            // Can read and write the media
            mExternalStorageAvailable = mExternalStorageWriteable = true;
        } else if (Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
            // Can only read the media
            mExternalStorageAvailable = true;
            mExternalStorageWriteable = false;
        } else {
            // Can't read or write
            mExternalStorageAvailable = mExternalStorageWriteable = false;
        }
        tv.append("\n\nExternal Media: readable="
                + mExternalStorageAvailable + " writable=" + mExternalStorageWriteable);
    }



    private void openSDFile() {

        File root = android.os.Environment.getExternalStorageDirectory();
        tv.append("\nExternal file system root: " + root);

        File mediaFile = new File(getExternalCacheDir(), "NewDirectory");
        boolean test = mediaFile.mkdirs();
        mFile = new File(mediaFile, "myData.txt");

        try {
            mFileOutputStream = new FileOutputStream(mFile);
            pWriter = new PrintWriter(mFileOutputStream);
            pWriter.println("Accelerometer X, AccelerometerY, Accelerometer Z, Gyroscope X, Gyroscope Y, Gyroscope Z");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            Log.i(TAG, "******* File not found. Did you" +
                    " add a WRITE_EXTERNAL_STORAGE permission to the   manifest?");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void writeToSDFile(String string) {
        pWriter.print(string);
    }

    private void closeSDFile() {

        try {
            pWriter.flush();
            pWriter.close();
            mFileOutputStream.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            Log.i(TAG, "******* File not found. Did you" +
                    " add a WRITE_EXTERNAL_STORAGE permission to the   manifest?");
        } catch (IOException e) {
            e.printStackTrace();
        }
        tv.append("\n\nFile written to " + mFile);
    }

    private void readRaw() {
        tv.append("\nData read from res/raw/textfile.txt:");
        InputStream is = this.getResources().openRawResource(R.raw.textfile);
        InputStreamReader isr = new InputStreamReader(is);
        BufferedReader br = new BufferedReader(isr, 8192);    // 2nd arg is buffer size

        // More efficient (less readable) implementation of above is the composite expression
    /*BufferedReader br = new BufferedReader(new InputStreamReader(
            this.getResources().openRawResource(R.raw.textfile)), 8192);*/

        try {
            String test;
            while (true) {
                test = br.readLine();
                // readLine() returns null if no more lines in the file
                if (test == null) break;
                tv.append("\n" + "    " + test);
            }
            isr.close();
            is.close();
            br.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        tv.append("\n\nThat is all");
    }
}
