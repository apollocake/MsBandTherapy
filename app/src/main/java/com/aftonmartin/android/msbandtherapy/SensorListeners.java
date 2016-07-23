package com.aftonmartin.android.msbandtherapy;

import android.app.Activity;

import com.microsoft.band.BandClient;;
import com.microsoft.band.BandIOException;
import com.microsoft.band.sensors.BandAccelerometerEvent;
import com.microsoft.band.sensors.BandAccelerometerEventListener;
import com.microsoft.band.sensors.BandGyroscopeEvent;
import com.microsoft.band.sensors.BandGyroscopeEventListener;
import com.microsoft.band.sensors.SampleRate;

public class SensorListeners {
    private static final int MAX_ENTRIES = 400;
    private static int mEntriesToWrite = MAX_ENTRIES;
    private BandClient mBandClient = null;
    private SensorModel mRawAccelData;
    private SensorModel mRawGyroData;
    SensorModel noGravData = null;
    private static SensorListeners mSensorListeners = null;
    Activity mCallerActivity = null;


    protected SensorListeners() {
        mRawAccelData = new SensorModel();
        mRawGyroData = new SensorModel();
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
            if (event != null && FileUtils.getInstance().bandAccelWriterExists()) {
                if (mEntriesToWrite > 0) {
                    mRawAccelData.pushAll(event.getAccelerationX(), event.getAccelerationY(), event.getAccelerationZ(), event.getTimestamp());

                    //FileUtils.getInstance().getBandAccelWriter().println(String.format(" X = %.3f  Y = %.3f Z = %.3f Time = %3d", accelX, accelY, accelZ, delay));
                    UIAsyncUtils.getInstance().appendToUI(R.id.textView3, "x: " + event.getAccelerationX() + "\ny: " + event.getAccelerationY() + "\nz: " + event.getAccelerationZ());

                    mEntriesToWrite--;
                } else {
                    unregisterListeners();
                    //subtract gravity by subtracting iteratively differences neighbor by neighbor
                    SensorModel noGravData = Algorithm.subtractGravity(mRawGyroData);
                    SensorModel lowPassed = Algorithm.lowPassFilter(mRawGyroData);
                    SensorModel Velocity = Algorithm.getVelocity(noGravData);
                    SensorModel Position = Algorithm.getPosition(Velocity);

                    //then integrate by summing up each value and adding i.e. velocity0= velocity1+accel1 .....

                    FileUtils.getInstance().closeSDFile();
                    mEntriesToWrite = MAX_ENTRIES;
                }
            }
        }
    };

    private BandGyroscopeEventListener mBandGyroscopeEventListener = new BandGyroscopeEventListener() {

        @Override
        public void onBandGyroscopeChanged(final BandGyroscopeEvent event) {
            if (event != null && FileUtils.getInstance().bandGyroWriterExists()) {

                if (mEntriesToWrite > 0) {
                    //FileUtils.getInstance().getBandGyroWriter().println(String.format(" X = %.3f  Y = %.3f Z = %.3f", event.getAccelerationX(), event.getAccelerationY(), event.getAccelerationZ()));
                    UIAsyncUtils.getInstance().appendToUI(R.id.textView4, "x: " + event.getAngularVelocityX() + "\ny: " + event.getAngularVelocityY() + "\nz: " + event.getAngularVelocityZ());
                    mRawGyroData.pushAll(event.getAngularVelocityX(), event.getAngularVelocityY(), event.getAngularVelocityZ(), event.getTimestamp());
                    mEntriesToWrite--;
                } else {
                    unregisterListeners();
                    /*Replace with angular version or abstract process function here*/
                    SensorModel noGravData = Algorithm.subtractGravity(mRawGyroData);
                    SensorModel lowPassed = Algorithm.lowPassFilter(mRawGyroData);
                    SensorModel Velocity = Algorithm.getVelocity(noGravData);
                    SensorModel Position = Algorithm.getPosition(Velocity);

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
