package com.socket.data;

import static android.content.ContentValues.TAG;

import android.util.Log;

import com.socket.model.MessageObject;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;

public class UdpServer {
    private DatagramSocket socket;
    private MessageListener listener;

    // Interface to send callback when a message is received
    public interface MessageListener {
        void onMessageReceived(MessageObject message);
    }

    public void setMessageListener(MessageListener listener) {
        this.listener = listener;
    }

    public void start(int port) {
        new Thread(() -> {
            try {
                socket = new DatagramSocket(port);
                Log.d(TAG, "UDP Server started on port " + port);
                byte[] buffer = new byte[1024];

                while (true) {
                    DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                    socket.receive(packet);

                    ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(packet.getData(), 0, packet.getLength());
                    ObjectInputStream objectInputStream = new ObjectInputStream(byteArrayInputStream);
                    MessageObject receivedMessage = (MessageObject) objectInputStream.readObject();
                    Log.d(TAG, "Received: " + receivedMessage);

                    // Call callback when a message is received
                    if (listener != null) {
                        listener.onMessageReceived(receivedMessage);
                    }
                }
            } catch (IOException | ClassNotFoundException e) {
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