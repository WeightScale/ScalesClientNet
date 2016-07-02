package com.kostya.scales_client_net.provider;

import android.content.*;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.provider.BaseColumns;
import android.text.TextUtils;

/**
 * User: Kostya
 * Date: 21.06.16
 * @author Kostya
 */
public class ScalesWiFiNetProvider extends ContentProvider {

    private static final String DATABASE_NAME = "scalesWifiNet.db";
    private static final int DATABASE_VERSION = 1;
    protected static final String AUTHORITY = "com.kostya.scaleswifinet.scalesWifiNet";
    private static final String DROP_TABLE_IF_EXISTS = "DROP TABLE IF EXISTS ";

    private static final int ALL_ROWS = 1;
    private static final int SINGLE_ROWS = 2;

    private enum TableList {
        CHECKS_LIST,
        CHECKS_ID,
        SENDER_LIST,
        SENDER_ID,
        SYSTEM_LIST,
        SYSTEM_ID,
        TASK_LIST,
        TASK_ID
    }

    private static final UriMatcher uriMatcher;
    private SQLiteDatabase db;

    static {
        uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        uriMatcher.addURI(AUTHORITY, EventsTable.TABLE, TableList.CHECKS_LIST.ordinal());
        uriMatcher.addURI(AUTHORITY, EventsTable.TABLE + "/#", TableList.CHECKS_ID.ordinal());
        uriMatcher.addURI(AUTHORITY, SystemTable.TABLE, TableList.SYSTEM_LIST.ordinal());
        uriMatcher.addURI(AUTHORITY, SystemTable.TABLE + "/#", TableList.SYSTEM_ID.ordinal());
        uriMatcher.addURI(AUTHORITY, TaskTable.TABLE, TableList.TASK_LIST.ordinal());
        uriMatcher.addURI(AUTHORITY, TaskTable.TABLE + "/#", TableList.TASK_ID.ordinal());
        /*uriMatcher.addURI(AUTHORITY, TypeTable.TABLE, TableList.TYPE_LIST.ordinal());
        uriMatcher.addURI(AUTHORITY, TypeTable.TABLE + "/#", TableList.TYPE_ID.ordinal());
        uriMatcher.addURI(AUTHORITY, ErrorTable.TABLE, TableList.ERROR_LIST.ordinal());
        uriMatcher.addURI(AUTHORITY, ErrorTable.TABLE + "/#", TableList.ERROR_ID.ordinal());
        uriMatcher.addURI(AUTHORITY, CommandTable.TABLE, TableList.COMMAND_LIST.ordinal());
        uriMatcher.addURI(AUTHORITY, CommandTable.TABLE + "/#", TableList.COMMAND_ID.ordinal());*/
        uriMatcher.addURI(AUTHORITY, SenderTable.TABLE, TableList.SENDER_LIST.ordinal());
        uriMatcher.addURI(AUTHORITY, SenderTable.TABLE + "/#", TableList.SENDER_ID.ordinal());
    }

    /*public void vacuum(){
        db.execSQL("VACUUM");
    }*/

    private String getTable(Uri uri) {
        switch (TableList.values()[uriMatcher.match(uri)]) {
            case CHECKS_LIST:
            case CHECKS_ID:
                return EventsTable.TABLE; // return
            case SYSTEM_LIST:
            case SYSTEM_ID:
                return SystemTable.TABLE; // return
            case TASK_LIST:
            case TASK_ID:
                return TaskTable.TABLE; // return
            /*case TYPE_LIST:
            case TYPE_ID:
                return TypeTable.TABLE; // return
            case ERROR_LIST:
            case ERROR_ID:
                return ErrorTable.TABLE; // return
            case COMMAND_LIST:
            case COMMAND_ID:
                return CommandTable.TABLE;*/ // return
            case SENDER_LIST:
            case SENDER_ID:
                return SenderTable.TABLE; // return
            /** PROVIDE A DEFAULT CASE HERE **/
            default:
                // If the URI doesn't match any of the known patterns, throw an exception.
                throw new IllegalArgumentException("Unknown URI " + uri);
        }
    }

