package com.kostya.scales_client_net.transferring;

import android.content.Context;

import java.io.IOException;
import java.net.ServerSocket;

public class JmDnsServerThreadProcessor {

    //private final ServerSocket serverSocket = null;
    private Thread serverProcessorThread;
    ServerSocketProcessorRunnable serverSocketProcessor;

    public void startServerProcessorThread(Context context) {
        serverSocketProcessor = new ServerSocketProcessorRunnable(context);
        serverProcessorThread = new Thread(serverSocketProcessor);
        serverProcessorThread.start();
    }

    public void stopServerProcessorThread() {

        try {
            // make sure you close the socket upon exiting
            serverSocketProcessor.closedSocket();

            if (serverProcessorThread != null)
                serverProcessorThread.interrupt();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
