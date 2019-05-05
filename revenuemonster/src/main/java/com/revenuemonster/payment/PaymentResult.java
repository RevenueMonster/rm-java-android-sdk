package com.revenuemonster.payment;

import com.revenuemonster.payment.model.Error;
import com.revenuemonster.payment.model.Transaction;

/**
 * Created by yussuf on 4/30/19.
 */

public interface PaymentResult {
    void onPaymentSuccess(Transaction transaction);
    void onPaymentFailed(Error error);
    void onPaymentCancelled();
}
