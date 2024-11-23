package com.socket.data;

import static android.content.ContentValues.TAG;

import android.util.Log;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;

public class UdpServer {
    private DatagramSocket socket;

    public void start(int port) {
        new Thread(() -> {
            try {
                socket = new DatagramSocket(port);
                Log.d(TAG, "UDP Server started on port " + port);
                byte[] buffer = new byte[256];
                while (true) {
                    DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                    socket.receive(packet);
                    Log.d(TAG, "Received packet from " + packet.getAddress() + ":" + packet.getPort());
                    // Handle packet
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