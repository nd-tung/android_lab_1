package com.socket.data;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;

public class UdpServer {
    private DatagramSocket socket;

    public void start(int port) throws IOException {
        socket = new DatagramSocket(port);
        System.out.println("UDP Server started on port " + port);
        byte[] buffer = new byte[256];
        while (true) {
            DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
            socket.receive(packet);
            System.out.println("Received packet from " + packet.getAddress() + ":" + packet.getPort());
            // Handle packet
        }
    }

    public void stop() {
        socket.close();
    }
}