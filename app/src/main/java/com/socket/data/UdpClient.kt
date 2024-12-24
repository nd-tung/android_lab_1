package com.socket.data

import android.util.Log
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

    // Send MessageObject to UDP server
    fun sendMessage(message: MessageObject) {
        try {
            // Serialize MessageObject thành byte array
            val byteArray = serializeMessage(message)

            // Create packet to send to server
            val packet = DatagramPacket(byteArray, byteArray.size, InetAddress.getByName(serverIp), serverPort)
            socket.send(packet)
            Log.d("UdpClient", "Message sent: ${message.message}")
        } catch (e: Exception) {
            Log.e("UdpClient", "Error sending message: ${e.message}")
            e.printStackTrace()
        }
    }

    // Receive MessageObject from UDP server
    fun receiveMessage(): MessageObject? {
        return try {
            // Create buffer to store received data
            val buffer = ByteArray(1024)
            val packet = DatagramPacket(buffer, buffer.size)

            // Receive packet from server
            socket.receive(packet)

            // Deserialize byte array thành MessageObject
            deserializeMessage(packet.data)
        } catch (e: Exception) {
            Log.e("UdpClient", "Error receiving message: ${e.message}")
            e.printStackTrace()
            null
        }
    }

    // Transform MessageObject to byte array
    private fun serializeMessage(message: MessageObject): ByteArray {
        val byteArrayOutputStream = ByteArrayOutputStream()
        val objectOutputStream = ObjectOutputStream(byteArrayOutputStream)
        objectOutputStream.writeObject(message)
        objectOutputStream.flush()
        return byteArrayOutputStream.toByteArray()
    }

    // Transform byte array to MessageObject
    private fun deserializeMessage(data: ByteArray): MessageObject? {
        return try {
            val byteArrayInputStream = ByteArrayInputStream(data)
            val objectInputStream = ObjectInputStream(byteArrayInputStream)
            objectInputStream.readObject() as? MessageObject
        } catch (e: Exception) {
            Log.e("UdpClient", "Error deserializing message: ${e.message}")
            e.printStackTrace()
            null
        }
    }

    // Close connection
    fun close() {
        socket.close()
    }
}