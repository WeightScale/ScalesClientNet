package com.kostya.scales_client_net.transferring;

import android.content.Context;
import android.content.Intent;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.net.wifi.WifiManager.MulticastLock;
import android.util.Log;
import com.kostya.scales_client_net.ActivityScales;
import com.kostya.scales_client_net.Main;
import com.kostya.scales_client_net.service.ServiceScalesNet;
import com.kostya.serializable.CommandObject;
import com.kostya.serializable.Commands;

import javax.jmdns.JmDNS;
import javax.jmdns.ServiceEvent;
import javax.jmdns.ServiceInfo;
import javax.jmdns.ServiceListener;
import java.io.IOException;
import java.net.InetAddress;
import java.util.*;
import java.util.concurrent.*;


public class DataTransferringManager {

    public static final int SERVICE_INFO_PORT = 8856;//8856
    public static final String SERVICE_INFO_TYPE_SCALES = "_scales._tcp.local.";
    public static final String SERVICE_INFO_NAME_CLIENT = "ScalesClient";
    public static final String SERVICE_INFO_NAME_SERVER = "ScalesServer";
    public static final String SERVICE_INFO_PROPERTY_IP_VERSION = "ipv4";
    public static final String SERVICE_INFO_PROPERTY_DEVICE = "device";
    private static final String TAG = DataTransferringManager.class.getName();

    private ServiceScalesNet.OnRegisterServiceListener onRegisterServiceListener;
    private ExecutorService executorService;
    private JmDNS jmdns;
    private Context context;

    private List<ServiceInfo> listServers = new ArrayList<>();
    private ServiceInfo currentServer;
    private ServiceListener listener;
    private ServiceInfo serviceInfo;
    private MulticastLock multiCastLock;
    private final JmDnsServerThreadProcessor serverThreadProcessor = new JmDnsServerThreadProcessor();
    private final String serviceType;
    private boolean registered;


    public DataTransferringManager(Context context, String type){
        this.context = context;
        executorService = Executors.newCachedThreadPool();
        serviceType = type;
    }

    public Context getContext() {return context;}

    public void setCurrentServer(ServiceInfo currentServer) {this.currentServer = currentServer;}

    public DataTransferringManager(String type, ServiceScalesNet.OnRegisterServiceListener listener){
        serviceType = type;
        onRegisterServiceListener = listener;
        executorService = Executors.newCachedThreadPool();
    }

    public void setOnRegisterServiceListener(ServiceScalesNet.OnRegisterServiceListener onRegisterServiceListener) {
        this.onRegisterServiceListener = onRegisterServiceListener;
    }

