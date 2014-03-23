package com.parrot.freeflight.drone;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * Created by Yang Zhang on 2014/3/22.
 */
public class DroneProxyDisconnectedReceiver extends BroadcastReceiver {
    private DroneProxyDisconnectedReceiverDelegate delegate;

    public DroneProxyDisconnectedReceiver(DroneProxyDisconnectedReceiverDelegate delegate) {
        this.delegate = delegate;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if (delegate != null) {
            delegate.onToolDisconnected();
        }
    }
}
