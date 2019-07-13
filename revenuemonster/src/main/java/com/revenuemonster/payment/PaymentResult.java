package com.revenuemonster.payment;

import com.revenuemonster.payment.model.Error;
import com.revenuemonster.payment.model.Transaction;

import java.io.Serializable;

/**
 * Created by yussuf on 4/30/19.
 */

public interface PaymentResult extends Serializable {
    void onPaymentSuccess(Transaction transaction);
    void onPaymentFailed(Error error);
    void onPaymentCancelled();
}
