package com.revenuemonster.payment;

import android.app.Activity;
import android.app.Application;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import com.alipay.sdk.app.PayTask;
import com.revenuemonster.payment.constant.Env;
import com.revenuemonster.payment.constant.Method;
import com.revenuemonster.payment.constant.PackageName;
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
    private HashMap<String, Object> card;
    private Method method;
    private String bankCode;
    private IWXAPI api;
    private static Boolean isAppInstalled = false;
    private static String checkOutID = "";
    private static String status = "";
    private static Application application;
    private static Activity activity;
    private static Boolean isLeaveApp = false;
    private static PaymentResult paymentResult;
    private static Checkout instance = null;

    @Override
    public void onActivityResumed(Activity activity) {
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
                e.printStackTrace();
                paymentResult.onPaymentFailed(Error.SYSTEM_BUSY);
            }
        } else {
            this.isLeaveApp = true;
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

    public Checkout(Activity activity){
        if (this.application == null) {
            this.activity = activity;
            this.application = activity.getApplication();
        }
    }

    public Checkout getInstance() {
        if (instance == null) {
            instance = new Checkout(this.activity);
            this.application.registerActivityLifecycleCallbacks(this);
        }
        return this;
    }

    public Checkout setToken(String token, String cvcNo) {
        this.card = new HashMap<String, Object>();
        this.card.put("isToken", true);
        this.card.put("no", token);
        this.card.put("cvc", cvcNo);
        return this;
    }

    public Checkout setBankCode(String bankCode) {
        this.bankCode = bankCode;
        return this;
    }

    public Checkout setWeChatAppID(String appID) {
        this.weChatAppID = appID;
        return this;
    }

    public Checkout setCardInfo(String name, String cardNo, String cvcNo, int expMonth, int expYear, String countryCode, Boolean isSave) {
        this.card = new HashMap<String, Object>();
        this.card.put("name", name);
        this.card.put("no", cardNo);
        this.card.put("month", expMonth);
        this.card.put("year", expYear);
        this.card.put("cvc", cvcNo);
        this.card.put("countryCode", countryCode);
        this.card.put("isSave", isSave);
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
        String bankCode;
        JSONObject response;
        HashMap<String, Object> card;

        private checkout(Method method, String code, Env env, HashMap<String, Object> card, String bankCode)  {
            this.code = code;
            this.method = method;
            this.env = env;
            this.card = card;
            this.bankCode = bankCode;
        }

        public void run()  {
            try {
                JSONObject request = new JSONObject();
                request.put("method", method);
                request.put("code", code);
                request.put("isAppInstalled",  isAppInstalled);
                request.put("bankCode", bankCode);
                if (card != null) {
                    request.put("card", new JSONObject(card));
                }
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
        this.isAppInstalled = new PackageName(this.activity, this.env).isInstalled(method);

        try {
            checkout c = new checkout(this.method, this.checkOutID, this.env, this.card, this.bankCode);
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

                    case ALIPAY_CN:
                        try {
                            String prepayID = url;
                            this.alipayChina(prepayID);
                        } catch(Exception e) {
                            paymentResult.onPaymentFailed(new Error(Status.FAILED.toString(), e.getMessage()));
                            return;
                        }
                        break;

                    case GRABPAY_MY:
                    case TNG_MY:
                    case MCASH_MY:
                    case RAZERPAY_MY:
                    case PRESTO_MY:
                    case GOBIZ_MY:
                    case FPX_MY:
                    case SHOPEEPAY_MY:
                    case ZAPP_MY:
                    case PAYDEE_MY:
                    case ALIPAYPLUS_MY:
                    case SENHENGPAY_MY:
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
        if (!this.isAppInstalled || url.contains("https://")) {
            this.openBrowser(url);
            return;
        }

        Intent intent = new Intent (Intent.ACTION_VIEW);
        intent.setData(Uri.parse(url));
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        this.isLeaveApp = true;
        this.application.startActivity(intent);
    }

    private void weChatPayMalaysia(String prepayID) throws Exception {
        try {
            if (this.weChatAppID == "") {
                paymentResult.onPaymentFailed(Error.WECHAT_APP_IS_REQUIRED);
                return;
            }

            api = WXAPIFactory.createWXAPI(this.application, this.weChatAppID);
            if (!api.isWXAppInstalled()) {
                paymentResult.onPaymentFailed(Error.WECHAT_APP_NOT_INSTALLED);
                return;
            }
            this.isLeaveApp = true;

            final String orderData = prepayID;

            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        WXOpenBusinessWebview.Req req = new WXOpenBusinessWebview.Req();
                        req.businessType = 7;
                        HashMap<String, String> queryInfo = new HashMap<>();
                        queryInfo.put("prepay_id", orderData);
                        req.queryInfo = queryInfo;
                        api.sendReq(req);
                    } catch(Exception e) {
                        throw e;
                    }
                }
            }).start();
        } catch(Exception e) {
            e.printStackTrace();
            throw new Exception("System busy");
        }
    }

    private void alipayChina(String prepayID) throws Exception {
        try {
            if (!this.isAppInstalled || prepayID.contains("https://")) {
                this.openBrowser(prepayID);
                return;
            }

            // if (!this.env.equals(Env.PRODUCTION)) {
            //     EnvUtils.setEnv(EnvUtils.EnvEnum.SANDBOX);
            // }

            byte[] data = Base64.decode(prepayID, Base64.DEFAULT);
            this.isLeaveApp = false;

            final String orderData = new String(data);

            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        PayTask alipay = new PayTask(activity);
                        alipay.payV2(orderData,false);
                    } catch(Exception e) {
                        throw e;
                    }
                }
            }).start();
        } catch(Exception e) {
            throw e;
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
