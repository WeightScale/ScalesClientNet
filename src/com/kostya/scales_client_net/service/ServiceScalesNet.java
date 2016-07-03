package com.kostya.scales_client_net.service;


import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v7.app.NotificationCompat;
import android.util.Log;
import com.felhr.usbserial.UsbSerialDevice;
import com.felhr.usbserial.UsbSerialInterface;
import com.felhr.usbserial.UsbSerialInterface.UsbReadCallback;
import com.kostya.scales_client_net.ActivityScales;
import com.kostya.scales_client_net.Main;
import com.kostya.scales_client_net.R;
import com.kostya.scales_client_net.WifiBaseManager;
import com.kostya.scales_client_net.provider.EventsTable;
import com.kostya.scales_client_net.provider.SystemTable;
import com.kostya.scales_client_net.settings.ActivityPreferencesAdmin;
import com.kostya.scales_client_net.transferring.DataTransferringManager;

import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/** Главный Сервис.
 * @author Kostya
 */
public class ServiceScalesNet extends Service{
    private ExecutorService executorService;
    private NotificationManager notificationManager;
    private NotificationCompat.Builder builder;
    private final UsbBroadcastReceiver usbBroadcastReceiver = new UsbBroadcastReceiver();
    private UsbManager usbManager;
    private UsbSerialDevice serialPort;
    private DataTransferringManager dataTransferringManager;
    private WifiBaseManager wifiBaseManager;
    private EventsTable eventsTable;
    private int usbDeviceId;
    private static final String TAG = ServiceScalesNet.class.getName();
    public static final String ACTION_USB_PERMISSION = "com.kostya.scales_client_net.USB_PERMISSION";
    public static final int DEFAULT_NOTIFICATION_ID = 101;

    public interface OnRegisterServiceListener{
        void onEvent(int count);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        executorService = Executors.newCachedThreadPool();
        notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        usbManager = (UsbManager) getSystemService(USB_SERVICE);
        eventsTable = new EventsTable(getApplicationContext());

        IntentFilter filter = new IntentFilter();
        filter.addAction(ACTION_USB_PERMISSION);
        filter.addAction(UsbManager.ACTION_USB_DEVICE_ATTACHED);
        filter.addAction(UsbManager.ACTION_USB_DEVICE_DETACHED);
        usbBroadcastReceiver.register(this,filter);
        wifiBaseManager = new WifiBaseManager(getApplicationContext(), new WifiBaseManager.OnWifiConnectListener() {
            @Override
            public void onConnect(String ssid) {
                eventsTable.insertNewEvent("Соединение с сетью " + ssid, EventsTable.Event.WIFI_EVENT);
                startDataTransferring();
            }

            @Override
            public void onDisconnect() {startDataTransferring();}
        });
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        String action = intent.getAction();
        if (action != null){
            switch (action){
                case ActivityPreferencesAdmin.ACTION_PREFERENCE_ADMIN:
                    Bundle bundle = intent.getBundleExtra(ActivityPreferencesAdmin.EXTRA_BUNDLE_WIFI);
                    if (bundle != null){
                        for (String key : bundle.keySet()){
                            switch (key){
                                case ActivityPreferencesAdmin.KEY_SSID:
                                    wifiBaseManager.setSsid(bundle.getString(key));
                                    break;
                                case ActivityPreferencesAdmin.KEY_PASS:
                                    wifiBaseManager.setPass(bundle.getString(key));
                                    break;
                                default:
                            }
                        }
                        return START_REDELIVER_INTENT;
                    }
                    break;
                default:
            }
        }

         sendNotification(getString(R.string.app_name),getString(R.string.app_name),"Программа \"Сетевые весы\" ");

        finedUsbDevice();
        //return Service.START_STICKY;
        return START_REDELIVER_INTENT;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        notificationManager.cancel(DEFAULT_NOTIFICATION_ID);
        usbBroadcastReceiver.unregister(getBaseContext());
        if (dataTransferringManager!=null){
            dataTransferringManager.stopDataTransferring();
        }
        executorService.shutdown();
        if (serialPort != null)
            serialPort.close();
        stopSelf();
    }

    private void startDataTransferring(){
        if (executorService.isShutdown())
            executorService = Executors.newCachedThreadPool();
        executorService.execute(new Runnable() {
            @Override
            public void run() {
                dataTransferringManager = Main.getInstance().getDataTransferring();
                dataTransferringManager.setOnRegisterServiceListener(new OnRegisterServiceListener() {
                    @Override
                    public void onEvent(int count) {
                        builder.setContentTitle(getString(R.string.app_name)+'('+count+')');
                        notificationManager.notify(DEFAULT_NOTIFICATION_ID, builder.build());
                    }
                });
                dataTransferringManager.startDataTransferring(getBaseContext());
            }
        });
    }

    private void stopDataTransferring(){
        if (dataTransferringManager != null)
            dataTransferringManager.stopDataTransferring();
    }

