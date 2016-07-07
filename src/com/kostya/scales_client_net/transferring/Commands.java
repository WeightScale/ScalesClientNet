package com.kostya.scales_client_net.transferring;

/**
 * @author Kostya
 */
public enum Commands {
    /** Имя сети. */
    CMD_SSID_WIFI("SSID", 300),
    /** Ключь сети. */
    CMD_KEY_WIFI("PASS", 300),
    /** Версия. */
    CMD_VERSION("VRS", 300);

    private final String name;
    private final int time;
    private String cmd;
    private static InterfaceCommands interfaceCommands;

    Commands(String n, int t){
        name = n;
        time = t;
    }

    public String toString() { return cmd; }

    /** Получит время timeout комманды.
     * @return Время в милисекундах.  */
    public int getTimeOut(){ return time;}

    /** Получить имя комманды.
     * @return Имя комманды.  */
    public String getName(){return name;}

    /** Выполнить комманду получить данные.
     * @return Данные выполненой комманды. */
    public String getParam(){
        cmd = name;
        return interfaceCommands.command(this);
    }

    /** Выполнить комманду установить данные.
     * @param param Данные для установки.
     * @return true - комманда выполнена.  */
    public boolean setParam(String param){
        cmd = name + param;
        return interfaceCommands.command(this).equals(name);
    }

    /** Выполнить комманду установить данные.
     * @param param Данные для установки.
     * @return true - комманда выполнена.  */
    public boolean setParam(int param){
        cmd = name + param;
        return interfaceCommands.command(this).equals(name);
    }

    public static void setInterfaceCommand(InterfaceCommands i){
        interfaceCommands = i;
    }
}
