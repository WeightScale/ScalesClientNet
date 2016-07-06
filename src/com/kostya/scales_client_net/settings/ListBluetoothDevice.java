package com.kostya.scales_client_net.settings;

import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.*;
import android.graphics.Color;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.preference.ListPreference;
import android.provider.Settings;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckedTextView;
import android.widget.ListAdapter;
import com.kostya.scales_client_net.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Kostya on 26.06.2016.
 */
public class ListBluetoothDevice extends ListPreference {
    private int mClickedDialogEntryIndex;
    List<BluetoothDevice> listDevice;
    private ArrayAdapter adapter;
    private BroadcastReceiver broadcastReceiver; //приёмник намерений


    public ListBluetoothDevice(Context context, AttributeSet attrs) {
        super(context, attrs);
        listDevice = new ArrayList<>();
        setPersistent(true);

        mClickedDialogEntryIndex = getPersistedInt(0);

        broadcastReceiver = new BroadcastReceiver() {

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
        getContext().registerReceiver(broadcastReceiver, intentFilter);
    }

    @Override
    protected void onSetInitialValue(boolean restoreValue, Object defaultValue) {
        int value = restoreValue? getPersistedInt(mClickedDialogEntryIndex) : (Integer) defaultValue;
        setValue(value);
    }

    @Override
    protected void onDialogClosed(boolean positiveResult) {
       if (positiveResult && mClickedDialogEntryIndex >= 0 /*&& entryValues != null*/) {
            BluetoothDevice value = listDevice.get(mClickedDialogEntryIndex);
            if (callChangeListener(value)) {
                setValue(mClickedDialogEntryIndex);
            }
        }
    }

    public void setValue(int value) {
        if (shouldPersist()) {
            persistInt(value);
        }
        if (value != mClickedDialogEntryIndex) {
            mClickedDialogEntryIndex = value;
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
        adapter = new ConfigurationAdapter(getContext(), R.layout.item_list_sender, listDevice);

        builder.setSingleChoiceItems(adapter, mClickedDialogEntryIndex, new DialogInterface.OnClickListener(){
            @Override
            public void onClick( DialogInterface dialog, int which ){
                long l = adapter.getItemId( which );
                setValue(which);

                    /*if (mClickedDialogEntryIndex != which) {
                        mClickedDialogEntryIndex = which;
                        if (shouldPersist()) {
                            persistInt(mClickedDialogEntryIndex);
                        }
                        ListPreferenceWifi.this.notifyChanged();
                    }*/
                ListBluetoothDevice.this.onClick(dialog, DialogInterface.BUTTON_POSITIVE);

                dialog.dismiss();
            }
        } );

        builder.setPositiveButton("Найти", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                getContext().startActivity(new Intent(Settings.ACTION_WIFI_SETTINGS));
                dialogInterface.dismiss();
            }
        });

        //setDefaultValue(mClickedDialogEntryIndex);
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
                view = layoutInflater.inflate(R.layout.item_list_sender, parent, false);
            }

            BluetoothDevice d = getItem(position);
            CheckedTextView textView = (CheckedTextView) view.findViewById(R.id.text1);
            textView.setText(d.getName().replace("\"",""));

            return view;
        }
    }
}
