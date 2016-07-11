package com.kostya.serializable;

import android.content.Context;
import android.content.Intent;
import com.kostya.scales_client_net.ActivityScales;
import com.kostya.terminals.Terminals;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

/**
 *  @author Kostya  on 10.07.2016.
 */
public enum Commands implements Serializable {

    /** Версия. */
    CMD_VERSION() {
        @Override
        void setup(Context context, String d) { }

        @Override
        String fetch(Context context) { return null; }
    },
    /** Имя сети. */
    CMD_SSID_WIFI() {
        @Override
        void setup(Context context, String d) {}

        @Override
        String fetch(Context context) {return null;}
    },
    /** Ключь сети. */
    CMD_KEY_WIFI() {
        @Override
        void setup(Context context, String d) { }

        @Override
        String fetch(Context context) { return null; }
    },
    CMD_RECONNECT_SERVER_NET() {
        @Override
        void setup(Context context, String d) { }

        @Override
        String fetch(Context context) {
            return null;
        }
    },
    CMD_OUT_USB() {
        @Override
        void setup(Context context, String d) {
            context.sendBroadcast(new Intent(ActivityScales.WEIGHT).putExtra("weight", d));
        }

        @Override
        String fetch(Context context) {
            return null;
        }
    },
    /** Выбор терминала. */
    CMD_DEFAULT_TERMINAL() {
        @Override
        void setup(Context context, String d) {

        }

        @Override
        String fetch(Context context) {
            return "";
        }
    },
    /** Лист список терминалов. */
    CMD_LIST_TERMINALS() {
        @Override
        void setup(Context context, String d) {

        }

        @Override
        String fetch(Context context) {
            StringBuilder stringBuilder = new StringBuilder();
            for (Terminals terminal : Terminals.values()){
                stringBuilder.append(terminal.name()).append('-').append(terminal.ordinal()).append(' ');
            }
            return stringBuilder.toString();
        }
    };

    private static final long serialVersionUID = 7526471155622776148L;
    private String data;
    private static String command;
    abstract void setup(Context context, String d);
    abstract String fetch(Context context);

    Commands(){ }

    /** Получит время timeout комманды.
     * @return Время в милисекундах.  */
    //public int getTimeOut(){ return time;}

    public void prepare(String data){
        command = name()+data;
    }

    public void execute(Context context){
        if (getData().isEmpty())
            fetch(context);
        else
            setup(context, getData());
    }

    public void setData(String data) {this.data = data;}
    public String getData() {return data;}


}
