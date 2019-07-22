package com.revenuemonster.payment;

import android.util.Log;

import com.revenuemonster.payment.constant.Env;
import com.revenuemonster.payment.model.Error;
import com.revenuemonster.payment.util.Domain;
import com.revenuemonster.payment.util.HttpClient;

import org.json.JSONObject;

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
            request.put("clearCache", true);
            HttpClient client = new HttpClient();
            Log.d("RM_QUERY_REQUEST", request.toString());
            this.response = client.request(new Domain(this.env).getPaymentGatewayURL()+"/v1/online/transaction/status", "POST", request.toString());
            Log.d("RM_QUERY_RESPONSE", response.toString());
        } catch (Exception e) {
            Log.e("RM_QUERY_ERROR", e.toString());
        }
    }

    public Boolean isPaymentSuccess() throws Exception {
        try {
            if (!this.response.isNull("item")) {
                String status =  this.response.getJSONObject("item").get("status").toString();
                switch (status) {
                    case "SUCCESS":
                        return true;
                }
            }
        } catch(Exception e) {
            Log.e("RM_PAYMENT_FLAG_ERROR", e.toString());
            throw e;
        }

        return false;
    }

    public String getTransactionStatus() throws Exception {
        try {
            if (!this.response.isNull("item")) {
                String status =  this.response.getJSONObject("item").get("status").toString();
                return status;
            }
        } catch(Exception e) {
            Log.e("RM_PAYMENT_FLAG_ERROR", e.toString());
            throw e;
        }

        return "";
    }


    public Error Error() throws Exception {
        try {
            if (!this.response.isNull("error")) {
                JSONObject error = this.response.getJSONObject("error");
                return new Error(error.getString("code"), error.getString("message"));
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
