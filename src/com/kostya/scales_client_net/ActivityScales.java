package com.kostya.scales_client_net;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.*;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.*;
import com.kostya.scales_client_net.service.ServiceScalesNet;
import com.kostya.scales_client_net.settings.ActivityPreferences;

/**
 * @author Kostya
 */
public class ActivityScales extends Activity implements View.OnClickListener{
    ImageView buttonBack;
    TextView textViewWeight ;
    Receiver receiver;
    private static final int FILE_SELECT_CODE = 10;
    private static  final String TAG = ActivityScales.class.getName();
    public static final String WEIGHT = "com.kostya.scales_client_net.WEIGHT";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        buttonBack = (ImageView)findViewById(R.id.buttonBack);
        buttonBack.setOnClickListener(this);

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

        findViewById(R.id.imageMenu).setOnClickListener(this);
        receiver = new Receiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction(ActivityScales.WEIGHT);
        receiver.register(getApplicationContext(),filter);
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
        receiver.unregister(getApplicationContext());
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
            case R.id.bluetooth:
                BluetoothBaseManager.start(getApplicationContext());
                break;

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

    class Receiver extends BroadcastReceiver{
        protected boolean isRegistered;

        @Override
        public void onReceive(Context context, Intent intent) {
            if(intent.getAction().equals(ActivityScales.WEIGHT)) {
                String weight = intent.getStringExtra("weight");
                weight.trim();
                try {
                    textViewWeight.setText(weight.substring(0,weight.indexOf("(")));
                }catch (Exception e){}

            }
        }

        public Intent register(Context context, IntentFilter filter) {
            isRegistered = true;
            return context.registerReceiver(this, filter);
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

}
