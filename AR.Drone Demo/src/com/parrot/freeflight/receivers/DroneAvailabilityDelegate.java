package com.parrot.freeflight.receivers;

/**
 * Created by Yang Zhang on 2014/3/22.
 */
public interface DroneAvailabilityDelegate {
    public void onDroneAvailabilityChanged(boolean isDroneOnNetwork);
}
