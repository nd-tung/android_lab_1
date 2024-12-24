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
import java.net.SocketException;

public class UdpServer {
    private DatagramSocket socket;
    private MessageListener listener;

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
                byte[] buffer = new byte[1024]; // Create a buffer to hold received data

                while (true) {
                    DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                    socket.receive(packet);

                    // Deserialize byte array to MessageObject
                    MessageObject receivedMessage = deserializeMessage(packet.getData());

                    if (receivedMessage != null) {
                        Log.d(TAG, "Received: " + receivedMessage.getMessage());
                        // Call the listener to notify that a message has been received
                        if (listener != null) {
                            listener.onMessageReceived(receivedMessage);
                        }
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

    // Deserialize byte array to MessageObject
    private MessageObject deserializeMessage(byte[] data) {
        try (ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(data);
             ObjectInputStream objectInputStream = new ObjectInputStream(byteArrayInputStream)) {
            return (MessageObject) objectInputStream.readObject();
        } catch (Exception e) {
            Log.e(TAG, "Error deserializing message: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    // Serialize MessageObject into byte array
    public static byte[] serializeMessage(MessageObject message) {
        try (ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
             ObjectOutputStream objectOutputStream = new ObjectOutputStream(byteArrayOutputStream)) {
            objectOutputStream.writeObject(message);
            objectOutputStream.flush();
            return byteArrayOutputStream.toByteArray();
        } catch (IOException e) {
            Log.e(TAG, "Error serializing message: " + e.getMessage());
            e.printStackTrace();
            return new byte[0]; // return empty byte array
        }
    }
}
