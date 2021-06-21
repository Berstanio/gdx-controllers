package com.badlogic.gdx.controllers;

import apple.uikit.UIDevice;

public class Utils {

    public static int getMajorSystemVersion() {
        String version = UIDevice.currentDevice().systemVersion();
        if (version != null) {
            String[] parts = version.split("\\.");
            if (parts.length > 0) return Integer.parseInt(parts[0]);
        }
        return  6;
    }
}
