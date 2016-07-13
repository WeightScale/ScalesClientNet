package com.kostya.scales_client_net.transferring;

import android.content.Context;
import com.kostya.serializable.CommandObject;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author Kostya on 13.07.2016.
 */
public class ClientThreadProcess implements Runnable {
    private Context context;
    private Socket socket;
    private ObjectOutputStream objectOutputStream;
    private ObjectInputStream objectInputStream;
    private ExecutorService executorService;
    private String ipAddress;
    private static final int TIME_OUT_CONNECT = 2000; /** Время в милисекундах. */

    public ClientThreadProcess(Context context, String ipAddress) {
        this.context = context;
        this.ipAddress = ipAddress;
        executorService = Executors.newCachedThreadPool();
    }

    @Override
    public void run() {
        while (!Thread.currentThread().isInterrupted()){
            try {
                socket =  getSocket();
                objectOutputStream = new ObjectOutputStream(socket.getOutputStream());
                objectInputStream = new ObjectInputStream(socket.getInputStream());
                processInputStream();

            } catch (Exception e) {
                closeSocket();
            }finally{
                closeSocket();
            }
        }
    }

    private void processInputStream() throws IOException, ClassNotFoundException {
        while (!Thread.currentThread().isInterrupted()){
            CommandObject object = (CommandObject)objectInputStream.readObject();
            executorService.execute(new Runnable() {
                @Override
                public void run() {
                    if (object !=null){
                        object.execute(context);
                    }
                }
            });
        }
    }

    void writeObject(Object o) throws IOException {
        objectOutputStream.writeObject(o);
    }

    public Socket getSocket() throws Exception {
        InetAddress serverAddress = InetAddress.getByName(ipAddress);
        socket = new Socket();
        socket.connect(new InetSocketAddress(serverAddress, ServerSocketProcessorRunnable.SERVER_PORT), TIME_OUT_CONNECT);
        return socket;
    }

    public void closeSocket() {
        try {objectOutputStream.close();} catch (IOException e) {}
        try {objectInputStream.close();} catch (IOException e) {}
        try {
            if (socket != null && !socket.isClosed())
                socket.close();
        } catch (IOException e) {
        }
    }
}
