package com.socket.data;

import static android.content.ContentValues.TAG;

import android.util.Log;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;

public class UdpServer {
    private DatagramSocket socket;
    private MessageListener listener;

    // Interface để gửi callback khi nhận tin nhắn
    public interface MessageListener {
        void onMessageReceived(String message);
    }

    public void setMessageListener(MessageListener listener) {
        this.listener = listener;
    }

    public void start(int port) {
        new Thread(() -> {
            try {
                socket = new DatagramSocket(port);
                Log.d(TAG, "UDP Server started on port " + port);
                byte[] buffer = new byte[256];

                while (true) {
                    DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                    socket.receive(packet);

                    String receivedMessage = new String(packet.getData(), 0, packet.getLength());
                    Log.d(TAG, "Received: " + receivedMessage);

                    // Gọi callback khi nhận được tin nhắn
                    if (listener != null) {
                        listener.onMessageReceived(receivedMessage);
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();
    }

    public void stop() {
        if (socket != null && !socket.isClosed()) {
            socket.close();
        }
    }
}
