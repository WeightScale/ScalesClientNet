package com.kostya.scales_client_net.transferring;

import android.content.Context;
import android.util.Log;
import com.kostya.scales_client_net.Globals;
import com.kostya.serializable.Command;
import com.kostya.serializable.CommandObject;
import com.kostya.serializable.Commands;
import com.kostya.terminals.TerminalObject;
import com.kostya.terminals.Terminals;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ServerSocketProcessorRunnable implements Runnable {

    public static final int SERVER_PORT = 8700;//8700
    private ServerSocket serverSocket;
    private ExecutorService executorService;
    private final Context context;
    private BufferedReader inputBufferedReader;
    private ObjectInputStream objectInputStream;
    private PrintWriter outputPrintWriter;


    private static final String TAG = "SERVER_SOCKET";

    public ServerSocketProcessorRunnable(Context context) {

        this.context = context;
        executorService = Executors.newCachedThreadPool();
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
                Socket socket = serverSocket.accept();

//                OutputStreamWriter outputStreamWriter = new OutputStreamWriter(socket.getOutputStream());
//                BufferedWriter bufferedWriter = new BufferedWriter(outputStreamWriter);
//                PrintWriter printWriter = new PrintWriter(bufferedWriter, true);
//                printWriter.println("some info");

                //outputPrintWriter = new PrintWriter(new BufferedWriter(new OutputStreamWriter(socket.getOutputStream())), true);
                //processInputInputOutputBuffers(socket);
                processInputInputOutputObject(socket);
            }

            //objectInputStream.close();
            //inputBufferedReader.close();
            //outputPrintWriter.close();
        } catch (Exception ex) {}
        try {serverSocket.close();} catch (IOException e) {}
    }

    private void processInputInputOutputBuffers(Socket socket){
        try {
            inputBufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            String inputLine = inputBufferedReader.readLine();
            if (inputLine != null){

                Command command = new Command(context);
                /** Выполняем принятую команду. */
                command.execute(inputLine);
                //sendNotifySubText(inputLine);
                //Log.d(TAG, "Received message : " + inputLine);
                //outputPrintWriter.println("YOU TEXT ARRIVED. THANKS");
            }
        } catch (Exception e) {}
        try {inputBufferedReader.close();} catch (IOException e1) {}
    }

    private void processInputInputOutputObject(Socket socket) {
        try {
            objectInputStream = new ObjectInputStream(socket.getInputStream());
            Object object = objectInputStream.readObject();
            executorService.execute(new Runnable() {
                @Override
                public void run() {
                    if (object !=null){
                        ((CommandObject)object).execute(context);
                    }
                }
            });

        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
        }
        try {objectInputStream.close();} catch (IOException e1) {}
    }

    public void closedSocket() throws IOException {
        if (serverSocket != null)
            serverSocket.close();
        if (inputBufferedReader != null)
            inputBufferedReader.close();
    }

}
