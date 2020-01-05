package org.daimhim.scancodetest;


import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.zxing.Result;

import org.daimhim.scancodetest.camera.CameraManager;


public class ScanCodeHandler extends Handler {

    private static final String TAG = ScanCodeHandler.class.getSimpleName();
    private State state;
    private IScanCode mIScanCode;
    private final CameraManager mCameraManager;
    private DecodeThread mDecodeThread;

    private enum State {
        PREVIEW,
        SUCCESS,
        DONE
    }

    public ScanCodeHandler(IScanCode pIScanCode,CameraManager pCameraManager) {
        mIScanCode = pIScanCode;
        mCameraManager = pCameraManager;
        mDecodeThread = new DecodeThread(mIScanCode,
                new ViewfinderResultPointCallback(mIScanCode.getViewfinderView()));
        state = State.SUCCESS;
        mCameraManager.startPreview();
        mDecodeThread.start();
        restartPreviewAndDecode();
    }

    @Override
    public void handleMessage(@NonNull Message msg) {
        Log.d(TAG, R.id.decode_failed + " handleMessage " + msg.what);
        if (msg.what == R.id.restart_preview){
            restartPreviewAndDecode();
        }else if (msg.what == R.id.decode_succeeded){
            state = State.SUCCESS;
            Bundle bundle = msg.getData();
            Bitmap barcode = null;
            float scaleFactor = 1.0f;
            if (bundle != null) {
                byte[] compressedBitmap = bundle.getByteArray(DecodeThread.BARCODE_BITMAP);
                if (compressedBitmap != null) {
                    barcode = BitmapFactory.decodeByteArray(compressedBitmap, 0, compressedBitmap.length, null);
                    // Mutable copy:
                    barcode = barcode.copy(Bitmap.Config.ARGB_8888, true);
                }
                scaleFactor = bundle.getFloat(DecodeThread.BARCODE_SCALED_FACTOR);
            }
            mIScanCode.handleDecode((Result) msg.obj, barcode, scaleFactor);
        }else if (msg.what == R.id.decode_failed){
            state = State.PREVIEW;
            mCameraManager.requestPreviewFrame(mDecodeThread.getHandler(), R.id.decode);
        }
    }

    public void quitSynchronously() {
        state = State.DONE;
        mCameraManager.stopPreview();
        Message quit = Message.obtain(mDecodeThread.getHandler(), R.id.quit);
        quit.sendToTarget();
        try {
            // Wait at most half a second; should be enough time, and onPause() will timeout quickly
            mDecodeThread.join(500L);
        } catch (InterruptedException e) {
            // continue
        }

        // Be absolutely sure we don't send any queued up messages
        removeMessages(R.id.decode_succeeded);
        removeMessages(R.id.decode_failed);
    }

    private void restartPreviewAndDecode() {
        if (state == State.SUCCESS) {
            state = State.PREVIEW;
            mCameraManager.requestPreviewFrame(mDecodeThread.getHandler(), R.id.decode);
            mIScanCode.drawViewfinder();
        }
    }
}
