package com.kostya.scales_client_net;

import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import com.kostya.scales_client_net.service.ServiceScalesNet;
import com.kostya.scales_client_net.transferring.DataTransferringManager;

/**
 * @author Kostya
 */
public class Main extends Application {
    private static Main instance;
    private DataTransferringManager dataTransferring;
    private Globals globals;
    private String deviceId = "";
    public static Context currentContext;

    @Override
    public void onCreate() {
        super.onCreate();

        instance = this;
        globals = Globals.getInstance();
        globals.initialize(this);

        TelephonyManager tm = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        if (tm.getDeviceId() != null) {
            deviceId = tm.getDeviceId();
        } else {
            deviceId = Settings.Secure.getString(getApplicationContext().getContentResolver(), Settings.Secure.ANDROID_ID);
        }
        /** Для передачи данных относящихся для весов. */
        dataTransferring = new DataTransferringManager(getApplicationContext(),DataTransferringManager.SERVICE_INFO_TYPE_SCALES);
        startService(new Intent(this,ServiceScalesNet.class));
    }

    public DataTransferringManager getDataTransferring() {
        if (dataTransferring == null)
            dataTransferring = new DataTransferringManager(getApplicationContext(),DataTransferringManager.SERVICE_INFO_TYPE_SCALES);
        return dataTransferring;
    }



    private void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public static Main getInstance() { return instance; }

    public static void setInstance(Main instance) { Main.instance = instance; }



}