    public void startDataTransferring(final Context context) {

        WifiManager wifi = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        changeMultiCastLock(wifi);

        try {
            if (jmdns == null) {

                InetAddress inetAddress = getInetAddress(wifi);
                jmdns = JmDNS.create(inetAddress);
                jmdns.addServiceListener(serviceType, listener = new ServiceListener() {
                    @Override
                    public void serviceResolved(ServiceEvent ev) {
                        /** Если сервер добавляем список серверов. */
                        if (ev.getName().startsWith(SERVICE_INFO_NAME_SERVER)){
                            listServers.add(ev.getInfo());
                            onRegisterServiceListener.onEvent(listServers.size());
                            getContext().sendBroadcast(new Intent(ActivityScales.ACTION_UPDATE_SERVER_LIST));
                            CommandObject commandObject = new CommandObject(Commands.CMD_DEFAULT_TERMINAL);
                            commandObject.getObjectFromDeviceInNetwork(context, getIPv4FromServiceInfo(ev.getInfo()));
                            //sendObjectOutputInputToDevice(context, getIPv4FromServiceInfo(ev.getInfo()), new CommandObject(Commands.CMD_GET_TERMINAL));
                            //sendObjectToDevicesInNetwork(context, getIPv4FromServiceInfo(ev.getInfo()), new CommandObject(Commands.CMD_GET_TERMINAL));
                        }
                    }

                    @Override
                    public void serviceRemoved(ServiceEvent ev) {
                        for (ServiceInfo info : listServers){
                            if(info.equals(ev.getInfo())){
                                listServers.remove(info);
                            }
                        }
                        onRegisterServiceListener.onEvent(listServers.size());
                        Log.i(TAG, "Service removed " + ev.getName());
                    }

                    @Override
                    public void serviceAdded(ServiceEvent event) {
                        jmdns.requestServiceInfo(event.getType(), event.getName(), 1);
                    }
                });
                Hashtable<String, String> settings = setSettingsHashTable(context);
                serviceInfo = ServiceInfo.create(serviceType, SERVICE_INFO_NAME_CLIENT , SERVICE_INFO_PORT, 0, 0, true, settings);
                jmdns.registerService(serviceInfo);
                serverThreadProcessor.startServerProcessorThread(context);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void registerService(){
        if (jmdns != null)
            try {
                jmdns.registerService(serviceInfo);
            } catch (IOException e) {
                e.printStackTrace();
            }
    }

    public void unregisterService(){
        if (jmdns != null)
            jmdns.unregisterService(serviceInfo);
    }

    public void stopDataTransferring() {
        if (jmdns != null) {
            if (listener != null) {
                jmdns.removeServiceListener(serviceType, listener);
                listener = null;
            }
            jmdns.unregisterAllServices();
            registered = false;
            try {
                jmdns.close();
            } catch (Exception ex) {
                ex.printStackTrace();
            }

            jmdns = null;
        }
        serverThreadProcessor.stopServerProcessorThread();
        if (multiCastLock != null && multiCastLock.isHeld())
            multiCastLock.release();
        executorService.shutdown();
    }

    private InetAddress getInetAddress(WifiManager wifiManager) throws IOException {
        WifiInfo wifiInfo = wifiManager.getConnectionInfo();
        int addressIntIp = wifiInfo.getIpAddress();

        byte[] byteAddress = {
                (byte) (addressIntIp & 0xff),
                (byte) (addressIntIp >> 8 & 0xff),
                (byte) (addressIntIp >> 16 & 0xff),
                (byte) (addressIntIp >> 24 & 0xff)
        };
        return InetAddress.getByAddress(byteAddress);
    }

    private void changeMultiCastLock(WifiManager wifiManager) {
        if (multiCastLock != null && multiCastLock.isHeld())
            multiCastLock.release();

        if (multiCastLock == null) {
            multiCastLock = wifiManager.createMulticastLock("mylockthereturn");
            multiCastLock.setReferenceCounted(true);
        }

        multiCastLock.acquire();
    }

    private Hashtable<String, String> setSettingsHashTable(Context context) {
        Hashtable<String, String> settings = new Hashtable<>();
        settings.put(SERVICE_INFO_PROPERTY_DEVICE, ((Main) context.getApplicationContext()).getDeviceId());
        settings.put(SERVICE_INFO_PROPERTY_IP_VERSION, IPUtils.getLocalIpAddress(context));
        return settings;
    }

    public JmDNS getJmDNS() {
        return jmdns;
    }

    private String getIPv4FromServiceInfo(ServiceInfo serviceInfo) {
        return serviceInfo.getPropertyString(SERVICE_INFO_PROPERTY_IP_VERSION);
    }

    public void sendMessageToAllDevicesInNetwork(final Context context, String message){
        if (jmdns != null) {

            if (executorService.isShutdown())
                executorService = Executors.newCachedThreadPool();

            Set<String> ipAddressesSet = getNeighborDevicesIpAddressesSet(context);

            for (String serverIpAddress : ipAddressesSet) {
                sendMessageToDevicesInNetwork(context, serverIpAddress, message);
            }
        }
    }

    public void sendMessageToDevicesInNetwork(final Context context, String ipAddress, String message){
        executorService.execute(new Runnable() {
            @Override
            public void run() {
                new ClientProcessor(message, ipAddress, context);
            }
        });
    }

    public void sendObjectToDevicesInNetwork(final Context context, String ipAddress, Object object){
        executorService.execute(new Runnable() {
            @Override
            public void run() {
                new ClientProcessor(object, ipAddress, context);
            }
        });
    }

    public void sendObjectOutputInputToDevice(final Context context, String ipAddress, Object object){
        executorService.execute(new Runnable() {
            @Override
            public void run() {
                ClientProcessor clientProcessor = new ClientProcessor(ipAddress, context);
                clientProcessor.sendObjectOutputInputToDevice(object);
            }
        });
    }

    private Set<String> getNeighborDevicesIpAddressesSet(Context context){

        Set<String> ipAddressesSet = new HashSet<>();
        ServiceInfo[] serviceInfoList = jmdns.list(serviceType);

        for (ServiceInfo currentServiceInfo : serviceInfoList) {
            String device = currentServiceInfo.getPropertyString(SERVICE_INFO_PROPERTY_DEVICE);
            String ownDeviceId = ((Main) context.getApplicationContext()).getDeviceId();

            if (!ownDeviceId.equals(device)) {
                String serverIpAddress = getIPv4FromServiceInfo(currentServiceInfo);
                ipAddressesSet.add(serverIpAddress);
            }
        }
        return ipAddressesSet;
    }

    public List<String> getOnlineDevicesList(Context context, String deviceId) {

        List<String> onlineDevices = new ArrayList<>();
        try {
            if (jmdns == null) {
                startDataTransferring(context);
            }

            ServiceInfo[] serviceInfoList = jmdns.list(serviceType);
            if (serviceInfoList != null) {

                for (ServiceInfo aServiceInfoList : serviceInfoList) {
                    String device = aServiceInfoList.getPropertyString(SERVICE_INFO_PROPERTY_DEVICE);

                    try {
                        if (!device.equals(deviceId)) {
                            String ip = getIPv4FromServiceInfo(aServiceInfoList);
                            if (!onlineDevices.contains(ip))
                                onlineDevices.add(ip);
                        }
                    } catch (NullPointerException e) {
                        e.printStackTrace();
                    }
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return onlineDevices;
    }

    public List<ServiceInfo> getListServers() {return listServers;}

}
