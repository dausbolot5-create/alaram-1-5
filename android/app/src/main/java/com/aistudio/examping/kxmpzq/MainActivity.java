package com.aistudio.examping.kxmpzq;

import android.os.Bundle;

import com.aistudio.examping.kxmpzq.alarm.ExactAlarmPlugin;
import com.getcapacitor.BridgeActivity;

public class MainActivity extends BridgeActivity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        registerPlugin(ExactAlarmPlugin.class);
        super.onCreate(savedInstanceState);
    }
}
