package com.kostya.scales_client_net.settings;

//import android.content.SharedPreferences;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
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
import com.kostya.scales_client_net.R;
import com.kostya.scales_client_net.provider.SenderTable;
import com.kostya.scales_client_net.provider.SystemTable;
import com.kostya.scales_client_net.service.ServiceScalesNet;

import java.io.*;
import java.util.List;


//import android.preference.PreferenceManager;

public class ActivityPreferencesAdmin extends PreferenceActivity implements SharedPreferences.OnSharedPreferenceChangeListener {
    protected static Dialog dialog;
    private static SystemTable systemTable;
    private EditText input;
    public static Intent intent;

    private static final int FILE_SELECT_CODE = 10;
    private static boolean flag_restore;
    private static final String TAG = ActivityPreferencesAdmin.class.getName();
    private static final String superCode = "343434";
    public static final String ACTION_PREFERENCE_ADMIN = "com.kostya.scales_client_net.settings.ACTION_PREFERENCE_ADMIN";
    public static final String EXTRA_BUNDLE_WIFI = "com.kostya.scales_client_net.settings.EXTRA_BUNDLE_WIFI";
    public static final String EXTRA_BUNDLE_USB = "com.kostya.scales_client_net.settings.EXTRA_BUNDLE_USB";
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

                        if(systemTable.updateEntry(SystemTable.Name.SPEED_PORT, o.toString())){
                            name.setTitle("Скорость: " + o);
                            Toast.makeText(mContext, mContext.getString(R.string.preferences_yes)+' '+ o.toString(), Toast.LENGTH_SHORT).show();
                            return flag_restore = true;
                        }
                        return false;
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
                        if(systemTable.updateEntry(SystemTable.Name.FRAME_PORT, o.toString())){
                            ((ListPreference)name).setValue(o.toString());
                            name.setTitle("Формат: " + o);
                            Toast.makeText(mContext, mContext.getString(R.string.preferences_yes)+' '+ o.toString(), Toast.LENGTH_SHORT).show();
                            return  true;
                        }
                        return false;
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

                        if(systemTable.updateEntry(SystemTable.Name.PARITY_BIT, o.toString())){
                            ((ListPreference)name).setValue(o.toString());
                            name.setTitle("Бит четности: " + o);
                            Toast.makeText(mContext, mContext.getString(R.string.preferences_yes)+' '+ o.toString(), Toast.LENGTH_SHORT).show();
                            return flag_restore = true;
                        }
                        return false;
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
                        name.setTitle("Стоп бит: " + o);
                        ((ListPreference)name).setValue(o.toString());
                        if (systemTable.updateEntry(SystemTable.Name.STOP_BIT, o.toString())){
                            name.setTitle("Стоп бит: " + o);
                            Toast.makeText(mContext, mContext.getString(R.string.preferences_yes)+' '+ o.toString(), Toast.LENGTH_SHORT).show();
                            return flag_restore = true;
                        }
                        return false;
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

