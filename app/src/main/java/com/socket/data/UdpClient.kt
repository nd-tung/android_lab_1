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

    fun sendMessage(message: MessageObject) {
        val byteArrayOutputStream = ByteArrayOutputStream()
        val objectOutputStream = ObjectOutputStream(byteArrayOutputStream)
        objectOutputStream.writeObject(message)
        objectOutputStream.flush()
        val data = byteArrayOutputStream.toByteArray()
        val packet = DatagramPacket(data, data.size, InetAddress.getByName(serverIp), serverPort)
        socket.send(packet)
    }

    fun receiveMessage(): MessageObject? {
        val buffer = ByteArray(1024)
        val packet = DatagramPacket(buffer, buffer.size)
        socket.receive(packet)
        val byteArrayInputStream = ByteArrayInputStream(packet.data)
        val objectInputStream = ObjectInputStream(byteArrayInputStream)
        return objectInputStream.readObject() as? MessageObject
    }

    fun close() {
        socket.close()
    }
}