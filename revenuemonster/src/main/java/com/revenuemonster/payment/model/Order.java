package com.revenuemonster.payment.model;

import org.json.JSONObject;

/**
 * Created by yussuf on 4/30/19.
 */

public class Order {
    private String id;
    private String title;
    private String detail;
    private String additionalData;
    private String currencyType;
    private float amount;

    public Order(JSONObject o) throws Exception {
        try {
            this.setId(o.getString("id"));
            this.setTitle(o.getString("title"));
            this.setDetail(o.getString("detail"));
            this.setAdditionalData(o.getString("additionalData"));
            this.setCurrencyType(o.getString("currencyType"));
            this.setAmount((float)o.getDouble("amount"));
        } catch(Exception e) {
            throw e;
        }
    }

    // Getter Methods

    public String getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getDetail() {
        return detail;
    }

    public String getAdditionalData() {
        return additionalData;
    }

    public String getCurrencyType() {
        return currencyType;
    }

    public float getAmount() {
        return amount;
    }

    // Setter Methods

    public void setId(String id) {
        this.id = id;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setDetail(String detail) {
        this.detail = detail;
    }

    public void setAdditionalData(String additionalData) {
        this.additionalData = additionalData;
    }

    public void setCurrencyType(String currencyType) {
        this.currencyType = currencyType;
    }

    public void setAmount(float amount) {
        this.amount = amount;
    }
}
