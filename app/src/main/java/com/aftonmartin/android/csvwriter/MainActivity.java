package com.aftonmartin.android.csvwriter;

import android.app.Activity;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.AsyncTask;
import android.os.Environment;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import android.view.View;

import com.microsoft.band.BandClient;
import com.microsoft.band.BandClientManager;
import com.microsoft.band.BandException;
import com.microsoft.band.BandIOException;
import com.microsoft.band.BandInfo;
import com.microsoft.band.ConnectionState;
import com.microsoft.band.sensors.BandAccelerometerEvent;
import com.microsoft.band.sensors.BandAccelerometerEventListener;
import com.microsoft.band.sensors.SampleRate;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.List;

public class MainActivity extends Activity {

    private static final String TAG = "MEDIA";
    private static final int MAX_ENTRIES = 1500;
    private static int mEntriesToWrite = MAX_ENTRIES;
    private TextView mResultsView;
    private Button mStartButton;
    private SensorManager mSensorManager = null;
    private TextView textView1 = null;
    private TextView textView2 = null;
    private List mAccelList;
    private List mGyroList;
    private PrintWriter mAccelWriter = null;
    private PrintWriter mGyroWriter = null;
    private FileOutputStream mFileOutputStream = null;
    private File mAccelFile = null;
    private File mGyroFile = null;
    private BandClient client = null;

    float[] mSensorValues = null;


    private BandAccelerometerEventListener mAccelerometerEventListener = new BandAccelerometerEventListener() {
        @Override
        public void onBandAccelerometerChanged(final BandAccelerometerEvent event) {
            if (event != null) {
                appendToUI(String.format(" X = %.3f \n Y = %.3f\n Z = %.3f", event.getAccelerationX(),
                        event.getAccelerationY(), event.getAccelerationZ()));
            }
        }
    };

    SensorEventListener mSensorEventListenerAccel = new SensorEventListener() {
        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {
        }

        @Override
        public void onSensorChanged(SensorEvent event) {

            if (mAccelWriter != null) {
                mSensorValues = event.values;
                if (mEntriesToWrite > 0) {
                    new Thread(new Runnable() {
                        public void run() {
                            mAccelWriter.println("" + mSensorValues[0] + "," + mSensorValues[1] + "," + mSensorValues[2]);
                            textView1.post(new Runnable() {
                                public void run() {
                                    textView1.setText("x: " + mSensorValues[0] + "\ny: " + mSensorValues[1] + "\nz: " + mSensorValues[2]);
                                }
                            });
                        }
                    }).start();
                    mEntriesToWrite--;

                } else {
                    //read from text file
                    readRaw();
                    //
                    unregisterListeners();
                    closeSDFile();
                    //reset values for next reading
                    mEntriesToWrite = MAX_ENTRIES;
                }
            }
        }
    };

