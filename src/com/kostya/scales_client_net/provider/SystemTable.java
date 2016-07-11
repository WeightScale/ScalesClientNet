package com.kostya.scales_client_net.provider;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.net.Uri;
import android.provider.BaseColumns;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class SystemTable {
    private final Context mContext;
    final ContentResolver contentResolver;

    public static final String TABLE = "systemTable";

    public static final String KEY_ID               = BaseColumns._ID;
    public static final String KEY_DATE             = "date";
    public static final String KEY_TIME             = "time";
    public static final String KEY_NAME             = "name";
    public static final String KEY_DATA             = "data";

    public enum Name{
        APP_PASSWORD("app_password"),
        SHEET_GOOGLE("sheet"),
        USER_GOOGLE("user"),
        PASSWORD("password"),
        PHONE("phone"),
        SPEED_PORT("speed_port"),
        FRAME_PORT("frame_port"),
        PARITY_BIT("parity_bit"),
        STOP_BIT("stop_bit"),
        FLOW_CONTROL("flow_control"),
        SERVICE_COD("service_cod"),
        WIFI_SSID("wifi_ssid"),
        //WIFI_KEY("wifi_key"),
        WIFI_DEFAULT("wifi_default"),
        PATH_FORM("path_form");

        public String getName() { return name; }

        private final String name;
        Name(String n){name = n;}
    }

    private static final String[] All_COLUMN_TABLE = {
            KEY_ID,
            KEY_DATE,
            KEY_TIME,
            KEY_NAME,
            KEY_DATA};

    public static final String TABLE_CREATE = "create table "
            + TABLE + " ("
            + KEY_ID + " integer primary key autoincrement, "
            + KEY_DATE + " text,"
            + KEY_TIME + " text,"
            + KEY_NAME + " integer,"
            + KEY_DATA + " text );";


    private static final Uri CONTENT_URI = Uri.parse("content://" + ScalesClientNetProvider.AUTHORITY + '/' + TABLE);

    /** Конструктор класса.
     * @param context Контекст.
     */
    public SystemTable(Context context) {
        mContext = context;
        contentResolver = mContext.getContentResolver();
    }

    /** Добавляем новую запись настройки.
     * @param name Имя настройки.
     * @param data Данные настройки.
     * @throws SQLiteException Исключение при ошибки добавления.
     */
    private void insertNewEntry(Name name, String data) throws SQLiteException {
        ContentValues newTaskValues = new ContentValues();
        Date date = new Date();
        newTaskValues.put(KEY_DATE, new SimpleDateFormat("dd.MM.yyyy", Locale.getDefault()).format(date));
        newTaskValues.put(KEY_TIME, new SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(date));
        newTaskValues.put(KEY_NAME, name.ordinal());
        newTaskValues.put(KEY_DATA, data);
        contentResolver.insert(CONTENT_URI, newTaskValues);
    }

    /** Обновляем если нет записи то добавляем.
     * @param name Имя настройки.
     * @param data Данные настройки.
     * @return Возвращяем true если обновили.
     */
    public boolean updateEntry(Name name, String data) {
        boolean flagUpdate = false;
        StringBuilder event = new StringBuilder();
        Cursor cursor = contentResolver.query(CONTENT_URI, All_COLUMN_TABLE, KEY_NAME + "= " + name.ordinal(), null, null);
        try {
            if ((cursor != null ? cursor.getCount() : 0) > 0) {
                cursor.moveToFirst();
                if (!cursor.isAfterLast()) {
                    int id = cursor.getInt(cursor.getColumnIndex(KEY_ID));
                    if (update(id, name, data)){
                        flagUpdate =  true;
                    }
                }
            }else {
                try {
                    insertNewEntry(name, data);
                    flagUpdate = true;
                }catch (SQLiteException e){
                    event = new StringBuilder(e.getMessage());
                    flagUpdate = false;
                }
            }
            cursor.close();
        }catch (Exception e){
            event = new StringBuilder(e.getMessage());
            return false;
        }
        if (flagUpdate){
            event = new StringBuilder(name.getName());
            event.append(' ').append(data);
            new EventsTable(mContext).insertNewEvent(event.toString(), EventsTable.Event.UPDATE_SYSTEM);
        }else {
            new EventsTable(mContext).insertNewEvent(event.toString(), EventsTable.Event.NOT_UPDATE_SYSTEM);
        }
        return flagUpdate;
    }

    /** Обновляем настройку.
     * @param _rowIndex Индекс записи.
     * @param name Имя настройки.
     * @param data Данные настройки.
     * @return Возвращяем true если обновили.
     */
    private boolean update(int _rowIndex, Name name, String data) {
        //boolean b;
        Uri uri = ContentUris.withAppendedId(CONTENT_URI, _rowIndex);
        Date date = new Date();
        try {
            ContentValues updateValues = new ContentValues();
            updateValues.put(KEY_DATE, new SimpleDateFormat("dd.MM.yyyy", Locale.getDefault()).format(date));
            updateValues.put(KEY_TIME, new SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(date));
            updateValues.put(KEY_NAME, name.ordinal());
            updateValues.put(KEY_DATA, data);
            return contentResolver.update(uri, updateValues, null, null) > 0;
        } catch (Exception e) {
            return false;
        }
    }

    /** Получаем настройку по имени.
     * @param name Имя настройки.
     * @return Данные настройки.
     * @throws Exception Исключение ошибка при получении настройки.
     */
    public String getProperty(Name name) throws Exception{
        Cursor cursor = contentResolver.query(CONTENT_URI, new String[]{KEY_ID,KEY_DATA}, KEY_NAME + "= " + name.ordinal(), null, null);
        if ((cursor != null ? cursor.getCount() : 0) > 0) {
            cursor.moveToFirst();
            if (!cursor.isAfterLast()) {
                return cursor.getString(cursor.getColumnIndex(KEY_DATA));
            }
        }
        throw new  Exception("Ошибка получения настройки.");
    }

    /** Добавляем настройки по умолчанию.
     * @param db База данных.
     */
    public void addDefault(SQLiteDatabase db){
        insert(db, Name.SPEED_PORT, "9600");
        insert(db, Name.FRAME_PORT, "8");
        insert(db, Name.STOP_BIT, "1");
        insert(db, Name.PARITY_BIT, "none");
        insert(db, Name.FLOW_CONTROL, "OFF");
        insert(db, Name.SERVICE_COD, "1234");
        insert(db, Name.SHEET_GOOGLE, "spread_sheet");
        insert(db, Name.USER_GOOGLE, "user_account_google");
        insert(db, Name.PHONE, "0500000000");
        insert(db, Name.WIFI_SSID, "SSID");
        //insert(db, Name.WIFI_KEY, "12345678");
        insert(db, Name.WIFI_DEFAULT, "0");
    }

    /** Вставляем настройку в таблицу.
     * @param db База данных.
     * @param name Имя настройки.
     * @param data Данные настройки.
     * @throws SQLiteException
     */
    private void insert(SQLiteDatabase db,Name name, String data) throws SQLiteException {
        ContentValues newTaskValues = new ContentValues();
        Date date = new Date();
        newTaskValues.put(KEY_DATE, new SimpleDateFormat("dd.MM.yyyy", Locale.getDefault()).format(date));
        newTaskValues.put(KEY_TIME, new SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(date));
        newTaskValues.put(KEY_NAME, name.ordinal());
        newTaskValues.put(KEY_DATA, data);
        db.insert(TABLE,null, newTaskValues);
    }

}
