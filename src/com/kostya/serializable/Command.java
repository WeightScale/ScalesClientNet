package com.kostya.serializable;

import android.content.Context;
import android.content.Intent;
import com.kostya.scales_client_net.ActivityScales;

/**
 * @author Kostya
 */
public class Command{
    static Context mContext;
    private InterfaceCommands interfaceCommands;

    public interface InterfaceCommands {
        Commands command(CommandObject object);
    }
    public Command(Context context){
        mContext = context;
    }
    public Command(Context context, InterfaceCommands i){
        mContext = context;
        interfaceCommands = i;
    }



    private static Commands contains(String s){
        for(Commands choice : Commands.values())
            if (s.startsWith(choice.name())){
                return choice;
            }
        return null;
    }

    public static Commands execute(String inputLine) {
        try {
            Commands cmd = contains(inputLine);
            String sub = inputLine.replace(cmd.name(), "");
            cmd.prepare("");
            if (sub.isEmpty())
                cmd.prepare(cmd.fetch(mContext));
            else
                cmd.setup(mContext, sub);
            return cmd;
        }catch (NullPointerException e){
            return null;
        }
    }

    /** Выполнить комманду получить данные.
     * @return Данные выполненой комманды. */
   /* public String getData(Commands cmd){
        return interfaceCommands.command(cmd);
    }*/

    /** Выполнить комманду установить данные.
     * @param //data Данные для установки.
     * @return true - комманда выполнена.  */
   /* public boolean setData(Commands cmd, String data){
        cmd.prepare(data);
        return interfaceCommands.command(cmd).equals(cmd.getName());
    }*/

    public boolean sendObject(CommandObject object){
        return interfaceCommands.command(object).equals(object.getCommandName());
    }

    public void setInterfaceCommand(InterfaceCommands i){
        interfaceCommands = i;
    }
}


