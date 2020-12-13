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
		setEnv(<<Environment Parameter>>). // set environment
		setWeChatAppID("<< WeChat Open Platform AppID >>"). // only use for wechatpay
		setCardInfo("<<Card Holder Name>>","<<Card No>>","<<Cvc No>>","<<Exp Month>>","<<Exp Year>>","<<Country Code>>","<<Card Save>>"). // only use for new card 
		setToken("<<Card Token>>","<<Cvc No>>"). // only use if use existing card token
		setBankCode("<<Set Bank Code>>"). // only use for fpx, get the bank code from open api
		pay(<<Method Parameter>>,"<<Get Checkout Id from API>>", new Result());
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
<br />

### Environment Parameter
- SANDBOX      
- PRODUCTION
<br/>
<br/>

### Method Parameter
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


## WeChatPay In-App Payment


#### 1. Create a developer account on the WeChat Open Platform:
- Go to https://open.weixin.qq.com/ and click Log In.
- Navigate to Admin Center > Mobile Application > Create Mobile Application, and input name, short introduction, official website, and package name.

<br>

#### 2. Pass the app id when trigger payment:
```java 
	try {
		new Checkout(MainActivity.this).getInstance().
			setEnv(<<Environment Parameter>>). // set environment
			setWeChatAppID("<< WeChat Open Platform AppID >>"). // only use for wechatpay
			pay(<<Method Parameter>>,"<<Get Checkout Id from API>>", new Result());
	} catch(Exception e) {
		e.printStackTrace();
	}
```