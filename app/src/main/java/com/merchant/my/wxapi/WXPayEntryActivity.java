package com.merchant.my.wxapi;

import com.merchant.my.R;
import com.revenuemonster.payment.EventListener;

import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

public class WXPayEntryActivity extends EventListener {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d("RESPONSEAPI_4", "onPaymentCallback: ");

        setContentView(R.layout.activity_main);
    }

    @Override
    public void onPaymentCallback(String resultCode) {
        Toast.makeText(this, "RESULT2: "+ resultCode, Toast.LENGTH_LONG).show();
    }
}