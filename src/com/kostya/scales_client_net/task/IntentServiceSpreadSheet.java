package com.kostya.scales_client_net.task;

import android.app.IntentService;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import com.google.android.gms.auth.GoogleAuthException;
import com.google.android.gms.auth.GoogleAuthUtil;
import com.kostya.scales_client_net.Internet;
import com.kostya.scales_client_net.R;
import com.kostya.scales_client_net.provider.EventsTable;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.*;

/**
 * Created by Kostya on 28.06.2016.
 */
public class IntentServiceSpreadSheet extends IntentService {
    private EventsTable eventsTable;
    private String account;
    HashMap<String, ContentValues> mapEvents;
    public static final String EXTRA_ACCOUNT = "com.kostya.scales_client_net.task.EXTRA_ACCOUNT";
    public static final String EXTRA_MAP_EVENTS = "com.kostya.scales_client_net.task.EXTRA_MAP_EVENTS";

    public IntentServiceSpreadSheet(String name) {
        super(name);
        eventsTable = new EventsTable(getApplicationContext());
    }


    @Override
    protected void onHandleIntent(Intent intent) {

        try {
            Bundle bundle = intent.getExtras();
            account = bundle.getString(EXTRA_ACCOUNT);
            mapEvents = (HashMap<String, ContentValues>)intent.getSerializableExtra(EXTRA_MAP_EVENTS);
        }catch (Exception e){}


    }


    /** Отправляем весовой чек Google disk spreadsheet таблицу. */
    public class Spreadsheet extends GoogleSpreadsheets {

        /** Конструктор экземпляра класса Spreadsheet.
         * @param service Имя сервиса SpreadsheetService.
         */
        public Spreadsheet(String service) {
            super(service);
        }

        /** Вызывается когда токен получен. */
        @Override
        protected void tokenIsReceived() {
            Collection<Callable<Integer>> tasks = new ArrayList<>();
            List<Future<Integer>> futures;
            if (!Internet.getConnection(10000, 10)) {
                return;
            }

            for (Map.Entry<String, ContentValues> entry : mapEvents.entrySet()) {
                int eventId = Integer.valueOf(entry.getKey());
                //int checkId = Integer.valueOf(entry.getValue().get(TaskTable.KEY_DOC).toString());
                try {
                    getSheetEntry(eventsTable.TABLE);
                    UpdateListWorksheets();
                    /** Посылаем чек в таблицу на диске. */
                    sendCheckToDisk(eventId);
                    /** Удаляем задачу которую выполнили из map. */
                    mapEvents.remove(String.valueOf(eventId));
                    /** Удаляем задачу из базы. */
                    //eventsTable.removeEntry(taskId);

                } catch (Exception e) {
                    /** Добавляем в контейнер обьект для отправки сообщения.*/

                }
            }

        }

        /** Вызываем если ошибка получения токена. */
        @Override
        protected void tokenIsFalse(String error) {
        }

        /** Вызывается при получении токена.
         * @return Возвращяет полученый токен.
         * @throws IOException
         * @throws GoogleAuthException
         */
        @Override
        protected String fetchToken() throws IOException, GoogleAuthException, IllegalArgumentException {
            if (!Internet.getConnection(10000, 10)) {
                return null;
            }
            return GoogleAuthUtil.getTokenWithNotification(getApplicationContext(), account, "oauth2:" + SCOPE, null, makeCallback());
        }

        /** Выполнить задачу отправки чеков.
         * @param map Контейнер чеков для отправки.
         */
        public void onExecute(final HashMap<String, ContentValues> map) {
            /** Сохраняем контейнер локально. */
            mapEvents = map;
            /** Процесс получения доступа к SpreadsheetService. */
            try {
                spreadsheetService.setAuthSubToken(fetchToken());
                /** Токен получен. */
                tokenIsReceived();
            }catch (IOException | GoogleAuthException e) {
                tokenIsFalse(e.getMessage());
            }catch (Exception e) {

            }
            //super.execute();
        }

        /** Отослать данные чека в таблицу.
         * @param id Индекс чека.
         * @throws Exception Ошибка отправки чека.
         */
        private void sendCheckToDisk(int id) throws Exception {
            Cursor cursor = eventsTable.getEntryItem(id, EventsTable.COLUMNS_SHEET);
            if (cursor == null)
                throw new Exception(getApplicationContext().getString(R.string.Check_N) + id + " null");

            if (cursor.moveToFirst()) {
                addRow(cursor, eventsTable.TABLE);
                eventsTable.updateEntry(id, EventsTable.KEY_STATE, EventsTable.State.CHECK_ON_SERVER.ordinal());
            }
            cursor.close();
        }
    }
}
