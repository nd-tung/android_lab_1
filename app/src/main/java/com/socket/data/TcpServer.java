package com.socket.data;

import static android.content.ContentValues.TAG;

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

public class TcpServer {
    private static final String TAG = "TcpServer";

    private ServerSocket serverSocket;
    private final List<ClientHandler> clients = new ArrayList<>();
    private final MessageCallback messageCallback;
    private final ExecutorService clientExecutor = Executors.newFixedThreadPool(10);

    private long totalDelay = 0;
    private int messageCount = 0;

    public TcpServer(MessageCallback messageCallback) {
        this.messageCallback = messageCallback;
    }

    public synchronized long getTotalDelay() {
        return totalDelay;
    }

    public synchronized int getMessageCount() {
        return messageCount;
    }

    public synchronized long getAverageDelay() {
        return messageCount > 0 ? totalDelay / messageCount : 0;
    }

    public void resetMetrics() {
        totalDelay = 0;
        messageCount = 0;
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

    private synchronized void updateMetrics(long delay) {
        totalDelay += delay;
        messageCount++;
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
                    long delay = System.currentTimeMillis() - message.getTimestamp();
                    updateMetrics(delay);
                    messageCallback.onMessageReceived(message);

                    Log.d(TAG, "Message received. Delay: " + delay + " ms, Average Delay: " + getAverageDelay() + " ms");
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



//public class TcpServer {
//    private ServerSocket serverSocket;
//    private final List<ClientHandler> clients = new ArrayList<>();
//    private final MessageCallback messageCallback;
//    private final ExecutorService clientExecutor = Executors.newCachedThreadPool();
//    private long totalDelay = 0;
//    private int messageCount = 0;
//
//    public long getTotalDelay() {
//        return totalDelay;
//    }
//
//    public int getMessageCount() {
//        return messageCount;
//    }
//
//    public long getAverageDelay() {
//        return messageCount > 0 ? totalDelay / messageCount : 0;
//    }
//
//
//    private synchronized void updateMetrics(long delay) {
//        totalDelay += delay;
//        messageCount++;
//    }
//
//    public TcpServer(MessageCallback messageCallback) {
//        this.messageCallback = messageCallback;
//    }
//
//    public void start(int port) {
//        new Thread(() -> {
//            try {
//                serverSocket = new ServerSocket(port);
//                Log.d(TAG, "TCP Server started on port " + port);
//
//                while (true) {
//                    Socket clientSocket = serverSocket.accept();
//                    ClientHandler clientHandler = new ClientHandler(clientSocket);
//                    clients.add(clientHandler);
//
//                    clientExecutor.execute(clientHandler);
//                }
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//        }).start();
//    }
//
//    public void stop() throws IOException {
//        serverSocket.close();
//        clientExecutor.shutdown();
//    }
//
//    public void sendMessageToAll(MessageObject message) {
//        for (ClientHandler client : clients) {
//            client.sendMessage(message);
//        }
//    }
//
//    private class ClientHandler implements Runnable {
//        private final Socket clientSocket;
//        private ObjectOutputStream out;
//        private ObjectInputStream in;
//
//        public ClientHandler(Socket socket) {
//            this.clientSocket = socket;
//        }
//
//        @Override
//        public void run() {
//            try {
//                out = new ObjectOutputStream(clientSocket.getOutputStream());
//                in = new ObjectInputStream(clientSocket.getInputStream());
//                while (true) {
//                    MessageObject message = (MessageObject) in.readObject();
//                    messageCallback.onMessageReceived(message);
//
//                    // Calculate delay
//                    long delay = System.currentTimeMillis() - message.getTimestamp();
//                    updateMetrics(delay);
//                    Log.d(TAG, "Delay: " + delay + " ms, Average Delay: " + averageDelay + " ms");
//                }
//            } catch (IOException | ClassNotFoundException e) {
//                e.printStackTrace();
//            } finally {
//                closeConnection();
//            }
//        }
//
//        public void sendMessage(MessageObject message) {
//            try {
//                if (out != null) {
//                    out.writeObject(message);
//                    out.flush();
//                }
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//        }
//
//        private void closeConnection() {
//            try {
//                if (clientSocket != null) {
//                    clientSocket.close();
//                }
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//        }
//    }
//
//    public interface MessageCallback {
//        void onMessageReceived(MessageObject message);
//    }
//}