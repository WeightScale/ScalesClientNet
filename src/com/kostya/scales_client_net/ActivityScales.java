package com.kostya.scales_client_net;

import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.*;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.TextAppearanceSpan;
import android.util.Log;
import android.view.*;
import android.widget.*;
import com.kostya.scales_client_net.service.ServiceScalesNet;
import com.kostya.scales_client_net.settings.ActivityPreferences;
import com.kostya.scales_client_net.transferring.DataTransferringManager;

import javax.jmdns.ServiceInfo;
import java.util.List;

/**
 * @author Kostya
 */
public class ActivityScales extends Activity implements View.OnClickListener{
    private ImageView buttonBack;
    private TextView textViewWeight;
    private Spinner spinnerServers;
    private SpinnerAdapter spinnerAdapter;
    private BaseReceiver baseReceiver;
    private SpannableStringBuilder textKg;
    private static final int FILE_SELECT_CODE = 10;
    private static  final String TAG = ActivityScales.class.getName();
    public static final String ACTION_WEIGHT = "com.kostya.scales_client_net.WEIGHT";
    public static final String ACTION_UPDATE_SERVER_LIST = "com.kostya.scales_client_net.UPDATE_SERVER_LIST";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        buttonBack = (ImageView)findViewById(R.id.buttonBack);
        buttonBack.setOnClickListener(this);

        textKg = new SpannableStringBuilder(getResources().getString(R.string.scales_kg));
        textKg.setSpan(new TextAppearanceSpan(this, R.style.SpanTextKg),0,textKg.length(), Spanned.SPAN_INCLUSIVE_INCLUSIVE);

