package com.revenuemonster.payment;

import android.app.Activity;
import android.app.Application;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.revenuemonster.payment.constant.Env;
import com.revenuemonster.payment.constant.Method;
import com.revenuemonster.payment.constant.Status;
import com.revenuemonster.payment.model.Transaction;
import com.revenuemonster.payment.util.Domain;
import com.revenuemonster.payment.util.HttpClient;
import com.tencent.mm.opensdk.modelbiz.WXOpenBusinessWebview;
import com.tencent.mm.opensdk.openapi.IWXAPI;
import com.tencent.mm.opensdk.openapi.WXAPIFactory;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.concurrent.TimeUnit;

/**
 * Created by yussuf on 4/24/19.
 */

public class Checkout implements Application.ActivityLifecycleCallbacks {
    private Env env;
    private String checkoutCode;
    private String weChatAppID;
    private Method method;
    private IWXAPI api;
    private static String status = "";
    private static Application application;
    private static Boolean isLoop = false;
    private static PaymentResult paymentResult;
    private static Checkout instance = null;

    @Override
    public void onActivityResumed(Activity activity) {
        this.isLoop = false;
        if(paymentResult != null && this.status.equalsIgnoreCase(Status.IN_PROCESS.toString())) {
            this.paymentResult.onPaymentCancelled();
        }
    }

    @Override
    public void onActivityPaused(Activity activity) {}

    @Override
    public void onActivityCreated(Activity activity, Bundle savedInstanceState) {}

    @Override
    public void onActivityStarted(Activity activity) {}

    @Override
    public void onActivityStopped(Activity activity) {}

    @Override
    public void onActivitySaveInstanceState(Activity activity, Bundle outState) {}

    @Override
    public void onActivityDestroyed(Activity activity) {}

    public Checkout(Application application){
        if (this.application == null) {
            this.application = application;
        }
    }

    public Checkout getInstance() {
        if (instance == null) {
            instance = new Checkout(this.application);
            this.application.registerActivityLifecycleCallbacks(this);
        }
        return this;
    }


    public Checkout setWeChatAppID(String appID) {
        this.weChatAppID = appID;
        return this;
    }

    public Checkout setEnv(Env env) {
        this.env = env;
        return this;
    }

    private class checkout implements Runnable {
        Env env;
        String checkoutCode;
        Method method;
        JSONObject response;

        private checkout(Method method, String checkoutCode, Env env) {
            this.checkoutCode = checkoutCode;
            this.method = method;
            this.env = env;
        }

        public void run() {
            try {
                JSONObject request = new JSONObject();
                request.put("method", method);
                request.put("code", this.checkoutCode);
                HttpClient client = new HttpClient();

                Log.d("RM_CHECKOUT_REQUEST", request.toString());
                this.response = client.request(new Domain(env).getPaymentGatewayURL()+"/v1/transaction/mobile", "POST", request.toString());
                Log.d("RM_CHECKOUT_RESPONSE", response.toString());
            } catch (Exception e) {
                Log.e("RM_CHECKOUT_ERROR", e.getMessage());
            }
        }

        public JSONObject response() {
            return this.response;
        }
    }

    public void pay(final Method method, String checkoutId, final PaymentResult result) throws Exception {
        this.method = method;
        this.checkoutCode = checkoutId;
        this.paymentResult = result;

        try {
            checkout c = new checkout(this.method, this.checkoutCode, this.env);
            Thread thread = new Thread(c);
            thread.start();
            thread.join();
            JSONObject response = c.response();
            if (!response.isNull("error")) {
                Toast.makeText(this.application, response.getJSONObject("error").get("message").toString(), Toast.LENGTH_LONG).show();
            } else {
                String url = response.getJSONObject("item").getString("url");
                this.isLoop = true;
                switch (this.method) {
                    case WECHATPAY_MY:
                        String prepayID = url;
                        this.weChatPayMalaysia(prepayID);
                        break;
                    default:
                        Toast.makeText(this.application, "Invalid payment method", Toast.LENGTH_LONG).show();
                }
            }

            AsyncTask.execute(new Runnable() {
                @Override
                public void run() {
                    do {
                        try {
                            TimeUnit.SECONDS.sleep(2);
                            QueryOrder queryOrder = new QueryOrder(checkoutCode, env);
                            Thread queryOrderThread = new Thread(queryOrder);
                            queryOrderThread.start();
                            queryOrderThread.join();
                            JSONObject queryOrderResponse = queryOrder.response();
                            status = queryOrder.getTransactionStatus();
                            if (queryOrder.isPaymentSuccess()) {
                                isLoop = false;
                                paymentResult.onPaymentSuccess(new Transaction(queryOrderResponse.getJSONObject("item")));
                            } else if (queryOrder.Error() != null) {
                                isLoop = false;
                                paymentResult.onPaymentFailed(queryOrder.Error());
                            }
                        } catch(Exception e) {
                            isLoop = false;
                            Toast.makeText(application, "System busy", Toast.LENGTH_LONG).show();
                        }
                    } while(isLoop);
                }
            });
        } catch(Exception e) {
            throw e;
        }
    }

    private void boostMalaysia(String url) {
        Intent intent = new Intent (Intent.ACTION_VIEW);
        intent.setData (Uri.parse(url));
        this.application.startActivity(intent);
    }

    private void weChatPayMalaysia(String prepayID) {
        api = WXAPIFactory.createWXAPI(this.application, this.weChatAppID);

        WXOpenBusinessWebview.Req req = new WXOpenBusinessWebview.Req();
        req.businessType = 7;
        HashMap<String, String> queryInfo = new HashMap<>();
        queryInfo.put("prepay_id",prepayID);
        req.queryInfo = queryInfo;
        api.sendReq(req);
    }

}
