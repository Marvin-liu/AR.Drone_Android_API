package com.parrot.freeflight.activities;

import android.content.*;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import com.parrot.freeflight.DemoApplication;
import com.parrot.freeflight.activities.base.BaseActivity;
import com.parrot.freeflight.drone.NavData;
import com.parrot.freeflight.receivers.*;
import com.parrot.freeflight.service.DroneControlService;
import com.parrot.freeflight.settings.ApplicationSettings;
import com.parrot.freeflight.ui.SettingsDialogDelegate;

/**
 * Created by Yang Zhang on 2014/3/22.
 */
public class ControlDroneActivity extends BaseActivity
        implements WifiSignalStrengthReceiverDelegate, DroneEmergencyChangeReceiverDelegate,
        DroneBatteryChangedReceiverDelegate, DroneFlyingStateReceiverDelegate,
        DroneCameraReadyActionReceiverDelegate, SettingsDialogDelegate {

    private static final int LOW_DISK_SPACE_BYTES_LEFT = 1048576 * 20; //20 mebabytes
    private static final int WARNING_MESSAGE_DISMISS_TIME = 5000; // 5 seconds

    private static final String TAG = "ControlDroneActivity";
    private static final float ACCELERO_TRESHOLD = (float) Math.PI / 180.0f * 2.0f;

    private static final int PITCH = 1;
    private static final int ROLL = 2;

    private DroneControlService droneControlService;
    private ApplicationSettings settings;
    private SettingsDialog settingsDialog;

    private int screenRotationIndex;

    private WifiSignalStrengthChangedReceiver wifiSignalReceiver;
    private DroneEmergencyChangeReceiver droneEmergencyReceiver;
    private DroneBatteryChangedReceiver droneBatteryReceiver;
    private DroneFlyingStateReceiver droneFlyingStateReceiver;
    private DroneCameraReadyChangeReceiver droneCameraReadyChangedReceiver;

    private boolean combinedYawEnabled;
    private boolean acceleroEnabled;
    private boolean magnetoEnabled;
    private boolean magnetoAvailable;
    private boolean controlLinkAvailable;

    private float pitchBase;
    private float rollBase;
    private boolean running;

    private boolean flying;
    private boolean cameraReady;

    private ServiceConnection mConnection = new ServiceConnection() {

        public void onServiceConnected(ComponentName name, IBinder service) {
            droneControlService = ((DroneControlService.LocalBinder) service).getService();
            onDroneServiceConnected();
        }

        public void onServiceDisconnected(ComponentName name) {
            droneControlService = null;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (isFinishing()) {
            return;
        }

        settings = getSettings();
        bindService(new Intent(this, DroneControlService.class), mConnection, Context.BIND_AUTO_CREATE);

        Bundle bundle = getIntent().getExtras();

        combinedYawEnabled = true;
        acceleroEnabled = false;

        wifiSignalReceiver = new WifiSignalStrengthChangedReceiver(this);
        droneEmergencyReceiver = new DroneEmergencyChangeReceiver(this);
        droneBatteryReceiver = new DroneBatteryChangedReceiver(this);
        droneFlyingStateReceiver = new DroneFlyingStateReceiver(this);
        droneCameraReadyChangedReceiver = new DroneCameraReadyChangeReceiver(this);
    }

    @Override
    protected void onDestroy() {
        unbindService(mConnection);
        super.onDestroy();
        Log.d(TAG, "ControlDroneActivity destroyed");
        System.gc();
    }

    @Override
    protected void onResume() {
        if (droneControlService != null) {
            droneControlService.resume();
        }

        registerReceivers();
        refreshWifiSignalStrength();

        super.onResume();
    }

    @Override
    protected void onPause() {
        if (droneControlService != null) {
            droneControlService.pause();
        }

        unregisterReceivers();

        System.gc();
        super.onPause();
    }

    private void registerReceivers() {
        // System wide receiver
        registerReceiver(wifiSignalReceiver, new IntentFilter(WifiManager.RSSI_CHANGED_ACTION));

        // Local receivers
        LocalBroadcastManager localBroadcastMgr = LocalBroadcastManager.getInstance(getApplicationContext());
        localBroadcastMgr.registerReceiver(droneEmergencyReceiver, new IntentFilter(DroneControlService.DRONE_EMERGENCY_STATE_CHANGED_ACTION));
        localBroadcastMgr.registerReceiver(droneBatteryReceiver, new IntentFilter(DroneControlService.DRONE_BATTERY_CHANGED_ACTION));
        localBroadcastMgr.registerReceiver(droneFlyingStateReceiver, new IntentFilter(DroneControlService.DRONE_FLYING_STATE_CHANGED_ACTION));
        localBroadcastMgr.registerReceiver(droneCameraReadyChangedReceiver, new IntentFilter(DroneControlService.CAMERA_READY_CHANGED_ACTION));
    }

    private void unregisterReceivers() {
        // Unregistering system receiver
        unregisterReceiver(wifiSignalReceiver);

        // Unregistering local receivers
        LocalBroadcastManager localBroadcastMgr = LocalBroadcastManager.getInstance(getApplicationContext());
        localBroadcastMgr.unregisterReceiver(droneEmergencyReceiver);
        localBroadcastMgr.unregisterReceiver(droneBatteryReceiver);
        localBroadcastMgr.unregisterReceiver(droneFlyingStateReceiver);
        localBroadcastMgr.unregisterReceiver(droneCameraReadyChangedReceiver);
    }

    /**
     * Called when we connected to DroneControlService
     */
    protected void onDroneServiceConnected() {
        if (droneControlService != null) {
            droneControlService.resume();
            droneControlService.requestDroneStatus();
        } else {
            Log.w(TAG, "DroneServiceConnected event ignored as DroneControlService is null");
        }

//        settingsDialog = new SettingsDialog(this, this, droneControlService, magnetoAvailable);

        applySettings(settings);

        initListeners();

        /*if (droneControlService.getMediaDir() != null) {
            view.setRecordButtonEnabled(true);
            view.setCameraButtonEnabled(true);
        }*/
    }

    private ApplicationSettings getSettings() {
        return ((DemoApplication) getApplication()).getAppSettings();
    }

    private void applySettings(ApplicationSettings settings) {
        applySettings(settings, false);
    }

    private void applySettings(ApplicationSettings settings, boolean skipJoypadConfig) {

        if (droneControlService != null)
            droneControlService.setMagnetoEnabled(magnetoEnabled);
    }

    public void refreshWifiSignalStrength() {
        WifiManager manager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
        int signalStrength = WifiManager.calculateSignalLevel(manager.getConnectionInfo().getRssi(), 4);
        onWifiSignalStrengthChanged(signalStrength);
    }

    private void initListeners() {

        /**
         * Here are the methods to control the drone
         */

       /* view.setSettingsButtonClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                showSettingsDialog();
            }
        });

        view.setBtnCameraSwitchClickListener(new View.OnClickListener() {

            public void onClick(View v) {
                if (droneControlService != null) {
                    droneControlService.switchCamera();
                }
            }
        });

        view.setBtnTakeOffClickListener(new View.OnClickListener() {

            public void onClick(View v) {
                if (droneControlService != null) {
                    droneControlService.triggerTakeOff();
                }
            }
        });

        view.setBtnEmergencyClickListener(new View.OnClickListener() {

            public void onClick(View v) {
                if (droneControlService != null) {
                    droneControlService.triggerEmergency();
                }
            }

        });

        view.setBtnPhotoClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (droneControlService != null) {
                    onTakePhoto();
                }
            }
        });

        view.setBtnRecordClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                onRecord();
            }
        });

        view.setBtnBackClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                finish();
            }
        });

        view.setDoubleTapClickListener(new GestureDetector.OnDoubleTapListener() {

            public boolean onSingleTapConfirmed(MotionEvent e) {
                // Left unimplemented
                return false;
            }

            public boolean onDoubleTapEvent(MotionEvent e) {
                // Left unimplemented
                return false;
            }

            public boolean onDoubleTap(MotionEvent e) {
                if (settings.isFlipEnabled() && droneControlService != null) {
                    droneControlService.doLeftFlip();
                    return true;
                }

                return false;
            }
        });*/
    }


    @Override
    public void onDroneBatteryChanged(int value) {
        Log.d(TAG, "BATTERY_VALUE" + value);
    }

    @Override
    public void onCameraReadyChanged(boolean ready) {
        cameraReady = ready;
    }

    @Override
    public void onDroneEmergencyChanged(int code) {

        if (code == NavData.ERROR_STATE_EMERGENCY_VBAT_LOW || code == NavData.ERROR_STATE_ALERT_VBAT_LOW) {
            Log.d(TAG, "ERROR_EMERGENCY_VBAT_LOW");
        } else {
            Log.d(TAG, "ERROR_DISMISSED");
        }

        controlLinkAvailable = (code != NavData.ERROR_STATE_NAVDATA_CONNECTION);

        if (!controlLinkAvailable) {
            /*view.setRecordButtonEnabled(false);
            view.setCameraButtonEnabled(false);
            view.setSwitchCameraButtonEnabled(false);*/
        } else {
            /*view.setSwitchCameraButtonEnabled(true);
            view.setRecordButtonEnabled(true);
            view.setCameraButtonEnabled(true);*/
        }

//        view.setEmergencyButtonEnabled(!NavData.isEmergency(code));
    }

    @Override
    public void onDroneFlyingStateChanged(boolean flying) {
        this.flying = flying;

    }

    @Override
    public void prepareDialog(SettingsDialog dialog) {

    }

    @Override
    public void onDismissed(SettingsDialog settingsDialog) {

    }

    @Override
    public void onOptionChangedApp(SettingsDialog dialog, ApplicationSettings.EAppSettingProperty property, Object value) {

    }

    @Override
    public void onWifiSignalStrengthChanged(int strength) {
        Log.d(TAG, "WIFI_SIGNAL" + strength);
    }
}