        textViewWeight = (TextView)findViewById(R.id.weightTextView);
        textViewWeight.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                //startActivityForResult(new Intent(Settings.ACTION_WIFI_SETTINGS), 0);
                //final ComponentName toLaunch = new ComponentName("com.android.settings",Settings.ACTION_WIFI_SETTINGS);
                final Intent intent = new Intent(Settings.ACTION_WIFI_SETTINGS);
                //intent.addCategory(Intent.CATEGORY_LAUNCHER);
                //intent.setComponent(toLaunch);
                //intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivityForResult(intent,0);
                return false;
            }
        });

        spinnerServers = (Spinner)findViewById(R.id.spinnerServer);
        loadTypeSpinnerData();

        findViewById(R.id.imageMenu).setOnClickListener(this);
        baseReceiver = new BaseReceiver(getApplicationContext());
        baseReceiver.register();
        /** Запускаем главный сервис. */
        startService(new Intent(this,ServiceScalesNet.class));
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.buttonBack:
                onBackPressed();
                break;
            case R.id.imageMenu:
                openOptionsMenu();
                break;
            default:
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        baseReceiver.unregister();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.preferences:
                startActivity(new Intent(this, ActivityPreferences.class));
                break;
            case R.id.exit:
                AlertDialog.Builder dialog = new AlertDialog.Builder(this);
                dialog.setTitle(getString(R.string.scale_off));
                dialog.setCancelable(false);
                dialog.setPositiveButton(getString(R.string.OK), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        if (i == DialogInterface.BUTTON_POSITIVE) {
                            stopService(new Intent(ActivityScales.this, ServiceScalesNet.class));
                            //todo сделать что то для выключения весов
                            finish();
                        }
                    }
                });
                dialog.setNegativeButton(getString(R.string.Close), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        //finish();
                    }
                });
                dialog.setMessage(getString(R.string.TEXT_MESSAGE));
                dialog.show();
                break;
            default:

        }
        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case FILE_SELECT_CODE:
                if (resultCode == RESULT_OK) {
                    // Get the Uri of the selected file
                    Uri uri = data.getData();
                    Log.d(TAG, "File Uri: " + uri.toString());
                    // Get the path
                    String path = uri.getPath();
                    //String path = File.getPath(this, uri);
                    Log.d(TAG, "File Path: " + path);
                    // Get the file instance
                    // File file = new File(path);
                    // Initiate the upload
                }
                break;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    public void showFileChooser() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("*/*");
        intent.addCategory(Intent.CATEGORY_OPENABLE);

        try {
            startActivityForResult(Intent.createChooser(intent, "Select a File to Upload"), FILE_SELECT_CODE);
        } catch (ActivityNotFoundException ex) {
            // Potentially direct the user to the Market with a Dialog
            Toast.makeText(this, "Please install a File Manager.",  Toast.LENGTH_SHORT).show();
        }
    }

    public void loadTypeSpinnerData() {
        spinnerAdapter = new SpinnerAdapter(this, R.layout.type_spinner, Main.getInstance().getDataTransferring().getListServers());
        spinnerAdapter.notifyDataSetChanged();
        spinnerServers.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                ServiceInfo serviceInfo = (ServiceInfo) adapterView.getItemAtPosition(i);
                Main.getInstance().getDataTransferring().setCurrentServer(serviceInfo);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
        spinnerServers.setAdapter(spinnerAdapter);
    }

    class SpinnerAdapter extends ArrayAdapter<ServiceInfo> implements android.widget.SpinnerAdapter{

        public SpinnerAdapter(Context context, int resource, List<ServiceInfo> list) {
            super(context, resource, list);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            LayoutInflater inflater=getLayoutInflater();
            View view=inflater.inflate(R.layout.type_spinner, parent, false);
            ServiceInfo s = getItem(position);
            TextView label=(TextView)view.findViewById(R.id.text1);
            label.setText(s.getName());

            return view;
        }

        public View getDropDownView(int position, View convertView, ViewGroup parent) {
            return getCustomView(position, convertView, parent);
            /*ServiceInfo s = getItem(position);
            TextView v = (TextView) super.getView(position, convertView, parent);
            //Typeface myTypeFace = Typeface.createFromAsset(context.getAssets(), "fonts/gilsanslight.otf");
            //v.setTypeface(myTypeFace);
            v.setText(s.getName());
            return v;*/
        }

        public View getCustomView(int position, View convertView, ViewGroup parent) {

            LayoutInflater inflater=getLayoutInflater();
            View row=inflater.inflate(R.layout.type_spinner_dropdown_item, parent, false);
            ServiceInfo s = getItem(position);
            TextView label=(TextView)row.findViewById(R.id.text1);
            label.setText(s.getName()+" "+s.getPropertyString(DataTransferringManager.SERVICE_INFO_PROPERTY_IP_VERSION));
            /*TextView sub=(TextView)row.findViewById(R.id.sub);
            sub.setText(subs[position]);

            ImageView icon=(ImageView)row.findViewById(R.id.image);
            icon.setImageResource(arr_images[position]);*/
            return row;
        }

        @Override
        public void notifyDataSetChanged() {
            super.notifyDataSetChanged();
        }
    }

    class BaseReceiver extends BroadcastReceiver{
        Context mContext;
        final IntentFilter intentFilter;
        protected boolean isRegistered;

        BaseReceiver(Context context){
            mContext = context;
            intentFilter = new IntentFilter(ACTION_UPDATE_SERVER_LIST);
            intentFilter.addAction(ACTION_WEIGHT);
        }

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action != null) {
                switch (action){
                    case ACTION_UPDATE_SERVER_LIST:
                        if (spinnerAdapter!= null)
                            spinnerAdapter.notifyDataSetChanged();
                    break;
                    case ACTION_WEIGHT:
                        String weight = intent.getStringExtra("weight");
                        weight.trim();
                        try {
                            weight = weight.substring(0,weight.indexOf("("));
                            SpannableStringBuilder spannableWeightText = new SpannableStringBuilder(weight);
                            spannableWeightText.setSpan(new TextAppearanceSpan(ActivityScales.this, R.style.SpanTextWeight),0,weight.length(), Spanned.SPAN_INCLUSIVE_INCLUSIVE);
                            spannableWeightText.append(textKg);
                            textViewWeight.setText(spannableWeightText, TextView.BufferType.SPANNABLE);
                        }catch (Exception e){}
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
