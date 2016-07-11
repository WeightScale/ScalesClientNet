package com.kostya.scales_client_net.settings;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.*;
import android.graphics.Color;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.preference.ListPreference;
import android.provider.Settings;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.CheckedTextView;
import android.widget.ListAdapter;
import android.widget.TextView;
import com.kostya.scales_client_net.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Kostya on 26.06.2016.
 */
public class ListBluetoothDevice extends ListPreference {
    private String nameDevice;
    List<BluetoothDevice> listDevice;
    AlertDialog dialogBluetooth;
    private ArrayAdapter adapter;
    private BluetoothReceiver bluetoothReceiver; //приёмник намерений
    //private BroadcastReceiver broadcastReceiver;


    public ListBluetoothDevice(Context context, AttributeSet attrs) {
        super(context, attrs);
        listDevice = new ArrayList<>();
        setPersistent(true);

        nameDevice = getPersistedString("");

        /*broadcastReceiver = new BroadcastReceiver() {

            @Override
            public void onReceive(Context context, Intent intent) { //обработчик Bluetooth
                String action = intent.getAction();
                if (action != null) {
                    if (BluetoothAdapter.ACTION_DISCOVERY_STARTED.equals(action)) {
                        listDevice.clear();
                        adapter.notifyDataSetChanged();
                    }//break;
                    else if (BluetoothDevice.ACTION_FOUND.equals(action)) {// case BluetoothDevice.ACTION_FOUND:  //найдено устройство
                        BluetoothDevice bd = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                        listDevice.add(bd);
                        adapter.notifyDataSetChanged();
                        //BluetoothDevice bluetoothDevice = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                        String name = null;
                        if (bd != null) {
                            name = bd.getName();
                        }
                    }//break;
                    else if(BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {  //case BluetoothAdapter.ACTION_DISCOVERY_FINISHED:  //поиск завершён
                    }//break;
                    else if (BluetoothDevice.ACTION_ACL_CONNECTED.equals(action)) {//case BluetoothDevice.ACTION_ACL_CONNECTED:
                    }//break;
                    else if (BluetoothDevice.ACTION_ACL_DISCONNECTED.equals(action)) {//case BluetoothDevice.ACTION_ACL_DISCONNECTED:
                    }//break;
                }
            }
        };

        IntentFilter intentFilter = new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
        intentFilter.addAction(BluetoothDevice.ACTION_FOUND);
        intentFilter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        intentFilter.addAction(BluetoothDevice.ACTION_ACL_CONNECTED);
        intentFilter.addAction(BluetoothDevice.ACTION_ACL_DISCONNECTED);
        getContext().registerReceiver(broadcastReceiver, intentFilter);*/

        bluetoothReceiver = new BluetoothReceiver(getContext());
        bluetoothReceiver.register();
    }

    @Override
    protected void onSetInitialValue(boolean restoreValue, Object defaultValue) {
        String value = restoreValue? getPersistedString("") : (String) defaultValue;
        setValue(value);
    }

   /* @Override
    protected void onDialogClosed(boolean positiveResult) {
       if (positiveResult ) {
            BluetoothDevice value = listDevice.get(mClickedDialogEntryIndex);
            if (callChangeListener(nameDevice)) {
                setValue(mClickedDialogEntryIndex);
            }
        }
    }*/

    public void setValue(String value) {
        if (shouldPersist()) {
            persistString(value);
        }
        if (!value.equals(nameDevice)) {
            nameDevice = value;
            notifyChanged();
        }
    }

    @Override
    protected void onBindDialogView(View view) {
        super.onBindDialogView(view);
    }

    @Override
    protected void onPrepareDialogBuilder( AlertDialog.Builder builder ){

            BluetoothAdapter.getDefaultAdapter().startDiscovery();
            adapter = new ConfigurationAdapter(getContext(), R.layout.item_list, listDevice);
            builder.setTitle("Поиск...");
            builder.setSingleChoiceItems(adapter, 0, new DialogInterface.OnClickListener(){
                @Override
                public void onClick( DialogInterface dialog, int which ){
                    long l = adapter.getItemId( which );
                    setValue(((BluetoothDevice)adapter.getItem(which)).getName());
                    //ListBluetoothDevice.this.onClick(dialog, DialogInterface.BUTTON_POSITIVE);
                    callChangeListener(adapter.getItem(which));
                    dialog.dismiss();
                }
            } );
            builder.setPositiveButton("Найти", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {

                }
            });

        //setDefaultValue(mClickedDialogEntryIndex);
    }

    @Override
    protected void showDialog(Bundle state) {
        super.showDialog(state);    //Call show on default first so we can override the handlers
        dialogBluetooth = (AlertDialog) getDialog();
        //dialogBluetooth.setTitle("Список Bluetooth");
        dialogBluetooth.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v){
                //bluetoothReceiver.register();

                BluetoothAdapter.getDefaultAdapter().startDiscovery();
            }
        });
    }

    class ConfigurationAdapter extends ArrayAdapter<BluetoothDevice>{

        public ConfigurationAdapter(Context context, int resource, List<BluetoothDevice> list) {
            super(context, resource, list);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View view = convertView;

            if (view == null) {
                LayoutInflater layoutInflater = LayoutInflater.from(getContext());
                view = layoutInflater.inflate(R.layout.item_list, parent, false);
            }

            BluetoothDevice d = getItem(position);
            TextView textView = (TextView) view.findViewById(R.id.text1);
            textView.setText(d.getName().replace("\"",""));

            return view;
        }
    }

    class BluetoothReceiver extends BroadcastReceiver{
        Context mContext;
        final IntentFilter intentFilter;
        protected boolean isRegistered;

        BluetoothReceiver(Context context){
            mContext = context;
            intentFilter = new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
            intentFilter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
            intentFilter.addAction(BluetoothDevice.ACTION_FOUND);
        }

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action != null) {
                switch (action){
                    case BluetoothAdapter.ACTION_DISCOVERY_STARTED:
                        try {
                            listDevice.clear();
                            adapter.notifyDataSetChanged();
                            if(dialogBluetooth != null){
                                if (dialogBluetooth.isShowing()){
                                    dialogBluetooth.setTitle("Поиск...");
                                }
                            }
                        }catch (Exception e){}
                        break;
                    case BluetoothDevice.ACTION_FOUND:
                        BluetoothDevice bd = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                        listDevice.add(bd);
                        try {
                            adapter.notifyDataSetChanged();
                        }catch (Exception e){}
                        break;
                    case BluetoothAdapter.ACTION_DISCOVERY_FINISHED:
                        if(dialogBluetooth != null){
                            if (dialogBluetooth.isShowing()){
                                dialogBluetooth.setTitle("Список Bluetooth");
                            }
                        }
                        break;
                    default:
                }
            }
        }

        public void register() {
            if (!isRegistered){
                isRegistered = true;
                mContext.registerReceiver(this, intentFilter);
            }
        }

        public void unregister() {
            if (isRegistered) {
                mContext.unregisterReceiver(this);  // edited
                isRegistered = false;
            }
        }
    }

}
