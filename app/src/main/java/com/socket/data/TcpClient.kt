package com.socket.data

import android.content.ContentValues.TAG
import android.util.Log
import com.socket.model.MessageObject
import java.io.*
import java.net.Socket

class TcpClient(private val serverIp: String, private val serverPort: Int) {
    private var socket: Socket? = null
    private var outputStream: ObjectOutputStream? = null
    private var inputStream: ObjectInputStream? = null

    fun connect() {
        socket = Socket(serverIp, serverPort)
        outputStream = ObjectOutputStream(socket!!.getOutputStream())
        inputStream = ObjectInputStream(socket!!.getInputStream())

        Log.d(TAG, "TCP CONNECTED")
    }

    fun sendMessage(message: MessageObject) {
        outputStream?.writeObject(message)
        outputStream?.flush()
    }

    fun receiveMessage(): MessageObject? {
        return try {
            inputStream?.readObject() as? MessageObject
        } catch (e: Exception) {
            Log.d(TAG, "Error receiving message: ${e.message}")
            e.printStackTrace()
            null
        }
    }

    fun close() {
        outputStream?.close()
        inputStream?.close()
        socket?.close()
    }
}