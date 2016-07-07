package com.kostya.scales_client_net.settings;

import android.app.AlertDialog;
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
import com.kostya.scales_client_net.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Kostya on 26.06.2016.
 */
public class ListBluetoothDevice extends ListPreference {
    private String nameDevice;
    List<BluetoothDevice> listDevice;
    private ArrayAdapter adapter;
    private BluetoothReceiver bluetoothReceiver; //приёмник намерений


    public ListBluetoothDevice(Context context, AttributeSet attrs) {
        super(context, attrs);
        listDevice = new ArrayList<>();
        setPersistent(true);

        nameDevice = getPersistedString("");

        bluetoothReceiver = new BluetoothReceiver();
        bluetoothReceiver.register(getContext());
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
        adapter = new ConfigurationAdapter(getContext(), R.layout.item_list_sender, listDevice);
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
        final AlertDialog d = (AlertDialog) getDialog();
        d.setTitle("Bluetooth устройства");
        d.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v){
                bluetoothReceiver.register(getContext());
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
                view = layoutInflater.inflate(R.layout.item_list_sender, parent, false);
            }

            BluetoothDevice d = getItem(position);
            CheckedTextView textView = (CheckedTextView) view.findViewById(R.id.text1);
            textView.setText(d.getName().replace("\"",""));

            return view;
        }
    }

    class BluetoothReceiver extends BroadcastReceiver{
        IntentFilter intentFilter;
        protected boolean isRegistered;
        BluetoothReceiver(){
            intentFilter = new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
            intentFilter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
            intentFilter.addAction(BluetoothDevice.ACTION_FOUND);
            intentFilter.addAction(BluetoothDevice.ACTION_ACL_CONNECTED);
            intentFilter.addAction(BluetoothDevice.ACTION_ACL_DISCONNECTED);
            intentFilter.addAction(BluetoothDevice.ACTION_PAIRING_REQUEST);
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
                        }catch (Exception e){}
                        break;
                    case BluetoothDevice.ACTION_FOUND:
                        BluetoothDevice bd = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                        listDevice.add(bd);
                        try {
                            adapter.notifyDataSetChanged();
                        }catch (Exception e){}
                        //BluetoothDevice bluetoothDevice = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                        String name = null;
                        if (bd != null) {
                            name = bd.getName();
                        }
                        break;
                    case BluetoothAdapter.ACTION_DISCOVERY_FINISHED:
                        break;
                    case BluetoothDevice.ACTION_ACL_CONNECTED:
                        break;
                    case BluetoothDevice.ACTION_ACL_DISCONNECTED:
                        break;
                    case BluetoothDevice.ACTION_PAIRING_REQUEST:
                        Log.d("TAG", action);
                        break;
                    default:
                }
            }
        }

        public void register(Context context) {
            if (!isRegistered){
                isRegistered = true;
                context.registerReceiver(this, intentFilter);
            }
        }

        public void unregister(Context context) {
            if (isRegistered) {
                context.unregisterReceiver(this);  // edited
                isRegistered = false;
            }
        }
    }

}
