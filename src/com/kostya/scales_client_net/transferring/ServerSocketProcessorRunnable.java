package com.kostya.scales_client_net.transferring;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import com.kostya.scales_client_net.ActivityScales;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;

public class ServerSocketProcessorRunnable implements Runnable {

    public static final int SERVER_PORT = 8700;//8700
    private ServerSocket serverSocket;
    private final Context context;
    private BufferedReader inputBufferedReader;
    private PrintWriter outputPrintWriter;


    private static final String TAG = "SERVER_SOCKET";

    public ServerSocketProcessorRunnable(ServerSocket serverSocket, Context context) {
        this.serverSocket = serverSocket;
        this.context = context;
    }

    @Override
    public void run() {
        try {
            serverSocket = new ServerSocket();
            serverSocket.setReuseAddress(true);
            serverSocket.bind(new InetSocketAddress(SERVER_PORT));
            //serverSocket = new ServerSocket(SERVER_PORT);

        } catch (IOException e) {
            e.printStackTrace();
        }


        try {
            while (!Thread.currentThread().isInterrupted()) {

                Log.v(TAG, "before socket ACCEPT");
                Socket socket = serverSocket.accept();
                Log.v(TAG, "ACCEPTED");

                InputStream inputStream = socket.getInputStream();
                InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
                inputBufferedReader = new BufferedReader(inputStreamReader);

//                OutputStreamWriter outputStreamWriter = new OutputStreamWriter(socket.getOutputStream());
//                BufferedWriter bufferedWriter = new BufferedWriter(outputStreamWriter);
//                PrintWriter printWriter = new PrintWriter(bufferedWriter, true);
//                printWriter.println("some info");

                //outputPrintWriter = new PrintWriter(new BufferedWriter(new OutputStreamWriter(socket.getOutputStream())), true);
                processInputInputOutputBuffers();
            }

            inputBufferedReader.close();
            //outputPrintWriter.close();

            Log.v(TAG, "BUFFERS CLOSED");

        } catch (Exception ex) {
            Log.v(TAG, "server socket processor thread EXCEPTION : " + ex);

        } catch (Error error){
            Log.v(TAG, "server socket processor thread ERROR : " + error);
        }

    }


    private void processInputInputOutputBuffers() throws IOException {

        Log.v(TAG, "...SOCKET DATA PROCESSING...");

        String inputLine = inputBufferedReader.readLine();
        if (inputLine != null){
            context.sendBroadcast(new Intent(ActivityScales.WEIGHT).putExtra("weight", inputLine));
            //sendNotifySubText(inputLine);
            Log.d(TAG, "Received message : " + inputLine);
            //outputPrintWriter.println("YOU TEXT ARRIVED. THANKS");
        }
    }

}
