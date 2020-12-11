# RM SDK for Android

[![](https://jitpack.io/v/RevenueMonster/RM-Android.svg)](https://jitpack.io/#RevenueMonster/RM-Android)


<!-- For more details check out the [documentation]() -->

Add it to your build.gradle with:
```gradle
allprojects {
    repositories {
        maven { url "https://jitpack.io" }
    }
}
```
and:

```gradle
dependencies {
    compile 'com.github.RevenueMonster:RM-Android:{latest version}'
}
```
<br/>
<br/>

### Checkout Sample Code
```java
try {
	new Checkout(MainActivity.this).getInstance().
	setWeChatAppID("<< WeChat Open Platform AppID >>"). // only use for wechatpay
	setEnv(Env.SANDBOX).
	setCardInfo("<<Card Holder Name>>","<<Card No>>","<<Cvc No>>","<<Exp Month>>","<<Exp Year>>","<<Country Code>>","<<Card Save>>"). // only use for new card 
	setToken("<<Token>>","<<Cvc No>>"). // only use if use existing card token
	setBankCode("<<Set Bank Code>>"). // only use for fpx, get the bank code from api
	pay(Method.WECHATPAY_MY, "<<Get Checkout Id from API>>", new Result());
} catch(Exception e) {
	e.printStackTrace();
}

// Callback Result
static public class Result implements PaymentResult {
	public void onPaymentSuccess(Transaction transaction) {
		Log.d("SUCCESS", transaction.getStatus());
	}
	public void onPaymentFailed(Error error) {
		Log.d("FAILED", error.getCode());
	}
	public void onPaymentCancelled() {
		Log.d("CANCELLED", "User cancelled payment");
	}
}
```
### Environment
- SANDBOX      
- PRODUCTION
<br/>
<br/>
### Method
- WECHATPAY_MY
- TNG_MY
- BOOST_MY
- ALIPAY_CN
- GRABPAY_MY
- MCASH_MY
- RAZERPAY_MY
- PRESTO_MY
- GOBIZ_MY
- FPX_MY
