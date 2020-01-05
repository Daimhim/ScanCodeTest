package org.daimhim.scancodetest;

import android.app.Activity;
import android.graphics.Bitmap;
import android.os.Handler;

import com.google.zxing.Result;

import org.daimhim.scancodetest.camera.CameraManager;
import org.daimhim.scancodetest.view.ViewfinderView;


public interface IScanCode {

    void handleDecode(Result pResult, Bitmap pBitmap, float scaleFactor);
    ViewfinderView getViewfinderView();
    void drawViewfinder();
    CameraManager getCameraManager();
    Activity getActivity();
    Handler getHandler();
}
