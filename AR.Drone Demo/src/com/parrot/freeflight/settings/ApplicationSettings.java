package com.parrot.freeflight.settings;

import android.content.Context;

/**
 * Created by Yang Zhang on 2014/3/22.
 */
public class ApplicationSettings {

    public static final long MEMORY_USAGE = 0;

    public enum EAppSettingProperty {
        FIRST_LAUNCH_PROP("first_launch"),
        LEFT_HANDED_PROP("left_handed"),
        FORCE_COMBINED_CTRL_PROP("force_combined_control"),
        CONTROL_MODE_PROP("control_mode"),
        INTERFACE_OPACITY_PROP("interface_opacity"),
        MAGNETO_ENABLED_PROP("magneto_enabled"),
        LOOPING_ENABLED_PROP("looping_enabled"),
        ASK_FOR_GPS("ask_for_gps");

        private final String propname;

        private EAppSettingProperty(final String propname) {
            this.propname = propname;
        }

        @Override
        public String toString() {
            return propname;
        }
    }

    public ApplicationSettings(Context context) {

    }
}
