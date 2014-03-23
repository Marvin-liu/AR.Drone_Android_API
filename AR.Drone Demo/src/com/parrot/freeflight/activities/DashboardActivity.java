package com.parrot.freeflight.activities;

import android.annotation.SuppressLint;
import android.content.*;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import com.parrot.freeflight.R;
import com.parrot.freeflight.activities.base.DashboardActivityBase;
import com.parrot.freeflight.receivers.*;
import com.parrot.freeflight.service.DroneControlService;
import com.parrot.freeflight.service.intents.DroneStateManager;
import com.parrot.freeflight.tasks.CheckDroneNetworkAvailabilityTask;
import com.parrot.freeflight.utils.GPSHelper;

/**
 * Created by Yang Zhang on 2014/3/22.
 */
public class DashboardActivity extends DashboardActivityBase
        implements ServiceConnection,
        DroneAvailabilityDelegate,
        NetworkChangeReceiverDelegate, DroneConnectionChangeReceiverDelegate {
    private DroneControlService mService;

    private BroadcastReceiver droneStateReceiver;
    private BroadcastReceiver networkChangeReceiver;
    private BroadcastReceiver droneConnectionChangeReceiver;

    private CheckDroneNetworkAvailabilityTask checkDroneConnectionTask;

    private boolean droneOnNetwork;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (isFinishing()) {
            return;
        }

        initBroadcastReceivers();

        bindService(new Intent(this, DroneControlService.class), this, Context.BIND_AUTO_CREATE);

        if (GPSHelper.deviceSupportGPS(this) && !GPSHelper.isGpsOn(this)) {
            onNotifyAboutGPSDisabled();
        }
    }


    protected void initBroadcastReceivers() {
        droneStateReceiver = new DroneAvailabilityReceiver(this);
        networkChangeReceiver = new NetworkChangeReceiver(this);
        droneConnectionChangeReceiver = new DroneConnectionChangedReceiver(this);
    }


    private void registerBroadcastReceivers() {
        LocalBroadcastManager broadcastManager = LocalBroadcastManager.getInstance(getApplicationContext());
        broadcastManager.registerReceiver(droneStateReceiver, new IntentFilter(
                DroneStateManager.ACTION_DRONE_STATE_CHANGED));

        IntentFilter mediaReadyFilter = new IntentFilter();
        broadcastManager.registerReceiver(droneConnectionChangeReceiver, new IntentFilter(DroneControlService.DRONE_CONNECTION_CHANGED_ACTION));

        registerReceiver(networkChangeReceiver, new IntentFilter(WifiManager.NETWORK_STATE_CHANGED_ACTION));
    }


    private void unregisterReceivers() {
        LocalBroadcastManager broadcastManager = LocalBroadcastManager.getInstance(getApplicationContext());
        broadcastManager.unregisterReceiver(droneStateReceiver);
        broadcastManager.unregisterReceiver(droneConnectionChangeReceiver);
        unregisterReceiver(networkChangeReceiver);
    }


    @Override
    protected void onDestroy() {
        unbindService(this);
        super.onDestroy();
    }


    @Override
    protected void onPause() {
        super.onPause();

        unregisterReceivers();
        stopTasks();
    }


    @Override
    protected void onResume() {
        super.onResume();

        registerBroadcastReceivers();

        disableAllButtons();

        /*if (mService != null) {
            checkMedia();
        }*/
        checkDroneConnectivity();
    }


    private void disableAllButtons() {
        droneOnNetwork = false;

        requestUpdateButtonsState();
    }


    @Override
    protected boolean onStartFreeflight() {
        if (!droneOnNetwork) {
            return false;
        }

        Intent connectActivity = new Intent(this, ConnectActivity.class);
        startActivity(connectActivity);

        return true;
    }


    public void onNetworkChanged(NetworkInfo info) {
        Log.d(TAG, "Network state has changed. State is: " + (info.isConnected() ? "CONNECTED" : "DISCONNECTED"));

        if (mService != null && info.isConnected()) {
            checkDroneConnectivity();
        } else {
            droneOnNetwork = false;
            requestUpdateButtonsState();
        }
    }


    public void onDroneConnected() {
        if (mService != null) {
            mService.pause();
        }
    }


    public void onDroneDisconnected() {
        // Left unimplemented
    }


    public void onDroneAvailabilityChanged(boolean droneOnNetwork) {
        if (droneOnNetwork) {
            Log.d(TAG, "AR.Drone connection [CONNECTED]");
            this.droneOnNetwork = droneOnNetwork;

            requestUpdateButtonsState();
        } else {
            Log.d(TAG, "AR.Drone connection [DISCONNECTED]");
        }
    }


    @SuppressLint("NewApi")
    private void checkDroneConnectivity() {
        if (checkDroneConnectionTask != null && checkDroneConnectionTask.getStatus() != AsyncTask.Status.FINISHED) {
            checkDroneConnectionTask.cancel(true);
        }

        checkDroneConnectionTask = new CheckDroneNetworkAvailabilityTask() {

            @Override
            protected void onPostExecute(Boolean result) {
                onDroneAvailabilityChanged(result);
            }

        };

        if (Build.VERSION.SDK_INT >= 11) {
            checkDroneConnectionTask.executeOnExecutor(CheckDroneNetworkAvailabilityTask.THREAD_POOL_EXECUTOR, this);
        } else {
            checkDroneConnectionTask.execute(this);
        }
    }


    public void onServiceConnected(ComponentName name, IBinder service) {
        /*mService = ((DroneControlService.LocalBinder) service).getService();

        File mediaDir = mService.getMediaDir();
        if (mediaDir == null) {
            mediaState = EPhotoVideoState.NO_SDCARD;
            requestUpdateButtonsState();
        }

        checkMedia();*/
    }


    public void onServiceDisconnected(ComponentName name) {
        // Left unimplemented
    }


    private boolean taskRunning(AsyncTask<?, ?, ?> checkMediaTask2) {
        if (checkMediaTask2 == null)
            return false;

        if (checkMediaTask2.getStatus() == AsyncTask.Status.FINISHED)
            return false;

        return true;
    }


    private void stopTasks() {

        if (taskRunning(checkDroneConnectionTask)) {
            checkDroneConnectionTask.cancelAnyFtpOperation();
        }

    }

    @Override
    protected boolean isFreeFlightEnabled() {
        return droneOnNetwork;
    }


    private void onNotifyAboutGPSDisabled() {
        showAlertDialog(getString(R.string.Location_services_alert), getString(R.string.If_you_want_to_store_your_location_anc_access_your_media_enable_it),
                null);
    }

}
