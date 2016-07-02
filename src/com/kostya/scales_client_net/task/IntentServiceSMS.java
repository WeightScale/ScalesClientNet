package com.kostya.scales_client_net.task;

import android.app.IntentService;
import android.content.Intent;
import android.os.Bundle;
import com.konst.sms_commander.SMS;

/**
 * @author Kostya  on 28.06.2016.
 */
public class IntentServiceSMS extends IntentService {
    public static final String EXTRA_ADDRESS_SMS = "com.kostya.scaleswifinet.task.EXTRA_ADDRESS_SMS";
    public static final String EXTRA_TEXT_SMS = "com.kostya.scaleswifinet.task.EXTRA_TEXT_SMS";

    public IntentServiceSMS(String name) { super(name); }
    public IntentServiceSMS() { super(IntentServiceSMS.class.getName()); }

    @Override
    protected void onHandleIntent(Intent intent) {
        Bundle bundle = intent.getExtras();
        if(bundle != null){
            String address = bundle.getString(EXTRA_ADDRESS_SMS);
            String body = bundle.getString(EXTRA_TEXT_SMS);

            try {
                SMS.sendSMS(address, body);
            } catch (Exception e) {

            }
        }

    }
}