    @Override
    public boolean onCreate() {
        DBHelper dbHelper = new DBHelper(getContext());
        //db = dbHelper.getWritableDatabase();
        db = dbHelper.getReadableDatabase();
        if (db != null) {
            db.setLockingEnabled(false);
        }
        return true;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sort) {
        SQLiteQueryBuilder queryBuilder = new SQLiteQueryBuilder();

        switch (TableList.values()[uriMatcher.match(uri)]) {
            case CHECKS_LIST: // общий Uri
                queryBuilder.setTables(EventsTable.TABLE);
                break;
            case CHECKS_ID: // Uri с ID
                queryBuilder.setTables(EventsTable.TABLE);
                queryBuilder.appendWhere(BaseColumns._ID + '=' + uri.getLastPathSegment());
                break;
            case SYSTEM_LIST: // общий Uri
                queryBuilder.setTables(SystemTable.TABLE);
                break;
            case SYSTEM_ID: // Uri с ID
                queryBuilder.setTables(SystemTable.TABLE);
                queryBuilder.appendWhere(BaseColumns._ID + '=' + uri.getLastPathSegment());
                break;
            case TASK_LIST: // общий Uri
                queryBuilder.setTables(TaskTable.TABLE);
                break;
            case TASK_ID: // Uri с ID
                queryBuilder.setTables(TaskTable.TABLE);
                queryBuilder.appendWhere(BaseColumns._ID + '=' + uri.getLastPathSegment());
                break;
            /*case TYPE_LIST: // общий Uri
                queryBuilder.setTables(TypeTable.TABLE);
                break;
            case TYPE_ID: // Uri с ID
                queryBuilder.setTables(TypeTable.TABLE);
                queryBuilder.appendWhere(BaseColumns._ID + '=' + uri.getLastPathSegment());
                break;

            case ERROR_LIST: // общий Uri
                queryBuilder.setTables(ErrorTable.TABLE);
                break;
            case ERROR_ID: // Uri с ID
                queryBuilder.setTables(ErrorTable.TABLE);
                queryBuilder.appendWhere(BaseColumns._ID + '=' + uri.getLastPathSegment());
                break;
            case COMMAND_LIST: // общий Uri
                queryBuilder.setTables(CommandTable.TABLE);
                break;
            case COMMAND_ID: // Uri с ID
                queryBuilder.setTables(CommandTable.TABLE);
                queryBuilder.appendWhere(BaseColumns._ID + '=' + uri.getLastPathSegment());
                break;*/
            case SENDER_LIST: // общий Uri
                queryBuilder.setTables(SenderTable.TABLE);
                break;
            case SENDER_ID: // Uri с ID
                queryBuilder.setTables(SenderTable.TABLE);
                queryBuilder.appendWhere(BaseColumns._ID + '=' + uri.getLastPathSegment());
                break;
            default:
                throw new IllegalArgumentException("Wrong URI: " + uri);
        }

        Cursor cursor = queryBuilder.query(db, projection, selection, selectionArgs, null, null, sort);
        if (cursor == null) {
            return null;
        }
        Context context = getContext();
        if (context != null) {
            ContentResolver contentResolver = context.getContentResolver();
            if (contentResolver != null) {
                cursor.setNotificationUri(contentResolver, uri);
            }
        }
        return cursor;
    }

    @Override
    public String getType(Uri uri) {
        switch (uriMatcher.match(uri)) {
            case ALL_ROWS:
                return "vnd.android.cursor.dir/vnd.";
            case SINGLE_ROWS:
                return "vnd.android.cursor.item/vnd.";
            default:
                return null;
        }
    }

    @Override
    public Uri insert(Uri uri, ContentValues contentValues) throws SQLiteException{

        long rowID = db.insert(getTable(uri), null, contentValues);
        if (rowID > 0L) {
            Uri resultUri = ContentUris.withAppendedId(uri, rowID);
            Context context = getContext();
            if (context != null) {
                context.getContentResolver().notifyChange(resultUri, null);
                return resultUri;
            }
        }
        throw new SQLiteException("Ошибка добавления записи " + uri);
    }

