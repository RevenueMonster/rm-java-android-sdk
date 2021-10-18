package com.merchant.my;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.app.Activity;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.revenuemonster.payment.Checkout;
import com.revenuemonster.payment.PaymentResult;
import com.revenuemonster.payment.constant.Env;
import com.revenuemonster.payment.constant.Method;
import com.revenuemonster.payment.model.Error;
import com.revenuemonster.payment.model.Transaction;
import com.revenuemonster.payment.util.HttpClient;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends Activity implements PaymentResult {
    JSONObject response;
    int paymentMethod;
    int cardSelected;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setContentView(R.layout.activity_main);
        super.onCreate(savedInstanceState);

        final EditText cardName = (EditText) findViewById(R.id.card_name);
        final EditText cardNo = (EditText) findViewById(R.id.card_no);
        final EditText cvc = (EditText) findViewById(R.id.cvc);
        final EditText expDate = (EditText) findViewById(R.id.exp_date);
        final CheckBox saveCard = (CheckBox) findViewById(R.id.save_card);
        final Spinner selectCard = (Spinner) findViewById(R.id.card_selection);

        Button button = (Button) findViewById(R.id.pay_now);
        button.setOnClickListener(new View.OnClickListener()
        {
            public void onClick(View v)
            {
                try {
                    Checkout c = new Checkout(MainActivity.this).getInstance().setEnv(Env.SANDBOX);
                    String checkoutID = getCheckoutID();
                    String weChatAppID = "";
                    Method method;
                        switch (paymentMethod) {
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
                            case 5:
                                c = c.setWeChatAppID(weChatAppID);
                                method = Method.WECHATPAY_MY;
                                break;
                            case 6:
                                method = Method.MCASH_MY;
                                break;
                            case 7:
                                method = Method.RAZERPAY_MY;
                                break;
                            case 8:
                                method = Method.PRESTO_MY;
                                break;
                            case 9:
                                if (selectCard.getSelectedItemPosition() == 0) {
                                    if (expDate.getText().toString().length() == 5) {
                                        int expMonth = Integer.parseInt(expDate.getText().toString().substring(0, 2));
                                        int expYear = Integer.parseInt(20 + expDate.getText().toString().substring(3, 5));

                                        c = c.setCardInfo(cardName.getText().toString(), cardNo.getText().toString(), cvc.getText().toString(), expMonth, expYear, "MY", saveCard.isChecked());
                                    }
                                } else {
                                    c = c.setToken(cardNo.getText().toString(), cvc.getText().toString());
                                }

                                method = Method.GOBIZ_MY;
                                break;
                            case 10:
                                c = c.setBankCode("TEST");
                                method = Method.FPX_MY;
                                break;

                            case 11:
                                method = Method.SHOPEEPAY_MY;
                                break;

                            case 12:
                                method = Method.ZAPP_MY;
                                break;

                            case 13:
                                method = Method.SENHENGPAY_MY;
                                break;

                            case 14:
                                method = Method.PAYDEE_MY;
                                break;

                            case 15:
                                method = Method.ALIPAYPLUS_MY;
                                break;

                            default:
                                return;
                        }
                        c.pay(method, checkoutID, MainActivity.this);
               } catch(Exception e) {
                   e.printStackTrace();
               }
            }
        });


        Spinner methodSpinner = (Spinner) findViewById(R.id.method);
        methodSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                paymentMethod = position;

                if (paymentMethod == 9 && cardSelected == 0) {
                    cardNo.setHint("Card No");
                    cardNo.setInputType(InputType.TYPE_CLASS_NUMBER);
                    selectCard.setVisibility(View.VISIBLE);
                    cardName.setVisibility(View.VISIBLE);
                    cardNo.setVisibility(View.VISIBLE);
                    cvc.setVisibility(View.VISIBLE);
                    expDate.setVisibility(View.VISIBLE);
                    saveCard.setVisibility(View.VISIBLE);
                    selectCard.setVisibility(View.VISIBLE);
                } else if (paymentMethod == 9 && cardSelected == 1) {
                    cardNo.setHint("Token");
                    cardNo.setInputType(InputType.TYPE_CLASS_TEXT);
                    cardNo.setVisibility(View.VISIBLE);
                    cvc.setVisibility(View.VISIBLE);
                } else {
                    cardName.setVisibility(View.INVISIBLE);
                    cardNo.setVisibility(View.INVISIBLE);
                    cvc.setVisibility(View.INVISIBLE);
                    expDate.setVisibility(View.INVISIBLE);
                    saveCard.setVisibility(View.INVISIBLE);
                    selectCard.setVisibility(View.INVISIBLE);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        List<String> methods = new ArrayList<>();
        methods.add("Select Payment Method");
        methods.add("Touch N Go");
        methods.add("Boost");
        methods.add("AliPay");
        methods.add("GrabPay");
        methods.add("WeChatPay MY");
        methods.add("MCash");
        methods.add("RazerPay");
        methods.add("PrestoPay");
        methods.add("GoBiz");
        methods.add("Online Banking");
        methods.add("ShopeePay");
        methods.add("Zapp");
        methods.add("SenHeng");
        methods.add("Paydee");
        methods.add("AliPay+");

        ArrayAdapter<String> methodAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, methods);
        methodAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        methodSpinner.setAdapter(methodAdapter);

        Spinner cardSpinner = (Spinner) findViewById(R.id.card_selection);
        cardSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                cardSelected = position;

                if (paymentMethod == 9 && cardSelected == 0) {
                    cardNo.setHint("Card No");
                    cardNo.setInputType(InputType.TYPE_CLASS_NUMBER);
                    cardName.setVisibility(View.VISIBLE);
                    cardNo.setVisibility(View.VISIBLE);
                    cvc.setVisibility(View.VISIBLE);
                    expDate.setVisibility(View.VISIBLE);
                    saveCard.setVisibility(View.VISIBLE);
                    selectCard.setVisibility(View.VISIBLE);
                } else if (paymentMethod == 9 && cardSelected == 1) {
                    cardNo.setHint("Token");
                    cardNo.setInputType(InputType.TYPE_CLASS_TEXT);
                    cardNo.setVisibility(View.VISIBLE);
                    cvc.setVisibility(View.VISIBLE);
                    selectCard.setVisibility(View.VISIBLE);
                    cardName.setVisibility(View.INVISIBLE);
                    expDate.setVisibility(View.INVISIBLE);
                    saveCard.setVisibility(View.INVISIBLE);
                } else {
                    cardName.setVisibility(View.INVISIBLE);
                    cardNo.setVisibility(View.INVISIBLE);
                    cvc.setVisibility(View.INVISIBLE);
                    expDate.setVisibility(View.INVISIBLE);
                    saveCard.setVisibility(View.INVISIBLE);
                    selectCard.setVisibility(View.INVISIBLE);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        List<String> cards = new ArrayList<>();
        cards.add("New Card");
        cards.add("Card Token");

        ArrayAdapter<String> cardAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, cards);
        cardAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        cardSpinner.setAdapter(cardAdapter);
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
                request.put("amount", 120);

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




