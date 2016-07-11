package com.kostya.scales_client_net;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.*;
import com.kostya.scales_client_net.provider.EventsTable;
import com.kostya.scales_client_net.provider.SystemTable;

import java.util.List;


/**
 * @author Kostya
 */
public class WifiBaseManager  {
    private final Context context;
    /** Таблица событий. */
    private final EventsTable eventsTable;
    private ConnectionReceiver connectionReceiver;
    private final Internet internet;
    /** Обратный вызов события соединения с сетью. */
    private final OnWifiConnectListener onWifiConnectListener;
    private final WifiManager wifiManager;
    private String ssid = ""/*, pass = ""*/;
    private int netConfigId;
    private static final String TAG = WifiBaseManager.class.getName();
    private static final String PSK = "PSK";
    private static final String WEP = "WEP";
    private static final String OPEN = "Open";

    public interface OnWifiConnectListener{
        void onConnect(String ssid);
        void onDisconnect();
    }

    /** Конструктор.
     * @param context Контекст программы.
     * @param listener Слушатель событий соединения.
     */
    public WifiBaseManager(Context context, OnWifiConnectListener listener){
        this.context = context;
        onWifiConnectListener = listener;
        wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        eventsTable = new EventsTable(context);
        internet = new Internet(context);
        SystemTable systemTable = new SystemTable(context);
        try {
            ssid = systemTable.getProperty(SystemTable.Name.WIFI_SSID);
            //pass = systemTable.getProperty(SystemTable.Name.WIFI_KEY);
            netConfigId = Integer.valueOf(systemTable.getProperty(SystemTable.Name.WIFI_DEFAULT));
        } catch (Exception e) {
            eventsTable.insertNewEvent(e.getMessage(), EventsTable.Event.WIFI_EVENT);
        }
        BaseReceiver baseReceiver = new BaseReceiver();
        IntentFilter intentFilter = new IntentFilter(WifiManager.WIFI_STATE_CHANGED_ACTION);
        baseReceiver.register(context, intentFilter);
        //handleWIFI();
    }

    public void setSsid(String ssid) {this.ssid = ssid;}

    //public void setPass(String pass) {this.pass = pass;}

    /**
     * Начало подключения к определенной сети wifi.
     */
    protected void handleWIFI() {
        if (wifiManager.isWifiEnabled()) {
            connectToSpecificNetwork();
        }
    }