                        ((ListPreference)name).setValue(o.toString());
                        if (systemTable.updateEntry(SystemTable.Name.FLOW_CONTROL, o.toString())){
                            name.setTitle("Флов контроль: " + o);
                            Toast.makeText(mContext, mContext.getString(R.string.preferences_yes)+' '+ o.toString(), Toast.LENGTH_SHORT).show();
                            return flag_restore = true;
                        }
                        return false;
                    }
                });
            }
        },
        WIFI_SSID(R.string.KEY_WIFI_SSID){
            @Override
            void setup(Preference name) throws Exception {
                Context mContext = name.getContext();
                name.setTitle(systemTable.getProperty(SystemTable.Name.WIFI_SSID));
                name.setEnabled(false);
                name.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                    @Override
                    public boolean onPreferenceChange(Preference preference, Object o) {
                        if(systemTable.updateEntry(SystemTable.Name.WIFI_SSID, o.toString())){
                            name.setTitle(o.toString());
                            Toast.makeText(mContext, mContext.getString(R.string.preferences_yes)+' '+ o.toString(), Toast.LENGTH_SHORT).show();
                            //intent.putExtra("ssid", o.toString());
                            Bundle bundle = intent.getBundleExtra(EXTRA_BUNDLE_WIFI);
                            if (bundle == null)
                                bundle = new Bundle();
                            bundle.putString(KEY_SSID, o.toString());
                            intent.putExtra(EXTRA_BUNDLE_WIFI, bundle);
                            return flag_restore = true;
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
                name.setTitle(systemTable.getProperty(SystemTable.Name.WIFI_KEY));
                name.setEnabled(false);
                name.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                    @Override
                    public boolean onPreferenceChange(Preference preference, Object o) {

                        if(systemTable.updateEntry(SystemTable.Name.WIFI_KEY, o.toString())){
                            name.setTitle(o.toString());
                            Toast.makeText(mContext, mContext.getString(R.string.preferences_yes)+' '+ o.toString(), Toast.LENGTH_SHORT).show();
                            Bundle bundle = intent.getBundleExtra(EXTRA_BUNDLE_WIFI);
                            if (bundle == null)
                                bundle = new Bundle();
                            bundle.putString(KEY_PASS, o.toString());
                            intent.putExtra(EXTRA_BUNDLE_WIFI, bundle);
                            return flag_restore = true;
                        }

                        preference.setSummary("Ключь сети WiFi: ???");
                        Toast.makeText(name.getContext(), R.string.preferences_no, Toast.LENGTH_SHORT).show();

                        return flag_restore = true;
                    }
                });
            }
        },
        KEY_WIFI_DEFAULT(R.string.KEY_WIFI_DEFAULT){
            @Override
            void setup(Preference name) throws Exception {
                Context mContext = name.getContext();
                try {
                    name.setTitle('"' + getNameOfId(mContext, Integer.valueOf(systemTable.getProperty(SystemTable.Name.WIFI_DEFAULT))) + '"');
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
                                EditTextPreference wifi_ssid = (EditTextPreference)preference.getPreferenceManager().findPreference(preference.getContext().getString(R.string.KEY_WIFI_SSID));
                                wifi_ssid.setTitle("Имя сети WiFi - " + netName);
                                wifi_ssid.getEditor().putString(preference.getContext().getString(R.string.KEY_WIFI_SSID), netName).commit();
                                wifi_ssid.getOnPreferenceChangeListener().onPreferenceChange(preference, netName);
                            }
                            name.setTitle(netName);
                            Toast.makeText(mContext, mContext.getString(R.string.preferences_yes)+' '+ netName, Toast.LENGTH_SHORT).show();
                            return true;
                        }
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
        EnumPreferenceAdmin(int key){
            resId = key;

        }
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
                                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB) {
                                    addPreferencesFromResource(R.xml.admin_preferences);
                                    process();
                                }else {
                                    getFragmentManager().beginTransaction().replace(android.R.id.content, new PrefFragmentAdmin()).commit();
                                }
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
        PreferenceManager.setDefaultValues(this, R.xml.admin_preferences, false);
        PreferenceManager.getDefaultSharedPreferences(this).registerOnSharedPreferenceChangeListener(this);
        intent = new Intent();
        flag_restore = false;
        dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        startDialog();
        systemTable = new SystemTable(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (flag_restore){
            intent.setClass(this,ServiceScalesNet.class).setAction(ACTION_PREFERENCE_ADMIN);
            startService(intent);
        }

    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public static class PrefFragmentAdmin extends PreferenceFragment {

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.admin_preferences);

            initAdminPreference();
        }

        void initAdminPreference(){
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
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String s) {
        /*switch (s){
            case getApplicationContext().getResources().getString(R.string.KEY_SPEED_PORT):
        }*/
    }
}
