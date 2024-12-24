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
    private ServerSocket serverSocket;
    private final List<ClientHandler> clients = new ArrayList<>();
    private final MessageCallback messageCallback;
    private final ExecutorService clientExecutor = Executors.newCachedThreadPool();

    public TcpServer(MessageCallback messageCallback) {
        this.messageCallback = messageCallback;
    }

    public void start(int port) {
        new Thread(() -> {
            try {
                serverSocket = new ServerSocket(port);
                Log.d(TAG, "TCP Server started on port " + port);

                while (true) {
                    Socket clientSocket = serverSocket.accept();
                    ClientHandler clientHandler = new ClientHandler(clientSocket);
                    clients.add(clientHandler);

                    clientExecutor.execute(clientHandler);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();
    }

    public void stop() throws IOException {
        serverSocket.close();
        clientExecutor.shutdown();
    }

    public void sendMessageToAll(MessageObject message) {
        for (ClientHandler client : clients) {
            client.sendMessage(message);
        }
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
                while (true) {
                    MessageObject message = (MessageObject) in.readObject();
                    messageCallback.onMessageReceived(message);
                }
            } catch (IOException | ClassNotFoundException e) {
                e.printStackTrace();
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
                e.printStackTrace();
            }
        }

        private void closeConnection() {
            try {
                if (clientSocket != null) {
                    clientSocket.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public interface MessageCallback {
        void onMessageReceived(MessageObject message);
    }
}