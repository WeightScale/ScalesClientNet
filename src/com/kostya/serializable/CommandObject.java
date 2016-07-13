package com.kostya.serializable;


import android.content.Context;
import com.kostya.scales_client_net.transferring.ClientProcessor;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.Socket;

/**
 * @author Kostya on 10.07.2016.
 */
public class CommandObject implements Serializable {
    private static final long serialVersionUID = 7526471155622776147L;
    private Commands commandName;
    Object object = null;
    public CommandObject(Commands name, Object o){
        commandName = name;
        object = o;
    }

    public CommandObject(Commands name){
        commandName = name;
    }

    public void execute(Context context){
        if (object == null)
            commandName.fetch(context);
        else
            commandName.setup(context, object);
    }

    public Commands getCommandName() {
        return commandName;
    }

    public void appendObject(Object o){
        object = o;
    }

    public Object readObject(Socket socket) {
        ObjectInputStream objectInputStream = null;
        Object obj = null;
        try{
            objectInputStream = new ObjectInputStream(socket.getInputStream());
            obj = objectInputStream.readObject();
            objectInputStream.close();
        } catch (Exception e) {
            try {objectInputStream.close();} catch (IOException e1) {}
        }finally{
            try {objectInputStream.close();} catch (IOException e1) {}
            return obj;
        }
        //return null;
    }

    public void getObjectFromDeviceInNetwork(final Context context, String ipAddress){
        new Thread(new Runnable() {
            @Override
            public void run() {
                ClientProcessor clientProcessor = new ClientProcessor(ipAddress, context);
                try {
                    Socket socket = clientProcessor.getSocket();
                    ObjectOutputStream objectOutputStream = new ObjectOutputStream(socket.getOutputStream());
                    objectOutputStream.writeObject(CommandObject.this);
                    CommandObject obj = (CommandObject)readObject(socket);
                    obj.execute(context);
                    objectOutputStream.close();
                    socket.close();

                    //clientProcessor.sendObjectOutputInputToDevice(CommandObject.this);
                } catch (Exception e) {
                    clientProcessor.closeSocket();
                }finally{
                    clientProcessor.closeSocket();
                }
            }
        }).start();
    }

}