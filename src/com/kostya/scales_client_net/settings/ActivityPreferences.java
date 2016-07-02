//Активность настроек
package com.kostya.scales_client_net.settings;

import android.annotation.TargetApi;
import android.content.*;
import android.os.Build;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import com.kostya.scales_client_net.R;

public class ActivityPreferences extends PreferenceActivity implements SharedPreferences.OnSharedPreferenceChangeListener {
    private boolean flagChange;
    enum EnumPreference{
        NULL(R.string.KEY_NULL){
            @Override
            void setup(Preference name)throws Exception {
                name.setSummary( "Ноль");
                name.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                    @Override
                    public boolean onPreferenceClick(Preference preference) {
                        return false;
                    }
                });
            }
        };

        private final int resId;
        abstract void setup(Preference name)throws Exception;

        EnumPreference(int key){
            resId = key;
        }

        public int getResId() { return resId; }
    }

    public void process(){
        for (EnumPreference enumPreference : EnumPreference.values()){
            Preference preference = findPreference(getString(enumPreference.getResId()));
            try {
                enumPreference.setup(preference);
            } catch (Exception e) {
                preference.setEnabled(false);
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        PreferenceManager.getDefaultSharedPreferences(this).registerOnSharedPreferenceChangeListener(this);

        String action = getIntent().getAction();
        //Manage single fragment with action parameter
        if (action != null && "com.kostya.scales_client_net.settings.GENERAL".equals(action)) {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB) {
                addPreferencesFromResource(R.xml.preferences);
                process();
            }else{
                getFragmentManager().beginTransaction().replace(android.R.id.content, new PrefsFragmentPreferences()).commit();
            }

        }else{
            addPreferencesFromResource(R.xml.preferences_legacy);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String s) {
        if(s.equals(getString(R.string.KEY_FILTER)))
            flagChange = true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == RESULT_OK)
            flagChange = true;
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public static class PrefsFragmentPreferences extends PreferenceFragment {

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.preferences);

            initPreferences();
        }

        public void initPreferences(){
            for (EnumPreference enumPreference : EnumPreference.values()){
                Preference preference = findPreference(getString(enumPreference.getResId()));
                if(preference != null){
                    try {
                        enumPreference.setup(preference);
                    } catch (Exception e) {
                        preference.setEnabled(false);
                    }
                }
            }
        }

    }

    /*@TargetApi(Build.VERSION_CODES.HONEYCOMB)
    @Override
    public void onBuildHeaders(List<Header> target) {
        loadHeadersFromResource(R.xml.preferences_headers, target);
    }*/
}
