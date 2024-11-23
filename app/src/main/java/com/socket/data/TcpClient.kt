package com.socket.data

import android.content.ContentValues.TAG
import java.io.PrintWriter
import java.net.Socket
import android.util.Log
import java.io.InputStream


class TcpClient(private val serverIp: String, private val serverPort: Int) {
    private var socket: Socket? = null
    private var writer: PrintWriter? = null

    fun connect() {

        socket = Socket(serverIp, serverPort)
        writer = PrintWriter(socket!!.getOutputStream(), true)

        Log.d(TAG, "TCP CONNECTED")
    }

    fun sendMessage(message: String) {
        writer?.println(message)
    }

    fun close() {
        writer?.close()
        socket?.close()
    }

    fun getInputStream(): InputStream? {
        return try {
            socket?.getInputStream()
        } catch (e: Exception) {
            Log.d(TAG, "Error getting input stream: ${e.message}")
            e.printStackTrace()
            null
        }
    }
}