    private class UsbBroadcastReceiver extends BroadcastReceiver {
        protected boolean isRegistered;

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            switch (action){
                case ACTION_USB_PERMISSION:
                    boolean granted = intent.getExtras().getBoolean(UsbManager.EXTRA_PERMISSION_GRANTED);
                    UsbDevice device = intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);
                    usbDeviceId = device.getProductId();
                    if (granted) {
                        setupSerialPort(device);
                    } else {
                        eventsTable.insertNewEvent("Не получено разрешение PID-" + usbDeviceId, EventsTable.Event.USB_EVENT);
                    }
                    break;
                case UsbManager.ACTION_USB_DEVICE_ATTACHED:
                    finedUsbDevice();
                    eventsTable.insertNewEvent("USB соеденено PID-"+ usbDeviceId, EventsTable.Event.USB_EVENT);
                    break;
                case UsbManager.ACTION_USB_DEVICE_DETACHED:
                    if (serialPort != null)
                        serialPort.close();
                    setNotifyContentText("COM порт закрыт");
                    eventsTable.insertNewEvent("USB отсоеденено PID-"+ usbDeviceId, EventsTable.Event.USB_EVENT);
                    break;
                case ConnectivityManager.CONNECTIVITY_ACTION:
                    ConnectivityManager connManager = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
                    NetworkInfo wifi = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
                    if (wifi.isConnected()){
                        //startDataTransferring();
                        Log.i(TAG,"WIFI STATE CONNECTED");
                    }else if (wifi.getState() == NetworkInfo.State.DISCONNECTED){
                        //internet.turnOnWiFiConnection(true);
                        //connectToSpecificNetwork();
                        Log.i(TAG,"WIFI STATE DISCONNECTED");
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

    private void setupSerialPort(UsbDevice usbDevice){
        try {
            PortProperties portProperties = new PortProperties(getApplicationContext());
            UsbDeviceConnection connection = usbManager.openDevice(usbDevice);
            serialPort = UsbSerialDevice.createUsbSerialDevice(usbDevice, connection);
            if (serialPort != null) {
                if (serialPort.open()) { //Set Serial Connection Parameters.
                    serialPort.setBaudRate(portProperties.getSpeed()); //serialPort.setBaudRate(9600);                                   /** Скорость порта. */
                    serialPort.setBaudRate(portProperties.getDataBits()); //serialPort.setDataBits(UsbSerialInterface.DATA_BITS_8);         /** Формат данных. */
                    serialPort.setStopBits(portProperties.getStopBits()); //serialPort.setStopBits(UsbSerialInterface.STOP_BITS_1);         /** Сторовый бит. */
                    serialPort.setParity(portProperties.getParity()); //serialPort.setParity(UsbSerialInterface.PARITY_NONE);           /** Бит четности. */
                    serialPort.setFlowControl(portProperties.getFlowControl()); //serialPort.setFlowControl(UsbSerialInterface.FLOW_CONTROL_OFF); /** Флов контроль. */
                    serialPort.read(mCallback);
                    setNotifyContentText("COM порт открыт");
                    eventsTable.insertNewEvent("Весы соеденены", EventsTable.Event.USB_EVENT);
                } else {
                    eventsTable.insertNewEvent("PORT NOT OPEN", EventsTable.Event.USB_EVENT);
                }
            } else {
                eventsTable.insertNewEvent("PORT IS NULL", EventsTable.Event.USB_EVENT);
            }
        } catch (Exception e) {
            eventsTable.insertNewEvent("Ошибка " + e.getMessage(), EventsTable.Event.USB_EVENT);
        }

    }

    private void finedUsbDevice(){
        HashMap<String, UsbDevice> usbDevices = usbManager.getDeviceList();
        if (!usbDevices.isEmpty()) {
            boolean keep = false;
            for (Map.Entry<String, UsbDevice> entry : usbDevices.entrySet()) {
                UsbDevice usbDevice = entry.getValue();
                usbDeviceId = usbDevice.getProductId();
                switch (usbDevice.getVendorId()){
                    case 0x2341://Arduino Vendor ID
                    case 0x067b://Prolific Technology, Inc.
                    case 0x0403://Future Technology Devices International, Ltd (FTDI)
                    case 0x03eb://Atmel Corp.
                    case 0x4348://WinChipHead
                    case 0x1a86://QinHeng Electronics
                    case 0x045b://Hitachi, Ltd
                    case 0x0471://Philips (or NXP)
                    case 0x10c4://Cygnal Integrated Products, Inc.
                        PendingIntent pi = PendingIntent.getBroadcast(this, 0, new Intent(ACTION_USB_PERMISSION), 0);
                        usbManager.requestPermission(usbDevice, pi);
                        //device = usbDevice;
                        keep = true;
                        break;
                    default:
                        //connection = null;
                        //device = null;
                }

                if (keep)
                    break;
            }
        }
    }

    private final UsbReadCallback mCallback = new UsbReadCallback() { //Defining a Callback which triggers whenever data is read.

        @Override
        public void onReceivedData(byte[] arg0) {
            try {
                String data = new String(arg0, "UTF-8");
                data.concat("\n").concat("\r");

                sendBroadcast(new Intent(ActivityScales.WEIGHT).putExtra("weight", data));
                sendNotifySubText(data);
                dataTransferringManager.sendMessageToAllDevicesInNetwork(getBaseContext(), data);
                executorService.execute(new Runnable() {
                    @Override
                    public void run() {
                        new EventsTable(getApplication()).insertNewEvent(data, EventsTable.Event.PORT_SCALE_OUT);
                    }
                });
            } catch (Exception e){
                e.printStackTrace();
            }
        }
    };

    public void sendNotification(String Ticker, String Title, String Text) {

        //These three lines makes Notification to open main activity after clicking on it
        Intent notificationIntent = new Intent(this, ActivityScales.class);
        notificationIntent.setAction(Intent.ACTION_MAIN);
        notificationIntent.addCategory(Intent.CATEGORY_LAUNCHER);
        notificationIntent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);

        PendingIntent contentIntent = PendingIntent.getActivity(getApplicationContext(), 0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        builder = new NotificationCompat.Builder(this);
        builder.setContentIntent(contentIntent)
                .setOngoing(true)   //Can't be swiped out
                .setSmallIcon(R.drawable.ic_stat_truck)
                //.setLargeIcon(BitmapFactory.decodeResource(res, R.drawable.large))   // большая картинка
                .setTicker(Ticker)
                .setContentTitle(Title) //Заголовок
                .setContentText(Text) // Текст уведомления
                .setWhen(System.currentTimeMillis());

        Notification notification = Build.VERSION.SDK_INT <= 15 ? builder.getNotification() : builder.build();

        startForeground(DEFAULT_NOTIFICATION_ID, notification);
    }

    public void setNotifyContentText(String text){
        builder.setContentText(text);
        notificationManager.notify(DEFAULT_NOTIFICATION_ID, builder.build());
    }

    public void sendNotifySubText(String text){
        builder.setSubText("Вес = "+text);
        notificationManager.notify(DEFAULT_NOTIFICATION_ID, builder.build());
    }

    private static class PortProperties{
        private final SystemTable systemTable;
        int speed;
        int dataBits;
        int stopBits;
        int parity;
        int flowControl;
        public static final HashMap<String, Integer> usbProperties = new LinkedHashMap<>();
        static {
            usbProperties.put("5", UsbSerialInterface.DATA_BITS_5);
            usbProperties.put("6", UsbSerialInterface.DATA_BITS_6);
            usbProperties.put("7", UsbSerialInterface.DATA_BITS_7 );
            usbProperties.put("8", UsbSerialInterface.DATA_BITS_8);

            usbProperties.put("1",UsbSerialInterface.STOP_BITS_1);
            usbProperties.put("2",UsbSerialInterface.STOP_BITS_2);
            usbProperties.put("1.5",UsbSerialInterface.STOP_BITS_15);

            usbProperties.put("even",UsbSerialInterface.PARITY_EVEN);
            usbProperties.put("mark",UsbSerialInterface.PARITY_MARK);
            usbProperties.put("none",UsbSerialInterface.PARITY_NONE);
            usbProperties.put("odd",UsbSerialInterface.PARITY_ODD);
            usbProperties.put("space",UsbSerialInterface.PARITY_SPACE);

            usbProperties.put("DSR_DTR",UsbSerialInterface.FLOW_CONTROL_DSR_DTR);
            usbProperties.put("OFF",UsbSerialInterface.FLOW_CONTROL_OFF);
            usbProperties.put("RTS_CTS",UsbSerialInterface.FLOW_CONTROL_RTS_CTS);
            usbProperties.put("XON_XOFF",UsbSerialInterface.FLOW_CONTROL_XON_XOFF);
        }

        PortProperties(Context context) throws Exception {
            systemTable = new SystemTable(context);
            speed = Integer.valueOf(systemTable.getProperty(SystemTable.Name.SPEED_PORT));
            dataBits = usbProperties.get(systemTable.getProperty(SystemTable.Name.FRAME_PORT));
            stopBits = usbProperties.get(systemTable.getProperty(SystemTable.Name.STOP_BIT));
            parity = usbProperties.get(systemTable.getProperty(SystemTable.Name.PARITY_BIT));
            flowControl = usbProperties.get(systemTable.getProperty(SystemTable.Name.FLOW_CONTROL));
        }

        public int getSpeed() {return speed;}
        public int getDataBits() {return dataBits;}
        public int getStopBits() {return stopBits;}
        public int getParity() {return parity;}
        public int getFlowControl() {return flowControl;}
    }
}
