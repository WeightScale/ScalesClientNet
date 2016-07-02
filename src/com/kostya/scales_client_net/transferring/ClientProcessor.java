package com.kostya.scales_client_net.transferring;

import android.content.Context;
import android.util.Log;

import java.io.*;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;

public class ClientProcessor {
    private Socket socket;
    private String textForSend;
    private final Context context;
    private final String serverIpAddress;
    private static  final String TAG = "ClientProcess";

    public ClientProcessor(String textForSend, String serverIpAddress, Context context) {
        this.textForSend = textForSend;
        this.context = context;
        this.serverIpAddress = serverIpAddress;

    }

    public ClientProcessor(String serverIpAddress, Context context) {
        this.context = context;
        this.serverIpAddress = serverIpAddress;
    }

    private Socket getSocket(String serverIpAddress) {
        try {
            InetAddress serverAddress = InetAddress.getByName(serverIpAddress);
            socket = new Socket();
            socket.connect(new InetSocketAddress(serverAddress, ServerSocketProcessorRunnable.SERVER_PORT), 500);

            //socket = new Socket(serverAddress, ServerSocketProcessorRunnable.SERVER_PORT);
        } catch (IOException e1) {
            e1.printStackTrace();
        }
        return socket;
    }

    private void closeSocket(Socket socket) {
        try {
            if (socket != null && !socket.isClosed())
                socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    ///////////////////////////////////////////////////////////////////////////

    public void sendTextToOtherDevice() {
        try {
            socket = getSocket(serverIpAddress);

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
            closeSocket(socket);
        }
    }

    public void sendSimpleMessageToOtherDevice(String message) {
        try {
            socket = getSocket(serverIpAddress);

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
            closeSocket(socket);
        }
    }


}
