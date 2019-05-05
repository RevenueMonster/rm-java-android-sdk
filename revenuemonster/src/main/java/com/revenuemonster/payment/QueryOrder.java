package com.revenuemonster.payment;

import android.util.Log;

import com.revenuemonster.payment.constant.Env;
import com.revenuemonster.payment.model.Error;
import com.revenuemonster.payment.util.Domain;
import com.revenuemonster.payment.util.HttpClient;

import org.json.JSONObject;

import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by yussuf on 4/29/19.
 */

public class QueryOrder implements Runnable {
    private String checkoutCode;
    private Env env;
    JSONObject response;

    public QueryOrder(String checkoutCode, Env env) {
        this.env = env;
        this.checkoutCode = checkoutCode;
    }

    public void run() {
        try {
            JSONObject request = new JSONObject();
            request.put("code", this.checkoutCode);
            HttpClient client = new HttpClient();
            Log.d("RM_QUERY_REQUEST", request.toString());
            this.response = client.request(new Domain(this.env).getPaymentGatewayURL()+"/v1/online/transaction/status", "POST", request.toString());
            Log.d("RM_QUERY_RESPONSE", response.toString());
        } catch (Exception e) {
            Log.e("RM_QUERY_ERROR", e.toString());
        }
    }

    public Boolean isPaymentSuccess() {
        try {
            if (this.response.getJSONObject("item") != null) {
                String status =  this.response.getJSONObject("item").get("status").toString();
                switch (status) {
                    case "SUCCESS":
                        return true;
                }
            }
        } catch(Exception e) {
            Log.e("RM_PAYMENT_FLAG_ERROR", e.toString());
        }

        return false;
    }


    public Error Error() throws Exception {
        try {
            if (this.response.getJSONObject("error") != null) {
                return new Error(this.response.getJSONObject("error"));
            }
        } catch(Exception e) {
            throw e;
        }

        return null;
    }

    public JSONObject response() {
        return this.response;
    }
}