    /**
     * Подключение к определенной сети wifi.
     */
    private void connectToSpecificNetwork() {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = cm.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        WifiInfo wifiInfo = wifiManager.getConnectionInfo();
        try {
            /** Проверяем сеть на соединение и имя конкретной сети.
             * Если верно то вызываем обратный вызов  и запускаем приемник на событие disconnect.  */
            if (networkInfo.isConnected() && wifiInfo.getSSID().replace("\"", "").equals(ssid)) {
                onWifiConnectListener.onConnect(ssid);
                new SupplicantDisconnectReceiver().register(context, new IntentFilter(WifiManager.SUPPLICANT_STATE_CHANGED_ACTION));
                return;
            }
            else {
                wifiManager.disconnect();
            }
        } catch (Exception e) {
            eventsTable.insertNewEvent(e.getMessage(), EventsTable.Event.WIFI_EVENT);
        }
        /** Запускаем приемник на прием события результат сканирования.*/
        context.registerReceiver(new ScanWifiReceiver(), new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
        /** Запускаем сканирование сети. */
        wifiManager.startScan();
    }

    /**
     * Приемник событий связаных со сканированием wifi.
     */
    private class ScanWifiReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            /** Получаем результат сканирования сети.*/
            List<ScanResult> scanResultList = wifiManager.getScanResults();
            try {
                String security = null;
                boolean found = false;
                /** Сравниваем результат с конктетной сетью. */
                for (ScanResult scanResult : scanResultList) {
                    /** Если верно то конкретная сеть есть в сети.*/
                    if (scanResult.SSID.equals(ssid)) {
                        /** Получаем тип безопасности сети. */
                        security = getScanResultSecurity(scanResult);
                        /** Флаг конкретная сеть в сети. */
                        found = true;
                        break; // found don't need continue
                    }
                }
                /** Провероверяем конкретную сеть в сохраненных конфигурациях.*/
                if (found) {
                    boolean isConfigNet = false;
                    WifiConfiguration conf = new WifiConfiguration();
                    List<WifiConfiguration> list = wifiManager.getConfiguredNetworks();
                    for (WifiConfiguration wifiConfiguration : list){
                        try {
                            /** Если конкретная сеть есть в конфигурациях. */
                            if (wifiConfiguration.SSID.replace("\"", "").equals(ssid)){
                                /** сохраняем конфигурацию во временный переменную.*/
                                conf = wifiConfiguration;
                                /** Флаг конкретная сеть есть в конфигурациях.*/
                                isConfigNet = true;
                                break;
                            }
                        }catch (Exception e){
                            /** Значит конфигурация сети дает исключение. Удаляем конфигурацию. */
                            wifiManager.removeNetwork(wifiConfiguration.networkId);
                            wifiManager.saveConfiguration();
                        }
                    }
                    int netId;
                    if(isConfigNet){
                        netId =conf.networkId;
                    }else {
                        conf.SSID = '"' + ssid + '"';
                        /*switch (security) {
                            case WEP:
                                conf.wepKeys[0] = '"' + pass + '"';
                                conf.wepTxKeyIndex = 0;
                                conf.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
                                conf.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP40);
                                break;
                            case PSK:
                                conf.preSharedKey = '"' + pass + '"';
                                break;
                            case OPEN:
                                conf.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
                                break;
                            default:
                        }*/
                        netId = wifiManager.addNetwork(conf);
                        /** Ошибка добавления конфигурации сети. */
                        if(netId == -1)
                            return;
                        /** Сохраняем конфиругацию и перезапускаем сеть. */
                        wifiManager.saveConfiguration();
                        SystemTable systemTable = new SystemTable(context);
                        systemTable.updateEntry(SystemTable.Name.WIFI_SSID, conf.SSID);
                        systemTable.updateEntry(SystemTable.Name.WIFI_DEFAULT, String.valueOf(netId));
                    }

                    /** Удаляем регистрацию приемника. */
                    try {context.unregisterReceiver(connectionReceiver);} catch (Exception e) {} // do nothing
                    /** Регестрируем приемник заново. */
                    connectionReceiver = new ConnectionReceiver();
                    IntentFilter intentFilter = new IntentFilter();
                    intentFilter.addAction(WifiManager.SUPPLICANT_STATE_CHANGED_ACTION);
                    intentFilter.addAction(WifiManager.SUPPLICANT_CONNECTION_CHANGE_ACTION);
                    intentFilter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
                    context.registerReceiver(connectionReceiver, intentFilter);
                    /** Если нет то добавляем конкретную сеть в список конфигураций. */
                    /*if(!isConfigNet){

                    }
                    int netId = wifiManager.updateNetwork(conf);*/


                    wifiManager.disconnect();
                    wifiManager.enableNetwork(netId, true);
                    wifiManager.reconnect();
                    /** Удаляем регистрацию приемника. */
                    context.unregisterReceiver(this);
                }else{
                    //todo что то сделать если нет конкретной сети открыть настройки для выбора
                }

            } catch (Exception e) {
                eventsTable.insertNewEvent(e.getMessage(), EventsTable.Event.WIFI_EVENT);
            }
        }
    }

    private void connectNet(int netId){
        try {context.unregisterReceiver(connectionReceiver);} catch (Exception e) {} // do nothing
        connectionReceiver = new ConnectionReceiver();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
        context.registerReceiver(connectionReceiver, intentFilter);
        wifiManager.disconnect();
        wifiManager.enableNetwork(netId, true);
        wifiManager.reconnect();
    }

    /**
     * Приемник событий связаных с соединением wifi.
     */
    private class ConnectionReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo networkInfo = cm.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
            WifiInfo wifiInfo = wifiManager.getConnectionInfo();
            /** Проверяем событие соединение с конкретной сетью. */
            if (networkInfo.isConnected() && wifiInfo.getSSID().replace("\"", "").equals(ssid)) {
                /** Если верно удаляем приемник сообщений. */
                context.unregisterReceiver(this);
                /** Посылаем собвтие соединение. */
                onWifiConnectListener.onConnect(ssid);
                /** Регистрируем приемник на disconnect. */
                new SupplicantDisconnectReceiver().register(context, new IntentFilter(WifiManager.SUPPLICANT_STATE_CHANGED_ACTION));
                return;
            }
            /** Проверяем событие ОШИБКА АВТОРИЗАЦИИ при подключении к сети. */
            int error=intent.getIntExtra(WifiManager.EXTRA_SUPPLICANT_ERROR, -1);
            if(error==WifiManager.ERROR_AUTHENTICATING){
                /** Удаляем приемник сообщений. */
                context.unregisterReceiver(this);
                /** Запускаем приемник на прием события результат сканирования.*/
                context.registerReceiver(new ScanWifiReceiver(), new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
                /** Запускаем сканирование сети. */
                wifiManager.startScan();
            }
        }
    }

    /** Получаем тип безопасности сети.
     * @param scanResult Результат сканирования wifi.
     * @return Тип WEP, PSK, OPEN
     */
    private String getScanResultSecurity(ScanResult scanResult) {
        final String cap = scanResult.capabilities;
        final String[] securityModes = {WEP, PSK};
        for (int i = securityModes.length - 1; i >= 0; i--) {
            if (cap.contains(securityModes[i])) {
                return securityModes[i];
            }
        }
        return OPEN;
    }

    /**
     * Приемник событий связяных с вкл/выкл wifi.
     */
    private class BaseReceiver extends BroadcastReceiver{
        protected boolean isRegistered;

        @Override
        public void onReceive(Context context, Intent intent) {
            /** Проверяем событие на выключение и включение WiFi приемника. */
            String action = intent.getAction();
            switch (action){
                case WifiManager.WIFI_STATE_CHANGED_ACTION:
                    int extraWifiState = intent.getIntExtra(WifiManager.EXTRA_WIFI_STATE, WifiManager.WIFI_STATE_UNKNOWN);
                    switch(extraWifiState){
                        case WifiManager.WIFI_STATE_DISABLED:
                            /** Если приемник был выключен заново включаем. */
                            internet.turnOnWiFiConnection(true);
                            break;
                        case WifiManager.WIFI_STATE_ENABLED:
                            /** Приемник включен. Соеденяемся с конкретной сетью заново. */
                            connectToSpecificNetwork();
                            break;
                        case WifiManager.WIFI_STATE_UNKNOWN:
                            break;
                        default:
                    }
                    break;
                default:
            }
        }

        public void register(Context context, IntentFilter filter) {
            isRegistered = true;
            context.registerReceiver(this, filter);
        }

        public boolean unregister(Context context) {
            if (isRegistered) {
                context.unregisterReceiver(this);  // edited
                isRegistered = false;
                return true;
            }
            return false;
        }
    }

    /**
     * Приемник событий связяных с disconnect.
     */
    private class SupplicantDisconnectReceiver extends BroadcastReceiver{
        protected boolean isRegistered;

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            switch (action){
                case WifiManager.SUPPLICANT_STATE_CHANGED_ACTION:
                    SupplicantState state = intent.getParcelableExtra(WifiManager.EXTRA_NEW_STATE);
                    /** Если событие disconnect. */
                    if (state == SupplicantState.DISCONNECTED){
                        /** Удаляем приемник сообщений. */
                        unregister(context);
                        /** Запускаем приемник на прием события результат сканирования.*/
                        context.registerReceiver(new ScanWifiReceiver(), new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
                        /** Запускаем сканирование сети. */
                        wifiManager.startScan();
                        eventsTable.insertNewEvent("Разьединение с сетью " + ssid, EventsTable.Event.WIFI_EVENT);
                    }
                    break;
                default:
            }
        }

        public void register(Context context, IntentFilter filter) {
            isRegistered = true;
            context.registerReceiver(this, filter);
        }

        public void unregister(Context context) {
            if (isRegistered) {
                context.unregisterReceiver(this);  // edited
                isRegistered = false;
            }
        }
    }

}
