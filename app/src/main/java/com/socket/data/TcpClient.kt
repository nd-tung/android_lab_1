package com.socket.data

import java.io.PrintWriter
import java.net.Socket

class TcpClient(private val serverIp: String, private val serverPort: Int) {
    private var socket: Socket? = null
    private var writer: PrintWriter? = null

    fun connect() {
        socket = Socket(serverIp, serverPort)
        writer = PrintWriter(socket!!.getOutputStream(), true)
    }

    fun sendMessage(message: String) {
        writer?.println(message)
    }

    fun close() {
        writer?.close()
        socket?.close()
    }
}