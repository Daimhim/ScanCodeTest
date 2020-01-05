package org.daimhim.scancodetest;

import org.daimhim.scancodetest.camera.FrontLightMode;

public class ScanCodeConfig {
    public static boolean KEY_AUTO_FOCUS = true;
    public static boolean KEY_DISABLE_CONTINUOUS_FOCUS = false;
    public static boolean KEY_INVERT_SCAN = false;
    public static boolean KEY_DISABLE_BARCODE_SCENE_MODE = true;
    public static boolean KEY_DISABLE_METERING = true;
    public static boolean KEY_DISABLE_EXPOSURE = true;

    public static String KEY_FRONT_LIGHT_MODE = FrontLightMode.OFF.toString();


//    BeepManager
    /**
     * 震动
     */
    public static boolean KEY_VIBRATE = true;
    /**
     * 声音信号
     */
    public static boolean KEY_PLAY_BEEP = true;
}
