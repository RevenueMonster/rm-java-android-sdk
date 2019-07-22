package com.revenuemonster.payment;

import android.app.Activity;
import android.app.Application;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;

import com.revenuemonster.payment.constant.Env;
import com.revenuemonster.payment.constant.Method;
import com.revenuemonster.payment.constant.Status;
import com.revenuemonster.payment.model.Error;
import com.revenuemonster.payment.model.Transaction;
import com.revenuemonster.payment.util.Domain;
import com.revenuemonster.payment.util.HttpClient;
import com.revenuemonster.payment.view.BrowserActivity;
import com.tencent.mm.opensdk.modelbiz.WXOpenBusinessWebview;
import com.tencent.mm.opensdk.openapi.IWXAPI;
import com.tencent.mm.opensdk.openapi.WXAPIFactory;

import org.json.JSONObject;

import java.util.HashMap;

/**
 * Created by yussuf on 4/24/19.
 */

public class Checkout implements Application.ActivityLifecycleCallbacks {
    private Env env;
    private String weChatAppID;
    private Method method;
    private IWXAPI api;
    private static String checkOutID = "";
    private static String status = "";
    private static Application application;
    private static Boolean isLoop = false;
    private static Boolean isLeaveApp = false;
    private static PaymentResult paymentResult;
    private static Checkout instance = null;

    @Override
    public void onActivityResumed(Activity activity) {
        this.isLoop = false;
        if(paymentResult != null && this.status.equalsIgnoreCase(Status.IN_PROCESS.toString()) && this.isLeaveApp) {
            try {
                this.isLeaveApp = false;
                QueryOrder queryOrder = new QueryOrder(this.checkOutID, env);
                Thread queryOrderThread = new Thread(queryOrder);
                queryOrderThread.start();
                queryOrderThread.join();
                JSONObject queryOrderResponse = queryOrder.response();
                status = queryOrder.getTransactionStatus();
                if (queryOrder.isPaymentSuccess()) {
                    paymentResult.onPaymentSuccess(new Transaction(queryOrderResponse.getJSONObject("item")));
                } else if (queryOrder.Error() != null) {
                    paymentResult.onPaymentFailed(queryOrder.Error());
                } else {
                    paymentResult.onPaymentCancelled();
                }
            } catch (Exception e) {
                paymentResult.onPaymentFailed(Error.SYSTEM_BUSY);
            }
        }
    }

    @Override
    public void onActivityPaused(Activity activity) {}

    @Override
    public void onActivityCreated(Activity activity, Bundle savedInstanceState) {}

    @Override
    public void onActivityStarted(Activity activity) {}

    @Override
    public void onActivityStopped(Activity activity) {
        if (this.isLeaveApp) {
            this.isLeaveApp = false;
        } else {
            this.isLeaveApp = true;
        }
    }

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
        String code;
        Method method;
        JSONObject response;

        private checkout(Method method, String code, Env env)  {
            this.code = code;
            this.method = method;
            this.env = env;
        }

        public void run()  {
            try {
                JSONObject request = new JSONObject();
                request.put("method", method);
                request.put("code", this.code);
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

    public void pay(final Method method, String code, final PaymentResult result) throws Exception {
        this.method = method;
        this.checkOutID = code;
        this.paymentResult = result;

        try {
            checkout c = new checkout(this.method, this.checkOutID, this.env);
            Thread thread = new Thread(c);
            thread.start();
            thread.join();

            JSONObject response = c.response();
            if (response == null) {
                paymentResult.onPaymentFailed(Error.SYSTEM_BUSY);
                return;
            } else if (!response.isNull("error")) {
                JSONObject error = response.getJSONObject("error");
                paymentResult.onPaymentFailed(new Error(error.getString("code"), error.getString("message")));
                return;
            } else {
                QueryOrder queryOrder = new QueryOrder(this.checkOutID, env);
                Thread queryOrderThread = new Thread(queryOrder);
                queryOrderThread.start();
                queryOrderThread.join();
                JSONObject queryOrderResponse = queryOrder.response();
                status = queryOrder.getTransactionStatus();
                if (queryOrder.isPaymentSuccess()) {
                    paymentResult.onPaymentSuccess(new Transaction(queryOrderResponse.getJSONObject("item")));
                    return;
                } else if (queryOrder.Error() != null) {
                    paymentResult.onPaymentFailed(queryOrder.Error());
                    return;
                }

                String url = response.getJSONObject("item").getString("url");
                this.isLoop = true;
                switch (this.method) {
                    case WECHATPAY_MY:
                        try {
                            String prepayID = url;
                            this.weChatPayMalaysia(prepayID);
                        } catch(Exception e) {
                            paymentResult.onPaymentFailed(new Error(Status.FAILED.toString(), e.getMessage()));
                            return;
                        }
                        break;

                    case GRABPAY_MY:
                    case ALIPAY_CN:
                    case TNG_MY:
                        this.openBrowser(url);
                        break;

                    case BOOST_MY:
                        this.openURL(url);
                        break;

                    default:
                        paymentResult.onPaymentFailed(Error.INVALID_PAYMENT_METHOD);
                        return;
                }
            }
        } catch(Exception e) {
            e.printStackTrace();
            paymentResult.onPaymentFailed(Error.SYSTEM_BUSY);
            throw e;
        }
    }

    private void openURL(String url) {
        Intent intent = new Intent (Intent.ACTION_VIEW);
        intent.setData(Uri.parse(url));
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        this.isLeaveApp = false;
        this.application.startActivity(intent);
    }

    private void weChatPayMalaysia(String prepayID) throws Exception {
        try {
            api = WXAPIFactory.createWXAPI(this.application, this.weChatAppID);
            if (!api.isWXAppInstalled()) {
                throw new Exception("WeChat app is not installed");
            }

            WXOpenBusinessWebview.Req req = new WXOpenBusinessWebview.Req();
            req.businessType = 7;
            HashMap<String, String> queryInfo = new HashMap<>();
            queryInfo.put("prepay_id",prepayID);
            req.queryInfo = queryInfo;
            this.isLeaveApp = false;
            api.sendReq(req);
        } catch(Exception e) {
            e.printStackTrace();
            throw new Exception("System busy");
        }
    }

    public void openBrowser(String url) {
        Intent intent = new Intent(this.application, BrowserActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        Bundle b = new Bundle();
        b.putString("url", url);
        intent.putExtras(b);
        this.isLeaveApp = false;
        this.application.startActivity(intent);
    }

}
