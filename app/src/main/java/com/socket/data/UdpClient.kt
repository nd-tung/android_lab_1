package com.socket.data

import com.socket.model.MessageObject
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.ObjectInputStream
import java.io.ObjectOutputStream
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetAddress

class UdpClient(private val serverIp: String, private val serverPort: Int) {
    private val socket = DatagramSocket()

    @Synchronized
    fun sendMessage(message: MessageObject) {
        val byteArrayOutputStream = ByteArrayOutputStream()
        val objectOutputStream = ObjectOutputStream(byteArrayOutputStream)
        objectOutputStream.writeObject(message)
        objectOutputStream.flush()
        val data = byteArrayOutputStream.toByteArray()
        val packet = DatagramPacket(data, data.size, InetAddress.getByName(serverIp), serverPort)
        socket.send(packet)
    }

    fun close() {
        socket.close()
    }
}