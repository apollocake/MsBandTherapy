package com.aftonmartin.android.msbandtherapy;


import android.app.Activity;
import android.os.Environment;
import android.util.Log;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

public class FileUtils {
    private static PrintWriter mBandAccelWriter = null;
    private static PrintWriter mBandGyroWriter = null;
    private static PrintWriter mLowPassWriter = null;
    private static PrintWriter mBiasWriter = null;
    private static PrintWriter mSubtractGravWriter = null;
    private static PrintWriter mGyroVelocityWriter = null;
    private static PrintWriter mPositionWriter = null;

    private static File mBandAccelFile = null;
    private static File mBandGyroFile = null;
    private static File mLowPassFile = null;
    private static File mBiasFile = null;
    private static File mSubtractGravFile = null;
    private static File mGyroVelocityFile = null;
    private static File mPositionFile = null;

    private static final String TAG = "MEDIA";
    private static FileUtils fileUtils = null;
    private static UIAsyncUtils mUIAsyncUtils = null;
    private static Activity mainActivity = null;

    protected FileUtils() {
        mUIAsyncUtils = UIAsyncUtils.getInstance();
    }

    public synchronized static FileUtils getInstance() {
        if (fileUtils == null) {
            fileUtils = new FileUtils();
        }
        return fileUtils;
    }

    public synchronized static void setFileActivity(Activity activity) {
        if (mainActivity == null) {
            mainActivity = activity;
        }
    }

    public static PrintWriter getBandAccelWriter() {
        return mBandAccelWriter;
    }

    public static PrintWriter getBandGyroWriter() {
        return mBandGyroWriter;
    }
    public static PrintWriter getLowPassWriter() {
        return mLowPassWriter;
    }
    public static PrintWriter getBiasWriter() {
        return mBiasWriter;
    }

    public static PrintWriter getSubtractGravWriter() {
        return mSubtractGravWriter;
    }

    public static PrintWriter getGyroVelocityWriter() {
        return mGyroVelocityWriter;
    }

    public static PrintWriter getPositionWriter() {
        return mPositionWriter;
    }


    public static boolean bandAccelWriterExists() {
        if (mBandAccelWriter == null) {
            return false;
        }
        return true;
    }

    public static boolean bandGyroWriterExists() {
        if (mBandGyroWriter == null) {
            return false;
        }
        return true;
    }


    public static boolean lowPassWriterExists() {
        if (mLowPassWriter == null) {
            return false;
        }
        return true;
    }
    public static boolean subtractGravWriterExists() {
        if (mSubtractGravWriter == null) {
            return false;
        }
        return true;
    }
    public static boolean gyroVelocityWriterExists() {
        if (mGyroVelocityWriter == null) {
            return false;
        }
        return true;
    }
    public static boolean positionWriterExists() {
        if (mPositionWriter == null) {
            return false;
        }
        return true;
    }
    public static boolean bandGyroFileExists() {
        if (mBandGyroFile == null) {
            return false;
        }
        return true;
    }

    public static boolean bandAccelFileExists() {
        if (mBandAccelFile == null) {
            return false;
        }
        return true;
    }
    public static boolean lowPassFileExists() {
        if (mLowPassFile == null) {
            return false;
        }
        return true;
    }
    public static boolean subtractGravFileExists() {
        if (mSubtractGravFile == null) {
            return false;
        }
        return true;
    }
    public static boolean gyroVelocityFileExists() {
        if (mGyroVelocityFile == null) {
            return false;
        }
        return true;
    }



    public void checkPermissionsExplicit() {
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

    }


    public void openSDFile() {
        //get base URI
        File root = android.os.Environment.getExternalStorageDirectory();
        //mResultsView.append("\nExternal file system root: " + root);
        mUIAsyncUtils.appendToUI(R.id.results, "\nExternal file system root: " + root);

        //set file up for writing
        File mediaFile = new File(mainActivity.getApplicationContext().getExternalCacheDir(), "SensorData");
        mediaFile.mkdirs();
        mBandAccelFile = new File(mediaFile, "band_accel.txt");
        mBandGyroFile = new File(mediaFile, "band_gyro.txt");
        mPositionFile = new File(mediaFile, "position_data.csv");
        mGyroVelocityFile = new File(mediaFile, "gyro_velocity.csv");
        mSubtractGravFile = new File(mediaFile, "sub_grav.csv");
        mLowPassFile = new File(mediaFile, "low_pass.csv");
        mBiasFile = new File(mediaFile, "bias_removal.csv");

        try {
            mBandAccelWriter = new PrintWriter(new BufferedWriter(new FileWriter(mBandAccelFile), 8192));
            mBandGyroWriter = new PrintWriter(new BufferedWriter(new FileWriter(mBandGyroFile), 8192));
            mPositionWriter = new PrintWriter(new BufferedWriter(new FileWriter(mPositionFile), 8192));
            mSubtractGravWriter = new PrintWriter(new BufferedWriter(new FileWriter(mSubtractGravFile), 8192));
            mLowPassWriter = new PrintWriter(new BufferedWriter(new FileWriter(mLowPassFile), 8192));
            mBiasWriter = new PrintWriter(new BufferedWriter(new FileWriter(mBiasFile), 8192));
            mGyroVelocityWriter = new PrintWriter(new BufferedWriter(new FileWriter(mGyroVelocityFile), 8192));


        } catch (FileNotFoundException e) {
            e.printStackTrace();
            Log.i(TAG, "******* File not found. Did you" +
                    " add a WRITE_EXTERNAL_STORAGE permission to the   manifest?");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void closeSDFile() {
        if ((mBandGyroWriter != null) && (mBandAccelWriter != null)
                                        && (mLowPassWriter != null)
                                        && (mBiasWriter != null)
                                        && (mSubtractGravWriter != null)
                                        && (mGyroVelocityWriter != null)
                                        && (mPositionWriter != null)) {
            try {
                mBandAccelWriter.close();
                mBandGyroWriter.close();
                mGyroVelocityWriter.close();
                mLowPassWriter.close();
                mBiasWriter.close();
                mPositionWriter.close();
                mSubtractGravWriter.close();
            } catch (Exception e) {

                e.printStackTrace();
            }
        }
        mBandAccelWriter = null;
        mBandGyroWriter= null;
        mGyroVelocityWriter= null;
        mLowPassWriter = null;
        mBiasWriter = null;
        mPositionWriter = null;
        mSubtractGravWriter = null;

    }



}