    SensorEventListener mSensorEventListenerGyro = new SensorEventListener() {
        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {
        }

        @Override
        public void onSensorChanged(SensorEvent event) {

            if (mAccelWriter != null) {
                mSensorValues = event.values;
                if (mEntriesToWrite > 0) {
                    new Thread(new Runnable() {
                        public void run() {
                            mGyroWriter.println("" + mSensorValues[0] + "," + mSensorValues[1] + "," + mSensorValues[2]);
                            textView2.post(new Runnable() {
                                public void run() {
                                    textView2.setText("x: " + mSensorValues[0] + "\ny: " + mSensorValues[1] + "\nz: " + mSensorValues[2]);
                                }
                            });
                        }
                    }).start();
                    mEntriesToWrite--;
                } else {
                    //read from text file
                    readRaw();
                    //
                    unregisterListeners();
                    closeSDFile();
                    //reset values for next reading
                    mEntriesToWrite = MAX_ENTRIES;
                }
            }
        }
    };


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //wire up view
        textView1 = (TextView) findViewById(R.id.textView1);
        textView2 = (TextView) findViewById(R.id.textView2);
        mResultsView = (TextView) findViewById(R.id.results);
        mStartButton = (Button) findViewById(R.id.start);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mStartButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                checkPermissionsExplicit();
                //blah blah GO ASYNC!
                openSDFile();
                registerListeners();
                new AccelerometerSubscriptionTask().execute();
            }
        });
        mResultsView.setText("");
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (client != null) {
            try {
                client.getSensorManager().unregisterAccelerometerEventListener(mAccelerometerEventListener);
            } catch (BandIOException e) {
                appendToUI(e.getMessage());
            }
        }
        unregisterListeners();
        closeSDFile();
    }

    private class AccelerometerSubscriptionTask extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... params) {
            try {
                if (getConnectedBandClient()) {
                    appendToUI("Band is connected.\n");
                    client.getSensorManager().registerAccelerometerEventListener(mAccelerometerEventListener, SampleRate.MS128);
                } else {
                    appendToUI("Band isn't connected. Please make sure bluetooth is on and the band is in range.\n");
                }
            } catch (BandException e) {
                String exceptionMessage = "";
                switch (e.getErrorType()) {
                    case UNSUPPORTED_SDK_VERSION_ERROR:
                        exceptionMessage = "Microsoft Health BandService doesn't support your SDK Version. Please update to latest SDK.\n";
                        break;
                    case SERVICE_ERROR:
                        exceptionMessage = "Microsoft Health BandService is not available. Please make sure Microsoft Health is installed and that you have the correct permissions.\n";
                        break;
                    default:
                        exceptionMessage = "Unknown error occured: " + e.getMessage() + "\n";
                        break;
                }
                appendToUI(exceptionMessage);

            } catch (Exception e) {
                appendToUI(e.getMessage());
            }
            return null;
        }
    }

    @Override
    protected void onDestroy() {
        if (client != null) {
            try {
                client.disconnect().await();
            } catch (InterruptedException e) {
                // Do nothing as this is happening during destroy
            } catch (BandException e) {
                // Do nothing as this is happening during destroy
            }
        }
        super.onDestroy();
    }


    private void registerListeners() {
        mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        mAccelList = mSensorManager.getSensorList(Sensor.TYPE_ACCELEROMETER);
        mGyroList = mSensorManager.getSensorList(Sensor.TYPE_GYROSCOPE);
        if (mAccelList.size() + mGyroList.size() > 1) {
            mSensorManager.registerListener(mSensorEventListenerAccel, (Sensor) mAccelList.get(0), SensorManager.SENSOR_DELAY_FASTEST);
            mSensorManager.registerListener(mSensorEventListenerGyro, (Sensor) mGyroList.get(0), SensorManager.SENSOR_DELAY_FASTEST);

        } else {
            Toast.makeText(getBaseContext(), "Error: No Accelerometer or Gyroscope.", Toast.LENGTH_LONG).show();
        }
    }


    private void unregisterListeners() {
        try {
            if (mAccelList.size() + mGyroList.size() > 0) {
                mSensorManager.unregisterListener(mSensorEventListenerAccel);
                mSensorManager.unregisterListener(mSensorEventListenerGyro);
            }
        } catch (NullPointerException e) { //quickly exiting app after start causes mSensorManager to remain null
            e.printStackTrace();
            Log.i(TAG, "Listeners unregistered before registering");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void checkPermissionsExplicit() {
        boolean mExternalStorageAvailable = false;
        boolean mExternalStorageWriteable = false;
        //if phone has no external SD, will still emulate SD card
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
        mResultsView.append("\n\nExternal Media: readable="
                + mExternalStorageAvailable + " writable=" + mExternalStorageWriteable);
    }


    private void openSDFile() {
        //get base URI
        File root = android.os.Environment.getExternalStorageDirectory();
        mResultsView.append("\nExternal file system root: " + root);


        //set file up for writing
        File mediaFile = new File(getExternalCacheDir(), "NewDirectory");
        mediaFile.mkdirs();
        mAccelFile = new File(mediaFile, "myData1.txt");
        mGyroFile = new File(mediaFile, "myData2.txt");
        try {
            mFileOutputStream = new FileOutputStream(mAccelFile);
            mFileOutputStream = new FileOutputStream(mGyroFile);
            mAccelWriter = new PrintWriter(new BufferedWriter(new FileWriter(mAccelFile), 8192));
            mGyroWriter = new PrintWriter(new BufferedWriter(new FileWriter(mGyroFile), 8192));
            //write header for CSV
            mAccelWriter.println("Accelerometer X, AccelerometerY, Accelerometer Z");
            mGyroWriter.println("Gyroscope X, Gyroscope Y, Gyroscope Z");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            Log.i(TAG, "******* File not found. Did you" +
                    " add a WRITE_EXTERNAL_STORAGE permission to the   manifest?");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    private void closeSDFile() {

        try {
            mAccelWriter.flush();
            mAccelWriter.close();
            mFileOutputStream.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            Log.i(TAG, "******* File not found. Did you" +
                    " add a WRITE_EXTERNAL_STORAGE permission to the   manifest?");
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
        mResultsView.append("\n\nFile written to " + mAccelFile + "\n");
        mResultsView.append("\n\nFile written to " + mGyroFile);
    }

    private void readRaw() {
        mResultsView.append("\nData read from res/raw/textfile.txt:");
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
                mResultsView.append("\n" + "    " + test);
            }
            isr.close();
            is.close();
            br.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        mResultsView.append("\n\nThat is all");
    }

    private void appendToUI(final String string) {
        this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mResultsView.setText(string);
            }
        });
    }

    private boolean getConnectedBandClient() throws InterruptedException, BandException {
        if (client == null) {
            BandInfo[] devices = BandClientManager.getInstance().getPairedBands();
            if (devices.length == 0) {
                appendToUI("Band isn't paired with your phone.\n");
                return false;
            }
            client = BandClientManager.getInstance().create(getBaseContext(), devices[0]);
        } else if (ConnectionState.CONNECTED == client.getConnectionState()) {
            return true;
        }

        appendToUI("Band is connecting...\n");
        return ConnectionState.CONNECTED == client.connect().await();
    }
}
