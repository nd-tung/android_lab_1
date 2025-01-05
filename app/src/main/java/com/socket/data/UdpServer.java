package com.socket.data;

import android.os.Build;
import android.util.Log;

import com.socket.model.MessageObject;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.time.Instant;

public class UdpServer {
    private static final String TAG = "UdpServer";

    private DatagramSocket socket;
    private MessageListener listener;

    private long totalDelay = 0;
    private int messageCount = 0;
    private volatile boolean isRunning = false;

    public synchronized void resetMetrics() {
        totalDelay = 0;
        messageCount = 0;
    }

    // Interface to send callback when a message is received
    public interface MessageListener {
        void onMessageReceived(MessageObject message);
    }

    public void setMessageListener(MessageListener listener) {
        this.listener = listener;
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

    public void start(int port) {
        isRunning = true;
        new Thread(() -> {
            try {
                socket = new DatagramSocket(port);
                Log.d(TAG, "UDP Server started on port " + port);
                byte[] buffer = new byte[1024];

                while (isRunning) {
                    DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                    try {
                        socket.receive(packet);

                        // Deserialize the message
                        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(packet.getData(), 0, packet.getLength());
                        ObjectInputStream objectInputStream = new ObjectInputStream(byteArrayInputStream);
                        MessageObject receivedMessage = (MessageObject) objectInputStream.readObject();

                        Log.d(TAG, "Received: " + receivedMessage);

                        //calculate delay
                        int delay = 0;
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                            delay = (int) (Instant.now().toEpochMilli() - receivedMessage.getTimestamp());
                        } else {
                            delay = (int) (System.currentTimeMillis() - receivedMessage.getTimestamp());
                        }

                        updateMetrics(delay);
                        Log.d(TAG, "Delay: " + delay + " ms, Average Delay: " + getAverageDelay() + " ms" + ", COUNT: " + getMessageCount());
                        // Call listener callback
                        if (listener != null) {
                            listener.onMessageReceived(receivedMessage);
                        }

                    } catch (IOException | ClassNotFoundException e) {
                        if (isRunning) {
                            Log.e(TAG, "Error receiving or processing packet", e);
                        }
                    }
                }
            } catch (SocketException e) {
                Log.e(TAG, "Socket exception in UDP Server", e);
            } finally {
                stop();
            }
        }).start();
    }

    public void stop() {
        isRunning = false;
        if (socket != null && !socket.isClosed()) {
            socket.close();
            Log.d(TAG, "UDP Server stopped");
        }
    }

    private synchronized void updateMetrics(long delay) {
        totalDelay += delay;
        messageCount++;

    }
}
