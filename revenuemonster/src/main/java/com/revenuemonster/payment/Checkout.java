package com.revenuemonster.payment;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import com.revenuemonster.payment.constant.Env;
import com.revenuemonster.payment.constant.Method;
import com.revenuemonster.payment.model.Transaction;
import com.revenuemonster.payment.util.Domain;
import com.revenuemonster.payment.util.HttpClient;
import com.tencent.mm.opensdk.modelbiz.WXOpenBusinessWebview;
import com.tencent.mm.opensdk.openapi.IWXAPI;
import com.tencent.mm.opensdk.openapi.WXAPIFactory;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Observable;

/**
 * Created by yussuf on 4/24/19.
 */

public class Checkout extends Observable {
    private Env env;
    private String checkoutCode;
    private String weChatAppID;
    private Method method;
    private Context context;
    private IWXAPI api;

    public Checkout(Context context,  String checkoutCode){
        this.context = context;
        this.checkoutCode=checkoutCode;
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
        String error;
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

    public void pay(final Method method, final PaymentResult result) {
        this.method = method;

        try {
            checkout c = new checkout(this.method, this.checkoutCode, this.env);
            Thread thread = new Thread(c);
            thread.start();
            thread.join();
            JSONObject response = c.response();
            if (!response.isNull("error")) {
                Toast.makeText(this.context, response.getJSONObject("error").get("message").toString(), Toast.LENGTH_LONG).show();
            } else {
                String url = response.getJSONObject("item").getString("url");

                switch (this.method) {
                    case WECHATPAY_MY:
                        String prepayID = url;
                        this.weChatPayMalaysia(prepayID);
                        break;
                    default:
                        Toast.makeText(this.context, "Invalid payment method", Toast.LENGTH_LONG).show();
                }
            }

            AsyncTask.execute(new Runnable() {
                @Override
                public void run() {
                    Boolean isLoop = true;

                    do {
                        try {
                            QueryOrder queryOrder = new QueryOrder(checkoutCode, env);
                            Thread queryOrderThread = new Thread(queryOrder);
                            queryOrderThread.start();
                            queryOrderThread.join();
                            JSONObject queryOrderResponse = queryOrder.response();
                            if (queryOrder.Error() != null) {
                                isLoop = false;
                                result.onPaymentFailed(queryOrder.Error());
                            } else if (queryOrder.isPaymentSuccess()) {
                                isLoop = false;
                                result.onPaymentSuccess(new Transaction(queryOrderResponse.getJSONObject("item")));
                            }
                        } catch(Exception e) {
                            Log.d("RM_CHECKOUT_ERROR", e.toString());
                        }
                    } while(isLoop);
                }
            });
        } catch(Exception e) {
            Toast.makeText(this.context, "System busy", Toast.LENGTH_LONG).show();
        }
    }

    private void boostMalaysia(String url) {
        Intent intent = new Intent (Intent.ACTION_VIEW);
        intent.setData (Uri.parse(url));
        this.context.startActivity(intent);
    }

    private void weChatPayMalaysia(String prepayID) {
        api = WXAPIFactory.createWXAPI(this.context, this.weChatAppID);

        WXOpenBusinessWebview.Req req = new WXOpenBusinessWebview.Req();
        req.businessType = 7;
        HashMap<String, String> queryInfo = new HashMap<>();
        queryInfo.put("prepay_id",prepayID);
        req.queryInfo = queryInfo;
        api.sendReq(req);
    }

}
