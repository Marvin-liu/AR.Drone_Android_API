package com.parrot.freeflight.drone;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * Created by Yang Zhang on 2014/3/22.
 */
public class DroneProxyConnectedReceiver extends BroadcastReceiver {
    private DroneProxyConnectedReceiverDelegate delegate;

    public DroneProxyConnectedReceiver(DroneProxyConnectedReceiverDelegate delegate) {
        this.delegate = delegate;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if (delegate != null) {
            delegate.onToolConnected();
        }
    }
}
