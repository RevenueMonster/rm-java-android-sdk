package com.merchant.my.wxapi;

import com.merchant.my.R;
import com.revenuemonster.payment.EventListener;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;


public class WXEntryActivity extends EventListener {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Intent intent = getIntent();
        if(intent != null && intent.getData() != null) {
            Toast.makeText(this, "RESULT1: "+ intent.getData().toString(), Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onPaymentCallback(String resultCode) {
        Toast.makeText(this, "RESULT2: "+ resultCode, Toast.LENGTH_LONG).show();
    }
}