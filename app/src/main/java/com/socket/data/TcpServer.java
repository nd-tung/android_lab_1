package com.socket.data;

import static android.content.ContentValues.TAG;

import android.util.Log;
import android.widget.Toast;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class TcpServer {
    private ServerSocket serverSocket;
    private final List<ClientHandler> clients = new ArrayList<>();
    private final MessageCallback messageCallback;
    private final ExecutorService clientExecutor = Executors.newCachedThreadPool(); // Sử dụng ExecutorService

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

                    // Use ExecutorService to run ClientHandler
                    clientExecutor.execute(clientHandler);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();
    }

    public void stop() throws IOException {
        serverSocket.close();

        //Stop ExecutorService when server stops
        clientExecutor.shutdown();
    }

    public void sendMessageToAll(String message) {
        for (ClientHandler client : clients) {
            client.sendMessage(message);
        }
    }

    private class ClientHandler implements Runnable {
        private final Socket clientSocket;
        private PrintWriter out;
        private Scanner in;

        public ClientHandler(Socket socket) {
            this.clientSocket = socket;
        }

        @Override
        public void run() {
            try {
                out = new PrintWriter(clientSocket.getOutputStream(), true);
                in = new Scanner(clientSocket.getInputStream());
                while (in.hasNextLine()) {
                    String message = in.nextLine();
                    messageCallback.onMessageReceived(message);
                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                closeConnection();
            }
        }

        public void sendMessage(String message) {
            if (out != null) {
                out.println(message);
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
        void onMessageReceived(String message);
    }

}