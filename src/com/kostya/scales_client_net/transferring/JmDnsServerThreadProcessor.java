package com.kostya.scales_client_net.transferring;

import android.content.Context;

import java.io.IOException;
import java.net.ServerSocket;

public class JmDnsServerThreadProcessor {

    private final ServerSocket serverSocket = null;
    private Thread serverProcessorThread;

    public void startServerProcessorThread(Context context) {
        ServerSocketProcessorRunnable serverSocketProcessor = new ServerSocketProcessorRunnable(serverSocket, context);
        serverProcessorThread = new Thread(serverSocketProcessor);
        serverProcessorThread.start();
    }

    public void stopServerProcessorThread() {

        try {
            // make sure you close the socket upon exiting
            if (serverSocket != null)
                serverSocket.close();

            if (serverProcessorThread != null)
                serverProcessorThread.interrupt();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