    @Override
    public int delete(Uri uri, String where, String[] whereArg) {
        int delCount;
        String id;


        switch (TableList.values()[uriMatcher.match(uri)]) {
            case CHECKS_LIST: // общий Uri
                delCount = db.delete(EventsTable.TABLE, where, whereArg);
                break;
            case CHECKS_ID:
                id = uri.getLastPathSegment();
                where = TextUtils.isEmpty(where) ? BaseColumns._ID + " = " + id : where + " AND " + BaseColumns._ID + " = " + id;
                delCount = db.delete(EventsTable.TABLE, where, whereArg);
                break;
            case SYSTEM_LIST: // общий Uri
                delCount = db.delete(SystemTable.TABLE, where, whereArg);
                break;
            case SYSTEM_ID:
                id = uri.getLastPathSegment();
                where = TextUtils.isEmpty(where) ? BaseColumns._ID + " = " + id : where + " AND " + BaseColumns._ID + " = " + id;
                delCount = db.delete(SystemTable.TABLE, where, whereArg);
                break;
            case TASK_LIST:
                delCount = db.delete(TaskTable.TABLE, where, whereArg);
                break;
            case TASK_ID:
                id = uri.getLastPathSegment();
                where = TextUtils.isEmpty(where) ? BaseColumns._ID + " = " + id : where + " AND " + BaseColumns._ID + " = " + id;
                delCount = db.delete(TaskTable.TABLE, where, whereArg);
                break;
            /*case TYPE_LIST:
                delCount = db.delete(TypeTable.TABLE, where, whereArg);
                break;
            case TYPE_ID:
                id = uri.getLastPathSegment();
                where = TextUtils.isEmpty(where) ? BaseColumns._ID + " = " + id : where + " AND " + BaseColumns._ID + " = " + id;
                delCount = db.delete(TypeTable.TABLE, where, whereArg);
                break;

            case ERROR_LIST:
                delCount = db.delete(ErrorTable.TABLE, where, whereArg);
                break;
            case ERROR_ID:
                id = uri.getLastPathSegment();
                where = TextUtils.isEmpty(where) ? BaseColumns._ID + " = " + id : where + " AND " + BaseColumns._ID + " = " + id;
                delCount = db.delete(ErrorTable.TABLE, where, whereArg);
                break;
            case COMMAND_LIST:
                delCount = db.delete(CommandTable.TABLE, where, whereArg);
                break;
            case COMMAND_ID:
                id = uri.getLastPathSegment();
                where = TextUtils.isEmpty(where) ? BaseColumns._ID + " = " + id : where + " AND " + BaseColumns._ID + " = " + id;
                delCount = db.delete(CommandTable.TABLE, where, whereArg);
                break;*/
            case SENDER_LIST:
                delCount = db.delete(SenderTable.TABLE, where, whereArg);
                break;
            case SENDER_ID:
                id = uri.getLastPathSegment();
                where = TextUtils.isEmpty(where) ? BaseColumns._ID + " = " + id : where + " AND " + BaseColumns._ID + " = " + id;
                delCount = db.delete(SenderTable.TABLE, where, whereArg);
                break;
            default:
                throw new IllegalArgumentException("Wrong URI: " + uri);
        }
        db.execSQL("VACUUM");
        if (delCount > 0) {
            if (getContext() != null) {
                getContext().getContentResolver().notifyChange(uri, null);
            }
        }

        return delCount;
    }

