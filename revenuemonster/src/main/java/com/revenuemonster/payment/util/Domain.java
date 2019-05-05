package com.revenuemonster.payment.util;

import com.revenuemonster.payment.constant.Env;

/**
 * Created by yussuf on 4/30/19.
 */



public class Domain {
    private Env env;
    private static final String PRODUCTION_PG_URL = "https://pg.revenuemonster.my";
    private static final String SANDBOX_PG_URL = "https://sb-pg.revenuemonster.my";
    private static final String DEVELOPMENT_PG_URL = "https://dev-rm-api.ap.ngrok.io";

    public Domain(Env env) {
        this.env = env;
    }

    public String getPaymentGatewayURL() {
        switch (this.env) {
            case Sandbox:
                return SANDBOX_PG_URL;
            case Development:
                return DEVELOPMENT_PG_URL;
        }

        return PRODUCTION_PG_URL;
    }
}
