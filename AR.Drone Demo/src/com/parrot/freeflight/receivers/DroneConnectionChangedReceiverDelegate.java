package com.parrot.freeflight.receivers;

/**
 * Created by JeffreyZhang on 2014/3/22.
 */
public interface DroneConnectionChangedReceiverDelegate {
    public void onDroneConnected();

    public void onDroneDisconnected();
}
