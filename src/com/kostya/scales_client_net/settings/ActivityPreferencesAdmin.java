package com.kostya.scales_client_net.settings;

//import android.content.SharedPreferences;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.*;
import android.database.Cursor;
import android.net.Uri;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.preference.*;
import android.provider.BaseColumns;
import android.text.InputType;
import android.text.method.PasswordTransformationMethod;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.widget.*;
import com.google.common.io.ByteStreams;
import com.kostya.scales_client_net.Globals;
import com.kostya.scales_client_net.Main;
import com.kostya.scales_client_net.R;
import com.kostya.scales_client_net.provider.SenderTable;
import com.kostya.scales_client_net.provider.SystemTable;
import com.kostya.scales_client_net.service.ServiceScalesNet;
import com.kostya.scales_client_net.transferring.DataTransferringManager;
import com.kostya.serializable.ComPortObject;
import com.kostya.serializable.Command;
import com.kostya.serializable.CommandObject;
import com.kostya.serializable.Commands;

import java.io.*;
import java.util.List;
import java.util.UUID;

//import android.preference.PreferenceManager;

public class ActivityPreferencesAdmin extends PreferenceActivity  {
    private static BluetoothSocket bluetoothSocket;
    private static BluetoothRequestReceiver bluetoothRequestReceiver;
    private static ConnectThread connectThread;
    private static Command commandBluetooth;
    protected static Dialog dialog;
    private static SystemTable systemTable;
    private EditText input;
    public static Intent intent;
    public static ComPortObject comPortObject = new ComPortObject();

    private static final int FILE_SELECT_CODE = 10;
    private static final int REQUEST_ENABLE_BLUETOOTH = 2;
    private static boolean flag_restore, flag_com;
    private static final String TAG = ActivityPreferencesAdmin.class.getName();
    private static final String superCode = "343434";
    public static final String ACTION_PREFERENCE_ADMIN = "com.kostya.scales_client_net.settings.ACTION_PREFERENCE_ADMIN";
    public static final String ACTION_COM_PORT = "com.kostya.scales_client_net.settings.ACTION_COM_PORT";
    public static final String EXTRA_BUNDLE_WIFI = "com.kostya.scales_client_net.settings.EXTRA_BUNDLE_WIFI";
    public static final String EXTRA_BUNDLE_USB = "com.kostya.scales_client_net.settings.EXTRA_BUNDLE_USB";
    public static final String EXTRA_BUNDLE_COM_PORT = "com.kostya.scales_client_net.settings.EXTRA_BUNDLE_COM_PORT";
    public static final String KEY_SSID = "com.kostya.scales_client_net.settings.KEY_SSID";
    public static final String KEY_PASS = "com.kostya.scales_client_net.settings.KEY_PASS";

