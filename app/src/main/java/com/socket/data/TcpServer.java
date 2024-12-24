package com.socket.data;

import static android.content.ContentValues.TAG;

import android.util.Log;
import android.widget.Toast;

import com.socket.model.MessageObject;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
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
    private final ExecutorService clientExecutor = Executors.newCachedThreadPool();  // Executor service to handle client threads

    public TcpServer(MessageCallback messageCallback) {
        this.messageCallback = messageCallback;
    }

    public void start(int port) {
        new Thread(() -> {
            try {
                serverSocket = new ServerSocket(port);
                Log.d(TAG, "TCP Server started on port " + port);

                while (true) {
                    // Accept new client connections
                    Socket clientSocket = serverSocket.accept();
                    ClientHandler clientHandler = new ClientHandler(clientSocket);
                    clients.add(clientHandler);
                    // Execute client handler in a separate thread
                    clientExecutor.execute(clientHandler);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();
    }

    public void stop() throws IOException {
        serverSocket.close();
        // Shutdown the ExecutorService when server stops
        clientExecutor.shutdown();
    }

    // Send MessageObject to all connected clients
    public void sendMessageToAll(MessageObject message) {
        for (ClientHandler client : clients) {
            client.sendObject(message);
        }
    }

    // Client handler class to manage individual client connections
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
                // Initialize ObjectOutputStream and ObjectInputStream
                out = new ObjectOutputStream(clientSocket.getOutputStream());
                in = new ObjectInputStream(clientSocket.getInputStream());
                out.flush();  // Ensure data is sent immediately

                while (true) {
                    // Read the object sent by the client
                    Object message = in.readObject();
                    if (message instanceof String) {
                        // Handle String message
                        messageCallback.onMessageReceived((String) message);
                    } else if (message instanceof MessageObject) {
                        // Handle MessageObject
                        messageCallback.onObjectReceived((MessageObject) message);
                    }
                }
            } catch (IOException | ClassNotFoundException e) {
                e.printStackTrace();
            } finally {
                closeConnection();
            }
        }

        // Send an Message Object to the client
        public void sendObject(MessageObject message) {
            try {
                if (out != null) {
                    out.writeObject(message);  // Send the object to the client
                    out.flush();  // Ensure data is sent immediately
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        private void closeConnection() {
            try {
                if (clientSocket != null) {
                    clientSocket.close();  // Close client connection
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    // Callback interface to handle received messages and objects
    public interface MessageCallback {
        void onMessageReceived(String message);
        void onObjectReceived(MessageObject message);
    }
}