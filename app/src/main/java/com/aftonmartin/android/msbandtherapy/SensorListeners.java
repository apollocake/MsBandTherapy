package com.aftonmartin.android.msbandtherapy;

import android.app.Activity;
import android.media.AudioManager;
import android.media.ToneGenerator;

import com.microsoft.band.BandClient;;
import com.microsoft.band.BandIOException;
import com.microsoft.band.sensors.BandAccelerometerEvent;
import com.microsoft.band.sensors.BandAccelerometerEventListener;
import com.microsoft.band.sensors.BandGyroscopeEvent;
import com.microsoft.band.sensors.BandGyroscopeEventListener;
import com.microsoft.band.sensors.SampleRate;

public class SensorListeners {
    private static final int MAX_ENTRIES = 400;
    private static Algorithm.MOVEMENT_STATE movementStatus;
    private static int mEntriesToWrite = MAX_ENTRIES;
    private BandClient mBandClient = null;
    private SensorModel mRawAccelData;
    private SensorModel mRawGyroData;
    SensorModel noGravData = null;
    private static SensorListeners mSensorListeners = null;
    Activity mCallerActivity = null;
    final ToneGenerator tg = new ToneGenerator(AudioManager.STREAM_NOTIFICATION, 50);


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
                    mRawAccelData.pushAll(event.getAccelerationX(), event.getAccelerationY(), event.getAccelerationZ(), event.getTimestamp());
                    FileUtils.getInstance().getBandAccelWriter().println(String.format("%.6f,%.6f,%.6f,%3d", event.getAccelerationX(),
                            event.getAccelerationY(), event.getAccelerationZ(), event.getTimestamp()));
                    UIAsyncUtils.getInstance().appendToUI(R.id.textView3, "x: " + event.getAccelerationX() + "\ny: " + event.getAccelerationY() + "\nz: " + event.getAccelerationZ());
            }
        }
    };

    private BandGyroscopeEventListener mBandGyroscopeEventListener = new BandGyroscopeEventListener() {

        @Override
        public void onBandGyroscopeChanged(final BandGyroscopeEvent event) {
            if (event != null && FileUtils.getInstance().bandGyroWriterExists()) {
                    FileUtils.getInstance().getGyroVelocityWriter().println(String.format("%.6f,%.6f,%.6f,%3d", event.getAngularVelocityX(), event.getAngularVelocityY(), event.getAngularVelocityZ(), event.getTimestamp()));
                    UIAsyncUtils.getInstance().appendToUI(R.id.textView4, "x: " + event.getAngularVelocityX() + "\ny: " + event.getAngularVelocityY() + "\nz: " + event.getAngularVelocityZ());
                    mRawGyroData.pushAll(event.getAngularVelocityX(), event.getAngularVelocityY(), event.getAngularVelocityZ(), event.getTimestamp());}
                    beep(Algorithm.detectStatus(event.getAngularVelocityZ()));
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
            mBandClient.getSensorManager().registerAccelerometerEventListener(mBandAccelerometerEventListener, SampleRate.MS16); //average sample rate every 16s
            mBandClient.getSensorManager().registerGyroscopeEventListener(mBandGyroscopeEventListener, SampleRate.MS16);
        } catch (BandIOException e) {
            throw e;
        } catch (Exception e) {
            throw e;
        }
    }
    public SensorModel getGyroData() {
        return mRawGyroData;
    }
    public SensorModel getAccelData() {
        return mRawAccelData;
    }

    public void beep(Algorithm.MOVEMENT_STATE movementStatus){
        switch (movementStatus){
            case AT_MAX:
                tg.startTone(ToneGenerator.TONE_PROP_BEEP);
                break;
            case AT_MIN:
                tg.startTone(ToneGenerator.TONE_PROP_BEEP2);
                break;
            default:
                break;
        }
    }

}
