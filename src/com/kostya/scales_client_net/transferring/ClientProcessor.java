package com.kostya.scales_client_net.transferring;

import android.content.Context;
import android.util.Log;
import com.kostya.serializable.CommandObject;

import java.io.*;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;

public class ClientProcessor {
    private Socket socket;
    private String textForSend;
    private final Context context;
    private final String ipAddress;
    private static final int TIME_OUT_CONNECT = 2000; /** Время в милисекундах. */
    private static  final String TAG = "ClientProcess";

    public ClientProcessor(String textForSend, String ipAddress, Context context) {
        this(ipAddress,context);
        sendSimpleMessageToOtherDevice(textForSend);
    }

    public ClientProcessor(String ipAddress, Context context) {
        this.context = context;
        this.ipAddress = ipAddress;
    }

    public ClientProcessor(Object object, String ipAddress, Context context) {
        this(ipAddress,context);
        sendSimpleObjectToOtherDevice(object);
    }

    public Socket getSocket() throws Exception {
        InetAddress serverAddress = InetAddress.getByName(ipAddress);
        socket = new Socket();
        socket.connect(new InetSocketAddress(serverAddress, ServerSocketProcessorRunnable.SERVER_PORT), TIME_OUT_CONNECT);
        return socket;
    }

    public void closeSocket() {
        try {
            if (socket != null && !socket.isClosed())
                socket.close();
        } catch (IOException e) {
        }
    }


    ///////////////////////////////////////////////////////////////////////////

    public void sendTextToOtherDevice() {
        try {
            socket = getSocket();

            PrintWriter output = new PrintWriter(new BufferedWriter(new OutputStreamWriter(socket.getOutputStream())), true);
            output.println("MESSAGE FROM CLIENT");

            BufferedReader input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            String message = input.readLine();

            Log.i(TAG,"Client received : " + message);

            input.close();
            output.close();
            socket.close();

        } catch (Exception e) {
            e.printStackTrace();
            Log.i(TAG,e.getMessage());

        } finally {
            closeSocket();
        }
    }

    public void sendSimpleMessageToOtherDevice(String message) {
        try {
            socket = getSocket();

            PrintWriter output = new PrintWriter(new BufferedWriter(new OutputStreamWriter(socket.getOutputStream())), true);
            output.println(message);
            output.flush();

            //BufferedReader input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            //String messageFromClient = input.readLine();
            //Log.i(TAG,"Received answer : " + messageFromClient);

            output.close();
            socket.close();

        } catch (Exception e) {
            e.printStackTrace();
            Log.i(TAG,e.getMessage());

        } finally {
            closeSocket();
        }
    }

    public void sendSimpleObjectToOtherDevice(Object object) {
        try {
            socket = getSocket();

            ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
            out.writeObject(object);

            out.close();
            socket.close();

        } catch (Exception e) {
            //// TODO: 09.07.2016
            Log.i(TAG,e.getMessage());

        } finally {
            closeSocket();
        }
    }

    public void sendObjectOutputInputToDevice(Object object) {
        try {
            socket = getSocket();

            ObjectOutputStream objectOutputStream = new ObjectOutputStream(socket.getOutputStream());
            objectOutputStream.writeObject(object);

            //ObjectInputStream objectInputStream = new ObjectInputStream(socket.getInputStream());
            Object obj = ((CommandObject)object).readObject(socket);
            ((CommandObject)obj).execute(context);
            //objectInputStream.close();
            objectOutputStream.close();
            socket.close();

        } catch (Exception e) {
            //// TODO: 09.07.2016
            Log.i(TAG,e.getMessage());

        } finally {
            closeSocket();
        }
    }
}
