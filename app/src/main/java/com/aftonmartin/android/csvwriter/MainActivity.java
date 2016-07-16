package com.aftonmartin.android.csvwriter;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Environment;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import android.view.View;

import com.microsoft.band.BandClient;
import com.microsoft.band.BandClientManager;
import com.microsoft.band.BandException;
import com.microsoft.band.BandIOException;
import com.microsoft.band.BandInfo;
import com.microsoft.band.ConnectionState;
import com.microsoft.band.sensors.BandAccelerometerEvent;
import com.microsoft.band.sensors.BandAccelerometerEventListener;
import com.microsoft.band.sensors.BandGyroscopeEvent;
import com.microsoft.band.sensors.BandGyroscopeEventListener;
import com.microsoft.band.sensors.SampleRate;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;

public class MainActivity extends Activity {

    private static final String TAG = "MEDIA";
    private static final int MAX_ENTRIES = 1500;
    private static int mEntriesToWrite = MAX_ENTRIES;
    private TextView mResultsView;
    private Button mStartButton;
    private TextView textView3 = null;
    private TextView textView4 = null;
    private PrintWriter mBandAccelWriter = null;
    private PrintWriter mBandGyroWriter = null;
    private File mBandAccelFile = null;
    private File mBandGyroFile = null;
    private BandClient client = null;
    AccelerometerSubscriptionTask aTask = null;



    private BandAccelerometerEventListener mBandAccelerometerEventListener = new BandAccelerometerEventListener() {
        @Override
        public void onBandAccelerometerChanged(final BandAccelerometerEvent event) {
            if (event != null && mBandAccelWriter != null) {
                if (mEntriesToWrite > 0) {
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            mBandAccelWriter.println(String.format(" X = %.3f  Y = %.3f Z = %.3f", event.getAccelerationX(), event.getAccelerationY(), event.getAccelerationZ()));
                            textView3.post(new Runnable() {
                                public void run() {
                                    textView3.setText("x: " + event.getAccelerationX() + "\ny: " + event.getAccelerationY() + "\nz: " + event.getAccelerationZ());
                                }
                            });
                        }
                    }).start();
                    mEntriesToWrite--;
                } else {
                    if (client != null) {
                        try {
                            client.getSensorManager().unregisterAccelerometerEventListener(mBandAccelerometerEventListener);
                            client.getSensorManager().unregisterGyroscopeEventListener(mBandGyroscopeEventListener);
                        } catch (BandIOException e) {
                            appendToUI(e.getMessage());
                        }
                    }
                    closeSDFile();
                    mEntriesToWrite = MAX_ENTRIES;
                }
            }
        }
    };

    private BandGyroscopeEventListener mBandGyroscopeEventListener = new BandGyroscopeEventListener() {

        @Override
        public void onBandGyroscopeChanged(final BandGyroscopeEvent event) {
            if (event != null && mBandGyroWriter != null) {
                if (mEntriesToWrite > 0) {
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            mBandGyroWriter.println(String.format(" X = %.3f  Y = %.3f Z = %.3f", event.getAccelerationX(), event.getAccelerationY(), event.getAccelerationZ()));
                            textView4.post(new Runnable() {
                                public void run() {
                                    textView4.setText("x: " + event.getAccelerationX() + "\ny: " + event.getAccelerationY() + "\nz: " + event.getAccelerationZ());
                                }
                            });
                        }
                    }).start();
                    mEntriesToWrite--;
                } else {
                    if (client != null) {
                        try {
                            client.getSensorManager().unregisterAccelerometerEventListener(mBandAccelerometerEventListener);
                            client.getSensorManager().unregisterGyroscopeEventListener(mBandGyroscopeEventListener);
                        } catch (BandIOException e) {
                            appendToUI(e.getMessage());
                        }
                    }
                    closeSDFile();
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
        textView3 = (TextView) findViewById(R.id.textView3);
        textView4 = (TextView) findViewById(R.id.textView4);
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
                openSDFile();
                //blah blah GO ASYNC!
                if (aTask == null || AsyncTask.Status.FINISHED == aTask.getStatus()){
                    aTask = new AccelerometerSubscriptionTask();
                    aTask.execute();
                }
            }
        });
        mResultsView.setText("");
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (client != null) {
            try {
                client.getSensorManager().unregisterAccelerometerEventListener(mBandAccelerometerEventListener);
                client.getSensorManager().unregisterGyroscopeEventListener(mBandGyroscopeEventListener);
            } catch (BandIOException e) {
                appendToUI(e.getMessage());
            }
        }
        closeSDFile();
    }

    private class AccelerometerSubscriptionTask extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... params) {
            try {
                if (getConnectedBandClient()) {
                    appendToUI("Band is connected.\n");
                    client.getSensorManager().registerAccelerometerEventListener(mBandAccelerometerEventListener, SampleRate.MS16);
                    client.getSensorManager().registerGyroscopeEventListener(mBandGyroscopeEventListener, SampleRate.MS16);
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
        File mediaFile = new File(getExternalCacheDir(), "SensorData");
        mediaFile.mkdirs();
        mBandAccelFile = new File(mediaFile, "band_accel.txt");
        mBandGyroFile = new File(mediaFile, "band_gyro.txt");
        try {
            mBandAccelWriter = new PrintWriter(new BufferedWriter(new FileWriter(mBandAccelFile), 8192));
            mBandGyroWriter = new PrintWriter(new BufferedWriter(new FileWriter(mBandGyroFile), 8192));
            //write header for CSV
            mBandAccelWriter.println("Accelerometer X, AccelerometerY, Accelerometer Z");
            mBandGyroWriter.println("Gyroscope X, Gyroscope Y, Gyroscope Z");

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
            mBandAccelWriter.close();
            mBandGyroWriter.close();
        } catch (Exception e) {

            e.printStackTrace();
        }
        //mResultsView.append("\n\nFile written to " + mBandAccelFile + "\n");
        //mResultsView.append("\n\nFile written to " + mBandGyroFile);
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
