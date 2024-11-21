package com.socket.data

import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetAddress

class UdpClient(private val serverIp: String, private val serverPort: Int) {
    private val socket = DatagramSocket()

    fun sendMessage(message: String) {
        val data = message.toByteArray()
        val packet = DatagramPacket(data, data.size, InetAddress.getByName(serverIp), serverPort)
        socket.send(packet)
    }

    fun close() {
        socket.close()
    }
}