    @Override
    public int update(Uri uri, ContentValues contentValues, String where, String[] whereArg) {
        int updateCount;
        String id;
        switch (TableList.values()[uriMatcher.match(uri)]) {
            case CHECKS_LIST: // общий Uri
                updateCount = db.update(EventsTable.TABLE, contentValues, where, whereArg);
                break;
            case CHECKS_ID:
                id = uri.getLastPathSegment();
                where = TextUtils.isEmpty(where) ? BaseColumns._ID + " = " + id : where + " AND " + BaseColumns._ID + " = " + id;
                updateCount = db.update(EventsTable.TABLE, contentValues, where, whereArg);
                break;
            case SYSTEM_LIST: // общий Uri
                updateCount = db.update(SystemTable.TABLE, contentValues, where, whereArg);
                break;
            case SYSTEM_ID:
                id = uri.getLastPathSegment();
                where = TextUtils.isEmpty(where) ? BaseColumns._ID + " = " + id : where + " AND " + BaseColumns._ID + " = " + id;
                updateCount = db.update(SystemTable.TABLE, contentValues, where, whereArg);
                break;
            case TASK_LIST: // общий Uri
                updateCount = db.update(TaskTable.TABLE, contentValues, where, whereArg);
                break;
            case TASK_ID: // Uri с ID
                id = uri.getLastPathSegment();
                where = TextUtils.isEmpty(where) ? BaseColumns._ID + " = " + id : where + " AND " + BaseColumns._ID + " = " + id;
                updateCount = db.update(TaskTable.TABLE, contentValues, where, whereArg);
                break;
            /*case TYPE_LIST: // общий Uri
                updateCount = db.update(TypeTable.TABLE, contentValues, where, whereArg);
                break;
            case TYPE_ID: // Uri с ID
                id = uri.getLastPathSegment();
                where = TextUtils.isEmpty(where) ? BaseColumns._ID + " = " + id : where + " AND " + BaseColumns._ID + " = " + id;
                updateCount = db.update(TypeTable.TABLE, contentValues, where, whereArg);
                break;

            case ERROR_LIST: // общий Uri
                updateCount = db.update(ErrorTable.TABLE, contentValues, where, whereArg);
                break;
            case ERROR_ID: // Uri с ID
                id = uri.getLastPathSegment();
                where = TextUtils.isEmpty(where) ? BaseColumns._ID + " = " + id : where + " AND " + BaseColumns._ID + " = " + id;
                updateCount = db.update(ErrorTable.TABLE, contentValues, where, whereArg);
                break;
            case COMMAND_LIST: // общий Uri
                updateCount = db.update(CommandTable.TABLE, contentValues, where, whereArg);
                break;
            case COMMAND_ID: // Uri с ID
                id = uri.getLastPathSegment();
                where = TextUtils.isEmpty(where) ? BaseColumns._ID + " = " + id : where + " AND " + BaseColumns._ID + " = " + id;
                updateCount = db.update(CommandTable.TABLE, contentValues, where, whereArg);
                break;*/
            case SENDER_LIST: // общий Uri
                updateCount = db.update(SenderTable.TABLE, contentValues, where, whereArg);
                break;
            case SENDER_ID: // Uri с ID
                id = uri.getLastPathSegment();
                where = TextUtils.isEmpty(where) ? BaseColumns._ID + " = " + id : where + " AND " + BaseColumns._ID + " = " + id;
                updateCount = db.update(SenderTable.TABLE, contentValues, where, whereArg);
                break;
            default:
                throw new IllegalArgumentException("Wrong URI: " + uri);
        }
        if (updateCount > 0) {
            if (getContext() != null) {
                getContext().getContentResolver().notifyChange(uri, null);
            }
        }

        return updateCount;
    }

    private static class DBHelper extends SQLiteOpenHelper {
        final SenderTable senderTable;
        final SystemTable systemTable;

        DBHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
            senderTable = new SenderTable(context);
            systemTable = new SystemTable(context);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL(EventsTable.TABLE_CREATE);
            db.execSQL(SenderTable.TABLE_CREATE);
            db.execSQL(SystemTable.TABLE_CREATE);
            db.execSQL(TaskTable.TABLE_CREATE);
            /*-----------------------------------------------*/
            senderTable.addSystemSheet(db);
            senderTable.addSystemHTTP(db);
            senderTable.addSystemMail(db);
            senderTable.addSystemSms(db);
            /*-----------------------------------------------*/
            systemTable.addDefault(db);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            db.execSQL(DROP_TABLE_IF_EXISTS + EventsTable.TABLE);
            db.execSQL(DROP_TABLE_IF_EXISTS + SystemTable.TABLE);
            db.execSQL(DROP_TABLE_IF_EXISTS + SenderTable.TABLE);
            db.execSQL(DROP_TABLE_IF_EXISTS + TaskTable.TABLE);
            onCreate(db);
        }
    }
}
