package com.kostya.scales_client_net;
/** Удалил бяку */
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.util.Log;
import com.kostya.scales_client_net.provider.EventsTable;
import com.kostya.scales_client_net.settings.Preferences;
import com.kostya.terminals.TerminalObject;

import java.io.File;
import java.util.List;

/** Created by Kostya on 23.01.2016.
 * @author Kostya
 */
public class Globals {
    private static Globals instance = new Globals();
    public static File pathLocalForms;
    /** Настройки для весов. */
    protected Preferences preferencesScales;
    protected PackageInfo packageInfo;
    private TerminalObject currentTerminal;
    public List<TerminalObject> terminalObjectList;
    /** Версия программы весового модуля. */
    private final int microSoftware = 4;
    protected String networkOperatorName;
    protected String simNumber;
    protected String telephoneNumber;
    protected String networkCountry;
    protected int versionNumber;
    protected String versionName = "";

    private String weight;
    /** Папка для хранения локальных данных программы. */
    public static String FOLDER_LOCAL_FORMS = "forms";
    private static  final String TAG = Globals.class.getName();
    /** Флаг есть соединение */
    private boolean isScalesConnect;

    public void initialize(Context context) {
        try {
            PackageManager packageManager = context.getPackageManager();
            packageInfo = packageManager.getPackageInfo(context.getPackageName(), 0);
            versionNumber = packageInfo.versionCode;
            versionName = packageInfo.versionName;
        } catch (PackageManager.NameNotFoundException e) {
            Log.e(TAG, e.getMessage());
        }

        /** Создаем путь к временной папке для для хранения файлов. */
        //pathLocalForms = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES) + File.separator + FOLDER_LOCAL_FORMS);
        /** Создаем путь к папке для для хранения файлов с данными формы google disk form. */
        pathLocalForms = new File(context.getFilesDir() + File.separator + FOLDER_LOCAL_FORMS);
        /** Если нет папки тогда создаем. */
        if (!pathLocalForms.exists()) {
            if (!pathLocalForms.mkdirs()) {
                new EventsTable(context).insertNewEvent("Путь не созданый: " + pathLocalForms.getPath(), EventsTable.Event.PATH_STORE);
            }
        }

    }

    public boolean isScalesConnect() {
        return isScalesConnect;
    }

    public void setScalesConnect(boolean connect) {
        isScalesConnect = connect;
    }

    public PackageInfo getPackageInfo() {
        return packageInfo;
    }

    public int getVersionNumber() {
        return versionNumber;
    }

    public String getVersionName() {
        return versionName;
    }

    public String getNetworkOperatorName() {
        return networkOperatorName;
    }

    public void setNetworkOperatorName(String networkOperatorName) {
        this.networkOperatorName = networkOperatorName;
    }

    public String getTelephoneNumber() {
        return telephoneNumber;
    }

    public void setTelephoneNumber(String telephoneNumber) {
        this.telephoneNumber = telephoneNumber;
    }

    public void setSimNumber(String simNumber) {
        this.simNumber = simNumber;
    }

    public void setNetworkCountry(String networkCountry) {
        this.networkCountry = networkCountry;
    }

    public int getMicroSoftware() { return microSoftware; }

    public String getWeight() {
        return weight;
    }

    public void setWeight(String weight) {
        this.weight = weight;
    }

    public Preferences getPreferencesScales() { return preferencesScales; }

    public static Globals getInstance() { return instance; }

    public static void setInstance(Globals instance) { Globals.instance = instance; }

    public void setCurrentTerminal(TerminalObject terminal) {this.currentTerminal = terminal;}

    public TerminalObject getCurrentTerminal() {return currentTerminal;}
}
