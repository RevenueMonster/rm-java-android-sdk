package com.merchant.my;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.app.Activity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.revenuemonster.payment.Checkout;
import com.revenuemonster.payment.PaymentResult;
import com.revenuemonster.payment.constant.Env;
import com.revenuemonster.payment.constant.Method;
import com.revenuemonster.payment.model.Error;
import com.revenuemonster.payment.model.Transaction;
import com.revenuemonster.payment.util.HttpClient;

import org.json.JSONObject;

public class MainActivity extends Activity implements PaymentResult {
    JSONObject response;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setContentView(R.layout.activity_main);
        super.onCreate(savedInstanceState);
        Intent intent = getIntent();
        Button button = (Button) findViewById(R.id.rmPay);




        button.setOnClickListener(new View.OnClickListener()
        {
            public void onClick(View v)
            {
                try {
                    AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                    builder.setTitle("Payment Method")
                            .setItems(new String[]{"WeChatPay MY", "TNG", "Boost", "AliPay CN", "GrabPay"}, new DialogInterface.OnClickListener() {
                                String checkoutID = getCheckoutID();

                                String weChatAppID = "wx62173edb65003c7c";
                                Method method;
                                public void onClick(DialogInterface dialog, int which) {
                                    switch (which) {
                                        case 0:
                                            method = Method.WECHATPAY_MY;
                                            break;
                                        case 1:
                                            method = Method.TNG_MY;
                                            break;
                                        case 2:
                                            method = Method.BOOST_MY;
                                            break;
                                        case 3:
                                            method = Method.ALIPAY_CN;
                                            break;
                                        case 4:
                                            method = Method.GRABPAY_MY;
                                            break;
                                    }
                                    try {
                                        new Checkout(MainActivity.this).getInstance().setWeChatAppID(weChatAppID).
                                                setEnv(Env.SANDBOX).
                                                pay(method, checkoutID, MainActivity.this);
                                    } catch(Exception e) {
                                        e.printStackTrace();
                                    }
                                }
                            });
                    builder.show();
               } catch(Exception e) {
                   e.printStackTrace();
               }
            }
        });
    }

    private String getCheckoutID() {
        String checkoutID = "";
        try {
            checkout c = new checkout();
            Thread thread = new Thread(c);
            thread.start();
            thread.join();
            checkoutID = c.getCheckOutID();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return checkoutID;
    }

    private class checkout implements Runnable {
        JSONObject response;

        public void run()  {
            try {
                JSONObject request = new JSONObject();
                request.put("type", "MOBILE_PAYMENT");
                request.put("redirectURL", "revenuemonster://test");
                request.put("notifyURL", "https://dev-rm-api.ap.ngrok.io");

                HttpClient client = new HttpClient();
                Log.d("RM_CHECKOUT_REQUEST", request.toString());
                this.response = client.request("https://sb-api.revenuemonster.my/demo/payment/online", "POST", request.toString());
                Log.d("RM_CHECKOUT_RESPONSE", response.toString());
            } catch (Exception e) {
                Log.e("RM_CHECKOUT_ERROR", e.toString());
            }
        }

        public String getCheckOutID() throws Exception {
            try {
                if (!this.response.isNull("item")) {
                    String checkoutId =  this.response.getJSONObject("item").get("checkoutId").toString();
                    return checkoutId;
                }
            } catch(Exception e) {
                Log.e("RM_PAYMENT_FLAG_ERROR", e.toString());
                throw e;
            }

            return "";
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    public void onPaymentSuccess(Transaction transaction) {
        Log.d("SUCCESS", "onPaymentSuccess");
        Toast toast=Toast.makeText(getApplicationContext(),"Payment Success",Toast.LENGTH_SHORT);
        toast.show();
    }
    public void onPaymentFailed(Error error) {
        Log.d("FAILED", error.getMessage());
        Toast toast=Toast.makeText(getApplicationContext(),"Payment Failed",Toast.LENGTH_SHORT);
        toast.show();
    }
    public void onPaymentCancelled() {
        Log.d("CANCELLED", "onPaymentCancelled");

        Toast toast=Toast.makeText(getApplicationContext(),"Payment Cancelled",Toast.LENGTH_SHORT);
        toast.show();
    }
}




