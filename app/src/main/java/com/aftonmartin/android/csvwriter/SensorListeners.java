package com.aftonmartin.android.csvwriter;

import android.app.Activity;

import com.microsoft.band.BandClient;;
import com.microsoft.band.BandIOException;
import com.microsoft.band.sensors.BandAccelerometerEvent;
import com.microsoft.band.sensors.BandAccelerometerEventListener;
import com.microsoft.band.sensors.BandGyroscopeEvent;
import com.microsoft.band.sensors.BandGyroscopeEventListener;
import com.microsoft.band.sensors.SampleRate;

public class SensorListeners {
    private static final int MAX_ENTRIES = 1000;
    private static int mEntriesToWrite = MAX_ENTRIES;
    private BandClient mBandClient = null;
    private static SensorListeners mSensorListeners = null;
    Activity mCallerActivity = null;


    protected SensorListeners() {
    }

    public synchronized static SensorListeners getInstance() {
        if (mSensorListeners == null) {
            mSensorListeners = new SensorListeners();
        }
        return mSensorListeners;
    }

    public void setCallerActivity(Activity callerActivity) {
        mCallerActivity = callerActivity;
    }

    public BandClient getBandClient() {
        return mBandClient;
    }

    public void setBandClient(BandClient bandClient) {
        mBandClient = bandClient;
    }

    private BandAccelerometerEventListener mBandAccelerometerEventListener = new BandAccelerometerEventListener() {
        @Override
        public void onBandAccelerometerChanged(final BandAccelerometerEvent event) {
            if (event != null && FileUtils.bandAccelWriterExists()) {
                //if (event != null && mBandAccelWriter != null) {

                if (mEntriesToWrite > 0) {
                    FileUtils.getInstance().getBandAccelWriter().println(String.format(" X = %.3f  Y = %.3f Z = %.3f", event.getAccelerationX(), event.getAccelerationY(), event.getAccelerationZ()));
                    UIAsyncUtils.getInstance().appendToUI(R.id.textView3, "x: " + event.getAccelerationX() + "\ny: " + event.getAccelerationY() + "\nz: " + event.getAccelerationZ());

                    mEntriesToWrite--;
                } else {
                    if (mBandClient != null) {
                        try {
                            mBandClient.getSensorManager().unregisterAccelerometerEventListener(mBandAccelerometerEventListener);
                            mBandClient.getSensorManager().unregisterGyroscopeEventListener(mBandGyroscopeEventListener);
                        } catch (BandIOException e) {
                            UIAsyncUtils.getInstance().appendToUI(R.id.results, "x: " + event.getAccelerationX() + "\ny: " + event.getAccelerationY() + "\nz: " + event.getAccelerationZ());
                        }
                    }
                    FileUtils.getInstance().closeSDFile();
                    mEntriesToWrite = MAX_ENTRIES;
                }
            }
        }
    };

    private BandGyroscopeEventListener mBandGyroscopeEventListener = new BandGyroscopeEventListener() {

        @Override
        public void onBandGyroscopeChanged(final BandGyroscopeEvent event) {
            if (event != null && FileUtils.bandGyroWriterExists()) {

                if (mEntriesToWrite > 0) {
                    FileUtils.getInstance().getBandGyroWriter().println(String.format(" X = %.3f  Y = %.3f Z = %.3f", event.getAccelerationX(), event.getAccelerationY(), event.getAccelerationZ()));
                    UIAsyncUtils.getInstance().appendToUI(R.id.textView4, "x: " + event.getAccelerationX() + "\ny: " + event.getAccelerationY() + "\nz: " + event.getAccelerationZ());

                    mEntriesToWrite--;
                } else {
                    if (mBandClient != null) {
                        try {
                            mBandClient.getSensorManager().unregisterAccelerometerEventListener(mBandAccelerometerEventListener);
                            mBandClient.getSensorManager().unregisterGyroscopeEventListener(mBandGyroscopeEventListener);
                        } catch (BandIOException e) {
                            UIAsyncUtils.getInstance().appendToUI(R.id.results, "x: " + event.getAccelerationX() + "\ny: " + event.getAccelerationY() + "\nz: " + event.getAccelerationZ());
                        }
                    }
                    FileUtils.getInstance().closeSDFile();
                    mEntriesToWrite = MAX_ENTRIES;
                }
            }
        }
    };

    public void unregisterListeners() {
        if (mBandClient != null) {
            try {
                mBandClient.getSensorManager().unregisterAccelerometerEventListener(mBandAccelerometerEventListener);
                mBandClient.getSensorManager().unregisterGyroscopeEventListener(mBandGyroscopeEventListener);
            } catch (BandIOException e) {
                UIAsyncUtils.getInstance().appendToUI(R.id.results, e.getMessage());
            }
        }
    }

    public void registerListeners() throws BandIOException {
        try {
            mBandClient.getSensorManager().registerAccelerometerEventListener(mBandAccelerometerEventListener, SampleRate.MS16);
            mBandClient.getSensorManager().registerGyroscopeEventListener(mBandGyroscopeEventListener, SampleRate.MS16);
        } catch (BandIOException e) {
            throw e;
        } catch (Exception e) {
            throw e;
        }
    }
}
