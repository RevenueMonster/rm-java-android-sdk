package com.revenuemonster.payment.model;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created by yussuf on 4/30/19.
 */

public class Transaction {
    private String key;
    private String id;
    private String merchantKey;
    private String storeKey;
    Order order;
    private String type;
    private String transactionId;
    private String platform;
    private ArrayList< String > method = new ArrayList < String > ();
    private String redirectUrl;
    private String notifyUrl;
    private String startAt;
    private String endAt;
    private String referenceKey;
    private String status;
    private String payload = null;
    private String createdAt;
    private String updatedAt;

    public Transaction(JSONObject o) throws Exception {
        try {
            this.setKey(o.getString("key"));
            this.setId(o.getString("id"));
            this.setMerchantKey(o.getString("merchantKey"));
            this.setStoreKey(o.getString("storeKey"));
            this.setOrder(new Order(o.getJSONObject("order")));
            this.setType(o.getString("type"));
            this.setTransactionId(o.getString("transactionId"));
            this.setPlatform(o.getString("platform"));
            this.setMethod(o.getJSONArray("method"));
            this.setRedirectUrl(o.getString("redirectUrl"));
            this.setStartAt(o.getString("startAt"));
            this.setEndAt(o.getString("endAt"));
            this.setReferenceKey(o.getString("referenceKey"));
            this.setStatus(o.getString("status"));
            this.setPayload(o.getString("payload"));
            this.setCreatedAt(o.getString("createdAt"));
            this.setUpdatedAt(o.getString("updatedAt"));
        } catch(Exception e) {
            throw e;
        }
    }


    // Getter Methods

    public String getKey() {
        return key;
    }

    public String getId() {
        return id;
    }

    public String getMerchantKey() {
        return merchantKey;
    }

    public String getStoreKey() {
        return storeKey;
    }

    public Order getOrder() {
        return order;
    }

    public String getType() {
        return type;
    }

    public String getTransactionId() {
        return transactionId;
    }

    public String getPlatform() {
        return platform;
    }

    public String getRedirectUrl() {
        return redirectUrl;
    }

    public String getNotifyUrl() {
        return notifyUrl;
    }

    public String getStartAt() {
        return startAt;
    }

    public String getEndAt() {
        return endAt;
    }

    public String getReferenceKey() {
        return referenceKey;
    }

    public String getStatus() {
        return status;
    }

    public String getPayload() {
        return payload;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public String getUpdatedAt() {
        return updatedAt;
    }

    // Setter Methods

    public void setKey(String key) {
        this.key = key;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setMerchantKey(String merchantKey) {
        this.merchantKey = merchantKey;
    }

    public void setStoreKey(String storeKey) {
        this.storeKey = storeKey;
    }

    public void setOrder(Order order) {
        this.order = order;
    }

    public void setMethod(JSONArray method) throws Exception {
        try {
            for (int i = 0; i < method.length(); i++) {
                this.method.add(method.getString(i));
            }
        } catch (Exception e) {
            throw e;
        }
    }

    public void setType(String type) {
        this.type = type;
    }

    public void setTransactionId(String transactionId) {
        this.transactionId = transactionId;
    }

    public void setPlatform(String platform) {
        this.platform = platform;
    }

    public void setRedirectUrl(String redirectUrl) {
        this.redirectUrl = redirectUrl;
    }

    public void setNotifyUrl(String notifyUrl) {
        this.notifyUrl = notifyUrl;
    }

    public void setStartAt(String startAt) {
        this.startAt = startAt;
    }

    public void setEndAt(String endAt) {
        this.endAt = endAt;
    }

    public void setReferenceKey(String referenceKey) {
        this.referenceKey = referenceKey;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public void setPayload(String payload) {
        this.payload = payload;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public void setUpdatedAt(String updatedAt) {
        this.updatedAt = updatedAt;
    }
}

