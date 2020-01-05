package org.daimhim.scancodetest;

import android.app.AlertDialog;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;


import com.google.zxing.Result;

import org.daimhim.scancodetest.camera.CameraManager;
import org.daimhim.scancodetest.view.ViewfinderView;

import java.io.IOException;

public class ScanCodeFragment  extends Fragment implements SurfaceHolder.Callback,IScanCode{

    private static final String TAG = ScanCodeFragment.class.getSimpleName();

    private ViewfinderView mViewfinderView;
    private CameraManager mCameraManager;
    private ScanCodeHandler mScanCodeHandler;
    private InactivityTimer mInactivityTimer;
    private BeepManager mBeepManager;
    private AmbientLightManager mAmbientLightManager;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_scan_code,container,false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        initView(view);
    }

    private void initView(View pView) {
        mViewfinderView = pView.findViewById(R.id.viewfinder_view);

        mCameraManager = new CameraManager(getActivity());
        mViewfinderView.setCameraManager(mCameraManager);

        mInactivityTimer = new InactivityTimer(this);
        mBeepManager = new BeepManager(this);
        mAmbientLightManager = new AmbientLightManager(this);

        SurfaceView surfaceView = (SurfaceView) pView.findViewById(R.id.preview_view);
        SurfaceHolder surfaceHolder = surfaceView.getHolder();
        surfaceHolder.addCallback(this);
    }

    private void initCamera(SurfaceHolder surfaceHolder) {
        if (surfaceHolder == null) {
            throw new IllegalStateException("No SurfaceHolder provided");
        }
        if (mCameraManager.isOpen()) {
            Log.w(TAG, "initCamera() while already open -- late SurfaceView callback?");
            return;
        }
        try {
            mCameraManager.openDriver(surfaceHolder);
            // Creating the handler starts the preview, which can also throw a RuntimeException.
            if (mScanCodeHandler == null) {
                mScanCodeHandler = new ScanCodeHandler(this, mCameraManager);
            }
        } catch (IOException ioe) {
            Log.w(TAG, ioe);
            displayFrameworkBugMessageAndExit();
        } catch (RuntimeException e) {
            // Barcode Scanner has seen crashes in the wild of this variety:
            // java.?lang.?RuntimeException: Fail to connect to camera service
            Log.w(TAG, "Unexpected error initializing camera", e);
            displayFrameworkBugMessageAndExit();
        }
    }

    private void displayFrameworkBugMessageAndExit() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle(getString(R.string.app_name));
        builder.setMessage("抱歉，Android相机遇到问题。 您可能需要重新启动设备。");
        builder.setPositiveButton(R.string.button_ok, new FinishListener(getActivity()));
        builder.setOnCancelListener(new FinishListener(getActivity()));
        builder.show();
    }

    private void decodeBitmap(Bitmap pBitmap, Result result) {
        if (mScanCodeHandler == null){
            return;
        }
        Message message = Message.obtain(mScanCodeHandler, R.id.decode_succeeded);
        mScanCodeHandler.sendMessage(message);
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        initCamera(holder);
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {

    }

    @Override
    public void handleDecode(Result pResult, Bitmap pBitmap, float scaleFactor) {
        if (pResult != null) {
            Log.w(TAG, "handleDecode " + pResult.toString());
        }
        mBeepManager.playBeepSoundAndVibrate();
        Message message = Message.obtain(mScanCodeHandler, R.id.restart_preview);
        mScanCodeHandler.sendMessageDelayed(message,3000);
    }

    @Override
    public ViewfinderView getViewfinderView() {
        return mViewfinderView;
    }

    @Override
    public void onPause() {
        if (mScanCodeHandler != null){
            mScanCodeHandler.quitSynchronously();
            mScanCodeHandler = null;
        }
        mInactivityTimer.onPause();
        mAmbientLightManager.stop();
        mBeepManager.close();
        mCameraManager.closeDriver();
        super.onPause();
    }

    @Override
    public void onResume() {
        super.onResume();
        mBeepManager.updatePrefs();
        mAmbientLightManager.start(mCameraManager);
        mInactivityTimer.onResume();
    }

    @Override
    public void onDetach() {
        mInactivityTimer.shutdown();
        super.onDetach();
    }

    @Override
    public void drawViewfinder() {
        mViewfinderView.drawViewfinder();
    }

    @Override
    public CameraManager getCameraManager() {
        return mCameraManager;
    }

    @Override
    public Handler getHandler() {
        return mScanCodeHandler;
    }
}
