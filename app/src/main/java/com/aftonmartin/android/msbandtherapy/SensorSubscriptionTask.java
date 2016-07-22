package com.aftonmartin.android.msbandtherapy;


import android.app.Activity;
import android.os.AsyncTask;

import com.microsoft.band.BandClientManager;
import com.microsoft.band.BandException;
import com.microsoft.band.BandInfo;
import com.microsoft.band.ConnectionState;

public class SensorSubscriptionTask extends AsyncTask<Void, Void, Void> {
    Activity mCallerActivity = null;
    public SensorSubscriptionTask(Activity callerActivity){
        mCallerActivity = callerActivity;
    }

    @Override
    protected Void doInBackground(Void... params) {
        try {
            if (getConnectedBandClient()) {

                UIAsyncUtils.getInstance().appendToUI(R.id.results, "Band is connected.\n");
                SensorListeners.getInstance().registerListeners();

            } else {
                UIAsyncUtils.getInstance().appendToUI(R.id.results, "Band isn't connected. Please make sure bluetooth is on and the band is in range.\n");
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
            UIAsyncUtils.getInstance().appendToUI(R.id.results, exceptionMessage);

        } catch (Exception e) {
            UIAsyncUtils.getInstance().appendToUI(R.id.results, e.getMessage());
        }
        return null;
    }



    private boolean getConnectedBandClient() throws InterruptedException, BandException {
        if (SensorListeners.getInstance().getBandClient() == null) {
            BandInfo[] devices = BandClientManager.getInstance().getPairedBands();
            if (devices.length == 0) {
                UIAsyncUtils.getInstance().appendToUI(R.id.results, "Band isn't paired with your phone.\n");
                return false;
            }
            SensorListeners.getInstance().setBandClient(BandClientManager.getInstance().create(mCallerActivity.getBaseContext(), devices[0]));
        } else if (ConnectionState.CONNECTED == SensorListeners.getInstance().getBandClient().getConnectionState()) {
            return true;
        }

        UIAsyncUtils.getInstance().appendToUI(R.id.results, "Band is connecting...\n");
        return ConnectionState.CONNECTED == SensorListeners.getInstance().getBandClient().connect().await();
    }


}
