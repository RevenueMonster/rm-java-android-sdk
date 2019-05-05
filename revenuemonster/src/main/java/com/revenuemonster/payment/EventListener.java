package com.revenuemonster.payment;

import com.tencent.mm.opensdk.modelbase.BaseReq;
import com.tencent.mm.opensdk.modelbase.BaseResp;
import com.tencent.mm.opensdk.modelbiz.WXOpenBusinessWebview;
import com.tencent.mm.opensdk.openapi.IWXAPI;
import com.tencent.mm.opensdk.openapi.IWXAPIEventHandler;
import com.tencent.mm.opensdk.openapi.WXAPIFactory;

import android.app.Activity;
import android.arch.lifecycle.Lifecycle;
import android.arch.lifecycle.LifecycleObserver;
import android.arch.lifecycle.LifecycleOwner;
import android.arch.lifecycle.OnLifecycleEvent;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

public class EventListener extends Activity implements IWXAPIEventHandler {
    private IWXAPI api;

    @Override
    public  void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        api = WXAPIFactory.createWXAPI(this, "", false);
        try {
            api.handleIntent(getIntent(), this);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);

        setIntent(intent);
        api.handleIntent(intent, this);
    }

    @Override
    public  void onReq(BaseReq req) {
        // Do nothing
    }

    @Override
    public void onResp(BaseResp resp) {
        String result = "";
        switch (resp.errCode) {
            case BaseResp.ErrCode.ERR_OK:
                result = "SUCCESS";
                break;
            case BaseResp.ErrCode.ERR_USER_CANCEL:
                result = "USER_CANCELLED";
                break;
            case BaseResp.ErrCode.ERR_AUTH_DENIED:
                result = "DENIED";
                break;
            case BaseResp.ErrCode.ERR_UNSUPPORT:
                result = "UNSUPPORTED";
                break;
            default:
                result = "UNKNOWN";
                break;
        }

        finish();
    }

    public void onPaymentCallback(String result) {}

    @Override
    protected void onDestroy() {
        super.onDestroy();
        api.detach();
    }
}