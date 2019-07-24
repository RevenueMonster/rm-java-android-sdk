package com.revenuemonster.payment.constant;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.support.annotation.NonNull;

import java.util.List;

public class PackageName {
    Env env;
    Context context;

    public PackageName(Context context,  Env env) {
        this.env = env;
        this.context = context;
    }

    public Boolean isInstalled(Method method) {
        String packageName = this.getPackageName(method);
        if (packageName != "") {
            Boolean isAppInstalled = this.isPackageExists(this.context, packageName);
            if (isAppInstalled) {
                return  true;
            }
        }
        return false;
    }

    private String getPackageName(Method method) {
        switch (method) {
            case ALIPAY_CN:
                return getAlipay();

            case BOOST_MY:
                return getBoost();

            case WECHATPAY_MY:
                return getWeChatPay();

            default:
                return "";

        }
    }

    private String getBoost() {
        if (this.env.equals(Env.PRODUCTION)) {
            return "my.com.myboost";
        } else {
            return "com.boostorium.staging";
        }
    }

    private String getAlipay() {
        if (this.env.equals(Env.PRODUCTION)) {
            return "com.eg.android.AlipayGphone";
        } else {
            return "com.eg.android.AlipayGphoneRC";
        }
    }

    private String getWeChatPay() {
        return "com.tencent.mm";
    }

    private static boolean isPackageExists(@NonNull final Context context, @NonNull final String targetPackage) {
        List<ApplicationInfo> packages = context.getPackageManager().getInstalledApplications(0);
        for (ApplicationInfo packageInfo : packages) {
            if (targetPackage.equals(packageInfo.packageName)) {
                return true;
            }
        }
        return false;
    }
}