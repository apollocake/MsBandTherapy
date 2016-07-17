package com.aftonmartin.android.csvwriter;


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
    private static File mBandAccelFile = null;
    private static File mBandGyroFile = null;
    private static final String TAG = "MEDIA";
    private static FileUtils fileUtils = null;
    private static UIAsyncUtils mUIAsyncUtils = null;
    private static Activity mainActivity = null;

    protected FileUtils() {
    }

    public synchronized static FileUtils getInstance() {
        if (fileUtils == null) {
            fileUtils = new FileUtils();
            mUIAsyncUtils = UIAsyncUtils.getInstance();
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

    public static boolean bandGyroFileExists() {
        if (mBandGyroFile == null) {
            return false;
        }
        return true;
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

    public static boolean bandAccelFileExists() {
        if (mBandAccelFile == null) {
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


    public void closeSDFile() {
        if ((mBandGyroWriter != null) && (mBandAccelWriter != null)) {
            try {
                mBandAccelWriter.close();
                mBandGyroWriter.close();
            } catch (Exception e) {

                e.printStackTrace();
            }
        }
        //mResultsView.append("\n\nFile written to " + mBandAccelFile + "\n");
        //mResultsView.append("\n\nFile written to " + mBandGyroFile);
    }

}
