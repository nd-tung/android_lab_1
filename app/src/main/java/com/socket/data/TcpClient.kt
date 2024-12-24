package com.socket.data

import android.content.ContentValues.TAG
import java.io.PrintWriter
import java.net.Socket
import android.util.Log
import com.socket.model.MessageObject
import java.io.InputStream
import java.io.ObjectInputStream
import java.io.ObjectOutputStream
import kotlinx.coroutines.*

class TcpClient(private val serverIp: String, private val serverPort: Int) {
    private var socket: Socket? = null
    private var outStream: ObjectOutputStream? = null
    private var inStream: ObjectInputStream? = null
    private var listeningJob: Job? = null

    // Connect to server
    fun connect() {
        socket = Socket(serverIp, serverPort)
        outStream = ObjectOutputStream(socket!!.getOutputStream()) // Send object
        inStream = ObjectInputStream(socket!!.getInputStream())  // Receive object

        Log.d(TAG, "TCP CONNECTED")
        startListening()
    }

    // Send MessageObject
    fun sendMessage(message: MessageObject) {
        try {
            outStream?.writeObject(message) // Send MessageObject
            outStream?.flush() // Make sure the message is sent
            Log.d(TAG, "Message sent: ${message.message}")
        } catch (e: Exception) {
            Log.e(TAG, "Error sending message: ${e.message}")
            e.printStackTrace()
        }
    }

    // Read MessageObject
    fun readMessage(): MessageObject? {
        return try {
            val message = inStream?.readObject() as? MessageObject // Read MessageObject
            Log.d(TAG, "Message received: ${message?.message}")
            message
        } catch (e: Exception) {
            Log.e(TAG, "Error reading message: ${e.message}")
            e.printStackTrace()
            null
        }
    }

    // Start listening for incoming messages
    private fun startListening() {
        listeningJob = CoroutineScope(Dispatchers.IO).launch {
            try {
                while (true) {
                    val message = readMessage()
                    if (message != null) {
                        Log.d(TAG, "Message received: ${message.message}")
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error in listening: ${e.message}")
                e.printStackTrace()
            }
        }
    }

    // Close connection
    fun close() {
        try {
            listeningJob?.cancel()
            outStream?.close()
            inStream?.close()
            socket?.close()
            Log.d(TAG, "TCP CONNECTION CLOSED")
        } catch (e: Exception) {
            Log.e(TAG, "Error closing connection: ${e.message}")
            e.printStackTrace()
        }
    }

    // Take input stream
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