    public enum EnumPreferenceAdmin{
        SPEED_PORT(R.string.KEY_SPEED_PORT){
            Context mContext;

            @Override
            void setup(Preference name) throws Exception {
                mContext = name.getContext();
                name.setTitle("Скорость: " + systemTable.getProperty(SystemTable.Name.SPEED_PORT));
                name.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                    @Override
                    public boolean onPreferenceChange(Preference preference, Object o) {
                        comPortObject.setSpeed(Integer.valueOf(o.toString()));
                        name.setTitle("Скорость: " + o);
                        Toast.makeText(mContext, mContext.getString(R.string.preferences_yes)+' '+ o.toString(), Toast.LENGTH_SHORT).show();
                        return flag_com = true;
                    }
                });
            }

        },
        FRAME_PORT(R.string.KEY_SERIAL_FRAME){
            Context mContext;

            @Override
            void setup(Preference name) throws Exception {
                mContext = name.getContext();
                name.setTitle("Формат: " + systemTable.getProperty(SystemTable.Name.FRAME_PORT));
                name.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                    @Override
                    public boolean onPreferenceChange(Preference preference, Object o) {
                        comPortObject.setDataBits(ComPortObject.usbProperties.get(o.toString()));
                        ((ListPreference)name).setValue(o.toString());
                        name.setTitle("Формат: " + o);
                        Toast.makeText(mContext, mContext.getString(R.string.preferences_yes)+' '+ o.toString(), Toast.LENGTH_SHORT).show();
                        return  flag_com = true;
                    }
                });
            }
        },
        PARITY_BIT(R.string.KEY_PARITY_BIT){
            Context mContext;

            @Override
            void setup(Preference name) throws Exception {
                mContext = name.getContext();
                name.setTitle("Бит четности: " + systemTable.getProperty(SystemTable.Name.PARITY_BIT));
                name.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                    @Override
                    public boolean onPreferenceChange(Preference preference, Object o) {
                        comPortObject.setParity(ComPortObject.usbProperties.get(o.toString()));
                        ((ListPreference)name).setValue(o.toString());
                        name.setTitle("Бит четности: " + o);
                        Toast.makeText(mContext, mContext.getString(R.string.preferences_yes)+' '+ o.toString(), Toast.LENGTH_SHORT).show();
                        return flag_com = true;
                    }
                });
            }
        },
        STOP_BIT(R.string.KEY_STOP_BIT){
            Context mContext;

            @Override
            void setup(Preference name) throws Exception {
                mContext = name.getContext();
                name.setTitle("Стоп бит: " + systemTable.getProperty(SystemTable.Name.STOP_BIT));
                name.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                    @Override
                    public boolean onPreferenceChange(Preference preference, Object o) {
                        comPortObject.setStopBits(ComPortObject.usbProperties.get(o.toString()));
                        ((ListPreference)name).setValue(o.toString());
                        name.setTitle("Стоп бит: " + o);
                        Toast.makeText(mContext, mContext.getString(R.string.preferences_yes)+' '+ o.toString(), Toast.LENGTH_SHORT).show();
                        return flag_com = true;
                    }
                });
            }
        },
        FLOW_CONTROL(R.string.KEY_FLOW_CONTROL){
            Context mContext;

            @Override
            void setup(Preference name) throws Exception {
                mContext = name.getContext();
                name.setTitle("Флов контроль: " + systemTable.getProperty(SystemTable.Name.FLOW_CONTROL));
                name.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                    @Override
                    public boolean onPreferenceChange(Preference preference, Object o) {
                        comPortObject.setFlowControl(ComPortObject.usbProperties.get(o.toString()));
                        ((ListPreference)name).setValue(o.toString());
                        name.setTitle("Флов контроль: " + o);
                        Toast.makeText(mContext, mContext.getString(R.string.preferences_yes)+' '+ o.toString(), Toast.LENGTH_SHORT).show();
                        return flag_com = true;
                    }
                });
            }
        },
        WIFI_SSID(R.string.KEY_WIFI_SSID){
            @Override
            void setup(Preference name) throws Exception {
                Context mContext = name.getContext();
                name.setTitle(systemTable.getProperty(SystemTable.Name.WIFI_SSID));
                name.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                    @Override
                    public boolean onPreferenceChange(Preference preference, Object o) {
                        //if (commandBluetooth.setData(Commands.CMD_SSID_WIFI,o.toString())){
                        if (((Commands)connectThread.write(new CommandObject(Commands.CMD_RECONNECT_SERVER_NET,o.toString()))).equals(Commands.CMD_RECONNECT_SERVER_NET)){
                        //if (commandBluetooth.sendObject(new CommandObject(Commands.CMD_SSID_WIFI,o.toString()))){
                            name.setTitle(o.toString());
                            Toast.makeText(mContext, mContext.getString(R.string.preferences_yes)+' '+ o.toString(), Toast.LENGTH_SHORT).show();
                            return false;
                        }

                        preference.setSummary("Имя сети WiFi: ???");
                        Toast.makeText(name.getContext(), R.string.preferences_no, Toast.LENGTH_SHORT).show();

                        return false;
                    }
                });
            }
        },
        WIFI_KEY(R.string.KEY_WIFI_KEY){
            @Override
            void setup(Preference name) throws Exception {
                Context mContext = name.getContext();
                //name.setTitle(systemTable.getProperty(SystemTable.Name.WIFI_KEY));
                name.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                    @Override
                    public boolean onPreferenceChange(Preference preference, Object o) {
                        //if (commandBluetooth.setData(Commands.CMD_KEY_WIFI,o.toString())){
                        if (commandBluetooth.sendObject(new CommandObject(Commands.CMD_KEY_WIFI,o.toString()))){
                            name.setTitle(o.toString());
                            Toast.makeText(mContext, mContext.getString(R.string.preferences_yes)+' '+ o.toString(), Toast.LENGTH_SHORT).show();
                            return false;
                        }

                        preference.setSummary("Ключь сети WiFi: ???");
                        Toast.makeText(name.getContext(), R.string.preferences_no, Toast.LENGTH_SHORT).show();

                        return flag_restore = true;
                    }
                });
            }
        },
        KEY_ENABLE_BLUETOOTH(R.string.KEY_ENABLE_BLUETOOTH){
            Context mContext;
            Preference preference;
            @Override
            void setup(Preference name) throws Exception {
                mContext = name.getContext();
                preference = name;
                //name.setTitle(name.getSharedPreferences().getString(mContext.getString(getResId()),""));
                name.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {

                    @Override
                    public boolean onPreferenceClick(Preference preference) {
                        if (!BluetoothAdapter.getDefaultAdapter().isEnabled()){
                            ((Activity)mContext).startActivityForResult(new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE), REQUEST_ENABLE_BLUETOOTH);
                        }
                        return false;
                    }
                });
            }

            public Preference getPreference(){
                return preference;
            }

            public void pairDevice(BluetoothDevice device) {
                Intent intent = new Intent(BluetoothDevice.ACTION_PAIRING_REQUEST);
                intent.putExtra(BluetoothDevice.EXTRA_DEVICE, device);
                int PAIRING_VARIANT_PIN = 272;
                intent.putExtra(BluetoothDevice.EXTRA_PAIRING_VARIANT, PAIRING_VARIANT_PIN);
                mContext.sendBroadcast(intent);


                /*Intent intent = new Intent(BluetoothDevice.ACTION_PAIRING_REQUEST);
                intent.putExtra(BluetoothDevice.EXTRA_DEVICE, device);
                intent.putExtra(BluetoothDevice.EXTRA_PAIRING_VARIANT, 1);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                mContext.startActivity(intent);*/
            }

        },
        KEY_ENABLE_SETTING_COM(R.string.KEY_ENABLE_SETTING_COM){
            Context mContext;
            Preference preference;
            @Override
            void setup(Preference name) throws Exception {
                mContext = name.getContext();
                preference = name;
                //name.setTitle(name.getSharedPreferences().getString(mContext.getString(getResId()),""));
                name.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {

                    @Override
                    public boolean onPreferenceClick(Preference preference) {
                        comPortObject = Globals.getInstance().getCurrentTerminal().getComPortObject();
                        if (comPortObject!= null){
                            name.setEnabled(true);
                        }
                        return false;
                    }
                });
            }

            public Preference getPreference(){
                return preference;
            }

            public void pairDevice(BluetoothDevice device) {
                Intent intent = new Intent(BluetoothDevice.ACTION_PAIRING_REQUEST);
                intent.putExtra(BluetoothDevice.EXTRA_DEVICE, device);
                int PAIRING_VARIANT_PIN = 272;
                intent.putExtra(BluetoothDevice.EXTRA_PAIRING_VARIANT, PAIRING_VARIANT_PIN);
                mContext.sendBroadcast(intent);


                /*Intent intent = new Intent(BluetoothDevice.ACTION_PAIRING_REQUEST);
                intent.putExtra(BluetoothDevice.EXTRA_DEVICE, device);
                intent.putExtra(BluetoothDevice.EXTRA_PAIRING_VARIANT, 1);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                mContext.startActivity(intent);*/
            }

        },
        KEY_LIST_BLUETOOTH(R.string.KEY_LIST_BLUETOOTH){
            Context mContext;
            @Override
            void setup(Preference name) throws Exception {
                mContext = name.getContext();
                //name.setTitle(name.getSharedPreferences().getString(mContext.getString(getResId()),""));
                name.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                    @Override
                    public boolean onPreferenceChange(Preference preference, Object o) {
                        if (o.toString().isEmpty()) {
                            Toast.makeText(mContext, R.string.preferences_no, Toast.LENGTH_SHORT).show();
                            return false;
                        }
                        BluetoothDevice device = (BluetoothDevice)o;
                        bluetoothRequestReceiver.register();

                        if (connectThread != null)
                            connectThread.interrupt();
                        connectThread = new ConnectThread(device);
                        connectThread.start();

                        //name.setTitle(device.getName());
                        //preference.getEditor().putString(mContext.getString(getResId()), device.getName()).commit();
                        //Toast.makeText(mContext, mContext.getString(R.string.preferences_yes)+' '+ device.getName(), Toast.LENGTH_SHORT).show();
                        return false;
                    }
                });
            }

            public void pairDevice(BluetoothDevice device) {
                Intent intent = new Intent(BluetoothDevice.ACTION_PAIRING_REQUEST);
                intent.putExtra(BluetoothDevice.EXTRA_DEVICE, device);
                int PAIRING_VARIANT_PIN = 272;
                intent.putExtra(BluetoothDevice.EXTRA_PAIRING_VARIANT, PAIRING_VARIANT_PIN);
                mContext.sendBroadcast(intent);


                /*Intent intent = new Intent(BluetoothDevice.ACTION_PAIRING_REQUEST);
                intent.putExtra(BluetoothDevice.EXTRA_DEVICE, device);
                intent.putExtra(BluetoothDevice.EXTRA_PAIRING_VARIANT, 1);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                mContext.startActivity(intent);*/
            }

        },
        KEY_RECONNECT_NET(R.string.KEY_RECONNECT_NET){
            Context mContext;
            @Override
            void setup(Preference name) throws Exception {
                mContext = name.getContext();
                name.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                    @Override
                    public boolean onPreferenceClick(Preference preference) {
                        //if (commandBluetooth.setData(Commands.CMD_RECONNECT_SERVER_NET,"")){
                        if (((Commands)connectThread.write(new CommandObject(Commands.CMD_RECONNECT_SERVER_NET))).equals(Commands.CMD_RECONNECT_SERVER_NET)){
                        //if (commandBluetooth.sendObject(new CommandObject(Commands.CMD_RECONNECT_SERVER_NET))){
                            Toast.makeText(mContext, mContext.getString(R.string.preferences_yes)+' '+ Commands.CMD_RECONNECT_SERVER_NET.name(), Toast.LENGTH_SHORT).show();
                            return false;
                        }
                        return false;
                    }
                });
            }
        },
        KEY_WIFI_DEFAULT(R.string.KEY_WIFI_DEFAULT){
            @Override
            void setup(Preference name) throws Exception {
                Context mContext = name.getContext();
                try {
                    name.setTitle("ИМЯ СЕТИ: " + getNameOfId(mContext, Integer.valueOf(systemTable.getProperty(SystemTable.Name.WIFI_DEFAULT))) );
                }catch (Exception e){}
                //name.setSummary("Сеть по умолчанию. Для выбора конкретной сети из списка кофигураций если есть.");
                name.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                    @Override
                    public boolean onPreferenceChange(Preference preference, Object o) {
                        if (o.toString().isEmpty()) {
                            Toast.makeText(mContext, R.string.preferences_no, Toast.LENGTH_SHORT).show();
                            return false;
                        }
                        String netName = ((WifiConfiguration)o).SSID.replace("\"","");
                        String netId = String.valueOf(((WifiConfiguration)o).networkId);
                        if(systemTable.updateEntry(SystemTable.Name.WIFI_DEFAULT, netId)){
                            if(systemTable.updateEntry(SystemTable.Name.WIFI_SSID, netName)){
                                name.setTitle("ИМЯ СЕТИ: " + netName);
                                Toast.makeText(mContext, mContext.getString(R.string.preferences_yes)+' '+ netName, Toast.LENGTH_SHORT).show();
                                Bundle bundle = intent.getBundleExtra(EXTRA_BUNDLE_WIFI);
                                if (bundle == null)
                                    bundle = new Bundle();
                                bundle.putString(KEY_SSID, netName);
                                intent.putExtra(EXTRA_BUNDLE_WIFI, bundle);
                                return flag_restore = true;
                            }
                        }
                        name.setTitle("ИМЯ СЕТИ: " + "???");
                        return false;
                    }
                });
            }

            String getNameOfId(Context context, int id){
                List<WifiConfiguration> list = ((WifiManager)context.getSystemService(Context.WIFI_SERVICE)).getConfiguredNetworks();
                for (WifiConfiguration wifiConfiguration : list){
                    if (wifiConfiguration.networkId == id){
                        return  wifiConfiguration.SSID.replace("\"", "");
                    }
                }
                return "";
            }
        },
        KEY_SHEET(R.string.KEY_SHEET){
            @Override
            void setup(Preference name) throws Exception {
                Context mContext = name.getContext();
                name.setTitle('"' + systemTable.getProperty(SystemTable.Name.SHEET_GOOGLE) + '"');
                //name.setSummary(mContext.getString(R.string.TEXT_MESSAGE7));
                name.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                    @Override
                    public boolean onPreferenceChange(Preference preference, Object o) {
                        if (o.toString().isEmpty()) {
                            Toast.makeText(mContext, R.string.preferences_no, Toast.LENGTH_SHORT).show();
                            return false;
                        }

                        if(systemTable.updateEntry(SystemTable.Name.SHEET_GOOGLE, o.toString())){
                            name.setTitle('"' + o.toString() + '"');
                            Toast.makeText(mContext, mContext.getString(R.string.preferences_yes)+' '+ o.toString(), Toast.LENGTH_SHORT).show();
                            return true;
                        }
                        return false;
                    }
                });
            }
        },
        USER(R.string.KEY_USER){
            @Override
            void setup(Preference name) throws Exception {
                Context mContext = name.getContext();
                //name.setTitle(mContext.getString(R.string.User_google_disk) + '"' + systemTable.getProperty(SystemTable.Name.USER) + '"');
                name.setTitle(systemTable.getProperty(SystemTable.Name.USER_GOOGLE));
                name.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                    @Override
                    public boolean onPreferenceChange(Preference preference, Object o) {
                        if (o.toString().isEmpty()) {
                            Toast.makeText(name.getContext(), R.string.preferences_no, Toast.LENGTH_SHORT).show();
                            return false;
                        }

                        if(systemTable.updateEntry(SystemTable.Name.USER_GOOGLE, o.toString())){
                            name.setTitle( o.toString());
                            Toast.makeText(mContext, mContext.getString(R.string.preferences_yes)+' '+ o.toString(), Toast.LENGTH_SHORT).show();
                            return true;
                        }

                        name.setSummary("Account Google: ???");
                        Toast.makeText(mContext, R.string.preferences_no, Toast.LENGTH_SHORT).show();
                        return false;
                    }
                });
            }
        },
        PASSWORD(R.string.KEY_PASSWORD){
            @Override
            void setup(Preference name) throws Exception {
                Context mContext = name.getContext();
                name.setTitle("******");
                name.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                    @Override
                    public boolean onPreferenceChange(Preference preference, Object o) {
                        if (o.toString().isEmpty()) {
                            Toast.makeText(name.getContext(), R.string.preferences_no, Toast.LENGTH_SHORT).show();
                            return false;
                        }

                        if(systemTable.updateEntry(SystemTable.Name.PASSWORD, o.toString())){
                            name.setTitle("******");
                            Toast.makeText(mContext, mContext.getString(R.string.preferences_yes)+' '+ o.toString(), Toast.LENGTH_SHORT).show();
                            return true;
                        }
                        preference.setSummary("Password account Google: ???");
                        Toast.makeText(name.getContext(), R.string.preferences_no, Toast.LENGTH_SHORT).show();

                        return false;
                    }
                });
            }
        },
        PHONE(R.string.KEY_PHONE){
            @Override
            void setup(Preference name) throws Exception {
                Context mContext = name.getContext();
                name.setTitle(systemTable.getProperty(SystemTable.Name.PHONE));
                name.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                    @Override
                    public boolean onPreferenceChange(Preference preference, Object o) {
                        if (o.toString().isEmpty()) {
                            Toast.makeText(name.getContext(), R.string.preferences_no, Toast.LENGTH_SHORT).show();
                            return false;
                        }
                        if(systemTable.updateEntry(SystemTable.Name.PHONE, o.toString())){
                            name.setTitle(o.toString());
                            Toast.makeText(mContext, mContext.getString(R.string.preferences_yes)+' '+ o.toString(), Toast.LENGTH_SHORT).show();
                            return true;
                        }

                        preference.setSummary("Номер телефона для СМС: ???");
                        Toast.makeText(name.getContext(), R.string.preferences_no, Toast.LENGTH_SHORT).show();

                        return false;
                    }
                });
            }
        },
        SENDER(R.string.KEY_SENDER){
            Context mContext;
            SenderTable senderTable;

            @Override
            void setup(Preference name) throws Exception {
                mContext = name.getContext();
                senderTable = new SenderTable(mContext);

                name.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                    @Override
                    public boolean onPreferenceClick(Preference preference) {
                        openListDialog();
                        return false;
                    }
                });
            }

            public void openListDialog() {
                final Cursor senders = senderTable.getAllEntries();
                //final Cursor emails = contentResolver.query(CommonDataKinds.Email.CONTENT_URI, null,CommonDataKinds.Email.CONTACT_ID + " = " + mContactId, null, null);
                if (senders == null) {
                    return;
                }
                if (senders.moveToFirst()) {
                    String[] columns = {SenderTable.KEY_TYPE};
                    int[] to = {R.id.text1};
                    SimpleCursorAdapter cursorAdapter = new SimpleCursorAdapter(mContext, R.layout.item_list_sender, senders, columns, to);
                    cursorAdapter.setViewBinder(new ListBinder());
                    //LayoutInflater layoutInflater = mContext.getLayoutInflater();
                    LayoutInflater layoutInflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                    View convertView = layoutInflater.inflate(R.layout.dialog_sender, null);
                    ListView listView = (ListView) convertView.findViewById(R.id.component_list);
                    TextView dialogTitle = (TextView) convertView.findViewById(R.id.dialog_title);
                    dialogTitle.setText("Выбрать отсылатель");
                    listView.setAdapter(cursorAdapter);
                    listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                        @Override
                        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                            Checkable v = (Checkable) view;
                            v.toggle();
                            if (v.isChecked())
                                senderTable.updateEntry((int)id, SenderTable.KEY_SYS, 1);
                            else
                                senderTable.updateEntry((int) id, SenderTable.KEY_SYS, 0);
                        }
                    });
                    dialog.setContentView(convertView);
                    dialog.setCancelable(false);
                    ImageButton buttonSelectAll = (ImageButton) dialog.findViewById(R.id.buttonSelectAll);
                    buttonSelectAll.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View arg0) {
                            selectedAll();
                        }
                    });
                    ImageButton buttonUnSelect = (ImageButton) dialog.findViewById(R.id.buttonUnselect);
                    buttonUnSelect.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View arg0) {
                            unselectedAll();
                        }
                    });
                    ImageButton buttonBack = (ImageButton) dialog.findViewById(R.id.buttonBack);
                    buttonBack.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View arg0) {
                            dialog.dismiss();
                        }
                    });
                    dialog.show();
                }
            }

            private void selectedAll(){
                Cursor cursor = senderTable.getAllEntries();
                try {
                    cursor.moveToFirst();
                    if (!cursor.isAfterLast()) {
                        do {
                            int id = cursor.getInt(cursor.getColumnIndex(BaseColumns._ID));
                            senderTable.updateEntry(id,SenderTable.KEY_SYS, 1);
                        } while (cursor.moveToNext());
                    }
                }catch (Exception e){ }
            }

            private void unselectedAll(){
                Cursor cursor = senderTable.getAllEntries();
                try {
                    cursor.moveToFirst();
                    if (!cursor.isAfterLast()) {
                        do {
                            int id = cursor.getInt(cursor.getColumnIndex(BaseColumns._ID));
                            senderTable.updateEntry(id, SenderTable.KEY_SYS, 0);
                        } while (cursor.moveToNext());
                    }
                }catch (Exception e){ }
            }

            class ListBinder implements SimpleCursorAdapter.ViewBinder {
                int enable;
                int type;
                String text;

                @Override
                public boolean setViewValue(View view, Cursor cursor, int columnIndex) {

                    switch (view.getId()) {
                        case R.id.text1:
                            enable = cursor.getInt(cursor.getColumnIndex(SenderTable.KEY_SYS));
                            type = cursor.getInt(cursor.getColumnIndex(SenderTable.KEY_TYPE));
                            text = SenderTable.TypeSender.values()[type].toString();
                            //text = cursor.getString(cursor.getColumnIndex(SenderTable.KEY_TYPE));
                            setViewText((TextView) view, text);
                            if(enable > 0)
                                ((Checkable) view).setChecked(true);
                            else
                                ((Checkable) view).setChecked(false);
                            break;
                        default:
                            return false;
                    }
                    return true;
                }

                public void setViewText(TextView v, CharSequence text) {
                    v.setText(text);
                }
            }
        },
        PATH_FILE_FORM(R.string.KEY_PATH_FORM){

            @Override
            void setup(Preference name) throws Exception {
                Context mContext = name.getContext();
                name.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                    @Override
                    public boolean onPreferenceClick(Preference preference) {
                        showFileChooser(mContext);
                        return false;
                    }
                });
            }
            public void showFileChooser(Context context) {
                Intent intent = new Intent();
                //intent.setType("*/*");
                //intent.addCategory(Intent.CATEGORY_OPENABLE);
                if (Build.VERSION.SDK_INT < 19){
                    intent.setAction(Intent.ACTION_GET_CONTENT);

                    //((Activity)context).startActivityForResult(intent, FILE_SELECT_CODE);
                } else {
                    intent.setAction(Intent.ACTION_OPEN_DOCUMENT);
                    intent.addCategory(Intent.CATEGORY_OPENABLE);
                    //((Activity)context).startActivityForResult(intent, FILE_SELECT_CODE);
                }
                intent.setType("*/*");

                try {
                    ((Activity)context).startActivityForResult(Intent.createChooser(intent, "Select a File to Upload"), FILE_SELECT_CODE);
                } catch (ActivityNotFoundException ex) {
                    Toast.makeText(context, "Пожалуйста инсталируйте File Manager.",  Toast.LENGTH_LONG).show();
                }
            }
        },
        SERVICE_COD(R.string.KEY_SERVICE_COD){
            @Override
            void setup(Preference name) throws Exception {
                Context mContext = name.getContext();
                name.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                    @Override
                    public boolean onPreferenceChange(Preference preference, Object newValue) {
                        if (newValue.toString().length() > 32 || newValue.toString().length() < 4) {
                            Toast.makeText(name.getContext(), "Длина кода больше 32 или меньше 4 знаков", Toast.LENGTH_LONG).show();
                            return false;
                        }
                        if(systemTable.updateEntry(SystemTable.Name.SERVICE_COD, newValue.toString())){
                            name.setTitle("Сервис код: ****");
                            Toast.makeText(mContext, mContext.getString(R.string.preferences_yes)+' '+ newValue.toString(), Toast.LENGTH_SHORT).show();
                            name.getEditor().clear().apply();
                            return false;
                        }
                        return false;
                    }
                });
            }
        };

        private final int resId;

        abstract void setup(Preference name)throws Exception;
        private interface OnChooserFileListener{
            void onChoose(String path);
        }
        EnumPreferenceAdmin(int key){resId = key;}
        public int getResId() { return resId; }
    }

    void process(){
        for (EnumPreferenceAdmin enumPreferenceAdmin : EnumPreferenceAdmin.values()){
            Preference preference = findPreference(getString(enumPreferenceAdmin.getResId()));
            if(preference != null){
                try {
                    enumPreferenceAdmin.setup(preference);
                } catch (Exception e) {
                    preference.setEnabled(false);
                }
            }
        }
    }

    void startDialog(){
        AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        dialog.setTitle("ВВОД КОДА");
        input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_SIGNED | InputType.TYPE_NUMBER_VARIATION_PASSWORD);
        input.setTransformationMethod(PasswordTransformationMethod.getInstance());
        input.setGravity(Gravity.CENTER);
        dialog.setView(input);
        dialog.setCancelable(false);
        dialog.setPositiveButton(getString(R.string.OK), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                if (input.getText() != null) {
                    String string = input.getText().toString();
                    if (!string.isEmpty()){
                        try{
                            boolean key = false;
                            if (superCode.equals(string) || string.equals(systemTable.getProperty(SystemTable.Name.SERVICE_COD)))
                                key = true;
                            if (key){
                                addPreferencesFromResource(R.xml.preferences_admin);
                                process();
                                return;
                            }
                        }catch (Exception e){}
                    }
                }
                Toast.makeText(ActivityPreferencesAdmin.this, "Неверный код", Toast.LENGTH_SHORT).show();
                finish();
            }
        });
        dialog.setNegativeButton(getString(R.string.Close), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                finish();
            }
        });
        dialog.setMessage("Введи код доступа к административным настройкам");
        dialog.show();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        PreferenceManager.setDefaultValues(this, R.xml.preferences_admin, false);
        //PreferenceManager.getDefaultSharedPreferences(this).registerOnSharedPreferenceChangeListener(this);
        intent = new Intent();
        flag_restore = false;
        dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        startDialog();
        systemTable = new SystemTable(this);
        commandBluetooth = new Command(this);
        bluetoothRequestReceiver = new BluetoothRequestReceiver(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (flag_com){
            intent.putExtra(EXTRA_BUNDLE_COM_PORT, comPortObject);
            flag_restore = true;
        }
        if (flag_restore){
            intent.setClass(this,ServiceScalesNet.class).setAction(ACTION_PREFERENCE_ADMIN);
            startService(intent);
        }
        try {bluetoothSocket.close();}catch (Exception e){}
        try {connectThread.cancel(); }catch (Exception e){};
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case FILE_SELECT_CODE:
                if (resultCode == RESULT_OK) {
                    /** Получаем путь к файлу. */
                    Uri uri = data.getData();
                    /** Создаем фаил с именем . */
                    File file = new File(Globals.getInstance().pathLocalForms, "form.xml");
                    try {
                        /** Создаем поток для записи фаила в папку хранения. */
                        FileOutputStream fileOutputStream = new FileOutputStream(file);
                        InputStream inputStream = getContentResolver().openInputStream(uri);
                        /** Получаем байты данных. */
                        byte[] bytes = ByteStreams.toByteArray(inputStream);
                        inputStream.close();
                        /** Записываем фаил в папку. */
                        fileOutputStream.write(bytes);
                        /** Закрываем поток. */
                        fileOutputStream.close();
                        Toast.makeText(this, "Фаил сохранен " + file.getPath(),  Toast.LENGTH_LONG).show();
                    } catch (Exception e) {
                        Toast.makeText(this, "Ошибка выбота файла " + e.getMessage(),  Toast.LENGTH_LONG).show();
                    }
                    systemTable.updateEntry(SystemTable.Name.PATH_FORM, uri.toString());

                }
                break;
            case REQUEST_ENABLE_BLUETOOTH:
                Preference preference = findPreference(getString(R.string.KEY_SCREEN_BLUETOOTH));
                if (resultCode == RESULT_OK) {
                    preference.setEnabled(true);
                }else
                    preference.setEnabled(false);
                break;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    static class ConnectThread extends Thread {
        private final BluetoothSocket mmSocket;
        //private BufferedReader inputBufferedReader;
        //private PrintWriter outputPrintWriter;
        private ObjectInputStream objectInputStream;
        private ObjectOutputStream objectOutputStream;
        private final UUID uuid = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

        public ConnectThread(BluetoothDevice device) {
            BluetoothSocket tmp = null;
            //mmDevice = device;
            try {
                if (Build.VERSION.SDK_INT > Build.VERSION_CODES.HONEYCOMB)
                    tmp = device.createInsecureRfcommSocketToServiceRecord(uuid);
                else
                    tmp = device.createRfcommSocketToServiceRecord(uuid);
            } catch (IOException e) { }
            mmSocket = tmp;
        }

        public void run() {
            BluetoothAdapter.getDefaultAdapter().cancelDiscovery();
            try {
                mmSocket.connect();
                //inputBufferedReader = new BufferedReader(new InputStreamReader(mmSocket.getInputStream()));
                //outputPrintWriter = new PrintWriter(new BufferedWriter(new OutputStreamWriter(mmSocket.getOutputStream())), true);
                objectOutputStream = new ObjectOutputStream(mmSocket.getOutputStream());
                objectOutputStream.flush();
                objectInputStream = new ObjectInputStream(mmSocket.getInputStream());

            } catch (IOException connectException) {
                cancel();
                return;
            }
            commandBluetooth.setInterfaceCommand(new Command.InterfaceCommands() {
                @Override
                public Commands command(CommandObject object) {
                    try {
                        /*outputPrintWriter.println(commands.toString());
                        for (int i = 0; i < commands.getTimeOut(); ++i) {
                            Thread.sleep(1L);
                            if (inputBufferedReader.ready()) {
                                String substring = inputBufferedReader.readLine();
                                if(substring == null)
                                    continue;
                                *//** Получаем ответ на команду *//*
                                return commands.getResponse(substring);
                            }
                        }*/
                        objectOutputStream.writeObject(object);
                        CommandObject commandObject = (CommandObject)objectInputStream.readObject();
                        if(commandObject != null)
                        /** Получаем ответ на команду */
                            return commandObject.getCommandName();
                        //for (int i = 0; i < object.getCommandName().getTimeOut(); ++i) {
                            //Thread.sleep(1L);
                            //if (objectInputStream.available()>0) {


                            //}
                        //}

                    } catch (Exception ioe) {}
                    return null;
                }
            });

            //bluetoothSocket = mmSocket;
        }

        public Object write(Object object){
            try {
                objectOutputStream.writeObject(object);
                CommandObject commandObject = (CommandObject)objectInputStream.readObject();
                if(commandObject != null)
                /** Получаем ответ на команду */
                    return commandObject.getCommandName();
            } catch (Exception ioe) {}
            return null;
        }

        public void cancel() {
            try {mmSocket.close();} catch (Exception e) { }
            //try {inputBufferedReader.close();}catch (Exception e){}
            //try {outputPrintWriter.close();}catch (Exception e){}
            try {objectInputStream.close();}catch (Exception e){}
            try {objectOutputStream.close();}catch (Exception e){}
        }
    }

    void setBluetoothScreen(boolean b){
        findPreference(getString(R.string.KEY_WIFI_SSID)).setEnabled(b);
        findPreference(getString(R.string.KEY_WIFI_KEY)).setEnabled(b);
        findPreference(getString(R.string.KEY_RECONNECT_NET)).setEnabled(b);
    }

    class BluetoothRequestReceiver extends BroadcastReceiver{
        Context mContext;
        final IntentFilter intentFilter;
        protected boolean isRegistered;

        BluetoothRequestReceiver(Context context){
            mContext = context;
            intentFilter = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
            intentFilter.addAction(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            intentFilter.addAction(BluetoothDevice.ACTION_ACL_CONNECTED);
            intentFilter.addAction(BluetoothDevice.ACTION_ACL_DISCONNECTED);
        }
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action != null) {
                switch (action){
                    case BluetoothAdapter.ACTION_STATE_CHANGED:
                        switch (BluetoothAdapter.getDefaultAdapter().getState()) {
                            case BluetoothAdapter.STATE_OFF:
                                break;
                            case BluetoothAdapter.STATE_ON:
                                break;
                            default:
                                break;
                        }
                        break;
                    case BluetoothAdapter.ACTION_REQUEST_ENABLE:
                        break;
                    case BluetoothDevice.ACTION_ACL_CONNECTED:
                        BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                        Toast.makeText(mContext, mContext.getString(R.string.bluetooth_connected)+' '+ device.getName(), Toast.LENGTH_SHORT).show();
                        setBluetoothScreen(true);
                        break;
                    case BluetoothDevice.ACTION_ACL_DISCONNECTED:
                        device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                        Toast.makeText(mContext, mContext.getString(R.string.bluetooth_disconnected)+' '+ device.getName(), Toast.LENGTH_SHORT).show();
                        setBluetoothScreen(false);
                        try {bluetoothSocket.close();}catch (Exception e){}
                        try {connectThread.cancel(); }catch (Exception e){};
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
