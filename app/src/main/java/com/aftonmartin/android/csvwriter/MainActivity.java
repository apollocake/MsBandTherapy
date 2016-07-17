package com.aftonmartin.android.csvwriter;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.view.View;

import com.microsoft.band.BandException;
public class MainActivity extends Activity {


    private Button mStartButton;
    private TextView mAccelDataText = null;
    private TextView mGyroDataText = null;
    private TextView mStatusText = null;

    private SensorListeners mSensorListeners = new SensorListeners();
    SensorSubscriptionTask aTask = null;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //wire up view
        mAccelDataText = (TextView) findViewById(R.id.textView3);
        mGyroDataText = (TextView) findViewById(R.id.textView4);
        mStartButton = (Button) findViewById(R.id.start);
        mStatusText = (TextView) findViewById(R.id.results);

    }

    @Override
    protected void onResume() {
        super.onResume();
        //set Activity for all utilities
        UIAsyncUtils.getInstance().setUIActivity(this);
        FileUtils.getInstance().setFileActivity(this);
        SensorListeners.getInstance().setCallerActivity(this);
        mStartButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                FileUtils.getInstance().checkPermissionsExplicit();
                FileUtils.getInstance().openSDFile();
                //blah blah GO ASYNC!
                if (aTask == null || AsyncTask.Status.FINISHED == aTask.getStatus()) {
                    aTask = new SensorSubscriptionTask(MainActivity.this);
                    aTask.execute();
                }
            }
        });
        mStatusText.setText("");
    }

    @Override
    protected void onPause() {
        super.onPause();
        SensorListeners.getInstance().unregisterListeners();
        FileUtils.getInstance().closeSDFile();
    }


    @Override
    protected void onDestroy() {
        if (SensorListeners.getInstance().getBandClient() != null) {
            try {
                SensorListeners.getInstance().getBandClient().disconnect().await();
            } catch (InterruptedException e) {
                // Do nothing as this is happening during destroy
            } catch (BandException e) {
                // Do nothing as this is happening during destroy
            }
        }
        super.onDestroy();
    }
}
