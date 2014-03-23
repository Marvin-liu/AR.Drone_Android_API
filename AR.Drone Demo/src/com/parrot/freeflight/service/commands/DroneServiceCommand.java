package com.parrot.freeflight.service.commands;

import com.parrot.freeflight.service.DroneControlService;

/**
 * Created by Yang Zhang on 2014/3/22.
 */
public abstract class DroneServiceCommand {
    protected DroneControlService context;

    public DroneServiceCommand(DroneControlService context) {
        this.context = context;
    }

    public abstract void execute();
}
