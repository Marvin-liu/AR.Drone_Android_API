package com.parrot.freeflight.service.commands;

import com.parrot.freeflight.drone.DroneProxy;
import com.parrot.freeflight.service.DroneControlService;

/**
 * Created by Yang Zhang on 2014/3/22.
 */
public class PauseCommand extends DroneServiceCommand {
    private DroneProxy droneProxy;


    public PauseCommand(DroneControlService context) {
        super(context);
        droneProxy = DroneProxy.getInstance(context.getApplicationContext());
    }


    @Override
    public void execute() {
        droneProxy.doPause();

        context.onCommandFinished(this);
    }
}
