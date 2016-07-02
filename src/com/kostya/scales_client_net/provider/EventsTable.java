package com.kostya.scales_client_net.provider;

import android.content.*;
import android.database.Cursor;
import android.database.sqlite.SQLiteException;
import android.net.Uri;
import android.provider.BaseColumns;
import com.kostya.scales_client_net.Main;
import com.kostya.scales_client_net.task.IntentServiceHttpPost;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class EventsTable {
    private final Context mContext;
    final ContentResolver contentResolver;

    public static final String TABLE = "eventsTable";

    public static final String KEY_ID               = BaseColumns._ID;
    public static final String KEY_DATE             = "date";
    public static final String KEY_TIME             = "time";
    public static final String KEY_DEVICE_ID        = "deviceId";
    public static final String KEY_EVENT            = "event";
    public static final String KEY_EVENT_TEXT       = "eventText";
    public static final String KEY_STATE            = "state";
    public static final String KEY_VISIBILITY       = "visibility";

    public static final int INVISIBLE = 0;
    public static final int VISIBLE = 1;

    public enum Event{
        PORT_SCALE_OUT("ПОЛУЧЕНЫЕ ДАННЫЕ"),
        PORT_SCALE_IN("ОТПРАВЛЕНЫЕ ДАННЫЕ"),
        UPDATE_SYSTEM("Обновлены настройки"),
        NOT_UPDATE_SYSTEM("Ошибка обновления настройки"),
        USB_EVENT("События USB"),
        WIFI_EVENT("События WiFi"),
        PATH_STORE("Хранение файлов");

        private final String text;
        Event(String event){text = event;}
    }

    /** Стадии события. */
    public enum State{
        /** Предварительный. */
        CHECK_PRELIMINARY("ПРЕДВАРИТЕЛЬНЫЙ"),
        /** Готовый. */
        CHECK_READY("ГОТОВЫЙ"),
        /** Сохранен на сервере. */
        CHECK_ON_SERVER("НА СЕРВЕРЕ");

        public String getText() {
            return text;
        }

        private final String text;

        State(String t) {
            text = t;
        }
    }

    private static final String[] All_COLUMN_TABLE = {
            KEY_ID,
            KEY_DATE,
            KEY_TIME,
            KEY_DEVICE_ID,
            KEY_EVENT,
            KEY_EVENT_TEXT,
            KEY_STATE,
            KEY_VISIBILITY};

    public static final String[] COLUMNS_SHEET = {
            //KEY_ID,
            KEY_DATE,
            KEY_TIME,
            KEY_DEVICE_ID,
            KEY_EVENT,
            KEY_EVENT_TEXT};

    public static final String TABLE_CREATE = "create table "
            + TABLE + " ("
            + KEY_ID + " integer primary key autoincrement, "
            + KEY_DATE + " text,"
            + KEY_TIME + " text,"
            + KEY_DEVICE_ID + " text,"
            + KEY_EVENT + " integer,"
            + KEY_EVENT_TEXT + " text,"
            + KEY_STATE + " integer,"
            + KEY_VISIBILITY + " integer );";


    private static final Uri CONTENT_URI = Uri.parse("content://" + ScalesClientNetProvider.AUTHORITY + '/' + TABLE);

    public EventsTable(Context context) {
        mContext = context;
        contentResolver = mContext.getContentResolver();
    }

    public Uri insertNewEvent(String text, Event event) {
        ContentValues newTaskValues = new ContentValues();
        Date date = new Date();
        newTaskValues.put(KEY_DATE, new SimpleDateFormat("dd.MM.yyyy", Locale.getDefault()).format(date));
        newTaskValues.put(KEY_TIME, new SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(date));
        newTaskValues.put(KEY_DEVICE_ID, ((Main) mContext.getApplicationContext()).getDeviceId());
        newTaskValues.put(KEY_EVENT_TEXT, text.trim());
        newTaskValues.put(KEY_EVENT, event.ordinal());
        newTaskValues.put(KEY_STATE, State.CHECK_PRELIMINARY.ordinal());
        newTaskValues.put(KEY_VISIBILITY, VISIBLE);
        Uri uri;
        try {
            uri =  contentResolver.insert(CONTENT_URI, newTaskValues);
            mContext.startService(new Intent(mContext, IntentServiceHttpPost.class).setAction(IntentServiceHttpPost.ACTION_EVENT_TABLE));
        }catch (SQLiteException e){
            throw new SQLiteException(e.getMessage());
        }
        return uri;
    }

    public Cursor getEntryItem(int _rowIndex, String... columns) {
        Uri uri = ContentUris.withAppendedId(CONTENT_URI, _rowIndex);
        try {
            Cursor result = contentResolver.query(uri, columns, null, null, null);
            result.moveToFirst();
            return result;
        } catch (Exception e) {
            return null;
        }
    }

    public boolean updateEntry(int _rowIndex, String key, int in) {
        //boolean b;
        Uri uri = ContentUris.withAppendedId(CONTENT_URI, _rowIndex);
        try {
            ContentValues newValues = new ContentValues();
            newValues.put(key, in);
            return contentResolver.update(uri, newValues, null, null) > 0;
        } catch (Exception e) {
            return false;
        }
    }

    public Cursor getPreliminary() {
        return contentResolver.query(CONTENT_URI, null, KEY_STATE + "= " + State.CHECK_PRELIMINARY.ordinal(), null, null);
    }
}
