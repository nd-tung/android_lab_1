package com.socket.data;

import android.os.Build;
import android.util.Log;
import com.socket.model.MessageObject;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import java.time.Instant;


public class TcpServer {
    private static final String TAG = "TcpServer";

    private ServerSocket serverSocket;
    private final List<ClientHandler> clients = new ArrayList<>();
    private final MessageCallback messageCallback;
    private final ExecutorService clientExecutor = Executors.newFixedThreadPool(10);

    private final AtomicInteger totalDelay = new AtomicInteger(0);
    private final AtomicInteger messageCount = new AtomicInteger(0);

    public TcpServer(MessageCallback messageCallback) {
        this.messageCallback = messageCallback;
    }

    public synchronized long getTotalDelay() {
        return totalDelay.get();
    }

    public synchronized int getMessageCount() {
        return messageCount.get();
    }

    public synchronized long getAverageDelay() {
        return messageCount.get() > 0 ? totalDelay.get() / messageCount.get() : 0;
    }

    public synchronized void resetMetrics() {
        totalDelay.set(0);
        messageCount.set(0);
    }

    public void start(int port) {
        new Thread(() -> {
            try {
                serverSocket = new ServerSocket(port);
                Log.d(TAG, "TCP Server started on port " + port);

                while (!serverSocket.isClosed()) {
                    Socket clientSocket = serverSocket.accept();
                    ClientHandler clientHandler = new ClientHandler(clientSocket);
                    synchronized (clients) {
                        clients.add(clientHandler);
                    }
                    clientExecutor.execute(clientHandler);
                }
            } catch (IOException e) {
                Log.e(TAG, "Error starting TCP server", e);
            }
        }).start();
    }

    public void stop() {
        try {
            if (serverSocket != null && !serverSocket.isClosed()) {
                serverSocket.close();
            }
            synchronized (clients) {
                for (ClientHandler client : clients) {
                    client.closeConnection();
                }
                clients.clear();
            }
            clientExecutor.shutdownNow();
            Log.d(TAG, "TCP Server stopped");
        } catch (IOException e) {
            Log.e(TAG, "Error stopping TCP server", e);
        }
    }

    public void sendMessageToAll(MessageObject message) {
        synchronized (clients) {
            for (ClientHandler client : clients) {
                client.sendMessage(message);
            }
        }
    }

    private synchronized void updateMetrics(int delay) {
        totalDelay.addAndGet(delay);
        messageCount.incrementAndGet();
    }

    private class ClientHandler implements Runnable {
        private final Socket clientSocket;
        private ObjectOutputStream out;
        private ObjectInputStream in;

        public ClientHandler(Socket socket) {
            this.clientSocket = socket;
        }

        @Override
        public void run() {
            try {
                out = new ObjectOutputStream(clientSocket.getOutputStream());
                in = new ObjectInputStream(clientSocket.getInputStream());

                while (!clientSocket.isClosed()) {
                    MessageObject message = (MessageObject) in.readObject();
                    int delay = 0;
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        //make sure to use the same time unit for the delay calculation
                        //when I test between the two devices, I found that there are some chances that time in each device is not sync
                        //so, I use Instant.now() in both devices to calculate the delay
                        delay = (int) (Instant.now().toEpochMilli() - message.getTimestamp());
                    } else {
                        delay = (int) (System.currentTimeMillis() - message.getTimestamp());
                    }
                    updateMetrics(delay);
                    messageCallback.onMessageReceived(message);

                    Log.d(TAG, "Message received. Delay: " + delay + " ms, Average Delay: " + getAverageDelay() + " ms" + ", COUNT: " + getMessageCount());
                }
            } catch (IOException | ClassNotFoundException e) {
                Log.e(TAG, "Error in client handler", e);
            } finally {
                closeConnection();
            }
        }

        public void sendMessage(MessageObject message) {
            try {
                if (out != null) {
                    out.writeObject(message);
                    out.flush();
                }
            } catch (IOException e) {
                Log.e(TAG, "Error sending message to client", e);
            }
        }

        public void closeConnection() {
            try {
                if (clientSocket != null && !clientSocket.isClosed()) {
                    clientSocket.close();
                }
            } catch (IOException e) {
                Log.e(TAG, "Error closing client connection", e);
            } finally {
                synchronized (clients) {
                    clients.remove(this);
                }
            }
        }
    }

    public interface MessageCallback {
        void onMessageReceived(MessageObject message);
    }
}