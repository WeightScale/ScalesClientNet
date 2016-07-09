package com.kostya.scales_client_net;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.UUID;

public class BluetoothBaseManager {

    private static final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (BluetoothAdapter.ACTION_STATE_CHANGED.equals(action))
                if (bluetoothAdapter.isEnabled())
                    bluetoothAdapter.startDiscovery();
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                if (device.getName().equals(NAME)) {
                    serverDevice = device;
                    bluetoothAdapter.cancelDiscovery();
                    try {
                        socket = serverDevice.createRfcommSocketToServiceRecord(uuid);
                        new Thread() {
                            @Override
                            public void run() {
                                try {
                                    socket.connect();
                                    OutputStream os = socket.getOutputStream();
                                    String ssid = SSID_COMMAND + Globals.getInstance().getPreferencesScales().read("key_wifi_ssid", "") + "\n";
                                    String pass = PASS_COMMAND + Globals.getInstance().getPreferencesScales().read("key_wifi_key", "") + "\n";
                                    os.write(ssid.getBytes("UTF-8"));
                                    os.write(pass.getBytes("UTF-8"));
                                    Thread.sleep(3000);
                                    socket.close();
                                    bluetoothAdapter.disable();
                                    context.unregisterReceiver(mReceiver);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                        }.start();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    };

    private static ActivityScales activity;
    private static  final String NAME = "ServerScales";
    private static final UUID uuid = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    private static BluetoothAdapter bluetoothAdapter;
    private static BluetoothDevice serverDevice;
    private static BluetoothSocket socket;
    private final static String SSID_COMMAND = "SSID";
    private final static String PASS_COMMAND = "PASS";
    private final static String OK = "com.kostya.scales_client_net.OK";

    public static void start(ActivityScales act) {
        activity = act;
        IntentFilter intF = new IntentFilter();
        intF.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
        intF.addAction(BluetoothDevice.ACTION_FOUND);
        activity.registerReceiver(mReceiver, intF);
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (!bluetoothAdapter.isEnabled())
            bluetoothAdapter.enable();
        else
            bluetoothAdapter.startDiscovery();
    }
}
