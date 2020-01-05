package org.daimhim.scancodetest;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentActivity;

public class ScanCodeActivity extends FragmentActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scan_code);
        getSupportFragmentManager()
                .beginTransaction()
                .replace(android.R.id.content,new ScanCodeFragment())
                .commit();
    }
}
